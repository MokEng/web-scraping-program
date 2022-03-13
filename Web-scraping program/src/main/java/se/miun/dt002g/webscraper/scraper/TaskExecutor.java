package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.concurrent.Callable;
class TaskExecutor implements Callable<Void> {
    private final List<Task> tasks;
    private final WebDriver driver;
    public TaskExecutor(List<Task> tasks, WebDriver driver){
        this.tasks = tasks;
        this.driver = driver;
    }

    @Override
    public Void call() {
        for(Task task: tasks){
            try{
                task.run(driver);
            }catch(Exception ignored){}
        }
        driver.close();
        return null;
    }
}