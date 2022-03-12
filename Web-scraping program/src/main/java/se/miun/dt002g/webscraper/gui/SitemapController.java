package se.miun.dt002g.webscraper.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import se.miun.dt002g.webscraper.database.MongoDbHandler;
import se.miun.dt002g.webscraper.scraper.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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
	String defaultStorageLocation;
	Button addButton = new Button("New"),
			editButton = new Button("Edit"),
			deleteButton = new Button("Delete"),
			saveButton = new Button("Save");
	Button runButton = new Button("Run"),
			scheduleButton = new Button("Schedule");
	int NR_OF_DRIVERS=1;
	MongoDbHandler mongoDbHandler = new MongoDbHandler();


	public SitemapController()
	{
		//NR_OF_DRIVERS = Runtime.getRuntime().availableProcessors();
		settings = new HashMap<>();
		setDriverSystemProperties();

		defaultStorageLocation = System.getProperty("user.dir");
		menuBar();
		sitemapList = new ListView<>();
		mainLabel = new Label("Sitemaps");
		mainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

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
			RunScraperPopup popup = new RunScraperPopup(currentSelectedSitemap,mongoDbHandler,settings);
			if(popup.isRunScraper()){
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s.getName(), currentSelectedSitemap)).findAny();
				ScrapeSettings scrapeSettings = popup.getScrapeSettings();
				scrapeSettings.localStorageLocation = defaultStorageLocation;
				scrapeSettings.NO_DRIVERS = NR_OF_DRIVERS;
				scrapeSettings.repetitions = 0;
				scrapeSettings.firstStart = Duration.seconds(0);
				scrapeSettings.interval = Duration.seconds(0);
				current.ifPresent(sitemap -> scheduleScraper(sitemap,scrapeSettings,mongoDbHandler));
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

	/**
	 * Sets the system properties needed to use the drivers in the scraper.
	 * A driver can only be selected if it is present in the /drivers folder,
	 * so even if we set the system property for a driver we don't have on the system, it shouldn't lead to any problems.
	 */
	private void setDriverSystemProperties()
	{
		File driverFolder = new File("drivers");
		if (!driverFolder.exists()) driverFolder.mkdir();
		String driverPath = driverFolder.getAbsolutePath() + File.separator;
		String fileExtension = (System.getProperty("os.name").toLowerCase().contains("windows")) ? ".exe" : "";
		System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver" + fileExtension);
		System.setProperty("webdriver.gecko.driver", driverPath + "geckodriver" + fileExtension);
		System.setProperty("webdriver.edge.driver", driverPath + "msedgedriver" + fileExtension);
		System.setProperty("webdriver.ie.driver", driverPath + "IEDriverServer" + fileExtension);
	}

	public boolean saveSitemaps(){
		sitemaps.forEach(Sitemap::clearDataFromTasks);
		return SitemapHandler.saveSitemaps(sitemapSourceDir,sitemaps);
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
			SettingsController settingsController = new SettingsController(settings,mongoDbHandler);
			settings = settingsController.getSettings();
			NR_OF_DRIVERS = Integer.parseInt(String.valueOf(settings.get("threadAmount")));
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

	/**
	 * Schedules a sitemap to be scraped.
	 * @param sitemap, the sitemap to be scraped
	 * @param settings, the settings for the scrape
	 * @param mongoDbHandler, for writing to mongodb
	 */
	private void scheduleScraper(Sitemap sitemap,ScrapeSettings settings,MongoDbHandler mongoDbHandler){
		TimerService service = new TimerService(sitemap,settings,mongoDbHandler); // create new Timer-object
		AtomicInteger count = new AtomicInteger(0);
		service.setCount(count.get());
		service.setDelay(Duration.seconds(settings.firstStart.toSeconds())); // set start time of first scrape
		service.setPeriod(Duration.seconds(settings.interval.toSeconds())); // set the interval between scrapers
		service.setOnSucceeded(t -> { // when a scrape has succeeded
			System.out.println("Called : " + t.getSource().getValue()
					+ " time(s)");
			if(settings.repetitions <= (Integer)t.getSource().getValue()){ // cancel new scrape if it has reached max repetitions
				t.getSource().cancel();
			}
			count.set((int) t.getSource().getValue());
			sitemap.setName(sitemap.getName().substring(0,sitemap.getName().indexOf("[")-1));
			updateSitemapListView();
			updateFields();
		});
		service.setOnScheduled(t->{ // when a scrape is scheduled
			if(sitemap.isRunning()){ // if a sitemap is already running, cancel this scheduled task
				t.getSource().cancel();
			}
		});
		service.setOnRunning(t->{ // when a scrape is running
			sitemap.setName(sitemap.getName()+" [Running]");
			updateSitemapListView();
			updateFields();
		});
		service.start();
	}

	private static class TimerService extends ScheduledService<Integer> {
		Sitemap sitemap;
		ScrapeSettings settings;
		MongoDbHandler mongoDbHandler;
		TimerService(Sitemap sitemap,ScrapeSettings settings,MongoDbHandler mongoDbHandler){
			this.sitemap = sitemap;
			this.settings = settings;
			this.mongoDbHandler = mongoDbHandler;
		}
		private IntegerProperty count = new SimpleIntegerProperty();

		public final void setCount(Integer value) {
			count.set(value);
		}
		public final Integer getCount() {
			return count.get();
		}
		public final IntegerProperty countProperty() {
			return count;
		}
		protected Task<Integer> createTask() {
			return new Task<>() {
				protected Integer call() {
					sitemap.clearDataFromTasks();
					try {
						sitemap.runMultiThreadedScraper(settings.NO_DRIVERS);
					} catch (ExecutionException | InterruptedException e) {
						e.printStackTrace();
					}
					if (settings.saveLocal) {
						if (settings.dataFormat == DATA_FORMAT.json) {
							DataHandler.toJSONFile(settings.groupby, sitemap, settings.localStorageLocation + "/" + sitemap.getName().substring(0, sitemap.getName().indexOf("[") - 1) + ".json");
						} else if (settings.dataFormat == DATA_FORMAT.csv) {
							DataHandler.toCSVFile(settings.groupby, sitemap, settings.localStorageLocation + "/" + sitemap.getName().substring(0, sitemap.getName().indexOf("[") - 1) + ".csv");
						}
					}
					if (settings.saveDb) {
						mongoDbHandler.storeJsonInDb(settings.dbStorageSettings, DataHandler.toJSONList(settings.groupby, sitemap));
					}
					//Adds 1 to the count
					count.set(getCount() + 1);
					return getCount();
				}
			};
		}
	}
}
