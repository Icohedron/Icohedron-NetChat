package io.github.icohedron.netchatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.UUID;

public class Server implements Runnable {

	private HashMap<String, ServerClient> clients = new HashMap<String, ServerClient>();
	
	private int port;
	private DatagramSocket socket;
	private volatile boolean running = false;
	private Thread run, manage, send, receive;
	
	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		run = new Thread(this, "Server");
		run.start();
	}
	
	@Override
	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
	}
	
	private synchronized void manageClients() {
		manage = new Thread(() -> {
			while (running) {
				
			}
		}, "Manage");
		manage.start();
	}
	
	private synchronized void sendToAll(String message) {
		for (int i = 0; i < clients.size(); i++) {
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.address, client.port);
		}
	}
	
	private synchronized void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			try {
				socket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "Send");
		send.start();
	}
	
	private synchronized void receive() {
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
		receive.start();
	}
	
	private synchronized void process(DatagramPacket packet) {
		String string = new String(packet.getData());
		if (string.startsWith("/c/")) {
			UUID uuid = UUID.randomUUID();
			clients.put(string.split("/c/|/e/")[1], new ServerClient(string.split("/c/|/e/")[1], packet.getAddress(), packet.getPort(), uuid));
			send(("/c/" + uuid.toString() + "/e/").getBytes(), packet.getAddress(), packet.getPort());
			System.out.println("New client: " + string.split("/c/|/e/")[1]);
		} else if (string.startsWith("/m/")){
			sendToAll(string);
			System.out.println(string.split("/m/|/e/")[1]);
		} else if (string.startsWith("/d/")) {
			String user = string.split("/d/|/e/")[1];
			sendToAll("/m/" + user + " has left the chat room." + "/e/");
			System.out.println(user + " has left the chat room.");
			clients.remove(user);
		}
	}
}
