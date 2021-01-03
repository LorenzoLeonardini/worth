package dev.leonardini.worth.client.gui.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.gui.assets.GuiUtils;
import dev.leonardini.worth.client.gui.assets.PropicManager;

/**
 * Component representing a user in the "users list" column
 */
public class UsersPanelElement extends JPanel {

	private static final long serialVersionUID = -4534260090855783841L;

	private JLabel propic;
	private String username;

	/**
	 * Initiate the object
	 * 
	 * @param username
	 */
	public UsersPanelElement(String username) {
		this.username = username;
		setLayout(null);
		propic = PropicManager.get(username);
		propic.setBounds(0, 0, PropicManager.SIZE, PropicManager.SIZE);
		JLabel name = new JLabel(username);
		name.setBounds(PropicManager.SIZE + 5, 0, 100, PropicManager.SIZE);
		name.setFont(GuiUtils.USER_PANEL_FONT);
		name.setVerticalAlignment(SwingConstants.CENTER);
		add(propic);
		add(name);
	}
	
	/**
	 * Call this method when the user update their propic. This will reload it
	 * and refresh the component
	 */
	public void updatePic() {
		propic.setIcon(PropicManager.get(username).getIcon());
		invalidate();
		validate();
		repaint();
	}
			
}
