package com.dant.app;

import com.dant.entity.HttpResponse;
import com.dant.entity.TableEntity;
import com.google.gson.Gson;
import db.structure.Database;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by pitton on 2017-02-20.
 */
@Path("/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DBEndpoint {

    @PUT
    @Path("/tables")
    public Response createTable(String body) {
        TableEntity table = new Gson().fromJson(body, TableEntity.class);
        Database.getInstance().addTable(table);
        return Response.status(200).build();
    }

    @GET
    @Path("/tables")
    public HttpResponse getTables() {
        return new HttpResponse("ok", "allTables", Database.getInstance().getTables());
    }

    @POST
    @Path("/tables/{tableName}")
    public void updateTable(@QueryParam("tableName") String tableName) {

        Response.status(200).build();
    }
}
