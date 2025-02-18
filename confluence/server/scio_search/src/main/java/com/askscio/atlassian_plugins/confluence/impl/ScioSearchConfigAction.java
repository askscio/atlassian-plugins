package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.SERVICE_ACCOUNT_USERNAME_CONFIG_KEY;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.xwork.ParameterSafe;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@WebSudoRequired
public class ScioSearchConfigAction extends ConfluenceActionSupport {

  private PluginSettingsFactory pluginSettingsFactory;

  private String target;
  private String username;

  public String getTarget() {
    return target;
  }

  @ParameterSafe
  public void setTarget(String target) {
    this.target = target;
  }

  public String getUsername() {
    return username;
  }

  @ParameterSafe
  public void setUsername(String username) {
    this.username = username;
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
    setUsername((String) pluginSettings.get(SERVICE_ACCOUNT_USERNAME_CONFIG_KEY));
    return super.doDefault();
  }

  public String execute() throws Exception {
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    String targetUrl = getTarget();
    String username = getUsername();
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
    pluginSettings.put(TARGET_CONFIG_KEY, targetUrl);
    addActionMessage("Target URL updated to " + targetUrl);

    if (username == null || username.isEmpty()) {
      addFieldError("scio_search.username", "Service account username is required", null);
      return ConfluenceActionSupport.ERROR;
    }

    pluginSettings.put(SERVICE_ACCOUNT_USERNAME_CONFIG_KEY, username);
    addActionMessage("Service account username updated to " + username);
    return ConfluenceActionSupport.SUCCESS;
  }
}
