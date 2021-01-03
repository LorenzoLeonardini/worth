package dev.leonardini.worth.client.gui.components;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.gui.assets.GuiUtils;
import dev.leonardini.worth.client.gui.assets.PropicManager;
import dev.leonardini.worth.client.gui.windows.MainScreen;

/**
 * A component representing a chat message in the chat view.
 */
public class ChatMessage extends JPanel {

	private static final long serialVersionUID = -8733742095087484567L;
	public String username;
	private boolean icon;
	private boolean system = false;
	
	private JLabel text;
	
	/**
	 * Initiate the object. This constructor is used for user messages
	 * 
	 * @param username the user who sent the message
	 * @param message the content of the message
	 * @param icon when this is set to true, before the message a "header" is displayed, 
	 * 			containing the username and propic of the sender. Generally this is set to false
	 * 			from the second message sent from the user, to "group" the chat
	 * @param mine when this is set to true, the message is displayed on the right side instead of the
	 * 			left side. Used for messages sent by "me"
	 */
	public ChatMessage(String username, String message, boolean icon, boolean mine) {
		message = message.trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br />");
		this.username = username;
		this.icon = icon;
		setLayout(null);
		
		int height = 0;
		
		if(icon) {
			height = 10;
			JLabel propic = PropicManager.get(username);
			propic.setBounds(mine ? MainScreen.CHAT_SIZE - PropicManager.SIZE - 27 : 0, height, PropicManager.SIZE, PropicManager.SIZE);
			
			JLabel usernameLabel = new JLabel(username);
			usernameLabel.setFont(GuiUtils.CHAT_USER_FONT);
			usernameLabel.setVerticalAlignment(SwingConstants.CENTER);
			if(mine) {
				usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			usernameLabel.setBounds(mine ? MainScreen.CHAT_SIZE - PropicManager.SIZE - 32 - 150 : PropicManager.SIZE + 5, height, 150, PropicManager.SIZE);
			
			add(propic);
			add(usernameLabel);
			height += PropicManager.SIZE + 5;
		}
		
        text = new JLabel("<html><body style='background: white; padding: 8px; width: " + (MainScreen.CHAT_SIZE * 0.55) + "px'>" + message + "</body></html>");
        
    	Dimension size = text.getPreferredSize();
    	text.setBounds(mine ? MainScreen.CHAT_SIZE - size.width - 27 : 0, height, size.width, size.height);
        height += text.getHeight();
        text.setBorder(BorderFactory.createMatteBorder(icon ? 1 : 0, 1, 1, 1, Color.gray));
        text.setOpaque(true);
        text.setBackground(Color.white);
        text.setFont(GuiUtils.CHAT_FONT);
        add(text);
        
        setPreferredSize(new Dimension(text.getWidth(), height));
	}
	
	/**
	 * Initiate the object. This constructor is used for system messages
	 * 
	 * @param message the content of the message
	 */
	public ChatMessage(String message) {
		message = message.trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		this.username = "";
		system = true;
		
		setLayout(null);
		
        text = new JLabel("<html><body style='padding: 8px; text-align: center; width: " + (MainScreen.CHAT_SIZE * 0.5) + "px'>" + message + "</body></html>");
        
    	Dimension size = text.getPreferredSize();
    	text.setBounds(35, 0, size.width, size.height);
        text.setFont(GuiUtils.CHAT_SYSTEM_FONT);
        add(text);
        
        setPreferredSize(new Dimension(text.getWidth(), size.height));
	}
	
	/**
	 * Remove the border from the bottom side. Used when grouping messages
	 */
	public void removeBottomBorder() {
		if(system) return;
		text.setBorder(BorderFactory.createMatteBorder(icon ? 1 : 0, 1, 0, 1, Color.gray));
	}
	
}