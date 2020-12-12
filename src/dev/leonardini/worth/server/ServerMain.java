package dev.leonardini.worth.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.data.Project;
import dev.leonardini.worth.data.Project.CardLocation;
import dev.leonardini.worth.networking.NetworkUtils;
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
	
	private void incoming(Session session, SelectionKey key) throws IOException {
		if(session.buffer.remaining() == 0) return;
		session.current_operation = session.buffer.getOperation();
		
		String username, password, projectName, user;
		List<String> projectMembers;
		
		switch(session.current_operation) {
			case LOGIN:
				username = session.buffer.getString();
				password = session.buffer.getString();
				session.username = username;
				Logger.Trace("Login request for user " + username);
				try {
					session.logged = userManager.login(username, password);
					if(!session.logged) {
						session.message = "Credenziali invalide";
					}
				} catch(Exception e) {
					session.message = "Utente già connesso";
				}
				break;
			case CHANGE_PROPIC:
				if(!session.logged) {
					break;
				}
				userManager.updateProfilePicture(session.username, session.buffer.getString());
				break;
			case CREATE_PROJECT:
				if(!session.logged) {
					break;
				}
				session.outcome = ProjectDB.createProject(session.buffer.getString(), session.username);
				break;
			case LOGOUT:
				if(session.logged) {
					userManager.logout(session.username);
				}
				key.cancel();
				key.channel().close();
				break;
			case SHOW_MEMBERS:
				projectName = session.buffer.getString();
				projectMembers = ProjectDB.getMembers(projectName);
				if(!projectMembers.contains(session.username)) {
					session.outcome = false;
					session.data = "Non fai parte del progetto";
					break;
				}
				session.data = projectName;
				session.outcome = true;
				break;
			case ADD_MEMBER:
				if(!session.logged) {
					break;
				}
				projectName = session.buffer.getString();
				user = session.buffer.getString();
				projectMembers = ProjectDB.getMembers(projectName);
				if(!projectMembers.contains(session.username)) {
					session.outcome = false;
					session.data = "Non fai parte del progetto";
					break;
				}
				if(!userManager.exists(user)) {
					session.outcome = false;
					session.data = "L'utente non esiste";
					break;
				}
				if(projectMembers.contains(user)) {
					session.outcome = false;
					session.data = "L'utente è già membro";
					break;
				}
				ProjectDB.addMember(projectName, user);
				session.outcome = true;
				break;
			case SHOW_CARDS:
				projectName = session.buffer.getString();
				projectMembers = ProjectDB.getMembers(projectName);
				if(!projectMembers.contains(session.username)) {
					session.outcome = false;
					session.data = "Non fai parte del progetto";
					break;
				}
				session.data = projectName;
				session.outcome = true;
				break;
			case SHOW_CARD:
				projectName = session.buffer.getString();
				String cardName = session.buffer.getString();
				projectMembers = ProjectDB.getMembers(projectName);
				if(!projectMembers.contains(session.username)) {
					session.outcome = false;
					session.data = "Non fai parte del progetto";
					break;
				}
				session.data = new String[] { projectName, cardName };
				session.outcome = true;
				break;
			case ADD_CARD:
				projectName = session.buffer.getString();
				cardName = session.buffer.getString();
				String cardDescription = session.buffer.getString();
				projectMembers = ProjectDB.getMembers(projectName);
				if(!projectMembers.contains(session.username)) {
					session.outcome = false;
					session.data = "Non fai parte del progetto";
					break;
				}
				session.data = new String[] { projectName, cardName, cardDescription };
				session.outcome = true;
				break;
			case MOVE_CARD:
				projectName = session.buffer.getString();
				cardName = session.buffer.getString();
				CardLocation src = CardLocation.values()[session.buffer.getInt()];
				CardLocation dst = CardLocation.values()[session.buffer.getInt()];
				projectMembers = ProjectDB.getMembers(projectName);
				if(!projectMembers.contains(session.username)) {
					session.outcome = false;
					session.data = "Non fai parte del progetto";
					break;
				}
				session.data = new Object[] { projectName, cardName, src, dst };
				session.outcome = true;
				break;
			default:
				break;
		}
		
		session.buffer.clear();
	}
	
	private void outgoing(SelectionKey key, WorthBuffer buffer) {
		Session session = (Session) key.attachment();
		buffer.putOperation(session.current_operation);
		switch(session.current_operation) {
			case LOGIN:
				buffer.putBoolean(session.logged);
				Logger.Trace("Login outcome: " + session.logged);
				if(session.logged) {
					buffer.put(userManager.getUsersStatus());
				} else {
					buffer.putString(session.message);
				}
				break;
			case CHANGE_PROPIC:
				buffer.putBoolean(session.logged);
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				}
				break;
			case CREATE_PROJECT:
				buffer.putBoolean(session.logged && session.outcome);
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString("Progetto già esistente");
				}
				break;
			case LIST_PROJECTS:
				buffer.putBoolean(session.logged);
				if(session.logged) {
					List<Project> projects = ProjectDB.listUserProjects(session.username);
					buffer.putInt(projects.size());
					for(Project p : projects) {
						buffer.putString(p.getName());
					}
				} else {
					buffer.putString("È necessario l'accesso");
				}
				break;
			case SHOW_MEMBERS:
				String projectName = (String) session.data;
				List<String> members = ProjectDB.getMembers(projectName);
				buffer.putBoolean(session.logged && session.outcome && (members != null));
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString((String) session.data);
				} else if(members == null) {
					buffer.putString("Progetto inesistente");
				} else {
					buffer.putInt(members.size());
					for(String member : members) {
						buffer.putString(member);
					}
				}
				break;
			case ADD_MEMBER:
				buffer.putBoolean(session.logged && session.outcome);
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString((String) session.data);
				}
				break;
			case SHOW_CARDS:
				projectName = (String) session.data;
				List<String> cards = ProjectDB.getCards(projectName);
				buffer.putBoolean(session.logged && session.outcome && (cards != null));
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString((String) session.data);
				} else if(cards == null) {
					buffer.putString("Progetto inesistente");
				} else {
					buffer.putInt(cards.size());
					for(String card : cards) {
						buffer.putString(card);
					}
				}
				break;
			case SHOW_CARD:
				projectName = ((String[]) session.data)[0];
				String cardName = ((String[]) session.data)[1];
				CardInfo info = ProjectDB.getCard(projectName, cardName);
				buffer.putBoolean(session.logged && session.outcome && (info != null));
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString((String) session.data);
				} else if(info == null) {
					buffer.putString("Progetto o card inesistente");
				} else {
					buffer.putString(info.description);
					buffer.putInt(info.list.ordinal());
				}
				break;
			case ADD_CARD:
				projectName = ((String[]) session.data)[0];
				cardName = ((String[]) session.data)[1];
				String cardDescription = ((String[]) session.data)[2];
				String error = ProjectDB.addCard(projectName, cardName, cardDescription, session.username);
				buffer.putBoolean(session.logged && session.outcome && error == null);
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString((String) session.data);
				} else if(error != null) {
					buffer.putString(error);
				}
				break;
			case MOVE_CARD:
				projectName = (String)((Object[]) session.data)[0];
				cardName = (String)((Object[]) session.data)[1];
				CardLocation src = (CardLocation)((Object[]) session.data)[2];
				CardLocation dst = (CardLocation)((Object[]) session.data)[3];
				boolean outcome;
				String message = "Progetto inesistente";
				try {
					outcome = ProjectDB.moveCard(projectName, cardName, src, dst, session.username);
				}
				catch (Exception e) {
					outcome = false;
					message = e.getMessage();
				}
				buffer.putBoolean(session.logged && session.outcome && outcome);
				if(!session.logged) {
					buffer.putString("È necessario l'accesso");
				} else if(!session.outcome) {
					buffer.putString((String) session.data);
				} else if(!outcome) {
					buffer.putString(message);
				}
				break;
			default:
				break;
		}
		buffer.end();
	}
	
	private void run() {
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
							
							if(session.buffer.isFinished()) {
								incoming(session, key);
								if(key.isValid())
									key.interestOps(SelectionKey.OP_WRITE);
							}
						} else if(key.isWritable()) {
							SocketChannel client = (SocketChannel) key.channel();
							WorthBuffer buffer = new WorthBuffer();
							outgoing(key, buffer);
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

	public static void main(String[] args) {
		new ServerMain();
	}
	
	class Session {
		String username;
		boolean logged = false;
		boolean outcome = false;
		Object data;
		NetworkUtils.Operation current_operation;
		WorthBuffer buffer = new WorthBuffer();
		public int ttl = 100;
		String message;
		
		@Override
		public String toString() {
			return "username:" + username + ";logged:" + logged + ";operation:" + current_operation;
		}
	}

	
}
