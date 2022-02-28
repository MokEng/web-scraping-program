import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class Task {
    // Data-members
    public WebDriver webDriver;
    public Task doFirst=null;
    List<String> data= new ArrayList<>();
    public String id;
    // Constructors
    public Task(WebDriver driver,String id) {
          webDriver = driver;
          this.id = id;
    }
    public Task(WebDriver driver,Task doFirst,String id) {
        webDriver = driver;
        this.doFirst = doFirst;
        this.id = id;
    }
    // Member-functions
    abstract void execute() throws  Exception;
    void run() throws Exception {
        if(doFirst!=null){
            doFirst.setWebDriver(webDriver);
            doFirst.run();
        }
        execute();
    }
    public List<String> getData() {
        return data;
    }
    public List<String> getDataFromAllTasks(){
        List<String> allData= new ArrayList<>();
        Task temp = this;
        while (temp != null) {
            allData.addAll(temp.getData());
            temp = temp.doFirst;
        }
        return allData;
    }
    public void clearDataFromAllTasks(){
        Task temp = this;
        while (temp != null) {
            temp.getData().clear();
            temp = temp.doFirst;
        }
    }

    public List<String> getAllDataWithId(String id){
        Task temp = this;
        List<String> allData= new ArrayList<>();
        while (temp != null) {
            if (Objects.equals(temp.id, id)) {
                allData.addAll(temp.getData());
            }
            temp = temp.doFirst;
        }
        return allData;
    }

    public void setWebDriver(WebDriver driver){
        this.webDriver = driver;
    }
}

class TextTask extends Task {
    private String xPathToElement;
    String text;
    public TextTask(WebDriver driver,String xPathToElement,String id) {
        super(driver,id);
        this.xPathToElement = xPathToElement;
    }
    public TextTask(WebDriver driver, String xPathToElement, Task doFirst,String id){
        super(driver,doFirst,id);
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
    public ClickTask(WebDriver driver, String xPathToElement,String id) {
        super(driver,id);
        this.xPathToElement = xPathToElement;
    }
    public ClickTask(WebDriver driver, String xPathToElement, Task doFirst,String id){
        super(driver, doFirst,id);
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

    public BackTask(WebDriver driver,String id) {
        super(driver,id);
    }
    public BackTask(WebDriver driver, Task doFirst,String id){
        super(driver, doFirst,id);
    }

    @Override
    void execute() throws Exception {
        webDriver.navigate().back();
    }
}



class NavigateTask extends Task{
    String url;
    public NavigateTask(WebDriver driver, String url,String id) {
        super(driver,id);
        this.url = url;
    }
    public NavigateTask(WebDriver driver, String url, Task doFirst,String id){
        super(driver, doFirst,id);
        this.url = url;
    }

    @Override
    void execute() throws Exception {
        webDriver.navigate().to(url);
    }
}