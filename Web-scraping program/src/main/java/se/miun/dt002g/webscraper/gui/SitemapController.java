package se.miun.dt002g.webscraper.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import se.miun.dt002g.webscraper.database.MongoDbHandler;
import se.miun.dt002g.webscraper.scraper.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main screen of the application.
 */
public class SitemapController extends GridPane
{
	Map<String, String> settings; // Program settings.
	List<TimerService> scheduledScrapes = new ArrayList<>(); // All currently scheduled scrapings.
	List<Sitemap> sitemaps; // All loaded sitemaps.
	String sitemapSourceDir; // The directory containing sitemaps.
	TextArea dataPreview;
	ListView<Sitemap> sitemapList;
	ListView<se.miun.dt002g.webscraper.scraper.Task> taskList;
	Label selectedSitemapLabel;
	VBox buttonVBox;
	VBox runButtonVbox;
	Label mainLabel;
	String defaultStorageLocation;
	Button addButton = new Button("New"),
			editButton = new Button("Edit"),
			deleteButton = new Button("Delete"),
			saveButton = new Button("Save");
	Button runButton = new Button("Run"),
			scheduleButton = new Button("Schedule");
	int NR_OF_DRIVERS;
	MongoDbHandler mongoDbHandler = new MongoDbHandler();
	ProgressStage progressStage = null; // Stage for the progress window.

