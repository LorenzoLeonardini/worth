package dev.leonardini.worth.server;

import java.rmi.RemoteException;

import dev.leonardini.worth.data.CardLocation;

public class ServerChat {

	protected void sendChatNotification(String projectName, String card, String user, CardLocation from, CardLocation to) {
		try {
			RMIServer.chatForwarder.send(System.currentTimeMillis(), projectName, card, user, from, to);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		} 
	}
	
	
}
