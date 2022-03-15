package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

class TaskExecutor implements Callable<Void> {
    private final List<Task> tasks;
    private final WebDriver driver;
    private final List<AtomicReference<Duration>> times;
    public TaskExecutor(List<Task> tasks, WebDriver driver, List<AtomicReference<Duration>> instants){
        this.tasks = tasks;
        this.driver = driver;
        this.times = instants;
    }

    @Override
    public Void call() {
        for(Task task: tasks){
            try{
                Instant before = Instant.now();
                task.run(driver);
                times.add(new AtomicReference<>(Duration.between(before, Instant.now())));
            }catch(Exception ignored){}
        }
        driver.close();
        return null;
    }
}