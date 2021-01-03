package dev.leonardini.worth.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import dev.leonardini.worth.data.Card.HistoryEntry;
import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.data.Project;
import dev.leonardini.worth.data.Project.CardLocation;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;

public class ServerMain {
	
	private UserManager userManager;
	
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private Scanner scanner;
	
	private boolean running = true;
	
	public ServerMain() {
		userManager = new UserManager("users.wdb");
		ProjectDB.load("projectdb");
		// Start RMI stuff
		RMIServer.init(userManager);
		
		// Start TCP server
		new Thread(() -> {
			run();
		}).start();
		Logger.Trace("Server listening on port " + NetworkUtils.SERVER_PORT);
		
		// Get terminal input
		System.out.println("\nType 'help' for a list of commands\n");
		scanner = new Scanner(System.in);
		String line;
		while((line = scanner.nextLine()) != null) {
			line = line.trim();
			switch(line) {
				case "help":
					System.out.println("help\t\tget a list of commands");
					System.out.println("verbose\t\tenable verbose mode -> all logs are shown in console");
					System.out.println("silent\t\tdisable verbose mode");
					System.out.println("exit\t\tgracefully close the server");
					System.out.println("\n");
					break;
				case "exit":
					System.out.println("Goodbye");
					cleanUp();
					System.exit(0);
					break;
				case "verbose":
					System.out.println("Toggled verbose");
					Logger.verbose(true);
					break;
				case "silent":
					System.out.println("Toggled verbose");
					Logger.verbose(false);
					break;
				default:
					System.out.println("Unknown command\n");
			}
		}
	}
	
