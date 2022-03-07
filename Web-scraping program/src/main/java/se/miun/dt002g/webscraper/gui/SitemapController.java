package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.Sitemap;
import se.miun.dt002g.webscraper.scraper.SitemapHandler;

import java.util.ArrayList;
import java.util.List;

public class SitemapController extends GridPane
{
	List<Sitemap> sitemaps;
	String sitemapSourceDir;

	public SitemapController()
	{
		ListView<String> sitemapList = new ListView<>();
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
			String baseURL = null,sitemapName=null;
			EnterBaseURLPopup popup = new EnterBaseURLPopup();
			baseURL = popup.getBaseURL();
			sitemapName = popup.getSitemapName();
			sitemaps.add(new Sitemap(baseURL,sitemapName));
			sitemapList.setItems(FXCollections.observableArrayList(sitemaps.stream().map(Sitemap::getName).toList()));
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

		sitemapSourceDir=System.getProperty("user.dir")+"/src/main/resources/";
		sitemaps = SitemapHandler.loadSitemaps(sitemapSourceDir,new ArrayList<>());
		System.out.println(sitemaps.size());
		sitemapList.setItems(FXCollections.observableArrayList(sitemaps.stream().map(Sitemap::getName).toList()));
	}

	public boolean saveSitemaps(){
		return SitemapHandler.saveSitemaps(sitemapSourceDir,sitemaps);
	}
}
