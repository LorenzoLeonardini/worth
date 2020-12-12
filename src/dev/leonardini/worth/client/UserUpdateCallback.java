package dev.leonardini.worth.client;

public interface UserUpdateCallback {

	public void updateUserStatus(String username, boolean status);
	public void updateUserPropic(String username);
	
}
