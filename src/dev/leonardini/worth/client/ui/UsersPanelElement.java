package dev.leonardini.worth.client.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ui.assets.PropicManager;

public class UsersPanelElement extends JPanel {

	private static final long serialVersionUID = -4534260090855783841L;

	private JLabel propic;
	private String username;

	public UsersPanelElement(String username) {
		this.username = username;
		setLayout(null);
		propic = PropicManager.get(username);
		propic.setBounds(0, 0, PropicManager.SIZE, PropicManager.SIZE);
		JLabel name = new JLabel(username);
		name.setBounds(PropicManager.SIZE + 5, 0, 100, PropicManager.SIZE);
		name.setFont(FontUtils.USER_PANEL_FONT);
		name.setVerticalAlignment(SwingConstants.CENTER);
		add(propic);
		add(name);
	}
	
	public void updatePic() {
		propic.setIcon(PropicManager.get(username).getIcon());
	}
			
}
