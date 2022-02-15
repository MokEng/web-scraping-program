import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Test {
    public static void main(String[] args) {

        //System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        WebDriver driver;

        driver = new ChromeDriver();
        driver.navigate().to("https://www.indeed.com/");
        //driver.manage().window().maximize();
        System.out.println(driver.getPageSource());

        driver.close();

    }
}
