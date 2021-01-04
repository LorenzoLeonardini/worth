package dev.leonardini.worth.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import dev.leonardini.worth.networking.ChatFallbackRegistration;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NotifyUsersChange;
import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.server.networking.ChatForwarder;
import dev.leonardini.worth.server.networking.UsersChangeNotifier;

/**
 * This abstract class is responsible for initializing all the RMI server endpoints.
 * 
 * Gives the possibility to retrieve UsersChangeNotifier and ChatForwarder objects.
 */
public abstract class RMIServer {
	
	public final static UsersChangeNotifier notifyUsersChange = new UsersChangeNotifier();
	public final static ChatForwarder chatForwarder = new ChatForwarder();
	
	/**
	 * Initialize the RMIServer. Call this function as soon as possible.
	 * 
	 * Since the UserManager is not responsibility of the RMI server, it needs to get passed
	 * as a parameter
	 * 
	 * @param userManager
	 */
	public static void init(UserManager userManager) {
		try {
			LocateRegistry.createRegistry(NetworkUtils.REGISTRY_PORT);
			Registry r = LocateRegistry.getRegistry(NetworkUtils.REGISTRY_PORT);
			
			initRegistration(r, userManager);
			initNotifier(r);
			initChatFallback(r);
			Logger.Trace("RMI Registry running on port " + NetworkUtils.REGISTRY_PORT);
		}
		catch (RemoteException e) {
			Logger.Error(e.getMessage());
			System.exit(1);
		}
	}
	
	private static void initRegistration(Registry r, UserManager user_manager) throws RemoteException {
		UserRegistration stub = (UserRegistration) UnicastRemoteObject.exportObject(user_manager, 0);
		r.rebind(NetworkUtils.USER_REGISTRATION, stub);
	}
	
	private static void initNotifier(Registry r) throws RemoteException {
		NotifyUsersChange stub = (NotifyUsersChange) UnicastRemoteObject.exportObject(notifyUsersChange, 0);
		r.rebind(NetworkUtils.USER_STATUS_NOTIFICATION, stub);
	}
	
	private static void initChatFallback(Registry r) throws RemoteException {
		ChatFallbackRegistration stub = (ChatFallbackRegistration) UnicastRemoteObject.exportObject(chatForwarder, 0);
		r.rebind(NetworkUtils.CHAT_FALLBACK, stub);
	}
	
}
