package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.PLUGIN_STATUS_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.PLUGIN_STATUS_LAST_UPDATED_KEY;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.gson.Gson;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

public class Utils {

  private static final Logger.Log logger = Logger.getInstance(Utils.class);
  private static final Gson gson = new Gson();
  private static final int PLUGIN_STATUS_UPDATE_INTERVAL_SECONDS = 60; // in seconds
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(
      ZoneId.from(ZoneOffset.UTC));

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
    String pluginStatusStr = (String) pluginSettings.get(PLUGIN_STATUS_KEY);
    if (pluginStatusStr == null) {
      return new PluginStatus();
    }
    return gson.fromJson(pluginStatusStr, PluginStatus.class);
  }

  public static void updatePluginStatus(Cache<String, Optional<String>> pluginSettingsCache,
      PluginSettings pluginSettings,
      int responseCode) {
    String pluginStatusStr = pluginSettingsCache.get(PLUGIN_STATUS_KEY).orElse(null);
    PluginStatus pluginStatus = pluginStatusStr == null ? new PluginStatus() : gson.fromJson(
        pluginStatusStr, PluginStatus.class);
    String currentTimeStr = formatter.format(Instant.now());
    pluginStatus.lastWebhookResponseTime = currentTimeStr;
    pluginStatus.lastWebhookResponseCode = String.valueOf(responseCode);
    if (responseCode >= 200 & responseCode < 300) {
      pluginStatus.lastWebhookSuccessTime = currentTimeStr;
    } else if (responseCode >= 400) {
      pluginStatus.lastWebhookFailureTime = currentTimeStr;
    }
    pluginSettingsCache.put(PLUGIN_STATUS_KEY, Optional.of(gson.toJson(pluginStatus)));
    persistInPluginSettingsIfNecessary(pluginSettingsCache, pluginSettings, pluginStatus);
  }

  private static void persistInPluginSettingsIfNecessary(Cache<String, Optional<String>> cache,
      PluginSettings pluginSettings,
      PluginStatus pluginStatus) {
    String pluginStatusLastUpdatedStr = cache.get(PLUGIN_STATUS_LAST_UPDATED_KEY).orElse(null);
    Instant pluginStatusLastUpdated =
        pluginStatusLastUpdatedStr == null ? Instant.EPOCH : Instant.parse(
            pluginStatusLastUpdatedStr);
    Instant now = Instant.now();
    long secondsSinceLastUpdate = pluginStatusLastUpdated.until(now, ChronoUnit.SECONDS);
    if (secondsSinceLastUpdate > PLUGIN_STATUS_UPDATE_INTERVAL_SECONDS) {
      cache.put(PLUGIN_STATUS_LAST_UPDATED_KEY, Optional.of(now.toString()));
      pluginSettings.put(PLUGIN_STATUS_KEY, gson.toJson(pluginStatus));
    }
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class PluginStatus implements Serializable {

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

  public static class PluginSettingsCacheLoader implements CacheLoader<String, Optional<String>> {

    private final PluginSettings pluginSettings;

    PluginSettingsCacheLoader(PluginSettings pluginSettings) {
      this.pluginSettings = pluginSettings;
    }

    @Override
    public Optional<String> load(String key) {
      return Optional.ofNullable((String) pluginSettings.get(key));
    }
  }
}
