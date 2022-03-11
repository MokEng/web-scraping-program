package se.miun.dt002g.webscraper.gui;

import se.miun.dt002g.webscraper.database.DbStorageSettings;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.GROUPBY;

public class ScrapeSettings {
    boolean saveLocal;
    boolean saveDb;
    DATA_FORMAT dataFormat;
    GROUPBY groupby;
    DbStorageSettings dbStorageSettings;
    int NO_DRIVERS;
    String localStorageLocation;

    public ScrapeSettings(boolean saveLocal, boolean saveDb, DATA_FORMAT dataFormat, GROUPBY groupby, DbStorageSettings dbStorageSettings, int NO_DRIVERS, String localStorageLocation) {
        this.saveLocal = saveLocal;
        this.saveDb = saveDb;
        this.dataFormat = dataFormat;
        this.groupby = groupby;
        this.NO_DRIVERS = NO_DRIVERS;
        this.localStorageLocation = localStorageLocation;
        this.dbStorageSettings = dbStorageSettings;
    }
    public ScrapeSettings(){};

}
