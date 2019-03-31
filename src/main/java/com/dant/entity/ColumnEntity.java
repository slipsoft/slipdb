package com.dant.entity;

import com.dant.utils.Log;
import com.google.gson.Gson;
import db.data.types.DataType;
import db.structure.Column;
import db.structure.Database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static com.dant.utils.Utils.*;

public class ColumnEntity extends Entity implements Serializable {
    public String type;
    public int size;

    public ColumnEntity(String name, String type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void validate(ArrayList<ResponseError> errors) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
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

            if (DataTypes.get(this.type) == null) {
                errors.add(new ResponseError(Location.createTable, Type.invalidData, "Column type is invalid"));
            } else {
                String dataTypeClassName = DataTypesClassPathPrefix+DataTypes.get(this.type);
                Class dataTypeClass = Class.forName(dataTypeClassName);
                boolean sizeIsRequired = dataTypeClass.getField("sizeIsRequired").getBoolean(null);

                if(sizeIsRequired) {
                    if (this.size == 0) {
                        errors.add(new ResponseError(Location.createTable, Type.invalidData, "size is missing"));
                    } else if (this.size < 0) {
                        errors.add(new ResponseError(Location.createTable, Type.invalidData, "size is invalid"));
                    } else  {
                        int maxSizeInBytes = dataTypeClass.getField("maxSizeInBytes").getInt(null);

                        if(this.size > maxSizeInBytes) {
                            errors.add(new ResponseError(Location.createTable, Type.invalidData, "field size is too high, max size for " + this.type + " is: " + maxSizeInBytes ));
                        }
                    }
                } else if (!sizeIsRequired && this.size != 0) {
                    errors.add(new ResponseError(Location.createTable, Type.invalidData, "field size is not needed for " + this.type));
                }
            }
        }
    }

    public Column convertToColumn() {
        try {
            String DataTypesClassPathPrefix = Database.getInstance().config.DataTypesClassPathPrefix;
            String className = Database.getInstance().config.DataTypes.get(type);
            Class dataTypeClass = Class.forName(DataTypesClassPathPrefix+className);
            DataType dataType;
            if (dataTypeClass.getField("sizeIsRequired").getBoolean(null)) {
                dataType = (DataType)dataTypeClass.getDeclaredConstructor(int.class).newInstance(this.size);
            } else {
                dataType = (DataType)dataTypeClass.getDeclaredConstructor().newInstance();
            }
            return new Column(this.name, dataType);
        } catch (Exception exp) {
            Log.error(exp);
            throw new RuntimeException("unable to create table");
        }
    }
}
