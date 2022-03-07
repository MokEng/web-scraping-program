package se.miun.dt002g.webscraper.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SitemapController extends GridPane
{
	public SitemapController()
	{
		Label sitemapLabel = new Label("Sitemaps");
		sitemapLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		Button addButton = new Button("New"),
				editButton = new Button("Edit"),
				deleteButton = new Button("Delete"),
				loadButton = new Button("Load"),
				saveButton = new Button("Save");

		addButton.setMinWidth(50);
		editButton.setMinWidth(50);
		deleteButton.setMinWidth(50);
		loadButton.setMinWidth(50);
		saveButton.setMinWidth(50);

		addButton.setOnAction(event ->
		{
			String baseURL = null;
			EnterBaseURLPopup popup = new EnterBaseURLPopup();
			baseURL = popup.getBaseURL();
			System.out.println(baseURL);

			if (baseURL != null)
			{
				Stage addStage = new Stage();
				addStage.setResizable(false);
				addStage.initModality(Modality.APPLICATION_MODAL);
				addStage.setTitle("Create New Sitemap");
				addStage.setScene(new Scene(new TaskController(baseURL)));
				addStage.sizeToScene();

				addStage.showAndWait();
			}
		});

		VBox buttonVBox = new VBox(5, addButton, editButton, deleteButton, saveButton, loadButton);

		ListView<String> sitemapList = new ListView<>();

		Button runButton = new Button("Run"),
				scheduleButton = new Button("Schedule");
		runButton.setMinWidth(65);
		scheduleButton.setMinWidth(65);
		VBox runButtonVbox = new VBox(5, runButton, scheduleButton);

		ListView<String> taskList = new ListView<>();

		Label selectedSitemapLabel = new Label("Selected Sitemap");
		selectedSitemapLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");

		setVgap(10);
		setHgap(5);
		setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");

		add(sitemapLabel, 0, 0, 2, 1);
		add(buttonVBox, 0, 1);
		add(sitemapList, 1, 1);
		add(selectedSitemapLabel, 2, 0, 2, 1);
		add(taskList, 2, 1);
		add(runButtonVbox, 3, 1);
	}
}
