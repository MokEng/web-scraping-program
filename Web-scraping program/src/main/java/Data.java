import java.util.List;

public class Data {
    String groupedBy;
    List<TaskData> taskData;

    public Data(String groupedBy, List<TaskData> taskData) {
        this.groupedBy = groupedBy;
        this.taskData = taskData;
    }
}
