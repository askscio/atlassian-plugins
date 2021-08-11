package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
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
  private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchConfigServlet(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
    System.err.println(String.format("INITIALIZED %s", pluginSettingsFactory));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.err.println("GET");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.err.println(String.format("POST %s", request.getParameterMap().keySet()));
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
