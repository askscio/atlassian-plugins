package ScioSearchConfigRestPlugin.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ScioProjectRole {
    public String id;
    public String name;
    public String description;
}
