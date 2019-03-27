package com.dant.app;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dant.utils.Log;

@Path("/table")

public class TableEndpoint {

    @POST
    @Path("/{tableName}/load/csv")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response loadCSV(String body) {
        Log.debug(body);
        return Response.status(200).entity("zbeb").build();
    }
}
