package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
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

  @JiraImport private final PluginSettingsFactory pluginSettingsFactory;
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
    if ((!httpreq.getRequestURI().contains("/browse/") &&
        !(httpreq.getRequestURI().contains("/secure/ProjectIssueNavigatorAction!issueViewWithSidebar.jspa")) &&
        !(httpreq.getRequestURI().contains("/secure/AjaxIssueAction!default.jspa")) &&
        !(httpreq.getRequestURI().contains("/secure/Dashboard.jspa")) &&
        !(httpreq.getRequestURI().replaceAll("/$", "").equals("/issues"))) ||
        /* ignore urls like /secure/Dashboard.jspa?null, /secure/AjaxIssueAction!default.jspa?null,
        /secure/ProjectIssueNavigatorAction!issueViewWithSidebar.jspa?null */
        (httpreq.getRequestURI().equals("/secure/Dashboard.jspa") &&
            httpreq.getQueryString()==null) ||
        (httpreq.getRequestURI().equals("/secure/AjaxIssueAction!default.jspa") &&
            httpreq.getQueryString()==null) ||
        (httpreq.getRequestURI().equals("/secure/ProjectIssueNavigatorAction!issueViewWithSidebar.jspa") &&
            httpreq.getQueryString()==null)) {
      logger.debug(String.format("Uninteresting visit: %s", httpreq.getRequestURI()));
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
    logger.debug(String.format("jira.baseurl: %s", baseURL));
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

    final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

    if (user == null) {
      logger.debug("Anonymous page visit");
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    final URL visitUrl;
    try {
      visitUrl =
          new URL(
              String.format("%s%s?%s", baseURL, httpreq.getRequestURI(), httpreq.getQueryString()));
    } catch (MalformedURLException e) {
      logger.warn(String.format("Malformed URL: %s", e.getMessage()));
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    logger.debug(String.format("Visit url %s", visitUrl));
    try {
      executor.submit(new ScioWebhookTask(target, visitUrl.toString(), user.getUsername().toLowerCase()));
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
