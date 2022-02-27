package se.miun.dt002g.webscraper.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLElement;

public class GUITest extends Application
{
	private static final String CSS_START =
			".selectedNode" +
			"{" +
				"background-color: ";
	private static final String CSS_END =
			" !important; }";

	private Node selectedNode = null;
	private String selectedNodePreviousClass = null;

	@Override
	public void start(Stage stage)
	{
		//nodes = new HashMap<>();

		String javaVersion = System.getProperty("java.version");
		String javafxVersion = System.getProperty("javafx.version");
		stage.setTitle("GUITest running on Java " + javaVersion + ", JavaFX " + javafxVersion);

		Label urlLabel = new Label("URL ");
		TextField urlField = new TextField("https://google.se");
		Button urlButton = new Button("Load URL");
		Label tagLabel = new Label("HTML Tag ");
		TextField tagField = new TextField("*");

		WebView webView = new WebView();

		urlButton.setOnAction(action ->
		{
			//if (filteredList != null) filteredList.getSource().clear();
			webView.getEngine().load(urlField.getText());
		});

		HBox hBox = new HBox(5, urlLabel, urlField, urlButton, tagLabel, tagField);
		HBox.setMargin(urlLabel, new Insets(5, 0, 0, 5));
		HBox.setMargin(urlButton, new Insets(0, 5, 0, 0));
		HBox.setMargin(tagLabel, new Insets(5, 0, 0, 0));

		VBox vBox = new VBox(5, hBox, webView);
		VBox.setVgrow(webView, Priority.SOMETIMES);
		VBox.setMargin(hBox, new Insets(5, 0, 0, 0));

		// ---------------------------------- Selection box -----------------------------------------
		Label idLabel = new Label("Id ");
		TextField idField = new TextField();
		idField.setEditable(false);

		Label typeLabel = new Label("Type ");
		ChoiceBox<String> typeChoice = new ChoiceBox<>();
		typeChoice.getItems().addAll("Text", "Image", "Video");
		typeChoice.setStyle("-fx-pref-width: 100;");

		ToggleButton selectButton = new ToggleButton("Select Element");

		Label colorLabel = new Label("Selection Color ");
		ColorPicker colorPicker = new ColorPicker();

		GridPane selectionPane = new GridPane();
		selectionPane.add(idLabel, 0, 0);
		selectionPane.add(idField, 1, 0);
		selectionPane.add(typeLabel, 0, 1);
		selectionPane.add(typeChoice, 1, 1);
		GridPane.setHgrow(typeChoice, Priority.ALWAYS);
		selectionPane.add(selectButton, 0, 3);
		selectionPane.add(colorLabel, 0, 2);
		selectionPane.add(colorPicker, 1, 2);
		selectionPane.setVgap(10);
		selectionPane.setHgap(10);
		selectionPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px; -fx-background-insets: 5px");

		//---------------------------------------------------------------------------------------------------

		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED)
			{
				Document doc = webView.getEngine().getDocument();
				Element styleNode = doc.createElement("style");
				Text styleContent = doc.createTextNode(CSS_START + "#" + Integer.toHexString(colorPicker.getValue().hashCode()) + CSS_END);
				styleNode.appendChild(styleContent);
				doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);

				//nodes.clear();
				NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName(tagField.getText());
				ObservableList<String> obsList = FXCollections.observableArrayList();
				//filteredList = new FilteredList<>(obsList);

				for (int i = 0; i < nodeList.getLength(); i++)
				{
					String xPath = NodeUtilities.getXPath(nodeList.item(i));
					//list.getItems().add(nodeList.item(i).getTextContent() + " || " + xPath);
					obsList.add(nodeList.item(i).getTextContent() + " || " + xPath);
					//nodes.put(xPath, nodeList.item(i));

					addOnclick(nodeList.item(i), evt ->
					{
						if (selectButton.isSelected())
						{
							if (selectedNode != null) ((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass);

							selectedNode = (Node)evt.getTarget();
							selectedNodePreviousClass = ((HTMLElement)selectedNode).getClassName();
							((HTMLElement)selectedNode).setClassName(selectedNodePreviousClass + " selectedNode");

							idField.setText(NodeUtilities.getXPath((Node)evt.getTarget()));
							evt.preventDefault();
							evt.stopPropagation();
						}
					});
				}

				//list.setItems(filteredList);
			}
			else if (newValue == Worker.State.READY)
			{
				//filteredList.getSource().clear();
			}
		});

		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(vBox, selectionPane);
		Scene scene = new Scene(splitPane, 1500, 900);

		stage.setScene(scene);
		stage.show();
	}

	private static void addOnclick(Node node, EventListener listener)
	{
		((EventTarget)node).addEventListener("click", listener, true);
	}

	public static void main(String[] args)
	{
		launch();
	}
}
