package eu.jnpmarques.utility;

import java.util.Map;

import com.google.protobuf.ByteString;
import com.salesforce.eventbus.protobuf.ReplayPreset;

import eu.jnpmarques.SFPubSubSourceSourceConnectorConfig;
import eu.jnpmarques.SalesforcePubsubPoller;

/**
 * The ExampleConfigurations class is used for setting up the configurations for
 * running the examples.
 * The configurations can be read from a YAML file or created directly via an
 * object. It also sets
 * default values when an optional configuration is not specified.
 */
public class Configuration {
    private String username;
    private String password;
    private String loginUrl;
    private String tenantId;
    private String accessToken;
    private String pubsubHost;
    private Integer pubsubPort;
    private String topic;
    private Integer numberOfEventsToPublish;
    private Boolean singlePublishRequest;
    private Integer numberOfEventsToSubscribeInEachFetchRequest;
    private Boolean processChangedFields;
    private Boolean plaintextChannel;
    private Boolean providedLoginUrl;
    private ReplayPreset replayPreset;
    private ByteString replayId;
    private String managedSubscriptionId;
    private String developerName;

    public Configuration() {
        this.username = null;
        this.password = null;
        this.loginUrl = null;
        this.tenantId = null;
        this.accessToken = null;
        this.pubsubHost = null;
        this.pubsubPort = null;
        this.topic = null;
        this.singlePublishRequest = false;
        this.numberOfEventsToPublish = 5;
        this.numberOfEventsToSubscribeInEachFetchRequest = 5;
        this.processChangedFields = false;
        this.plaintextChannel = false;
        this.providedLoginUrl = false;
        this.replayPreset = ReplayPreset.LATEST;
        this.replayId = null;
        this.developerName = null;
        this.managedSubscriptionId = null;
    }

