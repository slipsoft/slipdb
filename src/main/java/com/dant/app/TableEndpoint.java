package com.dant.app;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dant.entity.*;
import com.dant.utils.Log;
import db.data.load.CsvParser;
import db.structure.Table;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

@Path("/table")

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@PathParam("tableName") String tableName, ViewEntity view) {
        view.setTableName(tableName);
        try {
            return Controller.doSearch(view);

        } catch (Exception exp) {
            Log.error(exp);
            return com.dant.utils.Utils.generateResponse(500, "non", "non", "non");
        }
    }
}
