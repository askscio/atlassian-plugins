package com.askscio.atlassian_plugins.confluence.impl;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class UnauthorizedException extends WebApplicationException {
  public UnauthorizedException(String message) {
    super(Response.status(Response.Status.UNAUTHORIZED)
        .entity(message).type(MediaType.TEXT_PLAIN).build());
  }
}
