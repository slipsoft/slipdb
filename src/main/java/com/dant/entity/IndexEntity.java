package com.dant.entity;

import db.structure.Column;
import db.structure.Index;

public class IndexEntity {
    public ColumnEntity[] columnsToIndex;

    public IndexEntity (ColumnEntity[] columnsToIndex) {
        this.columnsToIndex = columnsToIndex;
    }

/*    public Index convertToIndex() {
        Column[] indexedColumnsList = this.columnsToIndex.map(ColumnEntity::convertToColumn).toArray(Column[]::new);
        return new Index(indexedColumnsList);
    }*/
}
