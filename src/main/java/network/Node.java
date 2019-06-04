package network;

import db.structure.Database;

import javax.xml.crypto.Data;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


public class Node {

    public String name;
    public String address;
    public int port;

    public Node(String argName, String argAddress, int argPort) {
        name = argName;
        address = argAddress;
        port = argPort;
    }

    public CompletableFuture<HttpResponse<String>> checkNode() {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpRequest checkRequest = HttpRequest.newBuilder()
                .uri(URI.create(address + ":" + port + "/db/check"))
                .headers("Content-Type", "application/json", "InternalToken", Database.getInstance().config.SuperSecretPassphrase)
                .GET()
                .build();
        return client.sendAsync(checkRequest, HttpResponse.BodyHandlers.ofString());

    }
}
