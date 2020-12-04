package dev.leonardini.worth.client.ui;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ui.assets.AssetsManager;
import dev.leonardini.worth.client.ui.assets.PropicManager;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 7674340927335094457L;

	private JLabel usernameLabel;
	private JLabel title;
	private JLabel propic;
	private JLabel settings;
	private JSeparator separator;
	
	public MainPanel(String username) {
		setLayout(null);
		
		title = new JLabel("WORkTogetHer");
		title.setFont(FontUtils.MAIN_TITLE_FONT);
		title.setForeground(FontUtils.COLOR);
	
		usernameLabel = new JLabel(username);
		usernameLabel.setFont(FontUtils.USERNAME_FONT);
		usernameLabel.setVerticalAlignment(SwingConstants.CENTER);
		usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		propic = PropicManager.get(username);
		
		settings = new JLabel(AssetsManager.COG);
		settings.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFrame f = new SettingsScreen();
				f.setVisible(true);
			}
		});
		settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		add(title);
		add(usernameLabel);
		add(propic);
		add(settings);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				update();
			}
		});
	}
	
	public void update() {
		int width = getWidth();
		int height = getHeight();
		settings.setBounds(width - PropicManager.SIZE - 10, 10, PropicManager.SIZE, PropicManager.SIZE);
		propic.setBounds(width - PropicManager.SIZE - 10 - PropicManager.SIZE - 10, 10, PropicManager.SIZE, PropicManager.SIZE);
		usernameLabel.setBounds(width - PropicManager.SIZE * 2 - 300 - 30, 10, 300, PropicManager.SIZE);
		title.setBounds(20, 10, 500, PropicManager.SIZE);
	}
}
