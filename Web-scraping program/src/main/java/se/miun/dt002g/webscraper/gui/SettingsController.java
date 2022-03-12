package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.database.MongoDbHandler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class SettingsController
{
	private final Map<String, String> webdriverNames = new HashMap<>() {{
		put("chromedriver.exe", "Google Chrome");
		put("msedgedriver.exe", "Microsoft Edge");
		put("IEDriverServer.exe", "Internet Explorer");
		put("geckodriver.exe", "Mozilla Firefox");
		put("safari", "Safari");
	}};

	private String defaultStorageLocation;
	private ObservableList<String> exesInDir = null;
	private final Map<String, String> settings;

	public SettingsController(Map<String, String> settingsMap, MongoDbHandler mongoDbHandler)
	{
		settings = settingsMap;
		defaultStorageLocation = System.getProperty("user.dir");

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
		storageLocationField.setDisable(true);
		storageLocationField.setMinWidth(400);
		Button selectDirectoryButton = new Button("Open");
		selectDirectoryButton.setOnAction(event ->
		{
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(defaultStorageLocation));

			try
			{
				defaultStorageLocation = directoryChooser.showDialog(new Stage()).getAbsolutePath();
				storageLocationField.setText(defaultStorageLocation);
				settings.put("defaultStorageLocation", defaultStorageLocation);
			}
			catch (Exception ignored) {}
		});

		// ------------------------- Database Config ----------------------------------------
		Label databaseLabel = new Label("MongoDb Connection String: ");
		databaseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		TextField connectionStringField = new TextField("");
		connectionStringField.setMinWidth(60);
		Button tryConnectButton = new Button("Connect");
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
		});


		// ------------------------- Driver selection ----------------------------------------
		Label webdriverLabel = new Label("Web Driver ");
		webdriverLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		ComboBox<String> webdriverComboBox = new ComboBox<>();
		Hyperlink driversLink = new Hyperlink("Get more drivers");
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
		HBox driversHBox = new HBox(5, webdriverComboBox, driversLink);

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
		File driverFolder = new File("drivers/");
		File[] files = driverFolder.listFiles((dir, name) -> name.endsWith(".exe"));

		List<String> fileNames;
		if (files == null || files.length == 0) fileNames = new ArrayList<>();
		else fileNames = Arrays.stream(files).map(File::getName).filter(webdriverNames::containsKey).map(webdriverNames::get).toList();

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
}
