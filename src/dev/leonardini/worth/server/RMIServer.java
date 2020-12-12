package dev.leonardini.worth.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NotifyUsersChange;
import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.server.networking.UsersChangeNotifier;

public class RMIServer {
	
	public static UsersChangeNotifier notifyUsersChange;
	
	public static void init(UserManager userManager) {
		try {
			LocateRegistry.createRegistry(NetworkUtils.REGISTRY_PORT);
			Registry r = LocateRegistry.getRegistry(NetworkUtils.REGISTRY_PORT);
			
			
			initRegistration(r, userManager);
			initNotifier(r);
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
		notifyUsersChange = new UsersChangeNotifier(); 
		NotifyUsersChange stub = (NotifyUsersChange) UnicastRemoteObject.exportObject(notifyUsersChange, 0);
		r.rebind(NetworkUtils.USER_STATUS_NOTIFICATION, stub);
	}
	
}
