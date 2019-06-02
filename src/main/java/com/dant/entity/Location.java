package com.dant.entity;

@Deprecated
public enum Location {
    createTable("createTable"),
    updateTable("updateTable"),
    deleteTable("deleteTable"),
    getTable("getTable"),
    addIndex("addIndex"),
    deleteIndex("deleteIndex"),
    loadCSV("loadCSV"),
    search("search");

    private final String fieldDescription;

    Location(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
}
