package io.github.icohedron.netchatserver;

public class ServerMain {
	
	private int port;
	private Server server;
	
	public ServerMain(int port) {
		this.port = port;
		server = new Server(port);
	}
	
	public static void main(String[] args) {
		int port;
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("Usage: java -jar NetChatServer.jar <port>");
			return;
		}
		new ServerMain(port);
	}
}
