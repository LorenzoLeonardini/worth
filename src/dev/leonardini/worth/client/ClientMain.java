package dev.leonardini.worth.client;

import javax.swing.JFrame;

import dev.leonardini.worth.client.ui.LoginScreen;

public class ClientMain {
	
	public static void main(String[] args) {
		ServerConnection connection = new ServerConnection();
		JFrame frame = new LoginScreen(connection);
		frame.setVisible(true);
	}
	
}
