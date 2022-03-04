package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import org.w3c.dom.*;
import org.w3c.dom.html.HTMLElement;

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

	public TaskCreator(String url)
	{
		setVgap(5);
		setHgap(10);
		setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");

		Label createLabel = new Label("Create New Task");
		createLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		Label addTaskLabel = new Label("Task Type ");
		addTaskLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		RadioButton textButton = new RadioButton("Text"),
				backButton = new RadioButton("Back"),
				clickButton = new RadioButton("Click"),
				navigateButton = new RadioButton("Navigate");

		textButton.setMinWidth(50);
		backButton.setMinWidth(50);
		clickButton.setMinWidth(50);
		navigateButton.setMinWidth(50);

		ToggleGroup typeGroup = new ToggleGroup();
		textButton.setToggleGroup(typeGroup);
		backButton.setToggleGroup(typeGroup);
		clickButton.setToggleGroup(typeGroup);
		navigateButton.setToggleGroup(typeGroup);
		textButton.setSelected(true);

		HBox taskButtonHBox = new HBox(5, textButton, backButton, clickButton, navigateButton);

		Label urlPathLabel = new Label("URL/xPath ");
		urlPathLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField urlPathField = new TextField();

		Label idLabel = new Label("Task ID ");
		idLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField idField = new TextField();

		ToggleButton selectButton = new ToggleButton("Select Element");
		selectButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue && selectedNode != null)
			{
				((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass);
			}
		});

		Button backArrowButton = new Button("<-");
		backArrowButton.setMinWidth(50);

		Button addButton = new Button("Add Task"),
				removeButton = new Button("Remove Task"),
				saveButton = new Button("Save Task");
		addButton.setMinWidth(50);
		removeButton.setMinWidth(50);
		saveButton.setMinWidth(50);

		HBox buttonHBox = new HBox(5, addButton, removeButton, saveButton);

		Label taskListLabel = new Label("Task List");
		taskListLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		ListView<String> taskList = new ListView<>();

		WebView webView = new WebView();
		webView.getEngine().load("https://google.se");
		GridPane.setVgrow(webView, Priority.ALWAYS);

		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED)
			{
				Document doc = webView.getEngine().getDocument();
				Element styleNode = doc.createElement("style");
				//Text styleContent = doc.createTextNode(CSS_START + "#" + Integer.toHexString(colorPicker.getValue().hashCode()) + CSS_END);
				Text styleContent = doc.createTextNode(CSS_START + "cyan" + CSS_END);
				styleNode.appendChild(styleContent);
				doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);

				//nodes.clear();
				NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName("*");
				ObservableList<String> obsList = FXCollections.observableArrayList();
				//filteredList = new FilteredList<>(obsList);

				for (int i = 0; i < nodeList.getLength(); i++)
				{
					String xPath = NodeUtilities.getXPath(nodeList.item(i));
					//list.getItems().add(nodeList.item(i).getTextContent() + " || " + xPath);
					obsList.add(nodeList.item(i).getTextContent() + " || " + xPath);
					//nodes.put(xPath, nodeList.item(i));

					NodeUtilities.addOnclick(nodeList.item(i), evt ->
					{
						if (selectButton.isSelected())
						{
							if (selectedNode != null) ((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass);

							selectedNode = (Node)evt.getTarget();
							selectedNodePreviousClass = ((HTMLElement)selectedNode).getClassName();
							((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass + " selectedNode");

							urlPathField.setText(NodeUtilities.getXPath((Node)evt.getTarget()));
							evt.preventDefault();
							evt.stopPropagation();
						}
					});
				}

				//list.setItems(filteredList);
			}
		});

		add(createLabel, 0, 0, 2, 1);
		add(addTaskLabel, 0, 1, 1, 1);
		add(taskButtonHBox, 1, 1, 1, 1);
		add(urlPathLabel, 0, 2, 1, 1);
		add(urlPathField, 1, 2, 1, 1);
		add(idLabel, 0, 3, 1, 1);
		add(idField, 1, 3, 1, 1);
		add(selectButton, 0 , 5, 1, 1);
		add(backArrowButton, 1, 5);
		add(taskListLabel, 2, 1);
		add(buttonHBox, 2, 2, 1, 1);
		add(taskList, 2, 3, 1, 4);
		add(webView, 0 ,6, 2, 1);

		textButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(false);
			idField.setDisable(false);
		});
		backButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(true);
			idField.setDisable(true);
		});
		clickButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(false);
			idField.setDisable(true);
		});
		navigateButton.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			urlPathField.setDisable(false);
			idField.setDisable(true);
		});
	}
}
