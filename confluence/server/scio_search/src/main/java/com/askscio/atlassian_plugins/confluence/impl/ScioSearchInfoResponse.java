package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_FAILURE_TIME_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_RESPONSE_CODE_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_RESPONSE_TIME_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.LAST_WEBHOOK_SUCCESS_TIME_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;
import static com.askscio.atlassian_plugins.confluence.impl.Utils.getPluginSettingsValueOrElse;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchInfoResponse {

  private final UserInfo userInfo;
  private final InstanceInfo instanceInfo;
  private final PluginInfo pluginInfo;

  public ScioSearchInfoResponse(UserInfo userInfo, InstanceInfo instanceInfo,
      PluginInfo pluginInfo) {
    this.userInfo = userInfo;
    this.instanceInfo = instanceInfo;
    this.pluginInfo = pluginInfo;
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class UserInfo {

    private final String userKey;
    private final String userName;
    private final String fullName;
    private final String email;
    private final boolean isAdmin;

    public UserInfo(UserManager userManager) {
      final UserProfile profile = userManager.getRemoteUser();
      this.userKey = profile.getUserKey().getStringValue();
      this.userName = profile.getUsername();
      this.fullName = profile.getFullName();
      this.email = profile.getEmail();
      this.isAdmin = userManager.isSystemAdmin(profile.getUserKey());
    }
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class InstanceInfo {

    private final String version;
    private final String baseUrl;
    private final List<InstalledPluginInfo> installedPluginInfos;

    public InstanceInfo(SettingsManager settingsManager, PluginAccessor pluginAccessor,
        boolean getInstalledPlugins) {
      this.version = GeneralUtil.getVersionNumber();
      this.baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
      if (getInstalledPlugins) {
        this.installedPluginInfos = new ArrayList<>();
        Collection<Plugin> installedPlugins = pluginAccessor.getPlugins();
        for (Plugin plugin : installedPlugins) {
          InstalledPluginInfo installedPluginInfo = new InstalledPluginInfo(pluginAccessor, plugin);
          this.installedPluginInfos.add(installedPluginInfo);
        }
      } else {
        this.installedPluginInfos = null;
      }
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class InstalledPluginInfo {

      private final String name;
      private final String key;
      private final String description;
      private final String vendorName;
      private final String vendorUrl;
      private final String version;
      private final boolean isEnabled;

      public InstalledPluginInfo(PluginAccessor accessor, Plugin plugin) {
        PluginInformation pluginInformation = plugin.getPluginInformation();
        this.name = plugin.getName();
        this.key = plugin.getKey();
        this.description = pluginInformation.getDescription();
        this.vendorName = pluginInformation.getVendorName();
        this.vendorUrl = pluginInformation.getVendorUrl();
        this.version = pluginInformation.getVersion();
        this.isEnabled = accessor.isPluginEnabled(plugin.getKey());
      }
    }
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class PluginInfo {

    private final String version;
    private final String target;
    private final String lastWebhookResponseTime;
    private final String lastWebhookResponseCode;
    private final String lastWebhookSuccessTime;
    private final String lastWebhookFailureTime;

    public PluginInfo(PluginAccessor pluginAccessor, PluginSettingsFactory pluginSettingsFactory) {
      final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
      this.version = pluginAccessor.getPlugin(Constants.PLUGIN_KEY).getPluginInformation()
          .getVersion();
      this.target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
      this.lastWebhookResponseTime = getPluginSettingsValueOrElse(pluginSettings,
          LAST_WEBHOOK_RESPONSE_TIME_KEY, "Never fired");
      this.lastWebhookResponseCode = getPluginSettingsValueOrElse(pluginSettings,
          LAST_WEBHOOK_RESPONSE_CODE_KEY, "Never fired");
      this.lastWebhookSuccessTime = getPluginSettingsValueOrElse(pluginSettings,
          LAST_WEBHOOK_SUCCESS_TIME_KEY, "Never succeeded");
      this.lastWebhookFailureTime = getPluginSettingsValueOrElse(pluginSettings,
          LAST_WEBHOOK_FAILURE_TIME_KEY, "Never failed");
    }
  }
}
