package com.dant.app;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dant.entity.*;
import com.dant.exception.BadRequestException;
import com.dant.utils.Log;
import db.data.load.CsvParser;
import db.structure.Table;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

@Api("table")
@Path("/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TableEndpoint {

    @POST
    @Path("/{tableName}/load/csv")
    @Consumes("text/csv")
    public Response loadCSV(@PathParam("tableName") String tableName, String body) {
        try {
            Optional<Table> tableOptional = Controller.getTableByName(tableName);
            if (tableOptional.isPresent()) {
                InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
                tableOptional.get().loadData(new CsvParser(), is, true);
                return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", "ok");

            } else {
                return com.dant.utils.Utils.generateResponse(500, "error", "application/json", new ResponseError(Location.loadCSV, Type.invalidData, "table name is invalid"));
            }

        } catch (Exception exp) {
            Log.error(exp);
        }
        return com.dant.utils.Utils.generateResponse(500, "error", "application/json", new ResponseError(Location.loadCSV, Type.invalidData, "error"));
    }

    @POST
    @Path("{tableName}/search")
    public Response search(
            @PathParam("tableName") String tableName,
            @ApiParam(value = "content", required = true) ViewEntity view) {
        view.setTableName(tableName);
        try {
            return Controller.doSearch(view);

        } catch (Exception exp) {
            Log.error(exp);
            return com.dant.utils.Utils.generateResponse(500, "non", "non", "non");
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tableName}/index")
    public HttpResponse addIndex(
            @PathParam("tableName") String tableName,
            @ApiParam(value = "content", required = true) IndexEntity index) {
        Log.debug(index);
        if (index == null) {
            throw new BadRequestException("body cannot be null");
        }
        return new HttpResponse("Oh yeah baby", null);
    }
}
