package ScioSearchConfigRestPlugin.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.util.List;

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
  }
}
