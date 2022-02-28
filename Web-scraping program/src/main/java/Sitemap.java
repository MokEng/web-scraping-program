import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Sitemap {
    private final List<Pair<String,Task>> tasks = new ArrayList<>();
    private final String rootUrl; // all drivers run from Sitemap starts scraping from rootUrl;
    public Sitemap(String rootUrl){
        this.rootUrl = rootUrl;
    }

    public boolean addTask(String groupId, Task task){
        return tasks.add(new Pair<>(groupId,task));
    }
    public void runScraper(){
        WebDriver driver = new ChromeDriver();
        driver.manage().window().minimize();
        driver.navigate().to(rootUrl);
        tasks.forEach(p-> p.second.setWebDriver(driver));//set driver for all tasks
        tasks.forEach(p-> { // run all tasks in list
            p.second.run();
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
            List<Pair<String, Task>> partitionOfTasks = new ArrayList<>(tasks.subList(i * tasksPerDriver + temp, (i + 1) * tasksPerDriver + leftOverTasks));
            temp = leftOverTasks;
            // driver to run partitionOfTasks
            WebDriver driver = new ChromeDriver();
            driver.manage().window().minimize();
            driver.navigate().to(rootUrl);
            partitionOfTasks.forEach(p-> p.second.setWebDriver(driver));
            Callable<Void> callable = new TaskThread(partitionOfTasks); // new callable for executing tasks
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
        tasks.forEach(p->p.second.clearDataFromAllTasks());
    }
    public List<Pair<String, Task>> getTasks(){
        return tasks;
    }

}
