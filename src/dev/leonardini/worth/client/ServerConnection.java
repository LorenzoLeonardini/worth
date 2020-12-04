package dev.leonardini.worth.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.networking.UserRegistration.InvalidRegistrationException;

public class ServerConnection {
	
	private String host;
	private Registry registry;
	private String message_info = "";
	private SocketChannel socketChannel = null;

	public boolean estabilish(String host) {
		if(this.host == null || !this.host.equalsIgnoreCase(host)) {
			if(socketChannel != null) {
				try {
					socketChannel.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			socketChannel = null;
		}
		this.host = host;
		try {
			registry = LocateRegistry.getRegistry(host, NetworkUtils.REGISTRY_PORT);
		} catch (RemoteException e) {
			this.host = null;
			this.registry = null;
			return false;
		}
		return true;
	}
	
	public boolean register(String username, String password) {
		try {
			UserRegistration registration_service;
			registration_service = (UserRegistration) registry.lookup(NetworkUtils.USER_REGISTRATION);
			String msg = registration_service.register(username, password);
			message_info = msg;
			return true;
		} catch(RemoteException | InvalidRegistrationException ex) {
			if(ex instanceof InvalidRegistrationException)
				message_info = ex.getMessage();
			else
				message_info = "Errore di connessione";
			return false;
		} catch (Exception e) {
			message_info = "Errore di connessione";
			return false;
		}
	}
	
	public boolean login(String username, String password) {
		try {
			if(socketChannel == null)
				socketChannel = SocketChannel.open(new InetSocketAddress(host, NetworkUtils.SERVER_PORT));
			
			// Buffer creation
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			
			// Constructing packet
			NetworkUtils.putOperation(buffer, Operation.LOGIN);
			// Data
			NetworkUtils.putString(buffer, username);
			NetworkUtils.putString(buffer, password);
			// End of packet
			NetworkUtils.end(buffer);

			buffer.flip();
			socketChannel.write(buffer);
			
			// Receive response
			buffer.clear();
			buffer.rewind();
			socketChannel.read(buffer);
			buffer.flip();
			
			if(NetworkUtils.getOperation(buffer) != Operation.LOGIN) {
				throw new Exception("Communication error");
			}
			if(NetworkUtils.getBoolean(buffer)) {
				return true;
			}
			message_info = NetworkUtils.getString(buffer);
			System.out.println(message_info);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getMessage() {
		return message_info;
	}
	
}
