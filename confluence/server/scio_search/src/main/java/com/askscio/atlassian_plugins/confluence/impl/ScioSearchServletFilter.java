package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.spring.container.ContainerManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import com.atlassian.extras.common.log.Logger;

@Named
public class ScioSearchServletFilter implements Filter {

  private static final int NUM_BACKGROUND_THREADS = 1;
  private static final int MAX_OUTSTANDING_REQUESTS = 10;
  private static final int BASE_URL_MIN_PREFIX = "https://".length() + 1;

  private static final Logger.Log logger = Logger.getInstance(ScioSearchServletFilter.class);

  private final ExecutorService executor =
      new ThreadPoolExecutor(
          NUM_BACKGROUND_THREADS,
          NUM_BACKGROUND_THREADS,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(MAX_OUTSTANDING_REQUESTS));

  @ConfluenceImport private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchServletFilter(final PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    logger.info(String.format("%s initialized", getClass().getName()));
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    if (!(servletRequest instanceof HttpServletRequest)) {
      logger.debug("Not an HTTP request");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final HttpServletRequest httpreq = (HttpServletRequest) servletRequest;
    // Saving a page or blogpost fires: PUT http://confluence-server:8090/rest/api/content/65603?status=draft
    if (httpreq.getRequestURI().startsWith("/rest/api/content/") &&
        ("PUT".equals(httpreq.getMethod()) || "POST".equals(httpreq.getMethod()) || "DELETE".equals(httpreq.getMethod()))) {
      logger.debug("Save url: " + httpreq.getMethod() + ": " + httpreq.getRequestURI());
    } else if (!httpreq.getRequestURI().contains("viewpage")
        && !httpreq.getRequestURI().contains("/display/")) {
      logger.debug(String.format("Uninteresting visit: %s", httpreq.getRequestURI()));
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final SettingsManager settingsManager =
        (SettingsManager) ContainerManager.getComponent("settingsManager");
    if (settingsManager == null) {
      logger.warn("Missing settingsManager");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final Settings globalSettings = settingsManager.getGlobalSettings();
    if (globalSettings == null) {
      logger.warn("Missing globalSettings");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    String baseURL = globalSettings.getBaseUrl();
    if (baseURL == null || baseURL.isEmpty()) {
      logger.warn("Missing baseURL");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    if (baseURL.length() > BASE_URL_MIN_PREFIX) {
      final int thirdSlash = baseURL.indexOf("/", BASE_URL_MIN_PREFIX);
      if (thirdSlash > 0) {
        logger.debug(String.format("BaseURL %s trimming to %d", baseURL, thirdSlash));
        baseURL = baseURL.substring(0, thirdSlash);
      }
    }

    if (pluginSettingsFactory == null) {
      logger.warn("Missing pluginSettingsFactory");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    if (pluginSettings == null) {
      logger.warn("Missing pluginSettings");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final String target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
    if (target == null || target.isEmpty()) {
      logger.info("Plugin not configured");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final ConfluenceUser user = AuthenticatedUserThreadLocal.get();
    if (user == null) {
      logger.debug("Anonymous page visit");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final URL visitUrl;
    try {
      String query = httpreq.getQueryString();
      if (query == null || query.isEmpty()) {
        visitUrl = new URL(
          String.format("%s%s?glean_http_method=%s", baseURL, httpreq.getRequestURI(), httpreq.getMethod()));
      } else {
        visitUrl =
            new URL(
                String.format("%s%s?%s&glean_http_method=%s", baseURL, httpreq.getRequestURI(), query, httpreq.getMethod()));
      }
    } catch (MalformedURLException e) {
      logger.warn(String.format("Malformed URL: %s", e.getMessage()));
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    logger.debug(String.format("Visit url %s", visitUrl));
    try {
      executor.submit(new ScioWebhookTask(target, visitUrl.toString(), user.getLowerName(),
          pluginSettings));
    } catch (RejectedExecutionException e) {
      logger.warn(String.format("Queue full: %s", e.getMessage()));
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
    logger.info(String.format("%s destroyed", getClass().getName()));
  }
}
