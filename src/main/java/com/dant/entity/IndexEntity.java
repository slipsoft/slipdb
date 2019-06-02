package com.dant.entity;

import com.google.gson.Gson;
import db.structure.Column;
import db.structure.Index;
import db.structure.Table;
import index.indexTree.IndexException;
import index.indexTree.IndexTreeDic;
import index.memDic.IndexMemDic;
import org.jboss.resteasy.spi.NotImplementedYetException;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class IndexEntity extends Entity implements Serializable {
    public String[] columnsToIndex;
    public IndexEnum type;

    public Index convertToIndex(Table table) throws IndexException {
        this.validate();
        int[] columnNumbers = table.columnNumbers(columnsToIndex);
        switch (type) {
            case dichotomy:
                return new IndexMemDic(table, columnNumbers);
            case tree:
                if (columnNumbers.length > 1) {
                    throw new BadRequestException("tree indexes are currently only single columns indexes");
                }
                return new IndexTreeDic(table, columnNumbers[0]);
            case hash:
            default:
                throw new NotImplementedYetException("this index type is not implemented yet... :(");
        }
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void validate() {
        if (columnsToIndex == null) {
            throw new BadRequestException("columnsToIndex parameter cannot be null");
        }
        if (columnsToIndex.length == 0) {
            throw new BadRequestException("columnsToIndex parameter cannot be an empty array");
        }
        if (type == null) {
            throw new BadRequestException("type parameter cannot be null");
        }
    }
}
