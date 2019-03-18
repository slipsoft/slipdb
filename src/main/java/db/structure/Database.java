package db.structure;

import com.dant.entity.*;
import com.dant.exception.BadRequestException;
import com.dant.utils.Log;
import com.google.gson.Gson;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Database {

    private ArrayList<Table> allTables;
    public Config config;

    private Database() {
        allTables = new ArrayList<>();
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

    public void addTable(ArrayList<TableEntity> allTables) throws BadRequestException {

        ArrayList<String> errors = new ArrayList<>();
        Gson gson = new Gson();
        for (TableEntity table : allTables) {
            table.validate(errors);
        }

        if(errors.size() > 0) {
            // throw new Error blabla
        }
    }

    public ArrayList<Table> getTables() {
        return allTables;
    }
}