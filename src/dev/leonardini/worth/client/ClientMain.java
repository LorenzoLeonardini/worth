package dev.leonardini.worth.client;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dev.leonardini.worth.client.ui.LoginScreen;

public class ClientMain {
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		ClientAPI connection = new ClientAPI();
		JFrame frame = new LoginScreen(connection);
		frame.setVisible(true);
	}
	
}
