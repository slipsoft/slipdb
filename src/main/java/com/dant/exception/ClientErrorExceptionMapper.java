package com.dant.exception;

import com.dant.entity.HttpResponse;
import db.search.SearchException;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by Peugnet on 2017-02-21.
 */
@Provider
public class ClientErrorExceptionMapper implements ExceptionMapper<ClientErrorException> {

    @Override
    public Response toResponse(ClientErrorException e) {
        return Response
                .status(e.getResponse().getStatus())
                .entity(new HttpResponse(false, "Bad Request Error: " + e.getMessage()))
                .type("application/json")
                .build();
    }
}
