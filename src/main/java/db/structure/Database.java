package db.structure;

import com.dant.utils.Log;
import com.google.gson.Gson;
import java.io.FileReader;
import java.util.ArrayList;
import db.network.Node;

public final class Database {

    private ArrayList<Table> allTables;
    public Config config;
    public ArrayList<Node> allNodes;

    private Database() {
        allTables = new ArrayList<>();
    }

    public void getConfigFromFile() {
        try {
            FileReader configReader = new FileReader("config.json");
            Gson gson = new Gson();
            this.config = gson.fromJson(configReader, Config.class);
        } catch (Exception exp) {
            Log.error(exp);
        }
    }

    public static Database getInstance() {
        return Init.INSTANCE;
    }

    private static class Init {
        public static final Database INSTANCE = new Database();
    }

    public ArrayList<Table> getAllTables() {
        return allTables;
    }
}