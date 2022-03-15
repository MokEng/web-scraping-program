package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Task for extracting data in form of text from the web.
 */
public class TextTask extends Task
{
	public final String xPathToElement; // path to the element which inner HTML should be scraped.
	public String data; // the extracted data
	public String dataName; // key to find the data;

	public TextTask(String xPathToElement, String id, String dataName)
	{
		super(id);
		this.xPathToElement = xPathToElement;
		this.dataName = dataName;
	}

	public TextTask(String xPathToElement, Task doFirst, String id, String dataName)
	{
		super(doFirst, id);
		this.xPathToElement = xPathToElement;
		this.dataName = dataName;
	}

	@Override
	void execute(WebDriver driver) throws Exception
	{
		// get inner html of the element
		data = new WebDriverWait(driver, Duration.ofSeconds(5))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathToElement))).getText();

	}

	@Override
	public String toString()
	{
		return "Text (" + id + ", " + dataName + ") - " + xPathToElement;
	}
}
