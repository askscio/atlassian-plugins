package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.askscio.atlassian_plugins.confluence.impl.Utils.PluginSettingsCacheLoader;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.spring.container.ContainerManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
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

@Named
public class ScioSearchServletFilter implements Filter {

  private static final int NUM_BACKGROUND_THREADS = 1;
  private static final int MAX_OUTSTANDING_REQUESTS = 10;
  private static final int BASE_URL_MIN_PREFIX = "https://".length() + 1;

  private static final Logger.Log logger = Logger.getInstance(ScioSearchServletFilter.class);
  private static final Set<String> ALLOWED_REST_API_CONTENT_ACTIONS = getAllowedRestApiContentActions();
  private final ExecutorService executor =
      new ThreadPoolExecutor(
          NUM_BACKGROUND_THREADS,
          NUM_BACKGROUND_THREADS,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(MAX_OUTSTANDING_REQUESTS));
  @ConfluenceImport
  private final PluginSettingsFactory pluginSettingsFactory;
  // It is not clear if atlassian cache is thread safe - since the executor thread pool has only 1
  // thread, we are okay.
  private final Cache<String, Optional<String>> pluginSettingsCache;

  @Inject
  public ScioSearchServletFilter(final PluginSettingsFactory pluginSettingsFactory,
      @ConfluenceImport final CacheManager cacheManager) {
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.pluginSettingsCache = cacheManager.getCache(getClass().getName() + ".pluginSettingsCache",
        new PluginSettingsCacheLoader(pluginSettingsFactory.createGlobalSettings()));
  }

  private static Set<String> getAllowedRestApiContentActions() {
      final Set<String> actions = new HashSet<String>();
      actions.add("PUT");
      actions.add("POST");
      actions.add("DELETE");
      return actions;
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
        ALLOWED_REST_API_CONTENT_ACTIONS.contains(httpreq.getMethod())) {
      if ("DELETE".equals(httpreq.getMethod())) {
        logger.debug("Delete url: " + httpreq.getMethod() + ": " + httpreq.getRequestURI());
      } else {
        logger.debug("Save url: " + httpreq.getMethod() + ": " + httpreq.getRequestURI());
      }
    } else if (!httpreq.getRequestURI().contains("viewpage")
        && !httpreq.getRequestURI().contains("/display/")) {
      logger.debug(String.format("Uninteresting visit: %s", httpreq.getRequestURI()));
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    // Fix bug:
    // [BUG] the plugin can't filter .min.js.map information in Confluence 8.5.8 version
    // https://github.com/askscio/atlassian-plugins/issues/38
    // This codes have been tested successfully. They can support to filter .min.js.map information in Confluence 8.5.8 version.
    // huang.rong.gang@navercorp.com
    else if (httpreq.getRequestURI().contains(".min.js.map")
            && httpreq.getRequestURI().contains("/display/")) {
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
            String.format("%s%s?glean_http_method=%s", baseURL, httpreq.getRequestURI(),
                httpreq.getMethod()));
      } else {
        visitUrl =
            new URL(
                String.format("%s%s?%s&glean_http_method=%s", baseURL, httpreq.getRequestURI(),
                    query, httpreq.getMethod()));
      }
    } catch (MalformedURLException e) {
      logger.warn(String.format("Malformed URL: %s", e.getMessage()));
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    logger.debug(String.format("Visit url %s", visitUrl));
    try {
      executor.submit(new ScioWebhookTask(target, visitUrl.toString(), user.getLowerName(),
          pluginSettings, pluginSettingsCache));
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
