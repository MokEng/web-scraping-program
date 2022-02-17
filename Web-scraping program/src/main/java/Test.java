import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class Test {
    public static void main(String[] args) {

        //System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        WebDriver driver;

        driver = new ChromeDriver();
        // Page for top-scorer statistics on Italian Serie A.
        driver.navigate().to("https://www.legaseriea.it/en/serie-a/statistics");
        // Fetch top scorers and wait for element to load. Timeout after 30 seconds.
        WebElement topScorers=new WebDriverWait(driver,30)
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > main > div.container.pagina_wrapper.competizione > section > div:nth-child(1) > section:nth-child(1) > table > tbody")));
        // Links to all players.
        List<WebElement> topScorersPlayerLinks =  topScorers.findElements(By.tagName("a"));
        System.out.println(topScorersPlayerLinks.size());
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Number of links available.
        int size = topScorersPlayerLinks.size();
        // Loop through each of the top scorers.
        for(int x=0 ; x < size;x++ ){
            // Fetch the elements again, otherwise error will occur because of stale elements.
            WebElement temp=new WebDriverWait(driver,30)
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/main/div[1]/section/div[1]/section[1]/table/tbody")));
            List<WebElement> playerLinks =  temp.findElements(By.tagName("a"));
            // Execute javascript for clicking a link for more information on each player.
            js.executeScript("arguments[0].click();",playerLinks.get(x));
            // add info on player
            String playerInfo1 = driver.findElement(By.xpath("/html/body/main/div[1]/section/section[1]/div[2]")).getText();
            playerInfo1+= "\n"+driver.findElement(By.xpath("/html/body/main/div[1]/section/section[1]/div[3]")).getText();
            System.out.println((x+1)+".\n"+playerInfo1+"\n--------------------");
            // go back to top scorer page
            driver.navigate().back();
        }
        driver.close();
    }
}
