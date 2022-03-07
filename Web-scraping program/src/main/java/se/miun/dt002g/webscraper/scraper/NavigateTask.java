package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.WebDriver;

/**
 * Task for navigating a webdriver to a new url
 */
public class NavigateTask extends Task
{
	String url;

	public NavigateTask(String url, String id)
	{
		super(id);
		this.url = url;
	}

	public NavigateTask(String url, Task doFirst, String id)
	{
		super(doFirst, id);
		this.url = url;
	}

	@Override
	void execute(WebDriver driver) throws Exception
	{
		driver.navigate().to(url);
	}
}
