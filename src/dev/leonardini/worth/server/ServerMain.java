package dev.leonardini.worth.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import dev.leonardini.worth.networking.NetworkUtils;

public class ServerMain {
	
	private UserManager userManager;
	
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private Scanner scanner;
	
	private boolean running = true;
	
	public ServerMain() {
		userManager = new UserManager();
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
					System.out.println("help\tget a list of commands\nexit\tgracefully close the server\n");
					break;
				case "exit":
					System.out.println("Goodbye");
					cleanUp();
					System.exit(0);
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
	}
	
	private void incoming(SelectionKey key, ByteBuffer buffer) {
		if(buffer.remaining() == 0) return;
		Session session = (Session) key.attachment();
		session.current_operation = NetworkUtils.getOperation(buffer);
		
		switch(session.current_operation) {
			case LOGIN:
				System.out.println(buffer);
				String username = NetworkUtils.getString(buffer);
				String password = NetworkUtils.getString(buffer);
				session.username = username;
				Logger.Trace("Login request for user " + username);
				session.logged = userManager.login(username, password);
				break;
			default:
				break;
		}
		
		key.attach(session);
	}
	
	private void outgoing(SelectionKey key, ByteBuffer buffer) {
		Session session = (Session) key.attachment();
		NetworkUtils.putOperation(buffer, session.current_operation);
		switch(session.current_operation) {
			case LOGIN:
				NetworkUtils.putBoolean(buffer, session.logged);
				if(!session.logged) {
					NetworkUtils.putString(buffer, "Credenziali invalide");
				}
				break;
			default:
				break;
		}
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
							Logger.Trace("Client connected " + client.getRemoteAddress());
							client.configureBlocking(false);
							SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
							key2.attach(new Session());
						} else if(key.isReadable()) {
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							client.read(buff);
							buff.flip();
							System.out.println(NetworkUtils.isBufferFinished(buff));
							incoming(key, buff);
							key.interestOps(SelectionKey.OP_WRITE);
						} else if(key.isWritable()) {
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							outgoing(key, buffer);
							buffer.flip();
							client.write(buffer);
							key.interestOps(SelectionKey.OP_READ);
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
		NetworkUtils.Operation current_operation;
		
		@Override
		public String toString() {
			return "username:" + username + ";logged:" + logged + ";operation:" + current_operation;
		}
	}
	
}
