package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Named
public class ScioSearchConfigServlet extends HttpServlet {

  @ConfluenceImport
  private final UserManager userManager;
  @ConfluenceImport
  private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchConfigServlet(UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
    this.userManager = userManager;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final UserProfile profile = userManager.getRemoteUser(request);
    if (profile == null || !userManager.isSystemAdmin(profile.getUserKey())) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    final String target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
    response.getWriter().println(target);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final UserProfile profile = userManager.getRemoteUser(request);
    if (profile == null || !userManager.isSystemAdmin(profile.getUserKey())) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    final String target = request.getParameter("target");
    if (target == null || target.isEmpty()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    try {
      new URL(target);
    } catch (MalformedURLException e) {
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
      return;
    }
    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    pluginSettings.put(TARGET_CONFIG_KEY, target);
  }
}
