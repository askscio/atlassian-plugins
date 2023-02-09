package com.askscio.atlassian_plugins.confluence.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchPluginVersionResponse {
  private final String version;

  ScioSearchPluginVersionResponse(String version) {
    this.version = version;
  }
}
