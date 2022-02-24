
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class Sitemap {
    private Queue<Pair<String,Task>> tasks = new LinkedList<>();
    private List<Pair<String, List<String>>> collectedData = new ArrayList<>();
    private String rootUrl;
    public Sitemap(String rootUrl){
        this.rootUrl = rootUrl;
    }

    public boolean addTask(String taskId, Task task){
        return tasks.add(new Pair<>(taskId,task));
    }
    public void runScraper() throws Exception {
        int fails = 0;
        int tot = tasks.size();
        while(!tasks.isEmpty()){
            Pair<String,Task> temp = tasks.poll();
            try{

                temp.second.run();
                collectedData.add(new Pair<>(temp.first,temp.second.getData()));

            }catch(Exception e){
                fails++;
                collectedData.add(new Pair<>(temp.first, null));
            }
        }
        System.out.println("Number of successful data extractions: "+(tot-fails)+"/"+(tot));
    }

    public void runMultiThreadedScraper(int nrOfDrivers) throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(nrOfDrivers);
        int tasksPerDriver = tasks.size() / nrOfDrivers;
        int leftOverTasks = tasks.size() % nrOfDrivers;
        Set<Future<List<Pair<String,List<String>>>>> set = new HashSet<>();
        List<WebDriver> drivers = new ArrayList<>();

        while (!tasks.isEmpty()) {
            List<Pair<String,Task>> partitionOfTasks = new ArrayList<>();
            WebDriver driver = new ChromeDriver();
            driver.manage().window().minimize();
            drivers.add(driver);
            driver.navigate().to(rootUrl);
            for(int x = 0; x < tasksPerDriver+leftOverTasks;x++){
                Pair<String,Task> temp = tasks.poll();
                temp.second.setWebDriver(driver);
                partitionOfTasks.add(temp);
            }
            leftOverTasks =0;
            Callable<List<Pair<String,List<String>>>> callable = new TaskThread(partitionOfTasks);
            Future<List<Pair<String,List<String>>>> future = pool.submit(callable);
            set.add(future);
        }

        for (Future<List<Pair<String,List<String>>>> future : set) {
            collectedData.addAll(future.get());
        }
        pool.shutdown();
        for(WebDriver d:drivers){
            d.close();
        }
        System.out.println("Data scraping is finished.");
    }

    public void printCollectedData(){
        for(Pair<String,List<String>> pair: collectedData){
            Stream.generate(() -> "-").limit(100).forEach(System.out::print);
            System.out.println("\nID: \n"+pair.first);
            System.out.println("DATA:");
            for(String item : pair.second){
                System.out.println(item);
            }
        }
    }

}
