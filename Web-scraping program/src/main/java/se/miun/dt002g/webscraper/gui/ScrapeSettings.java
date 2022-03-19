package se.miun.dt002g.webscraper.gui;

import se.miun.dt002g.webscraper.database.DbStorageSettings;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.GROUPBY;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Settings for the web scraper.
 */
public class ScrapeSettings implements Serializable
{
    boolean saveLocal; // If the data should be saved locally.
    boolean saveDb; // If the data should be saved in a database.
    DATA_FORMAT dataFormat; // The data format.
    GROUPBY groupby; // How the data should be grouped.
    DbStorageSettings dbStorageSettings; // The database storage settings.
    int NO_DRIVERS; // How many drivers are available.
    String localStorageLocation; // The storage location on the computer, should the data be saved locally.
    java.time.Duration interval, // How long to wait between each scraping.
            firstStart; // When the first scraping should start.
    int repetitions; // How many times to repeat the scraping.
    LocalDateTime startAt; // When the scraping should start.
    String webDriverName; // The name of the web driver.
    public ScrapeSettings(){};
}
