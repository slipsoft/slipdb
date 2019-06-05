package com.dant.entity;

import com.dant.app.Controller;
import com.google.gson.Gson;
import db.search.*;
import db.structure.Column;
import db.structure.Database;
import db.structure.Table;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViewEntity {
    public String tableName;
    public FilterTermEntity filterTerm;
    public List<Field> fieldList;
    public List<SortEntity> sortList;
    public GroupEntity groupBy;

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public ViewEntity setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public void validate() {
        if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.tableName)) {
            throw new BadRequestException("table name is invalid");
        }

        Table table = Controller.getTableByName(tableName);

        if (filterTerm == null) {
            throw new BadRequestException("filterTerm is missing");
        }

        if (fieldList != null) {
            fieldList.stream().forEach(f -> f.validate(table));
        } else {
            throw new BadRequestException("fieldList is missing");
        }
        if (sortList != null) {
            sortList.stream().forEach(s-> s.validate(table));
        }

    }

    public View convertToView() {
        this.validate();
        Table table = Controller.getTableByName(tableName);
        FilterTerm concreteFilterTerm = filterTerm.convertToFilterTerm(table);
        Group group = null;
        if (groupBy != null) {
            group = this.groupBy.convertToGroup(table);
        }
        List<Sort> sorts = new ArrayList<>();
        for (SortEntity sort : this.sortList) {
            sorts.add(sort.convertToSort(table));
        }
        return new View(table, concreteFilterTerm, this.fieldList, sorts, group);
    }
}
