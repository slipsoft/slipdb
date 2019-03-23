package db.structure;

import com.dant.entity.*;
import com.dant.exception.BadRequestException;
import com.dant.utils.Log;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

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

    public Response addTables(ArrayList<TableEntity> allTableEntities) throws BadRequestException {
        ArrayList<ResponseError> errors = new ArrayList<>();

        ArrayList<String> names = new ArrayList<>();

        //a really dirty way of checking for duplicate, but it can always be improved later
        for (TableEntity table : allTableEntities) {
            if (table.name == null)
                continue;

            if (names.stream().anyMatch(n -> table.name.equals(n))) {
                errors.add(new ResponseError(Location.createTable, Type.invalidData, "table name is duplicate"));
            }

            if (this.allTables.stream().anyMatch(n -> table.name.equals(n.name))) {
                errors.add(new ResponseError(Location.createTable, Type.invalidData, "table name is duplicate"));
            }
            names.add(table.name);
        }

        allTableEntities.stream().forEach(t -> t.validate(errors));

        if(errors.size() > 0) {
            return com.dant.utils.Utils.generateResponse(400, "error", "application/json", errors);
        } else {
            ArrayList<Table> tablesToAdd = allTableEntities.stream().map(t -> {
                try {
                    return t.convertToTable();
                } catch (IOException exp) {
                    throw new RuntimeException("unable to create tables");
                }
            }).collect(Collectors.toCollection(ArrayList::new));
            tablesToAdd.stream().forEach(t -> allTables.add(t));
        }

        return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", "table successfully inserted");
    }

    public Response getTable(String tableName) {
        Table table = getTableByName(tableName);
        if (table != null) {
            return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", table.convertToEntity());
        } else {
            ResponseError error = new ResponseError(Location.getTable, Type.invalidData, "Table was not found");
            return com.dant.utils.Utils.generateResponse(400, "error", "application/json", error);
        }

    }

    public Response deleteTable(String tableName) {
        Table table = getTableByName(tableName);
        if (table != null) {
            allTables.remove(table);
            return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", "table successfully removed");
        } else {
            ResponseError error = new ResponseError(Location.deleteTable, Type.invalidData, "Table was not found");
            return com.dant.utils.Utils.generateResponse(400, "error", "application/json", error);
        }

    }

    public ArrayList<Table> getAllTables() {
        return allTables;
    }

    public Response getTables() {
        ArrayList<TableEntity> allTableEntities = allTables.stream().map(Table::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
        return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", allTableEntities);
    }

    public Table getTableByName(String tableName) {
        if (com.dant.utils.Utils.validateRegex(config.tableNamePattern, tableName)) {
            Optional<Table> tableOptional = allTables.stream().filter(t -> t.name.equals(tableName)).findFirst();
            if (tableOptional.isPresent()) {
                return tableOptional.get();
            }
        }
        return null;
    }
}