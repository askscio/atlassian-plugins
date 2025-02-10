package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
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

  @Inject
  public ScioSearchGroupMembersFetch(UserAccessor userAccessor) {
    this.userAccessor = userAccessor;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioSearchGroupMembersResponse getGroupMembers(@QueryParam("groupName") String groupName) {
    Group group = userAccessor.getGroup(groupName);
    if (group == null) {
      throw new NotFoundException("Group not found");
    }
    List<String> groupMembers = userAccessor.getMemberNamesAsList(group);

    ScioSearchGroupMembersResponse response = new ScioSearchGroupMembersResponse();
    response.userNames = groupMembers;
    return response;
  }
}
