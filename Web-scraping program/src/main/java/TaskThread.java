
import java.util.List;
import java.util.concurrent.Callable;
class TaskThread implements Callable<Void> {
    private final List<Pair<String,Task>> tasks;
    public TaskThread(List<Pair<String,Task>> tasks){
        this.tasks = tasks;
    }

    @Override
    public Void call() throws Exception {
        for(Pair<String,Task> task: tasks){
            task.second.run();
        }
        tasks.get(0).second.webDriver.close();
        return null;
    }
}