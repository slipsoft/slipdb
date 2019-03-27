package com.dant.app;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dant.utils.Log;
import db.parsers.CsvParser;
import db.structure.Database;
import db.structure.Table;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Path("/table")

public class TableEndpoint {

    @POST
    @Path("/{tableName}/load/csv")
    @Consumes("text/csv")
    public Response loadCSV(@PathParam("tableName") String tableName, String body) {
        try {
            Table tableSchema = Database.getInstance().getTableByName(tableName);
            CsvParser parser = new CsvParser(tableSchema);
            InputStream in = new ByteArrayInputStream(body.getBytes("UTF-8"));
            // ici on appelle le tableHandler
            if (tableSchema == null) {
                return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", "table successfully inserted");
            }
            return Response.status(200).entity("zbeb").build();

        } catch (Exception exp) {
            Log.error(exp);
        }
    }
}
