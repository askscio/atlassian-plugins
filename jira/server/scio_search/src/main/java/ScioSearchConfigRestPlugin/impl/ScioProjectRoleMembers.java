package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import java.util.Set;

@Named
@Path("/project_role_members")
public class ScioProjectRoleMembers {

    @JiraImport
    private final UserManager userManager;

    @Inject
    public ScioProjectRoleMembers(UserManager userManager) {
        this.userManager = userManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectRoleMembersResponse getProjectRoleMembers(@QueryParam("projectId") String projectId, @QueryParam("roleId") String roleId) {
        Utils.validateUserIsAdmin(userManager);
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);

        Project project = ComponentAccessor.getProjectManager().getProjectObj(Long.parseLong(projectId));
        ProjectRole projectRole = projectRoleManager.getProjectRole(Long.parseLong(roleId));
        ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);

        ProjectRoleMembersResponse response = new ProjectRoleMembersResponse();
        response.name = projectRole.getName();
        response.id = projectRole.getId();
        response.description = projectRole.getDescription();
        response.actors = new ArrayList<>();
        Set<RoleActor> roleActors = projectRoleActors.getRoleActors();
        for (RoleActor actor: roleActors){
            ProjectRoleMembersResponse.JiraRoleActorInfo actorInfo = new ProjectRoleMembersResponse.JiraRoleActorInfo();
            actorInfo.id = actor.getId().toString();
            actorInfo.displayName = actor.getDescriptor();
            actorInfo.type = actor.getType();
            if (actorInfo.type.equals("atlassian-user-role-actor")) {
                UserKey userKey = new UserKey(actor.getParameter());
                UserProfile userProfile = userManager.getUserProfile(userKey);
                if (userProfile == null) {
                    continue;
                }
                actorInfo.name = userProfile.getUsername();
            } else {
                actorInfo.name = actor.getParameter();
            }
            response.actors.add(actorInfo);
        }
        return response;
    }
}