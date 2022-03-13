package se.miun.dt002g.webscraper.scraper;

import javafx.application.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Sitemap implements Serializable {
    private List<Task> tasks = new ArrayList<>();
    private final String rootUrl; // all drivers run from Sitemap starts scraping from rootUrl;
    private String name;


    public void setRunning(boolean running) {
        this.running = running;
    }

    private boolean running=false;

    public Sitemap(String rootUrl,String name){
        this.rootUrl = rootUrl;
        this.name = name;
    }
    public String toString(){
        return name;
    }

    public boolean isRunning(){
        return running;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean addTask(Task task){
        return tasks.add(task);
    }
    public void runScraper(){
        WebDriver driver = new ChromeDriver();
        driver.manage().window().minimize();
        driver.navigate().to(rootUrl);
        running = true;
        tasks.forEach(p-> { // run all tasks in list
            try {
                p.run(driver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        driver.close();
        running =false;
    }

    public void runMultiThreadedScraper(int nrOfDrivers, Runnable runOnTaskFinish) throws ExecutionException, InterruptedException {
        if(tasks.isEmpty()){
            return;
        }
        running = true;
        if(nrOfDrivers > tasks.size()){
            nrOfDrivers = tasks.size();
        }
        ExecutorService pool = Executors.newFixedThreadPool(nrOfDrivers); // create thread-pool
        int tasksPerDriver = tasks.size() / nrOfDrivers;
        int leftOverTasks = tasks.size() % nrOfDrivers;
        CompletionService<Void> completionService = new ExecutorCompletionService<>(pool);
        int temp = 0;
        for(int i=0; i < nrOfDrivers;i++){
            // A part of all tasks to be run by a driver
            List<Task> partitionOfTasks = new ArrayList<>(tasks.subList(i * tasksPerDriver + temp, (i + 1) * tasksPerDriver + leftOverTasks));
            temp = leftOverTasks;
            // driver to run partitionOfTasks
            WebDriver driver = new ChromeDriver();
            driver.manage().window().minimize();
            driver.navigate().to(rootUrl);
            Callable<Void> callable = new TaskExecutor(partitionOfTasks,driver); // new callable for executing tasks
            completionService.submit(callable);
        }

        int driversLeft = nrOfDrivers;
        while (driversLeft > 0)
        {
            completionService.take();
            if (runOnTaskFinish != null) Platform.runLater(runOnTaskFinish);
            driversLeft--;
        }
        pool.shutdown(); // destroy thread-pool
        running = false;
    }

    public void clearDataFromTasks(){
        tasks.forEach(p-> {
            Task temp = p;
            while (temp != null) {
                if(temp instanceof TextTask){
                    ((TextTask) temp).data=null;
                }
                temp = temp.getDoFirst();
            }
        });
    }
    public List<Task> getTasks(){
        return tasks;
    }

    public void setTasks(List<Task> tasks)
    {
        this.tasks = tasks;
    }

}
