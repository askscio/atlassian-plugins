package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@WebSudoRequired
public class ScioSearchConfigAction extends ConfluenceActionSupport {

  private PluginSettingsFactory pluginSettingsFactory;

  private String target;

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }


  @Override
  protected List<String> getPermissionTypes() {
    List<String> requiredPermissions = super.getPermissionTypes();
    requiredPermissions.add(SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION);
    return requiredPermissions;
  }

  @Override
  public String doDefault() throws Exception {
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    setTarget((String) pluginSettings.get(TARGET_CONFIG_KEY));
    return super.doDefault();
  }

  public String execute() throws Exception {
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    String targetUrl = getTarget();
    if (targetUrl == null || targetUrl.isEmpty()) {
      addFieldError("scio_search.target", "Target URL is required", null);
      return ConfluenceActionSupport.ERROR;
    }
    try {
      new URL(targetUrl);
    } catch (MalformedURLException e) {
      addFieldError("scio_search.target", "Invalid target URL", null);
      return ConfluenceActionSupport.ERROR;
    }
    pluginSettings.put(TARGET_CONFIG_KEY, getTarget());
    addActionMessage("Target URL updated to " + targetUrl);
    return ConfluenceActionSupport.SUCCESS;
  }
}
