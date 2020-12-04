package dev.leonardini.worth.server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.Map;

import dev.leonardini.worth.networking.UserRegistration;

public class UserManager extends RemoteServer implements UserRegistration {

	private static final long serialVersionUID = -4247880533992442369L;
	
	private Map<String, String> users = new HashMap<String, String>();

	@Override
	public String register(String username, String password) throws RemoteException, InvalidRegistrationException {
		if(username == null || password == null)
			throw new NullPointerException();
		username = username.trim().toLowerCase();
		if(username.length() < 4)
			throw new InvalidRegistrationException("Username troppo corto. Almeno 4 caratteri");
		if(password.length() < 6)
			throw new InvalidRegistrationException("Password troppo corta. Almeno 6 caratteri");
		
		synchronized(users) {
			if(users.containsKey(username))
				throw new InvalidRegistrationException("Username già in uso");
			users.put(username, password);
		}
		
		return "Registrazione avvenuta con successo";
	}
	
	public boolean login(String username, String password) {
		return users.containsKey(username) && users.get(username).equals(password);
	}

}
