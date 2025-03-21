package ScioSearchConfigRestPlugin.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PermissionSchemeResponse {
  public String id;
  public String name;
  public String description;
  public List<JiraPermissionInfo> permissions;

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class JiraPermissionInfo {
    public String id;
    public JiraPermissionHolderInfo holder;
    public String permission;
  }

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class JiraPermissionHolderInfo {
    public String type;
    public String parameter;
    public AtlassianUser user;
    public Group group;
    public ScioProjectRole projectRole;
  }
}
