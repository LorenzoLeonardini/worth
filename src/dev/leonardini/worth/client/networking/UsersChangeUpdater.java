package dev.leonardini.worth.client.networking;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import dev.leonardini.worth.client.UserUpdateCallback;
import dev.leonardini.worth.client.gui.assets.PropicManager;
import dev.leonardini.worth.networking.UsersChangeNotification;

/**
 * This class implements the UsersChangeNotification interface, used as a stub
 * to receive all the updates about user status and propic changes from the server
 * 
 * This class is used internally by ClientAPI, however this class register a local
 * callback (UserUpdateCallback) which can be used by the "frontend" to interpret all
 * these changes.
 */
public class UsersChangeUpdater extends RemoteObject implements UsersChangeNotification {

	private static final long serialVersionUID = 7029744550636542835L;
	private final UserUpdateCallback callback;
	
	/**
	 * @param callback
	 */
	public UsersChangeUpdater(UserUpdateCallback callback) {
		super();
		this.callback = callback;
	}
	
	@Override
	public void notifyChange(String username, boolean status) throws RemoteException {
		callback.updateUserStatus(username, status);
	}

	@Override
	public void notifyPropicChange(String username, String hash) throws RemoteException {
		PropicManager.addPropic(username, hash);
		callback.updateUserPropic(username);
	}

}
