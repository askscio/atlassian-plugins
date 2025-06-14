package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)


public class ScioServiceAccountConfigureResponse {
    private String serviceAccountEmail;

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }
}
