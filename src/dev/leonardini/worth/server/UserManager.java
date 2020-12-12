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

import dev.leonardini.worth.data.User;
import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.networking.WorthBuffer;

public class UserManager extends RemoteServer implements UserRegistration {

	private static final long serialVersionUID = -4247880533992442369L;

	private Map<String, User> users = new HashMap<String, User>();
	private String filename;
	private Map<String, Boolean> user_status = new HashMap<String, Boolean>();

	public UserManager(String filename) {
		this.filename = filename;
		Logger.Trace("Reading user db file");
		try {
			ReadableByteChannel file = Channels.newChannel(new FileInputStream(filename));
			WorthBuffer db = new WorthBuffer(1024, true);
			while(db.read(file) != -1) {
				while(db.canGetArray()) {
					User u = new User(db.getArray());
					Logger.Trace("Loaded " + u.getUsername());
					users.put(u.getUsername(), u);
					user_status.put(u.getUsername(), false);
				}
				db.compact();
			}
			file.close();
			Logger.Trace("Loaded " + users.size() + " users");
		} catch(FileNotFoundException e) {
			Logger.Trace("No user db file");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Error("Error reading user db file");
		}
	}

	@Override
	public String register(String username, String password) throws RemoteException, InvalidRegistrationException {
		Logger.Trace("Registration request");
		if (username == null || password == null)
			throw new NullPointerException();
		username = username.trim().toLowerCase();
		if (username.length() < 4)
			throw new InvalidRegistrationException("Username troppo corto. Almeno 4 caratteri");
		if (password.length() < 6)
			throw new InvalidRegistrationException("Password troppo corta. Almeno 6 caratteri");

		User user;
		synchronized (users) {
			if (users.containsKey(username))
				throw new InvalidRegistrationException("Username già in uso");
			user = new User(username, password);
			users.put(username, user);
		}
		synchronized (user_status) {
			updateUserStatus(username, false);
		}
		saveUserToFile(user);

		return "Registrazione avvenuta con successo";
	}

	public boolean login(String username, String password) throws Exception {
		boolean response;
		synchronized(user_status) {
			if(user_status.containsKey(username) && user_status.get(username))
				throw new Exception("Already logged in");
		}
		synchronized (users) {
			response = users.containsKey(username) && users.get(username).checkPassword(password);
		}
		if(response) {
			synchronized(user_status) {
				updateUserStatus(username, true);
			}
		}
		return response;
	}
	
	public void logout(String username) {
		if(username == null) return;
		updateUserStatus(username, false);
		Logger.Trace(username + " logged out");
	}
	
	private void updateUserStatus(String username, boolean status) {
		synchronized(user_status) {
			user_status.put(username, status);
			try {
				RMIServer.notifyUsersChange.updateClients(username, status);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public WorthBuffer getUsersStatus() {
		WorthBuffer buffer = new WorthBuffer(1024);
		synchronized (user_status) {
			for(String user : user_status.keySet()) {
				buffer.putString(user);
				buffer.putBoolean(user_status.get(user));
				String hash = users.get(user).getMailHash();
				buffer.putString(hash != null ? hash : "");
			}
		}
		return buffer;
	}
	
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
	private void saveUserToFile(User user) {
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
	public void saveToFile() {
		synchronized (filename) {
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
	}

	public boolean exists(String username) {
		synchronized (users) {
			return users.containsKey(username);
		}
	}

}
