package com.askscio.atlassian_plugins.confluence.impl;

import com.askscio.atlassian_plugins.confluence.api.MyPluginComponent;
import com.atlassian.sal.api.ApplicationProperties;

public class MyPluginComponentImpl implements MyPluginComponent {
  public static final String TARGET_CONFIG_KEY =
      "com.askscio.atlassian_plugins.confluence.targetURL";
  public static final String LAST_WEBHOOK_RESPONSE_TIME_KEY = "com.askscio.atlassian_plugins.confluence.lastWebhookResponseTime";
  public static final String LAST_WEBHOOK_RESPONSE_CODE_KEY = "com.askscio.atlassian_plugins.confluence.lastWebhookResponseCode";
  public static final String LAST_WEBHOOK_SUCCESS_TIME_KEY = "com.askscio.atlassian_plugins.confluence.lastWebhookSuccessTime";
  public static final String LAST_WEBHOOK_FAILURE_TIME_KEY = "com.askscio.atlassian_plugins.confluence.lastWebhookFailureTime";

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
