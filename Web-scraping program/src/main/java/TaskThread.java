
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.concurrent.Callable;
class TaskThread implements Callable<Void> {
    private final List<Pair<String,Task>> tasks;
    private WebDriver driver;
    public TaskThread(List<Pair<String,Task>> tasks,WebDriver driver){
        this.tasks = tasks;
        this.driver = driver;
    }

    @Override
    public Void call() {
        for(Pair<String,Task> task: tasks){
            task.second.run(driver);
        }
        driver.close();
        return null;
    }
}