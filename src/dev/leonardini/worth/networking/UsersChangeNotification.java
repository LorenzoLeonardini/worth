package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface UsersChangeNotification extends Remote {

	public void notifyChange(List<String> online_users, List<String> offline_users) throws RemoteException;
	
}
