package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
