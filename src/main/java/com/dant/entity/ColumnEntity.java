package com.dant.entity;

import com.dant.utils.Log;
import com.google.gson.Gson;
import db.data.DataType;
import db.structure.Column;
import db.structure.Database;
import java.util.ArrayList;
import java.util.HashMap;

import static com.dant.utils.Utils.*;

public class ColumnEntity extends Entity{
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

    public void validate(ArrayList<ResponseError> errors) {
        if (this.name == null || this.name.length() == 0) {
            errors.add(new ResponseError(Location.createTable, Type.missingData, "Column name is missing"));
        } else {
            if (!validateRegex(Database.getInstance().config.columnNamePattern, this.name)) {
                errors.add(new ResponseError(Location.createTable, Type.invalidData, "Column name is invalid: " + this.name));
            }
        }
        if (this.type == null) {
            errors.add(new ResponseError(Location.createTable, Type.missingData, "Column Type is missing"));
        } else {
            HashMap<String, String> DataTypes = Database.getInstance().config.DataTypes;
            String DataTypesClassPathPrefix = Database.getInstance().config.DataTypesClassPathPrefix;
            Log.debug(validateClass(DataTypesClassPathPrefix+DataTypes.get(this.type)),DataTypesClassPathPrefix+DataTypes.get(this.type));
            if (!DataTypes.containsKey(this.type) || !validateClass(DataTypesClassPathPrefix+DataTypes.get(this.type))) {
                errors.add(new ResponseError(Location.createTable, Type.invalidData, "Column Type is invalid: " + this.type));
            }
        }
    }

    public Column convertToColumn() {
        try {
            // TODO si c'est une string, véiifier la présence d'un argument size
            String DataTypesClassPathPrefix = Database.getInstance().config.DataTypesClassPathPrefix;
            String className = Database.getInstance().config.DataTypes.get(type);
            Class dataTypeClass = Class.forName(DataTypesClassPathPrefix+className);
            DataType dataType = (DataType)dataTypeClass.getDeclaredConstructor().newInstance();
            return new Column(this.name, dataType);
        } catch (Exception exp) {
            Log.debug(exp);
            throw new RuntimeException("unable to create table");
        }
    }
}
