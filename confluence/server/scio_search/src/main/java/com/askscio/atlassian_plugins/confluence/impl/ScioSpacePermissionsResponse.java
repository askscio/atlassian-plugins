package com.askscio.atlassian_plugins.confluence.impl;

import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSpacePermissionsResponse {
  public Map<String, List<String>> groups; // map from permission type to list of group names
  public Map<String, List<String>> users;  // map from permission type to list of user emails
}