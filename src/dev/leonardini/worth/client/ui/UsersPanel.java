package dev.leonardini.worth.client.ui;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import dev.leonardini.worth.client.ui.assets.PropicManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class UsersPanel extends JPanel {

	private static final long serialVersionUID = -2568054359984653653L;
	
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
		SwingUtilities.updateComponentTreeUI(this);
		
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
		
		for(int i = 0; i < 20; i++) {
			setOffline("" + i);
		}
		setOnline("pianka");
		setOnline("pippo");
		setOnline("pluto");
		setOffline("paperino");
		setOffline("minnie");
		
		reload();
	}
	
	private void reload() {
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
		SwingUtilities.updateComponentTreeUI(this);
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
	
	class UsersPanelElement extends JPanel {

		private static final long serialVersionUID = -1634521492568386834L;

		public UsersPanelElement(String username) {
			setLayout(null);
			JLabel propic = PropicManager.get(username);
			propic.setBounds(0, 0, PropicManager.SIZE, PropicManager.SIZE);
			JLabel name = new JLabel(username);
			name.setBounds(PropicManager.SIZE + 5, 0, 100, PropicManager.SIZE);
			name.setFont(FontUtils.USER_PANEL_FONT);
			name.setVerticalAlignment(SwingConstants.CENTER);
			add(propic);
			add(name);
		}
		
	}
	
}
