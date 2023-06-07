package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import ScioSearchConfigRestPlugin.impl.ScioIssueSecurityMembersResponse.IssueSecuritySchemeMemberInfo;
import ScioSearchConfigRestPlugin.impl.ScioIssueSecurityMembersResponse.JiraPermissionHolderInfo;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.util.List;
import java.util.ArrayList;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Named
@Path("/issue_security_scheme_members")
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioIssueSecurityMembersResponse getIssueSecuritySchemeMembers(@QueryParam("schemeId") String schemeIdStr) {
    logger.debug(String.format("Received request for getting issue security scheme members: %s", schemeIdStr));
    Utils.validateUserIsAdmin(userManager);
    Long schemeId = Long.parseLong(schemeIdStr);

    final IssueSecurityLevelManager issueSecurityLevelManager = ComponentAccessor.getIssueSecurityLevelManager();
    final IssueSecuritySchemeManager issueSecuritySchemeManager = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class);
    final Collection<IssueSecurityLevel> issueSecurityLevels = issueSecurityLevelManager.getIssueSecurityLevels(schemeId);

    final ScioIssueSecurityMembersResponse response = new ScioIssueSecurityMembersResponse();
    response.startAt = 0;
    response.isLast = true; // no response pagination
    response.values = new ArrayList();

    if (issueSecurityLevels == null) {
      return response;
    }
    for (IssueSecurityLevel isl : issueSecurityLevels) {
      logger.debug("IssueSecurityLevel: " + isl.getDescription() + ":" + isl.getName() + ":" + String.valueOf(isl.getId()) + ":" + String.valueOf(isl.getSchemeId()));
      List<IssueSecurityLevelPermission> issueSecurityLevelPermissions = issueSecuritySchemeManager.getPermissionsBySecurityLevel(isl.getId());
      if (issueSecurityLevelPermissions == null) {
        continue;
      }
      for (IssueSecurityLevelPermission islp : issueSecurityLevelPermissions) {
        logger.debug("IssueSecurityLevelPermission: " + islp.getId() + ":" + islp.getParameter() + ":" + islp.getSchemeId() + ":" + islp.getSecurityLevelId() + ":" + islp.getType());
        JiraPermissionHolderInfo holder = new JiraPermissionHolderInfo();
        holder.type = islp.getType();
        holder.parameter = islp.getParameter();
        IssueSecuritySchemeMemberInfo memberInfo = new IssueSecuritySchemeMemberInfo();
        memberInfo.id = String.valueOf(islp.getId());
        memberInfo.issueSecurityLevelId = String.valueOf(islp.getSecurityLevelId());
        memberInfo.holder = holder;
        response.values.add(memberInfo);
        response.maxResults += 1;
        response.total += 1;
      }
    }
    return response;
  }
}
