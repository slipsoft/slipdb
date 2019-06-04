package network;

import db.structure.Database;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Network {

    public static void broadcast(String endpoint, Serializable body) {

    }

    void sendToSlave(String csvToSend) { //envoie le csv à un esclave, ne se précouppe pas de savoir lequel

    }

}
