package se.miun.dt002g.webscraper.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.w3c.dom.html.HTMLElement;

import java.util.*;


public class GUITest extends Application
{
	// http://tutorials.jenkov.com/javafx/css-styling.html
	// https://stackoverflow.com/questions/60081353/running-javafx-project-in-intellij-with-gradle

	private static final String CSS =
			".selectedNode" +
			"{" +
				"background-color: green !important;" +
				"color: cyan !important;" +
			"}";

	private String selectedNodeKey = null;
	private String selectedNodePrevClass = null;
	private Map<String, Node> nodes;
	private FilteredList<String> filteredList = null;

	@Override
	public void start(Stage stage)
	{
		nodes = new HashMap<>();

		String javaVersion = System.getProperty("java.version");
		String javafxVersion = System.getProperty("javafx.version");
		stage.setTitle("GUITest running on Java " + javaVersion + ", JavaFX " + javafxVersion);

		Label urlLabel = new Label("URL ");
		TextField urlField = new TextField("http://google.se");
		Button urlButton = new Button("Load URL");
		Label tagLabel = new Label("HTML Tag ");
		TextField tagField = new TextField("a");

		WebView webView = new WebView();

		urlButton.setOnAction(action -> webView.getEngine().load(urlField.getText()));

		HBox hBox = new HBox(5, urlLabel, urlField, urlButton, tagLabel, tagField);
		HBox.setMargin(urlLabel, new Insets(5, 0, 0, 5));
		HBox.setMargin(urlButton, new Insets(0, 5, 0, 0));
		HBox.setMargin(tagLabel, new Insets(5, 0, 0, 0));

		VBox vBox = new VBox(5, hBox, webView);
		VBox.setVgrow(webView, Priority.SOMETIMES);
		VBox.setMargin(hBox, new Insets(5, 0, 0, 0));

		ListView<String> list = new ListView<>();
		list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (selectedNodeKey != null)
			{
				Node n = nodes.get(selectedNodeKey);
				((HTMLElement)n).setClassName(selectedNodePrevClass);
			}

			if (newValue != null)
			{
				selectedNodeKey = newValue.substring(newValue.indexOf(" || ")+4);
				HTMLElement element = (HTMLElement)nodes.get(selectedNodeKey);
				selectedNodePrevClass = element.getClassName();
				element.setClassName(selectedNodePrevClass + " selectedNode");
			}
		});

		Label filterLabel = new Label("Filter ");
		TextField filterField = new TextField();
		HBox filterHBox = new HBox(5, filterLabel, filterField);
		HBox.setMargin(filterLabel, new Insets(10, 0, 0, 5));
		HBox.setMargin(filterField, new Insets(5, 0, 0, 0));
		filterField.textProperty().addListener((observable, oldValue, newValue) ->
		{
			String f = filterField.getText();

			if (f == null || f.length() == 0) filteredList.setPredicate(s -> true);
			else filteredList.setPredicate(s -> s.contains(f));
		});

		VBox listVBox = new VBox(5, filterHBox, list);
		VBox.setVgrow(list, Priority.ALWAYS);

		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED)
			{
				Document doc = webView.getEngine().getDocument();
				Element styleNode = doc.createElement("style");
				Text styleContent = doc.createTextNode(CSS);
				styleNode.appendChild(styleContent);
				doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);

				nodes.clear();
				NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName(tagField.getText());
				ObservableList<String> obsList = FXCollections.observableArrayList();
				filteredList = new FilteredList<>(obsList);

				for (int i = 0; i < nodeList.getLength(); i++)
				{
					String xPath = getXPath(nodeList.item(i));
					//list.getItems().add(nodeList.item(i).getTextContent() + " || " + xPath);
					obsList.add(nodeList.item(i).getTextContent() + " || " + xPath);
					nodes.put(xPath, nodeList.item(i));
				}

				list.setItems(filteredList);
			}
			else if (newValue == Worker.State.READY)
			{
				//list.getItems().clear();
				filteredList.getSource().clear();
			}
		});

		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(vBox, listVBox);
		Scene scene = new Scene(splitPane, 1500, 900);

		stage.setScene(scene);
		stage.show();
	}

	private static String getXPath(Node node)
	{
		StringBuilder builder = new StringBuilder();
		Stack<String> stack = new Stack<>();

		Node currNode = node;
		while (currNode.getParentNode() != null)
		{
			List<Node> nodeList = filterNodes(currNode.getParentNode().getChildNodes(), currNode.getNodeName());
			if (nodeList.size() > 1)
			{
				for (int i = 0; i < nodeList.size(); i++)
				{
					if (currNode.isSameNode(nodeList.get(i)))
					{
						stack.push(currNode.getNodeName().toLowerCase() + "[" + (i+1) + "]");
						break;
					}
				}
			}
			else stack.push(currNode.getNodeName().toLowerCase());

			currNode = currNode.getParentNode();
		}

		while (!stack.isEmpty())
		{
			builder.append("/").append(stack.pop());
		}

		return builder.toString();
	}

	private static List<Node> filterNodes(NodeList list, String name)
	{
		List<Node> newList = new ArrayList<>();

		for (int i = 0; i < list.getLength(); i++)
		{
			if (list.item(i).getNodeName().equals(name)) newList.add(list.item(i));
		}

		return newList;
	}

	public static void main(String[] args)
	{
		launch();
	}
}
