package dev.leonardini.worth.client.networking;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.List;

import dev.leonardini.worth.client.ui.UsersPanel;
import dev.leonardini.worth.networking.UsersChangeNotification;

public class UsersChangeUpdater extends RemoteObject implements UsersChangeNotification {

	private static final long serialVersionUID = 7029744550636542835L;
	private final UsersPanel panel;
	
	public UsersChangeUpdater(UsersPanel panel) throws RemoteException {
		super();
		this.panel = panel;
	}
	
	@Override
	public void notifyChange(List<String> online_users, List<String> offline_users) throws RemoteException {
		for(String s : online_users) {
			panel.setOnline(s);
		}
		for(String s : offline_users) {
			panel.setOffline(s);
		}
		panel.reload();
	}

}
