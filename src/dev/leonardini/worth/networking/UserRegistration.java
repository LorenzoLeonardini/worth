package dev.leonardini.worth.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote RMI interface for user registration
 */
public interface UserRegistration extends Remote {

	public String register(String username, String password) throws RemoteException, InvalidRegistrationException;
	
	class InvalidRegistrationException extends Exception {
		private static final long serialVersionUID = -4235294810926720493L;
		public InvalidRegistrationException(String s) {
			super(s);
		}
	}
	
}
