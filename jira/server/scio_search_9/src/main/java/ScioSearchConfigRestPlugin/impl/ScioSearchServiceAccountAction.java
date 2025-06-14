package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import javax.inject.Inject;
import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY;

// This class is used to configure service account user email in the plugin Settings from the Jira
// Web Action.
// It does a simple validation check before saving the email in the plugin settings.
@SupportedMethods({RequestMethod.POST})
public class ScioSearchServiceAccountAction extends JiraWebActionSupport {
  @JiraImport private PluginSettingsFactory pluginSettingsFactory;
  private String serviceAccountUserEmail;

  public ScioSearchServiceAccountAction() {}

  @Inject
  public ScioSearchServiceAccountAction(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  public String getServiceAccountUserEmail() {
    return serviceAccountUserEmail;
  }

  public void setServiceAccountUserEmail(String email) {
    this.serviceAccountUserEmail = email;
  }

  public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @Override
  @SupportedMethods({RequestMethod.POST})
  public void doValidation() {
    String email = getServiceAccountUserEmail();
    if (email == null || email.trim().isEmpty()) {
      addErrorMessage("Email is required");
      return;
    }
    email = email.trim();
    /**
     * Must have text before and after '@' Must have a dot ('.') in the domain with text on both
     * sides
     */
    String emailRegex = "^[^@]+@[^@]+\\.[^@]+$";
    if (!email.matches(emailRegex)) {
      addErrorMessage("Invalid email format");
    }
  }

  @Override
  @SupportedMethods({RequestMethod.GET})
  public String doDefault() throws Exception {
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    setServiceAccountUserEmail((String) pluginSettings.get(SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY));
    return super.doDefault();
  }

  @Override
  @SupportedMethods({RequestMethod.POST})
  public String doExecute() throws Exception {
    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    pluginSettings.put(SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY, getServiceAccountUserEmail());
    return SUCCESS;
  }
}
