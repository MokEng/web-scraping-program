package se.miun.dt002g.webscraper.database;

import java.io.Serializable;

/**
 * Holds variables used for MongoDb storage
 */
public class DbStorageSettings implements Serializable {
    public String databaseName;
    public String collectionName;
    public boolean dropPreviousData;
}
