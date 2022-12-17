package com.askscio.atlassian_plugins.confluence.impl;

import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSpacePermissionsResponse {
  @JsonProperty("Anonymous")
  public List<String> anonymous; // list of permission types allowed by anonymous

  @JsonProperty("Groups")
  public Map<String, List<String>> groups; // map from permission type to list of group names

  @JsonProperty("Users")
  public Map<String, List<String>> users;  // map from permission type to list of user emails
}