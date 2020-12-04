package dev.leonardini.worth.server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public static void Trace(String message) {
		System.out.println(date() + "[Server] " + message);
	}
	
	public static void Error(String message) {
		System.err.println(date() + "[Server] " + message);
	}
	
	private static String date() {
		return new SimpleDateFormat("[dd/mm/yy HH:mm:ss.SSS]").format(new Date());
	}
	
}
