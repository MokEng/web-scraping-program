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
	private String sitemapName;
	public EnterBaseURLPopup()
	{
		Stage baseURLStage = new Stage();
		baseURLStage.setResizable(false);
		baseURLStage.initModality(Modality.APPLICATION_MODAL);
		baseURLStage.setTitle("Enter base URL");
		Label enterBaseURLLabel = new Label("Enter base URL ");
		Label enterSitemapNameLabel = new Label("Sitemap name ");
		enterBaseURLLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		enterSitemapNameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold");
		TextField enterBaseURLField = new TextField();
		TextField enterSitemapNameField = new TextField();
		Button enterBaseURLButton = new Button("Enter");
		HBox enterBaseURLHBox = new HBox(5, enterBaseURLLabel, enterBaseURLField,enterSitemapNameLabel,enterSitemapNameField, enterBaseURLButton);
		enterBaseURLButton.setOnAction(event1 ->
		{
			if (enterBaseURLField.getText().length() > 0 && enterSitemapNameField.getText().length() > 0)
			{
				baseURL = enterBaseURLField.getText();
				sitemapName = enterSitemapNameField.getText();
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
	public String getSitemapName(){return sitemapName;}
}
