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

/**
 * Window where the user can change program settings.
 */
public class SettingsController
{
	// Maps the driver filenames to the browser names.
	private final Map<String, String> webdriverNames = new HashMap<>()
	{{
		put("chromedriver", "Google Chrome");
		put("msedgedriver", "Microsoft Edge");
		put("IEDriverServer", "Internet Explorer");
		put("geckodriver", "Mozilla Firefox");
		put("safari", "Safari");
	}};

	private String defaultStorageLocation; // The local storage location.
	private ObservableList<String> exesInDir = null; // Which driver executables are in the /drivers directory.
	private final Map<String, String> settings; // Stores the settings.

	public SettingsController(Map<String, String> settingsMap, MongoDbHandler mongoDbHandler)
	{
		settings = settingsMap;
		// Get the storage location if it is set, otherwise default to the users default directory.
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
		storageLocationField.setTooltip(new Tooltip("The location on you computer where the scraped data will be saved. " +
				"Press the Open button to select a new location"));
		storageLocationField.setMinWidth(400);
		Button selectDirectoryButton = new Button("Open");
		selectDirectoryButton.setTooltip(new Tooltip("Click to select a new storage location"));
		selectDirectoryButton.setOnAction(event -> // Choose a new local storage directory.
		{
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(defaultStorageLocation));

			try
			{
				// Opens a window where you can select a directory.
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
		connectionStringField.setTooltip(new Tooltip("Enter the connection string used to connect to your MongoDB database"));
		Button tryConnectButton = new Button("Connect");
		tryConnectButton.setTooltip(new Tooltip("Press to try to connect to MongoDB using the connection string"));
		Label connectMessageLabel = new Label("");

		if(mongoDbHandler.isConnected()) // Whether the program is connected to the database or not.
		{
			connectMessageLabel.setText("Connected to database");
			connectMessageLabel.setStyle("-fx-background-color: lightgreen;");
			connectionStringField.setText(mongoDbHandler.getConnectionString());
		}
		else
		{
			connectMessageLabel.setText("Not connected to database");
			connectMessageLabel.setStyle("-fx-background-color: red;");
		}

		// Try to connect to MongoDB.
		tryConnectButton.setOnAction(event ->
		{
			String text = connectionStringField.getText();
			if(mongoDbHandler.tryConnect(text))
			{
				connectMessageLabel.setText("Connected to database");
				connectMessageLabel.setStyle("-fx-background-color: lightgreen;");
				settings.put("dbConnection",text);
			}
			else
			{
				connectMessageLabel.setText("Connection failed");
				connectMessageLabel.setStyle("-fx-background-color: red;");
			}
		});


		// ------------------------- Driver selection ----------------------------------------
		Label webdriverLabel = new Label("Web Driver ");
		webdriverLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		ComboBox<String> webdriverComboBox = new ComboBox<>();
		webdriverComboBox.setTooltip(new Tooltip("Select the driver that will be used to scrape the data. " +
				"You must have the browser that corresponds to the selected driver installed on your computer for the scraping to work"));
		Hyperlink driversLink = new Hyperlink("Get more drivers");
		driversLink.setTooltip(new Tooltip("Opens the Selenium webpage from where you can download more drivers"));

		driversLink.setOnAction(event -> // Opens a browser window to the Selenium driver download page, if opening browser windows is supported.
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
		openDriverDirectoryButton.setTooltip(new Tooltip("Opens the /drivers directory. Place the downloaded drivers in here."));
		openDriverDirectoryButton.setOnAction(event -> // Opens the drivers directory.
		{
			try
			{
				Desktop.getDesktop().open(new File("drivers"));
			}
			catch (IOException ignored) {}
		});
		HBox driversHBox = new HBox(5, webdriverComboBox, driversLink, openDriverDirectoryButton);

		getExesFromDriverDir(); // Get all driver executables in folder.
		webdriverComboBox.setItems(exesInDir); // Set all available driver exes in combobox.
		// If a driver is already in the settings, make that the selected one in the combobox.
		if (settings.containsKey("driver")) webdriverComboBox.getSelectionModel().select(settings.get("driver"));
		else webdriverComboBox.getSelectionModel().select(0); // Otherwise, select the first one.
		webdriverComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
		{
			switch (newValue) // Sets the settings value to the correct driver.
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
		// Adds the driver amount to the settings.
		driverAmountSlider.valueProperty().addListener((observable, oldValue, newValue) ->
				settings.put("threadAmount", Integer.toString((int) driverAmountSlider.getValue())));
		driverAmountSlider.setTooltip(new Tooltip("Select how many drivers will scrape data at the same time. " +
				"Higher values should speed up scraping time. " +
				"Higher values also lead to higher memory and CPU usage, and, depending on the structure and amount of tasks, " +
				"a higher amount of drivers could slow down scraping time."));

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

	/**
	 * Get the settings map.
	 * @return Map that contains the settings values.
	 */
	public Map<String, String> getSettings() { return settings; }

	/**
	 * Searches the drivers directory and finds all driver executables.
	 */
	private void getExesFromDriverDir()
	{
		new File("drivers").mkdir(); // Create the drivers directory if it does not exist.
		File driverFolder = new File("drivers" + File.separator);
		File[] files = driverFolder.listFiles(); // Find all files in directory.

		List<String> fileNames;
		if (files == null || files.length == 0) fileNames = new ArrayList<>();
		// Remove all files that are not one of the drivers from the webdriverNames map. Remove .exe file extension if we are no Windows.
		else fileNames = Arrays.stream(files).map(File::getName).map(s -> s.replace(".exe", "")).filter(webdriverNames::containsKey).map(webdriverNames::get).toList();

		exesInDir = FXCollections.observableArrayList(fileNames);
		// Add Safari to the list if we are on Mac.
		if (System.getProperty("os.name").toLowerCase().contains("mac")) exesInDir.add("Safari");
	}

	/**
	 * Connect to the database.
	 * @param connectionString The connection string.
	 * @param mongoDbHandler The MongoDB handler.
	 */
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

	/**
	 * Save the settings to the settings.cfg file.
	 * @param settings The map containing the settings.
	 * @throws IOException If the file could not be created or opened.
	 */
	public static void saveSettings(@Nonnull Map<String, String> settings) throws IOException
	{
		File file = new File("settings.cfg");
		if (file.createNewFile()) System.out.println("Created new settings file");
		FileWriter fileWriter = new FileWriter(file);

		for (Map.Entry<String, String> e : settings.entrySet()) // Writes all values to file.
		{
			fileWriter.write(e.getKey() + ": " + e.getValue() + "\n");
		}

		fileWriter.close();
	}

	/**
	 * Load the settings from the settings.cfg file.
	 * @param settings The map that will be filled with the settings values.
	 * @return If the settings.cfg file was found or not.
	 */
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
