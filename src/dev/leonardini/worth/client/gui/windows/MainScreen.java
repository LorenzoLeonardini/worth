package dev.leonardini.worth.client.gui.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.panels.ChatPanel;
import dev.leonardini.worth.client.gui.panels.MainPanel;
import dev.leonardini.worth.client.gui.panels.NoChatPanel;
import dev.leonardini.worth.client.gui.panels.ProjectPanel;
import dev.leonardini.worth.client.gui.panels.UsersPanel;

public class MainScreen extends JFrame {

	private static final long serialVersionUID = 613339661340118082L;

	public static final int USER_LIST_SIZE = 175;
	public static final int CHAT_SIZE = 275;
	
	private JPanel mainPanel;
	private JPanel noChat;
	private String username;
	private ChatPanel currentChat;
	private ClientAPI serverConnection;
	
	private void addSeparator(JPanel panel, int gridx) {
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 1));
        separator.setForeground(Color.gray);
        GridBagConstraints gbc_separator = new GridBagConstraints();
        gbc_separator.insets = new Insets(0, 0, 0, 0);
        gbc_separator.gridx = gridx;
        gbc_separator.gridy = 0;
        gbc_separator.fill = GridBagConstraints.VERTICAL;
        gbc_separator.weighty = 1;
        getContentPane().add(separator, gbc_separator);
	}

	public MainScreen(String username, ClientAPI serverConnection) {
		this.username = username;
		this.serverConnection = serverConnection;
		this.setSize(1100, 630);
		this.setLocationRelativeTo(null);
		this.setTitle("WORkTogetHer");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		mainPanel = (JPanel) this.getContentPane();
		mainPanel.setLayout(new GridBagLayout());
		
		noChat = new NoChatPanel();
		noChat.setPreferredSize(new Dimension(CHAT_SIZE, 10));
		
		UsersPanel usersPanel = new UsersPanel();
		usersPanel.setUsers(serverConnection.listUsers());
		serverConnection.registerUsersRMI(usersPanel);
		
		JScrollPane scrollableUsers = new JScrollPane();
		scrollableUsers.setViewportView(usersPanel);
		scrollableUsers.setAutoscrolls(true);
		scrollableUsers.setBorder(BorderFactory.createEmptyBorder());
		scrollableUsers.getVerticalScrollBar().setUnitIncrement(12);
		scrollableUsers.setPreferredSize(new Dimension(USER_LIST_SIZE, 10));
		JPanel centralPanel = new MainPanel(username, serverConnection, this);
		
		mainPanel.add(scrollableUsers,  new GridBagConstraints(0, 0, 1, 1, 0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		addSeparator(mainPanel, 1);
		mainPanel.add(centralPanel,  new GridBagConstraints(2, 0, 1, 1, 1, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		addSeparator(mainPanel, 3);
        mainPanel.add(noChat,  new GridBagConstraints(4, 0, 1, 1, 0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.setVisible(true);
	}
	
	public void openChat(String projectName, ProjectPanel projectPanel) {
		noChat.setVisible(false);
		currentChat = new ChatPanel(username, projectName, projectPanel, serverConnection);
		currentChat.setPreferredSize(new Dimension(CHAT_SIZE, 10));
		mainPanel.add(currentChat,  new GridBagConstraints(4, 0, 1, 1, 0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		serverConnection.readChat(projectName, currentChat);
	}
	
	public void closeChat() {
		noChat.setVisible(true);
		mainPanel.remove(currentChat);
		serverConnection.exitChat();
	}
	
}
