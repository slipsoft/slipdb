package db.structure;

import java.util.HashMap;

public class Config {
    public String tableNamePattern;
    public String columnNamePattern;
    public String DataTypesClassPathPrefix;
    public HashMap<String, String> DataTypes;
    public String NTPHostName;
    public String SuperSecretPassphrase;

    public Config (String NTPHostName, HashMap<String, String> DataTypes, String tableNamePattern, String columnNamePattern,
                   String DataTypesClassPathPrefix, String SuperSecretPassphrase) {
        this.NTPHostName = NTPHostName;
        this.DataTypes = DataTypes;
        this.tableNamePattern = tableNamePattern;
        this.columnNamePattern = columnNamePattern;
        this.DataTypesClassPathPrefix = DataTypesClassPathPrefix;
        this.SuperSecretPassphrase = SuperSecretPassphrase;
    }
}
