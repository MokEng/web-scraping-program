package se.miun.dt002g.webscraper.gui;

import se.miun.dt002g.webscraper.database.DbStorageSettings;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.GROUPBY;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public class ScrapeSettings implements Serializable {
    boolean saveLocal;
    boolean saveDb;
    DATA_FORMAT dataFormat;
    GROUPBY groupby;
    DbStorageSettings dbStorageSettings;
    int NO_DRIVERS;
    String localStorageLocation;
    java.time.Duration interval,firstStart;
    int repetitions;
    LocalDateTime startAt;
    String webDriverName;
    public ScrapeSettings(){};

}
