package dev.leonardini.worth.client;

import java.nio.channels.SocketChannel;

import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;

public class ServerCommunication {

	private Operation op;
	private WorthBuffer buffer;
	private SocketChannel socket;
	private String errorMessage;
	
	public ServerCommunication(Operation op, SocketChannel socket) {
		this.op = op;
		this.socket = socket;
		this.buffer = new WorthBuffer(1024);
		this.buffer.putOperation(op);
	}
	
	public WorthBuffer getBuffer() {
		return this.buffer;
	}
	
	public boolean send() {
		try {
			buffer.write(socket);
	
			// Receive response
			buffer.clear();
			buffer.rewind();
			buffer.read(socket);
			
			if(buffer.getOperation() != op) {
				errorMessage = "Errore di comunicazione";
				return false;
			}
			if(buffer.getBoolean()) {
				return true;
			}
			errorMessage = buffer.getString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
