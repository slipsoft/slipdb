package com.dant.exception;

import com.dant.entity.HttpResponse;
import com.google.gson.JsonSyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by Peugnet on 2017-02-21.
 */
@Provider
public class JsonSyntaxExceptionMapper implements ExceptionMapper<JsonSyntaxException> {

    @Override
    public Response toResponse(JsonSyntaxException e) {
        return Response.status(400).entity(new HttpResponse(false, "Bad Request Error: " + e.getMessage())).type("application/json").build();
    }
}
