package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioIssueSecurityMembersResponse {
  public int startAt;
  public int total;
  public int maxResults;
  public boolean isLast;
  public List<IssueSecuritySchemeMemberInfo> values;

  public static class IssueSecuritySchemeMemberInfo {
    public String id;
    public String issueSecurityLevelId;
    public JiraPermissionHolderInfo holder;
  }

  public static class JiraPermissionHolderInfo {
    public String type;
    public String parameter;
    public AtlassianUser user;
    public AtlassianGroup group;
    public JiraProjectRole projectRole;
    public JiraCustomField field;
  }

  public static class AtlassianUser {
    public String accountId;
  
    public String username;
    public String name;
    public String key;
    public boolean active;
    public String accountType;
  
    public String emailAddress;
  
    public String displayName;
  }
  
  public static class AtlassianGroup {
    public String name;
  }

  public static class JiraProjectRole {
    public String name;
    public String id;
    public String description;
  
    public JiraProjectRoleScope scope;
  
    public static class JiraProjectRoleScope {
      public String type; // "PROJECT"
      public ProjectInfo project;
    }
  }

  public static class JiraCustomField {
    public String id;
    public String key;
    public String name;
    public String[] clauseNames;
  }
}
