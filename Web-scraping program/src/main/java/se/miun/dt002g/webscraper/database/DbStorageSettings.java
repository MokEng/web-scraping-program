package se.miun.dt002g.webscraper.database;

import java.io.Serializable;

public class DbStorageSettings implements Serializable {
    public String databaseName;
    public String collectionName;
    public boolean dropPreviousData;
}
