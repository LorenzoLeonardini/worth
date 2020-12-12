package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UsersChangeNotification extends Remote {

	public void notifyChange(String username, boolean status) throws RemoteException;
	public void notifyPropicChange(String username, String hash) throws RemoteException;
	
}
