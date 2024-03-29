package com.dant.app;

import com.dant.entity.HttpResponse;
import com.dant.entity.TableEntity;
import com.dant.utils.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import db.structure.Database;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import network.Network;
import network.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;


@Api("db")
@Path("/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DBEndpoint {

    @GET
    @Path("/check")
    public HttpResponse checkNode(@DefaultValue("null") @HeaderParam("InternalToken") String InternalToken) {
        if (!Database.getInstance().config.SuperSecretPassphrase.equals(InternalToken)) {
            throw new JsonSyntaxException("passPhraseDoesNotMatch");
        }
        return new HttpResponse("ok");
    }
    @PUT
    @Path("/addNode")
    public void addNodes(@ApiParam(value = "nodeData", required = true) List<Node> allNodes, final @Suspended AsyncResponse responseToClient) {
        //TODO check for duplicates && remove the "http://" part
        List<CompletableFuture<java.net.http.HttpResponse<String>>> completableFutures = allNodes.stream().map(Node::checkNode).collect(Collectors.toList());
        // met toutes mes completableFutures dans une liste

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        // appelle la fonction allOff pour retourner 1 seul objet Future

        CompletableFuture<List<java.net.http.HttpResponse<String>>> allCompletableFutures = allFutures.thenApply(future ->
                completableFutures.stream()
                    .map(completableFuture -> completableFuture.join())
                    .collect(Collectors.toList())
        );
        // on met tous les resultats dans une liste qu'on passe à un nouveau Future

        allCompletableFutures.thenAccept(responses -> {
            responses.stream().forEach(response -> {
                if (response.statusCode() != 200) {
                    throw new JsonSyntaxException("one or more nodes could not be validated " + response.request().uri() + "error code " + response.statusCode() + " " + response.body());
                }
                responseToClient.resume(new HttpResponse("ok"));
                Database.getInstance().allNodes.addAll(allNodes);
            });
        });
    }

    @PUT
    @Path("/tables")
    public void createTables(
            @ApiParam(value = "content", required = true) ArrayList<TableEntity> allTables,
            @DefaultValue("null") @HeaderParam("InternalToken") String InternalToken, final @Suspended AsyncResponse responseToClient) {
        // si la requète vient d'un endpoint, pas besoin de valider
        boolean addImmediatly = InternalToken.equals(Database.getInstance().config.SuperSecretPassphrase);
        Controller.addTables(allTables, addImmediatly);
        if (!addImmediatly && Database.getInstance().allNodes.size() > 0) {
            Gson gson = new Gson();
            String body = gson.toJson(allTables);
            Network.broadcast("/db/tables", "PUT", body).thenAccept(responses -> {
                responses.stream().forEach(response -> {
                    if (response.statusCode() != 200) {
                        responseToClient.resume(new JsonSyntaxException("error when broadcasting to node " + response.statusCode() + " " +response.body() + response.uri()));
                    }
                    responseToClient.resume(new HttpResponse("ok"));
                });
            });
        } else {
            responseToClient.resume(new HttpResponse("ok"));
        }
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
    public void deleteTable(@PathParam("tableName") String tableName, final @Suspended AsyncResponse responseToClient) {
        Controller.deleteTable(tableName);
        if (Database.getInstance().allNodes.size() > 0) {
            Network.broadcast("/db/tables/" + tableName, "DELETE").thenAccept(responses ->
                    responses.stream().forEach(response -> {
                        if (response.statusCode() != 200) {
                            throw new JsonSyntaxException("the table on node" + response.request().uri() + "could not be deleted");
                        }
                        responseToClient.resume(new HttpResponse("ok"));
                    })
            );
        } else {
            responseToClient.resume(new HttpResponse("ok"));
        }

    }
}
