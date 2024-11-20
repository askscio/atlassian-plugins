package ScioSearchConfigRestPlugin.impl;

import java.util.List;
import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GroupMemberResponse {
    public int startAt;
    public int maxResults;
    public int total;
    public List<AtlassianUser> values;
}
