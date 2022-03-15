package se.miun.dt002g.webscraper.gui;

import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.miun.dt002g.webscraper.scraper.Sitemap;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressStage extends Stage {
    private Runnable runnable;
    ProgressStage(Sitemap sitemap,int NR_OF_DRIVERS){
        ProgressBar progressBar = new ProgressBar(0.0);
        int nTasks = NR_OF_DRIVERS;
        Text progressText = new Text("Drivers done 0/"+nTasks);
        progressText.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
        HBox progessHBox = new HBox(5, progressText, progressBar);
        progessHBox.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
        setScene(new Scene(progessHBox));
        AtomicInteger finishedTasks = new AtomicInteger(0);
        sizeToScene();
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Executing Tasks...");
        runnable = ()->{
            progressBar.setProgress((double)finishedTasks.incrementAndGet()/(double)nTasks);
            progressText.setText("Drivers done "+finishedTasks.get() + "/" + nTasks);
        };
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
