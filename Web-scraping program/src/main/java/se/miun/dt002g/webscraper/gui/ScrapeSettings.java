package se.miun.dt002g.webscraper.gui;

import se.miun.dt002g.webscraper.database.DbStorageSettings;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.GROUPBY;

import java.time.Duration;

public class ScrapeSettings {
    boolean saveLocal;
    boolean saveDb;
    DATA_FORMAT dataFormat;
    GROUPBY groupby;
    DbStorageSettings dbStorageSettings;
    int NO_DRIVERS;
    String localStorageLocation;
    javafx.util.Duration interval,firstStart;
    int repetitions;

    public ScrapeSettings(){};

}
