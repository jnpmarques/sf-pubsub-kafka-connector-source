package eu.jnpmarques.utility;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.protobuf.ByteString;
import com.salesforce.eventbus.protobuf.*;

import io.grpc.*;

import static eu.jnpmarques.utility.EventParser.getFieldListFromBitmap;

/**
 * The CommonContext class provides a list of member variables and functions that is used across
 * all examples for various purposes like setting up the HttpClient, CallCredentials, stubs for
 * sending requests, generating events etc.
 */
public class CommonContext implements AutoCloseable {

    protected static final Logger logger = LoggerFactory.getLogger(CommonContext.class.getClass());

    protected final ManagedChannel channel;
    protected final PubSubGrpc.PubSubStub asyncStub;
    protected final PubSubGrpc.PubSubBlockingStub blockingStub;

    protected final HttpClient httpClient;
    protected final SessionTokenService sessionTokenService;
    protected final CallCredentials callCredentials;

    protected String tenantGuid;
    protected String busTopicName;
    protected TopicInfo topicInfo;
    protected SchemaInfo schemaInfo;
    protected String sessionToken;

    public CommonContext(final Configuration options) {
        String grpcHost = options.getPubsubHost();
        int grpcPort = options.getPubsubPort();
        logger.info("Using grpcHost {} and grpcPort {}", grpcHost, grpcPort);

        if (options.usePlaintextChannel()) {
            channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort).usePlaintext().build();
        } else {
            channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort).build();
        }

        httpClient = setupHttpClient();
        sessionTokenService = new SessionTokenService(httpClient);

        callCredentials = setupCallCredentials(options);
        sessionToken = ((APISessionCredentials) callCredentials).getToken();

        Channel interceptedChannel = ClientInterceptors.intercept(channel, new XClientTraceIdClientInterceptor());

        asyncStub = PubSubGrpc.newStub(interceptedChannel).withCallCredentials(callCredentials);
        blockingStub = PubSubGrpc.newBlockingStub(interceptedChannel).withCallCredentials(callCredentials);
    }

    /**
     * Helper function to setup the HttpClient used for sending requests.
     */
    private HttpClient setupHttpClient() {
        HttpClient httpClient = new HttpClient();
        Map<String, String> env = System.getenv();

        String httpProxy = env.get("HTTP_PROXY");
        if (httpProxy != null) {
            String[] httpProxyParts = httpProxy.split(":");
            httpClient.getProxyConfiguration().getProxies()
                    .add(new HttpProxy(httpProxyParts[0], Integer.parseInt(httpProxyParts[1])));
        }

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("cannot create HTTP client", e);
        }
        return httpClient;
    }

    /**
     * Helper function to setup the CallCredentials of the requests.
     *
     * @param options Command line arguments passed.
     * @return CallCredentials
     */
    public CallCredentials setupCallCredentials(Configuration options) {
        if (options.getAccessToken() != null) {
            try {
                return sessionTokenService.loginWithAccessToken(options.getLoginUrl(),
                        options.getAccessToken(), options.getTenantId());
            } catch (Exception e) {
                close();
                throw new IllegalArgumentException("cannot log in with access token", e);
            }
        } else if (options.getUsername() != null && options.getPassword() != null) {
            try {
                return sessionTokenService.login(options.getLoginUrl(),
                        options.getUsername(), options.getPassword(), options.useProvidedLoginUrl());
            } catch (Exception e) {
                close();
                throw new IllegalArgumentException("cannot log in with username/password", e);
            }
        } else {
            logger.warn("Please use either username/password or session token for authentication");
            close();
            return null;
        }
    }

    /**
     * Helper function to setup the topic details in the PublishUnary, PublishStream and
     * SubscribeStream examples. Function also checks whether the topic under consideration
     * can publish or subscribe.
     *
     * @param topicName name of the topic
     * @param pubOrSubMode publish mode if true, subscribe mode if false
     * @param fetchSchema specify whether schema info has to be fetched
     */
    protected void setupTopicDetails(final String topicName, final boolean pubOrSubMode, final boolean fetchSchema) {
        if (topicName != null && !topicName.isEmpty()) {
            try {
                topicInfo = blockingStub.getTopic(TopicRequest.newBuilder().setTopicName(topicName).build());
                tenantGuid = topicInfo.getTenantGuid();
                busTopicName = topicInfo.getTopicName();

                if (pubOrSubMode && !topicInfo.getCanPublish()) {
                    throw new IllegalArgumentException(
                            "Topic " + topicInfo.getTopicName() + " is not available for publish");
                }

                if (!pubOrSubMode && !topicInfo.getCanSubscribe()) {
                    throw new IllegalArgumentException(
                            "Topic " + topicInfo.getTopicName() + " is not available for subscribe");
                }

                if (fetchSchema) {
                    SchemaRequest schemaRequest = SchemaRequest.newBuilder().setSchemaId(topicInfo.getSchemaId())
                            .build();
                    schemaInfo = blockingStub.getSchema(schemaRequest);
                }
            } catch (final Exception ex) {
                logger.error("Error during fetching topic", ex);
                close();
                throw ex;
            }
        }
    }

    /**
     * Helper function to convert the replayId in long to ByteString type.
     *
     * @param replayValue value of the replayId in long
     * @return ByteString value of the replayId
     */
    public static ByteString getReplayIdFromLong(long replayValue) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(replayValue);
        buffer.flip();

        return ByteString.copyFrom(buffer);
    }


    /**
     * Helper function to print the gRPC exception and trailers while a
     * StatusRuntimeException is caught
     *
     * @param context
     * @param e
     */
    public static final void printStatusRuntimeException(final String context, final Exception e) {
        logger.error(context);

        if (e instanceof StatusRuntimeException) {
            final StatusRuntimeException expected = (StatusRuntimeException)e;
            logger.error(" === GRPC Exception ===", e);
            Metadata trailers = ((StatusRuntimeException)e).getTrailers();
            logger.error(" === Trailers ===");
            trailers.keys().stream().forEach(t -> {
                logger.error("[Trailer] = " + t + " [Value] = "
                        + trailers.get(Metadata.Key.of(t, Metadata.ASCII_STRING_MARSHALLER)));
            });
        } else {
            logger.error(" === Exception ===", e);
        }
    }

    /**
     * Helper function to deserialize the event payload received in bytes.
     *
     * @param schema
     * @param payload
     * @return
     * @throws IOException
     */
    public static GenericRecord deserialize(Schema schema, ByteString payload) throws IOException {
        DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);
        ByteArrayInputStream in = new ByteArrayInputStream(payload.toByteArray());
        BinaryDecoder decoder = DecoderFactory.get().directBinaryDecoder(in, null);
        return reader.read(null, decoder);
    }

    /**
     * Helper function to process and print bitmap fields
     *
     * @param schema
     * @param record
     * @param bitmapField
     * @return
     */
    public static void processAndPrintBitmapFields(Schema schema, GenericRecord record, String bitmapField) {
        String bitmapFieldPascal = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, bitmapField);
        try {
            List<String> changedFields = getFieldListFromBitmap(schema,
                    (GenericData.Record) record.get("ChangeEventHeader"), bitmapField);
            if (!changedFields.isEmpty()) {
                logger.info("============================");
                logger.info("       " + bitmapFieldPascal + "       ");
                logger.info("============================");
                for (String field : changedFields) {
                    logger.info(field);
                }
                logger.info("============================\n");
            } else {
                logger.info("No " + bitmapFieldPascal + " found\n");
            }
        } catch (Exception e) {
            logger.info("Trying to process " + bitmapFieldPascal + " on unsupported events or no " +
                    bitmapFieldPascal + " found. Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * Helper function to setup Subscribe configurations in some examples.
     *
     * @param requiredParams
     * @param topic
     * @return
     */
    public static Configuration setupSubscriberParameters(Configuration requiredParams, String topic, int numberOfEvents) {
        Configuration subParams = new Configuration();
        setCommonParameters(subParams, requiredParams);
        subParams.setTopic(topic);
        subParams.setReplayPreset(ReplayPreset.LATEST);
        subParams.setNumberOfEventsToSubscribeInEachFetchRequest(numberOfEvents);
        return subParams;
    }

    /**
     * Helper function to setup common configurations for publish and subscribe operations.
     *
     * @param ep
     * @param requiredParams
     */
    private static void setCommonParameters(Configuration ep, Configuration requiredParams) {
        ep.setLoginUrl(requiredParams.getLoginUrl());
        ep.setPubsubHost(requiredParams.getPubsubHost());
        ep.setPubsubPort(requiredParams.getPubsubPort());
        if (requiredParams.getUsername() != null && requiredParams.getPassword() != null) {
            ep.setUsername(requiredParams.getUsername());
            ep.setPassword(requiredParams.getPassword());
        } else {
            ep.setAccessToken(requiredParams.getAccessToken());
            ep.setTenantId(requiredParams.getTenantId());
        }
        ep.setPlaintextChannel(requiredParams.usePlaintextChannel());
    }

    /**
     * Implementation of the close() function from AutoCloseable interface for relinquishing the
     * resources used in the try-with-resource blocks in the examples and the resources used
     * in this class.
     */
    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Throwable t) {
                logger.warn("Cannot stop session HTTP client", t);
            }
        }

        try {
            channel.shutdown().awaitTermination(20, TimeUnit.SECONDS);
        } catch (Throwable t) {
            logger.warn("Cannot shutdown GRPC channel", t);
        }
    }
}