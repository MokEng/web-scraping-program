package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.database.MongoDbHandler;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;

public class SettingsController
{
	private final Map<String, String> webdriverNames = new HashMap<>() {{
		put("chromedriver", "Google Chrome");
		put("msedgedriver", "Microsoft Edge");
		put("IEDriverServer", "Internet Explorer");
		put("geckodriver", "Mozilla Firefox");
		put("safari", "Safari");
	}};

	private String defaultStorageLocation;
	private ObservableList<String> exesInDir = null;
	private final Map<String, String> settings;

	public SettingsController(Map<String, String> settingsMap, MongoDbHandler mongoDbHandler)
	{
		settings = settingsMap;
		defaultStorageLocation = settings.containsKey("storageLocation") ? settings.get("storageLocation") : System.getProperty("user.dir");

		Stage settingsStage = new Stage();
		settingsStage.initModality(Modality.APPLICATION_MODAL);
		settingsStage.setTitle("Settings");

		GridPane mainPane = new GridPane();
		mainPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
		mainPane.setVgap(10);
		mainPane.setHgap(5);

		Label settingsHeaderLabel = new Label("Settings");
		settingsHeaderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

		// ------------------------- Storage Location ----------------------------------------
		Label storageLocationLabel = new Label("Local Storage Location ");
		storageLocationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		TextField storageLocationField = new TextField(defaultStorageLocation);
		storageLocationField.setEditable(false);
		Tooltip storageLocationFieldTooltip = new Tooltip("The location on you computer where the scraped data will be saved. " +
				"Press the Open button to select a new location");
		storageLocationField.setTooltip(storageLocationFieldTooltip);
		storageLocationField.setMinWidth(400);
		Button selectDirectoryButton = new Button("Open");
		Tooltip selectDirectoryButtonTooltip = new Tooltip("Click to select a new storage location");
		selectDirectoryButton.setTooltip(selectDirectoryButtonTooltip);
		selectDirectoryButton.setOnAction(event ->
		{
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(defaultStorageLocation));

			try
			{
				defaultStorageLocation = directoryChooser.showDialog(new Stage()).getAbsolutePath();
				storageLocationField.setText(defaultStorageLocation);
				settings.put("storageLocation", defaultStorageLocation);
			}
			catch (Exception ignored) {}
		});

		// ------------------------- Database Config ----------------------------------------
		Label databaseLabel = new Label("MongoDb Connection String: ");
		databaseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		TextField connectionStringField = new TextField("");
		connectionStringField.setMinWidth(60);
		Tooltip connectionStringFieldTooltip = new Tooltip("Enter the connection string used to connect to your MongoDB database");
		connectionStringField.setTooltip(connectionStringFieldTooltip);
		Button tryConnectButton = new Button("Connect");
		Tooltip tryConnectButtonTooltip = new Tooltip("Press to try to connect to MongoDB using the connection string");
		tryConnectButton.setTooltip(tryConnectButtonTooltip);
		Label connectMessageLabel = new Label("");

		if(mongoDbHandler.isConnected()){
			connectMessageLabel.setText("Connected to database");
			connectMessageLabel.setStyle("-fx-background-color: lightgreen;");
			connectionStringField.setText(mongoDbHandler.getConnectionString());
		}else{
			connectMessageLabel.setText("Not connected to database");
			connectMessageLabel.setStyle("-fx-background-color: red;");
		}

		tryConnectButton.setOnAction(event -> {
			String text = connectionStringField.getText();
			if(mongoDbHandler.tryConnect(text)){
				connectMessageLabel.setText("Connected to database");
				connectMessageLabel.setStyle("-fx-background-color: lightgreen;");
			}else{
				connectMessageLabel.setText("Connection failed");
				connectMessageLabel.setStyle("-fx-background-color: red;");
			}
			settings.put("dbConnection",text);
		});


		// ------------------------- Driver selection ----------------------------------------
		Label webdriverLabel = new Label("Web Driver ");
		webdriverLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		ComboBox<String> webdriverComboBox = new ComboBox<>();
		Tooltip webDriverComboBoxTooltip = new Tooltip("Select the driver that will be used to scrape the data. " +
				"You must have the browser that corresponds to the selected driver installed on your computer for the scraping to work");
		webdriverComboBox.setTooltip(webDriverComboBoxTooltip);
		Hyperlink driversLink = new Hyperlink("Get more drivers");
		Tooltip driversLinkTooltip = new Tooltip("Opens the Selenium webpage from where you can download more drivers");
		driversLink.setTooltip(driversLinkTooltip);
		driversLink.setOnAction(event ->
		{
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://www.selenium.dev/documentation/webdriver/getting_started/install_drivers/#quick-reference"));
				}
				catch (IOException | URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
		});
		Button openDriverDirectoryButton = new Button("Place drivers here");
		Tooltip openDriverDirectoryButtonTooltip = new Tooltip("Opens the /drivers directory. Place the downloaded drivers in here.");
		openDriverDirectoryButton.setTooltip(openDriverDirectoryButtonTooltip);
		openDriverDirectoryButton.setOnAction(event ->
		{
			try
			{
				Desktop.getDesktop().open(new File("drivers"));
			}
			catch (IOException ignored) {}
		});
		HBox driversHBox = new HBox(5, webdriverComboBox, driversLink, openDriverDirectoryButton);

