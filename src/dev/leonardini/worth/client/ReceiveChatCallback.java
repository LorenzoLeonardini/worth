package dev.leonardini.worth.client;

import dev.leonardini.worth.data.Project.CardLocation;

public interface ReceiveChatCallback {

	public void receivedChatMessage(String username, String message);
	public void receivedSystemNotification(String user, String card, CardLocation from, CardLocation to);
	
}
