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

import java.util.Map;
import java.util.Objects;

public class RunScraperPopup {
    public boolean isRunScraper() {
        return runScraper;
    }

    private boolean runScraper=false;
    private final DbStorageSettings dbSettings = new DbStorageSettings();

    private final ScrapeSettings scrapeSettings = new ScrapeSettings();

    public RunScraperPopup(String sitemapName,MongoDbHandler mongoDbHandler, Map<String,String> settings)
    {
        GridPane mainPane = new GridPane();
        mainPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
        mainPane.setVgap(10);
        mainPane.setHgap(5);
        Stage runScraperStage = new Stage();
        runScraperStage.setResizable(false);
        runScraperStage.initModality(Modality.APPLICATION_MODAL);
        runScraperStage.setTitle("Run web scraper on "+sitemapName+" sitemap");
        Label chooseStorageLabel = new Label("Select where to save your data: ");
        Label dataFormatLabel  = new Label("Choose format for device storage: ");
        chooseStorageLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        dataFormatLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        RadioButton localButton = new RadioButton("Local Storage");
        Tooltip localButtonTooltip = new Tooltip("Save the scraped data locally on your computer");
        localButton.setTooltip(localButtonTooltip);
        RadioButton databaseButton = new RadioButton("MongoDb");
        Tooltip databaseButtonTooltip = new Tooltip("Save the scraped data on a MongoDB database");
        databaseButton.setTooltip(databaseButtonTooltip);
        Label dbIsConnectedLabel= new Label("");
        if(!mongoDbHandler.isConnected()){
            databaseButton.setDisable(true);
            dbIsConnectedLabel.setText("Not connected to database");
            dbIsConnectedLabel.setStyle("-fx-background-color: red;");
        }else{
            dbIsConnectedLabel.setText("Connected");
            dbIsConnectedLabel.setStyle("-fx-background-color: lightgreen;");
        }

        ChoiceBox<String> formatPicker = new ChoiceBox<>();
        formatPicker.getItems().add(DATA_FORMAT.json.name());
        formatPicker.getItems().add(DATA_FORMAT.csv.name());
        formatPicker.getSelectionModel().select(0);
        Tooltip formatPickerTooltip = new Tooltip("Select the format used to save the data");
        formatPicker.setTooltip(formatPickerTooltip);

        Label groupByLabel = new Label("Select way to group the data: ");
        ChoiceBox<String> groupPicker = new ChoiceBox<>();
        groupPicker.getItems().add(GROUPBY.id.name());
        groupPicker.getItems().add(GROUPBY.dataName.name());
        groupPicker.getSelectionModel().select(0);
        Tooltip groupPickerTooltip = new Tooltip("Select how to group the data");
        groupPicker.setTooltip(groupPickerTooltip);

        Label databaseSettingsLabel = new Label("Database Settings");
        databaseSettingsLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");

        Label databaseNameLabel = new Label("Database: ");
        TextField databaseNameTextField= new TextField(settings.get("latestDb"));
        Tooltip databaseNameTextFieldTooltip = new Tooltip("The name of the database");
        databaseNameTextField.setTooltip(databaseNameTextFieldTooltip);

        Label collectionNameLabel = new Label("Collection: ");
        TextField collectionNameTextField = new TextField(settings.get("latestColl"));
        Tooltip collectionNameTextFieldTooltip = new Tooltip("The name of the collection to store the data in");
        collectionNameTextField.setTooltip(collectionNameTextFieldTooltip);
        RadioButton dropPreviousData = new RadioButton("Drop previous data in collection");
        Tooltip dropPreviousDataTooltip = new Tooltip("If the data currently in the collection should be deleted before the new data is inserted");
        dropPreviousData.setTooltip(dropPreviousDataTooltip);

        Button runButton = new Button("Run");
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

        mainPane.add(runButton,0,8);
        mainPane.add(cancelButton,1,8);

        runButton.setOnAction(event1 -> {

            scrapeSettings.saveLocal = localButton.isSelected();
            scrapeSettings.saveDb = databaseButton.isSelected();
            if(Objects.equals(formatPicker.getSelectionModel().getSelectedItem(), DATA_FORMAT.json.name())){
                scrapeSettings.dataFormat = DATA_FORMAT.json;
            }else{
                scrapeSettings.dataFormat = DATA_FORMAT.csv;
            }
            if(Objects.equals(groupPicker.getSelectionModel().getSelectedItem(), GROUPBY.id.name())){
                scrapeSettings.groupby = GROUPBY.id;
            }else{
                scrapeSettings.groupby = GROUPBY.dataName;
            }
            dbSettings.dropPreviousData = dropPreviousData.isSelected();
            dbSettings.collectionName = collectionNameTextField.getText();
            dbSettings.databaseName = databaseNameTextField.getText();

            settings.put("latestDb",databaseNameTextField.getText());
            settings.put("latestColl",collectionNameTextField.getText());
            if ((scrapeSettings.saveLocal || scrapeSettings.saveDb)) {
                runScraper = true;
                runScraperStage.close();
            }
        });
        cancelButton.setOnAction(event -> {
            runScraper=false;
            runScraperStage.close();
        });
        mainPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px; -fx-label-padding: 10px;");
        runScraperStage.setScene(new Scene(mainPane));
        runScraperStage.showAndWait();
    }


    public ScrapeSettings getScrapeSettings() {
        scrapeSettings.dbStorageSettings = dbSettings;
        return scrapeSettings;
    }
}

