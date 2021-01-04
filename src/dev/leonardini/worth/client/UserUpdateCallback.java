package dev.leonardini.worth.client;

/**
 * This is a callback used to get users updates from ClientAPI
 */
public interface UserUpdateCallback {

	public void updateUserStatus(String username, boolean status);
	public void updateUserPropic(String username);
	
}
