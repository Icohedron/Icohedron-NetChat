package io.github.icohedron.netchat;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoginController {
	
	private static final String IPV4ADDRESS_PATTERN =
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private URI uri;
	
	@FXML
	private Button loginButton;
	@FXML
	private TextField nameField;
	@FXML
	private TextField IPField;
	@FXML
	private TextField portField;
	@FXML
	private ImageView image;
	
	public LoginController() {
		try {
			uri = new URI("http://icohedron.github.io");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void login(ActionEvent event) {
		Stage nextStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
		FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("/io/github/icohedron/netchat/fxml/chat.fxml"));
		
		Parent chat = null;
		try {
			chat = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean shouldReturn = false;
		
		String name = nameField.getText();
		if (!name.matches("[a-zA-Z0-9]*") || (name.length() == 0 || name.length() > 14)) {
			nameField.setText("INVALID NAME");
			shouldReturn = true;
		}
		
		String address = IPField.getText();
		if (!isValidIP(address) && !address.equals("localhost")) {
			IPField.setText("INVALID IP");
			shouldReturn = true;
		}
		
		int port = 0;
		if (isInteger(portField.getText())) port = Integer.parseInt(portField.getText());
		else {
			portField.setText("INVALID PORT");
			shouldReturn = true;
		}
		
		if (shouldReturn == true) return;
		
		ChatController controller = fxmlLoader.<ChatController>getController();
		controller.setUserAndAddress(name, address, port);
		controller.setCloseOperation(nextStage);
		
		Scene chatScene = new Scene(chat, 800, 600);
		nextStage.setScene(chatScene);
	}
	
	@FXML
	private void showSite() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean isValidIP(String ip) {
		return Pattern.matches(IPV4ADDRESS_PATTERN, ip) ? true : false;
	}
	
	private boolean isInteger(String s) {
		try { Integer.parseInt(s); }
		catch (NumberFormatException e) { return false; }
		return true;
	}
}
