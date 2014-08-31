package io.github.icohedron.netchat;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent login = FXMLLoader.load(Client.class.getResource("/io/github/icohedron/netchat/fxml/login.fxml"));
			Scene loginScene = new Scene(login,500,600);
			
			primaryStage.setResizable(false);
			primaryStage.setTitle("Icohedron's NetChat");
			primaryStage.setScene(loginScene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
