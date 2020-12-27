package dev.leonardini.worth.server;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;
import dev.leonardini.worth.server.ServerMain.Session;

public class ServerHandler {

	private Map<Operation, OperationServerHandler> handlers = new HashMap<Operation, OperationServerHandler>();
	private Map<Operation, Integer> requirements = new HashMap<Operation, Integer>();
	public static final int NONE = 0;
	public static final int LOGGED = 1;
	private static final int MEMBER = 2;
	public static final int PROJECT_MEMBER = LOGGED | MEMBER;
	
	public void addHandler(Operation op, OperationServerHandler handler, int requirements) {
		handlers.put(op, handler);
		this.requirements.put(op, requirements);
	}
	
	public WorthBuffer handle(SelectionKey key) {
		Session session = (Session) key.attachment();
		Operation op = session.current_operation;
		int requirements = this.requirements.get(op);
		WorthBuffer out = new WorthBuffer();
		out.putOperation(session.current_operation);
		
		if((requirements & LOGGED) > 0) {
			if(!session.logged) {
				out.putBoolean(false);
				out.putString("È necessario l'accesso");
				return out;
			}
		}
		if((requirements & MEMBER) > 0) {
			session.buffer.mark();
			String projectName = session.buffer.getString();
			session.buffer.reset();
			List<String> projectMembers = ProjectDB.getMembers(projectName);
			if(projectMembers == null) {
				out.putBoolean(false);
				out.putString("Progetto inesistente");
				return out;
			}
			if(!projectMembers.contains(session.username)) {
				out.putBoolean(false);
				out.putString("Non fai parte del progetto");
				return out;
			}
		}
		
		handlers.get(op).handle(session, key, out);
		session.buffer.clear();
		return out;
	}
	
	@FunctionalInterface
	public static interface OperationServerHandler {
		public void handle(Session session, SelectionKey key, WorthBuffer out);
	}
	
}
