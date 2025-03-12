package eu.jnpmarques;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFPubSubSourceConnectorTask extends SourceTask {
  static final Logger log = LoggerFactory.getLogger(SFPubSubSourceConnectorTask.class);

  private RecordFactory<String, String> recordFactory;

  private Map<String, RecordPoller<String, String>> pollerMap;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> configs) {
    // Implement and initialize the recordFactory
    this.recordFactory = new SFPubSubRecordFactory();

    // Create a RecordPoller for each topic
    this.pollerMap = new HashMap<String, RecordPoller<String, String>>();
    for (String split : configs.get(SFPubSubSourceSourceConnectorConfig.TOPIC_MAP_CONFIG).split(",")) {
      String[] splitArray = split.split(":");
      if (splitArray.length != 2) {
        throw new RuntimeException("Invalid topic map configuration");
      }
      String pubSubTopic = splitArray[0];
      String kafkaTopic = splitArray[1];
      Map<String, String> topicConfigs = configs.keySet().stream()
          .collect(Collectors.toMap(e -> e, configs::get));
      topicConfigs.put(SalesforcePubsubPoller.PUBSUB_TOPIC_CONFIG, pubSubTopic);
      topicConfigs.put(SalesforcePubsubPoller.KAFKA_TOPIC_CONFIG, kafkaTopic);

      // Read Current Offsets from the OffsetReader
      var offsetMap = this.recordFactory.getOffsetMap(getOffsetStorageReader(), kafkaTopic);
      // Create new Poller
      pollerMap.put(kafkaTopic,
          new SalesforcePubsubPoller(topicConfigs, offsetMap.get(SFPubSubRecordFactory.SOURCE_OFFSET)));
    }
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    log.info("Polling new Records!");

    List<SourceRecord> records = new ArrayList<>();
    for (var entry : this.pollerMap.entrySet()) {
      log.info("Polling new Records for topic " + entry.getKey());
      var data = entry.getValue().pollRecords();
      for (var inputRecord : data) {
        records.add(this.recordFactory.createSourceRecord(inputRecord, entry.getKey()));
      }
      log.info("Polled " + data.size() + " new records for topic " + entry.getKey());
    }
    return records;
  }

  @Override
  public void stop() {
    this.recordFactory.stop();
    for(RecordPoller rp : this.pollerMap.values()){
      rp.stop();
    }
  }

  /***
   * 
   * @return
   *         the reader class that provides access to offsets previously stored
   *         by the connector
   *         if the connector hasn't run before, this could return null
   */
  private OffsetStorageReader getOffsetStorageReader() {
    if (context == null) {
      log.debug("No context - assuming that this is the first time the Connector has run");
      return null;
    } else if (context.offsetStorageReader() == null) {
      log.debug("No offset reader - assuming that this is the first time the Connector has run");
      return null;
    } else {
      return context.offsetStorageReader();
    }
  }
}