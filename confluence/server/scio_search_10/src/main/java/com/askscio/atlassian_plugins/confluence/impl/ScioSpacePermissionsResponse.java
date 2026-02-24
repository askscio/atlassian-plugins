package com.askscio.atlassian_plugins.confluence.impl;

import java.util.List;
import java.util.Map;

public class ScioSpacePermissionsResponse {
  public List<String> anonymous; // list of permission types allowed by anonymous

  public Map<String, List<String>> groups; // map from permission type to list of group names

  public Map<String, List<String>> users; // map from permission type to list of user emails
}
