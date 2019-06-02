package com.dant.exception;

import com.dant.entity.HttpResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by pitton on 2017-02-21.
 */
@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	@Override
	public Response toResponse(RuntimeException e) {
		return Response.status(500).entity(new HttpResponse("Unknown Server Error", e.getMessage())).type("application/json").build();
	}
}
