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

/**
 * Window that allows for the creation of task chains.
 */
public class TaskCreator extends GridPane
{
	// The start and end of the CSS class that is added to all webpages.
	private static final String CSS_START =
			".selectedNode" +
					"{" +
					"background-color: ";
	private static final String CSS_END =
			" !important; }";

	private Node selectedNode = null; // The currently selected node.
	private String selectedNodePreviousClass = null; // The classes of the currently selected node.
	private List<Node> selectedNodes = null; // The currently selected nodes.

	private final Stack<Task> tasks; // Stack containing all tasks in the chain.

	private WebView webView;

	public TaskCreator(String url, Task editTask)
	{
		if (editTask == null) tasks = new Stack<>(); // If this is a new task chain.
		else tasks = taskToStack(editTask); // If a task chain is to be edited.

		setVgap(5);
		setHgap(10);
		setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");

		Label createLabel = new Label("Create New Task Chain");
		createLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		Label addTaskLabel = new Label("Task Type ");
		addTaskLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		RadioButton textButton = new RadioButton("Text"),
				clickButton = new RadioButton("Click"),
				navigateButton = new RadioButton("Navigate");

		textButton.setMinWidth(50);
		clickButton.setMinWidth(50);
		navigateButton.setMinWidth(50);
		textButton.setTooltip(new Tooltip("Task that is used to scrape text"));
		clickButton.setTooltip(new Tooltip("Task that is to simulate a click on the webpage"));
		navigateButton.setTooltip(new Tooltip("Task that is used to change to a new URL. " +
				"Enter the URl in the URL/xPath field and then press the ADD button to go to a new URL"));

		ToggleGroup typeGroup = new ToggleGroup();
		textButton.setToggleGroup(typeGroup);
		clickButton.setToggleGroup(typeGroup);
		navigateButton.setToggleGroup(typeGroup);
		textButton.setSelected(true);

		HBox taskButtonHBox = new HBox(5, textButton, clickButton, navigateButton);

		Label urlPathLabel = new Label("URL/xPath ");
		urlPathLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField urlPathField = new TextField();
		urlPathField.setTooltip(new Tooltip("The xPath of the element you want to scrape or click, or the URL you want to navigate to"));

		Label idLabel = new Label("Task ID ");
		idLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField idField = new TextField();
		idField.setTooltip(new Tooltip("The id of the task. Can be used to group the scraped data"));

		Label nameLabel = new Label("Data Name ");
		nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField nameField = new TextField();
		nameField.setTooltip(new Tooltip("The name/type of the task data. Can be used to group the scraped data"));

		ToggleButton selectButton = new ToggleButton("Select Element");
		selectButton.setTooltip(new Tooltip("Press the button to enable element selection mode. " +
				"When active, clicking on an element on the webpage will select it and fill the xPath field with the path to the element. " +
				"Normal click events do not occur when element selection mode is active."));

		selectButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue) // Deselect all selected nodes when quitting out of the selection mode.
			{
				deselectNodes();
			}
		});

		Button backArrowButton = new Button("<-");
		backArrowButton.setMinWidth(50);
		backArrowButton.setTooltip(new Tooltip("Navigates back in the webpage history. Acts the same as the back button in browsers"));
		Button selectChildrenButton = new Button("Select Children");
		selectChildrenButton.setTooltip(new Tooltip("Select all elements inside the currently selected element"));
		selectChildrenButton.setMinWidth(50);
		Button selectSiblingsButton = new Button("Select Siblings");
		selectSiblingsButton.setTooltip(new Tooltip("Select all elements that are in the same parent element as the currently selected element"));
		Button selectParentButton = new Button("Select Parent");
		selectParentButton.setMinWidth(50);
		selectParentButton.setTooltip(new Tooltip("Select the element containing the current selected element"));
		Button goToRootButton = new Button("Go to Root");
		goToRootButton.setMinWidth(50);
		goToRootButton.setTooltip(new Tooltip("Navigates to the root URL"));
		HBox domManipButtonHBox = new HBox(5, backArrowButton, selectChildrenButton, selectSiblingsButton, selectParentButton, goToRootButton);

		Button addButton = new Button("Add Task"),
				removeButton = new Button("Remove Task"),
				saveButton = new Button("Save Chain");
		addButton.setMinWidth(50);
		addButton.setStyle("-fx-background-color: lightblue;");
		removeButton.setMinWidth(50);
		saveButton.setMinWidth(50);
		addButton.setTooltip(new Tooltip("Adds a task with the currently selected options"));
		removeButton.setTooltip(new Tooltip("Removes the latest task from the list"));
		saveButton.setTooltip(new Tooltip("Saves and combines all tasks in the list and adds it to the sitemap"));

		HBox buttonHBox = new HBox(5, addButton, removeButton, saveButton);

		Label taskListLabel = new Label("Task Chain");
		taskListLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		ListView<Task> taskList = new ListView<>();
		taskList.setItems(FXCollections.observableArrayList(tasks));

		addButton.setOnAction(event ->
		{
			String taskType = ((RadioButton)typeGroup.getSelectedToggle()).getText(); // Get the task type.

			switch (taskType)
			{
				case "Text" -> // Text tasks.
				{
					String textPath = urlPathField.getText();
					String id = idField.getText();
					String dataName = nameField.getText();

					// If only one node is selected.
					if (textPath != null && !textPath.isEmpty() && selectedNodes == null)
					{
						TextTask textTask;
						// If there are tasks already in the chain, add this task to the chain.
						if (tasks.isEmpty()) textTask = new TextTask(textPath, id, dataName);
						else textTask = new TextTask(textPath, tasks.peek(), id, dataName);
						tasks.push(textTask);
						taskList.setItems(FXCollections.observableArrayList(tasks));
					}
					else if (selectedNodes != null) // If multiple nodes are selected.
					{
						for (int i = 0; i < selectedNodes.size(); i++) // Create a new task for each selected node.
						{
							textPath = NodeUtilities.getXPath(selectedNodes.get(i));
							TextTask textTask;
							// If there are tasks already in the chain, add this task to the chain.
							if (tasks.isEmpty()) textTask = new TextTask(textPath, id+i, dataName);
							else textTask = new TextTask(textPath, tasks.peek(), id+i, dataName);
							tasks.push(textTask);
						}
						taskList.setItems(FXCollections.observableArrayList(tasks));
					}
				}
				case "Click" -> // Click tasks.
				{
					String clickPath = urlPathField.getText();
					// If only one node is selected.
					if (clickPath != null && !clickPath.isEmpty() && selectedNodes == null)
					{
						ClickTask clickTask;
						// If there are tasks already in the chain, add this task to the chain.
						if (tasks.isEmpty()) clickTask = new ClickTask(clickPath, "");
						else clickTask = new ClickTask(clickPath, tasks.peek(), "");
						tasks.push(clickTask);
						taskList.setItems(FXCollections.observableArrayList(tasks));
					}
				}
				case "Navigate" -> // Navigate tasks.
				{
					String navigateUrl = urlPathField.getText();
					if (!navigateUrl.startsWith("http://") && !navigateUrl.startsWith("https://"))
					{
						navigateUrl = "https://" + navigateUrl; // Add https to url if not provided by the user.
					}

					if (navigateUrl != null && !navigateUrl.isEmpty() && selectedNodes == null)
					{
						NavigateTask navigateTask;
						// If there are tasks already in the chain, add this task to the chain.
						if (tasks.isEmpty()) navigateTask = new NavigateTask(navigateUrl, "navigate");
						else navigateTask = new NavigateTask(navigateUrl, tasks.peek(), "navigate");
						tasks.push(navigateTask);
						taskList.setItems(FXCollections.observableArrayList(tasks));


						webView.getEngine().load(navigateUrl); // Load the new URL.
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
			if (history.getCurrentIndex() != 0) // If there is history to go back to.
			{
				// Create a new NavigateTask that navigates to the previous page in the history.
				NavigateTask navigateTask;
				String backUrl = history.getEntries().get(history.getCurrentIndex() - 1).getUrl();
				if (tasks.isEmpty()) navigateTask = new NavigateTask(backUrl, "back");
				else navigateTask = new NavigateTask(backUrl, tasks.peek(), "back");
				tasks.push(navigateTask);
				taskList.setItems(FXCollections.observableArrayList(tasks));

				// Run the history.back() JS function on the browser.
				Platform.runLater(() -> webView.getEngine().executeScript("history.back()"));
			}
		});

		removeButton.setOnAction(event ->
		{
			if (!tasks.isEmpty()) // If there are tasks to remove.
			{
				Task task = tasks.pop();
				taskList.setItems(FXCollections.observableArrayList(tasks));
			}
		});

		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue == Worker.State.SUCCEEDED) // If the page has finished loading.
			{
				Document doc = webView.getEngine().getDocument();
				// Create and add a style element with the custom selectedNode class.
				Element styleNode = doc.createElement("style");
				Text styleContent = doc.createTextNode(CSS_START + "cyan" + CSS_END);
				styleNode.appendChild(styleContent);
				doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);

				// Get a list of all elements in the page.
				NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName("*");

				for (int i = 0; i < nodeList.getLength(); i++)
				{
					// Add an onClickListener to all nodes.
					NodeUtilities.addOnclick(nodeList.item(i), evt ->
					{
						if (selectButton.isSelected()) // If the "Select Element" button is active.
						{
							deselectNodes(); // Deselect all nodes.

							// Add custom class to element.
							selectedNode = (Node)evt.getTarget();
							selectedNodePreviousClass = ((HTMLElement)selectedNode).getClassName();
							((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass + " selectedNode");

							// Set the path field to the path of the node.
							urlPathField.setText(NodeUtilities.getXPath((Node)evt.getTarget()));
							// Stop the click from continuing up the event path.
							evt.preventDefault();
							evt.stopPropagation();
						}
						else // If element selection mode is not on.
						{
							deselectNodes(); // Deselect all nodes.

							// Create a new ClickTask clicking on what you clicked on.
							ClickTask clickTask;
							if (tasks.isEmpty()) clickTask = new ClickTask(NodeUtilities.getXPath((Node)evt.getTarget()), "");
							else clickTask = new ClickTask(NodeUtilities.getXPath((Node)evt.getTarget()), tasks.peek(), "");

							// Don't allow multiple ClickTasks clicking on the same thing in a row.
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

		saveButton.setOnAction(event -> // Closes the window.
		{
			((Stage)getScene().getWindow()).close();
		});

		selectChildrenButton.setOnAction(event -> // Select all child nodes.
		{
			if (selectedNode != null)
			{
				NodeList children = selectedNode.getChildNodes(); // Get all child nodes.
				int nChildren = children.getLength();

				if (nChildren > 0)
				{
					deselectNodes();
					selectedNodes = new ArrayList<>();

					for (int i = 0; i < nChildren; i++)
					{
						// Only add nodes that aren't of the type #text.
						if (!children.item(i).getNodeName().equals("#text")) selectedNodes.add(children.item(i));
					}

					for (Node n : selectedNodes) // Add the selectedNode class to all selected nodes.
					{
						HTMLElement e = (HTMLElement)n;
						e.setClassName(e.getClassName() + " selectedNode");
					}

					urlPathField.setText("<Multiple values>");
				}
			}
		});

		selectSiblingsButton.setOnAction(event -> // Select add sibling nodes.
		{
			NodeList siblings = selectedNode.getParentNode().getChildNodes(); // Get all sibling nodes.
			int nSiblings = siblings.getLength();

			if (nSiblings > 0)
			{
				deselectNodes();
				selectedNodes = new ArrayList<>();

				for (int i = 0; i < nSiblings; i++)
				{
					// Only add nodes that aren't of the type #text.
					if (!siblings.item(i).getNodeName().equals("#text")) selectedNodes.add(siblings.item(i));
				}

				for (Node n : selectedNodes) // Add the selectedNode class to all selected nodes.
				{
					HTMLElement e = (HTMLElement)n;
					e.setClassName(e.getClassName() + " selectedNode");
				}

				urlPathField.setText("<Multiple values>");
			}
		});

		selectParentButton.setOnAction(event -> // Select the parent node.
		{
			if (selectedNode != null) // If only on node is selected.
			{
				Node n = selectedNode.getParentNode(); // Get the parent node.
				deselectNodes();
				selectedNode = n;
				// Add the selectedNode class to the parent node.
				HTMLElement e = (HTMLElement)selectedNode;
				e.setClassName(e.getClassName() + " selectedNode");
				urlPathField.setText(NodeUtilities.getXPath(selectedNode));
			}
			else if (selectedNodes != null && selectedNodes.size() != 0) // If multiple nodes are selected.
			{
				Node n = selectedNodes.get(0).getParentNode(); // Get the parent node.
				deselectNodes();
				selectedNode = n;
				// Add the selectedNode class to the parent node.
				HTMLElement e = (HTMLElement)selectedNode;
				e.setClassName(e.getClassName() + " selectedNode");
				urlPathField.setText(NodeUtilities.getXPath(selectedNode));
			}
		});

		goToRootButton.setOnAction(event -> // Loads the root URL.
		{
			tasks.push(new NavigateTask(url, ""));
			taskList.setItems(FXCollections.observableArrayList(tasks));
			webView.getEngine().load(url);
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

	/**
	 * Deselects all selected nodes.
	 */
	private void deselectNodes()
	{
		if (selectedNode != null) // Remove the custom class from the selected node.
		{
			HTMLElement e = (HTMLElement)selectedNode;
			if (e != null && e.getClassName().contains("selectedNode"))
				e.setClassName(e.getClassName().substring(0, e.getClassName().lastIndexOf(" ")));
			selectedNode = null;
		}

		if (selectedNodes != null) // Remove the custom class from the selected nodes.
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

	/**
	 * Turn a Task chain into a stack.
	 * @param task The task chain.
	 * @return A Stack containing the Tasks in the same order as in the Task chain.
	 */
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

	/**
	 * Get the Task chain.
	 * @return The task chain.
	 */
	public Task getTask()
	{
		return tasks.isEmpty() ? null : tasks.peek();
	}
}
