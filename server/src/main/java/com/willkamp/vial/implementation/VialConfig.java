package com.willkamp.vial.implementation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

@Getter
class VialConfig {
  private final int port;
  private final String address;
  private final boolean useTls;
  private final int maxContentLength;

  VialConfig() {
    Config config = ConfigFactory.load().getConfig("vial");
    port = config.getInt("port");
    address = config.getString("address");
    useTls = config.getBoolean("useTls");
    maxContentLength = config.getInt("maxContentLength");
  }
}
