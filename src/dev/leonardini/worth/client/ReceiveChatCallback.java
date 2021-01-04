package dev.leonardini.worth.client;

import dev.leonardini.worth.server.data.Project.CardLocation;

/**
 * This is a callback used to listen to chat messages.
 */
public interface ReceiveChatCallback {

	public void receivedChatMessage(String username, String message);
	public void receivedSystemNotification(String user, String card, CardLocation from, CardLocation to);
	
}
