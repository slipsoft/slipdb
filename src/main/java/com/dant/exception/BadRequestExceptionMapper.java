package com.dant.exception;

import com.dant.entity.HttpResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by pitton on 2017-02-21.
 */
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(BadRequestException e) {
        return Response.status(400).entity(new HttpResponse("Error: bad request", e.getMessage())).type("application/json").build();
    }
}
