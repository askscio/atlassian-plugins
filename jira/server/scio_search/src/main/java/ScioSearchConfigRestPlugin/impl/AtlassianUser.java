package ScioSearchConfigRestPlugin.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AtlassianUser {

  public String username;
  public String name;
  public String key;
  public boolean active;
  public String emailAddress;
  public String displayName;
}
