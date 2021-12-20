package gui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.application.Application;

public class PasswordManager extends Application
{
	private static Stage mainStage;
	private static PasswordManagerWindow control;

	public void start(final Stage primaryStage) {
		PasswordManager.mainStage = primaryStage;
		try {
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(this.getClass().getResource("tbView.fxml"));
			final AnchorPane anchorpane = (AnchorPane)loader.load();
			final Scene scene = new Scene((Parent)anchorpane);
			PasswordManager.control = (PasswordManagerWindow)loader.getController();
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Password Manager");
			primaryStage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PasswordManagerWindow getControl() {
		return PasswordManager.control;
	}

	public static Stage getMainStage() {
		return PasswordManager.mainStage;
	}

	public static void main(final String[] args) {
		launch(args);
	}
}
