package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * Actual Api call: https://docs.atlassian.com/software/jira/docs/api/REST/9.14.0/#api/2/group-getUsersFromGroup
 * GroupManager class doc to get details of the functions used: https://docs.atlassian.com/software/jira/docs/api/7.1.4/com/atlassian/jira/security/groups/GroupManager.html
 */
@Named
@Path("/group_members")
public class ScioGroupMembers {

    @JiraImport private final UserManager userManager;
    @JiraImport private final GroupManager groupManager;
    @JiraImport private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ScioGroupMembers(GroupManager groupManager, UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
        this.groupManager = groupManager;
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GroupMemberResponse getGroupMembers(@QueryParam("groupname") String groupName, @QueryParam("startAt") int startAt, @QueryParam("maxResults") int maxResults, @QueryParam("includeInactiveUsers") boolean includeInactiveUsers) {
        Utils.validateUser(userManager, pluginSettingsFactory.createGlobalSettings());
        if (maxResults > 100){
            throw new BadRequestException("maxResults must not be greater than 100");
        }
        PageRequest pageRequest = PageRequests.request((long)startAt, maxResults);
        Page<ApplicationUser> users = groupManager.getUsersInGroup(groupName, includeInactiveUsers, pageRequest);
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
            atlassianUser.username = user.getUsername();
            response.values.add(atlassianUser);
        }
        return response;
    }
}

