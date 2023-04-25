package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchInfoResponse {
  public UserInfo userInfo;
  public InstanceInfo instanceInfo;

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class UserInfo {
    public String userKey;
    public String userName;
    public String fullName;
    public String email;
    public boolean isAdmin;
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class InstanceInfo {
    String version;
    String baseUrl;
  }
}
