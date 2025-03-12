package eu.jnpmarques;

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

public class SFPubSubSourceSourceConnectorConfig extends AbstractConfig {

  public static final String USERNAME_CONFIG = "username";
  public static final String USERNAME_DOC = "username";
  public static final String PASSWORD_CONFIG = "password";
  public static final String PASSWORD_DOC = "password";
  public static final String LOGIN_URL_CONFIG = "login_url";
  public static final String LOGIN_URL_DOC = "login_url";
  public static final String TENANT_ID_CONFIG = "tenant_id";
  public static final String TENANT_ID_DOC = "tenant_id";
  public static final String ACCESS_TOKEN_CONFIG = "access_token";
  public static final String ACCESS_TOKEN_DOC = "access_token";
  public static final String PUB_SUB_HOST_CONFIG = "pubsub_host";
  public static final String PUB_SUB_HOST_DOC = "pubsub_host";
  public static final String PUB_SUB_PORT_CONFIG = "pubsub_port";
  public static final String PUB_SUB_PORT_DOC = "pubsub_port";
  public static final String TOPIC_MAP_CONFIG = "topics_map";
  public static final String TOPIC_MAP_DOC = "topics_map";
  public static final String NUMBER_OF_EVENTS_IN_FETCH_REQUEST_CONFIG = "number_of_events_in_fetch_request";
  public static final String NUMBER_OF_EVENTS_IN_FETCH_REQUEST_DOC = "number_of_events_in_fetch_request";
  public static final String PLAINTEXT_CHANNEL_CONFIG = "use_plaintext_channel";
  public static final String PLAINTEXT_CHANNEL_DOC = "use_plaintext_channel";
  public static final String PROVIDED_LOGIN_URL_CONFIG = "use_provided_login_url";
  public static final String PROVIDED_LOGIN_URL_DOC = "use_provided_login_url";
  public static final String REPLAY_PRESET_CONFIG = "replay_preset";
  public static final String REPLAY_PRESET_DOC = "replay_preset";
  public static final String REPLAY_ID_CONFIG = "replay_id";
  public static final String REPLAY_ID_DOC = "replay_id";

  public SFPubSubSourceSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
    super(config, parsedConfig);
  }

  public SFPubSubSourceSourceConnectorConfig(Map<String, String> parsedConfig) {
    this(conf(), parsedConfig);
  }

  public static ConfigDef conf() {
    return new ConfigDef()
        .define(USERNAME_CONFIG, Type.STRING, Importance.HIGH, USERNAME_DOC)
        .define(PASSWORD_CONFIG, Type.STRING, Importance.HIGH, PASSWORD_DOC)
        .define(LOGIN_URL_CONFIG, Type.STRING, Importance.HIGH, LOGIN_URL_DOC)
        .define(TENANT_ID_CONFIG, Type.STRING, null, Importance.LOW, TENANT_ID_DOC)
        .define(ACCESS_TOKEN_CONFIG, Type.STRING, null, Importance.HIGH, ACCESS_TOKEN_DOC)
        .define(PUB_SUB_HOST_CONFIG, Type.STRING, Importance.HIGH, PUB_SUB_HOST_DOC)
        .define(PUB_SUB_PORT_CONFIG, Type.INT, Importance.HIGH, PUB_SUB_PORT_DOC)
        .define(TOPIC_MAP_CONFIG, Type.LIST, null, Importance.HIGH, TOPIC_MAP_DOC)
        .define(NUMBER_OF_EVENTS_IN_FETCH_REQUEST_CONFIG, Type.INT, 5, Importance.HIGH,
            NUMBER_OF_EVENTS_IN_FETCH_REQUEST_DOC)
        .define(PLAINTEXT_CHANNEL_CONFIG, Type.BOOLEAN, false, Importance.HIGH, PLAINTEXT_CHANNEL_DOC)
        .define(PROVIDED_LOGIN_URL_CONFIG, Type.BOOLEAN, false, Importance.HIGH, PROVIDED_LOGIN_URL_DOC)
        .define(REPLAY_PRESET_CONFIG, Type.STRING, "LATEST", Importance.HIGH, REPLAY_PRESET_DOC)
        .define(REPLAY_ID_CONFIG, Type.STRING, "null", Importance.HIGH, REPLAY_ID_DOC);
  }

  public String getUsername() {
    return this.getString(USERNAME_CONFIG);
  }

  public String getPassword() {
    return this.getString(PASSWORD_CONFIG);
  }

  public String getLoginUrl() {
    return this.getString(LOGIN_URL_CONFIG);
  }

  public String getTenantId() {
    return this.getString(TENANT_ID_CONFIG);
  }

  public String getAccessToken() {
    return this.getString(ACCESS_TOKEN_CONFIG);
  }

  public String getPubSubHost() {
    return this.getString(PUB_SUB_HOST_CONFIG);
  }

  public int getPubSubPort() {
    return this.getInt(PUB_SUB_PORT_CONFIG);
  }

  public List<String> getTopicMap() {
    return this.getList(TOPIC_MAP_CONFIG);
  }

  public int getNumberOfEventsInFetchRequest() {
    return this.getInt(NUMBER_OF_EVENTS_IN_FETCH_REQUEST_CONFIG);
  }

  public boolean usePlainChannel() {
    return this.getBoolean(PLAINTEXT_CHANNEL_CONFIG);
  }

  public boolean useProvidedLoginUrl() {
    return this.getBoolean(PROVIDED_LOGIN_URL_CONFIG);
  }

  public String getReplayPresetConfig() {
    return this.getString(REPLAY_PRESET_CONFIG);
  }

  public String getReplayId() {
    return this.getString(REPLAY_ID_CONFIG);
  }

}
