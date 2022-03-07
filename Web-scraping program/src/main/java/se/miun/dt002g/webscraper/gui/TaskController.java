package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.Sitemap;
import se.miun.dt002g.webscraper.scraper.Task;

public class TaskController extends GridPane
{
	private final String baseURL;
	private final Sitemap sitemap;

	public TaskController(String url, Sitemap sitemap)
	{
		baseURL = url;
		this.sitemap = sitemap;
		Label taskLabel = new Label("Tasks");
		taskLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		Label baseURLLabel = new Label("Base URL ");
		baseURLLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField baseURLField = new TextField();
		baseURLField.setText(url);
		baseURLField.setEditable(false);

		Button addButton = new Button("Add"),
				editButton = new Button("Edit"),
				deleteButton = new Button("Delete");

		addButton.setMinWidth(50);
		editButton.setMinWidth(50);
		deleteButton.setMinWidth(50);

		VBox buttonVBox = new VBox(5, addButton, editButton, deleteButton);

		ListView<Task> list = new ListView<>();

		addButton.setOnAction(event ->
		{
			Stage addStage = new Stage();
			addStage.setResizable(false);
			addStage.initModality(Modality.APPLICATION_MODAL);
			addStage.setTitle("Create New Task");
			TaskCreator taskCreator = new TaskCreator(baseURL);
			addStage.setScene(new Scene(taskCreator));
			addStage.sizeToScene();
			addStage.showAndWait();

			Task createdTask = taskCreator.getTask();
			if (createdTask != null)
			{
				sitemap.addTask(createdTask);
				list.setItems(FXCollections.observableArrayList(sitemap.getTasks()));
			}
		});

		setVgap(10);
		setHgap(10);
		setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");

		add(taskLabel, 0, 0, 2, 1);
		add(baseURLLabel, 0, 1);
		add(baseURLField, 1, 1);
		add(buttonVBox, 0, 2);
		add(list, 1, 2);
	}
}
