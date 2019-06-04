package db.structure;

import java.util.HashMap;

public class Config {
    public String tableNamePattern;
    public String columnNamePattern;
    public String NTPHostName;
    public String SuperSecretPassphrase;

    public Config (String NTPHostName, String tableNamePattern, String columnNamePattern, String SuperSecretPassphrase) {
        this.NTPHostName = NTPHostName;
        this.tableNamePattern = tableNamePattern;
        this.columnNamePattern = columnNamePattern;
        this.SuperSecretPassphrase = SuperSecretPassphrase;
    }
}
