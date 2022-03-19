package se.miun.dt002g.webscraper.scraper;

import javafx.application.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages and executes a List of Task-objects.
 *
 */
public class Sitemap implements Serializable {
    private List<Task> tasks = new ArrayList<>();
    private final String rootUrl; // all drivers run from Sitemap starts scraping from rootUrl;
    private String name; // the sitemap name
    // Variables for gathering statistics about a completed scrape.
    private final List<List<AtomicReference<Duration>>> times;
    private int totalBytes = 0;
    private final List<Integer> bytesPerTask;

    public void setRunning(boolean running) {
        this.running = running;
    }

    private boolean running=false;

    public Sitemap(String rootUrl,String name){
        this.rootUrl = rootUrl;
        this.name = name;
        this.times = new ArrayList<>();
        this.bytesPerTask = new ArrayList<>();
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

    /**
     * Run every task in the tasks-list.
     * @param nrOfDrivers, NO drivers to use
     * @param runOnTaskFinish, Runnable to be executed when scraping has completed
     * @param webDriverName, what webdriver should be used for scraping the data
     * @throws InterruptedException
     */
    public void runMultiThreadedScraper(int nrOfDrivers, Runnable runOnTaskFinish,String webDriverName) throws InterruptedException {
        if(tasks.isEmpty()){
            return;
        }
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
            WebDriver driver = getWebdriverOfChoice(webDriverName);
            driver.manage().window().minimize();
            driver.navigate().to(rootUrl);
            List<AtomicReference<Duration>> time = new ArrayList<>();
            Callable<Void> callable = new TaskExecutor(partitionOfTasks,driver, time); // new callable for executing tasks
            times.add(time);
            completionService.submit(callable); // submit to executor
        }

        int driversLeft = nrOfDrivers;
        while (driversLeft > 0)
        {
            completionService.take(); // take next finished callable
            if (runOnTaskFinish != null) Platform.runLater(runOnTaskFinish);
            driversLeft--;
        }
        pool.shutdown(); // destroy thread-pool


        tasks.forEach(p-> { // calculate size of fetched data and save to instance variables
            Task t = p;
            int bytesPerChain = 0;
            while (t != null) {
                if(t instanceof TextTask){
                    if(((TextTask)t).data == null){
                        bytesPerChain = -1;
                        break;
                    }
                    int b = ((TextTask) t).data.getBytes().length;
                    bytesPerChain+=b;
                    totalBytes+=b;
                }
                t = t.getDoFirst();
            }
            bytesPerTask.add(bytesPerChain);
        });
    }

    /**
     * Clear all data from all Task-objects in tasks-list
     */
    public void clearData(){
        totalBytes = 0;
        bytesPerTask.clear();
        times.clear();
        tasks.forEach(p-> {
            Task temp = p;
            while (temp != null) {
                if(temp instanceof TextTask){
                    ((TextTask) temp).data=null;
                }
                if(temp.exception !=null){
                    temp.exception = null;
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

    /**
     * Fetch all times from the latest execution
     * @return list of Duration-objects
     */
    public List<AtomicReference<Duration>> getTimes()
    {
        List<AtomicReference<Duration>> temp = new ArrayList<>();

        for (List<AtomicReference<Duration>> l : times)
        {
            temp.addAll(l);
        }

        return temp;
    }

    public int getTotalBytes()
    {
        return totalBytes;
    }

    public List<Integer> getBytesPerTask()
    {
        return bytesPerTask;
    }

    /**
     * Return WebDriver based on parameter
     * @param webDriverName, name of WebDriver
     * @return the WebDriver mapped to the input string
     */
    private WebDriver getWebdriverOfChoice(String webDriverName){

        WebDriver driver;
        if(webDriverName == null){
            driver = new ChromeDriver();
            return driver;
        }
        switch(webDriverName){
            case "chrome": driver = new ChromeDriver(); break;
            case "firefox":driver = new FirefoxDriver();break;
            case "edge": driver = new EdgeDriver();break;
            case "ie":driver = new InternetExplorerDriver();break;
            case "safari":driver = new SafariDriver();break;
            default: driver = new ChromeDriver();
        }
        return driver;
    }
}
