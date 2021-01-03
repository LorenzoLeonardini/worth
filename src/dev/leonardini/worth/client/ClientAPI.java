package dev.leonardini.worth.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.leonardini.worth.client.gui.assets.PropicManager;
import dev.leonardini.worth.client.networking.UsersChangeUpdater;
import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.data.Project.CardLocation;
import dev.leonardini.worth.networking.ChatFallbackReceiver;
import dev.leonardini.worth.networking.ChatFallbackRegistration;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.NotifyUsersChange;
import dev.leonardini.worth.networking.UserRegistration;
import dev.leonardini.worth.networking.UserRegistration.InvalidRegistrationException;
import dev.leonardini.worth.networking.UsersChangeNotification;
import dev.leonardini.worth.networking.WorthBuffer;

/**
 * This class handles the communication with the server and provides
 * an API which implements the basic functions required by the assignment
 * 
 * Make sure to call `estabilish()` before registering and logging in
 */
public class ClientAPI implements UserUpdateCallback {
	
	private String host;
	private Registry registry;
	private String info_message = "";
	private SocketChannel socketChannel = null;
	private UsersChangeNotification usersStub;
	private ChatFallbackReceiver chatStub;
	private ClientChatAPI chat;
	private String username = null;
	private Set<UserUpdateCallback> stubSet = new HashSet<UserUpdateCallback>();
	
	private Map<String, Boolean> users;
	
	private static ClientAPI instance = null;
	
	/**
	 * This class makes sense only as a singleton
	 */
	private static void init() {
		instance = new ClientAPI();
	}
	
	/**
	 * @return ClientAPI singleton instance
	 */
	public static ClientAPI get() {
		if(instance == null) init();
		return instance;
	}
	
