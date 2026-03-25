package ScioSearchConfigRestPlugin.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ScioConfigResponse {
  private String target;

  public void setTarget(String target) {
    this.target = target;
  }

  public String getTarget() {
    return target;
  }
}
