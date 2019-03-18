package com.dant.entity;

import com.google.gson.Gson;
import db.structure.Column;
import db.structure.Database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.dant.utils.Utils;
import db.structure.Index;
import db.structure.Table;

public class TableEntity {
    public String name;
    public ArrayList<ColumnEntity> allColumns;
    public ArrayList<IndexEntity> allIndexes;

    public TableEntity(String name, ArrayList<ColumnEntity> allColumns) {
        this.name = name;
        this.allColumns = allColumns;
    }

    public TableEntity(String name, ArrayList<ColumnEntity> allColumns, ArrayList<IndexEntity> allIndexes) {
        this.name = name;
        this.allColumns = allColumns;
        this.allIndexes = allIndexes;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void validate(ArrayList<String> errors) {
        Gson gson = new Gson();
        if (this.name == null) {
            errors.add(gson.toJson(new ResponseError(Location.createTable, Type.missingData, "name property is missing")));
        } else {
            if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.name)) {
                errors.add(gson.toJson(new ResponseError(Location.createTable, Type.invalidData, "name property is invalid: " + this.name)));
            }
        }
        if (this.allColumns == null) {
            errors.add(gson.toJson(new ResponseError(Location.createTable, Type.missingData, "column list is missing")));
        } else {
            this.allColumns.stream().forEach(c ->validate(errors));
        }
    }

    public Table convertToTable() throws IOException{
        ArrayList<Column> allColumns = this.allColumns.stream().map(ColumnEntity::convertToColumn).collect(Collectors.toCollection(ArrayList::new));
//        ArrayList<Index> allIndexes = this.allColumns.stream().map(IndexEntity::convertToIndex).collect(Collectors.toCollection(ArrayList::new));
        return new Table(this.name, allColumns);
    }
}
