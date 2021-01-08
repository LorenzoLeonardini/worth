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
import java.util.Set;

import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.data.CardLocation;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;
import dev.leonardini.worth.server.data.Card.HistoryEntry;
import dev.leonardini.worth.server.data.Project;

/**
 * Server TCP is the main server thread, managing user TCP connections and handling
 * all the major communications.
 */
public class ServerTCP extends Thread {
	
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ServerHandlerManager handler;
	private ServerChat serverChat = new ServerChat();
	
	private boolean running = true;

	protected ServerTCP() {
		super("ServerTCP thread");
		handler = new ServerHandlerManager();
		registerHandlers();
		
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
	}
	
	protected void cleanUp() {
		running = false;
		try {
			selector.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		UserManager.get().saveToFile();
		ProjectDB.saveProjectsFile();
	}
	
	@Override
	public void run() {
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
							// Initialize connection and session
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							SocketChannel client = server.accept();
							client.configureBlocking(false);
							Logger.Log("Client connected " + client.getRemoteAddress());
							
							SelectionKey client_key = client.register(selector, SelectionKey.OP_READ);
							client_key.attach(new Session());
						} else if(key.isReadable()) {
							// Read incoming data
							SocketChannel client = (SocketChannel) key.channel();
							Session session = (Session) key.attachment();
							int read = session.buffer.read(client);
							
							// When user crashes, they send empty packets and the server gets stuck
							// this makes sure that doesn't happen
							if(read == 0) {
								session.ttl--;
								if(session.ttl == 0) {
									Logger.Log((session.username == null ? "" : session.username) + " timed out");
									UserManager.get().logout(session.username);
									key.cancel();
									key.channel().close();
									continue;
								}
							} else {
								session.ttl = 100;
							}

							if(session.buffer.remaining() == 0) break;
							if(key.isValid())
								key.interestOps(SelectionKey.OP_WRITE);
						} else if(key.isWritable()) {
							// When ready to respond to the client, manage the request and send the answer
							SocketChannel client = (SocketChannel) key.channel();
							WorthBuffer buffer = handler.handle(key);
							buffer.write(client);
							key.interestOps(SelectionKey.OP_READ);
						}
					} catch(IOException ex) {
						Logger.Log("Closing a user connection");
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
	
	private void registerHandlers() {
		// Login handler
		handler.addHandler(Operation.LOGIN, (Session session, SelectionKey key, WorthBuffer out) -> {
			String username = session.buffer.getString().trim();
			String password = session.buffer.getString().trim();
			session.username = username;
			
			// Try to log in
			try {
				session.logged = UserManager.get().login(username, password);
				Logger.Log("Login request for user " + username + ", outcome: " + session.logged);
			} catch(Exception e) {
				Logger.Log("Login request for user " + username + ", outcome: " + false);
				out.putBoolean(false);
				out.putString("Utente già connesso");
				return;
			}
			out.putBoolean(session.logged);
			
			if(session.logged) { // Send all the user data
				out.put(UserManager.get().getUsersStatus());
			} else { // Error message
				out.putString("Credenziali invalide");
			}
		}, ServerHandlerManager.NONE);
		
		// Propic change handler
		handler.addHandler(Operation.CHANGE_PROPIC, (Session session, SelectionKey key, WorthBuffer out) -> {
			// If user is logged, profile picture update can never fail
			UserManager.get().updateProfilePicture(session.username, session.buffer.getString().trim());
			Logger.Log(session.username + " changed their profile picture");
			out.putBoolean(true);
		}, ServerHandlerManager.LOGGED);
		
		// Project creation handler
		handler.addHandler(Operation.CREATE_PROJECT, (Session session, SelectionKey key, WorthBuffer out) -> {
			try {
				String projectName = session.buffer.getString().trim();
				ProjectDB.createProject(projectName, session.username);
				out.putBoolean(true);
				Logger.Log(session.username + " created project '" + projectName + "'");
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.LOGGED);
		
		// Logout handler
		handler.addHandler(Operation.LOGOUT, (Session session, SelectionKey key, WorthBuffer out) -> {
			try {
				if(session.logged) {
					UserManager.get().logout(session.username);
				}
				key.cancel();
				key.channel().close();
			} catch(Exception e) {}
		}, ServerHandlerManager.NONE);
		
		// Show members handler
		handler.addHandler(Operation.SHOW_MEMBERS, (Session session, SelectionKey key, WorthBuffer out) -> {
			try {
				String projectName = session.buffer.getString().trim();
				List<String> members = ProjectDB.getProjectMembers(projectName);
				out.putBoolean(true);
				out.putInt(members.size());
				for(String member : members) {
					out.putString(member);
				}
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Add member handler
		handler.addHandler(Operation.ADD_MEMBER, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString().trim();
			String user = session.buffer.getString().trim();
			try {
				UserManager.get().checkExistance(user);
				ProjectDB.addMember(projectName, user);
				out.putBoolean(true);
				Logger.Log(session.username + " added " + user + " to the project '" + projectName + "'");
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Show cards handler
		handler.addHandler(Operation.SHOW_CARDS, (Session session, SelectionKey key, WorthBuffer out) -> {
			try {
				String projectName = session.buffer.getString().trim();
				List<String> cards = ProjectDB.getCards(projectName);
				out.putBoolean(true);
				out.putInt(cards.size());
				for(String card : cards) {
					out.putString(card);
				}
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Show card handler
		handler.addHandler(Operation.SHOW_CARD, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString().trim();
			String cardName = session.buffer.getString().trim();
			try {
				CardInfo info = ProjectDB.getCard(projectName, cardName);
				out.putBoolean(true);
				out.putString(info.description);
				out.putInt(info.list.ordinal());
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Get card history handler
		handler.addHandler(Operation.GET_CARD_HISTORY, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString().trim();
			String cardName = session.buffer.getString().trim();
			try {
				List<HistoryEntry> history = ProjectDB.getCardHistory(projectName, cardName);
				out.putBoolean(true);
				out.putInt(history.size());
				for(HistoryEntry e : history) {
					out.putLong(e.timestamp);
					out.putString(e.user);
					out.putInt(e.location.ordinal());
				}
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Add card handler
		handler.addHandler(Operation.ADD_CARD, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString().trim();
			String cardName = session.buffer.getString().trim();
			String cardDescription = session.buffer.getString().trim();
			try {
				ProjectDB.addCard(projectName, cardName, cardDescription, session.username);
				out.putBoolean(true);
				Logger.Log(session.username + " created card '" + cardName + "' in project '" + projectName + "'");
				new Thread(() -> {
					serverChat.sendChatNotification(projectName, cardName, session.username, null, CardLocation.TODO);
				}).start();
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Move card handler
		handler.addHandler(Operation.MOVE_CARD, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString().trim();
			String cardName = session.buffer.getString().trim();
			CardLocation src = CardLocation.values()[session.buffer.getInt()];
			CardLocation dst = CardLocation.values()[session.buffer.getInt()];
			try {
				ProjectDB.moveCard(projectName, cardName, src, dst, session.username);
				out.putBoolean(true);
				Logger.Log(session.username + " moved card '" + cardName + "' in project '" + projectName + "'");
				serverChat.sendChatNotification(projectName, cardName, session.username, src, dst);
			} catch (Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// Delete project handler
		handler.addHandler(Operation.DELETE_PROJECT, (Session session, SelectionKey key, WorthBuffer out) -> {
			String projectName = session.buffer.getString().trim();
			String password = session.buffer.getString().trim();
			try {
				if(!UserManager.get().securityLogin(session.username, password)) {
					out.putBoolean(false);
					out.putString("Password errata");
					return;
				}
				ProjectDB.deleteProject(projectName, session.username);
				out.putBoolean(true);
				Logger.Log(session.username + " deleted project '" + projectName + "'");
			} catch (Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
			}
		}, ServerHandlerManager.PROJECT_MEMBER);
		
		// List projects handler
		handler.addHandler(Operation.LIST_PROJECTS, (Session session, SelectionKey key, WorthBuffer out) -> {
			// If logged can never return any error
			out.putBoolean(true);
			List<Project> projects = ProjectDB.listUserProjects(session.username);
			out.putInt(projects.size());
			for(Project p : projects) {
				out.putString(p.getName());
			}
		}, ServerHandlerManager.LOGGED);
		
		// Chat message handler
		handler.addHandler(Operation.CHAT, (Session session, SelectionKey key, WorthBuffer out) -> {
			session.buffer.getInt();
			long timestamp = session.buffer.getLong();
			String projectName = session.buffer.getString().trim();
			String username = session.buffer.getString().trim();
			String text = session.buffer.getString().trim();
			
			try {
				RMIServer.chatForwarder.send(timestamp, projectName, username, text);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			out.putBoolean(true);
		}, ServerHandlerManager.LOGGED);
	}
	
	class Session {
		String username;
		boolean logged = false;
		WorthBuffer buffer = new WorthBuffer();
		public int ttl = 100;
		
		@Override
		public String toString() {
			return "username:" + username + ";logged:" + logged;
		}
	}
	
}
