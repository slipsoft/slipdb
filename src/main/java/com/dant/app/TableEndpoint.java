package com.dant.app;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.dant.entity.*;
import db.data.load.CsvParser;
import db.search.SearchException;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@Api("table")
@Path("/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TableEndpoint {

    @POST
    @Path("/{tableName}/load")
    @Consumes("text/csv")
    public HttpResponse loadCSV(
            @PathParam("tableName") String tableName,
            @ApiParam(value = "content", required = true) String body) throws UnsupportedEncodingException {
        if (body == null) {
            throw new BadRequestException("content cannot be null");
        }
        InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
        Controller.getTableByName(tableName).loadData(new CsvParser(), is);
        return new HttpResponse("ok");

    }

    @POST
    @Path("{tableName}/search")
    public HttpResponse search(
            @PathParam("tableName") String tableName,
            @ApiParam(value = "content", required = true) ViewEntity view) throws SearchException {
        if (view == null) {
            throw new BadRequestException("content cannot be null");
        }
        view.setTableName(tableName);
        return Controller.doSearch(view);
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tableName}/index")
    public HttpResponse addIndex(
            @PathParam("tableName") String tableName,
            @ApiParam(value = "content", required = true) IndexEntity indexEntity) throws IndexException {
        if (indexEntity == null) {
            throw new BadRequestException("content cannot be null");
        }
        Table table = Controller.getTableByName(tableName);
        Index index = indexEntity.convertToIndex(table);
        table.addIndex(index);
        return new HttpResponse("Oh yeah baby, l'index a bien été ajouté");
    }
}
