package se.miun.dt002g.webscraper.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUITest extends Application
{
	@Override
	public void start(Stage stage)
	{
		Scene scene = new Scene(new SitemapController());

		stage.setScene(scene);
		stage.setResizable(false);
		stage.sizeToScene();
		stage.show();
	}

	public static void main(String[] args)
	{
		launch();
	}
}
