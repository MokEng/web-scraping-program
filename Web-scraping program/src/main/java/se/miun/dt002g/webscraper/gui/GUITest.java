package se.miun.dt002g.webscraper.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUITest extends Application
{
	SitemapController sitemapController;
	@Override
	public void start(Stage stage)
	{

		sitemapController = new SitemapController();
		Scene scene = new Scene(sitemapController);

		stage.setScene(scene);
		stage.setResizable(false);
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		sitemapController.saveSitemaps();
	}
	public static void main(String[] args)
	{
		launch();
	}
}
