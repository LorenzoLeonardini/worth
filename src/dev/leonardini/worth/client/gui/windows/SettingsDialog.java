package dev.leonardini.worth.client.gui.windows;

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

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.GuiUtils;

/**
 * Settings window used to set the Gravatar email address
 */
public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 6378209988325009361L;

	public SettingsDialog() {
		setSize(300, 310);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Impostazioni");
		setAlwaysOnTop(true);
		JPanel panel = (JPanel) getContentPane();
		panel.setLayout(null);
		
		JLabel settings = new JLabel("Impostazioni");
		settings.setFont(GuiUtils.USERNAME_FONT);
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
			// This shouldn't really give any error, since the only requirement is
			// that the user is logged (and this window can be displayed only after logging in)
			ClientAPI.get().updateProfilePicture(email);
		}).start();
	}
	
}
