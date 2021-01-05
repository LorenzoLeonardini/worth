package dev.leonardini.worth.server;

import dev.leonardini.worth.networking.NetworkUtils;

public class ServerMain {
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: ./WORTHServer RMIhost");
			System.err.println("where RMIhost is the hostname used for rmi lookup");
			System.exit(1);
		}
		
		System.setProperty("java.rmi.server.hostname", args[0]);
		
		// Start dbs
		UserManager.init("users.wdb");
		ProjectDB.loadProjectsFile("projectdb");
		// Start RMI stuff
		RMIServer.init();
		
		// Start TCP server
		ServerTCP serverTCP = new ServerTCP();
		serverTCP.start();
		Logger.Log("Server listening on port " + NetworkUtils.SERVER_PORT);
		
		// Get terminal input
		new ServerCLI(serverTCP).start();
	}
}
