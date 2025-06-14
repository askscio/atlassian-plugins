package ScioSearchConfigRestPlugin.impl;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

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
