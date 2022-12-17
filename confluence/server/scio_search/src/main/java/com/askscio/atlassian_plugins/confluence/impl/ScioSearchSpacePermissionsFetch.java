package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.confluence.api.model.people.Anonymous;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.atlassian.extras.common.log.Logger;

@Named
@Path("/space_permissions")
public class ScioSearchSpacePermissionsFetch {

  private static final Logger.Log logger = Logger.getInstance(ScioSearchSpacePermissionsFetch.class);

  @ConfluenceImport private final UserManager userManager;
  @ConfluenceImport private final SpaceManager spaceManager;
  @ConfluenceImport private final SpacePermissionManager spacePermissionManager;

  @Inject
  public ScioSearchSpacePermissionsFetch(
      UserManager userManager, SpaceManager spaceManager, SpacePermissionManager spacePermissionManager) {
    this.userManager = userManager;
    this.spaceManager = spaceManager;
    this.spacePermissionManager = spacePermissionManager;
  }

  private void validateUserIsAdmin() {
    final UserProfile profile = userManager.getRemoteUser();
    if (profile == null || !userManager.isSystemAdmin(profile.getUserKey())) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  private String getUsername(String username) {
    UserProfile userProfile = userManager.getUserProfile(username);
    if (userProfile!=null) {
      return userProfile.getUsername();
    }
    return null;
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioSpacePermissionsResponse getPermissionsForSpace(@QueryParam("spaceKey") String spaceKey) {
    validateUserIsAdmin();

    // getSpace is deprecated v7.3.0 onwards, but is the only way to get this info currently,
    // see the comments on this thread- https://community.atlassian.com/t5/Confluence-articles/Get-spaces-by-keys-with-the-Confluence-API/ba-p/1939901
    // the thread proposes an alternate method using SpaceService which returns com.atlassian.confluence.api.model.content.Space object,
    // but we want com.atlassian.confluence.spaces.Space object, as it has the permissions set (another way is to use SpaceDao, but plugin installation is
    // timing out when trying to inject it).
    // ALSO, a class part of the core package (com.atlassian.confluence.content.service.space.KeySpaceLocator) still uses getSpace!
    Space space = this.spaceManager.getSpace(spaceKey);
    if (space == null) {
      logger.debug("Space not found for key: " + spaceKey);
      throw new NotFoundException("Space not found");
    }

    ScioSpacePermissionsResponse response = new ScioSpacePermissionsResponse();

    Map<String, Long> groupsAndPermissionIds = this.spacePermissionManager.getGroupsForPermissionType(SpacePermission.VIEWSPACE_PERMISSION, space);
    logger.debug("Groups and permission ids: " + groupsAndPermissionIds.toString());
    List<String> groupsWithViewspacePermissions = new ArrayList<>();
    groupsAndPermissionIds.forEach((group, permissionId) -> {
      groupsWithViewspacePermissions.add(group);
    });
    logger.debug("Groups with view space permissions: " + groupsWithViewspacePermissions);

    Map<String, Long> usersAndPermissionIds = this.spacePermissionManager.getUsersForPermissionType(SpacePermission.VIEWSPACE_PERMISSION, space);
    logger.debug("Users and permission ids: " + usersAndPermissionIds.toString());
    List<String> usersWithViewspacePermissions = new ArrayList<>();
    usersAndPermissionIds.forEach((user, permissionId) -> {
      String username = getUsername(user);
      usersWithViewspacePermissions.add(username);
    });
    logger.debug("Users with view space permissions: " + usersWithViewspacePermissions);

    // Ref: https://docs.atlassian.com/atlassian-confluence/6.5.2/com/atlassian/confluence/security/SpacePermissionManager.html#hasPermission-java.lang.String-com.atlassian.confluence.spaces.Space-com.atlassian.user.User-
    boolean anonymousView = this.spacePermissionManager.hasPermission(SpacePermission.VIEWSPACE_PERMISSION, space, null);
    if (anonymousView) {
      response.anonymous = Collections.singletonList(SpacePermission.VIEWSPACE_PERMISSION);
    }

    response.groups = new HashMap<String, List<String>>() {{
      put(SpacePermission.VIEWSPACE_PERMISSION, groupsWithViewspacePermissions);
    }};

    response.users = new HashMap<String, List<String>>() {{
      put(SpacePermission.VIEWSPACE_PERMISSION, usersWithViewspacePermissions);
    }};

    return response;
  }

}
