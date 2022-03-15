package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.Pair;
import se.miun.dt002g.webscraper.scraper.Sitemap;

import java.util.List;

public class ScheduledScrapersController {
    ScheduledScrapersController(List<SitemapController.TimerService> scheduledList){
        Stage scheduleStage = new Stage();
        scheduleStage.initModality(Modality.APPLICATION_MODAL);
        scheduleStage.setTitle("Scheduled Scrapes");

        GridPane mainPane = new GridPane();
        mainPane.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
        mainPane.setVgap(10);
        mainPane.setHgap(5);

        Label scheduledLabel = new Label("Scheduled Scrapes");
        scheduledLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");

        ListView<SitemapController.TimerService> scheduledScrapesList= new ListView<>();
        scheduledScrapesList.setItems(FXCollections.observableArrayList(scheduledList));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            scheduledScrapesList.getSelectionModel().getSelectedItem().cancel();
            scheduledList.remove(scheduledScrapesList.getSelectionModel().getSelectedItem());
            scheduledScrapesList.setItems(FXCollections.observableArrayList(scheduledList));
        });

        mainPane.add(scheduledLabel,0,0);
        mainPane.add(scheduledScrapesList,0,1,4,4);
        mainPane.add(deleteButton,4,0);

        scheduleStage.setScene(new Scene(mainPane));
        scheduleStage.sizeToScene();
        scheduleStage.showAndWait();

    }
}
