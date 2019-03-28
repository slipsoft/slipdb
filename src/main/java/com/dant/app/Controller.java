package com.dant.app;

import com.dant.entity.Location;
import com.dant.entity.ResponseError;
import com.dant.entity.TableEntity;
import com.dant.entity.Type;
import com.dant.exception.BadRequestException;
import com.dant.utils.Log;

import db.serial.SerialStructure;
import db.structure.Database;
import db.structure.Table;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class Controller {

    public static ArrayList<ResponseError> addTables(ArrayList<TableEntity> allTableEntities, boolean addImmediately) throws BadRequestException {

        ArrayList<ResponseError> errors = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        if (allTableEntities.size() == 0) {
            errors.add(new ResponseError(Location.createTable, Type.invalidData, "table array is empty"));
        }

        if (!addImmediately) {
            //a really dirty way of checking for duplicate, but it can always be improved later

            for (TableEntity table : allTableEntities) {
                if (table.name == null)
                    continue;

                if (names.stream().anyMatch(n -> table.name.equals(n))) {
                    errors.add(new ResponseError(Location.createTable, Type.invalidData, "table name is duplicate"));
                }

                if (Database.getInstance().getAllTables().stream().anyMatch(n -> table.name.equals(n.getName()))) {
                    errors.add(new ResponseError(Location.createTable, Type.invalidData, "table name is duplicate"));
                }
                names.add(table.name);
            }

            allTableEntities.stream().forEach(t -> t.validate(errors));

            if(errors.size() > 0) {
                return errors;
            }
        }

        ArrayList<Table> tablesToAdd = allTableEntities.stream().map(t -> {
            try {
                return t.convertToTable();
            } catch (IOException exp) {
                throw new RuntimeException("unable to create tables");
            }
        }).collect(Collectors.toCollection(ArrayList::new));

        tablesToAdd.stream().forEach(t -> Database.getInstance().getAllTables().add(t));
        
		SerialStructure.saveStructure();
        return null;
    }

    public static Response getTable(String tableName) {
        Optional<Table> tableOptional = getTableByName(tableName);
        if (tableOptional.isPresent()) {
            return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", tableOptional.get().convertToEntity());
        } else {
            ResponseError error = new ResponseError(Location.getTable, Type.invalidData, "Table was not found");
            return com.dant.utils.Utils.generateResponse(400, "error", "application/json", error);
        }

    }

    public static Response deleteTable(String tableName) {
        Optional tableOptional = getTableByName(tableName);
        if (tableOptional.isPresent()) {
            Database.getInstance().getAllTables().remove(tableOptional.get());
            return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", "table successfully removed");
        } else {
            ResponseError error = new ResponseError(Location.deleteTable, Type.invalidData, "Table was not found");
            return com.dant.utils.Utils.generateResponse(400, "error", "application/json", error);
        }
    }


    public static Response getTables() {
        ArrayList<TableEntity> allTableEntities = Database.getInstance().getAllTables().stream().map(Table::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
        return com.dant.utils.Utils.generateResponse(200, "ok", "application/json", allTableEntities);
    }

    public static Optional<Table> getTableByName(String tableName) {
        if (com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, tableName)) {
            return Database.getInstance().getAllTables().stream().filter(t -> t.getName().equals(tableName)).findFirst();
        }
        return Optional.empty();
    }

}
