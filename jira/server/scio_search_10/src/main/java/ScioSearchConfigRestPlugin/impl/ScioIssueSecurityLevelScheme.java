package ScioSearchConfigRestPlugin.impl;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.jira.project.Project;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
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

/**
 * Equivalent api call: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-project-permission-schemes/#api-rest-api-3-project-projectkeyorid-issuesecuritylevelscheme-get
 * ProjectManager Doc: https://docs.atlassian.com/software/jira/docs/api/7.6.1/com/atlassian/jira/project/ProjectManager.html
 * IssueSecuritySchemeManager Doc: https://docs.atlassian.com/software/jira/docs/api/8.4.1/com/atlassian/jira/issue/security/IssueSecuritySchemeManager.html
 */
@Named
@Path("/issue_security_level_scheme")
public class ScioIssueSecurityLevelScheme {
    private static final Logger.Log logger = Logger.getInstance(ScioIssueSecurityLevelScheme.class);

    @JiraImport private UserManager userManager;
    @JiraImport private PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ScioIssueSecurityLevelScheme(UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public IssueSecuritySchemeResponse getIssueSecuritySchemeMembers(@QueryParam("projectId") String projectId) {
        Utils.validateUser(userManager, pluginSettingsFactory.createGlobalSettings());

        Project project = ComponentAccessor.getProjectManager().getProjectObj(Long.parseLong(projectId));
        if (project == null) {
            logger.info(String.format("Project %s not found", projectId));
            throw new NotFoundException("Project not found");
        }
        Scheme scheme = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class).getSchemeFor(project);
        IssueSecuritySchemeResponse response = new IssueSecuritySchemeResponse();
        if (scheme == null) {
            logger.info(String.format("Scheme not found for project: %s", projectId));
            throw new NotFoundException(String.format("Security level for project %s does not exist", projectId));
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
