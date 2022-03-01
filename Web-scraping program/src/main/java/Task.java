import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class Task implements Serializable {
    // Data-members
    public Task doFirst=null;
    List<String> data= new ArrayList<>();
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
    abstract void execute(WebDriver driver) throws  Exception;
    void run(WebDriver driver) {
        if(doFirst!=null){
            doFirst.run(driver);
        }
        try{
            execute(driver);
        }catch(Exception e){
            exception =e;
        }
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

    public List<Pair<String, Exception>> getFailedTasks(){
        Task temp = this;
        List<Pair<String,Exception>> pairList=new ArrayList<>();
        while (temp != null) {
            if(temp.exception !=null){
                pairList.add(Pair.of(temp.id,temp.exception));
            }
            temp = temp.doFirst;
        }
        return pairList;
    }
}

class TextTask extends Task {
    private String xPathToElement;
    String text;
    public TextTask(String xPathToElement,String id) {
        super(id);
        this.xPathToElement = xPathToElement;
    }
    public TextTask(String xPathToElement, Task doFirst,String id){
        super(doFirst,id);
        this.xPathToElement = xPathToElement;
    }

    @Override
    void execute(WebDriver driver) throws  Exception{
        text = new WebDriverWait(driver,5)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement))).getText();
        data.add(text);
    }


}

class ClickTask extends Task{
    private String xPathToElement;
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
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement element = new WebDriverWait(driver,5)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement)));
        js.executeScript("arguments[0].click();",element);
    }
}

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