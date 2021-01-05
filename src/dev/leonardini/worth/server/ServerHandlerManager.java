package dev.leonardini.worth.server;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;
import dev.leonardini.worth.server.ServerTCP.Session;

/**
 * Handles incoming communications from clients. Register a handler for a specific operation
 * code and call `handle` when a selection key is readable.
 * 
 * The registered handler is responsible for reading data from the client and writing to a
 * WorthBuffer which will need to be sent.
 * 
 * This handler manager allows to specify requirements for each operation. Does the client need
 * to be logged? Do they need to be members of a project? These checks will be performed here
 */
public class ServerHandlerManager {

	private Map<Operation, ServerHandler> handlers = new HashMap<Operation, ServerHandler>();
	private Map<Operation, Integer> requirements = new HashMap<Operation, Integer>();
	
	// Requirement definition
	public static final int NONE = 0;
	public static final int LOGGED = 1;
	private static final int MEMBER = 2;
	public static final int PROJECT_MEMBER = LOGGED | MEMBER;
	
	/**
	 * Register a handler for a specific operation. Note that handlers are unique and registering
	 * another handler for the same operation will overwrite the original handler.
	 * @param op
	 * @param handler
	 * @param requirements
	 */
	public void addHandler(Operation op, ServerHandler handler, int requirements) {
		handlers.put(op, handler);
		this.requirements.put(op, requirements);
	}
	
	/**
	 * Call this function when the client is ready to be sent data.
	 * A proper handler will be called according to the operation code.
	 * 
	 * @param key
	 * @return the WorthBuffer to send back to the user
	 */
	public WorthBuffer handle(SelectionKey key) {
		try {
		// Get some data and initialize some objects
		Session session = (Session) key.attachment();
		Operation op = session.buffer.getOperation();
		int requirements = this.requirements.get(op);
		WorthBuffer out = new WorthBuffer();
		out.putOperation(op);
		
		// If the requirements need the user to be logged,
		// verify that
		if((requirements & LOGGED) > 0) {
			if(!session.logged) {
				out.putBoolean(false);
				out.putString("È necessario l'accesso");
				return out;
			}
		}
		// If the requirements need the user to be part of the given project,
		// verify that. This relies on the defined protocol for worth communications
		if((requirements & MEMBER) > 0) {
			session.buffer.mark();
			String projectName = session.buffer.getString();
			session.buffer.reset();
			try {
				ProjectDB.checkProjectMembership(session.username, projectName);
			} catch(Exception e) {
				out.putBoolean(false);
				out.putString(e.getMessage());
				return out;
			}
		}
		
		handlers.get(op).handle(session, key, out);
		session.buffer.clear();
		return out;
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	@FunctionalInterface
	public static interface ServerHandler {
		public void handle(Session session, SelectionKey key, WorthBuffer out);
	}
	
}
