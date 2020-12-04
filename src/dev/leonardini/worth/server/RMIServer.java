package dev.leonardini.worth.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.UserRegistration;

public class RMIServer {
	
	public static void init(UserManager userManager) {
		try {
			LocateRegistry.createRegistry(NetworkUtils.REGISTRY_PORT);
			Registry r = LocateRegistry.getRegistry(NetworkUtils.REGISTRY_PORT);
			
			
			initRegistration(r, userManager);
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
	
}
