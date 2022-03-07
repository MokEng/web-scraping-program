import java.util.List;

/**
 * Class that hold data for several TextData objects along with an identifier.
 */
public class Data {
    String groupedBy;
    List<TaskData> taskData;

    public Data(String groupedBy, List<TaskData> taskData) {
        this.groupedBy = groupedBy;
        this.taskData = taskData;
    }
}
