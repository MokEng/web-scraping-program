package se.miun.dt002g.webscraper.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.DATA_FORMAT;
import se.miun.dt002g.webscraper.scraper.GROUPBY;

import java.util.Objects;

public class RunScraperPopup {
    public boolean isRunScraper() {
        return runScraper;
    }

    public boolean isSaveOnDevice() {
        return saveOnDevice;
    }

    public boolean isSaveOnDatabase() {
        return saveOnDatabase;
    }

    public int getNrOfWebDrivers() {
        return nrOfWebDrivers;
    }

    private boolean runScraper=false;
    private boolean saveOnDevice=false;
    private boolean saveOnDatabase=false;
    private int nrOfWebDrivers=1;

    public GROUPBY getGroupby() {
        return groupby;
    }

    private GROUPBY groupby = GROUPBY.id;
    public DATA_FORMAT getLocalDataFormat() {
        return localDataFormat;
    }

    private DATA_FORMAT localDataFormat= DATA_FORMAT.json;

    public RunScraperPopup(String sitemapName)
    {
        Stage runScraperStage = new Stage();
        runScraperStage.setResizable(false);
        runScraperStage.initModality(Modality.APPLICATION_MODAL);
        runScraperStage.setTitle("Run web scraper on "+sitemapName+" sitemap");
        Label chooseStorageLabel = new Label("Select where to save your data: ");
        Label dataFormatLabel  = new Label("Choose format for device storage: ");
        chooseStorageLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        dataFormatLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
        RadioButton localButton = new RadioButton("Local Storage");
        RadioButton databaseButton = new RadioButton("Database");
        ChoiceBox<String> formatPicker = new ChoiceBox<>();
        formatPicker.getItems().add(DATA_FORMAT.json.name());
        formatPicker.getItems().add(DATA_FORMAT.csv.name());
        formatPicker.getSelectionModel().select(0);
        Label groupByLabel = new Label("Select way to group the data: ");
        ChoiceBox<String> groupPicker = new ChoiceBox<>();
        groupPicker.getItems().add(GROUPBY.id.name());
        groupPicker.getItems().add(GROUPBY.dataName.name());
        groupPicker.getSelectionModel().select(0);

        Button runButton = new Button("Run");
        Button cancelButton = new Button("Cancel");
        HBox hBox = new HBox(5, chooseStorageLabel, localButton,databaseButton,dataFormatLabel,formatPicker,groupByLabel,groupPicker, runButton,cancelButton);
        runButton.setOnAction(event1 -> {
            saveOnDevice = localButton.isSelected();
            saveOnDatabase = databaseButton.isSelected();
            if(Objects.equals(formatPicker.getSelectionModel().getSelectedItem(), DATA_FORMAT.json.name())){
                this.localDataFormat = DATA_FORMAT.json;
            }else{
                this.localDataFormat = DATA_FORMAT.csv;
            }
            if(Objects.equals(groupPicker.getSelectionModel().getSelectedItem(), GROUPBY.id.name())){
                this.groupby = GROUPBY.id;
            }else{
                this.groupby = GROUPBY.dataName;
            }
            if ((saveOnDevice || saveOnDatabase)) {
                runScraper = true;
                runScraperStage.close();
            }
        });
        cancelButton.setOnAction(event -> {
            runScraper=false;
            runScraperStage.close();
        });
        hBox.setStyle("-fx-border-insets: 5px; -fx-padding: 5px; -fx-label-padding: 10px;");
        runScraperStage.setScene(new Scene(hBox));
        runScraperStage.showAndWait();
    }

}

