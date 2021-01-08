package dev.leonardini.worth.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import dev.leonardini.worth.data.CardLocation;
import dev.leonardini.worth.networking.ChatFallbackReceiver;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;

/**
 * This class manages chat communication with other users and the server.
 * The main messaging "system" is via UDP Multicast, but this object takes care of
 * a discovering system to check if every user is in fact reachable with Multicast
 * (maybe they are not on the same network or a firewall is blocking that), in that
 * case chat messages are forwarded from the server via RMI.
 */
public class ClientChatAPI extends RemoteObject implements ChatFallbackReceiver, Runnable {

	private static final long serialVersionUID = 2419155737431494779L;
	
	private ReceiveChatCallback callback = null;
	private String project = null;
	private boolean running = true;
	private String username = null;
	
	private MulticastSocket socket;
	
	private Set<String> messagesHash = new HashSet<String>();
	private Map<Integer, Long> discoveryQueries = new HashMap<Integer, Long>();
	private Set<String> reachableUsers = new HashSet<String>();
	private Set<String> onlineUsers = new HashSet<String>();
	
	protected ClientChatAPI() {
		try {
			socket = new MulticastSocket(NetworkUtils.MULTICAST_PORT);
			InetAddress ia = InetAddress.getByName(NetworkUtils.MULTICAST_ADDRESS);
			socket.joinGroup(ia);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
			
		new Thread(this).start();
	}
	
	@Override
	/**
	 * Listen to the multicast socket and manage incoming messages
	 */
	public void run() {
		WorthBuffer buffer = new WorthBuffer(65535); // overkill, but better safe than sorry
		while(running) {
			try {
				buffer.clear();

				DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit());
				socket.receive(packet);
				
				NetworkUtils.ChatOperation op = NetworkUtils.ChatOperation.values()[buffer.getInt()];
				if(op == NetworkUtils.ChatOperation.MESSAGE) {
					// This is a standard chat message
					long timestamp = buffer.getLong();
					String forProject = buffer.getString();
					String from = buffer.getString();
					String message = buffer.getString();
					
					receiveMessage(timestamp, forProject, from, message);
				} else if(op == NetworkUtils.ChatOperation.SERVER) {
					// This is a system message from the server
					long timestamp = buffer.getLong();
					String forProject = buffer.getString();
					String cardName = buffer.getString();
					String user = buffer.getString();
					CardLocation from = CardLocation.values()[buffer.getInt()];
					CardLocation to = CardLocation.values()[buffer.getInt()];
					
					receiveSystem(timestamp, forProject, cardName, user, from, to);
				} else if(op == NetworkUtils.ChatOperation.MULTICAST_DISCOVERY) {
					// This is a discovery message
					if(username == null) continue;
					
					// Cleanup old discovery queries
					// This allows both not to waste memory, speed up searches
					// and minimize the risk of random collisions
					long now = System.currentTimeMillis();
					for(int id : discoveryQueries.keySet()) {
						if(now - discoveryQueries.get(id) > 5 * 60 * 1000) {
							discoveryQueries.remove(id);
						}
					}
					
					int id = buffer.getInt();
					String user = buffer.getString();

					reachableUsers.add(user);
					System.out.println("new user reachable (" + reachableUsers.size() + "): " + user);
					
					// If this is a new discovery, tell others I'm reachable
					if(discoveryQueries.containsKey(id)) continue;
					
					multicastDiscovery(id);
				}
			} catch(SocketException e) {
				// socket closed
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Send a user message on the active chat.
	 * 
	 * @param socketChannel this is the socketchannel used to communicate with the server.
	 * 			it is used as a fallback method of sending messages to users not reachable via UDP
	 * @param message
	 * @return outcome
	 */
	protected boolean send(SocketChannel socketChannel, String message) {
		boolean outcome = true, shouldSendServer;
		
		long now = System.currentTimeMillis();
		
		synchronized (this) {
			if(username == null) {
				return false;
			}
			
			// Always send with UDP multicast
			outcome &= sendMulticast(message, now);
			shouldSendServer = reachableUsers.size() < onlineUsers.size();
		}
		
		if(shouldSendServer) {
			outcome &= sendServer(socketChannel, message, now);
		}
		
		// No real reason to fail this
		return outcome;
	}
	
	/**
	 * Send a chat message to server TCP connection
	 * 
	 * @param socketChannel
	 * @param message
	 * @return outcome
	 */
	private boolean sendServer(SocketChannel socketChannel, String message, long now) {
		if(socketChannel == null) {
			return false;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.CHAT, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putInt(NetworkUtils.ChatOperation.MESSAGE.ordinal());
		buffer.putLong(now);
		synchronized(this) {
			buffer.putString(project);
			buffer.putString(username);
		}
		buffer.putString(message);
		
		if(!comm.send()) {
			System.err.println(comm.getErrorMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Send chat message directly to all the other users via UDP Multicast 
	 * @param message
	 * @return outcome
	 */
	private synchronized boolean sendMulticast(String message, long now) {
		try {
			InetAddress ia = InetAddress.getByName(NetworkUtils.MULTICAST_ADDRESS);
			WorthBuffer buffer = new WorthBuffer();
			buffer.putInt(NetworkUtils.ChatOperation.MESSAGE.ordinal());
			buffer.putLong(now);
			buffer.putString(project);
			buffer.putString(username);
			buffer.putString(message);
			buffer.flip();
			
			DatagramSocket ms = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit(), ia, NetworkUtils.MULTICAST_PORT);
			ms.send(packet);
			ms.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Initiate muticast discovery. It is mandatory that this method is called after logging in
	 * and before sending any kind of message
	 * 
	 * @param username
	 * @param users
	 */
	public synchronized void multicastDiscovery(String username, Map<String, Boolean> users) {
		for(Entry<String, Boolean> u : users.entrySet())
			if(u.getValue())
				onlineUsers.add(u.getKey());
		this.username = username;
		Random r = new Random();
		multicastDiscovery(r.nextInt());
	}
	
	/**
	 * Send a multicast message for discovery reasons with my username
	 * @param id the id of the interested discovery "query"
	 */
	private synchronized void multicastDiscovery(int id) {
		discoveryQueries.put(id, System.currentTimeMillis());
		try {
			InetAddress ia = InetAddress.getByName(NetworkUtils.MULTICAST_ADDRESS);
			WorthBuffer buffer = new WorthBuffer();
			buffer.putInt(NetworkUtils.ChatOperation.MULTICAST_DISCOVERY.ordinal());
			buffer.putInt(id);
			buffer.putString(username);
			buffer.flip();
			
			DatagramSocket ms = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit(), ia, NetworkUtils.MULTICAST_PORT);
			ms.send(packet);
			ms.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initiate chat read callback
	 * @param project
	 * @param callback
	 * @return
	 */
	protected synchronized boolean read(String project, ReceiveChatCallback callback) {
		this.callback = callback;
		this.project = project;
		return true;
	}
	
	/**
	 * Stop listening to chat messages
	 * @return
	 */
	protected synchronized boolean stop() {
		this.callback = null;
		this.project = null;
		messagesHash.clear();
		return true;
	}
	
	/**
	 * Stop this thread
	 */
	protected synchronized void cleanUp() {
		socket.close();
		running = false;
	}
	
	/**
	 * Calculate message hash for a user message. Message hash is used to avoid receiving
	 * multiple times the same message
	 */
	private String messageHash(long timestamp, String project, String username, String message) {
		return messageHash(timestamp + project + username + message);
	}
	
	/**
	 * Calculate message hash for a system message. Message hash is used to avoid receiving
	 * multiple times the same message
	 */
	private String messageHash(long timestamp, String project, String card, String username, CardLocation from, CardLocation to) {
		return messageHash(timestamp + project + card + username + from + to);
	}		
	
	/**
	 * Calculate message hash for a message. Message hash is used to avoid receiving
	 * multiple times the same message
	 */
	private String messageHash(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(message.getBytes()); 
			  
            BigInteger no = new BigInteger(1, messageDigest); 
            String out = no.toString(16);
            while (out.length() < 32) { 
            	out = "0" + out; 
            }
            return out;
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Keep online users and reachable users sets updated, in order to have
	 * functioning server fallback
	 * 
	 * @param username
	 * @param status
	 */
	protected void updateUserStatus(String username, boolean status) {
		if(status) {
			onlineUsers.add(username);
		} else {
			onlineUsers.remove(username);
			reachableUsers.remove(username);
		}
	}

	@Override
	/**
	 * Interpret a user message. Can be called either after receving a message via multicast, or
	 * remotely by the server via RMI
	 */
	public synchronized void receiveMessage(long timestamp, String project, String username, String message) throws RemoteException {
		if(this.project == null) return;
		if(!this.project.equals(project)) return;
		message = message.trim();
		username = username.trim();
		project = project.trim();
		
		String hash = messageHash(timestamp, project, username, message);
		if(messagesHash.contains(hash)) return;
		messagesHash.add(hash);
		
		callback.receivedChatMessage(username, message);
	}

	@Override
	/**
	 * Interpret a system message. Can be called either after receving a message via multicast, or
	 * remotely by the server via RMI
	 */
	public synchronized void receiveSystem(long timestamp, String project, String card, String user, CardLocation from, CardLocation to) throws RemoteException {
		if(this.project == null) return;
		if(!this.project.equals(project)) return;
		
		card = card.trim();
		username = username.trim();
		project = project.trim();
		
		String hash = messageHash(timestamp, project, card, user, from, to);
		if(messagesHash.contains(hash)) return;
		messagesHash.add(hash);
		
		callback.receivedSystemNotification(user, card, from, to);
	}


}
