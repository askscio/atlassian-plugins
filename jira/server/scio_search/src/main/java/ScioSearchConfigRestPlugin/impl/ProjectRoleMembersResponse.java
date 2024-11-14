package ScioSearchConfigRestPlugin.impl;

import java.util.List;

public class ProjectRoleMembersResponse {
    public String name;
    public long id;
    public String description;
    public List<JiraRoleActorInfo> actors;
    
    public static class JiraRoleActorInfo {
        public String id;
        public String displayName;
        public String type;
        public String name;
    }
}

