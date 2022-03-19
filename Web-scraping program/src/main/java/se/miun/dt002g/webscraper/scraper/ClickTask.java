package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Task for clicking an element in a web-page.
 */
public class ClickTask extends Task
{
	public String xPathToElement; // path to the element which should be clicked

	public ClickTask(String xPathToElement, String id)
	{
		super(id);
		this.xPathToElement = xPathToElement;
	}

	public ClickTask(String xPathToElement, Task doFirst, String id)
	{
		super(doFirst, id);
		this.xPathToElement = xPathToElement;
	}

	@Override
	void execute(WebDriver driver) {
		// Use javascript to click an element on the current webpage
		JavascriptExecutor js = (JavascriptExecutor) driver;
		// Wait for element to appear in 5 seconds
		WebElement element = new WebDriverWait(driver, Duration.ofSeconds(5))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement)));
		js.executeScript("arguments[0].click();", element);
	}

	@Override
	public String toString()
	{
		return "Click - " + xPathToElement;
	}
}
