
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
class TaskThread implements Callable<List<Pair<String,List<String>>>> {
    private final List<Pair<String,Task>> tasks;
    public TaskThread(List<Pair<String,Task>> tasks){
        this.tasks = tasks;
    }

    @Override
    public List<Pair<String,List<String>>> call() throws Exception {
        List<Pair<String,List<String>>> collectedData= new ArrayList<>();
        for(Pair<String,Task> task: tasks){
            task.second.run();
            collectedData.add(new Pair<>(task.first,task.second.getData()));
        }
        return collectedData;
    }
}
