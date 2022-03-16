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
        String root = driver.getCurrentUrl();
        for(Task task: tasks){
            Instant before = Instant.now();
            try{
                task.run(driver);
            }catch(Exception ignored){}
            times.add(new AtomicReference<>(Duration.between(before, Instant.now())));
            driver.navigate().to(root);
        }
        driver.close();
        return null;
    }
}