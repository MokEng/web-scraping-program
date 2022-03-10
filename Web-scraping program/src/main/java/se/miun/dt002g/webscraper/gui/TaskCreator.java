package se.miun.dt002g.webscraper.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.w3c.dom.html.HTMLElement;
import se.miun.dt002g.webscraper.scraper.ClickTask;
import se.miun.dt002g.webscraper.scraper.NavigateTask;
import se.miun.dt002g.webscraper.scraper.Task;
import se.miun.dt002g.webscraper.scraper.TextTask;

import java.util.*;

public class TaskCreator extends GridPane
{
	private static final String CSS_START =
			".selectedNode" +
					"{" +
					"background-color: ";
	private static final String CSS_END =
			" !important; }";

	private Node selectedNode = null;
	private String selectedNodePreviousClass = null;
	private List<Node> selectedNodes = null;

	private final Stack<Task> tasks;
	private String lastestButtonPressed = "";

	private WebView webView;

	public TaskCreator(String url, Task editTask)
	{
		if (editTask == null) tasks = new Stack<>();
		else tasks = taskToStack(editTask);

		setVgap(5);
		setHgap(10);
		setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");

		Label createLabel = new Label("Create New Task");
		createLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		Label addTaskLabel = new Label("Task Type ");
		addTaskLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		RadioButton textButton = new RadioButton("Text"),
				clickButton = new RadioButton("Click"),
				navigateButton = new RadioButton("Navigate");

		textButton.setMinWidth(50);
		clickButton.setMinWidth(50);
		navigateButton.setMinWidth(50);

		ToggleGroup typeGroup = new ToggleGroup();
		textButton.setToggleGroup(typeGroup);
		clickButton.setToggleGroup(typeGroup);
		navigateButton.setToggleGroup(typeGroup);
		textButton.setSelected(true);

		HBox taskButtonHBox = new HBox(5, textButton, clickButton, navigateButton);

		Label urlPathLabel = new Label("URL/xPath ");
		urlPathLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField urlPathField = new TextField();

		Label idLabel = new Label("Task ID ");
		idLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField idField = new TextField();

		Label nameLabel = new Label("Data Name ");
		nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField nameField = new TextField();

		ToggleButton selectButton = new ToggleButton("Select Element");
		selectButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue && selectedNode != null)
			{
				deselectNodes();
			}
		});

		Button backArrowButton = new Button("<-");
		backArrowButton.setMinWidth(50);
		Button selectChildrenButton = new Button("Select Children");
		selectChildrenButton.setMinWidth(50);
		Button selectSiblingsButton = new Button("Select Siblings");
		Button selectParentButton = new Button("Select Parent");
		selectParentButton.setMinWidth(50);
		HBox domManipButtonHBox = new HBox(5, backArrowButton, selectChildrenButton, selectSiblingsButton, selectParentButton);

		Button addButton = new Button("Add Task"),
				removeButton = new Button("Remove Task"),
				saveButton = new Button("Save Task");
		addButton.setMinWidth(50);
		removeButton.setMinWidth(50);
		saveButton.setMinWidth(50);

		HBox buttonHBox = new HBox(5, addButton, removeButton, saveButton);

		Label taskListLabel = new Label("Task List");
		taskListLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		ListView<Task> taskList = new ListView<>();
		taskList.setItems(FXCollections.observableArrayList(tasks));

		addButton.setOnAction(event ->
		{
			String taskType = ((RadioButton)typeGroup.getSelectedToggle()).getText();

			switch (taskType)
			{
				case "Text" ->
				{
					String textPath = urlPathField.getText();
					String id = idField.getText();
					String dataName = nameField.getText();

					if (textPath != null && !textPath.isEmpty() && selectedNodes == null)
					{
						TextTask textTask;
						if (tasks.isEmpty()) textTask = new TextTask(textPath, id, dataName);
						else textTask = new TextTask(textPath, tasks.peek(), id, dataName);
						tasks.push(textTask);
						taskList.setItems(FXCollections.observableArrayList(tasks));
					}
					else if (selectedNodes != null)
					{
						for (int i = 0; i < selectedNodes.size(); i++)
						{
							textPath = NodeUtilities.getXPath(selectedNodes.get(i));
							TextTask textTask;
							if (tasks.isEmpty()) textTask = new TextTask(textPath, id+i, dataName);
							else textTask = new TextTask(textPath, tasks.peek(), id+i, dataName);
							tasks.push(textTask);
						}
						taskList.setItems(FXCollections.observableArrayList(tasks));
					}
				}
				case "Click" ->
				{
					String clickPath = urlPathField.getText();
					if (clickPath != null && !clickPath.isEmpty() && selectedNodes == null)
					{
						ClickTask clickTask;
						if (tasks.isEmpty()) clickTask = new ClickTask(clickPath, "");
						else clickTask = new ClickTask(clickPath, tasks.peek(), "");
						tasks.push(clickTask);
						taskList.setItems(FXCollections.observableArrayList(tasks));
					}
				}
				case "Navigate" ->
				{
					String navigateUrl = urlPathField.getText();
					if (navigateUrl != null && !navigateUrl.isEmpty() && selectedNodes == null)
					{
						NavigateTask navigateTask;
						if (tasks.isEmpty()) navigateTask = new NavigateTask(navigateUrl, "navigate");
						else navigateTask = new NavigateTask(navigateUrl, tasks.peek(), "navigate");
						tasks.push(navigateTask);
						taskList.setItems(FXCollections.observableArrayList(tasks));

						webView.getEngine().load(navigateUrl);
					}
				}
			}
		});

		webView = new WebView();
		webView.getEngine().load(url);
		GridPane.setVgrow(webView, Priority.ALWAYS);
		backArrowButton.setOnAction(event ->
		{
			WebHistory history = webView.getEngine().getHistory();
			if (history.getCurrentIndex() != 0)
			{
				NavigateTask navigateTask;
				String backUrl = history.getEntries().get(history.getCurrentIndex() - 1).getUrl();
				if (tasks.isEmpty()) navigateTask = new NavigateTask(backUrl, "back");
				else navigateTask = new NavigateTask(backUrl, tasks.peek(), "back");
				tasks.push(navigateTask);
				taskList.setItems(FXCollections.observableArrayList(tasks));
				Platform.runLater(() -> webView.getEngine().executeScript("history.back()"));
				lastestButtonPressed = "back";
			}
		});
		removeButton.setOnAction(event ->
		{
			if (!tasks.isEmpty())
			{
				Task task = tasks.pop();
				if (task instanceof NavigateTask)
				{
					if (Objects.equals(task.id, "back"))
					{
						webView.getEngine().executeScript("window.history.forward()");
					}
					else if (Objects.equals(task.id, "navigate"))
					{
						webView.getEngine().executeScript("history.back()");
					}
				}
				taskList.setItems(FXCollections.observableArrayList(tasks));
				lastestButtonPressed = "remove";
			}
		});
		webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!tasks.isEmpty() && !lastestButtonPressed.equals("remove"))
			{
				tasks.pop();

				NavigateTask navigateTask;
				if (tasks.isEmpty()) navigateTask = new NavigateTask(newValue, "navigate");
				else navigateTask = new NavigateTask(newValue, tasks.peek(), "navigate");
				tasks.push(navigateTask);
				taskList.setItems(FXCollections.observableArrayList(tasks));
			}
		});

		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED)
			{
				Document doc = webView.getEngine().getDocument();
				Element styleNode = doc.createElement("style");
				Text styleContent = doc.createTextNode(CSS_START + "cyan" + CSS_END);
				styleNode.appendChild(styleContent);
				doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);

				NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName("*");

				for (int i = 0; i < nodeList.getLength(); i++)
				{
					NodeUtilities.addOnclick(nodeList.item(i), evt ->
					{
						if (selectButton.isSelected())
						{
							deselectNodes();

							selectedNode = (Node)evt.getTarget();
							selectedNodePreviousClass = ((HTMLElement)selectedNode).getClassName();
							((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass + " selectedNode");

							urlPathField.setText(NodeUtilities.getXPath((Node)evt.getTarget()));
							evt.preventDefault();
							evt.stopPropagation();
						}
						else
						{
							lastestButtonPressed = "";
							deselectNodes();

							ClickTask clickTask;
							if (tasks.isEmpty()) clickTask = new ClickTask(NodeUtilities.getXPath((Node)evt.getTarget()), "");
							else clickTask = new ClickTask(NodeUtilities.getXPath((Node)evt.getTarget()), tasks.peek(), "");

							if (tasks.isEmpty() || !Objects.equals(tasks.peek().toString(), clickTask.toString()))
							{
								tasks.push(clickTask);
								taskList.setItems(FXCollections.observableArrayList(tasks));
							}
						}
					});
				}
			}
		});

		saveButton.setOnAction(event -> ((Stage)getScene().getWindow()).close());

		selectChildrenButton.setOnAction(event ->
		{
			if (selectedNode != null)
			{
				NodeList children = selectedNode.getChildNodes();
				int nChildren = children.getLength();

				if (nChildren > 0)
				{
					deselectNodes();
					selectedNodes = new ArrayList<>();

					for (int i = 0; i < nChildren; i++)
					{
						if (!children.item(i).getNodeName().equals("#text")) selectedNodes.add(children.item(i));
					}

					for (Node n : selectedNodes)
					{
						HTMLElement e = (HTMLElement)n;
						e.setClassName(e.getClassName() + " selectedNode");
					}

					urlPathField.setText("<Multiple values>");
				}
			}
		});

		selectSiblingsButton.setOnAction(event ->
		{
			NodeList siblings = selectedNode.getParentNode().getChildNodes();
			int nSiblings = siblings.getLength();

			if (nSiblings > 0)
			{
				deselectNodes();
				selectedNodes = new ArrayList<>();

				for (int i = 0; i < nSiblings; i++)
				{
					if (!siblings.item(i).getNodeName().equals("#text")) selectedNodes.add(siblings.item(i));
				}

				for (Node n : selectedNodes)
				{
					HTMLElement e = (HTMLElement)n;
					e.setClassName(e.getClassName() + " selectedNode");
				}

				urlPathField.setText("<Multiple values>");
			}
		});

		selectParentButton.setOnAction(event ->
		{
			if (selectedNode != null)
			{
				Node n = selectedNode.getParentNode();
				deselectNodes();
				selectedNode = n;
				HTMLElement e = (HTMLElement)selectedNode;
				e.setClassName(e.getClassName() + " selectedNode");
				urlPathField.setText(NodeUtilities.getXPath(selectedNode));
			}
			else if (selectedNodes != null && selectedNodes.size() != 0)
			{
				Node n = selectedNodes.get(0).getParentNode();
				deselectNodes();
				selectedNode = n;
				HTMLElement e = (HTMLElement)selectedNode;
				e.setClassName(e.getClassName() + " selectedNode");
				urlPathField.setText(NodeUtilities.getXPath(selectedNode));
			}
		});

		add(createLabel, 0, 0, 2, 1);
		add(addTaskLabel, 0, 1, 1, 1);
		add(taskButtonHBox, 1, 1, 1, 1);
		add(urlPathLabel, 0, 2, 1, 1);
		add(urlPathField, 1, 2, 1, 1);
		add(idLabel, 0, 3, 1, 1);
		add(idField, 1, 3, 1, 1);
		add(nameLabel, 0, 4,1,1);
		add(nameField, 1, 4,1,1);
		add(selectButton, 0 , 6, 1, 1);
		add(domManipButtonHBox, 1, 6, 1, 1);
		add(taskListLabel, 2, 1, 1, 1);
		add(buttonHBox, 2, 2, 1, 1);
		add(taskList, 2, 3, 1, 5);
		add(webView, 0 ,7, 2, 1);

		textButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(false);
			idField.setDisable(false);
			nameField.setDisable(false);
		});
		clickButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(false);
			idField.setDisable(true);
			nameField.setDisable(true);
		});
		navigateButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(false);
			idField.setDisable(true);
			nameField.setDisable(true);
		});
	}

	private void deselectNodes()
	{
		if (selectedNode != null)
		{
			HTMLElement e = (HTMLElement)selectedNode;
			if (e != null && e.getClassName().contains("selectedNode"))
				e.setClassName(e.getClassName().substring(0, e.getClassName().lastIndexOf(" ")));
			selectedNode = null;
		}

		if (selectedNodes != null)
		{
			for (Node n : selectedNodes)
			{
				HTMLElement e = (HTMLElement)n;
				if (e != null && e.getClassName().contains("selectedNode"))
					e.setClassName(e.getClassName().substring(0, e.getClassName().lastIndexOf(" ")));
			}

			selectedNodes = null;
		}
	}

	private Stack<Task> taskToStack(Task task)
	{
		Stack<Task> done = new Stack<>();
		List<Task> temp = new ArrayList<>();

		Task currTask = task;
		while (currTask != null)
		{
			temp.add(currTask);
			currTask = currTask.getDoFirst();
		}

		Collections.reverse(temp);
		for (Task t : temp)
		{
			done.push(t);
		}

		return done;
	}

	public Task getTask()
	{
		return tasks.isEmpty() ? null : tasks.peek();
	}
}
