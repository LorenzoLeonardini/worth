package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote object to register chat fallback callbacks
 */
public interface ChatFallbackRegistration extends Remote {

	public void registerForCallback(ChatFallbackReceiver callback) throws RemoteException;
	public void unregisterForCallback(ChatFallbackReceiver callback) throws RemoteException;
	
}
