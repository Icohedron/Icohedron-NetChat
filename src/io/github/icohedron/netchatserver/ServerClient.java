package io.github.icohedron.netchatserver;

import java.net.InetAddress;

public class ServerClient {

	public String name;
	public InetAddress address;
	public int port;
	public final String ID;
	public int attempt = 0;
	
	public ServerClient(String name, InetAddress address, int port, String id) {
		this.name = name;
		this.address = address;
		this.port = port;
		ID = id;
	}
}
