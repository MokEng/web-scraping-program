package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.*;

import java.util.*;

public class SitemapController extends GridPane
{
	Map<String, String> settings;

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
	Button addButton = new Button("New"),
			editButton = new Button("Edit"),
			deleteButton = new Button("Delete"),
			saveButton = new Button("Save");
	Button runButton = new Button("Run"),
			scheduleButton = new Button("Schedule");
	int NR_OF_DRIVERS=2;
	public SitemapController()
	{
		//NR_OF_DRIVERS = Runtime.getRuntime().availableProcessors();
		settings = new HashMap<>();

		defaultStorageLocation = System.getProperty("user.dir");
		menuBar();
		sitemapList = new ListView<>();
		mainLabel = new Label("Sitemaps");
		mainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		okDataStorageButton = new Button("Ok");
		okDataStorageButton.setMinWidth(60);
		okDataStorageButton.setOnAction(event -> showStorage(false));
		dataStorageButtonVbox = new VBox(5,okDataStorageButton);
		dataStorageButtonVbox.setVisible(false);
		add(dataStorageButtonVbox,3,2);

		addButton.setMinWidth(50);
		editButton.setMinWidth(50);
		deleteButton.setMinWidth(50);
		saveButton.setMinWidth(50);

		addButton.setOnAction(event ->
		{
			String baseURL, sitemapName;
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

		runButton.setMinWidth(65);
		scheduleButton.setMinWidth(65);

		runButton.setOnAction(event -> {
			if(currentSelectedSitemap == null){
				return;
			}
			RunScraperPopup popup = new RunScraperPopup(currentSelectedSitemap);
			if(popup.isRunScraper()){
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
				current.ifPresent(sitemap -> runScraper(sitemap, 2,popup.isSaveOnDevice(), popup.isSaveOnDatabase(),popup.getLocalDataFormat(),popup.getGroupby()));
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

	private void runScraper(Sitemap sitemap,int nrOfDrivers,boolean saveLocal,boolean saveDb, DATA_FORMAT dataFormat, GROUPBY groupby){
		javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
			@Override
			protected Void call() throws Exception {
				//updateMessage("");
				//updateProgress("iterations", "totalIterations");
				//updateFields();
				sitemap.clearDataFromTasks();
				sitemap.runMultiThreadedScraper(nrOfDrivers);
				if(saveLocal){
					if(dataFormat == DATA_FORMAT.json){
						DataHandler.toJSONFile(groupby,sitemap,defaultStorageLocation+"/"+sitemap.getName().substring(0,sitemap.getName().indexOf("[")-1)+".json");
					}else if(dataFormat == DATA_FORMAT.csv){
						DataHandler.toCSVFile(groupby,sitemap,defaultStorageLocation+"/"+sitemap.getName().substring(0,sitemap.getName().indexOf("[")-1)+".csv");
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
				sitemap.setName(sitemap.getName().substring(0,sitemap.getName().indexOf("[")-1));
				updateSitemapListView();
				updateFields();
			}
			if(newValue == Worker.State.RUNNING){
				System.out.println("RUNNING");
				sitemap.setName(sitemap.getName()+" [Running]");

				updateSitemapListView();
				updateFields();
			}
		});

		//start Task
		new Thread(task).start();
	}

	private void updateFields(){
		dataPreview.setText("");
		taskList.getItems().clear();
		currentSelectedSitemap = sitemapList.getSelectionModel().getSelectedItem();
		editButton.setDisable(false);
		runButton.setDisable(false);
		Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
		current.ifPresent(sitemap -> {
			taskList.setItems(FXCollections.observableArrayList(sitemap.getTasks().stream().map(Object::toString).toList()));
			Optional<String> jsonString = DataHandler.toJSONList(GROUPBY.id, sitemap).stream().reduce((s, s2) -> s + ",\n" + s2);
			jsonString.ifPresent(s -> dataPreview.setText(s));
			editButton.setDisable(sitemap.isRunning());
			runButton.setDisable(sitemap.isRunning());
			deleteButton.setDisable(sitemap.isRunning());
			runButton.setDisable(sitemap.getTasks().isEmpty());
		});
	}

	private void updateSitemapListView(){
		sitemapList.setItems(FXCollections.observableArrayList(sitemaps.stream().map(Sitemap::getName).toList()));
	}

	private void menuBar(){
		//creating menu bar
		MenuBar menuBar=new MenuBar();
		//creating menu for adding menu items
		Menu menu=new Menu("File");
		//creating menu items
		MenuItem settingsButton =new MenuItem("Settings");
		settingsButton.setOnAction(event -> {
			//showStorage(true);
			SettingsController settingsController = new SettingsController(settings);
			settings = settingsController.getSettings();
		});
		MenuItem sitemapFromFile=new MenuItem("Load Sitemap");
		MenuItem quitWebScraperApp=new MenuItem("Quit App");
		quitWebScraperApp.setStyle("-fx-font-weight: bold; -fx-font-size: 12px");
		//adding menu items to the menu
		menu.getItems().add(settingsButton);
		menu.getItems().add(sitemapFromFile);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(quitWebScraperApp);
		//adding menu to the menu bar
		menuBar.getMenus().add(menu);
		//creating VBox for adding all menu bar
		VBox vBox=new VBox(menuBar);
		add(vBox,0,0,4,2);
	}
	
	private void showStorage(boolean showStorage){

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
