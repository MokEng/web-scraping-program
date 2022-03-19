package se.miun.dt002g.webscraper.gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.database.DbStorageSettings;
import se.miun.dt002g.webscraper.database.MongoDbHandler;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.GROUPBY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

public class ScheduleScraperPopup
{
    private boolean runScraper=false; // If the scraper should be scheduled.
    private DbStorageSettings dbSettings = new DbStorageSettings();
    private ScrapeSettings scrapeSettings = new ScrapeSettings();

    public ScheduleScraperPopup(String sitemapName, MongoDbHandler mongoDbHandler, Map<String,String> settings)
    {
        GridPane mainPane = new GridPane();
        mainPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
        mainPane.setVgap(10);
        mainPane.setHgap(5);

        Stage runScraperStage = new Stage();
        runScraperStage.setResizable(false);
        runScraperStage.initModality(Modality.APPLICATION_MODAL);
        runScraperStage.setTitle("Schedule "+sitemapName);

        Label chooseStorageLabel = new Label("Select where to save your data: ");
        Label dataFormatLabel  = new Label("Choose format for device storage: ");
        chooseStorageLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        dataFormatLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        RadioButton localButton = new RadioButton("Local Storage");
        RadioButton databaseButton = new RadioButton("MongoDb");
        Label dbIsConnectedLabel= new Label("");

        if(!mongoDbHandler.isConnected()) // Whether the program is connected to MongoDB or not.
        {
            databaseButton.setDisable(true);
            dbIsConnectedLabel.setText("Not connected to database");
            dbIsConnectedLabel.setStyle("-fx-background-color: red;");
        }
        else
        {
            dbIsConnectedLabel.setText("Connected");
            dbIsConnectedLabel.setStyle("-fx-background-color: lightgreen;");
        }

        // Data format.
        ChoiceBox<String> formatPicker = new ChoiceBox<>();
        formatPicker.getItems().add(DATA_FORMAT.json.name());
        formatPicker.getItems().add(DATA_FORMAT.csv.name());
        formatPicker.getSelectionModel().select(0);

        // Data grouping.
        Label groupByLabel = new Label("Select way to group the data: ");
        ChoiceBox<String> groupPicker = new ChoiceBox<>();
        groupPicker.getItems().add(GROUPBY.id.name());
        groupPicker.getItems().add(GROUPBY.dataName.name());
        groupPicker.getSelectionModel().select(0);

        Label databaseSettingsLabel = new Label("Database Settings");
        databaseSettingsLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");

        // Database name.
        Label databaseNameLabel = new Label("Database: ");
        TextField databaseNameTextField= new TextField(settings.get("latestDb"));

        // Collection name.
        Label collectionNameLabel = new Label("Collection: ");
        TextField collectionNameTextField = new TextField(settings.get("latestColl"));
        RadioButton dropPreviousData = new RadioButton("Drop previous data in collection");

        Label schedulingLabel = new Label("Scheduling Options");
        schedulingLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        // Scheduling date and time picker.
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ChoiceBox<LocalTime> hours = new ChoiceBox<>();
        for(int x = 0; x < 24;x++)
        {
            hours.getItems().add(LocalTime.of(x,0));
        }
        hours.getSelectionModel().select(15);

        Button runButton = new Button("Schedule");
        Button cancelButton = new Button("Cancel");
        mainPane.add(localButton,0,0);

        mainPane.add(dataFormatLabel,0,1);
        mainPane.add(formatPicker,1,1);

        mainPane.add(new Separator(),0,2,5,1);

        mainPane.add(groupByLabel,0,3);
        mainPane.add(groupPicker,1,3);

        mainPane.add(new Separator(),0,4,5,1);

        mainPane.add(databaseButton,0,5);
        mainPane.add(dbIsConnectedLabel,1,5);

        mainPane.add(databaseNameLabel,0,6);
        mainPane.add(databaseNameTextField,1,6);
        mainPane.add(collectionNameLabel,2,6);
        mainPane.add(collectionNameTextField,3,6);
        mainPane.add(dropPreviousData,4,6);

        mainPane.add(new Separator(),0,7,5,1);

        mainPane.add(schedulingLabel,0,8);
        mainPane.add(datePicker,1,8,2,1);
        mainPane.add(hours,3,8);


        mainPane.add(runButton,0,10);
        mainPane.add(cancelButton,1,10);

        // Runs when the run button is pressed.
        runButton.setOnAction(event1 ->
        {
            scrapeSettings.startAt = LocalDateTime.of(datePicker.getValue(),hours.getValue());
            scrapeSettings.saveLocal = localButton.isSelected();
            scrapeSettings.saveDb = databaseButton.isSelected();

            // Get data format.
            if(Objects.equals(formatPicker.getSelectionModel().getSelectedItem(), DATA_FORMAT.json.name()))
            {
                scrapeSettings.dataFormat = DATA_FORMAT.json;
            }
            else
            {
                scrapeSettings.dataFormat = DATA_FORMAT.csv;
            }

            // Get data grouping.
            if(Objects.equals(groupPicker.getSelectionModel().getSelectedItem(), GROUPBY.id.name()))
            {
                scrapeSettings.groupby = GROUPBY.id;
            }
            else
            {
                scrapeSettings.groupby = GROUPBY.dataName;
            }

            // Get database settings.
            dbSettings.dropPreviousData = dropPreviousData.isSelected();
            dbSettings.collectionName = collectionNameTextField.getText();
            dbSettings.databaseName = databaseNameTextField.getText();

            settings.put("latestDb",databaseNameTextField.getText());
            settings.put("latestColl",collectionNameTextField.getText());

            if ((scrapeSettings.saveLocal || scrapeSettings.saveDb)) // If either or both of the storage options are active.
            {
                // Signal that the scraper should be scheduled and close the window.
                runScraper = true;
                runScraperStage.close();
            }
        });

        // Runs when the cancel button is pressed.
        cancelButton.setOnAction(event ->
        {
            // Signal that the scraper should not be scheduled and close the window.
            runScraper=false;
            runScraperStage.close();
        });

        mainPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px; -fx-label-padding: 10px;");
        runScraperStage.setScene(new Scene(mainPane));
        runScraperStage.showAndWait();
    }

    /**
     * Get the scraper settings.
     * @return The settings the scraper should use.
     */
    public ScrapeSettings getScrapeSettings() {
        scrapeSettings.dbStorageSettings = dbSettings;
        return scrapeSettings;
    }

    /**
     * Get if the scraper should be scheduled.
     * @return If the scraper should be scheduled.
     */
    public boolean isRunScraper() {
        return runScraper;
    }
}
