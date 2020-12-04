package dev.leonardini.worth.client.ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsScreen extends JFrame {

	private static final long serialVersionUID = 1184624931400285351L;

	public SettingsScreen() {
		setSize(300, 310);
		setLocationRelativeTo(null);
		setResizable(false);
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
		getContentPane().add(email);
		
		JButton save = new JButton("Salva");
		save.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});
		save.setBounds(100, 180, 100, 30);
		getContentPane().add(save);
	}
	
}
