package dev.leonardini.worth.networking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

public abstract class NetworkUtils {
	
	public static final int SERVER_PORT = 4000;
	public static final int REGISTRY_PORT = 4001;
	public static final int MULTICAST_PORT = 1300;
	public static final String MULTICAST_ADDRESS = "239.255.1.3";
	public static final String USER_REGISTRATION = "REGISTRATION-SERVICE";
	public static final String USER_STATUS_NOTIFICATION = "NOTIFICATION-SERVICE";
	public static final String CHAT_FALLBACK = "CHAT-SERVICE";
	
	public enum Operation {
		LOGIN,
		CHANGE_PROPIC,
		CREATE_PROJECT,
		LIST_PROJECTS,
		SHOW_MEMBERS,
		ADD_MEMBER,
		SHOW_CARDS,
		SHOW_CARD,
		ADD_CARD,
		GET_CARD_HISTORY,
		MOVE_CARD,
		DELETE_PROJECT,
		LOGOUT,
		CHAT
	}
	
	public enum ChatOperation {
		MESSAGE,
		SERVER,
		MULTICAST_DISCOVERY
	}
	
	// https://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
	public static String getExternalIp() {
		try {
			URL whatismyip = new URL("https://ipinfo.io/ip");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			return in.readLine();
		} catch(Exception e) {
			return "localhost";
		}
	}
	
	/**
	 * Get the client IP. Works with various attempts.
	 * @param channel
	 * @return ip address
	 */
	public static String getInternalIp(SocketChannel channel) {
		if(channel != null) { // If possible, get the ip address from the server connection
			return channel.socket().getLocalAddress().getHostAddress();
		}
		try { // Trying the ip which connects to the internet
			Socket s = new Socket("papillegustative.com", 80);
			String ip = s.getLocalAddress().getHostAddress();
			s.close();
			return ip;
		} catch(Exception e) {
			try { // Trying the most common gateway
				Socket s = new Socket("192.168.1.1", 80);
				String ip = s.getLocalAddress().getHostAddress();
				s.close();
				return ip;
			} catch(Exception e1) {
			}
		}
		// If nothing works returns localhost (most commonly loopback)
		try {
			return InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e) {
			return "127.0.0.1";
		}
	}
	
}
