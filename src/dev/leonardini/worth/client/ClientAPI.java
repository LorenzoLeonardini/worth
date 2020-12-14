package dev.leonardini.worth.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.leonardini.worth.client.networking.UsersChangeUpdater;
import dev.leonardini.worth.client.ui.assets.PropicManager;
import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.data.Project.CardLocation;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.NotifyUsersChange;
import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.networking.UserRegistration.InvalidRegistrationException;
import dev.leonardini.worth.networking.UsersChangeNotification;
import dev.leonardini.worth.networking.WorthBuffer;

public class ClientAPI {
	
	private String host;
	private Registry registry;
	private String message_info = "";
	private SocketChannel socketChannel = null;
	private UsersChangeNotification stub;
	
	private Map<String, Boolean> users;
	
	public ClientAPI() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if(socketChannel != null)
				logout();
			if(stub != null)
				unregisterUsersRMI();
		}));
	}

	public boolean estabilish(String host) {
		if(this.host == null || !this.host.equalsIgnoreCase(host)) {
			if(socketChannel != null) {
				try {
					logout();
					socketChannel.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			socketChannel = null;
		}
		this.host = host;
		try {
			registry = LocateRegistry.getRegistry(host, NetworkUtils.REGISTRY_PORT);
		} catch (RemoteException e) {
			this.host = null;
			this.registry = null;
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean register(String username, String password) {
		try {
			UserRegistration registration_service;
			registration_service = (UserRegistration) registry.lookup(NetworkUtils.USER_REGISTRATION);
			String msg = registration_service.register(username, password);
			message_info = msg;
			return true;
		} catch(RemoteException | InvalidRegistrationException ex) {
			if(ex instanceof InvalidRegistrationException)
				message_info = ex.getMessage();
			else
				message_info = "Errore di connessione";
			return false;
		} catch (Exception e) {
			message_info = "Errore di connessione";
			return false;
		}
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean login(String username, String password) {
		try {
			if(socketChannel == null)
				socketChannel = SocketChannel.open(new InetSocketAddress(host, NetworkUtils.SERVER_PORT));
		}
		catch (IOException e) {
			message_info = "Errore di connessione";
			e.printStackTrace();
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.LOGIN, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(username);
		buffer.putString(password);
		
		if(comm.send()) { // Login outcome
			this.users = new HashMap<String, Boolean>();
			WorthBuffer users = new WorthBuffer(buffer.getArray());
			while(users.hasRemaining()) {
				String u = users.getString();
				this.users.put(u, users.getBoolean());
				String hash = users.getString();
				if(hash.length() != 0) {
					PropicManager.addPropic(u, hash);
				}
			}
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean logout() {
		try {
			WorthBuffer buffer = new WorthBuffer();
			buffer.putOperation(Operation.LOGOUT);
			buffer.end();
			buffer.write(socketChannel);
			socketChannel.close();
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean registerUsersRMI(UserUpdateCallback callback) {
		try {
			UsersChangeUpdater usersChangeUpdater = new UsersChangeUpdater(callback);
			stub = (UsersChangeNotification) UnicastRemoteObject.exportObject(usersChangeUpdater, 0);
			NotifyUsersChange notification_service = (NotifyUsersChange) registry.lookup(NetworkUtils.USER_STATUS_NOTIFICATION);
			notification_service.registerForCallback(stub);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean unregisterUsersRMI() {
		try {
			NotifyUsersChange notification_service;
			notification_service = (NotifyUsersChange) registry.lookup(NetworkUtils.USER_STATUS_NOTIFICATION);
			notification_service.unregisterForCallback(stub);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean updateProfilePicture(String email) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.CHANGE_PROPIC, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(email);
		
		if(comm.send()) {
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}

	public Map<String, Boolean> listUsers() {
		return new HashMap<String, Boolean>(this.users);
	}
	
	public List<String> listOnlineUsers() {
		List<String> list = new ArrayList<String>();
		for(String user : users.keySet()) {
			if(users.get(user))
				list.add(user);
		}
		return list;
	}
	
	public List<String> listProjects() {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return null;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.LIST_PROJECTS, socketChannel);

		if(comm.send()) {
			WorthBuffer buffer = comm.getBuffer();
			int amount = buffer.getInt();
			List<String> projects = new ArrayList<String>(amount);
			for(int i = 0; i < amount; i++) {
				projects.add(buffer.getString());
			}
			return projects;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return null;
	}
	
	public boolean createProject(String projectName) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.CREATE_PROJECT, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);

		if(comm.send()) {
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
	public List<String> showCards(String projectName) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return null;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.SHOW_CARDS, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);

		if(comm.send()) {
			int len = buffer.getInt();
			List<String> cards = new ArrayList<String>(len);
			for(int i = 0; i < len; i++) {
				cards.add(buffer.getString());
			}
			return cards;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return null;
	}
	
	public CardInfo showCard(String projectName, String card) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return null;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.SHOW_CARD, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);
		buffer.putString(card);

		if(comm.send()) {
			return new CardInfo(card, buffer.getString(), CardLocation.values()[buffer.getInt()]);
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return null;
	}
	
	public boolean addCard(String projectName, String cardName, String cardDescription) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.ADD_CARD, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);
		buffer.putString(cardName);
		buffer.putString(cardDescription);

		if(comm.send()) {
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
	public boolean moveCard(String projectName, String cardName, CardLocation src, CardLocation dst) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.MOVE_CARD, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);
		buffer.putString(cardName);
		buffer.putInt(src.ordinal());
		buffer.putInt(dst.ordinal());

		if(comm.send()) {
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
	public boolean cancelProject(String projectName, String password) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.DELETE_PROJECT, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);
		buffer.putString(password);

		if(comm.send()) {
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
	public String getMessage() {
		return message_info;
	}

	public List<String> showMembers(String projectName) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return null;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.SHOW_MEMBERS, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);

		if(comm.send()) {
			int amount = buffer.getInt();
			List<String> members = new ArrayList<String>(amount);
			for(int i = 0; i < amount; i++) {
				members.add(buffer.getString());
			}
			return members;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return null;
	}
	
	public boolean addMember(String projectName, String username) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		username = username.trim();
		if(username.length() < 4) {
			message_info = "";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.ADD_MEMBER, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putString(projectName);
		buffer.putString(username);

		if(comm.send()) {
			return true;
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
}
