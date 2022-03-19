package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.Sitemap;
import se.miun.dt002g.webscraper.scraper.Task;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Window that displays and allows editing of the tasks in a sitemap.
 */
public class TaskController extends GridPane
{
	private final String baseURL; // The base URL of the sitemap.
	private final Sitemap sitemap;
	private Task selectedTask; // The currently selected task.

	public TaskController(String url, Sitemap sitemap)
	{
		baseURL = url;
		this.sitemap = sitemap;
		Label taskLabel = new Label("Sitemap Branches");
		taskLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		Label baseURLLabel = new Label("Base URL ");
		baseURLLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField baseURLField = new TextField();
		baseURLField.setText(url);
		baseURLField.setEditable(false);
		baseURLField.setTooltip(new Tooltip("The URL all tasks in the sitemap start from"));

		Button addButton = new Button("Add"),
				editButton = new Button("Edit"),
				deleteButton = new Button("Delete");

		addButton.setMinWidth(50);
		editButton.setMinWidth(50);
		deleteButton.setMinWidth(50);
		addButton.setTooltip(new Tooltip("Add new task"));
		editButton.setTooltip(new Tooltip("Edit task"));
		deleteButton.setTooltip(new Tooltip("Delete task"));

		VBox buttonVBox = new VBox(5, addButton, editButton, deleteButton);

		ListView<Task> list = new ListView<>();
		list.setItems(FXCollections.observableArrayList(sitemap.getTasks())); // Add the tasks to the list.

		addButton.setOnAction(event -> // Open the task creation window.
		{
			Stage addStage = new Stage();
			addStage.setResizable(false);
			addStage.initModality(Modality.APPLICATION_MODAL);
			addStage.setTitle("New Task Chain");
			TaskCreator taskCreator = new TaskCreator(baseURL, null);
			addStage.setScene(new Scene(taskCreator));
			addStage.sizeToScene();
			addStage.showAndWait();

			Task createdTask = taskCreator.getTask();
			if (createdTask != null) // If a task was created, add it to the list.
			{
				sitemap.addTask(createdTask);
				list.setItems(FXCollections.observableArrayList(sitemap.getTasks()));
			}
		});

		editButton.setOnAction(event -> // Open the task creation window with the currently selected task loaded.
		{
			Optional<Task> editTask = sitemap.getTasks().stream().filter(t -> Objects.equals(t, selectedTask)).findAny();

			if (editTask.isPresent()) // If a task is selected.
			{
				List<Task> tasks = sitemap.getTasks();
				tasks.remove(editTask.get());

				Stage addStage = new Stage();
				addStage.setResizable(false);
				addStage.initModality(Modality.APPLICATION_MODAL);
				addStage.setTitle("Edit Task Chain");
				TaskCreator taskCreator = new TaskCreator(baseURL, editTask.get());
				addStage.setScene(new Scene(taskCreator));
				addStage.sizeToScene();
				addStage.showAndWait();

				Task newTask = taskCreator.getTask();
				if (newTask != null) tasks.add(newTask); // If a task was created, add it to the list.
				list.setItems(FXCollections.observableArrayList(sitemap.getTasks()));
				list.getSelectionModel().selectFirst();
			}
		});

		deleteButton.setOnAction(event ->
		{
			Optional<Task> removeTask = sitemap.getTasks().stream().filter(t -> Objects.equals(t, selectedTask)).findAny();

			if (removeTask.isPresent()) // Remove a task if it is selected.
			{
				List<Task> tasks = sitemap.getTasks();
				tasks.remove(removeTask.get());

				list.setItems(FXCollections.observableArrayList(sitemap.getTasks()));
				list.getSelectionModel().selectFirst();
			}
		});

		list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
				selectedTask = list.getSelectionModel().getSelectedItem());

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
