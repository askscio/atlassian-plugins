package com.askscio.atlassian_plugins.confluence.impl;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioSearchGroupMembersResponse {
  @JsonProperty("UserNames")
  public List<String> usernames; // list of user names of the memebers
}
