package com.askscio.atlassian_plugins.confluence.impl;

import com.askscio.atlassian_plugins.confluence.impl.ScioSearchInfoResponse.InstanceInfo;
import com.askscio.atlassian_plugins.confluence.impl.ScioSearchInfoResponse.ScioPluginInfo;
import com.askscio.atlassian_plugins.confluence.impl.ScioSearchInfoResponse.UserInfo;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/info")
public class ScioSearchInfoFetch {

  @ConfluenceImport
  private final UserManager userManager;
  @ConfluenceImport
  private final SettingsManager settingsManager;
  @ConfluenceImport
  private final PluginAccessor pluginAccessor;
  @ConfluenceImport
  private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchInfoFetch(UserManager userManager, SettingsManager settingsManager,
      PluginAccessor pluginAccessor, PluginSettingsFactory pluginSettingsFactory) {
    this.userManager = userManager;
    this.settingsManager = settingsManager;
    this.pluginAccessor = pluginAccessor;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public ScioSearchInfoResponse getInfo() {
    final UserInfo userInfo = new UserInfo(userManager);
    final InstanceInfo instanceInfo = new InstanceInfo(settingsManager);
    final ScioPluginInfo scioPluginInfo = new ScioPluginInfo(pluginAccessor, pluginSettingsFactory);
    return new ScioSearchInfoResponse(userInfo, instanceInfo, scioPluginInfo);
  }
}
