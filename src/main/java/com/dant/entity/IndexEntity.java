package com.dant.entity;

import com.google.gson.Gson;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;
import index.indexTree.IndexTreeDic;
import index.memDic.IndexMemDic;
import org.jboss.resteasy.spi.NotImplementedYetException;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

public class IndexEntity extends Entity implements Serializable {
    public String[] columnsToIndex;
    public IndexEnum type;

    public Index convertToIndex(Table table) throws IndexException {
        this.validate();
        int[] columnNumbers = table.columnNumbers(columnsToIndex);
        switch (type) {
            case dichotomy:
                IndexMemDic index = new IndexMemDic(table, columnNumbers);
                index.refreshIndexWithColumnsData(false);
                return index;
            case tree:
            case hash:
            default:
                throw new BadRequestException("this index type is not implemented yet :( : " + type);
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
