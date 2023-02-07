package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/issuesecuritymembers")
public class ScioSearchIssueSecurityMembers {

  private static final Logger.Log logger = Logger.getInstance(ScioSearchIssueSecurityMembers.class);

  @JiraImport private final UserManager userManager;

  @JiraImport private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchIssueSecurityMembers(
      UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
    this.userManager = userManager;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  private void validateUserIsAdmin() {
    final UserProfile profile = userManager.getRemoteUser();
    if (profile == null || !userManager.isSystemAdmin(profile.getUserKey())) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ScioIssueSecurityMembersResponse setTarget(ScioIssueSecurityMembersRequest request) {
    logger.debug(String.format("Received request for getting issuesecuritymembers: %s", request.getSchemeId()));
    validateUserIsAdmin();
    final ScioIssueSecurityMembersResponse response = new ScioIssueSecurityMembersResponse();
    final IssueSecurityLevelManager issueSecurityLevelManager = ComponentAccessor.getIssueSecurityLevelManager();
    final IssueSecuritySchemeManager issueSecuritySchemeManager = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class);

    final Collection<IssueSecurityLevel> issueSecurityLevels = issueSecurityLevelManager.getIssueSecurityLevels(request.getSchemeId());
    if (issueSecurityLevels != null) {
      for (IssueSecurityLevel isl : issueSecurityLevels) {
        logger.debug(isl.getDescription() + ":" + isl.getName() + ":" + String.valueOf(isl.getId()) + ":" + String.valueOf(isl.getSchemeId()));
        List<IssueSecurityLevelPermission> issueSecurityLevelPermissions = issueSecuritySchemeManager.getPermissionsBySecurityLevel(isl.getId());
        if (issueSecurityLevels != null) {
          for (IssueSecurityLevelPermission islp : issueSecurityLevelPermissions) {
            logger.debug(islp.getId() + ":" + islp.getParameter() + ":" + islp.getSchemeId() + ":" + islp.getSecurityLevelId() + ":" + islp.getType());
          }
        }
      }
    }

    response.setSchemeName("hello, world!");
    return response;
  }
}
