package se.miun.dt002g.webscraper.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.Duration;
import java.util.List;

/**
 * Window that displays the results of a scraping.
 */
public class StatsPopup
{
	public StatsPopup(int nBytesTotal, Duration timeTotal, List<String> tasks, List<Integer> nBytesPerTask, List<Duration> timePerTask)
	{
		Stage statsStage = new Stage();

		GridPane mainPane = new GridPane();
		mainPane.setStyle("-fx-border-insets: 10px; -fx-padding: 10px;");

		Label totalBytesLabel = new Label("Total bytes scraped "),
				totalTimeLabel = new Label("Total scraping duration ");
		totalBytesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		totalTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		Text totalBytesText = new Text("" + nBytesTotal + " bytes"),
				totalTimeText = new Text("" + timeTotal.getSeconds() + "." + timeTotal.toMillisPart() + " seconds");
		totalBytesText.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		totalTimeText.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");

		ListView<String> taskList = new ListView<>();
		Text taskBytesText = new Text("Bytes scraped: "),
				taskTimeText = new Text("Scraping duration: ");
		VBox taskDataVBox = new VBox(5, taskBytesText, taskTimeText);

		ObservableList<String> list = FXCollections.observableArrayList();
		for (int i = 0; i < tasks.size(); i++)
		{
			// If the amount of bytes scraped is -1, add the [Failed] text at the end.
			list.add(tasks.get(i) + (nBytesPerTask.get(i) == -1 ? " [Failed]" : ""));
		}

		taskList.setItems(list);

		taskList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			// Show the number of bytes and seconds when selecting a task in the list.
			int i = taskList.getSelectionModel().getSelectedIndex();
			taskBytesText.setText("Bytes scraped: " + ((nBytesPerTask.get(i) == -1) ? 0 : nBytesPerTask.get(i)));
			taskTimeText.setText("Scraping duration: " + timePerTask.get(i).getSeconds() + "." + timePerTask.get(i).toMillisPart() + " seconds");
		});

		Label timePieChartLabel = new Label("Task Duration"),
				dataPieChartLabel = new Label("Task Data Size");
		timePieChartLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		dataPieChartLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
		PieChart timePieChart = new PieChart(),
				dataPieChart = new PieChart();

		for (int i = 0; i < tasks.size(); i++)
		{
			if (nBytesPerTask.get(i) != -1)
			{
				// Add the data from each task to the pie charts.
				dataPieChart.getData().add(new PieChart.Data(tasks.get(i).substring(tasks.get(i).indexOf("(")+1, tasks.get(i).indexOf(",")), nBytesPerTask.get(i)));
				timePieChart.getData().add(new PieChart.Data(tasks.get(i).substring(tasks.get(i).indexOf("(")+1, tasks.get(i).indexOf(",")), timePerTask.get(i).toMillis()));
			}
		}

		mainPane.setVgap(5);
		mainPane.setHgap(5);
		mainPane.add(totalBytesLabel, 0, 0);
		mainPane.add(totalBytesText, 1, 0);
		mainPane.add(totalTimeLabel, 0, 1);
		mainPane.add(totalTimeText, 1, 1);
		mainPane.add(taskList,0, 2);
		mainPane.add(taskDataVBox, 1, 2);
		mainPane.add(dataPieChartLabel, 0, 3);
		mainPane.add(timePieChartLabel, 1, 3);
		mainPane.add(dataPieChart, 0, 4);
		mainPane.add(timePieChart, 1, 4);

		statsStage.setScene(new Scene(mainPane));
		statsStage.setWidth(700);
		statsStage.setHeight(600);
		statsStage.setTitle("Scraping Summary");
		statsStage.initModality(Modality.APPLICATION_MODAL);
		statsStage.showAndWait();
	}
}
