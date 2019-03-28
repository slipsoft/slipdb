package com.dant.entity;

import com.google.gson.Gson;
import db.structure.Column;
import db.structure.Index;

import java.io.Serializable;

public class IndexEntity extends Entity implements Serializable {
    public ColumnEntity[] columnsToIndex;

    public IndexEntity (ColumnEntity[] columnsToIndex) {
        this.columnsToIndex = columnsToIndex;
    }

/*    public Index convertToIndex() {
        Column[] indexedColumnsList = this.columnsToIndex.map(ColumnEntity::convertToColumn).toArray(Column[]::new);
        return new Index(indexedColumnsList);
    }*/

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
