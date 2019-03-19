package com.dant.app;

import com.dant.entity.Account;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DbEndpoint {
    @PUT
    @Path("/tables/{tableName}")
    public Response insertTable(@PathParam("tableName") String tableName, String body) {

        return new Response();
    }
}
