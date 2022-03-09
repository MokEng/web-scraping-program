package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SitemapController extends GridPane
{

	List<Sitemap> sitemaps;
	String sitemapSourceDir;
	String currentSelectedSitemap;
	TextArea dataPreview;
	ListView<String> sitemapList;
	ListView<String> taskList;
	Label selectedSitemapLabel;
	Label dataStorageLabel;
	TextField chosenLocation;
	VBox buttonVBox;
	VBox runButtonVbox;
	VBox dataStorageButtonVbox;
	VBox dataStorageConfigVbox;
	Label mainLabel;
	Button okDataStorageButton, openDirectoryChooser;
	String defaultStorageLocation;

	public SitemapController()
	{
		defaultStorageLocation = System.getProperty("user.dir");
		menuBar();
		sitemapList = new ListView<>();
		mainLabel = new Label("Sitemaps");
		mainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		okDataStorageButton = new Button("Ok");
		okDataStorageButton.setMinWidth(60);
		okDataStorageButton.setOnAction(event -> {
			showStorage(false);
		});
		dataStorageButtonVbox = new VBox(5,okDataStorageButton);
		dataStorageButtonVbox.setVisible(false);
		add(dataStorageButtonVbox,3,2);


		Button addButton = new Button("New"),
				editButton = new Button("Edit"),
				deleteButton = new Button("Delete"),
				saveButton = new Button("Save");

		addButton.setMinWidth(50);
		editButton.setMinWidth(50);
		deleteButton.setMinWidth(50);
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
		

		buttonVBox = new VBox(5, addButton, editButton, deleteButton, saveButton);

		Button runButton = new Button("Run"),
				scheduleButton = new Button("Schedule");
		runButton.setMinWidth(65);
		scheduleButton.setMinWidth(65);

		runButton.setOnAction(event -> {
			if(currentSelectedSitemap == null){
				return;
			}
			RunScraperPopup runScraperPopup = new RunScraperPopup();
			if(runScraperPopup.isRunScraper()){
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
				current.ifPresent(sitemap -> runScraper(sitemap, 2,runScraperPopup.isSaveOnDevice(), runScraperPopup.isSaveOnDatabase(),runScraperPopup.getLocalDataFormat()));
			}
		});
		runButtonVbox = new VBox(5, runButton, scheduleButton);

		taskList = new ListView<>();
		selectedSitemapLabel = new Label("Selected Sitemap");

		dataPreview = new TextArea();
		dataPreview.setPromptText("No data scraped for this task.");
		selectedSitemapLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");

		setVgap(10);
		setHgap(5);
		setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");

		add(mainLabel, 0, 2, 2, 1);
		add(buttonVBox, 0, 3);
		add(sitemapList, 1, 3);
		add(selectedSitemapLabel, 2, 2, 2, 1);
		add(taskList, 2, 3);
		add(runButtonVbox, 3, 3);
		add(dataPreview,1,5);
		// STORAGE BUTTON
		dataStorageLabel = new Label("Local storage location");
		dataStorageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px");
		openDirectoryChooser = new Button("Open");
		openDirectoryChooser.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(defaultStorageLocation));
			try{
				defaultStorageLocation = directoryChooser.showDialog(new Stage()).getAbsolutePath();
				chosenLocation.setText(defaultStorageLocation);
				System.out.println(defaultStorageLocation);
			}catch(Exception e){
				System.out.println(defaultStorageLocation);
			}

		});

		chosenLocation = new TextField(defaultStorageLocation);
		chosenLocation.setDisable(true);
		chosenLocation.setMinWidth(60);

		dataStorageConfigVbox = new VBox(10, dataStorageLabel, chosenLocation,openDirectoryChooser);
		dataStorageConfigVbox.setVisible(false);
		add(dataStorageConfigVbox,2,2,1,1);

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

	public void runScraper(Sitemap sitemap,int nrOfDrivers,boolean saveLocal,boolean saveDb, DATA_FORMAT dataFormat){
		javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
			@Override
			protected Void call() throws Exception {
				//updateMessage("");
				//updateProgress("iterations", "totalIterations");
				sitemap.clearDataFromTasks();
				sitemap.runMultiThreadedScraper(nrOfDrivers);
				if(saveLocal){
					if(dataFormat == DATA_FORMAT.json){
						DataHandler.toJSONFile(GROUPBY.id,sitemap,defaultStorageLocation+"/"+sitemap.getName()+".json");
					}else if(dataFormat == DATA_FORMAT.csv){
						DataHandler.toCSVFile(GROUPBY.id,sitemap,defaultStorageLocation+"/"+sitemap.getName()+".csv");
					}
				}

				System.out.println(DataHandler.toJSON(GROUPBY.id,sitemap));
				return null;
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

	public void menuBar(){
		//creating menu bar
		MenuBar menuBar=new MenuBar();
		//creating menu for adding menu items
		Menu menu=new Menu("File");
		//creating menu items
		MenuItem configureDataStorage=new MenuItem("Configure data storage");
		configureDataStorage.setOnAction(event -> {
			showStorage(true);
		});
		MenuItem sitemapFromFile=new MenuItem("Load sitemap from file");
		MenuItem quitWebScraperApp=new MenuItem("Quit Web Scraper App");
		quitWebScraperApp.setStyle("-fx-font-weight: bold; -fx-font-size: 12px");
		//adding menu items to the menu
		menu.getItems().add(configureDataStorage);
		menu.getItems().add(sitemapFromFile);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(quitWebScraperApp);
		//adding menu to the menu bar
		menuBar.getMenus().add(menu);
		//creating VBox for adding all menu bar
		VBox vBox=new VBox(menuBar);
		add(vBox,0,0,4,2);
	}
	
	public void showStorage(boolean showStorage){

		buttonVBox.setVisible(!showStorage);
		runButtonVbox.setVisible(!showStorage);
		sitemapList.setVisible(!showStorage);
		taskList.setVisible(!showStorage);
		dataPreview.setVisible(!showStorage);
		selectedSitemapLabel.setVisible(!showStorage);
		dataStorageConfigVbox.setVisible(showStorage);
		dataStorageButtonVbox.setVisible(showStorage);
		if(showStorage){
			mainLabel.setText("Configure Storage");
			return;
		}
		mainLabel.setText("Sitemaps");
	}
	
}
