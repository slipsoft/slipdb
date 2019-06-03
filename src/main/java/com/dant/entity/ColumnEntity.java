package com.dant.entity;

import com.dant.utils.Log;
import com.google.gson.Gson;
import db.data.types.DataType;
import db.structure.Column;
import db.structure.Database;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static com.dant.utils.Utils.*;

public class ColumnEntity extends Entity implements Serializable {
    public TypeEnum type;
    public int size;

    public ColumnEntity(String name, TypeEnum type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void validate() throws NoSuchFieldException, IllegalAccessException {
        if (this.name == null || this.name.length() == 0) {
            throw new BadRequestException( "Column name is missing");
        } else {
            if (!validateRegex(Database.getInstance().config.columnNamePattern, this.name)) {
                throw new BadRequestException( "Column name is invalid: " + this.name);
            }
        }
        if (this.type == null) {
            throw new BadRequestException("Column Type is missing for column: " + this.name);
        }
        Class dataTypeClass = this.type.convertToClass();
        boolean sizeIsRequired = dataTypeClass.getField("sizeIsRequired").getBoolean(null);

        if(sizeIsRequired) {
            if (this.size == 0) {
                throw new BadRequestException( "size is missing for column: " + this.name);
            } else if (this.size < 0) {
                throw new BadRequestException( "size is invalid for column: " + this.name);
            } else  {
                int maxSizeInBytes = dataTypeClass.getField("maxSizeInBytes").getInt(null);

                if(this.size > maxSizeInBytes) {
                    throw new BadRequestException( "field size is too high, max size for " + this.type + " is: " + maxSizeInBytes );
                }
            }
        }
    }

    public Column convertToColumn() {
        try {
            Class dataTypeClass = this.type.convertToClass();
            DataType dataType;
            if (dataTypeClass.getField("sizeIsRequired").getBoolean(null)) {
                dataType = (DataType)dataTypeClass.getDeclaredConstructor(int.class).newInstance(this.size);
            } else {
                dataType = (DataType)dataTypeClass.getDeclaredConstructor().newInstance();
            }
            return new Column(this.name, dataType);
        } catch (ReflectiveOperationException exp) {
            Log.error(exp);
            throw new RuntimeException("unable to create table");
        }
    }
}
