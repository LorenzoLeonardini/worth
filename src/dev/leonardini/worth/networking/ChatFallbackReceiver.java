package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

import dev.leonardini.worth.data.Project.CardLocation;

public interface ChatFallbackReceiver extends Remote {

	public void receiveMessage(long timestamp, String project, String username, String message) throws RemoteException;
	public void receiveSystem(long timestamp, String project, String card, String user, CardLocation from, CardLocation to) throws RemoteException;
	
}
