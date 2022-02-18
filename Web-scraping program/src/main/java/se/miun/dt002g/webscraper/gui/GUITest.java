package se.miun.dt002g.webscraper.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;



public class GUITest extends Application
{
	// http://tutorials.jenkov.com/javafx/your-first-javafx-application.html
	// https://stackoverflow.com/questions/60081353/running-javafx-project-in-intellij-with-gradle

	@Override
	public void start(Stage stage)
	{
		String javaVersion = System.getProperty("java.version");
		String javafxVersion = System.getProperty("javafx.version");
		stage.setTitle("GUITest running on Java " + javaVersion + ", JavaFX " + javafxVersion);

		Label guiTestLabel = new Label("GUITest");
		Scene labelScene = new Scene(guiTestLabel, 400, 200);
		stage.setScene(labelScene);

		stage.show();
	}

	public static void main(String[] args)
	{
		launch();
	}
}
