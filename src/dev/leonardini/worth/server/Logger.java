package dev.leonardini.worth.server;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	private static WritableByteChannel file;
	private static ByteBuffer buffer;
	private static boolean verbose = true;
	
	private static void init() {
		try {
			file = Channels.newChannel(new FileOutputStream("logs.txt", true));
			buffer = ByteBuffer.allocateDirect(1024);
			buffer.put("--------------------------------------------------\n".getBytes());
			buffer.flip();
			while(buffer.hasRemaining())
				file.write(buffer);
			buffer.compact();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void logToFile(String level, String message) {
		synchronized(file) {
			try {
				buffer.put((level + message + '\n').getBytes());
				buffer.flip();
				while(buffer.hasRemaining())
					file.write(buffer);
				buffer.compact();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void Trace(String message) {
		if(file == null) {
			init();
		}
		message = date() + "[Server] " + message;
		if(verbose)
			System.out.println(message);
		logToFile("[T]", message);
	}
	
	public static void Error(String message) {
		if(file == null) {
			init();
		}
		message = date() + "[Server] " + message;
		if(verbose)
			System.err.println(message);
		logToFile("[E]", message);
	}
	
	private static String date() {
		return new SimpleDateFormat("[dd/MM/yy HH:mm:ss.SSS]").format(new Date());
	}
	
	public static void verbose(boolean verbose) {
		Logger.verbose = verbose;
	}
	
}
