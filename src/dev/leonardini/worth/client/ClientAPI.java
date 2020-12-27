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

public class ClientAPI implements UserUpdateCallback {
	
	private String host;
	private Registry registry;
	private String message_info = "";
	private SocketChannel socketChannel = null;
	private UsersChangeNotification stub;
	private ChatFallbackReceiver chatStub;
	private ClientChatAPI chat;
	private String username = null;
	private Set<UserUpdateCallback> stubSet = new HashSet<UserUpdateCallback>();
	
	private Map<String, Boolean> users;
	
	public ClientAPI() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if(socketChannel != null)
				logout();
			if(stub != null)
				unregisterUsersRMI();
			chat.cleanUp();
			unregisterChatRMI();
		}));
		chat = new ClientChatAPI();
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
			ex.printStackTrace();
			if(ex instanceof InvalidRegistrationException)
				message_info = ex.getMessage();
			else
				message_info = "Errore di connessione";
			return false;
		} catch (Exception e) {
			e.printStackTrace();
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
		return serverCommunicationBoolean(Operation.LOGIN,
		(buffer) -> {
			buffer.putString(username);
			buffer.putString(password);
		}, (buffer) -> {
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
			this.username = username;
			registerUsersRMI();
			registerChatRMI();
			chat.multicastDiscovery(username, this.users);
			return true;
		});
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
			username = null;
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void registerUsersRMI(UserUpdateCallback callback) {
		stubSet.add(callback);
	}
	
	public boolean registerUsersRMI() {
		try {
			UsersChangeUpdater usersChangeUpdater = new UsersChangeUpdater(this);
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
	
	private boolean unregisterUsersRMI() {
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
	
	private void registerChatRMI() {
		try {
			chatStub = (ChatFallbackReceiver) UnicastRemoteObject.exportObject(chat, 0);
			ChatFallbackRegistration chat_service = (ChatFallbackRegistration) registry.lookup(NetworkUtils.CHAT_FALLBACK);
			chat_service.registerForCallback(chatStub);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void unregisterChatRMI() {
		try {
			ChatFallbackRegistration chat_service = (ChatFallbackRegistration) registry.lookup(NetworkUtils.CHAT_FALLBACK);
			chat_service.unregisterForCallback(chatStub);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean updateProfilePicture(String email) {
		return serverCommunicationBoolean(Operation.CHANGE_PROPIC,
		(buffer) -> {
			buffer.putString(email);
		});
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
	
	@SuppressWarnings("unchecked")
	public List<String> listProjects() {
		return (List<String>) serverCommunicationObject(Operation.LIST_PROJECTS,
		(buffer) -> {}, (buffer) -> {
			int amount = buffer.getInt();
			List<String> projects = new ArrayList<String>(amount);
			for(int i = 0; i < amount; i++) {
				projects.add(buffer.getString());
			}
			return projects;
		});
	}
	
	public boolean createProject(String projectName) {
		return serverCommunicationBoolean(Operation.CREATE_PROJECT,
		(buffer) -> {
			buffer.putString(projectName);
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<String> showCards(String projectName) {
		return (List<String>) serverCommunicationObject(Operation.SHOW_CARDS,
		(buffer) -> {
			buffer.putString(projectName);
		}, (buffer) -> {
			int len = buffer.getInt();
			List<String> cards = new ArrayList<String>(len);
			for(int i = 0; i < len; i++) {
				cards.add(buffer.getString());
			}
			return cards;
		});
	}
	
	public CardInfo showCard(String projectName, String cardName) {
		return (CardInfo) serverCommunicationObject(Operation.SHOW_CARD,
		(buffer) -> {
			buffer.putString(projectName);
			buffer.putString(cardName);
		}, (buffer) -> {
			return new CardInfo(cardName, buffer.getString(), CardLocation.values()[buffer.getInt()]);
		});
	}
	
	public boolean addCard(String projectName, String cardName, String cardDescription) {
		return serverCommunicationBoolean(Operation.ADD_CARD,
		(buffer) -> {
			buffer.putString(projectName);
			buffer.putString(cardName);
			buffer.putString(cardDescription);
		});
	}
	
	public boolean moveCard(String projectName, String cardName, CardLocation src, CardLocation dst) {
		return serverCommunicationBoolean(Operation.MOVE_CARD,
		(buffer) -> {
			buffer.putString(projectName);
			buffer.putString(cardName);
			buffer.putInt(src.ordinal());
			buffer.putInt(dst.ordinal());
		});
	}
	
	public boolean cancelProject(String projectName, String password) {
		return serverCommunicationBoolean(Operation.DELETE_PROJECT,
		(buffer) -> {
			buffer.putString(projectName);
			buffer.putString(password);
		});
	}
	
	public String getMessage() {
		return message_info;
	}

	@SuppressWarnings("unchecked")
	public List<String> showMembers(String projectName) {
		return (List<String>) serverCommunicationObject(Operation.SHOW_MEMBERS,
		(buffer) -> {
			buffer.putString(projectName);
		}, (buffer) -> {
			int amount = buffer.getInt();
			List<String> members = new ArrayList<String>(amount);
			for(int i = 0; i < amount; i++) {
				members.add(buffer.getString());
			}
			return members;
		});
	}
	
	public boolean addMember(String projectName, String username) {
		return serverCommunicationBoolean(Operation.ADD_MEMBER,
		(buffer) -> {
			buffer.putString(projectName);
			buffer.putString(username);
		});
	}
	
	@SuppressWarnings("unchecked")
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
	
	private boolean serverCommunicationBoolean(Operation op, BufferWrite write) {
		return serverCommunicationBoolean(op, write, (buffer) -> {return true;});
	}
	
	private boolean serverCommunicationBoolean(Operation op, BufferWrite write, BufferReadBool read) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(op, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		write.run(buffer);
		
		if(comm.send()) {
			return read.run(buffer);
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return false;
	}
	
	private Object serverCommunicationObject(Operation op, BufferWrite write, BufferReadObj read) {
		if(socketChannel == null) {
			message_info = "Nessuna connessione";
			return null;
		}
		
		ServerCommunication comm = new ServerCommunication(op, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		write.run(buffer);
		
		if(comm.send()) {
			return read.run(buffer);
		}
		message_info = comm.getErrorMessage();
		System.out.println(message_info);
		return null;
	}
	
	public boolean readChat(String projectName, ReceiveChatCallback chatCallback) {
		chat.read(projectName, chatCallback);
		return false;
	}
	
	public boolean exitChat() {
		chat.stop();
		return false;
	}
	
	public boolean sendChatMsg(String projectName, String message) {
		chat.send(socketChannel, username, message);
		return true;
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
