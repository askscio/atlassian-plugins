package ScioSearchConfigRestPlugin.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class UnacceptableException extends WebApplicationException {
  public UnacceptableException(String message) {
    super(
        Response.status(Status.NOT_ACCEPTABLE).entity(message).type(MediaType.TEXT_PLAIN).build());
  }
}
