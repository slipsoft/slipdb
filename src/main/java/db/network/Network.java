package db.network;

import db.structure.Database;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Network {

    public static boolean broadcast(String endpoint, Serializable body) {
        Client client = ClientBuilder.newClient();
        ArrayList<WebTarget> allTargets = Database.getInstance().allNodes.stream().map(n -> client.target(n.address + endpoint)).collect(Collectors.toCollection(ArrayList::new));
        allTargets.stream().forEach(t -> {
            t.request().post(Entity.entity("cul", "application/json"));
            /*TODO proper HTTP request*/
        });
        return true;
    }

    public static boolean CheckNode() { return true; }

}