	private void cleanUp() {
		running = false;
		try {
			selector.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		scanner.close();
		userManager.saveToFile();
		ProjectDB.save("projectdb");
	}
	
	private void run() {
		ServerHandler handler = new ServerHandler();
		registerHandlers(handler);
		
		serverChannel = null;
		selector = null;
		try {
			serverChannel = ServerSocketChannel.open();
			ServerSocket ss = serverChannel.socket();
			ss.bind(new InetSocketAddress(NetworkUtils.SERVER_PORT));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		while(running) {
			try {
				selector.select();
				Set<SelectionKey> readyKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = readyKeys.iterator();
				
				while(iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					
					try {
						if(key.isAcceptable()) {
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							SocketChannel client = server.accept();
							client.configureBlocking(false);
							Logger.Trace("Client connected " + client.getRemoteAddress());
							
							SelectionKey client_key = client.register(selector, SelectionKey.OP_READ);
							client_key.attach(new Session());
						} else if(key.isReadable()) {
							SocketChannel client = (SocketChannel) key.channel();
							Session session = (Session) key.attachment();
							int read = session.buffer.read(client);
							if(read == 0) {
								session.ttl--;
								if(session.ttl == 0) {
									Logger.Trace((session.username == null ? "" : session.username) + " timed out");
									userManager.logout(session.username);
									key.cancel();
									key.channel().close();
									continue;
								}
							} else {
								session.ttl = 100;
							}

							if(session.buffer.remaining() == 0) break;
							session.current_operation = session.buffer.getOperation();
							if(key.isValid())
								key.interestOps(SelectionKey.OP_WRITE);
						} else if(key.isWritable()) {
							SocketChannel client = (SocketChannel) key.channel();
							WorthBuffer buffer = handler.handle(key);
							buffer.write(client);
							key.interestOps(SelectionKey.OP_READ);
						} else {
							System.out.println(key.isAcceptable() + " " + key.isConnectable() + " " + key.isReadable() + " " + key.isValid() + " " + key.isWritable());
						}
					} catch(IOException ex) {
						System.err.println("Closing connection");
						key.cancel();
						key.channel().close();
					}
				}
			} catch(Exception e) {
				if(running) {
					e.printStackTrace();
					Logger.Error(e.getMessage());
				}
			}
		}
	}
	
	private void registerHandlers(ServerHandler handler) {
		handler.addHandler(Operation.LOGIN, (Session session, SelectionKey key, WorthBuffer out) -> {
			String username = session.buffer.getString();
			String password = session.buffer.getString();
			session.username = username;
			Logger.Trace("Login request for user " + username);
			try {
				session.logged = userManager.login(username, password);
				Logger.Trace("Login outcome: " + session.logged);
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString("Utente già connesso");
				return;
			}
			out.putBoolean(session.logged);
			if(session.logged) {
				out.put(userManager.getUsersStatus());
			} else {
				out.putString("Credenziali invalide");
			}
		}, ServerHandler.NONE);
		
		handler.addHandler(Operation.CHANGE_PROPIC, (Session session, SelectionKey key, WorthBuffer out) -> {
			userManager.updateProfilePicture(session.username, session.buffer.getString());
			out.putBoolean(true);
		}, ServerHandler.LOGGED);
		
		handler.addHandler(Operation.CREATE_PROJECT, (Session session, SelectionKey key, WorthBuffer out) -> {
			boolean outcome = ProjectDB.createProject(session.buffer.getString(), session.username);
			out.putBoolean(outcome);
			if(!outcome) {
				out.putString("Progetto già esistente");
			}
		}, ServerHandler.LOGGED);
		
		handler.addHandler(Operation.LOGOUT, (Session session, SelectionKey key, WorthBuffer out) -> {
			if(session.logged) {
				userManager.logout(session.username);
			}
			key.cancel();
			try {
				key.channel().close();
			}
			catch (IOException e) {}				
		}, ServerHandler.NONE);
		
		handler.addHandler(Operation.SHOW_MEMBERS, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			List<String> members = ProjectDB.getMembers(projectName);
			out.putBoolean(true);
			out.putInt(members.size());
			for(String member : members) {
				out.putString(member);
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.ADD_MEMBER, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			String user = session.buffer.getString();
			List<String> members = ProjectDB.getMembers(projectName);
			if(!userManager.exists(user)) {
				out.putBoolean(false);
				out.putString("L'utente non esiste");
				return;
			}
			if(members.contains(user)) {
				out.putBoolean(false);
				out.putString("L'utente è già membro");
				return;
			}
			ProjectDB.addMember(projectName, user);
			out.putBoolean(true);
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.SHOW_CARDS, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			List<String> cards = ProjectDB.getCards(projectName);
			out.putBoolean(true);
			out.putInt(cards.size());
			for(String card : cards) {
				out.putString(card);
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.SHOW_CARD, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			String cardName = session.buffer.getString();
			CardInfo info = ProjectDB.getCard(projectName, cardName);
			out.putBoolean(info != null);
			if(info == null) {
				out.putString("Card inesistente");
			} else {
				out.putString(info.description);
				out.putInt(info.list.ordinal());
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.GET_CARD_HISTORY, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			String cardName = session.buffer.getString();
			List<HistoryEntry> history = ProjectDB.getCardHistory(projectName, cardName);
			out.putBoolean(history != null);
			if(history == null) {
				out.putString("Card inesistente");
			} else {
				out.putInt(history.size());
				for(HistoryEntry e : history) {
					out.putLong(e.timestamp);
					out.putString(e.user);
					out.putInt(e.location.ordinal());
				}
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.ADD_CARD, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			String cardName = session.buffer.getString();
			String cardDescription = session.buffer.getString();
			String error = ProjectDB.addCard(projectName, cardName, cardDescription, session.username);
			out.putBoolean(error == null);
			if(error == null) {
				new Thread(() -> {
					sendChatNotification(projectName, cardName, session.username, null, CardLocation.TODO);
				}).start();
			} else {
				out.putString(error);
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.MOVE_CARD, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			String cardName = session.buffer.getString();
			CardLocation src = CardLocation.values()[session.buffer.getInt()];
			CardLocation dst = CardLocation.values()[session.buffer.getInt()];
			try {
				ProjectDB.moveCard(projectName, cardName, src, dst, session.username);
				out.putBoolean(true);
				sendChatNotification(projectName, cardName, session.username, src, dst);
			}
			catch (Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.DELETE_PROJECT, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString();
			String password = session.buffer.getString();
			if(!userManager.securityLogin(session.username, password)) {
				out.putBoolean(false);
				out.putString("Password errata");
				return;
			}
			try {
				ProjectDB.deleteProject(projectName, session.username);
				out.putBoolean(true);
			}
			catch (Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandler.PROJECT_MEMBER);
		
		handler.addHandler(Operation.CHAT, (Session session, SelectionKey key, WorthBuffer out) -> {
			session.buffer.getInt();
			long timestamp = session.buffer.getLong();
			String projectName = session.buffer.getString();
			String username = session.buffer.getString();
			String text = session.buffer.getString();
			
			RMIServer.chatForwarder.send(timestamp, projectName, username, text);
			out.putBoolean(true);
		}, ServerHandler.LOGGED);
		
		handler.addHandler(Operation.LIST_PROJECTS, (Session session, SelectionKey key, WorthBuffer out) -> {
			out.putBoolean(true);
			List<Project> projects = ProjectDB.listUserProjects(session.username);
			out.putInt(projects.size());
			for(Project p : projects) {
				out.putString(p.getName());
			}
		}, ServerHandler.LOGGED);
	}
	
	private void sendChatNotification(String projectName, String card, String user, CardLocation from, CardLocation to) {
		try {
			RMIServer.chatForwarder.send(System.currentTimeMillis(), projectName, card, user, from, to);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		} 
	}
	
	class Session {
		String username;
		boolean logged = false;
		NetworkUtils.Operation current_operation;
		WorthBuffer buffer = new WorthBuffer();
		public int ttl = 100;
		
		@Override
		public String toString() {
			return "username:" + username + ";logged:" + logged + ";operation:" + current_operation;
		}
	}

	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: ./WORTHServer RMIhost");
			System.err.println("where RMIhost is the hostname used for rmi lookup");
			System.exit(1);
		}
		
		System.setProperty("java.rmi.server.hostname", args[0]);
		new ServerMain();
	}
}
