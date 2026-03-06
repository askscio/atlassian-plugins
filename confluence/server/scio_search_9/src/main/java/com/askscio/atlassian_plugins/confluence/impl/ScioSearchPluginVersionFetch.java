package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/version")
public class ScioSearchPluginVersionFetch {

  @ConfluenceImport private final PluginAccessor pluginAccessor;

  @Inject
  public ScioSearchPluginVersionFetch(PluginAccessor pluginAccessor) {
    this.pluginAccessor = pluginAccessor;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public ScioSearchPluginVersionResponse getVersion() {
    String pluginVersion = pluginAccessor.getPlugin(Constants.PLUGIN_KEY).getPluginInformation().getVersion();
    return new ScioSearchPluginVersionResponse(pluginVersion);
  }
}
