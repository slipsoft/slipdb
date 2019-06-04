package db.structure;

import com.dant.utils.Log;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import network.Node;

public final class Database {

    private ArrayList<Table> allTables;
    public Config config;
    public ArrayList<Node> allNodes;

    private AtomicInteger nextTableID = new AtomicInteger(1);
	private AtomicInteger nextIndexTreeDicUniqueId = new AtomicInteger(1);

    private Database() {
        allTables = new ArrayList<>();
    }

    public Database getConfigFromFile() {
        try {
            FileReader configReader = new FileReader("config.json");
            Gson gson = new Gson();
            this.config = gson.fromJson(configReader, Config.class);
        } catch (Exception exp) {
            Log.error(exp);
        }
        return this;
    }

    public static Database getInstance() {
        return Init.INSTANCE;
    }

    private static class Init {
        static final Database INSTANCE = new Database().getConfigFromFile();
    }

    public ArrayList<Table> getAllTables() {
        return allTables;
    }

    /** Ecrire les données : SEULEMENT LES TABLES
     *  @param objectOutputStream output
     *  @throws IOException in case of IO failure
     */
    public void writeSerialData(ObjectOutputStream objectOutputStream) throws IOException {
    	objectOutputStream.writeObject(nextTableID);
    	objectOutputStream.writeObject(nextIndexTreeDicUniqueId);
		for (Table cTable : allTables) {
			cTable.doBeforeSerialWrite(); // flush des arbres sur le disque pour ne pas perdre de donnée
		}
		objectOutputStream.writeObject(allTables);
    }

    /** Lire les données : SEULEMENT LES TABLES
     *  @param objectInputStream input
     *  @throws IOException in case of IO failure
     */
    @SuppressWarnings("unchecked")
	public void readSerialData(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
    	nextTableID              = (AtomicInteger) objectInputStream.readObject();
    	nextIndexTreeDicUniqueId = (AtomicInteger) objectInputStream.readObject();
    	allTables                = (ArrayList<Table>) objectInputStream.readObject();
    }

    /*public void readAdditionalSerialData(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
    	nextTableID = (AtomicInteger) objectInputStream.readObject();
    }*/

    public int getAndIncrementNextTableID() {
    	return nextTableID.getAndIncrement();
    }
    public int getAndIncrementNextIndexTreeDicID() {
    	return nextIndexTreeDicUniqueId.getAndIncrement();
    }

}




