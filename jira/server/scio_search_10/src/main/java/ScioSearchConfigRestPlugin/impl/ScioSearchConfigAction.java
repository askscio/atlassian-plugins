package ScioSearchConfigRestPlugin.impl;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

import static ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl.TARGET_CONFIG_KEY;

public class ScioSearchConfigAction extends JiraWebActionSupport {
    @JiraImport private PluginSettingsFactory pluginSettingsFactory;
    private String target;

    public ScioSearchConfigAction() {}

    @Inject
    public ScioSearchConfigAction(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void doValidation() {
        String targetUrl = getTarget();
        if(targetUrl == null || targetUrl.isEmpty()) {
            addErrorMessage("Target URL is required");
            return;
        }
        try {
            new URL(targetUrl);
        } catch (MalformedURLException e) {
            addErrorMessage("Invalid target URL");
        }
    }

    @Override
    public String doDefault() throws Exception {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        setTarget((String) pluginSettings.get(TARGET_CONFIG_KEY));
        return super.doDefault();
    }

    @Override
    public String doExecute() throws Exception {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(TARGET_CONFIG_KEY, getTarget());
        return SUCCESS;
    }
}
