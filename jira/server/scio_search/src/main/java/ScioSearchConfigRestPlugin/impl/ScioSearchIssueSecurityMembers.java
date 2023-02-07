package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import ScioSearchConfigRestPlugin.impl.ScioIssueSecurityMembersResponse.IssueSecuritySchemeMemberInfo;
import ScioSearchConfigRestPlugin.impl.ScioIssueSecurityMembersResponse.JiraPermissionHolderInfo;
import ScioSearchConfigRestPlugin.impl.ScioIssueSecurityMembersResponse.AtlassianUser;
import ScioSearchConfigRestPlugin.impl.ScioIssueSecurityMembersResponse.AtlassianGroup;
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

  private void validateUserIsAdmin() {
    final UserProfile profile = userManager.getRemoteUser();
    if (profile == null || !userManager.isSystemAdmin(profile.getUserKey())) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ScioIssueSecurityMembersResponse setTarget(@QueryParam("schemeId") String schemeIdStr) {
    logger.debug(String.format("Received request for getting issuesecuritymembers: %s", schemeIdStr));
    validateUserIsAdmin();
    Long schemeId = Long.parseLong(schemeIdStr);

    final IssueSecurityLevelManager issueSecurityLevelManager = ComponentAccessor.getIssueSecurityLevelManager();
    final IssueSecuritySchemeManager issueSecuritySchemeManager = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class);
    final Collection<IssueSecurityLevel> issueSecurityLevels = issueSecurityLevelManager.getIssueSecurityLevels(schemeId);

    final ScioIssueSecurityMembersResponse response = new ScioIssueSecurityMembersResponse();
    response.startAt = 0;
    response.isLast = true; // no response pagination
    if (issueSecurityLevels != null) {
      for (IssueSecurityLevel isl : issueSecurityLevels) {
        logger.debug(isl.getDescription() + ":" + isl.getName() + ":" + String.valueOf(isl.getId()) + ":" + String.valueOf(isl.getSchemeId()));
        List<IssueSecurityLevelPermission> issueSecurityLevelPermissions = issueSecuritySchemeManager.getPermissionsBySecurityLevel(isl.getId());
        if (issueSecurityLevels != null) {
          response.maxResults += 1;
          response.total += 1;
          response.values = new ArrayList();
          for (IssueSecurityLevelPermission islp : issueSecurityLevelPermissions) {
            logger.debug(islp.getId() + ":" + islp.getParameter() + ":" + islp.getSchemeId() + ":" + islp.getSecurityLevelId() + ":" + islp.getType());
            IssueSecuritySchemeMemberInfo memberInfo = new IssueSecuritySchemeMemberInfo();
            memberInfo.id = String.valueOf(islp.getId());
            memberInfo.issueSecurityLevelId = String.valueOf(islp.getSecurityLevelId());
            JiraPermissionHolderInfo holder = new JiraPermissionHolderInfo();
            holder.type = islp.getType();
            memberInfo.holder = holder;
            switch (islp.getType()) {
              case "applicationRole":
                break;
              case "user":
                // TODO: Set accountId or name? Set active based on api call?
                holder.user = new AtlassianUser();
                holder.user.key = islp.getParameter();
                holder.user.active = true;
                break;
              case "group":
                // TODO: Handle deleted group issue.
                holder.group = new AtlassianGroup();
                holder.group.name = islp.getParameter();
                break;
              case "projectrole":
                holder.type = "projectRole";  // Overwrite the type since the Jira Cloud response has this.
                holder.parameter = islp.getParameter(); // roleId is the parameter.
                break;
              case "lead":
                holder.type = "projectLead";  // Overwrite the type since the Jira Cloud response has this.
                break;
              case "reporter":
                break;
              case "assignee":
                break;
              case "userCF":  // userCustomField
                holder.type = "userCustomField"; // Overwrite the type since the Jira Cloud response has this.
                holder.parameter = islp.getParameter(); // custom field name
                break;
              case "groupCF": // groupCustomField
                holder.type = "groupCustomField";    // Overwrite the type since the Jira Cloud response has this.
                holder.parameter = islp.getParameter(); // custom field name
                break;
              case "anyone":
              case "sd.customer.portal.only":
              default:
                logger.warn("Unsupported issue security level permission type: " + islp.getType());
            }
            response.values.add(memberInfo);
          }
        }
      }
    }
    return response;
  }
}
