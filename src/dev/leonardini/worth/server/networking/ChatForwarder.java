package dev.leonardini.worth.server.networking;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.List;

import dev.leonardini.worth.data.CardLocation;
import dev.leonardini.worth.networking.ChatFallbackReceiver;
import dev.leonardini.worth.networking.ChatFallbackRegistration;

/**
 * This class implements the ChatFallbackRegistration interface to manage chat messages
 * when some users are not reachable via UDP Multicast. Allows to register remote RMI
 * callbacks and to send them chat messages from the server
 */
public class ChatForwarder extends RemoteObject implements ChatFallbackRegistration {

	private static final long serialVersionUID = -789172292414787069L;
	private List<ChatFallbackReceiver> clients;
	
	public ChatForwarder() {
		super();
		clients = new ArrayList<ChatFallbackReceiver>();
	}

	@Override
	public synchronized void registerForCallback(ChatFallbackReceiver callback) throws RemoteException {
		if(!clients.contains(callback)) {
			clients.add(callback);
		}
	}

	@Override
	public synchronized void unregisterForCallback(ChatFallbackReceiver callback) throws RemoteException {
		clients.remove(callback);
	}
	
	public synchronized void send(long timestamp, String project, String username, String message) throws RemoteException {
		for(ChatFallbackReceiver client : clients)
			client.receiveMessage(timestamp, project, username, message);
	}
	
	public synchronized void send(long timestamp, String project, String card, String user, CardLocation from, CardLocation to) throws RemoteException {
		for(ChatFallbackReceiver client : clients)
			client.receiveSystem(timestamp, project, card, user, from, to);
	}
	
}
