package com.dant.entity;

import com.google.gson.Gson;
import db.data.DataType;
import db.structure.Column;
import db.structure.Database;
import java.util.ArrayList;
import java.util.HashMap;

import static com.dant.utils.Utils.*;

public class ColumnEntity {
    public String name;
    public String type;

    public ColumnEntity(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void validate(ArrayList<String> errors) {
        Gson gson = new Gson();
        if (this.name == null) {
            errors.add(gson.toJson(new ResponseError(Location.createTable, Type.missingData, "Column name is missing")));
        } else {
            if (!validateRegex(Database.getInstance().config.columnNamePattern, this.name)) {
                errors.add(gson.toJson(new ResponseError(Location.createTable, Type.invalidData, "Column name is invalid: " + this.name)));
            }
        }
        if (this.type == null) {
            errors.add(gson.toJson(new ResponseError(Location.createTable, Type.missingData, "Type is missing")));
        } else {
            HashMap<String, String> DataTypes = Database.getInstance().config.DataTypes;
            if (!DataTypes.containsKey(this.type) || !validateClass(DataTypes.get(this.type))) {
                errors.add(gson.toJson(new ResponseError(Location.createTable, Type.invalidData, "Type is invalid: " + this.type)));
            }
        }
    }

    public Column convertToColumn() {
        try {
            Class dataTypeClass = Class.forName(type);
            DataType dataType = (DataType)dataTypeClass.getDeclaredConstructor().newInstance();
            return new Column(this.name, dataType);
        } catch (Exception exp) {
            return null;
        }
    }
}
