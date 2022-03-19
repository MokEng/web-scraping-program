package se.miun.dt002g.webscraper.scraper;

import java.util.List;

/**
 * Class that hold data for several TextData objects along with an identifier.
 * Is converted into JSON format with Gson.
 */
public class Data {
    String groupedBy;
    List<TaskData> taskData;

    public Data(String groupedBy, List<TaskData> taskData) {
        this.groupedBy = groupedBy;
        this.taskData = taskData;
    }
}
