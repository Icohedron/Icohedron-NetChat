package io.github.icohedron.netchatserver;

import java.net.InetAddress;
import java.util.UUID;

public class ServerClient {

	public String name;
	public InetAddress address;
	public int port;
	public final UUID ID;
	public int attempt = 0;
	
	public ServerClient(String name, InetAddress address, int port, UUID id) {
		this.name = name;
		this.address = address;
		this.port = port;
		ID = id;
	}
}
