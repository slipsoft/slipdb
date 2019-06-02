package com.dant.app;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dant.entity.*;
import com.dant.utils.Log;
import db.data.load.CsvParser;
import db.search.SearchException;
import db.structure.Table;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Api("table")
@Path("/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TableEndpoint {

    @POST
    @Path("/{tableName}/load/csv")
    @Consumes("text/csv")
    public HttpResponse loadCSV(@PathParam("tableName") String tableName, String body) throws UnsupportedEncodingException {
        Optional<Table> tableOptional = Controller.getTableByName(tableName);
        Table table = tableOptional.orElseThrow(() -> new BadRequestException("table name is invalid"));
        InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
        table.loadData(new CsvParser(), is, true);
        return new HttpResponse("ok", "ok");

    }

    @POST
    @Path("{tableName}/search")
    public HttpResponse search(
            @PathParam("tableName") String tableName,
            @ApiParam(value = "content", required = true) ViewEntity view) throws SearchException {
        view.setTableName(tableName);
        return Controller.doSearch(view);
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
