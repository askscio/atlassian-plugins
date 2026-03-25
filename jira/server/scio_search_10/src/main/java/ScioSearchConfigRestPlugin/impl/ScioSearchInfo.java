package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;
import ScioSearchConfigRestPlugin.impl.ScioSearchInfoResponse.InstanceInfo;
import ScioSearchConfigRestPlugin.impl.ScioSearchInfoResponse.ScioPluginInfo;
import ScioSearchConfigRestPlugin.impl.ScioSearchInfoResponse.UserInfo;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/info")
public class ScioSearchInfo {
  private static final Logger.Log logger = Logger.getInstance(ScioSearchInfo.class);

  @JiraImport
  private final UserManager userManager;
  // Ref: https://docs.atlassian.com/software/jira/docs/api/7.6.1/com/atlassian/jira/config/properties/ApplicationProperties.html
  @JiraImport
  private final ApplicationProperties applicationProperties;
  // Ref: https://docs.atlassian.com/software/jira/docs/api/7.6.1/com/atlassian/jira/util/BuildUtilsInfo.html
  @JiraImport
  private final BuildUtilsInfo buildUtilsInfo;
  @JiraImport
  private final PluginSettingsFactory pluginSettingsFactory;
  @JiraImport
  private final PluginAccessor pluginAccessor;

  @Inject
  public ScioSearchInfo(UserManager userManager, ApplicationProperties applicationProperties,
      BuildUtilsInfo buildUtilsInfo, PluginSettingsFactory pluginSettingsFactory,
      PluginAccessor pluginAccessor) {
    this.userManager = userManager;
    this.applicationProperties = applicationProperties;
    this.buildUtilsInfo = buildUtilsInfo;
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.pluginAccessor = pluginAccessor;
  }

  private UserInfo getUserInfo() {
    UserInfo userInfo = new UserInfo();
    UserProfile profile = userManager.getRemoteUser();
    userInfo.userKey = profile.getUserKey().getStringValue();
    userInfo.userName = profile.getUsername();
    userInfo.fullName = profile.getFullName();
    userInfo.email = profile.getEmail();
    userInfo.isAdmin = Utils.isCurrentUserAdmin(userManager);
    return userInfo;
  }

  private InstanceInfo getInstanceInfo() {
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.baseUrl = applicationProperties.getDefaultBackedString("jira.baseurl");
    instanceInfo.version = buildUtilsInfo.getVersion();
    return instanceInfo;
  }

  private ScioPluginInfo getScioPluginInfo() {
    ScioPluginInfo scioPluginInfo = new ScioPluginInfo();

    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    final Plugin plugin = pluginAccessor.getPlugin(Constants.PLUGIN_KEY);
    if (plugin != null && plugin.getPluginInformation() != null) {
      scioPluginInfo.version = pluginAccessor
          .getPlugin(Constants.PLUGIN_KEY)
          .getPluginInformation()
          .getVersion();
    } else {
      logger.warn(String.format("Plugin version not found for %s", Constants.PLUGIN_KEY));
      scioPluginInfo.version = null;
    }

    scioPluginInfo.target = (String) pluginSettings.get(TARGET_CONFIG_KEY);

    return scioPluginInfo;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public ScioSearchInfoResponse getInfo() {
    ScioSearchInfoResponse response = new ScioSearchInfoResponse();
    response.userInfo = getUserInfo();
    response.instanceInfo = getInstanceInfo();
    response.scioPluginInfo = getScioPluginInfo();
    return response;
  }
}
