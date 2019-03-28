package com.dant.app;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.dant.entity.Location;
import com.dant.entity.ResponseError;
import com.dant.entity.Type;
import com.dant.utils.Log;
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
                tableOptional.get().getTableHandler().parseCsvData(is, true);
            } else {
                return com.dant.utils.Utils.generateResponse(500, "error", "application/json", new ResponseError(Location.loadCSV, Type.invalidData, "table name is invalid"));
            }

        } catch (Exception exp) {
            Log.error(exp);
        }
        return com.dant.utils.Utils.generateResponse(500, "error", "application/json", new ResponseError(Location.loadCSV, Type.invalidData, "error"));
    }
}
