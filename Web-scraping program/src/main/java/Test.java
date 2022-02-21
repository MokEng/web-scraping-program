import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {

        WebDriver driver;
        driver = new ChromeDriver();
        // go to root page
        Task nav = new NavigateTask(driver,"https://www.legaseriea.it/en/press/news");
        nav.run();

        WebElement wrapper = driver.findElement(By.xpath("/html/body/main/div[1]/section[1]"));
        List<WebElement> elems = wrapper.findElements(By.tagName("a"));
        // adds the paths to all links in the wrapper section of the page to a list
        List<String> linkXpaths = elems.stream().map(e-> getAbsoluteXPath(e,driver)).toList();
        // paths to elements in the linked page
        String articleTitle ="/html/body/main/div[1]/section[1]/article/section/header/h1";
        String articleContent = "/html/body/main/div[1]/section[1]/article/section/p[1]";


        // Get title and content from all articles on page.
        // One way to do it is with a MultiLinkTask

        // Decorator pattern.
        Task fetchArticle = new TextTask(driver,articleTitle);
        fetchArticle = new TextTask(driver,articleContent,fetchArticle);
        fetchArticle = new BackTask(driver,fetchArticle);

        Task repeatedTask = new MultiLinkTask(driver,fetchArticle,linkXpaths);
        try{
            repeatedTask.run(); // run scraper
        }catch(Exception e ){
            System.out.println("Error while performing tasks...");
        }
        int x = 0;
        for(Object text : repeatedTask.getData()){
            x++;
            System.out.println(text);
            if(x%2==0){
                System.out.println("\n");
            }
        }
        // Does same as above but without MultiLinkTask
        // Can be executed on multiple threads if you use
        // different drivers.
        List<Task> allTasks = new ArrayList<>();
        List<String> data = new ArrayList<>();
        for(String p : linkXpaths){
            Task t = new ClickTask(driver,p);
            t = new TextTask(driver,articleTitle,t);
            t = new TextTask(driver,articleContent,t);
            t = new BackTask(driver,t);
            allTasks.add(t);
        }

        for(Task task : allTasks){
            try{
                task.run(); // run scraper
                System.out.println(task.getData()); // present data
            }catch(Exception e ){
                System.out.println("Error while performing tasks...");
            }
        }




        driver.close();
    }

    /*
        Gets the absolut xPath to an element
     */
    public static String getAbsoluteXPath(WebElement element,WebDriver driver) {
        return (String) ((JavascriptExecutor) driver).executeScript(
                "function absoluteXPath(element) {"+
                        "var comp, comps = [];"+
                        "var parent = null;"+
                        "var xpath = '';"+
                        "var getPos = function(element) {"+
                        "var position = 1, curNode;"+
                        "if (element.nodeType == Node.ATTRIBUTE_NODE) {"+
                        "return null;"+
                        "}"+
                        "for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {"+
                        "if (curNode.nodeName == element.nodeName) {"+
                        "++position;"+
                        "}"+
                        "}"+
                        "return position;"+
                        "};"+

                        "if (element instanceof Document) {"+
                        "return '/';"+
                        "}"+

                        "for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {"+
                        "comp = comps[comps.length] = {};"+
                        "switch (element.nodeType) {"+
                        "case Node.TEXT_NODE:"+
                        "comp.name = 'text()';"+
                        "break;"+
                        "case Node.ATTRIBUTE_NODE:"+
                        "comp.name = '@' + element.nodeName;"+
                        "break;"+
                        "case Node.PROCESSING_INSTRUCTION_NODE:"+
                        "comp.name = 'processing-instruction()';"+
                        "break;"+
                        "case Node.COMMENT_NODE:"+
                        "comp.name = 'comment()';"+
                        "break;"+
                        "case Node.ELEMENT_NODE:"+
                        "comp.name = element.nodeName;"+
                        "break;"+
                        "}"+
                        "comp.position = getPos(element);"+
                        "}"+

                        "for (var i = comps.length - 1; i >= 0; i--) {"+
                        "comp = comps[i];"+
                        "xpath += '/' + comp.name.toLowerCase();"+
                        "if (comp.position !== null) {"+
                        "xpath += '[' + comp.position + ']';"+
                        "}"+
                        "}"+

                        "return xpath;"+

                        "} return absoluteXPath(arguments[0]);", element);
    }
}
