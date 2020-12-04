package dev.leonardini.worth.client.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import dev.leonardini.worth.client.ui.assets.PropicManager;

public class ChatPanel extends JPanel {

	private static final long serialVersionUID = -8597017979489297098L;
	
	private JLabel title;
	private JScrollPane scrollable;
	private JPanel messages;
	private SpringLayout messagesLayout;
	private JTextArea input;
	
	private String my_username;
	private ChatMessage lastAdded = null;

	public ChatPanel(String username) {
		this.my_username = username;
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		title = new JLabel("Chat");
		title.setFont(FontUtils.USERNAME_FONT);
		title.setPreferredSize(new Dimension(100, 20));
		add(title);
		
		messages = new JPanel();
		messagesLayout = new SpringLayout();
		messages.setLayout(messagesLayout);
		
		scrollable = new JScrollPane();
		scrollable.setViewportView(messages);
		scrollable.setAutoscrolls(true);
		scrollable.setBorder(BorderFactory.createEmptyBorder());
		scrollable.getVerticalScrollBar().setUnitIncrement(12);
		scrollable.setPreferredSize(new Dimension(175, 10));
		add(scrollable);
		
		input = new JTextArea();
		input.setPreferredSize(new Dimension(100, 60));
		input.setBorder(BorderFactory.createLineBorder(Color.gray));
		add(input);
		
		layout.putConstraint(SpringLayout.NORTH, title, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, title, -5, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.NORTH, scrollable, 10, SpringLayout.SOUTH, title);
		layout.putConstraint(SpringLayout.WEST, scrollable, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, scrollable, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, scrollable, -10, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.SOUTH, input, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, input, 6, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, input, -6, SpringLayout.EAST, this);
		
		addMessage("paolo", "Prova 123");
		addMessage("paolo", "Prova 12345677");
		addMessage("asdasd", "Ascolta le :Papille Gustative");
		addMessage("pianka", "<3");
		systemMessage("System message");
		addMessage("pianka", "<3");
		addMessage("pianka", "<3");
		addMessage("pianka", "<3");
		addMessage("pianka", "<3");
	}
	
	public void addMessage(String username, String message) {
		boolean icon = lastAdded == null || !lastAdded.username.equalsIgnoreCase(username);
		if(!icon) lastAdded.removeBottomBorder();
		boolean me = username.equalsIgnoreCase(my_username);
		
		ChatMessage new_message = new ChatMessage(username, message, icon, me);
		appendMessage(new_message, icon);
	}
	
	public void systemMessage(String message) {
		ChatMessage new_message = new ChatMessage(message);
		appendMessage(new_message, true);
	}
	
	private void appendMessage(ChatMessage new_message, boolean icon) {
		if(lastAdded == null)
			messagesLayout.putConstraint(SpringLayout.NORTH, new_message, 5, SpringLayout.NORTH, messages);
		else
			messagesLayout.putConstraint(SpringLayout.NORTH, new_message, icon ? 5 : -5, SpringLayout.SOUTH, lastAdded);
		messagesLayout.putConstraint(SpringLayout.WEST, new_message, 5, SpringLayout.WEST, messages);
		messagesLayout.putConstraint(SpringLayout.EAST, new_message, -5, SpringLayout.EAST, messages);
		messages.add(new_message);
		lastAdded = new_message;
		
		Dimension size = messages.getPreferredSize();
		size.height += new_message.getPreferredSize().height += 10;
		messages.setPreferredSize(size);
		
		scrollable.getVerticalScrollBar().setValue(scrollable.getVerticalScrollBar().getMaximum());
		
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	class ChatMessage extends JPanel {

		private static final long serialVersionUID = -8733742095087484567L;
		public String username;
		private boolean icon;
		private boolean system = false;
		
		private JLabel text;
		
		public ChatMessage(String username, String message, boolean icon, boolean mine) {
			message = message.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
			this.username = username;
			this.icon = icon;
			setLayout(null);
			
			int height = 0;
			
			if(icon) {
				height = 10;
				JLabel propic = PropicManager.get(username);
				propic.setBounds(mine ? MainScreen.CHAT_SIZE - PropicManager.SIZE - 27 : 0, height, PropicManager.SIZE, PropicManager.SIZE);
				
				JLabel usernameLabel = new JLabel(username);
				usernameLabel.setFont(FontUtils.CHAT_USER_FONT);
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
	        text.setFont(FontUtils.CHAT_FONT);
	        add(text);
	        
	        setPreferredSize(new Dimension(text.getWidth(), height));
		}
		
		public ChatMessage(String message) {
			message = message.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
			this.username = "";
			system = true;
			
			setLayout(null);
			
			int height = 0;
			
	        text = new JLabel("<html><body style='padding: 8px; text-align: center; width: " + (MainScreen.CHAT_SIZE * 0.5) + "px'>" + message + "</body></html>");
	        
        	Dimension size = text.getPreferredSize();
        	text.setBounds(35, height, size.width, size.height);
	        height += text.getHeight();
	        text.setFont(FontUtils.CHAT_SYSTEM_FONT);
	        add(text);
	        
	        setPreferredSize(new Dimension(text.getWidth(), height));
		}
		
		public void removeBottomBorder() {
			if(system) return;
			text.setBorder(BorderFactory.createMatteBorder(icon ? 1 : 0, 1, 0, 1, Color.gray));
		}
		
	}
	
}
