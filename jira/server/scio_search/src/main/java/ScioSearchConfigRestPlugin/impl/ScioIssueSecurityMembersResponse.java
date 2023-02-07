package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
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
