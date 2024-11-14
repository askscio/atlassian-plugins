package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class IssueSecuritySchemeResponse {
    public String id;
    public String name;
    public String description;
    public String defaultSecurityLevelId;
    public List<IssueSecurityLevel> levels;

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class IssueSecurityLevel {
        public String id;
        public String name;
        public String description;
    }

}
