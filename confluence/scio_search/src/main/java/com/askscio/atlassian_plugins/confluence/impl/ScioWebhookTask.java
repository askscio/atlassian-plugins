package com.askscio.atlassian_plugins.confluence.impl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.atlassian.extras.common.log.Logger;

public class ScioWebhookTask implements Runnable {

  private static final Logger.Log logger = Logger.getInstance(ScioWebhookTask.class);

  private final String target;
  private final String visitedUrl;
  private final String user;

  public ScioWebhookTask(String target, String visitedUrl, String user) {
    this.target = target;
    this.visitedUrl = visitedUrl;
    this.user = user;
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
      logger.debug(String.format("Webhook sent %d", connection.getResponseCode()));
    } catch (Exception e) {
      logger.warn(String.format("Failed to send Scio webhook: %s", e));
    }
  }
}
