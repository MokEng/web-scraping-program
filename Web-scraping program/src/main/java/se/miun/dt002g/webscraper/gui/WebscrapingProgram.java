package se.miun.dt002g.webscraper.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Class that launches the program.
 */
public class WebscrapingProgram extends Application
{
	SitemapController sitemapController;
	@Override
	public void start(Stage stage)
	{
		// Creates and shows the SitemapController window.
		sitemapController = new SitemapController();
		Scene scene = new Scene(sitemapController);
		stage.setTitle("Web scraping application");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		// Saves the settings, sitemaps, and scheduled sitemaps when the program closes.
		super.stop();
		sitemapController.saveSitemaps();
		sitemapController.saveSettings();
		sitemapController.saveScheduledScrapes();
	}
	public static void main(String[] args)
	{
		launch();
	}
}
