package dev.leonardini.worth.client.gui.panels;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.AssetsManager;
import dev.leonardini.worth.client.gui.assets.FontUtils;
import dev.leonardini.worth.client.gui.assets.PropicManager;
import dev.leonardini.worth.client.gui.windows.MainScreen;
import dev.leonardini.worth.client.gui.windows.SettingsDialog;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1131461282211603252L;

	private JLabel usernameLabel;
	private JLabel title;
	private JLabel propic;
	private JLabel settings;
	private JScrollPane projectList;
	private JPanel projectListPanel;
	
	private MainScreen mainScreen;
	
	public MainPanel(String username, MainScreen mainScreen) {
		this.mainScreen = mainScreen;
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
				JDialog f = new SettingsDialog();
				f.setVisible(true);
			}
		});
		settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		projectListPanel = new ProjectListPanel(this);
		projectList = new JScrollPane();
		projectList.setViewportView(projectListPanel);
		projectList.setAutoscrolls(true);
		projectList.setBorder(BorderFactory.createEmptyBorder());
		projectList.getVerticalScrollBar().setUnitIncrement(12);
		
		add(title);
		add(usernameLabel);
		add(propic);
		add(settings);
		add(projectList);
		
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
		projectList.setBounds(0, 20 + PropicManager.SIZE, width, height - PropicManager.SIZE - 20);
		updateUI();
	}
	
	protected void openProject(String projectName) {
		ProjectPanel projectPanel = new ProjectPanel(projectName, this);
		projectList.setViewportView(projectPanel);
		mainScreen.openChat(projectName, projectPanel);
	}

	public void backToProjectList() {
		projectList.setViewportView(projectListPanel);
		((ProjectListPanel)projectListPanel).refresh();
		mainScreen.closeChat();
	}
}