	public SitemapController()
	{
		// Load settings.
		settings = new HashMap<>();
		if (!SettingsController.loadSettings(settings)) System.out.println("No settings.cfg was found.");
		NR_OF_DRIVERS = Integer.parseInt(settings.getOrDefault("threadAmount", "1"));
		setDriverSystemProperties();

		connectToDb();
		loadScheduledScrapes();

		defaultStorageLocation = settings.getOrDefault("storageLocation",System.getProperty("user.dir"));

		menuBar(); // Create menu bar.

		sitemapList = new ListView<>();
		mainLabel = new Label("Sitemaps");
		mainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		addButton.setMinWidth(50);
		editButton.setMinWidth(50);
		deleteButton.setMinWidth(50);
		saveButton.setMinWidth(50);

		addButton.setOnAction(event -> // Run the EnterBaseURLPopup and then show the sitemap editing window.
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

		editButton.setOnAction(event -> // Open the sitemap editing window.
		{
			Stage addStage = new Stage();
			addStage.setResizable(false);
			addStage.initModality(Modality.APPLICATION_MODAL);
			addStage.setTitle("Edit Sitemap");
			Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
			if(current.isPresent()) // If a sitemap is currently selected.
			{
				addStage.setScene(new Scene(new TaskController(current.get().getRootUrl(),current.get())));
				addStage.sizeToScene();
				addStage.showAndWait();
				updateFields();
			}
		});

		buttonVBox = new VBox(5, addButton, editButton, deleteButton, saveButton);

		runButton.setMinWidth(65);
		scheduleButton.setMinWidth(65);
		scheduleButton.setOnAction(event ->
		{
			if(sitemapList.getSelectionModel().getSelectedItem() == null) // If no sitemap is selected.
			{
				return;
			}

			// Open the scheduling window.
			ScheduleScraperPopup popup = new ScheduleScraperPopup(sitemapList.getSelectionModel().getSelectedItem().getName(),mongoDbHandler,settings);

			if(popup.isRunScraper()) // If the scraper should be scheduled.
			{
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
				ScrapeSettings scrapeSettings = popup.getScrapeSettings();
				scrapeSettings.localStorageLocation = defaultStorageLocation;
				scrapeSettings.NO_DRIVERS = NR_OF_DRIVERS;
				scrapeSettings.repetitions = 0;
				scrapeSettings.interval = java.time.Duration.ofSeconds(0);
				current.ifPresent(sitemap -> // If a sitemap is selected.
				{
					// Schedules the scraping.
					progressStage = new ProgressStage(current.get(), Math.min(current.get().getTasks().size(), NR_OF_DRIVERS));
					scheduleScraper(sitemap,scrapeSettings,mongoDbHandler, progressStage.getRunnable());
				});
			}
		});

		runButton.setOnAction(event ->
		{
			if(sitemapList.getSelectionModel().getSelectedItem() == null) // If no sitemap is selected.
			{
				return;
			}

			// Opens the run configuration window.
			RunScraperPopup popup = new RunScraperPopup(sitemapList.getSelectionModel().getSelectedItem().getName(),mongoDbHandler,settings);

			if(popup.isRunScraper()) // If the scraper should run.
			{
				Optional<Sitemap> current = sitemaps.stream().filter(s-> Objects.equals(s, sitemapList.getSelectionModel().getSelectedItem())).findAny();
				ScrapeSettings scrapeSettings = popup.getScrapeSettings();
				scrapeSettings.localStorageLocation = defaultStorageLocation;
				scrapeSettings.NO_DRIVERS = NR_OF_DRIVERS;
				scrapeSettings.repetitions = 0;
				scrapeSettings.firstStart = java.time.Duration.ofSeconds(0);
				scrapeSettings.interval = java.time.Duration.ofSeconds(0);
				scrapeSettings.startAt = LocalDateTime.now();

				current.ifPresent(sitemap -> // If a sitemap is selected.
				{
					// Create the progress window and start the scraper.
					progressStage = new ProgressStage(current.get(), Math.min(current.get().getTasks().size(), NR_OF_DRIVERS));
					scheduleScraper(sitemap,scrapeSettings,mongoDbHandler, progressStage.getRunnable());
				});
			}
		});
		runButtonVbox = new VBox(5, runButton, scheduleButton);

		taskList = new ListView<>();
		selectedSitemapLabel = new Label("Sitemap Branches");

		dataPreview = new TextArea();
		dataPreview.setPromptText("Data preview not available");
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

		// Update the fields when clicking on the sitemap list.
		sitemapList.getSelectionModel().selectedItemProperty().addListener(l-> updateFields());

		deleteButton.setOnAction(event -> // Remove a sitemap.
		{
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

	/**
	 * Saves the sitemaps to the current directory
	 * @return true if successful
	 */
	public boolean saveSitemaps(){
		sitemaps.forEach(Sitemap::clearData);
		sitemaps.forEach(s-> s.setRunning(false));
		return SitemapHandler.saveSitemaps(sitemapSourceDir,sitemaps);
	}

	/**
	 * Save settings to config file
	 */
	public void saveSettings()
	{
		try
		{
			SettingsController.saveSettings(settings);
		}
		catch (IOException ignored) {}
	}

	/**
	 * Updates buttons, textfields and Listviews
	 * depending on which sitemap is currently selected.
	 */
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

	/**
	 * Connect to MongoDb through settings string
	 */
	private void connectToDb(){
		String s = settings.get("dbConnection");
		if(s!=null){
			mongoDbHandler.tryConnect(s);
		}
	}

	/**
	 * Saves scheduled scrapes to a file.
	 */
	public void saveScheduledScrapes() {
		ObjectOutputStream oos;
		try {
			oos= new ObjectOutputStream(new FileOutputStream(System.getProperty("user.dir")+"/scheduledSitemaps.ssm"));
			scheduledScrapes.forEach(scheduled -> {
				try {
					oos.writeObject(Pair.of(scheduled.sitemap,scheduled.settings)); // Write a pair of a sitemap object and a its corresponding scrape settings
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Loads Pair of Sitemap objects and ScrapeSettings from the file "scheduledSitemaps.ssm"
	 * and re-schedules them.
	 */
	public void loadScheduledScrapes(){
		ObjectInputStream ois;
		try {
			File file = new File("scheduledSitemaps.ssm");
			if (!file.exists()){
				System.out.println("File didnt exist");
				return;
			}
			ois = new ObjectInputStream(new FileInputStream(file));
			while(true){
				Pair<Sitemap,ScrapeSettings> pair = (Pair<Sitemap,ScrapeSettings>) ois.readObject(); // read serialized object from file
				pair.second.NO_DRIVERS = NR_OF_DRIVERS;
				pair.second.localStorageLocation = defaultStorageLocation;
				progressStage = new ProgressStage(pair.first, Math.min(pair.first.getTasks().size(), NR_OF_DRIVERS)); // add progressStage
				scheduleScraper(pair.first,pair.second,mongoDbHandler, progressStage.getRunnable()); // re-schedule the Sitemap for execution
			}
		} catch (IOException | ClassNotFoundException ignored) {
		}
	}

	/**
	 * Creates the menu bar at the top of the scene
	 */
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
			defaultStorageLocation = settings.getOrDefault("storageLocation",System.getProperty("user.dir"));
		});
		MenuItem scheduledScraper = new MenuItem("Scheduled Scrapes");
		scheduledScraper.setOnAction(event -> {
			ScheduledScrapersController s = new ScheduledScrapersController(scheduledScrapes);

		});
		MenuItem helpButton = new MenuItem("Show Manual");
		helpButton.setOnAction(event ->
		{
			Stage helpStage = new Stage();
			WebView helpWebView = new WebView();
			try
			{
				helpWebView.getEngine().load(new File("help/help.html").toURI().toString());
				helpStage.setScene(new Scene(helpWebView));
				helpStage.sizeToScene();
				helpStage.setTitle("Help");
				helpStage.show();
			}
			catch (NullPointerException ignored) { }
		});
		MenuItem quitApp=new MenuItem("Quit App");
		quitApp.setStyle("-fx-font-weight: bold; -fx-font-size: 12px");
		quitApp.setOnAction(event -> { // close application
			Stage stage = (Stage) getScene().getWindow();
			stage.close();
		});
		//adding menu items to the menu
		menu.getItems().add(settingsButton);
		menu.getItems().add(scheduledScraper);
		menu.getItems().add(helpButton);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(quitApp);
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
	 * @param update, the Runnable which will update the progressbar
	 */
	private void scheduleScraper(Sitemap sitemap,ScrapeSettings settings,MongoDbHandler mongoDbHandler, Runnable update){
		// Calculate start time
		java.time.Duration startIn = java.time.Duration.between(LocalDateTime.now(),settings.startAt);
		if(settings.startAt.isBefore(LocalDateTime.now())){
			startIn = java.time.Duration.ofSeconds(0);
		}
		settings.webDriverName = this.settings.get("driver");
		TimerService service = new TimerService(sitemap,settings,mongoDbHandler, update); // create new Timer-object
		AtomicInteger count = new AtomicInteger(0);
		service.setCount(count.get());
		service.setDelay(Duration.seconds(startIn.toSeconds())); // set start time of first scrape
		service.setPeriod(Duration.seconds(settings.interval.toSeconds())); // set the interval between scrapers
		service.setOnSucceeded(t -> { // when a scrape has succeeded
			sitemap.setRunning(false);
			if(settings.repetitions <= (Integer)t.getSource().getValue()){ // cancel new scrape if it has reached max repetitions
				t.getSource().cancel();
				scheduledScrapes.removeIf(timerService -> timerService.equals(service)); // remove if previously scheduled
			}
			count.set((int) t.getSource().getValue());
			updateSitemapListView();
			updateFields();

			if (progressStage != null)
			{
				List<java.time.Duration> times = sitemap.getTimes().stream().map(AtomicReference::get).toList(); // fetch scraping times
				java.time.Duration totalTime = java.time.Duration.ZERO;
				for (java.time.Duration d : times)
				{
					totalTime = totalTime.plus(d);
				}

				Timer closeTimer = new Timer();
				java.time.Duration finalTotalTime = totalTime;
				closeTimer.schedule(new TimerTask() // TimerTask for closing the progressbar and showing the popup with statistics
				{
					@Override
					public void run()
					{
						Platform.runLater(() ->
						{
							progressStage.close();
							progressStage = null;
							closeTimer.cancel();

							StatsPopup stats = new StatsPopup(sitemap.getTotalBytes(), finalTotalTime, sitemap.getTasks().stream().map(Object::toString).toList(), sitemap.getBytesPerTask(), times);
						});
					}
				}, 1000);
			}
		});

		service.setOnRunning(t-> {
			for(Sitemap sm :sitemaps){
				if(sm.isRunning()){
					t.getSource().cancel();
					return;
				}
			}
			sitemap.setRunning(true);
			progressStage.show();
		}); // show progressbar when the scraping begins

		service.setOnFailed(t->{ // show alert-box if something went wrong during execution
			scheduledScrapes.removeIf(timerService -> timerService.equals(service));
			sitemap.setRunning(false);
			if (progressStage != null){
				progressStage.close();
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Error");
				alert.setHeaderText("Something went wrong while scraping.");
				alert.showAndWait();
			}
		});
		service.setRestartOnFailure(false);

		service.start();
		if(startIn==java.time.Duration.ofSeconds(0)){ // dont add scrapes that start immediately to the scheduled list
			return;
		}
		scheduledScrapes.add(service);
	}

}
