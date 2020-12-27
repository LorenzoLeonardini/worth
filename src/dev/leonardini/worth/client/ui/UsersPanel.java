package dev.leonardini.worth.client.ui;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import dev.leonardini.worth.client.UserUpdateCallback;
import dev.leonardini.worth.client.ui.assets.PropicManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class UsersPanel extends JPanel implements UserUpdateCallback {

	private static final long serialVersionUID = -2888416411224027971L;

	private Map<String, UsersPanelElement> offline_users = new HashMap<String, UsersPanelElement>();
	private Map<String, UsersPanelElement> online_users = new HashMap<String, UsersPanelElement>();
	
	private JLabel offlineLabel;
	private JLabel onlineLabel;

	public UsersPanel() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				reload();
			}
		});
		setLayout(null);
		setVisible(true);
		
		invalidate();
		validate();
		repaint();
		
		offlineLabel = new JLabel("Utenti offline:");
		offlineLabel.setFont(FontUtils.USER_PANEL_TITLE_FONT);
		offlineLabel.setSize(150, 20);
		offlineLabel.setVerticalAlignment(SwingConstants.CENTER);
		add(offlineLabel);
		
		onlineLabel = new JLabel("Utenti online:");
		onlineLabel.setFont(FontUtils.USER_PANEL_TITLE_FONT);
		onlineLabel.setBounds(0, 0, 150, 20);
		onlineLabel.setVerticalAlignment(SwingConstants.CENTER);
		add(onlineLabel);
	}
	
	public void reload() {
		int y = 20;
		for(UsersPanelElement e : online_users.values()) {
			e.setBounds(0, y, 150, PropicManager.SIZE);
			y += PropicManager.SIZE + 5;
		}
		y += 10;
		offlineLabel.setBounds(0, y, 150, 20);
		y += 20;
		for(UsersPanelElement e : offline_users.values()) {
			e.setBounds(0, y, 150, PropicManager.SIZE);
			y += PropicManager.SIZE + 5;
		}
		
		invalidate();
		validate();
		repaint();
		
		if(getParent() != null)
			setPreferredSize(new Dimension(150, Math.max(y, getParent().getSize().height)));
		else
			setPreferredSize(new Dimension(150, y));
	}
	
	public void setOffline(String username) {
		updateStructure(username, online_users, offline_users);
	}
	
	public void setOnline(String username) {
		updateStructure(username, offline_users, online_users);
	}
	
	public void setUsers(Map<String, Boolean> users) {
		for (String u : users.keySet()) {
			if(users.get(u))
				setOnline(u);
			else
				setOffline(u);
		}
		reload();
	}
	
	private void updateStructure(String username, Map<String, UsersPanelElement> from, Map<String, UsersPanelElement> to) {
		if(to.containsKey(username)) return;
		UsersPanelElement view;
		if(from.containsKey(username)) {
			view = from.get(username);
			from.remove(username);
		} else {
			view = new UsersPanelElement(username);
			add(view);
		}
		to.put(username, view);
	}

	@Override
	public void updateUserStatus(String username, boolean status) {
		if(status)
			setOnline(username);
		else
			setOffline(username);
		reload();
	}

	@Override
	public void updateUserPropic(String username) {
		UsersPanelElement panel = online_users.get(username);
		if(panel == null)
			panel = offline_users.get(username);
		if(panel == null) return;
		panel.updatePic();
		reload();
	}
	
}
