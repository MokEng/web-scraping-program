package se.miun.dt002g.webscraper.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EnterBaseURLPopup
{
	private String baseURL;

	public EnterBaseURLPopup()
	{
		Stage baseURLStage = new Stage();
		baseURLStage.setResizable(false);
		baseURLStage.initModality(Modality.APPLICATION_MODAL);
		baseURLStage.setTitle("Enter base URL");
		Label enterBaseURLLabel = new Label("Enter base URL ");
		enterBaseURLLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField enterBaseURLField = new TextField();
		Button enterBaseURLButton = new Button("Enter");
		HBox enterBaseURLHBox = new HBox(5, enterBaseURLLabel, enterBaseURLField, enterBaseURLButton);
		enterBaseURLButton.setOnAction(event1 ->
		{
			if (enterBaseURLField.getText().length() > 0)
			{
				baseURL = enterBaseURLField.getText();
				baseURLStage.close();
			}
		});
		enterBaseURLHBox.setStyle("-fx-border-insets: 5px; -fx-padding: 5px;");
		baseURLStage.setScene(new Scene(enterBaseURLHBox));

		baseURLStage.showAndWait();
	}

	public String getBaseURL()
	{
		return baseURL;
	}
}