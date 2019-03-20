package com.dant.app;

import com.dant.entity.HttpResponse;
import com.dant.entity.Location;
import com.dant.entity.ResponseError;
import com.dant.entity.TableEntity;
import com.dant.exception.BadRequestException;
import com.dant.utils.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import db.structure.Database;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;

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
        Type tableListType = new TypeToken<ArrayList<TableEntity>>(){}.getType();
        try {
            ArrayList<TableEntity> allTables = new Gson().fromJson(body, tableListType);
            if (allTables.size() == 0) {
                throw new BadRequestException("No table found in request body");
            } else {
                return Database.getInstance().addTables(allTables);
            }
        } catch (Exception exp) { //Todo improve on error handling by catching only specific exception types
            Log.error(exp);
            throw new BadRequestException("invalid table body, check your args");
        }
    }

    @GET
    @Path("/tables")
    public Response getTables() {
        return Database.getInstance().getTables();
    }

    @GET
    @Path("/tables/{tableName}")
    public Response getTable(@PathParam("tableName") String tableName) {
        return Database.getInstance().getTable(tableName);
    }

    @DELETE
    @Path("/tables/{tableName}")
    public Response deleteTable(@PathParam("tableName") String tableName) {
        return Database.getInstance().deleteTable(tableName);
    }
}
