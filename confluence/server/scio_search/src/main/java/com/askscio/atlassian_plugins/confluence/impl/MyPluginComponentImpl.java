package com.askscio.atlassian_plugins.confluence.impl;

import com.askscio.atlassian_plugins.confluence.api.MyPluginComponent;
import com.atlassian.sal.api.ApplicationProperties;

public class MyPluginComponentImpl implements MyPluginComponent {

  public static final String TARGET_CONFIG_KEY =
      "com.askscio.atlassian_plugins.confluence.targetURL";
  public static final String PLUGIN_STATUS_KEY = "com.askscio.atlassian_plugins.confluence.status";
  public static final String PLUGIN_STATUS_LAST_UPDATED_KEY =
      "com.askscio.atlassian_plugins.confluence.status.lastUpdated";
  private final ApplicationProperties applicationProperties;

  public MyPluginComponentImpl(final ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  public String getName() {
    if (null != applicationProperties) {
      return "myComponent:" + applicationProperties.getDisplayName();
    }

    return "myComponent";
  }
}
