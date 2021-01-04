package dev.leonardini.worth.client;

import java.nio.channels.SocketChannel;

import dev.leonardini.worth.networking.NetworkUtils.Operation;
import dev.leonardini.worth.networking.WorthBuffer;

/**
 * This is a helper class used to simplify the process of communicating with the server
 * The main method is the send function, which is responsible for sending the buffer to
 * the server, get the response, analyze the response to verify the operation code and
 * the outcome status
 */
public class ServerCommunication {

	private Operation op;
	private WorthBuffer buffer;
	private SocketChannel socket;
	private String errorMessage;
	
	/**
	 * Initialize a server communication for a specific operation and on a
	 * specific socket
	 * @param op
	 * @param socket
	 */
	public ServerCommunication(Operation op, SocketChannel socket) {
		this.op = op;
		this.socket = socket;
		this.buffer = new WorthBuffer(1024);
		this.buffer.putOperation(op);
	}
	
	public WorthBuffer getBuffer() {
		return this.buffer;
	}
	
	/**
	 * Send the buffer to the server, check the response for the proper
	 * op code.
	 * @return the status code
	 */
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
