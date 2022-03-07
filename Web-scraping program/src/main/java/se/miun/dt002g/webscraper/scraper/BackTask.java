package se.miun.dt002g.webscraper.scraper;

import org.openqa.selenium.WebDriver;

/**
 * Task for ordering a web-window to go back to last visited page.
 */
public class BackTask extends Task
{

	public BackTask(String id)
	{
		super(id);
	}

	public BackTask(Task doFirst, String id)
	{
		super(doFirst, id);
	}

	@Override
	void execute(WebDriver driver) throws Exception
	{
		driver.navigate().back();
	}
}
