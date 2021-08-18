package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.spring.container.ContainerManager;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Named
public class ScioSearchServletFilter implements Filter {

  private static final Logger logger = Logger.getLogger(ScioSearchServletFilter.class.getName());

  @ConfluenceImport private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchServletFilter(final PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    System.err.println("FILTER INITIALIZED");
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    SettingsManager settingsManager =
        (SettingsManager) ContainerManager.getComponent("settingsManager");
    System.err.println(
        String.format("SETTINGS MANAGER %s", settingsManager.getGlobalSettings().getBaseUrl()));
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    System.err.println(String.format("PLUGIN SETTINGS %s", pluginSettings));
    final String target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
    System.err.println(String.format("TARGET %s", target));
    if (servletRequest instanceof HttpServletRequest) {
      HttpServletRequest httpreq = (HttpServletRequest) servletRequest;
      System.err.println(String.format("VISIT RECORDED: %s", httpreq.getRequestURI()));
      ConfluenceUser user = AuthenticatedUserThreadLocal.get();
      if (user == null) {
        System.err.println("NO CURRENT USER");
      } else {
        System.err.println("CURRENT USER: " + user.getKey().getStringValue());
        if (target != null && !target.isEmpty()) {
          sendWebhook(target, httpreq.getRequestURI(), user.getLowerName());
        }
      }
    } else {
      System.err.println(String.format("UNKNOWN REQUEST: %s", servletRequest.getClass().getName()));
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
    System.err.println("FILTER DESTROYED");
  }

  private void sendWebhook(String target, String visitedUrl, String user) {
    try {
      final URL url = new URL(target);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-type", "application/json; charset=utf-8");
      connection.setDoOutput(true);
      final OutputStream output = connection.getOutputStream();
      final String json =
          String.format("{\"url\":\"%s\",\"user\":\"%s\"}", visitedUrl, user);
      final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      output.write(bytes);
      System.err.println(String.format("RESPONSE %d", connection.getResponseCode()));
    } catch (Exception e) {
      logger.warning(String.format("Failed to send Scio webhook: %s", e));
    }
  }
}
