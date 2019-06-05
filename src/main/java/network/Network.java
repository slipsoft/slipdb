package network;

import db.structure.Database;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Network {

    public static CompletableFuture<List<java.net.http.HttpResponse<String>>> broadcast(String endpoint, String method) {
        ArrayList<Node> allNodes = Database.getInstance().allNodes;
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        ArrayList<HttpRequest> allRequests = allNodes.stream().map(node -> HttpRequest.newBuilder()
                        .uri(URI.create(node.address + ":" + node.port + endpoint))
                        .headers("Content-Type", "application/json", "InternalToken", Database.getInstance().config.SuperSecretPassphrase)
                        .method(method, HttpRequest.BodyPublishers.noBody())
                        .build()
        ).collect(Collectors.toCollection(ArrayList::new));

        List<CompletableFuture<HttpResponse<String>>> completableFutures = allRequests.stream().map(request ->
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        ).collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));

        return allFutures.thenApply(future ->
            completableFutures.stream()
                    .map(completableFuture -> completableFuture.join())
                    .collect(Collectors.toList())
        );
    }

    public static CompletableFuture<List<java.net.http.HttpResponse<String>>> sendForParsing(ArrayList<String> strings) {
        return null;
    }

    public static CompletableFuture<List<java.net.http.HttpResponse<String>>> broadcast(String endpoint, String method, String body) {
        ArrayList<Node> allNodes = Database.getInstance().allNodes;
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        ArrayList<HttpRequest> allRequests = allNodes.stream().map(node -> HttpRequest.newBuilder()
                .uri(URI.create(node.address + ":" + node.port + endpoint))
                .headers("Content-Type", "application/json", "InternalToken", Database.getInstance().config.SuperSecretPassphrase)
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build()
        ).collect(Collectors.toCollection(ArrayList::new));

        List<CompletableFuture<HttpResponse<String>>> completableFutures = allRequests.stream().map(request ->
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        ).collect(Collectors.toList());
        // met toutes mes completableFutures dans une liste

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        // appelle la fonction allOff pour retourner 1 seul objet Future

        return allFutures.thenApply(future ->
                completableFutures.stream()
                        .map(completableFuture -> completableFuture.join())
                        .collect(Collectors.toList())
        );
        // on met tous les resultats dans une liste qu'on passe à un nouveau Future
    }


}
