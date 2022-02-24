import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

abstract class Task {
    // Data-members
    public WebDriver webDriver;
    public Task doFirst=null;
    List<String> data= new ArrayList<>();
    // Constructors
    public Task(WebDriver driver) {
          webDriver = driver;
    }
    public Task(WebDriver driver,Task doFirst) {
        webDriver = driver;
        this.doFirst = doFirst;
    }
    // Member-functions
    abstract void execute() throws  Exception;
    void run() throws Exception {
        if(doFirst!=null){
            doFirst.setWebDriver(webDriver);
            doFirst.run();
            data.addAll(doFirst.getData());
            doFirst.getData().clear();
        }
        execute();
    }
    public List<String> getData() {
        return data;
    }

    public void setWebDriver(WebDriver driver){
        this.webDriver = driver;
    }
}

class TextTask extends Task {
    private String xPathToElement;
    String text;
    public TextTask(WebDriver driver,String xPathToElement) {
        super(driver);
        this.xPathToElement = xPathToElement;
    }
    public TextTask(WebDriver driver, String xPathToElement, Task doFirst){
        super(driver,doFirst);
        this.xPathToElement = xPathToElement;
    }

    @Override
    void execute() throws  Exception{
        text = new WebDriverWait(webDriver,5)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement))).getText();
        data.add(text);
    }

}

class ClickTask extends Task{
    private String xPathToElement;
    public ClickTask(WebDriver driver, String xPathToElement) {
        super(driver);
        this.xPathToElement = xPathToElement;
    }
    public ClickTask(WebDriver driver, String xPathToElement, Task doFirst){
        super(driver, doFirst);
        this.xPathToElement = xPathToElement;
    }

    @Override
    void execute() throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        WebElement element = new WebDriverWait(webDriver,5)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement)));
        js.executeScript("arguments[0].click();",element);
    }
}

class BackTask extends Task{

    public BackTask(WebDriver driver) {
        super(driver);
    }
    public BackTask(WebDriver driver, Task doFirst){
        super(driver, doFirst);
    }

    @Override
    void execute() throws Exception {
        webDriver.navigate().back();
    }
}



class NavigateTask extends Task{
    String url;
    public NavigateTask(WebDriver driver, String url) {
        super(driver);
        this.url = url;
    }
    public NavigateTask(WebDriver driver, String url, Task doFirst){
        super(driver, doFirst);
        this.url = url;
    }

    @Override
    void execute() throws Exception {
        webDriver.navigate().to(url);
    }
}

class MultiLinkTask extends Task{
    Task repeatedTask;
    List<String> xPaths;
    public MultiLinkTask(WebDriver driver, Task repeatedTask, List<String> xPaths) {
        super(driver);
        this.repeatedTask = repeatedTask;
        this.xPaths = xPaths;
    }

    public MultiLinkTask(WebDriver driver, Task repeatedTask, List<String> xPaths, Task doFirst){
        super(driver, doFirst);
        this.repeatedTask = repeatedTask;
        this.xPaths = xPaths;
    }

    @Override
    void execute() throws Exception {
        for(String p : xPaths){
            new ClickTask(webDriver,p).run();
            repeatedTask.run();
        }
        data.addAll(repeatedTask.getData());
    }
}

