package dev.leonardini.worth.client;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dev.leonardini.worth.client.gui.windows.LoginScreen;

public class ClientMain {
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		JFrame frame = new LoginScreen();
		frame.setVisible(true);
	}
	
}
