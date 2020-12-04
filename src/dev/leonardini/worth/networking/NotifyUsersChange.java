package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyUsersChange extends Remote {

	public void registerForCallback(UsersChangeNotification callback) throws RemoteException;
	public void unregisterForCallback(UsersChangeNotification callback) throws RemoteException;
	
}
