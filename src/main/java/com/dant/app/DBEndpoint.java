package com.dant.app;

import com.dant.entity.HttpResponse;
import com.dant.entity.TableEntity;
import db.structure.Database;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;


@Api("db")
@Path("/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DBEndpoint {

    @PUT
    @Path("/tables")
    public HttpResponse createTables(
            @ApiParam(value = "content", required = true) ArrayList<TableEntity> allTables,
            @DefaultValue("null") @HeaderParam("InternalToken") String InternalToken) {
        // si la requète vient d'un endpoint, pas besoin de valider
        boolean addImmediatly = InternalToken.equals(Database.getInstance().config.SuperSecretPassphrase);
        Controller.addTables(allTables, addImmediatly);
        return new HttpResponse("ok", "table successfully inserted");
    }

    @GET
    @Path("/tables")
    public HttpResponse getTables() {
        return Controller.getTables();
    }

    @GET
    @Path("/tables/{tableName}")
    public HttpResponse getTable(@PathParam("tableName") String tableName) {
        return Controller.getTable(tableName);
    }

    @DELETE
    @Path("/tables/{tableName}")
    public HttpResponse deleteTable(@PathParam("tableName") String tableName) {
        return Controller.deleteTable(tableName);
    }
}
