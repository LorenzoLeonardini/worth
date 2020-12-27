package dev.leonardini.worth.networking;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {
	
	public static final int SERVER_PORT = 4000;
	public static final int REGISTRY_PORT = 4001;
	public static final int MULTICAST_PORT = 1300;
	public static final String MULTICAST_ADDRESS = "239.255.1.3";
	public static final String USER_REGISTRATION = "REGISTRATION-SERVICE";
	public static final String USER_STATUS_NOTIFICATION = "NOTIFICATION-SERVICE";
	public static final String CHAT_FALLBACK = "CHAT-SERVICE";
	
	protected static final byte END_CODE[] = { 0, 0, 0, 1, 1, 2, 3, 5, 8, 0, 0, 0 };
	
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
	
	public static void putString(ByteBuffer buffer, String s) {
		buffer.putInt(s.length());
		buffer.put(s.getBytes());
	}
	
	public static String getString(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte data[] = new byte[length];
		buffer.get(data, 0, length);
		return new String(data, StandardCharsets.UTF_8);
	}
	
	public static void putBoolean(ByteBuffer buffer, boolean b) {
		buffer.put((byte)(b ? 1 : 0));
	}
	
	public static boolean getBoolean(ByteBuffer buffer) {
		return buffer.get() == 1;
	}
	
	public static void putOperation(ByteBuffer buffer, Operation op) {
		buffer.put((byte)op.ordinal());
	}
	
	public static Operation getOperation(ByteBuffer buffer) {
		return Operation.values()[buffer.get()];
	}
	
	public static void end(ByteBuffer buffer) {
		buffer.put(END_CODE);
	}
	
	public static boolean isBufferFinished(ByteBuffer buffer) {
		buffer.mark();
		int length = buffer.remaining();
		int code_len = END_CODE.length;
		if(code_len > length) return false;
		byte end[] = new byte[code_len];
		buffer.position(length - code_len);
		buffer.get(end, 0, code_len);
		boolean finished = true;
		for(int i = 0; i < code_len && finished; i++) {
			finished = end[i] == END_CODE[i];
		}
		buffer.reset();
		return finished;
	}
	
}
