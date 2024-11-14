package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.jira.project.Project;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.ArrayList;

@Named
@Path("/issue_security_level_scheme")

public class ScioIssueSecurityLevelScheme {

    @JiraImport private UserManager userManager;

    @Inject
    public ScioIssueSecurityLevelScheme(UserManager userManager) {
        this.userManager = userManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public IssueSecuritySchemeResponse getIssueSecuritySchemeMembers(@QueryParam("projectId") String projectId) {
        Utils.validateUserIsAdmin(userManager);

        Project project = ComponentAccessor.getProjectManager().getProjectObj(Long.parseLong(projectId));
        Scheme scheme = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class).getSchemeFor(project);
        IssueSecuritySchemeResponse response = new IssueSecuritySchemeResponse();
        if (scheme == null) {
            return response;
        }
        IssueSecurityLevelScheme schemeObject = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class).getIssueSecurityLevelScheme(scheme.getId());
        List<IssueSecurityLevel> securityLevels = ComponentAccessor.getIssueSecurityLevelManager().getIssueSecurityLevels(scheme.getId());

        response.id = schemeObject.getId().toString();
        response.name = schemeObject.getName();
        response.description = schemeObject.getDescription();
        if (schemeObject.getDefaultSecurityLevelId() != null) {
            response.defaultSecurityLevelId = schemeObject.getDefaultSecurityLevelId().toString();
        }
        response.levels = new ArrayList<>();
        for (IssueSecurityLevel securityLevel : securityLevels){
            if (securityLevel == null) {
                continue;
            }
            IssueSecuritySchemeResponse.IssueSecurityLevel level = new IssueSecuritySchemeResponse.IssueSecurityLevel();
            level.id = securityLevel.getId().toString();
            level.name = securityLevel.getName();
            level.description = securityLevel.getDescription();
            response.levels.add(level);
        }
        return response;
    }
}
