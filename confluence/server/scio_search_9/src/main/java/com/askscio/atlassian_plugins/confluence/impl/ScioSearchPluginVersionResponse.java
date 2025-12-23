package com.askscio.atlassian_plugins.confluence.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchPluginVersionResponse {
  private final String version;

  ScioSearchPluginVersionResponse(String version) {
    this.version = version;
  }
}
