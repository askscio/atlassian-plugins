package ScioSearchConfigRestPlugin.impl;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Path("/configure/serviceaccount_email")
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
    public ScioServiceAccountConfigureResponse getServiceAccountEmail() {
        Utils.validateUserIsAdmin(userManager);
        final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        final String username = (String) pluginSettings.get(SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY);
        final ScioServiceAccountConfigureResponse response = new ScioServiceAccountConfigureResponse();
        response.setServiceAccountEmail(username);
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ScioServiceAccountConfigureResponse setServiceAccountEmail(ScioServiceAccountConfigureRequest request) {
        logger.debug(String.format("Received request for setting service account email: %s", request.getServiceAccountEmail()));
        Utils.validateUserIsAdmin(userManager);
        if (!isEmailValid(request.getServiceAccountEmail())) {
            throw new UnacceptableException("Invalid Email Address");
        }
        final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY, request.getServiceAccountEmail());
        logger.info(String.format("service account email changed to: %s", request.getServiceAccountEmail()));
        return getServiceAccountEmail();
    }

    private boolean isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        email = email.trim();
        /**
         * Must have text before and after '@'
         * Must have a dot ('.') in the domain with text on both sides
         */
        String emailRegex = "^[^@]+@[^@]+\\.[^@]+$";
        return email.matches(emailRegex);
    }
}

