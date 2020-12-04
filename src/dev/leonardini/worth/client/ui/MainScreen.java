package dev.leonardini.worth.client.ui;

import java.awt.BorderLayout;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MainScreen extends JFrame {

	private static final long serialVersionUID = -1653094696197849470L;
	
	public static final int USER_LIST_SIZE = 175;
	public static final int CHAT_SIZE = 275;
	
	private JPanel mainPanel;
	
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

	public MainScreen() {
		this.setSize(1100, 630);
		this.setLocationRelativeTo(null);
		this.setTitle("WORkTogetHer");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		mainPanel = (JPanel) this.getContentPane();
		mainPanel.setLayout(new GridBagLayout());
		
		JScrollPane scrollableUsers = new JScrollPane();
		JPanel usersPanel = new UsersPanel();
		scrollableUsers.setViewportView(usersPanel);
		scrollableUsers.setAutoscrolls(true);
		scrollableUsers.setBorder(BorderFactory.createEmptyBorder());
		scrollableUsers.getVerticalScrollBar().setUnitIncrement(12);
		scrollableUsers.setPreferredSize(new Dimension(USER_LIST_SIZE, 10));
		JPanel centralPanel = new MainPanel("pianka");
		JPanel chatPanel = new ChatPanel("pianka");
		chatPanel.setPreferredSize(new Dimension(CHAT_SIZE, 10));
		
		mainPanel.add(scrollableUsers,  new GridBagConstraints(0, 0, 1, 1, 0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		addSeparator(mainPanel, 1);
		mainPanel.add(centralPanel,  new GridBagConstraints(2, 0, 1, 1, 1, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		addSeparator(mainPanel, 3);
        mainPanel.add(chatPanel,  new GridBagConstraints(4, 0, 1, 1, 0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		this.setVisible(true);
	}
	
}
