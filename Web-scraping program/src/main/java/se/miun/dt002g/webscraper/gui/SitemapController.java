package se.miun.dt002g.webscraper.gui;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import se.miun.dt002g.webscraper.database.MongoDbHandler;
import se.miun.dt002g.webscraper.scraper.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class SitemapController extends GridPane
{
	Map<String, String> settings;
	List<TimerService> scheduledScrapes=new ArrayList<>();
	List<Sitemap> sitemaps;
	String sitemapSourceDir;
	//String currentSelectedSitemap;
	TextArea dataPreview;
	ListView<Sitemap> sitemapList;
	ListView<se.miun.dt002g.webscraper.scraper.Task> taskList;
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
	Stage progressStage = null;

	public SitemapController()
	{
		//NR_OF_DRIVERS = Runtime.getRuntime().availableProcessors();
		System.out.println(System.getProperty("os.name"));
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
				sitemapList.setItems(FXCollections.observableArrayList(sitemaps));
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
			addStage.setTitle("Edit Sitemap");
			Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
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
		scheduleButton.setOnAction(event ->{
			if(sitemapList.getSelectionModel().getSelectedItem() == null){
				return;
			}
			ScheduleScraperPopup popup = new ScheduleScraperPopup(sitemapList.getSelectionModel().getSelectedItem().getName(),mongoDbHandler,settings);
			if(popup.isRunScraper()){
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
				ScrapeSettings scrapeSettings = popup.getScrapeSettings();
				scrapeSettings.localStorageLocation = defaultStorageLocation;
				scrapeSettings.NO_DRIVERS = NR_OF_DRIVERS;
				scrapeSettings.repetitions = 0;
				scrapeSettings.interval = java.time.Duration.ofSeconds(0);
				current.ifPresent(sitemap -> scheduleScraper(sitemap,scrapeSettings,mongoDbHandler, null));
			}
		});

		runButton.setOnAction(event -> {
			if(sitemapList.getSelectionModel().getSelectedItem() == null){
				return;
			}
			RunScraperPopup popup = new RunScraperPopup(sitemapList.getSelectionModel().getSelectedItem().getName(),mongoDbHandler,settings);
			if(popup.isRunScraper()){
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
				ScrapeSettings scrapeSettings = popup.getScrapeSettings();
				scrapeSettings.localStorageLocation = defaultStorageLocation;
				scrapeSettings.NO_DRIVERS = NR_OF_DRIVERS;
				scrapeSettings.repetitions = 0;
				scrapeSettings.firstStart = java.time.Duration.ofSeconds(0);
				scrapeSettings.interval = java.time.Duration.ofSeconds(0);
				scrapeSettings.startAt = LocalDateTime.now();

				progressStage = new Stage();
				ProgressBar progressBar = new ProgressBar(0.0);
				int nTasks = current.map(value -> value.getTasks().size()).orElse(-1);
				Text progressText = new Text("Tasks done 0/"+nTasks);
				progressText.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
				HBox progessHBox = new HBox(5, progressText, progressBar);
				progessHBox.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
				progressStage.setScene(new Scene(progessHBox));
				AtomicInteger finishedTasks = new AtomicInteger(0);
				progressStage.sizeToScene();
				progressStage.setResizable(false);
				progressStage.initModality(Modality.APPLICATION_MODAL);
				progressStage.setTitle("Executing Tasks...");

				current.ifPresent(sitemap -> scheduleScraper(sitemap,scrapeSettings,mongoDbHandler, () ->
				{
					progressBar.setProgress((double)finishedTasks.incrementAndGet()/(double)nTasks);
					progressText.setText("Tasks done "+finishedTasks.get() + "/" + nTasks);
				}));
				progressStage.show();
			}

		});
		runButtonVbox = new VBox(5, runButton, scheduleButton);

		taskList = new ListView<>();
		selectedSitemapLabel = new Label("Sitemap Branches");

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
			SitemapHandler.removeSitemapFile(sitemapSourceDir,sitemapList.getSelectionModel().getSelectedItem().getName());
			sitemaps.removeIf(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem()));
			sitemapList.setItems(FXCollections.observableArrayList(sitemaps));
			dataPreview.setText("");
			taskList.setItems(FXCollections.observableArrayList(new ArrayList<>()));
		});

		sitemapSourceDir=System.getProperty("user.dir")+"/src/main/resources/";
		sitemaps = SitemapHandler.loadSitemaps(sitemapSourceDir,new ArrayList<>());
		sitemapList.setItems(FXCollections.observableArrayList(sitemaps));
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
		sitemaps.forEach(s-> s.setRunning(false));
		return SitemapHandler.saveSitemaps(sitemapSourceDir,sitemaps);
	}

	private void updateFields(){
		dataPreview.setText("");
		taskList.getItems().clear();
		editButton.setDisable(false);
		runButton.setDisable(false);
		Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
		current.ifPresent(sitemap -> {
			taskList.setItems(FXCollections.observableArrayList(sitemap.getTasks().stream().toList()));
			Optional<String> jsonString = DataHandler.toJSONList(GROUPBY.id, sitemap).stream().reduce((s, s2) -> s + ",\n" + s2);
			jsonString.ifPresent(s -> dataPreview.setText(s));
			editButton.setDisable(sitemap.isRunning());
			runButton.setDisable(sitemap.isRunning());
			deleteButton.setDisable(sitemap.isRunning());
			runButton.setDisable(sitemap.getTasks().isEmpty());
		});
	}

	private void updateSitemapListView(){
		sitemapList.setItems(FXCollections.observableArrayList(sitemaps));
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
		MenuItem scheduledScraper = new MenuItem("Scheduled Scrapes");
		scheduledScraper.setOnAction(event -> {
			ScheduledScrapersController s = new ScheduledScrapersController(scheduledScrapes);

		});
		MenuItem quitWebScraperApp=new MenuItem("Quit App");
		quitWebScraperApp.setStyle("-fx-font-weight: bold; -fx-font-size: 12px");
		//adding menu items to the menu
		menu.getItems().add(settingsButton);
		menu.getItems().add(sitemapFromFile);
		menu.getItems().add(scheduledScraper);
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
	private void scheduleScraper(Sitemap sitemap,ScrapeSettings settings,MongoDbHandler mongoDbHandler, Runnable update){

		java.time.Duration startIn = java.time.Duration.between(LocalDateTime.now(),settings.startAt);
		if(settings.startAt.isBefore(LocalDateTime.now())){
			startIn = java.time.Duration.ofSeconds(0);
		}
		TimerService service = new TimerService(sitemap,settings,mongoDbHandler, update); // create new Timer-object
		AtomicInteger count = new AtomicInteger(0);
		service.setCount(count.get());
		service.setDelay(Duration.seconds(startIn.toSeconds())); // set start time of first scrape
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
			for(int x = 0 ; x < scheduledScrapes.size();x++){
				scheduledScrapes.removeIf(timerService -> timerService.equals(service));
			}

			if (progressStage != null)
			{
				Timer closeTimer = new Timer();
				closeTimer.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						Platform.runLater(() ->
						{
							progressStage.close();
							progressStage = null;
							closeTimer.cancel();
						});
					}
				}, 1000);
			}
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
		if(startIn==java.time.Duration.ofSeconds(0)){
			return;
		}
		scheduledScrapes.add(service);
	}

	public static class TimerService extends ScheduledService<Integer> {
		Sitemap sitemap;
		ScrapeSettings settings;
		MongoDbHandler mongoDbHandler;
		Runnable update;
		TimerService(Sitemap sitemap,ScrapeSettings settings,MongoDbHandler mongoDbHandler, Runnable update){
			this.sitemap = sitemap;
			this.settings = settings;
			this.mongoDbHandler = mongoDbHandler;
			this.update = update;
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
						sitemap.runMultiThreadedScraper(settings.NO_DRIVERS, update);
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
		public String toString(){
			return new String("Sitemap:"+sitemap.getName()+
					"\nExecute at:"+settings.startAt.toString());
		}
	}
}
