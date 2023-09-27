package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.cache.Cache;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ScioWebhookTask implements Runnable {

  private static final Logger.Log logger = Logger.getInstance(ScioWebhookTask.class);

  private final String target;
  private final String visitedUrl;
  private final String user;
  private final PluginSettings pluginSettings;
  private final Cache<String, Optional<String>> pluginSettingsCache;

  public ScioWebhookTask(String target, String visitedUrl, String user,
      PluginSettings pluginSettings, Cache<String, Optional<String>> pluginSettingsCache) {
    this.target = target;
    this.visitedUrl = visitedUrl;
    this.user = user;
    this.pluginSettings = pluginSettings;
    this.pluginSettingsCache = pluginSettingsCache;
  }

  @Override
  public void run() {
    try {
      final URL url = new URL(target);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-type", "application/json; charset=utf-8");
      connection.setDoOutput(true);
      connection.setConnectTimeout(60);
      connection.setReadTimeout(60);
      final OutputStream output = connection.getOutputStream();
      final String json = String.format("{\"url\":\"%s\",\"user\":\"%s\"}", visitedUrl, user);
      final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      output.write(bytes);
      int responseCode = connection.getResponseCode();
      try {
        Utils.updatePluginStatus(pluginSettingsCache, pluginSettings, responseCode);
      } catch (Exception e) {
        logger.warn(String.format("Exception while updating plugin status: %s", e));
      }
      logger.debug(String.format("Webhook sent %d", responseCode));
    } catch (Exception e) {
      logger.warn(String.format("Failed to send Scio webhook: %s", e));
    }
  }
}
