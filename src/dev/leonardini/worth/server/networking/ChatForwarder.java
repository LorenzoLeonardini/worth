package dev.leonardini.worth.server.networking;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.List;

import dev.leonardini.worth.data.Project.CardLocation;
import dev.leonardini.worth.networking.ChatFallbackReceiver;
import dev.leonardini.worth.networking.ChatFallbackRegistration;

public class ChatForwarder extends RemoteObject implements ChatFallbackRegistration {

	private static final long serialVersionUID = -789172292414787069L;
	private List<ChatFallbackReceiver> clients;
	
	public ChatForwarder() throws RemoteException {
		super();
		clients = new ArrayList<ChatFallbackReceiver>();
	}

	@Override
	public void registerForCallback(ChatFallbackReceiver callback) throws RemoteException {
		if(!clients.contains(callback)) {
			clients.add(callback);
		}
	}

	@Override
	public void unregisterForCallback(ChatFallbackReceiver callback) throws RemoteException {
		clients.remove(callback);
	}
	
	public void send(long timestamp, String project, String username, String message) {
		for(ChatFallbackReceiver client : clients)
			try {
				client.receiveMessage(timestamp, project, username, message);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
	}
	
	public void send(long timestamp, String project, String card, String user, CardLocation from, CardLocation to) throws RemoteException {
		for(ChatFallbackReceiver client : clients)
			client.receiveSystem(timestamp, project, card, user, from, to);
	}
	
}
