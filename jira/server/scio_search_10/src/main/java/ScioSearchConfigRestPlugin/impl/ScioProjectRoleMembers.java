package ScioSearchConfigRestPlugin.impl;

import com.atlassian.extras.common.log.Logger;
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

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import java.util.Set;

/**
 * Equivalent api call: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-project-roles/#api-rest-api-3-project-projectidorkey-role-id-get
 * ProjectManager Doc: https://docs.atlassian.com/software/jira/docs/api/7.6.1/com/atlassian/jira/project/ProjectManager.html
 * ProjectRoleManager Doc: https://docs.atlassian.com/software/jira/docs/api/7.0.3/com/atlassian/jira/security/roles/ProjectRoleManager.html
 */
@Named
@Path("/project_role_members")
public class ScioProjectRoleMembers {
    private static final Logger.Log logger = Logger.getInstance(ScioProjectRoleMembers.class);
    @JiraImport
    private final UserManager userManager;
    @JiraImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ScioProjectRoleMembers(UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectRoleMembersResponse getProjectRoleMembers(@QueryParam("projectId") String projectId, @QueryParam("roleId") String roleId) {
        Utils.validateUser(userManager, pluginSettingsFactory.createGlobalSettings());
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);

        Project project = ComponentAccessor.getProjectManager().getProjectObj(Long.parseLong(projectId));
        if (project == null){
            logger.info(String.format("Project %s not found", projectId));
            throw new NotFoundException("Project not found");
        }
        ProjectRole projectRole = projectRoleManager.getProjectRole(Long.parseLong(roleId));
        if (projectRole == null){
            throw new NotFoundException(String.format("Project Role %s not found for project %s", roleId, projectId));
        }
        ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
        if (projectRoleActors == null){
            throw new NotFoundException(String.format("Project Role Actors not found for project %s, role %s", projectId, roleId));
        }
        ProjectRoleMembersResponse response = new ProjectRoleMembersResponse();
        response.name = projectRole.getName();
        response.id = projectRole.getId();
        response.description = projectRole.getDescription();
        response.actors = new ArrayList<>();
        Set<RoleActor> roleActors = projectRoleActors.getRoleActors();
        for (RoleActor actor: roleActors) {
            ProjectRoleMembersResponse.JiraRoleActorInfo actorInfo = new ProjectRoleMembersResponse.JiraRoleActorInfo();
            actorInfo.id = actor.getId().toString();
            actorInfo.displayName = actor.getDescriptor();
            actorInfo.type = actor.getType();
            // Jira's api call returns userName as actor name whereas actor.getParameter is the userKey so we need to get the username from the usermanager
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