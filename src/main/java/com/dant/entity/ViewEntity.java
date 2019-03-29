package com.dant.entity;

import com.dant.app.Controller;
import com.dant.utils.Log;
import com.google.gson.Gson;
import db.search.*;
import db.structure.Database;
import db.structure.Table;
import db.structure.recherches.TableHandler;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViewEntity {
    public String tableName;
    public FilterTermEntity filterTerm;
    public List<Field> fieldList;
    public List<Sort> sortList;
    public Group groupBy;

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void validate(ArrayList<ResponseError> allErrors) {
        if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.tableName)) {
            allErrors.add(new ResponseError(Location.search, Type.invalidData, "table name is invalid"));
            return;
        }

        Optional<Table> tableOptional = Controller.getTableByName(this.tableName);
        if (!tableOptional.isPresent()) {
            allErrors.add(new ResponseError(Location.search, Type.invalidData, "no table with this name"));
            return;
        }
        Table table = tableOptional.get();

        if (filterTerm != null) {
            filterTerm.validate(table, allErrors);
        } else {
            allErrors.add(new ResponseError(Location.search, Type.missingData, "filterTerm is missing"));

        }

        if (fieldList != null) {
            fieldList.stream().forEach(f -> f.validate(table, allErrors));
        } else {
            allErrors.add(new ResponseError(Location.search, Type.missingData, "fieldList is missing"));
        }
        if (sortList != null) {
            sortList.stream().forEach(s-> s.validate(table, allErrors));
        } else {
            allErrors.add(new ResponseError(Location.search, Type.missingData, "sort list is missing"));
        }

        if (groupBy != null) {
            groupBy.validate(table, allErrors);
        } else {
            allErrors.add(new ResponseError(Location.search, Type.missingData, "groupBy is missing"));
        }

    }

    public View convertToView() {
        Table table = Controller.getTableByName(tableName).get();
        TableHandler handler = table.getTableHandler();
        FilterTerm concreteFilterTerm = filterTerm.convertToFilterTerm(table);

        return new View(handler, concreteFilterTerm, this.fieldList, this.sortList, this.groupBy);
    }
}
