package eu.jnpmarques;

import org.junit.Test;

public class MySourceConnectorConfigTest {
  @Test
  public void doc() {
    System.out.println(SFPubSubSourceSourceConnectorConfig.conf().toRst());
  }
}