package io.github.icohedron.netchatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

	private volatile HashMap<String, ServerClient> clients = new HashMap<String, ServerClient>();
	private volatile ArrayList<String> clientResponse = new ArrayList<String>();
	private final int MAX_ATTEMPTS = 5;
	
	private int port;
	private DatagramSocket socket;
	private volatile boolean running = false;
	private Thread run, manage, send, receive;
	private ExecutorService executor;
	
	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		run = new Thread(this, "Server");
		executor = Executors.newFixedThreadPool(4);
		executor.execute(run);
	}
	
	@Override
	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		
		Scanner scan = new Scanner(System.in);
		while (running) {
			String command = scan.nextLine();
			if (!command.startsWith("/")) {
				sendToAll("/m/" + "<Server> " + command + "/e/");
			} else if (command.equalsIgnoreCase("/exit")) {
				exit();
			} else if (command.equalsIgnoreCase("/help")) {
				printHelp();
			} else if (command.equalsIgnoreCase("/clients")) {
				System.out.println("Number of clients connected: " + clients.size());
				for (String key : clients.keySet()) {
					System.out.println(clients.get(key).name);
				}
			}
		}
		scan.close();
	}
	
	private synchronized void manageClients() {
		manage = new Thread(() -> {
			while (running) {
				sendToAll("/i/server");
				sendClientList();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				for (String key : clients.keySet()) {
					if (!clientResponse.contains(clients.get(key).ID)) {
						if (clients.get(key).attempt >= MAX_ATTEMPTS) {
							disconnect(clients.get(key).ID, false);
						} else {
							clients.get(key).attempt++;
						}
					} else {
						clientResponse.remove(key);
						clients.get(key).attempt = 0;
					}
				}
			}
		}, "Manage");
		executor.execute(manage);
	}
	
	private void sendClientList() {
		if (clients.size() <= 0) return;
		String users = "/u/";
		for (String key : clients.keySet()) {
			users += clients.get(key).name + "/n/";
		}
		users += "/e/";
		sendToAll(users);
	}
	
	private void sendToAll(String message) {
		for (String key : clients.keySet()) {
			ServerClient sc = clients.get(key);
			send(message.getBytes(), sc.address, sc.port);
		}
	}
	
	private void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			try {
				socket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
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
					e.printStackTrace();
				}
				process(packet);
			}
		}, "Receive");
		executor.execute(receive);
	}
	
	private void process(DatagramPacket packet) {
		String string = new String(packet.getData());
		if (string.startsWith("/c/")) {
			String uuid = UUID.randomUUID().toString();
			clients.put(uuid, new ServerClient(string.split("/c/|/e/")[1], packet.getAddress(), packet.getPort(), uuid));
			send(("/c/" + uuid + "/e/").getBytes(), packet.getAddress(), packet.getPort());
			System.out.println("New client: " + string.split("/c/|/e/")[1]);
		} else if (string.startsWith("/m/")){
			sendToAll(string);
			System.out.println(string.split("/m/|/e/")[1]);
		} else if (string.startsWith("/d/")) {
			String userId = string.split("/d/|/e/")[1];
			disconnect(userId, true);
		} else if (string.startsWith("/i/")) {
			clientResponse.add(string.split("/i/|/e/")[1]);
		}
	}
	
	private void disconnect(String id, boolean status) {
		String user = null;
		try {
			user = clients.get(id).name;
			clients.remove(id);
		} catch (NullPointerException e) {
			return;
		}
		String message = "";
		if (status) {
			message = user + " has disconnected from the server!";
		} else {
			message = user + " has timed out.";
		}
		sendToAll("/m/" + message + "/e/");
		System.out.println(message);
	}
	
	private void printHelp() {
		System.out.println("Available Commands:");
		System.out.println("/help - displays this help message");
		System.out.println("/clients - displays all connected clients");
		System.out.println("/exit - safely closes the server");
	}
	
	private void exit() {
		sendToAll("/m/" + "SERVER SHUTTING DOWN..." + "/e/");
		System.out.println("SERVER SHUTTING DOWN...");
		running = false;
		executor.shutdown();
		socket.close();
		System.exit(0);
	}
}
