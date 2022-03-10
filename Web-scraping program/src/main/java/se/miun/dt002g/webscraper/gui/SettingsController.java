package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

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

	public SettingsController(Map<String, String> settingsMap)
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

		// ------------------------- Driver selection ----------------------------------------
		Label webdriverLabel = new Label("Web Driver ");
		webdriverLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		ComboBox<String> webdriverComboBox = new ComboBox<>();

		getExesFromDriverDir();
		webdriverComboBox.setItems(exesInDir);
		webdriverComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
		{
			switch (newValue)
			{
				case "Google Chrome" ->
				{
					// todo: Change system property and update settings map.
				}
				case "Mozilla Firefox" ->
				{

				}
				case "Microsoft Edge" ->
				{

				}
				case "Internet Explorer" ->
				{

				}
				case "Safari" ->
				{

				}
			}
		});

		mainPane.add(settingsHeaderLabel, 0, 0, 3, 1);
		mainPane.add(storageLocationLabel, 0, 1);
		mainPane.add(storageLocationField, 1, 1);
		mainPane.add(selectDirectoryButton, 2, 1);
		mainPane.add(webdriverLabel, 0, 3);
		mainPane.add(webdriverComboBox, 1, 3, 2, 1);

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
}
