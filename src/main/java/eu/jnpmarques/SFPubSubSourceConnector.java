package eu.jnpmarques;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFPubSubSourceConnector extends SourceConnector {
  private static final Logger log = LoggerFactory.getLogger(SFPubSubSourceConnector.class);
  private SFPubSubSourceSourceConnectorConfig config;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> map) {
    config = new SFPubSubSourceSourceConnectorConfig(map);
    log.info("Starting Connector...");
  }

  @Override
  public Class<? extends Task> taskClass() {
    return SFPubSubSourceConnectorTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    if (maxTasks > 1) {
      log.warn("Only one task is supported. Ignoring tasks.max which is set to {}", maxTasks);
    }
    List<Map<String, String>> taskConfigs = new ArrayList<>(1);
    taskConfigs.add(config.originalsStrings());
    return taskConfigs;
  }

  @Override
  public void stop() {
    // TODO: Do things that are necessary to stop your connector.
  }

  @Override
  public ConfigDef config() {
    return SFPubSubSourceSourceConnectorConfig.conf();
  }
}
