package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Named
@Path("/group_members")
public class ScioGroupMembers {

    @JiraImport private final UserManager userManager;
    @JiraImport private final GroupManager groupManager;

    @Inject
    public ScioGroupMembers(GroupManager groupManager, UserManager userManager) {
        this.groupManager = groupManager;
        this.userManager = userManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GroupMemberResponse getGroupMembers(@QueryParam("groupname") String groupName, @QueryParam("startAt") int startAt, @QueryParam("maxResults") int maxResults) {
        Utils.validateUserIsAdmin(userManager);
        PageRequest pageRequest = PageRequests.request((long)startAt, maxResults);
        Page<ApplicationUser> users = groupManager.getUsersInGroup(groupName, false /*includeInactiveUsers*/, pageRequest);
        GroupMemberResponse response = new GroupMemberResponse();
        response.total = groupManager.getUsersInGroupCount(groupName);
        response.startAt = startAt;
        response.maxResults = maxResults;
        response.values = new ArrayList<>();
        for (ApplicationUser user : users.getValues()) {
            AtlassianUser atlassianUser = new AtlassianUser();
            atlassianUser.name = user.getName();
            atlassianUser.displayName = user.getDisplayName();
            atlassianUser.emailAddress = user.getEmailAddress();
            atlassianUser.key = user.getKey();
            atlassianUser.active = user.isActive();
            atlassianUser.displayName = user.getDisplayName();
            atlassianUser.username = user.getUsername();
            response.values.add(atlassianUser);
        }
        return response;
    }
}

