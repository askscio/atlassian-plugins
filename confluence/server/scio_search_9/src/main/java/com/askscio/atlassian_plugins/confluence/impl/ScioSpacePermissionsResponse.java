package com.askscio.atlassian_plugins.confluence.impl;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSpacePermissionsResponse {
  @JsonProperty("Anonymous")
  public List<String> anonymous; // list of permission types allowed by anonymous

  @JsonProperty("Groups")
  public Map<String, List<String>> groups; // map from permission type to list of group names

  @JsonProperty("Users")
  public Map<String, List<String>> users;  // map from permission type to list of user emails
}