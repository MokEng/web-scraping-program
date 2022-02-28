
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) throws Exception {
        //----------- Finding the paths ------------------------------------------------
        WebDriver driver;
        driver = new ChromeDriver();
        // go to root page
        Task nav = new NavigateTask(driver,"https://www.legaseriea.it/en/press/news","nav");
        nav.run();

        WebElement wrapper = driver.findElement(By.xpath("/html/body/main/div[1]/section[1]"));
        List<WebElement> elems = wrapper.findElements(By.tagName("a"));
        // adds the paths to all links in the wrapper section of the page to a list
        List<String> linkXpaths = elems.stream().map(e-> getAbsoluteXPath(e,driver)).toList();
        // paths to elements in the linked page
        String articleTitle ="/html/body/main/div[1]/section[1]/article/section/header/h1";
        String articleContent = "/html/body/main/div[1]/section[1]/article/section/p[1]";
        String urlP = "https://www.legaseriea.it/en/press/news?page=";
        // -----------------------------------------------------------------------------------

        List<Task> pages = new ArrayList<>();
        final int nrOfPages = 5;
        for(int x = 1; x < nrOfPages+1;x++){
            if(x==1) { // this will create an exception
                pages.add(new NavigateTask(driver, "not a valid url", "pager"));
                continue;
            }
            pages.add(new NavigateTask(driver,urlP+x,"pager"));


        }
        for(String p : linkXpaths){
            pages = pages.stream().map(t -> new ClickTask(driver,p,t,"click"))
                    .map(t -> new TextTask(driver,articleTitle,t,p))
                    .map(t -> new TextTask(driver,articleContent,t,p))
                    .map(t -> new BackTask(driver,t,"return")).collect(Collectors.toList());
        }
        driver.close();
        String rootUrl = "https://www.legaseriea.it/en/press/news";
        Sitemap sitemap = new Sitemap(rootUrl);
        int x = 0;
        for(Task page : pages){
            x++;
            sitemap.addTask("News page #"+x,page); // add task + id for each
        }
        Instant starts = Instant.now();
        sitemap.runMultiThreadedScraper(2); // run scraper(s)
        Instant ends = Instant.now();
        System.out.println("TIME: "+Duration.between(starts, ends).getSeconds()+" SECONDS");
        // ----------- Data handling -----------
        if(!sitemap.getTasks().stream().filter(p->p.second.getFailedTasks().size()>0) // if any exceptions has been caught
                .toList().isEmpty()){
            System.out.println("--------- EXCEPTIONS WHILE RUNNING SCRAPER ----------");
            // print all exception messages from tasks
            sitemap.getTasks().stream().map(p->p.second.getFailedTasks())
                    .toList().forEach(l-> l.forEach(p->{
                        System.out.println("Task id: "+p.first);
                        System.out.println("Error message: "+p.second.getMessage());
                    }));
            return; // Don't print any data since the sitemap is broken.
        }
        // group and print data from tasks with same id together
        linkXpaths.forEach(s->{
            sitemap.getTasks().forEach(p->{
                Stream.generate(() -> "-").limit(100).forEach(System.out::print);
                System.out.print("\n");
                p.second.getAllDataWithId(s).forEach(System.out::println);
            });
        });

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
