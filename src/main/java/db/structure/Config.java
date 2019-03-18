package db.structure;

import java.util.HashMap;

public class Config {
    public String tableNamePattern;
    public String columnNamePattern;
    public String NTPHostName;
    public HashMap<String, String> DataTypes;

    public Config (String NTPHostName, HashMap<String, String> DataTypes, String tableNamePattern, String columnNamePattern) {
        this.NTPHostName = NTPHostName;
        this.DataTypes = DataTypes;
        this.tableNamePattern = tableNamePattern;
        this.columnNamePattern = columnNamePattern;
    }
}
