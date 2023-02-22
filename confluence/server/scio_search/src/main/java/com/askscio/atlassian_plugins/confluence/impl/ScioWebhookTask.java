package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_FAILURE_TIME_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_RESPONSE_CODE_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_RESPONSE_TIME_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_SUCCESS_TIME_KEY;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ScioWebhookTask implements Runnable {

  private static final Logger.Log logger = Logger.getInstance(ScioWebhookTask.class);

  private final String target;
  private final String visitedUrl;
  private final String user;
  private final PluginSettings pluginSettings;

  public ScioWebhookTask(String target, String visitedUrl, String user,
      PluginSettings pluginSettings) {
    this.target = target;
    this.visitedUrl = visitedUrl;
    this.user = user;
    this.pluginSettings = pluginSettings;
  }

  @Override
  public void run() {
    try {
      final URL url = new URL(target);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-type", "application/json; charset=utf-8");
      connection.setDoOutput(true);
      final OutputStream output = connection.getOutputStream();
      final String json = String.format("{\"url\":\"%s\",\"user\":\"%s\"}", visitedUrl, user);
      final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      output.write(bytes);
      int responseCode = connection.getResponseCode();
      updatePluginStatus(responseCode);
      logger.debug(String.format("Webhook sent %d", responseCode));
    } catch (Exception e) {
      logger.warn(String.format("Failed to send Scio webhook: %s", e));
    }
  }

  private void updatePluginStatus(int responseCode) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(
        ZoneId.from(ZoneOffset.UTC));
    String currentTime = formatter.format(Instant.now());
    pluginSettings.put(LAST_WEBHOOK_RESPONSE_TIME_KEY, currentTime);
    pluginSettings.put(LAST_WEBHOOK_RESPONSE_CODE_KEY, String.valueOf(responseCode));
    if (responseCode >= 200 & responseCode < 300) {
      pluginSettings.put(LAST_WEBHOOK_SUCCESS_TIME_KEY, currentTime);
    } else if (responseCode >= 400) {
      pluginSettings.put(LAST_WEBHOOK_FAILURE_TIME_KEY, currentTime);
    }
  }
}
