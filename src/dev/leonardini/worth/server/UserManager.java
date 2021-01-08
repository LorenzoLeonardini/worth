package dev.leonardini.worth.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.Map;

import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.networking.WorthBuffer;
import dev.leonardini.worth.server.data.User;

/**
 * This singleton class manages all the user for the application. It is responsible
 * for registration, queries, file saves/loads.
 */
public class UserManager extends RemoteServer implements UserRegistration {

	private static final long serialVersionUID = -4247880533992442369L;

	private Map<String, User> users = new HashMap<String, User>();
	private String filename;
	private Map<String, Boolean> user_status = new HashMap<String, Boolean>();
	
	private static UserManager instance = null;
	
	/**
	 * Initialize user manager, loading user db from filename
	 * 
	 * @param filename
	 */
	private UserManager(String filename) {
		this.filename = filename;
		Logger.Log("Reading user db file");
		try {
			ReadableByteChannel file = Channels.newChannel(new FileInputStream(filename));
			WorthBuffer db = new WorthBuffer(1024, true);
			while(db.read(file) != -1) {
				while(db.canGetArray()) {
					User u = new User(db.getArray());
					users.put(u.getUsername(), u);
					user_status.put(u.getUsername(), false);
				}
				db.compact();
			}
			file.close();
			Logger.Log("Loaded " + users.size() + " users");
		} catch(FileNotFoundException e) {
			Logger.Log("No user db file");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Error("Error reading user db file");
		}
	}
	
	/**
	 * Retrieve the singleton instance of the UserManager
	 * @throws NullPointerException if instance is not initialized with `init()`
	 */
	public static UserManager get() {
		if(instance == null) throw new NullPointerException();
		return instance;
	}

	/**
	 * Initialize singleton instance, loading user db from filename
	 * @param filename
	 */
	public static void init(String filename) {
		instance = new UserManager(filename);
	}

	@Override
	/**
	 * Register a user. Can throw a number of exceptions related to the invalidity of input data
	 * (i.e. password length)
	 */
	public String register(String username, String password) throws RemoteException, InvalidRegistrationException {
		Logger.Log("Registration request");
		if (username == null || password == null)
			throw new NullPointerException();
		username = username.trim().toLowerCase();
		if (username.length() < 4)
			throw new InvalidRegistrationException("Username troppo corto. Almeno 4 caratteri");
		if (password.length() < 6)
			throw new InvalidRegistrationException("Password troppo corta. Almeno 6 caratteri");
		if (username.equalsIgnoreCase("system") || username.equalsIgnoreCase("server"))
			throw new InvalidRegistrationException("Username riservato");

		User user;
		synchronized (users) {
			if (users.containsKey(username))
				throw new InvalidRegistrationException("Username già in uso");
			user = new User(username, password);
			users.put(username, user);
		}
		updateUserStatus(username, false);
		saveUserToFile(user);

		return "Registrazione avvenuta con successo";
	}

	/**
	 * Request a user login. When succeeding, all other users are updated
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception if user already logged
	 */
	public boolean login(String username, String password) throws Exception {
		synchronized(user_status) {
			if(user_status.containsKey(username) && user_status.get(username))
				throw new Exception("Utente già connesso");
		}
		boolean response = securityLogin(username, password);
		if(response) {
			updateUserStatus(username, true);
		}
		return response;
	}
	
	/**
	 * Check if username-password pair is valid. Can be used for logging in or for
	 * checking password in case of 'sudo mode' (deleting projects)
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean securityLogin(String username, String password) {
		synchronized (users) {
			return users.containsKey(username) && users.get(username).checkPassword(password);
		}
	}
	
	public void logout(String username) {
		Logger.Log("LOGGING OUT " + username);
		if(username == null) return;
		updateUserStatus(username, false);
		Logger.Log(username + " logged out");
	}
	
	/**
	 * Update internal data structure with the new user status. Send RMI notification
	 * to all connected users to inform them of the change.
	 * 
	 * @param username
	 * @param status offline/online
	 */
	private void updateUserStatus(String username, boolean status) {
		synchronized(user_status) {
			user_status.put(username, status);
		}
		try {
			RMIServer.notifyUsersChange.updateClients(username, status);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return a map associating all users to their current status (online/offline)
	 */
	public Map<String, Boolean> getUsersStatusMap() {
		synchronized (user_status) {
			return new HashMap<String, Boolean>(user_status);
		}
	}
	
	/**
	 * Retrieve user status information in the form of a WorthBuffer.
	 * This is used when a user logs in, to inform them of all users and their propic
	 * info
	 */
	public WorthBuffer getUsersStatus() {
		WorthBuffer buffer = new WorthBuffer();
		synchronized (users) {
			synchronized (user_status) {
				for(String user : user_status.keySet()) {
					buffer.putString(user);
					buffer.putBoolean(user_status.get(user));
					String hash = users.get(user).getMailHash();
					buffer.putString(hash != null ? hash : "");
				}
			}
		}
		return buffer;
	}
	
	/**
	 * Update a user profile picture data
	 * 
	 * @param username
	 * @param email the email linked to the gravatar image
	 */
	public void updateProfilePicture(String username, String email) {
		String hash;
		synchronized (users) {
			users.get(username).setEmail(email);
			hash = users.get(username).getMailHash();
		}
		try {
			RMIServer.notifyUsersChange.updatePropic(username, hash);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * When a user is registered its information is appended to the userdb file
	 * This allows to have all sensitive and important data stored on the disk,
	 * even if the application suddenly crashes the main user data don't get lost
	 */
	private synchronized void saveUserToFile(User user) {
		synchronized(filename) {
			try {
				WritableByteChannel file = Channels.newChannel(new FileOutputStream(filename, true));
				WorthBuffer db = new WorthBuffer(1024, true);
				db.put(user.toByteArray());
				while(db.hasRemaining()) {
					db.write(file);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When the application exits, all user data is rewritten to the
	 * userdb file. This allows saving less sensitive data without the need for
	 * large computations and file parsing.
	 */
	public synchronized void saveToFile() {
		try {
			WritableByteChannel file = Channels.newChannel(new FileOutputStream(filename));
			WorthBuffer db = new WorthBuffer(1024, true);
			for(User u : users.values()) {
				byte arr[] = u.toByteArray();
				while(arr.length + Integer.BYTES > db.remaining()) {
					db.write(file);
					db.compact();
				}
				db.put(arr);
			}
			while(db.hasRemaining()) {
				db.write(file);
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean exists(String username) {
		synchronized (users) {
			return users.containsKey(username);
		}
	}
	
	/**
	 * If provided user does not exist, an exception is thrown
	 * @param username
	 */
	public void checkExistance(String username) throws Exception {
		if(!exists(username))
			throw new Exception("L'utente non esiste");
	}

}
