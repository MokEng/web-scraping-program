package se.miun.dt002g.webscraper.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import se.miun.dt002g.webscraper.scraper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SitemapController extends GridPane
{

	List<Sitemap> sitemaps;
	List<Sitemap> scrapedSitemaps=new ArrayList<>();
	String sitemapSourceDir;
	String currentSelectedSitemap;
	TextArea dataPreview;
	ListView<String> sitemapList;
	ListView<String> taskList;
	public SitemapController()
	{
		sitemapList = new ListView<>();
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
			System.out.println(baseURL);

			if (baseURL != null && sitemapName!= null)
			{
				Sitemap newSitemap = new Sitemap(baseURL,sitemapName);
				sitemaps.add(newSitemap);
				sitemapList.setItems(FXCollections.observableArrayList(sitemaps.stream().map(Sitemap::getName).toList()));
				Stage addStage = new Stage();
				addStage.setResizable(false);
				addStage.initModality(Modality.APPLICATION_MODAL);
				addStage.setTitle("Create New Sitemap");
				addStage.setScene(new Scene(new TaskController(baseURL,newSitemap)));
				addStage.sizeToScene();

				addStage.showAndWait();
			}
		});

		editButton.setOnAction(event -> {
			Stage addStage = new Stage();
			addStage.setResizable(false);
			addStage.initModality(Modality.APPLICATION_MODAL);
			addStage.setTitle("Create New Sitemap");
			Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
			if(current.isPresent()){
				addStage.setScene(new Scene(new TaskController(current.get().getRootUrl(),current.get())));
				addStage.sizeToScene();
				addStage.showAndWait();
				updateFields();
			}
		});

		VBox buttonVBox = new VBox(5, addButton, editButton, deleteButton, saveButton, loadButton);


		Button runButton = new Button("Run"),
				scheduleButton = new Button("Schedule");
		runButton.setMinWidth(65);
		scheduleButton.setMinWidth(65);

		runButton.setOnAction(event -> {
			Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
			current.ifPresent(sitemap -> runScraper(sitemap, 1));
		});
		VBox runButtonVbox = new VBox(5, runButton, scheduleButton);

		taskList = new ListView<>();
		Label selectedSitemapLabel = new Label("Selected Sitemap");

		dataPreview = new TextArea();
		dataPreview.setPromptText("No data scraped for this task.");

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
		add(dataPreview,1,3);

		sitemapList.getSelectionModel().selectedItemProperty().addListener(l-> updateFields());
		deleteButton.setOnAction(event -> {
			SitemapHandler.removeSitemapFile(sitemapSourceDir,currentSelectedSitemap);
			sitemaps.removeIf(s-> Objects.equals(s.getName(),currentSelectedSitemap));
			sitemapList.setItems(FXCollections.observableArrayList(sitemaps.stream().map(Sitemap::getName).toList()));
			dataPreview.setText("");
			taskList.setItems(FXCollections.observableArrayList(new ArrayList<>()));
		});


		sitemapSourceDir=System.getProperty("user.dir")+"/src/main/resources/";
		sitemaps = SitemapHandler.loadSitemaps(sitemapSourceDir,new ArrayList<>());
		sitemapList.setItems(FXCollections.observableArrayList(sitemaps.stream().map(Sitemap::getName).toList()));
	}

	public boolean saveSitemaps(){
		sitemaps.forEach(Sitemap::clearDataFromTasks);
		return SitemapHandler.saveSitemaps(sitemapSourceDir,sitemaps);
	}

	public void runScraper(Sitemap sitemap,int nrOfDrivers){
		javafx.concurrent.Task<Integer> task = new javafx.concurrent.Task<>() {
			@Override
			protected Integer call() throws Exception {
				//updateMessage("");
				//updateProgress("iterations", "totalIterations");
				sitemap.runMultiThreadedScraper(nrOfDrivers);
				System.out.println(DataHandler.toJSON(GROUPBY.id,sitemap));
				return 1;
			}
		};
		//stateProperty for Task:
		task.stateProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue==Worker.State.SUCCEEDED){
				System.out.println("Now the scraper has finished.");
				updateFields();
			}
		});

		//start Task
		new Thread(task).start();
	}

	public void updateFields(){
		dataPreview.setText("");
		currentSelectedSitemap = sitemapList.getSelectionModel().getSelectedItem();
		Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
		current.ifPresent(sitemap -> taskList.setItems(FXCollections.observableArrayList(sitemap.getTasks().stream().map(Object::toString).toList())));
		current.ifPresent(sitemap -> {

			Optional<String> jsonString = DataHandler.toJSONList(GROUPBY.id, sitemap).stream().reduce((s, s2) -> s + ",\n" + s2);
			jsonString.ifPresent(s -> dataPreview.setText(s));

		});
	}
}