    public Configuration(Map<String, String> configs) {
        // Reading Required Parameters
        this.loginUrl = configs.get(SFPubSubSourceSourceConnectorConfig.LOGIN_URL_CONFIG).toString();
        this.pubsubHost = configs.get(SFPubSubSourceSourceConnectorConfig.PUB_SUB_HOST_CONFIG).toString();
        this.pubsubPort = Integer
                .parseInt(configs.get(SFPubSubSourceSourceConnectorConfig.PUB_SUB_PORT_CONFIG).toString());

        // Reading Optional Parameters
        this.username = configs.get(SFPubSubSourceSourceConnectorConfig.USERNAME_CONFIG) == null ? null
                : configs.get(SFPubSubSourceSourceConnectorConfig.USERNAME_CONFIG).toString();
        this.password = configs.get(SFPubSubSourceSourceConnectorConfig.PASSWORD_CONFIG) == null ? null
                : configs.get(SFPubSubSourceSourceConnectorConfig.PASSWORD_CONFIG).toString();
        this.topic = configs.get(SalesforcePubsubPoller.PUBSUB_TOPIC_CONFIG) == null ? "/event/Order_Event__e"
                : configs.get(SalesforcePubsubPoller.PUBSUB_TOPIC_CONFIG).toString();
        this.tenantId = configs.get(SFPubSubSourceSourceConnectorConfig.TENANT_ID_CONFIG) == null ? null
                : configs.get(SFPubSubSourceSourceConnectorConfig.TENANT_ID_CONFIG).toString();
        this.accessToken = configs.get(SFPubSubSourceSourceConnectorConfig.ACCESS_TOKEN_CONFIG) == null ? null
                : configs.get(SFPubSubSourceSourceConnectorConfig.ACCESS_TOKEN_CONFIG).toString();
        this.numberOfEventsToSubscribeInEachFetchRequest = configs
                .get(SFPubSubSourceSourceConnectorConfig.NUMBER_OF_EVENTS_IN_FETCH_REQUEST_CONFIG) == null ? 5
                        : Integer.parseInt(configs
                                .get(SFPubSubSourceSourceConnectorConfig.NUMBER_OF_EVENTS_IN_FETCH_REQUEST_CONFIG)
                                .toString());
        this.plaintextChannel = configs.get(SFPubSubSourceSourceConnectorConfig.PLAINTEXT_CHANNEL_CONFIG) != null
                && Boolean.parseBoolean(
                        configs.get(SFPubSubSourceSourceConnectorConfig.PLAINTEXT_CHANNEL_CONFIG).toString());
        this.providedLoginUrl = configs.get(SFPubSubSourceSourceConnectorConfig.PROVIDED_LOGIN_URL_CONFIG) != null
                && Boolean.parseBoolean(
                        configs.get(SFPubSubSourceSourceConnectorConfig.PROVIDED_LOGIN_URL_CONFIG).toString());

        this.processChangedFields = configs.get("PROCESS_CHANGE_EVENT_HEADER_FIELDS") == null ? false
                : Boolean.parseBoolean(configs.get("PROCESS_CHANGE_EVENT_HEADER_FIELDS").toString());

        if (configs.get(SFPubSubSourceSourceConnectorConfig.REPLAY_PRESET_CONFIG) != null) {
            if (configs.get(SFPubSubSourceSourceConnectorConfig.REPLAY_PRESET_CONFIG).toString().equals("EARLIEST")) {
                this.replayPreset = ReplayPreset.EARLIEST;
            } else if (configs.get(SFPubSubSourceSourceConnectorConfig.REPLAY_PRESET_CONFIG).toString()
                    .equals("CUSTOM")) {
                this.replayPreset = ReplayPreset.CUSTOM;
                this.replayId = getByteStringFromReplayIdInputString(
                        configs.get(SFPubSubSourceSourceConnectorConfig.REPLAY_ID_CONFIG).toString());
            } else {
                this.replayPreset = ReplayPreset.LATEST;
            }
        } else {
            this.replayPreset = ReplayPreset.LATEST;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPubsubHost() {
        return pubsubHost;
    }

    public void setPubsubHost(String pubsubHost) {
        this.pubsubHost = pubsubHost;
    }

    public int getPubsubPort() {
        return pubsubPort;
    }

    public void setPubsubPort(int pubsubPort) {
        this.pubsubPort = pubsubPort;
    }

    public Integer getNumberOfEventsToPublish() {
        return numberOfEventsToPublish;
    }

    public void setNumberOfEventsToPublish(Integer numberOfEventsToPublish) {
        this.numberOfEventsToPublish = numberOfEventsToPublish;
    }

    public Boolean getSinglePublishRequest() {
        return singlePublishRequest;
    }

    public void setSinglePublishRequest(Boolean singlePublishRequest) {
        this.singlePublishRequest = singlePublishRequest;
    }

    public int getNumberOfEventsToSubscribeInEachFetchRequest() {
        return numberOfEventsToSubscribeInEachFetchRequest;
    }

    public void setNumberOfEventsToSubscribeInEachFetchRequest(int numberOfEventsToSubscribeInEachFetchRequest) {
        this.numberOfEventsToSubscribeInEachFetchRequest = numberOfEventsToSubscribeInEachFetchRequest;
    }

    public Boolean getProcessChangedFields() {
        return processChangedFields;
    }

    public void setProcessChangedFields(Boolean processChangedFields) {
        this.processChangedFields = processChangedFields;
    }

    public boolean usePlaintextChannel() {
        return plaintextChannel;
    }

    public void setPlaintextChannel(boolean plaintextChannel) {
        this.plaintextChannel = plaintextChannel;
    }

    public Boolean useProvidedLoginUrl() {
        return providedLoginUrl;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setProvidedLoginUrl(Boolean providedLoginUrl) {
        this.providedLoginUrl = providedLoginUrl;
    }

    public ReplayPreset getReplayPreset() {
        return replayPreset;
    }

    public void setReplayPreset(ReplayPreset replayPreset) {
        this.replayPreset = replayPreset;
    }

    public ByteString getReplayId() {
        return replayId;
    }

    public void setReplayId(ByteString replayId) {
        this.replayId = replayId;
    }

    public String getManagedSubscriptionId() {
        return managedSubscriptionId;
    }

    public void setManagedSubscriptionId(String managedSubscriptionId) {
        this.managedSubscriptionId = managedSubscriptionId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    /**
     * NOTE: replayIds are meant to be opaque (See docs:
     * https://developer.salesforce.com/docs/platform/pub-sub-api/guide/intro.html)
     * and this is used for example purposes only. A long-lived subscription client
     * will use the stored replay to
     * resubscribe on failure. The stored replay should be in bytes and not in any
     * other form.
     */
    public ByteString getByteStringFromReplayIdInputString(String input) {
        ByteString replayId;
        String[] values = input.substring(1, input.length() - 2).split(",");
        byte[] b = new byte[values.length];
        int i = 0;
        for (String x : values) {
            if (x.strip().length() != 0) {
                b[i++] = (byte) Integer.parseInt(x.strip());
            }
        }
        replayId = ByteString.copyFrom(b);
        return replayId;
    }
}
