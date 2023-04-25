package ScioSearchConfigRestPlugin.impl;

import ScioSearchConfigRestPlugin.impl.ScioSearchInfoResponse.UserInfo;
import com.atlassian.jira.config.properties.ApplicationProperties;
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

  @Inject
  public ScioSearchInfo(UserManager userManager) {
    this.userManager = userManager;
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

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public ScioSearchInfoResponse getInfo() {
    ScioSearchInfoResponse response = new ScioSearchInfoResponse();
    response.userInfo = getUserInfo();
    return response;
  }
}
