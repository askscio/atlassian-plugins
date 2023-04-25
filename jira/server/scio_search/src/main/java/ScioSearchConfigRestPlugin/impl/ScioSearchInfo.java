package ScioSearchConfigRestPlugin.impl;

import ScioSearchConfigRestPlugin.impl.ScioSearchInfoResponse.InstanceInfo;
import ScioSearchConfigRestPlugin.impl.ScioSearchInfoResponse.UserInfo;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
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
  @JiraImport
  private final UserManager userManager;
  // Ref: https://docs.atlassian.com/software/jira/docs/api/7.6.1/com/atlassian/jira/issue/fields/rest/json/beans/JiraBaseUrls.html
  @JiraImport
  private final ApplicationProperties applicationProperties;
  // Ref: https://docs.atlassian.com/software/jira/docs/api/7.6.1/com/atlassian/jira/util/BuildUtilsInfo.html
  @JiraImport
  private final BuildUtilsInfo buildUtilsInfo;

  @Inject
  public ScioSearchInfo(UserManager userManager, ApplicationProperties applicationProperties,
      BuildUtilsInfo buildUtilsInfo) {
    this.userManager = userManager;
    this.applicationProperties = applicationProperties;
    this.buildUtilsInfo = buildUtilsInfo;
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

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public ScioSearchInfoResponse getInfo() {
    ScioSearchInfoResponse response = new ScioSearchInfoResponse();
    response.userInfo = getUserInfo();
    response.instanceInfo = getInstanceInfo();
    return response;
  }
}
