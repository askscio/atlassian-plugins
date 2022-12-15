package com.askscio.atlassian_plugins.confluence.impl;


import com.atlassian.confluence.api.service.content.SpaceService;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.security.persistence.dao.SpacePermissionDao;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.atlassian.extras.common.log.Logger;


@Named
@Path("/space_permissions")
public class ScioSearchSpacePermissionsFetch {

  private static final Logger.Log logger = Logger.getInstance(ScioSearchSpacePermissionsFetch.class);

  @ConfluenceImport private final UserManager userManager;
  @ConfluenceImport private final SpaceService spaceService;
  @ConfluenceImport private final SpacePermissionDao spacePermissionDao;

  @Inject
  public ScioSearchSpacePermissionsFetch(
      UserManager userManager, SpaceService spaceService, SpacePermissionDao spacePermissionDao) {
    this.userManager = userManager;
    this.spaceService = spaceService;
    this.spacePermissionDao = spacePermissionDao;
  }

  private void validateUserIsAdmin() {
    final UserProfile profile = userManager.getRemoteUser();
    if (profile == null || !userManager.isSystemAdmin(profile.getUserKey())) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  private Long fetchSpaceId(String spaceKey) {
    com.atlassian.confluence.api.model.content.Space space = spaceService.find().withKeys(spaceKey).fetchOrNull();
    if (space == null) {
      return null;
    }
    return space.getId();
  }

  private Map<String, List<String>> constructPermissionTypeToGroupsMap(List<SpacePermission> spacePermissions) {
    // returns a map from permission type to list of groups (group names) with that permission
    HashMap<String, List<String>> permissionToGroups = new HashMap<>();
    for(SpacePermission spacePermission : spacePermissions) {
      String groupName = spacePermission.getGroup();
      if(groupName!=null){
        permissionToGroups.getOrDefault(spacePermission.getType(), new ArrayList<>()).add(spacePermission.getGroup());
      }
    }
    return permissionToGroups;
  }

  private Map<String, List<String>> constructPermissionTypeToUsersMap(List<SpacePermission> spacePermissions) {
    // returns a map from permission type to list of users (user emails) with that permission
    HashMap<String, List<String>> permissionToUsers = new HashMap<>();
    for(SpacePermission spacePermission : spacePermissions) {
      ConfluenceUser user = spacePermission.getUserSubject();
      if(user!=null){
        permissionToUsers.getOrDefault(spacePermission.getType(), new ArrayList<String>()).add(user.getEmail());
      }
    }
    return permissionToUsers;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioSpacePermissionsResponse getPermissionsForSpace(ScioSpacePermissionsRequest request) {
    // fetch space permissions for a given space key

    validateUserIsAdmin();

    // we fetch permissions using the spacePermissionDao.
    // space id is required for spacePermissionDao.getSpacePermissions, hence we fetch that first.
    Long spaceId = fetchSpaceId(request.spaceKey);
    if (spaceId == null) {
      logger.debug("Space not found for key " + request.spaceKey);
      return null;
    }
    logger.debug("Space found for key " + request.spaceKey + " with id " + spaceId);
    Space space = new Space(request.spaceKey);
    space.setId(spaceId);

    List<SpacePermission> spacePermissionList =  spacePermissionDao.findPermissionsForSpace(space);
    logger.debug("Space permissions found for space " + request.spaceKey + " : " + spacePermissionList.toString());

    // now parse the permissions list into the required response format
    Map<String, List<String>> permissionTypeToGroupsMap = constructPermissionTypeToGroupsMap(spacePermissionList);
    logger.debug("Permission type to groups map for space " + request.spaceKey + " : " + permissionTypeToGroupsMap);
    Map<String, List<String>> permissionTypeToUsersMap = constructPermissionTypeToUsersMap(spacePermissionList);
    logger.debug("Permission type to users map for space " + request.spaceKey + " : " + permissionTypeToUsersMap);
    final ScioSpacePermissionsResponse response = new ScioSpacePermissionsResponse();
    response.groups = permissionTypeToGroupsMap;
    response.users = permissionTypeToUsersMap;

    return response;
  }

}
