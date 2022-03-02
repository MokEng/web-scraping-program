import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.util.*;

/**
 * Abstract class for representing a web-scraping Task.
 * Tasks can be combined in a decorator design-pattern.
 */
abstract class Task implements Serializable {
    // Data-members
    private Task doFirst=null;
    protected String id;
    protected Exception exception;
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
    void run(WebDriver driver) {
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

/**
 * Task for extracting data in form of text from the web.
 */
class TextTask extends Task {
    private final String xPathToElement; // path to the element which inner HTML should be scraped.
    String data; // the extracted data
    String dataName; // key to find the data;
    public TextTask(String xPathToElement,String id,String dataName) {
        super(id);
        this.xPathToElement = xPathToElement;
        this.dataName=dataName;
    }
    public TextTask(String xPathToElement, Task doFirst,String id,String dataName){
        super(doFirst,id);
        this.xPathToElement = xPathToElement;
        this.dataName = dataName;
    }

    @Override
    void execute(WebDriver driver) throws  Exception{
        // get inner html of the element
        data = new WebDriverWait(driver,5)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement))).getText();

    }


}

/**
 * Task for clicking an element in a web-page.
 */
class ClickTask extends Task{
    private String xPathToElement; // path to the element which should be clicked
    public ClickTask(String xPathToElement,String id) {
        super(id);
        this.xPathToElement = xPathToElement;
    }
    public ClickTask(String xPathToElement, Task doFirst,String id){
        super(doFirst,id);
        this.xPathToElement = xPathToElement;
    }

    @Override
    void execute(WebDriver driver) throws Exception {
        // Use javascript to click an element on the current webpage
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement element = new WebDriverWait(driver,5)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement)));
        js.executeScript("arguments[0].click();",element);
    }
}

/**
 * Task for ordering a web-window to go back to last visited page.
 */
class BackTask extends Task{

    public BackTask(String id) {
        super(id);
    }
    public BackTask(Task doFirst,String id){
        super(doFirst,id);
    }

    @Override
    void execute(WebDriver driver) throws Exception {
        driver.navigate().back();
    }
}


/**
 * Task for navigating a webdriver to a new url
 */
class NavigateTask extends Task{
    String url;
    public NavigateTask(String url,String id) {
        super(id);
        this.url = url;
    }
    public NavigateTask(String url, Task doFirst,String id){
        super(doFirst,id);
        this.url = url;
    }

    @Override
    void execute(WebDriver driver) throws Exception {
        driver.navigate().to(url);
    }
}