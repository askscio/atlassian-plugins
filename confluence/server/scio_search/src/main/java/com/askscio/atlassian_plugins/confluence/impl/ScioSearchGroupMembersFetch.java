package com.askscio.atlassian_plugins.confluence.impl;

import static com.askscio.atlassian_plugins.confluence.impl.MyPluginComponentImpl.SERVICE_ACCOUNT_USERNAME_CONFIG_KEY;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.Group;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Named
@Path("/group_members")
public class ScioSearchGroupMembersFetch {
  @ConfluenceImport private final UserAccessor userAccessor;
  @ConfluenceImport private final UserManager userManager;
  @ConfluenceImport private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchGroupMembersFetch(
      UserAccessor userAccessor,
      UserManager userManager,
      PluginSettingsFactory pluginSettingsFactory) {
    this.userAccessor = userAccessor;
    this.userManager = userManager;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  private String getServiceAccountUsername() {
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    return (String) pluginSettings.get(SERVICE_ACCOUNT_USERNAME_CONFIG_KEY);
  }

  private void validateUserIsServiceAccount() {
    final UserProfile curentUser = userManager.getRemoteUser();
    final String serviceAccountUserName = getServiceAccountUsername();
    if (curentUser == null
        || serviceAccountUserName == null
        || serviceAccountUserName.isEmpty()
        || !serviceAccountUserName.equals(curentUser.getUsername())) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioSearchGroupMembersResponse getGroupMembers(@QueryParam("groupName") String groupName) {
    validateUserIsServiceAccount();
    Group group = userAccessor.getGroup(groupName);
    if (group == null) {
      throw new NotFoundException("Group not found");
    }
    List<String> groupMembers = userAccessor.getMemberNamesAsList(group);

    ScioSearchGroupMembersResponse response = new ScioSearchGroupMembersResponse();
    response.usernames = groupMembers;
    return response;
  }
}
