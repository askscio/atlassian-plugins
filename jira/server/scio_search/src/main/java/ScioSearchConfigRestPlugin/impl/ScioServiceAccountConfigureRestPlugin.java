package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY;

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
@Path("/configure/serviceaccount_username")
public class ScioServiceAccountConfigureRestPlugin {

    private static final Logger.Log logger = Logger.getInstance(ScioSearchConfigRestPlugin.class);

    @JiraImport
    private final UserManager userManager;

    @JiraImport private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ScioServiceAccountConfigureRestPlugin(
            UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ScioServiceAccountConfigureResponse getServiceAccountUsername() {
        Utils.validateUserIsAdmin(userManager);
        final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        final String username = (String) pluginSettings.get(SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY);
        final ScioServiceAccountConfigureResponse response = new ScioServiceAccountConfigureResponse();
        response.setServiceAccountUsername(username);
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ScioServiceAccountConfigureResponse setServiceAccountUsername(ScioConfigRequest request) {
        logger.debug(String.format("Received request for setting service account username: %s", request.getTarget()));
        Utils.validateUserIsAdmin(userManager);
        try {
            new URL(request.getTarget());
        } catch (MalformedURLException e) {
            throw new UnacceptableException("Unacceptable");
        }
        final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY, request.getTarget());
        logger.info(String.format("Target changed to: %s", request.getTarget()));
        return getServiceAccountUsername();
    }
}

