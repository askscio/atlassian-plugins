package com.askscio.atlassian_plugins.confluence.impl;

import java.util.List;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchGroupMembersResponse {
  @JsonProperty("Usernames")
  public List<String> usernames; // list of user names of the memebers
}
