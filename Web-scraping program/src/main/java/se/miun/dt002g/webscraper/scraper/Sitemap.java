package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Sitemap implements Serializable {
    private List<Task> tasks = new ArrayList<>();
    private final String rootUrl; // all drivers run from Sitemap starts scraping from rootUrl;
    private String name;


    public Sitemap(String rootUrl,String name){
        this.rootUrl = rootUrl;
        this.name = name;
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
        tasks.forEach(p-> { // run all tasks in list
            p.run(driver);
        });
        driver.close();
        System.out.println("Data scraping is finished.");
    }

    public void runMultiThreadedScraper(int nrOfDrivers) throws ExecutionException, InterruptedException {
        if(nrOfDrivers > tasks.size()){
            nrOfDrivers = tasks.size();
        }
        ExecutorService pool = Executors.newFixedThreadPool(nrOfDrivers); // create thread-pool
        int tasksPerDriver = tasks.size() / nrOfDrivers;
        int leftOverTasks = tasks.size() % nrOfDrivers;
        Set<Future<Void>> set = new HashSet<>();
        int temp = 0;
        for(int i=0; i < nrOfDrivers;i++){
            // A part of all tasks to be run by a driver
            List<Task> partitionOfTasks = new ArrayList<>(tasks.subList(i * tasksPerDriver + temp, (i + 1) * tasksPerDriver + leftOverTasks));
            temp = leftOverTasks;
            // driver to run partitionOfTasks
            WebDriver driver = new ChromeDriver();
            driver.manage().window().minimize();
            driver.navigate().to(rootUrl);
            Callable<Void> callable = new TaskThread(partitionOfTasks,driver); // new callable for executing tasks
            Future<Void> future = pool.submit(callable); // submit callable to pool for execution
            set.add(future);
        }
        for (Future<Void> future : set) {
            future.get(); // wait for all threads to finish
        }
        pool.shutdown(); // destroy thread-pool
        System.out.println("Data scraping is finished.");
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
