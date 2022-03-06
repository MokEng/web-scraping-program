import java.util.List;

public class Data {
    String groupedBy;
    List<TaskData> data;

    public Data(String groupedBy, List<TaskData> taskDataList) {
        this.groupedBy = groupedBy;
        this.data = taskDataList;
    }
}
