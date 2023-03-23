package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.Utils.validateUserIsAdmin;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/configure")
public class ScioSearchConfigRestPlugin {

  private static final Logger.Log logger = Logger.getInstance(ScioSearchConfigRestPlugin.class);

  @ConfluenceImport
  private final UserManager userManager;
  @ConfluenceImport
  private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchConfigRestPlugin(UserManager userManager,
      PluginSettingsFactory pluginSettingsFactory) {
    this.userManager = userManager;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioConfigResponse getTarget() {
    validateUserIsAdmin(userManager);
    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    final String target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
    final ScioConfigResponse response = new ScioConfigResponse();
    response.setTarget(target);
    return response;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ScioConfigResponse setTarget(ScioConfigRequest request) {
    validateUserIsAdmin(userManager);
    try {
      new URL(request.getTarget());
    } catch (MalformedURLException e) {
      throw new UnacceptableException("Unacceptable");
    }
    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    pluginSettings.put(TARGET_CONFIG_KEY, request.getTarget());
    logger.info(String.format("Target changed to: %s", request.getTarget()));
    return getTarget();
  }
}
