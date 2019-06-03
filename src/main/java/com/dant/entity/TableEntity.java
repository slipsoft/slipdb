package com.dant.entity;

import com.dant.utils.Log;
import com.dant.utils.Utils;
import com.google.gson.Gson;
import db.structure.Column;
import db.structure.Database;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;
import db.structure.Table;

import javax.ws.rs.BadRequestException;

public class TableEntity extends Entity implements Serializable {
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

    public void validate() {
        if (this.name == null || this.name.length() == 0) {
            throw new BadRequestException( "Table name property is missing");
        }
        if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.name)) {
            throw new BadRequestException( "Table name property is invalid: " + this.name);
        }
        if (this.allColumns == null || this.allColumns.size() == 0) {
            throw new BadRequestException( "column list is missing");
        } else {
            ArrayList<Entity> columnCheckForDuplicate = allColumns.stream().collect(Collectors.toCollection(ArrayList::new));
            boolean columnListHasDuplicates = allColumns.stream().filter(c -> Utils.isNameDuplicate(columnCheckForDuplicate, c.name)).collect(Collectors.toCollection(ArrayList::new)).size() <= 1;

            if (!columnListHasDuplicates){
                this.allColumns.stream().forEach(c -> {
                    try {
                        c.validate();
                    } catch (ReflectiveOperationException exp) {
                        Log.error(exp);
                        throw new RuntimeException(exp);
                    }
                });
            } else {
                throw new BadRequestException( "duplicates in column names");
            }
        }
    }

    public Table convertToTable() throws IOException {
        ArrayList<Column> allColumns = this.allColumns.stream().map(ColumnEntity::convertToColumn).collect(Collectors.toCollection(ArrayList::new));
        Table table = new Table(this.name);
        allColumns.forEach(c -> {
            try {
                table.addColumn(c);
            } catch (Exception exp) {
                Log.error(exp);
            }
        });
        return table;
    }
}
