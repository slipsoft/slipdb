package com.dant.app;

import com.dant.entity.*;
import com.dant.utils.Log;
import db.search.ResultSet;
import db.search.SearchException;
import db.search.View;
import db.serial.SerialStructure;
import db.structure.Database;
import db.structure.Table;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class Controller {

    public static void addTables(ArrayList<TableEntity> allTableEntities, boolean addImmediately) {

        ArrayList<String> names = new ArrayList<>();

        if (allTableEntities.size() == 0) {
            throw new BadRequestException( "table array is empty");
        }

        if (!addImmediately) {
            //a really dirty way of checking for duplicate, but it can always be improved later

            for (TableEntity table : allTableEntities) {
                if (table.name == null)
                    continue;

                if (names.stream().anyMatch(n -> table.name.equals(n))) {
                    throw new BadRequestException( "table name is duplicate");
                }

                if (Database.getInstance().getAllTables().stream().anyMatch(n -> table.name.equals(n.getName()))) {
                    throw new BadRequestException( "table name is duplicate");
                }
                names.add(table.name);
            }

            allTableEntities.stream().forEach(t -> t.validate());
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
    }

    public static HttpResponse getTable(String tableName) {
        Table table = Controller.getTableByName(tableName);
        return new HttpResponse("ok", table.convertToEntity());
    }

    public static HttpResponse deleteTable(String tableName) {
        Table table = Controller.getTableByName(tableName);
        Database.getInstance().getAllTables().remove(table);
        return new HttpResponse( "ok", "table successfully removed");
    }


    public static HttpResponse getTables() {
        ArrayList<TableEntity> allTableEntities = Database.getInstance().getAllTables().stream().map(Table::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
        return new HttpResponse( "ok",  allTableEntities);
    }

    public static Table getTableByName(String tableName) {
        if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, tableName)) {
            throw new BadRequestException("table name is invalid: " + tableName);
        }
        Optional<Table> tableOptional = Database.getInstance().getAllTables().stream().filter(t -> t.getName().equals(tableName)).findFirst();
        return tableOptional.orElseThrow(() -> new BadRequestException("table not found: " + tableName));
    }

    public static HttpResponse doSearch(ViewEntity viewEntity) throws SearchException {
        viewEntity.validate();
        View viewToExecute = viewEntity.convertToView();
        ResultSet resultSet = viewToExecute.execute();
        return new HttpResponse( "ok", resultSet);

    }

}
