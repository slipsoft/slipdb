package com.dant.app;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.crypto.Data;

import com.dant.entity.*;
import db.data.load.CsvParser;
import db.search.SearchException;
import db.structure.Database;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import network.Network;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
            @ApiParam(value = "content", required = true) String body,
            @DefaultValue("null") @HeaderParam("InternalToken") String InternalToken) throws UnsupportedEncodingException {
        if (body == null) {
            throw new BadRequestException("content cannot be null");
        }
        if (InternalToken.equals("null")) {

            if (Database.getInstance().allNodes.size() > 0) {
                String[] lines = body.split("\\r?\\n");
                Integer nodeCount = Database.getInstance().allNodes.size() + 1;
                ArrayList<ArrayList<String>> fileSplitBynodes = new ArrayList<>();
                Integer currentNode = 0;

                for (int i = 0; i < lines.length; i++) {

                    fileSplitBynodes.get(currentNode).add(lines[i]);
                    currentNode = (currentNode + 1) % nodeCount;
                }

                ArrayList<String> toSend = fileSplitBynodes.stream().map(strings -> {
                    StringBuffer buffer = new StringBuffer();
                    strings.stream().forEach(s -> buffer.append(s));
                    return buffer.toString();
                }).collect(Collectors.toCollection(ArrayList::new));
                String forLocal = toSend.remove(0);

                Network.sendForParsing(toSend);

                InputStream is = new ByteArrayInputStream(forLocal.getBytes());
                Controller.getTableByName(tableName).loadData(new CsvParser(), is);
                return new HttpResponse("ok");
            }
            InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
            Controller.getTableByName(tableName).loadData(new CsvParser(), is);
            return new HttpResponse("ok");

        } else if (InternalToken.equals(Database.getInstance().config.SuperSecretPassphrase)) {
            InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
            Controller.getTableByName(tableName).loadData(new CsvParser(), is);
            return new HttpResponse("ok");
        } else {
            throw new RuntimeException("laTarentule");
        }
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
