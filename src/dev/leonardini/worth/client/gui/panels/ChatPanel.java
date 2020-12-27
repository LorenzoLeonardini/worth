package dev.leonardini.worth.client.gui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.ReceiveChatCallback;
import dev.leonardini.worth.client.gui.assets.FontUtils;
import dev.leonardini.worth.client.gui.components.ChatMessage;
import dev.leonardini.worth.data.Project.CardLocation;

public class ChatPanel extends JPanel implements ReceiveChatCallback {

	private static final long serialVersionUID = 1317326421983834032L;

	private JLabel title;
	private JScrollPane scrollable;
	private JPanel messages;
	private SpringLayout messagesLayout;
	private JTextArea input;
	
	private String my_username;
	private ChatMessage lastAdded = null;
	private ProjectPanel projectPanel;

	public ChatPanel(String username, String projectName, ProjectPanel projectPanel, ClientAPI clientApi) {
		this.my_username = username;
		this.projectPanel = projectPanel;
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
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					new Thread(() -> {
						clientApi.sendChatMsg(projectName, input.getText());
						input.setText("");
					}).start();
				}
			}
		});
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
		size.height += new_message.getPreferredSize().height + 5 + (icon ? 5 : -5);
		messages.setPreferredSize(size);
		
		scrollable.getVerticalScrollBar().setValue(scrollable.getVerticalScrollBar().getMaximum());
		
		invalidate();
		validate();
		repaint();
	}
	
	@Override
	public void receivedChatMessage(String username, String message) {
		addMessage(username, message);
	}

	@Override
	public void receivedSystemNotification(String user, String card, CardLocation from, CardLocation to) {
		projectPanel.moveCard(card, from, to);
		systemMessage(user + " ha spostato " + card + " in " + to);
	}
	
}
