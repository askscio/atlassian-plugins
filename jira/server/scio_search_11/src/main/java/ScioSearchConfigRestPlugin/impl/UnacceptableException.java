package ScioSearchConfigRestPlugin.impl;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class UnacceptableException extends WebApplicationException {
  public UnacceptableException(String message) {
    super(Response.status(Status.NOT_ACCEPTABLE)
        .entity(message).type(MediaType.TEXT_PLAIN).build());
  }
}
