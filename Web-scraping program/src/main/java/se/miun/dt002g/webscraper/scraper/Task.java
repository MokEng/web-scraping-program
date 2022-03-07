package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.WebDriver;

import java.io.Serializable;

/**
 * Abstract class for representing a web-scraping Task.
 * Tasks can be combined in a decorator design-pattern.
 */
public abstract class Task implements Serializable {
    // Data-members
    public Task doFirst=null;
    public String id;
    public Exception exception;
    // Constructors
    public Task(String id) {
          this.id = id;
    }
    public Task(Task doFirst,String id) {
        this.doFirst = doFirst;
        this.id = id;
    }
    // Member-functions
    abstract void execute(WebDriver driver) throws  Exception; // what should this specific task do

    /**
     * Runs all Tasks in the chain
     * then execute the job of this Task.
     * @param driver used to crawl the web
     */
    public void run(WebDriver driver) {
        if(doFirst!=null){
            doFirst.run(driver);
        }
        try{
            execute(driver);
        }catch(Exception e){
            exception = e; // save exception
        }
    }
    public Task getDoFirst(){
        return doFirst;
    }
}