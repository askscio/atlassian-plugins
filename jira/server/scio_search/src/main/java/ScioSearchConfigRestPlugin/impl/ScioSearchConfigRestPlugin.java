package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/configure")
public class ScioSearchConfigRestPlugin {

  private static final Logger.Log logger = Logger.getInstance(ScioSearchConfigRestPlugin.class);

  @JiraImport private final UserManager userManager;

  @JiraImport private final PluginSettingsFactory pluginSettingsFactory;

  @Inject
  public ScioSearchConfigRestPlugin(
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
  public ScioConfigResponse getTarget() {
    validateUserIsAdmin();
    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    final String target = (String) pluginSettings.get(TARGET_CONFIG_KEY);
    final ScioConfigResponse response = new ScioConfigResponse();
    response.setTarget(target);
    return response;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ScioConfigResponse setTarget(ScioConfigRequest request) {
    logger.debug(String.format("Received request for setting target url: %s", request.getTarget()));
    validateUserIsAdmin();
    try {
      new URL(request.getTarget());
    } catch (MalformedURLException e) {
      throw new UnacceptableException("Unacceptable");
    }
    final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    pluginSettings.put(TARGET_CONFIG_KEY, request.getTarget());
    logger.info(String.format("Target changed to: %s", request.getTarget()));
    return getTarget();
  }
}
