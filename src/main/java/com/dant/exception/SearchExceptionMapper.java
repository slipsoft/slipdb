package com.dant.exception;

import com.dant.entity.HttpResponse;
import com.google.gson.JsonSyntaxException;
import db.search.SearchException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by Peugnet on 2017-02-21.
 */
@Provider
public class SearchExceptionMapper implements ExceptionMapper<SearchException> {

    @Override
    public Response toResponse(SearchException e) {
        return Response.status(500).entity(new HttpResponse(false, "Search Error: " + e.getMessage())).type("application/json").build();
    }
}
