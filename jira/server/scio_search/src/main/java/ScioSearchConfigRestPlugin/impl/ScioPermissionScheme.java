package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
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
@Path("/permission_scheme")
public class ScioPermissionScheme {
    @JiraImport
    private final UserManager userManager;
    @Inject
    public ScioPermissionScheme(UserManager userManager) {

        this.userManager = userManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PermissionSchemeResponse getIssueSecuritySchemeMembers(@QueryParam("projectId") String projectId) {
        Utils.validateUserIsAdmin(userManager);

        Project project = ComponentAccessor.getProjectManager().getProjectObj(Long.parseLong(projectId));
        Scheme scheme = ComponentAccessor.getComponentOfType(PermissionSchemeManager.class).getSchemeFor(project);
        PermissionSchemeResponse response = new PermissionSchemeResponse();
        if (scheme == null) {
            return response;
        }
        response.id = scheme.getId().toString();
        response.name = scheme.getName();
        response.description = scheme.getDescription();
        response.permissions = new ArrayList<>();
        for (SchemeEntity entity : scheme.getEntities()) {
            PermissionSchemeResponse.JiraPermissionInfo permission = new PermissionSchemeResponse.JiraPermissionInfo();
            permission.id = entity.getId().toString();
            permission.permission = entity.getEntityTypeId().toString();
            PermissionSchemeResponse.JiraPermissionHolderInfo holder = new PermissionSchemeResponse.JiraPermissionHolderInfo();
            holder.type = entity.getType();
            holder.parameter = entity.getParameter();
            if (entity.getType().equals("group")) {
                holder.group = new Group();
                holder.group.name = entity.getParameter();
            }
            else if (entity.getType().equals("user")) {
                ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(entity.getParameter());
                if (applicationUser == null) {
                    continue;
                }
                holder.user = new AtlassianUser();
                holder.user.name = applicationUser.getName();
                holder.user.displayName = applicationUser.getDisplayName();
                holder.user.emailAddress = applicationUser.getEmailAddress();
                holder.user.key = applicationUser.getKey();
                holder.user.active = applicationUser.isActive();
                holder.user.username = applicationUser.getUsername();

            } else if (entity.getType().equals("projectrole")) {
                ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
                ProjectRole projectRole = projectRoleManager.getProjectRole(Long.parseLong(entity.getParameter()));
                holder.projectRole = new ScioProjectRole();
                holder.projectRole.id = projectRole.getId().toString();
                holder.projectRole.name = projectRole.getName();
                holder.projectRole.description = projectRole.getDescription();
            }
            permission.holder = holder;
            response.permissions.add(permission);
        }
        return response;
    }
}
