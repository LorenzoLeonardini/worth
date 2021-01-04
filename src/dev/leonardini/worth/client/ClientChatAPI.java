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
import java.util.Random;
import java.util.Set;

import dev.leonardini.worth.networking.ChatFallbackReceiver;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.server.data.Project.CardLocation;
import dev.leonardini.worth.networking.WorthBuffer;

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
	
	public ClientChatAPI() {
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
	public void run() {
		WorthBuffer buffer = new WorthBuffer(65535); // overkill, but better safe than sorry
		while(running) {
			try {
				buffer.clear();

				DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit());
				socket.receive(packet);
				
				NetworkUtils.ChatOperation op = NetworkUtils.ChatOperation.values()[buffer.getInt()];
				if(op == NetworkUtils.ChatOperation.MESSAGE) {
					long timestamp = buffer.getLong();
					String forProject = buffer.getString();
					synchronized (this) {
						String from = buffer.getString();
						String message = buffer.getString();
						receiveMessage(timestamp, forProject, from, message);
					}
				} else if(op == NetworkUtils.ChatOperation.SERVER) {
					long timestamp = buffer.getLong();
					String forProject = buffer.getString();
					synchronized (this) {
						String cardName = buffer.getString();
						String user = buffer.getString();
						CardLocation from = CardLocation.values()[buffer.getInt()];
						CardLocation to = CardLocation.values()[buffer.getInt()];
						receiveSystem(timestamp, forProject, cardName, user, from, to);
					}
				} else if(op == NetworkUtils.ChatOperation.MULTICAST_DISCOVERY) {
					if(username == null) continue;
					
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
					
					if(discoveryQueries.containsKey(id)) continue;
					discoveryQueries.put(id, now);
					
					multicastDiscovery(id);
				}
			} catch(SocketException e) {
				// socket closed
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized boolean send(SocketChannel socketChannel, String username, String message) {
		if(reachableUsers.size() < onlineUsers.size()) {
			System.out.println("There are users which are not reachable via multicast");
			System.out.println("REACHABLE:");
			for(String u : reachableUsers)
				System.out.println("\t" + u);
			System.out.println("ONLINE:");
			for(String u : onlineUsers)
				System.out.println("\t" + u);
			sendServer(socketChannel, username, message);
		} else {
			sendMulticast(username, message);
		}
		return true;
	}
	
	private void sendServer(SocketChannel socketChannel, String username, String message) {
		if(socketChannel == null) {
			return;
		}
		
		ServerCommunication comm = new ServerCommunication(Operation.CHAT, socketChannel);
		WorthBuffer buffer = comm.getBuffer();
		buffer.putInt(NetworkUtils.ChatOperation.MESSAGE.ordinal());
		buffer.putLong(System.currentTimeMillis());
		synchronized(this) {
			buffer.putString(project);
		}
		buffer.putString(username);
		buffer.putString(message);
		
		if(!comm.send()) {
			System.err.println(comm.getErrorMessage());
		}
	}
	
	private synchronized void sendMulticast(String username, String message) {
		try {
			InetAddress ia = InetAddress.getByName(NetworkUtils.MULTICAST_ADDRESS);
			WorthBuffer buffer = new WorthBuffer();
			buffer.putInt(NetworkUtils.ChatOperation.MESSAGE.ordinal());
			buffer.putLong(System.currentTimeMillis());
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
		}
	}
	
	public synchronized void multicastDiscovery(String username, Map<String, Boolean> users) {
		for(String u : users.keySet())
			if(users.get(u))
				onlineUsers.add(u);
		this.username = username;
		Random r = new Random();
		multicastDiscovery(r.nextInt());
	}
	
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
	
	public synchronized boolean read(String project, ReceiveChatCallback callback) {
		this.callback = callback;
		this.project = project;
		return true;
	}
	
	public synchronized boolean stop() {
		this.callback = null;
		this.project = null;
		messagesHash.clear();
		return true;
	}
	
	public synchronized void cleanUp() {
		socket.close();
		running = false;
	}
	
	private String messageHash(long timestamp, String project, String username, String message) {
		return messageHash(timestamp + project + username + message);
	}
	
	private String messageHash(long timestamp, String project, String card, String username, CardLocation from, CardLocation to) {
		return messageHash(timestamp + project + card + username + from + to);
	}		
	
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

	public void updateUserStatus(String username, boolean status) {
		if(status) {
			onlineUsers.add(username);
		} else {
			onlineUsers.remove(username);
			reachableUsers.remove(username);
		}
	}

	@Override
	public void receiveMessage(long timestamp, String project, String username, String message) throws RemoteException {
		if(this.project == null) return;
		if(!this.project.equals(project)) return;
		String hash = messageHash(timestamp, project, username, message);
		if(messagesHash.contains(hash)) return;
		messagesHash.add(hash);
		
		callback.receivedChatMessage(username, message);
	}

	@Override
	public void receiveSystem(long timestamp, String project, String card, String user, CardLocation from, CardLocation to) throws RemoteException {
		if(this.project == null) return;
		if(!this.project.equals(project)) return;
		String hash = messageHash(timestamp, project, card, user, from, to);
		if(messagesHash.contains(hash)) return;
		messagesHash.add(hash);
		
		callback.receivedSystemNotification(user, card, from, to);
	}


}
