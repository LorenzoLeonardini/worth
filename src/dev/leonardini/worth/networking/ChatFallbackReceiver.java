package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

import dev.leonardini.worth.data.CardLocation;

/**
 * Local callback for chat fallback. Used to receive chat messages from the server when
 * not all the users are reachable with UDP Multicast
 */
public interface ChatFallbackReceiver extends Remote {

	public void receiveMessage(long timestamp, String project, String username, String message) throws RemoteException;
	public void receiveSystem(long timestamp, String project, String card, String user, CardLocation from, CardLocation to) throws RemoteException;
	
}
