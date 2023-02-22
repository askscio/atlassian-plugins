package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.PLUGIN_STATUS_KEY;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.gson.Gson;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

public class Utils {

  private static final Gson gson = new Gson();

  public static boolean isCurrentUserAdmin(UserManager userManager) {
    final UserProfile profile = userManager.getRemoteUser();
    return profile != null && userManager.isSystemAdmin(profile.getUserKey());
  }

  public static void validateUserIsAdmin(UserManager userManager) {
    if (!isCurrentUserAdmin(userManager)) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  public static PluginStatus getPluginStatus(PluginSettings pluginSettings) {
    if (pluginSettings.get(PLUGIN_STATUS_KEY) == null) {
      return new PluginStatus();
    }
    return gson.fromJson((String) pluginSettings.get(PLUGIN_STATUS_KEY), PluginStatus.class);
  }

  public static void updatePluginStatus(PluginSettings pluginSettings, int responseCode) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(
        ZoneId.from(ZoneOffset.UTC));
    String currentTime = formatter.format(Instant.now());
    PluginStatus pluginStatus = getPluginStatus(pluginSettings);
    pluginStatus.lastWebhookResponseTime = currentTime;
    pluginStatus.lastWebhookResponseCode = String.valueOf(responseCode);
    if (responseCode >= 200 & responseCode < 300) {
      pluginStatus.lastWebhookSuccessTime = currentTime;
    } else if (responseCode >= 400) {
      pluginStatus.lastWebhookFailureTime = currentTime;
    }
    pluginSettings.put(PLUGIN_STATUS_KEY, gson.toJson(pluginStatus));
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class PluginStatus {

    public String lastWebhookResponseTime;
    public String lastWebhookResponseCode;
    public String lastWebhookSuccessTime;
    public String lastWebhookFailureTime;

    public PluginStatus() {
      lastWebhookResponseTime = "Never fired";
      lastWebhookResponseCode = "Never fired";
      lastWebhookSuccessTime = "Never succeeded";
      lastWebhookFailureTime = "Never failed";
    }
  }
}
