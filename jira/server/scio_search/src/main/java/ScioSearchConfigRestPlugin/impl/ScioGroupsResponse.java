package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import java.util.List;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioGroupsResponse {
  public String header;
  public List<JiraGroup> groups;

  public class JiraGroup {
    public String name;
    public List<JiraGroupLabel> labels;
  }

  public class JiraGroupLabel {
    public String text;
    public String title;
    public String type;
  }
}