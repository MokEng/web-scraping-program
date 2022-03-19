package se.miun.dt002g.webscraper.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import se.miun.dt002g.webscraper.database.MongoDbHandler;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.DataHandler;
import se.miun.dt002g.webscraper.scraper.Sitemap;

import java.util.concurrent.ExecutionException;

/**
 * A class that will be scheduled to run a sitemap in the future
 */
public class TimerService extends ScheduledService<Integer> {
    Sitemap sitemap;
    ScrapeSettings settings;
    MongoDbHandler mongoDbHandler;
    Runnable update;

    TimerService(Sitemap sitemap, ScrapeSettings settings, MongoDbHandler mongoDbHandler, Runnable update) {
        this.sitemap = sitemap;
        this.settings = settings;
        this.mongoDbHandler = mongoDbHandler;
        this.update = update;
    }

    private IntegerProperty count = new SimpleIntegerProperty();

    public final void setCount(Integer value) {
        count.set(value);
    }

    public final Integer getCount() {
        return count.get();
    }

    public final IntegerProperty countProperty() {
        return count;
    }

    /**
     * Runs the scraper of the sitemap and then stores the data
     * with the specified settings in 'this.settings'.
     *
     * @return an integer telling the caller how many times the createTask() function has been run.
     */
    protected Task<Integer> createTask() {
        return new Task<>() {
            protected Integer call() throws ExecutionException, InterruptedException {
                sitemap.clearData(); // clear data from previous scrapes
                sitemap.runMultiThreadedScraper(settings.NO_DRIVERS, update, settings.webDriverName); // run web scraper
                if (settings.saveLocal) { // save the data locally
                    if (settings.dataFormat == DATA_FORMAT.json) {
                        DataHandler.toJSONFile(settings.groupby, sitemap, settings.localStorageLocation + "/" + sitemap.getName() + ".json");
                    } else if (settings.dataFormat == DATA_FORMAT.csv) {
                        DataHandler.toCSVFile(settings.groupby, sitemap, settings.localStorageLocation + "/" + sitemap.getName() + ".csv");
                    }
                }
                if (settings.saveDb) { // save to database
                    mongoDbHandler.storeJsonInDb(settings.dbStorageSettings, DataHandler.toJSONList(settings.groupby, sitemap));
                }
                //Adds 1 to the count
                count.set(getCount() + 1);
                return getCount();
            }
        };
    }

    public String toString() {
        return new String("Sitemap:" + sitemap.getName() +
                "\nExecute at:" + settings.startAt.toString());
    }
}
