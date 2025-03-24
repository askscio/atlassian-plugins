package ScioSearchConfigRestPlugin.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchInfoResponse {
  public UserInfo userInfo;
  public InstanceInfo instanceInfo;
  public ScioPluginInfo scioPluginInfo;

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
    public String version;
    public String baseUrl;
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class ScioPluginInfo {
    public String version;
    public String target;
  }
}
