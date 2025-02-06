package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)


public class ScioServiceAccountConfigureResponse {
    private String serviceAccountUsername;

    public void setServiceAccountUsername(String serviceAccountUsername) {
        this.serviceAccountUsername = serviceAccountUsername;
    }

    public String getServiceAccountUsername() {
        return serviceAccountUsername;
    }
}
