package dev.leonardini.worth.server.networking;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.List;

import dev.leonardini.worth.networking.NotifyUsersChange;
import dev.leonardini.worth.networking.UsersChangeNotification;

public class UsersChangeNotifier extends RemoteObject implements NotifyUsersChange {

	private static final long serialVersionUID = -789172292414787069L;
	private List<UsersChangeNotification> clients;
	
	public UsersChangeNotifier() throws RemoteException {
		super();
		clients = new ArrayList<UsersChangeNotification>();
	}

	@Override
	public void registerForCallback(UsersChangeNotification callback) throws RemoteException {
		if(!clients.contains(callback)) {
			clients.add(callback);
		}
	}

	@Override
	public void unregisterForCallback(UsersChangeNotification callback) throws RemoteException {
		clients.remove(callback);
	}
	
	public void updateClients(String username, boolean status) throws RemoteException {
		for(UsersChangeNotification c : clients) {
			c.notifyChange(username, status);
		}
	}
	
	public void updatePropic(String username, String hash) throws RemoteException {
		for(UsersChangeNotification c : clients) {
			c.notifyPropicChange(username, hash);
		}
	}
	
}
