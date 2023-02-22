package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.askscio.atlassian_plugins.confluence.impl.Utils.PluginStatus;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.extras.common.log.Logger;
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

  private static final Logger.Log logger = Logger.getInstance(ScioSearchInfoResponse.class);
  private final UserInfo userInfo;
  private final InstanceInfo instanceInfo;
  private final ScioPluginInfo scioPluginInfo;

  public ScioSearchInfoResponse(UserInfo userInfo, InstanceInfo instanceInfo,
      ScioPluginInfo scioPluginInfo) {
    this.userInfo = userInfo;
    this.instanceInfo = instanceInfo;
    this.scioPluginInfo = scioPluginInfo;
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
      this.isAdmin = Utils.isCurrentUserAdmin(userManager);
    }
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class InstanceInfo {

    private final String version;
    private final String baseUrl;
    private final List<InstalledPluginInfo> installedPluginInfos;

    public InstanceInfo(SettingsManager settingsManager, PluginAccessor pluginAccessor,
        UserManager userManager,
        boolean getInstalledPlugins) {
      this.version = GeneralUtil.getVersionNumber();
      this.baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
      if (getInstalledPlugins && Utils.isCurrentUserAdmin(userManager)) {
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
  public static class ScioPluginInfo {

    private final String version;
    private final String target;
    private final PluginStatus pluginStatus;

    public ScioPluginInfo(PluginAccessor pluginAccessor,
        PluginSettingsFactory pluginSettingsFactory) {
      final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
      final Plugin plugin = pluginAccessor.getPlugin(Constants.PLUGIN_KEY);
      if (plugin != null && plugin.getPluginInformation() != null) {
        this.version = pluginAccessor.getPlugin(Constants.PLUGIN_KEY).getPluginInformation()
            .getVersion();
      } else {
        logger.warn(String.format("Plugin version not found for %s", Constants.PLUGIN_KEY));
        this.version = null;
      }
      this.target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
      this.pluginStatus = Utils.getPluginStatus(pluginSettings);
    }
  }
}
