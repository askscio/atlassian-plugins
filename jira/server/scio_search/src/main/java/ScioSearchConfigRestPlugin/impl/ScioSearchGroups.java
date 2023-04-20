package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.web.component.admin.group.GroupLabelsService;
import com.atlassian.crowd.embedded.api.Group;
import ScioSearchConfigRestPlugin.impl.ScioGroupsResponse.JiraGroup;
import ScioSearchConfigRestPlugin.impl.ScioGroupsResponse.JiraGroupLabel;
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

// Ref: https://community.atlassian.com/t5/Jira-questions/Search-all-groups-using-findGroups-in-GroupPickerSearchService/qaq-p/657197
// Ref: https://docs.atlassian.com/software/jira/docs/api/7.1.6/com/atlassian/jira/bc/group/search/GroupPickerSearchServiceImpl.html
// Ref: https://docs.atlassian.com/software/jira/docs/api/7.1.6/com/atlassian/jira/web/component/admin/group/GroupLabelsService.html

@Named
@Path("/groups")
public class ScioSearchGroups {
  private static final Logger.Log logger = Logger.getInstance(ScioSearchGroups.class);

  @JiraImport private final UserManager userManager;

  @JiraImport private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchGroups(
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
  public ScioGroupsResponse getGroups(@QueryParam("username") String username) {
    logger.debug(String.format("Received request for getting groups"));
    validateUserIsAdmin();
    GroupPickerSearchService groupSearch = ComponentAccessor.getComponent(GroupPickerSearchService.class);
    GroupLabelsService groupLabelsService = ComponentAccessor.getComponent(GroupLabelsService.class);
    ScioGroupsResponse response = new ScioGroupsResponse();

    if (!Strings.isNullOrEmpty(username)) {
      response.header = String.format("USERGROUPS: Showing %d of %d groups", groupList.size(), groupList.size());
      response.groups = new ArrayList<Group>();

      Collection<String> groupList = groupSearch.getGroupNamesForUser(username);
      for (String g : groupList) {
        JiraGroup ng = new JiraGroup();
        ng.name = g;
        response.groups.add(ng);
      }
      return response;
    }

    // TODO: Does this really return all groups?
    List<Group> groupList = groupSearch.findGroups("");

    response.header = String.format("GROUPS: Showing %d of %d groups", groupList.size(), groupList.size());
    response.groups = new ArrayList<Group>();

    // TODO: Is there a more cpu/memory efficient method for constructing the response?
    // Can we write the response to temp storage and then paginate it?
    for (Group g : groupList) {
      JiraGroup ng = new JiraGroup();
      ng.name = g.getName();
      ng.labels = new ArrayList<JiraGroupLabel>();

      List<GroupLabelView> labels = groupLabelsService.getGroupLabels(g, null);
      if (labels == null) {
        continue;
      }
      for (GroupLabelView lv : labels) {
        JiraGroupLabel l = new JiraGroupLabel();
        l.text = lv.getText();
        l.title = lv.getTitle();
        l.type = lv.getType();
        ng.labels.add(l);
      }
      response.groups.add(ng);
    }
    return response;
  }
}