package eu.jnpmarques;

import java.util.Base64;
import java.util.LinkedList;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.salesforce.eventbus.protobuf.ConsumerEvent;
import com.salesforce.eventbus.protobuf.FetchResponse;
import com.salesforce.eventbus.protobuf.ReplayPreset;

import io.grpc.stub.StreamObserver;
import eu.jnpmarques.genericpubsub.Subscribe;
import eu.jnpmarques.utility.CommonContext;
import eu.jnpmarques.utility.Configuration;

public class SalesforcePubsubPoller implements RecordPoller<String, String> {
    public static final String KAFKA_TOPIC_CONFIG = "kafkaTopic";
    public static final String PUBSUB_TOPIC_CONFIG = "pubSubTopic";

    static final Logger log = LoggerFactory.getLogger(SalesforcePubsubPoller.class);

    private final LinkedList<DataPoint<String, String>> streamRecords;
    protected Subscribe subscriber;

    SalesforcePubsubPoller(Map<String, String> configs, String lastOffset) {
        this.streamRecords = new LinkedList<>();
        log.info("Setting up the Subscriber");
        Configuration subscriberParams = new Configuration(configs);
        if (lastOffset != null && !lastOffset.isEmpty()) {
            subscriberParams.setReplayPreset(ReplayPreset.CUSTOM);
            subscriberParams.setReplayId(string2ByteString(lastOffset));
        }
        this.subscriber = new Subscribe(subscriberParams, getAccountListenerResponseObserver());
        this.subscriber.startSubscription();
    }

    private String byteString2String(ByteString byteString) {
        return Base64.getEncoder().encodeToString(byteString.toByteArray());
    }

    private ByteString string2ByteString(String string) {
        return ByteString.copyFrom(Base64.getDecoder().decode(string));
    }

    /**
     * Custom StreamObserver for the AccountListener.
     *
     * @return StreamObserver<FetchResponse>
     */
    private StreamObserver<FetchResponse> getAccountListenerResponseObserver() {
        return new StreamObserver<FetchResponse>() {
            @Override
            public void onNext(FetchResponse fetchResponse) {
                for (ConsumerEvent ce : fetchResponse.getEventsList()) {
                    try {
                        Schema writerSchema = subscriber.getSchema(ce.getEvent().getSchemaId());
                        GenericRecord eventPayload = CommonContext.deserialize(writerSchema,
                                ce.getEvent().getPayload());
                        subscriber.updateReceivedEvents(1);
                        String payload = eventPayload.toString();

                        DataPoint<String, String> dataPoint = new DataPoint<>(payload,
                                byteString2String(ce.getReplayId()));
                        synchronized (this) {
                            streamRecords.addLast(dataPoint);
                        }
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                }
                if (fetchResponse.getPendingNumRequested() == 0) {
                    subscriber.fetchMore(subscriber.getBatchSize());
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error during SubscribeStream", (Exception) t);
                subscriber.isActive.set(false);
            }

            @Override
            public void onCompleted() {
                log.info("Received requested number of events! Call completed by server.");
                subscriber.isActive.set(false);
            }
        };
    }

    @Override
    public LinkedList<DataPoint<String, String>> pollRecords() {
        log.debug("Polling Records");
        LinkedList<DataPoint<String, String>> results = new LinkedList<>();
        synchronized (this) {
            while (this.streamRecords.size() > 0) {
                results.addLast(this.streamRecords.pop());
            }
        }
        return results;
    }

    @Override
    public void setConfigs(Map configs) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOffsetMaps(Map offsetMap) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stop() {
        this.subscriber.close();

    }
}
