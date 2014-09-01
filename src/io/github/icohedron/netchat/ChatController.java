package io.github.icohedron.netchat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ChatController {
	
	private String user;
	private String address;
	private int port;
	private String uuid;
	private volatile boolean running = false;
	
	private DatagramSocket socket; // Using UDP instead of TCP because this is for practicing multiplayer in games
	private InetAddress ip;
	
	private Thread receive, send;
	private ExecutorService executor;
	
	@FXML
	private Parent root;
	@FXML
	private TextArea textArea;
	@FXML
	private TextArea serverList;
	@FXML
	private TextField messageField;
	@FXML
	private Button sendButton;
	
	public void setCloseOperation(Stage stage) {
		stage.setOnCloseRequest((WindowEvent) -> {
			send(("/d/" + uuid + "/e/").getBytes());
			running = false;
			executor.shutdown();
			socket.close();
			Platform.exit();
			System.exit(0);
		});
	}
	
	public void setUserAndAddress(String user, String address, int port) {
		this.user = user;
		this.address = address;
		this.port = port;
		
		executor = Executors.newFixedThreadPool(2);
		running = true;
		
		textArea.appendText("Attempting to connect to " + address + ":" + port + " as user: " + user + "\n");
		String connection = "/m/" + user + " has connected to the server" + "/e/";
		
		boolean connected = openConnection(address);
		if (!connected) {
			textArea.appendText("Connection failed! \n");
		}
		
		receive();
		
		Platform.runLater(() -> {
			messageField.requestFocus();
			send(connection.getBytes());
			send(("/c/" + user + "/e/").getBytes());
		});
	}
	
	private boolean openConnection(String address) {
		try {
			socket = new DatagramSocket();
			ip = InetAddress.getByName(address);
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@FXML
	private void sendMessage() {
		String message = "/m/" + user + ": " + messageField.getText() + "/e/";
		send(message.getBytes());
		messageField.clear();
	}
	
	private void send(final byte[] data) {
		send = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
			try {
				socket.send(packet);
			} catch (IOException e) {
			}
		}, "Send");
		executor.execute(send);
	}
	
	private void receive() {
		receive = new Thread(() -> {
			while (running) {
				byte[] data = new byte[1024];
				DatagramPacket packet = new DatagramPacket(data, data.length);
				
				try {
					socket.receive(packet);
				} catch (IOException e) {
				}
			
				process(packet);
			}
		}, "Receive");
		executor.execute(receive);
	}
	
	private void process(DatagramPacket packet) {
		String message = new String(packet.getData());
		Platform.runLater(() -> {
			if (message.startsWith("/c/")) {
				uuid = message.split("/c/|/e/")[1];
				textArea.appendText("Connection established! \n");
				textArea.appendText("Your UUID is " + uuid + "\n");
			} else if (message.startsWith("/i/")) {
				send(("/i/" + uuid + "/e/").getBytes());
			} else if (message.startsWith("/u/")) {
				String[] usersOnline = message.split("/u/|/n/|/e/");
				serverList.clear();
				for (int i = 1; i < usersOnline.length; i++) {
					serverList.appendText(usersOnline[i] + "\n");
				}
			} else {
				textArea.appendText(message.split("/m/|/e/")[1] + "\n");
			}
		});
	}
}
