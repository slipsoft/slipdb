package com.dant.app;

import com.dant.entity.HttpResponse;
import com.dant.entity.TableEntity;
import com.google.gson.JsonSyntaxException;
import db.structure.Database;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import network.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;


@Api("db")
@Path("/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DBEndpoint {

    @GET
    @Path("/check")
    public HttpResponse checkNode(String superSecretPassPhrase) {
        if (Database.getInstance().config.SuperSecretPassphrase != superSecretPassPhrase) {
            throw new JsonSyntaxException("passPhraseDoesNotMatch");
        }
        return new HttpResponse("ok");
    }

    // Sans doute un moyen de la condenser qcio
    @PUT
    @Path("/addNode")
    public void addNodes(@ApiParam(value = "nodeData", required = true) List<Node> allNodes, final @Suspended AsyncResponse responseToClient) {
        try {
            List<CompletableFuture<java.net.http.HttpResponse<String>>> completableFutures = allNodes.stream().map(Node::checkNode).collect(Collectors.toList());
            // met toutes mes completableFutures dans une liste

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            // appelle la fonction allOff pour retourner 1 seul objet Future

            CompletableFuture<List<java.net.http.HttpResponse<String>>> allCompletableFutures = allFutures.thenApply(future -> {
                return completableFutures.stream()
                        .map(completableFuture -> completableFuture.join())
                        .collect(Collectors.toList());
            });
            // on met tous les resultats dans une liste qu'on passe à un nouveau Future

            allCompletableFutures.thenAccept(responses -> {
                responses.stream().forEach(response -> {
                    if (response.statusCode() != 200) {
                        throw new JsonSyntaxException("node could not be validated");
                    }
                    responseToClient.resume("ok");
                });
            });
        } catch (Exception exp) {
            responseToClient.resume(exp);
        }
    }

    @PUT
    @Path("/tables")
    public HttpResponse createTables(
            @ApiParam(value = "content", required = true) ArrayList<TableEntity> allTables,
            @DefaultValue("null") @HeaderParam("InternalToken") String InternalToken) {
        // si la requète vient d'un endpoint, pas besoin de valider
        boolean addImmediatly = InternalToken.equals(Database.getInstance().config.SuperSecretPassphrase);
        Controller.addTables(allTables, addImmediatly);
        return new HttpResponse("table successfully inserted");
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