	/**
	 * private constructor to enforce singleton
	 */
	private ClientAPI() {
		// Set system property to allow using client and server on different machines
		System.setProperty("java.rmi.server.hostname", NetworkUtils.getPrivateIp());
		
		// Setup shutdown cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if(socketChannel != null)
				logout();
			chat.cleanUp();
			unregisterRMIs();
		}));
		
		// Initialize chat api
		chat = new ClientChatAPI();
	}

	/**
	 * Initialize all the connections to the host. This is necessary both before
	 * registering and logging in the user.
	 * 
	 * @param host server address
	 * @return outcome
	 */
	public boolean estabilish(String host) {
		// Cleanup TCP connection
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
		
		// Init RMI registry
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
	 * Register user account with username and password.
	 * Note that after calling this method a message is set even with outcome == true
	 * 
	 * @param username
	 * @param password
	 * @return outcome
	 */
	public boolean register(String username, String password) {
		try {
			UserRegistration registration_service;
			registration_service = (UserRegistration) registry.lookup(NetworkUtils.USER_REGISTRATION);
			String msg = registration_service.register(username, password);
			info_message = msg;
			return true;
		} catch(RemoteException | InvalidRegistrationException e) {
			if(e instanceof InvalidRegistrationException)
				info_message = e.getMessage();
			else
				info_message = "Errore di connessione";
			return false;
		} catch (Exception e) {
			info_message = "Errore di connessione";
			return false;
		}
	}
	
	/**
	 * Login a user. This is necessary before doing anything, since a stateful connection
	 * is estabilished with the server.
	 * 
	 * @param username
	 * @param password
	 * @return outcome
	 */
	public boolean login(String username, String password) {
		// Init TCP connection
		try {
			if(socketChannel == null)
				socketChannel = SocketChannel.open(new InetSocketAddress(host, NetworkUtils.SERVER_PORT));
		}
		catch (IOException e) {
			info_message = "Errore di connessione";
			e.printStackTrace();
			return false;
		}
		
		return serverCommunicationBoolean(Operation.LOGIN,
		(buffer) -> { // send
			buffer.putString(username);
			buffer.putString(password);
		}, (buffer) -> { // receive
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
			// Setup variables and register RMI callbacks
			this.username = username;
			registerRMIs();
			
			// Discover which clients are available with multicast
			chat.multicastDiscovery(username, this.users);
			
			return true;
		});
	}
	
	/**
	 * Logout, clean up connection etc
	 * @return outcome
	 */
	private boolean logout() {
		try {
			WorthBuffer buffer = new WorthBuffer();
			buffer.putOperation(Operation.LOGOUT);
			buffer.write(socketChannel);
			socketChannel.close();
			username = null;
			return true;
		} catch(IOException e) {
			info_message = e.getMessage();
			return false;
		}
	}
	
	/**
	 * Register a UserUpdateCallback
	 * @param callback
	 */
	public void registerUsersRMI(UserUpdateCallback callback) {
		stubSet.add(callback);
	}
	
	/**
	 * Registers RMIs callback for user status updates and chat messages.
	 * @return outcome
	 */
	private boolean registerRMIs() {
		try {
			UsersChangeUpdater usersChangeUpdater = new UsersChangeUpdater(this);
			usersStub = (UsersChangeNotification) UnicastRemoteObject.exportObject(usersChangeUpdater, 0);
			NotifyUsersChange notification_service = (NotifyUsersChange) registry.lookup(NetworkUtils.USER_STATUS_NOTIFICATION);
			notification_service.registerForCallback(usersStub);
			
			chatStub = (ChatFallbackReceiver) UnicastRemoteObject.exportObject(chat, 0);
			ChatFallbackRegistration chat_service = (ChatFallbackRegistration) registry.lookup(NetworkUtils.CHAT_FALLBACK);
			chat_service.registerForCallback(chatStub);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unegisters RMI callbacks
	 * @return outcome
	 */
	private boolean unregisterRMIs() {
		try {
			if(usersStub != null) {
				NotifyUsersChange notification_service;
				notification_service = (NotifyUsersChange) registry.lookup(NetworkUtils.USER_STATUS_NOTIFICATION);
				notification_service.unregisterForCallback(usersStub);
			}
			
			if(chatStub != null) {
				ChatFallbackRegistration chat_service = (ChatFallbackRegistration) registry.lookup(NetworkUtils.CHAT_FALLBACK);
				chat_service.unregisterForCallback(chatStub);
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Update user gravatar email to set a new profile picture.
	 * 
	 * @param email new gravatar profile
	 * @return outcome
	 */
	public boolean updateProfilePicture(String email) {
		return serverCommunicationBoolean(Operation.CHANGE_PROPIC,
		(buffer) -> { // send
			buffer.putString(email);
		});
	}

	/**
	 * @return all system users with their "logged" status
	 */
	public Map<String, Boolean> listUsers() {
		return new HashMap<String, Boolean>(this.users);
	}
	
	/**
	 * @return a list of online users extracted from the listUsers() map
	 */
	public List<String> listOnlineUsers() {
		List<String> list = new ArrayList<String>();
		for(String user : users.keySet()) {
			if(users.get(user))
				list.add(user);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Query the server for all the projects the current user is in
	 * 
	 * @return a list of strings defining the project names. null in case of error
	 */
	public List<String> listProjects() {
		return (List<String>) serverCommunicationObject(Operation.LIST_PROJECTS,
		(buffer) -> {}, (buffer) -> { // receive
			int amount = buffer.getInt();
			List<String> projects = new ArrayList<String>(amount);
			for(int i = 0; i < amount; i++) {
				projects.add(buffer.getString());
			}
			return projects;
		});
	}
	
	/**
	 * Create a new project. The user gets automatically added to the project
	 * 
	 * @param projectName
	 * @return outcome of the request
	 */
	public boolean createProject(String projectName) {
		return serverCommunicationBoolean(Operation.CREATE_PROJECT,
		(buffer) -> { // send
			buffer.putString(projectName);
		});
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Query the server for all the cards in a project
	 * 
	 * @param projectName
	 * @return a list of strings defining card names, null in case of error
	 */
	public List<String> showCards(String projectName) {
		return (List<String>) serverCommunicationObject(Operation.SHOW_CARDS,
		(buffer) -> { // send
			buffer.putString(projectName);
		}, (buffer) -> { // receive
			int len = buffer.getInt();
			List<String> cards = new ArrayList<String>(len);
			for(int i = 0; i < len; i++) {
				cards.add(buffer.getString());
			}
			return cards;
		});
	}

	/**
	 * Query the server for all the information regarding a card, i.e. name, description, location
	 * history is not included, see `getCardHistory`
	 * 
	 * @param projectName
	 * @param cardName
	 * @return CardInfo object, null in case of error
	 */
	public CardInfo showCard(String projectName, String cardName) {
		return (CardInfo) serverCommunicationObject(Operation.SHOW_CARD,
		(buffer) -> { // send
			buffer.putString(projectName);
			buffer.putString(cardName);
		}, (buffer) -> { // receive
			return new CardInfo(cardName, buffer.getString(), CardLocation.values()[buffer.getInt()]);
		});
	}
	
	/**
	 * Create a new card in the specified project
	 * 
	 * @param projectName
	 * @param cardName
	 * @param cardDescription
	 * 
	 * @return outcome
	 */
	public boolean addCard(String projectName, String cardName, String cardDescription) {
		return serverCommunicationBoolean(Operation.ADD_CARD,
		(buffer) -> { // send
			buffer.putString(projectName);
			buffer.putString(cardName);
			buffer.putString(cardDescription);
		});
	}
	
	/**
	 * Move a card from a list to another
	 * 
	 * @param projectName
	 * @param cardName
	 * @param src
	 * @param dst
	 * @return outcome
	 */
	public boolean moveCard(String projectName, String cardName, CardLocation src, CardLocation dst) {
		return serverCommunicationBoolean(Operation.MOVE_CARD,
		(buffer) -> { // send
			buffer.putString(projectName);
			buffer.putString(cardName);
			buffer.putInt(src.ordinal());
			buffer.putInt(dst.ordinal());
		});
	}
	
	/**
	 * Try to delete a project. Note that all cards must be in the "done" list in
	 * order to do that
	 * 
	 * @param projectName
	 * @param password
	 * 
	 * @return outcome
	 */
	public boolean cancelProject(String projectName, String password) {
		return serverCommunicationBoolean(Operation.DELETE_PROJECT,
		(buffer) -> { // send
			buffer.putString(projectName);
			buffer.putString(password);
		});
	}

	@SuppressWarnings("unchecked")
	/**
	 * Query the server for a list of all the members of a project
	 * 
	 * @param projectName
	 * @return a list of strings defining the users, null in case of error
	 */
	public List<String> showMembers(String projectName) {
		return (List<String>) serverCommunicationObject(Operation.SHOW_MEMBERS,
		(buffer) -> { // send
			buffer.putString(projectName);
		}, (buffer) -> { // receive
			int amount = buffer.getInt();
			List<String> members = new ArrayList<String>(amount);
			for(int i = 0; i < amount; i++) {
				members.add(buffer.getString());
			}
			return members;
		});
	}
	
	/**
	 * Add a user as a member of a project
	 * 
	 * @param projectName
	 * @param username the user to add
	 * @return outcome
	 */
	public boolean addMember(String projectName, String username) {
		return serverCommunicationBoolean(Operation.ADD_MEMBER,
		(buffer) -> { // send
			buffer.putString(projectName);
			buffer.putString(username);
		});
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Query the server for a project card's history
	 * 
	 * @param projectName
	 * @param cardName
	 * @return a list of strings describing the history of the card, null in case of error
	 */
	public List<String> getCardHistory(String projectName, String cardName) {
		return (List<String>) serverCommunicationObject(Operation.GET_CARD_HISTORY, 
		(buffer) -> {
			buffer.putString(projectName);
			buffer.putString(cardName);
		}, (buffer) -> {
			int length = buffer.getInt();
			List<String> history = new ArrayList<String>(length);
			for(int i = 0; i < length; i++) {
				long timestamp = buffer.getLong();
				String user = buffer.getString();
				CardLocation location = CardLocation.values()[buffer.getInt()];
				String date = new SimpleDateFormat("[dd/MM/yy HH:mm:ss] ").format(new Date(timestamp));
				history.add(date + user + " ha spostato la card in " + location);
			}
			return history;
		});
	}
	
	/**
	 * Tell ClientAPI to listen for chat messages for a specific project. All incoming
	 * messages will be passed to the given callback
	 * 
	 * @param projectName
	 * @param chatCallback
	 * @return outcome
	 */
	public boolean readChat(String projectName, ReceiveChatCallback chatCallback) {
		return chat.read(projectName, chatCallback);
	}
	
	/**
	 * Tell ClientAPI to stop listening for chat messages for a specific project.

	 * @return outcome
	 */
	public boolean exitChat() {
		return chat.stop();
	}
	
	/**
	 * Send a message on a project chat
	 * 
	 * @param projectName
	 * @param message
	 * @return outcome
	 */
	public boolean sendChatMsg(String projectName, String message) {
		return chat.send(socketChannel, username, message);
	}

	@Override
	public void updateUserStatus(String username, boolean status) {
		for(UserUpdateCallback callback : stubSet)
			callback.updateUserStatus(username, status);
		
		chat.updateUserStatus(username, status);
	}

	@Override
	public void updateUserPropic(String username) {
		for(UserUpdateCallback callback : stubSet)
			callback.updateUserPropic(username);
	}

	/**
	 * Get the info message retrieved from the server regarding the last operation
	 * Most commonly used for error messages
	 * 
	 * @return message
	 */
	public String getMessage() {
		return info_message;
	}
	
	/**
	 * Helper function to make the code slimmer
	 * 
	 * @param operation
	 * @param write what to write to the buffer
	 * @return outcome
	 */
	private boolean serverCommunicationBoolean(Operation op, BufferWrite write) {
		return serverCommunicationBoolean(op, write, (buffer) -> {return true;});
	}
	
	private boolean busy = false;
	
	private synchronized void makeBusy() {
		try {
			while(busy) this.wait();
			busy = true;
		}
		catch (InterruptedException e) {
		}
	}
	
	private synchronized void unbusy() {
		busy = false;
		this.notifyAll();
	}
	
	/**
	 * Helper function to make the code slimmer
	 * 
	 * @param operation
	 * @param write what to write to the buffer
	 * @param read what to read from the buffer
	 * @return outcome
	 */
	private boolean serverCommunicationBoolean(Operation op, BufferWrite write, BufferReadBool read) {
		makeBusy();
		if(socketChannel == null) {
			info_message = "Nessuna connessione";
			unbusy();
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(op, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		write.run(buffer);
		
		if(comm.send()) {
			boolean ret = read.run(buffer);
			unbusy();
			return ret;
		}
		info_message = comm.getErrorMessage();
		System.out.println(info_message);
		unbusy();
		return false;
	}
	
	/**
	 * Helper function to make the code slimmer
	 * 
	 * @param operation
	 * @param write what to write to the buffer
	 * @param read what to read from the buffer
	 * @return result or null in case of error
	 */
	private Object serverCommunicationObject(Operation op, BufferWrite write, BufferReadObj read) {
		makeBusy();
		if(socketChannel == null) {
			info_message = "Nessuna connessione";
			unbusy();
			return null;
		}
		
		ServerCommunication comm = new ServerCommunication(op, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		write.run(buffer);

		if(comm.send()) {
			Object ret = read.run(buffer);
			unbusy();
			return ret;
		}
		info_message = comm.getErrorMessage();
		System.out.println(info_message);
		unbusy();
		return null;
	}
	
	@FunctionalInterface
	public interface BufferWrite {
		void run(WorthBuffer buffer);
	}
	
	@FunctionalInterface
	public interface BufferReadBool {
		boolean run(WorthBuffer buffer);
	}
	
	@FunctionalInterface
	public interface BufferReadObj {
		Object run(WorthBuffer buffer);
	}
	
}
