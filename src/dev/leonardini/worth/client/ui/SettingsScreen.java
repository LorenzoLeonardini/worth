package dev.leonardini.worth.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dev.leonardini.worth.client.ClientAPI;

public class SettingsScreen extends JDialog {

	private static final long serialVersionUID = 6378209988325009361L;

	private ClientAPI serverConnection;

	public SettingsScreen(ClientAPI serverConnection) {
		this.serverConnection = serverConnection;
		setSize(300, 310);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Impostazioni");
		JPanel panel = (JPanel) getContentPane();
		panel.setLayout(null);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		JLabel settings = new JLabel("Impostazioni");
		settings.setFont(FontUtils.USERNAME_FONT);
		settings.setVerticalAlignment(SwingConstants.CENTER);
		settings.setHorizontalAlignment(SwingConstants.CENTER);
		settings.setBounds(0, 45, 300, 40);
		getContentPane().add(settings);
		
		JLabel emailLabel = new JLabel("Indirizzo email (gravatar):");
		emailLabel.setVerticalAlignment(SwingConstants.CENTER);
		emailLabel.setHorizontalAlignment(SwingConstants.CENTER);
		emailLabel.setBounds(25, 90, 250, 40);
		getContentPane().add(emailLabel);
		
		JTextField email = new JTextField();
		email.setBounds(30, 130, 240, 30);
		email.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					updatePic(email.getText());
				}
			}
		});
		getContentPane().add(email);
		
		JButton save = new JButton("Salva");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updatePic(email.getText());
			}
		});
		save.setBounds(100, 180, 100, 30);
		getContentPane().add(save);
	}
	
	private void updatePic(String email) {
		new Thread(() -> {
			dispose();
			serverConnection.updateProfilePicture(email);
		}).start();
	}
	
}