		getExesFromDriverDir();
		webdriverComboBox.setItems(exesInDir);
		if (settings.containsKey("driver")) webdriverComboBox.getSelectionModel().select(settings.get("driver"));
		else webdriverComboBox.getSelectionModel().select(0);
		webdriverComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
		{
			switch (newValue)
			{
				case "Google Chrome" -> settings.put("driver", "chrome");
				case "Mozilla Firefox" -> settings.put("driver", "firefox");
				case "Microsoft Edge" -> settings.put("driver", "edge");
				case "Internet Explorer" -> settings.put("driver", "ie");
				case "Safari" -> settings.put("driver", "safari");
			}
		});

		// ------------------------- Driver amount -------------------------------------------
		int threadAmount = settings.containsKey("threadAmount") ? Integer.parseInt(settings.get("threadAmount")) : 1;
		if (threadAmount == 1) settings.put("threadAmount", "1");
		Label driverAmountLabel = new Label("No. of Drivers");
		driverAmountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		Slider driverAmountSlider = new Slider(1, Runtime.getRuntime().availableProcessors(), threadAmount);
		driverAmountSlider.setMajorTickUnit(1);
		driverAmountSlider.setMinorTickCount(0);
		driverAmountSlider.setShowTickLabels(true);
		driverAmountSlider.setShowTickMarks(true);
		driverAmountSlider.setSnapToTicks(true);
		driverAmountSlider.valueProperty().addListener((observable, oldValue, newValue) ->
				settings.put("threadAmount", Integer.toString((int) driverAmountSlider.getValue())));
		Tooltip driverAmountSliderTooltip = new Tooltip("Select how many drivers will scrape data at the same time. " +
				"Higher values should speed up scraping time. " +
				"Higher values also lead to higher memory and CPU usage, and, depending on the structure and amount of tasks, " +
				"a higher amount of drivers could slow down scraping time.");
		driverAmountSlider.setTooltip(driverAmountSliderTooltip);

		// Local storage config
		mainPane.add(settingsHeaderLabel, 0, 0, 3, 1);
		mainPane.add(storageLocationLabel, 0, 1);
		mainPane.add(storageLocationField, 1, 1);
		mainPane.add(selectDirectoryButton, 2, 1);
		// Database config
		mainPane.add(databaseLabel,0,2);
		mainPane.add(connectionStringField,1,2);
		mainPane.add(tryConnectButton,2,2);
		mainPane.add(connectMessageLabel,3,2);
		// Web driver config
		mainPane.add(webdriverLabel, 0, 3);
		//mainPane.add(webdriverComboBox, 1, 3, 2, 1);
		mainPane.add(driversHBox, 1, 3, 2, 1);
		// Driver amount config
		mainPane.add(driverAmountLabel, 0, 4);
		mainPane.add(driverAmountSlider, 1, 4, 3, 1);

		settingsStage.setScene(new Scene(mainPane));
		settingsStage.sizeToScene();
		settingsStage.showAndWait();
	}

	public Map<String, String> getSettings() { return settings; }

	private void getExesFromDriverDir()
	{
		new File("drivers").mkdir();
		File driverFolder = new File("drivers" + File.separator);
		File[] files = driverFolder.listFiles();

		List<String> fileNames;
		if (files == null || files.length == 0) fileNames = new ArrayList<>();
		else fileNames = Arrays.stream(files).map(File::getName).map(s -> s.replace(".exe", "")).filter(webdriverNames::containsKey).map(webdriverNames::get).toList();

		exesInDir = FXCollections.observableArrayList(fileNames);
		exesInDir.add("Safari");
	}

	private void connectToDatabase(String connectionString, MongoDbHandler mongoDbHandler)
	{
		if(mongoDbHandler.tryConnect(connectionString))
		{
			System.out.println("Connected to database");
		}
		else
		{
			System.out.println("Conncection");
		}
	}

	public static void saveSettings(@Nonnull Map<String, String> settings) throws IOException
	{
		File file = new File("settings.cfg");
		if (file.createNewFile()) System.out.println("Created new settings file");
		FileWriter fileWriter = new FileWriter(file);

		for (Map.Entry<String, String> e : settings.entrySet())
		{
			fileWriter.write(e.getKey() + ": " + e.getValue() + "\n");
		}

		fileWriter.close();
	}

	public static boolean loadSettings(@Nonnull Map<String, String> settings)
	{
		File file = new File("settings.cfg");
		if (!file.exists()) return false;

		System.out.println("Loaded settings file");

		try
		{
			Scanner scanner = new Scanner(file);

			while (scanner.hasNext())
			{
				String line = scanner.nextLine();
				String key = line.substring(0, line.indexOf(":"));
				String value = line.substring(line.indexOf(": ")+2);
				settings.put(key, value);
			}

			scanner.close();
		}
		catch (FileNotFoundException ignored) {}

		return true;
	}
}
