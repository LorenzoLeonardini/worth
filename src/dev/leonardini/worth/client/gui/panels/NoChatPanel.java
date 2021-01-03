package dev.leonardini.worth.client.gui.panels;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.gui.assets.GuiUtils;

/**
 * Simple panel displaying a message telling the user to open a project
 * in order to chat
 */
public class NoChatPanel extends JPanel {

	private static final long serialVersionUID = -2422038220952298377L;

	private JLabel title;
	private JLabel messages;
	
	public NoChatPanel() {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		title = new JLabel("Chat");
		title.setFont(GuiUtils.USERNAME_FONT);
		title.setPreferredSize(new Dimension(100, 20));
		add(title);
		
		messages = new JLabel("<html><body style='text-align: center'>Apri un progetto per accedere alla chat</body></html>");
		messages.setVerticalAlignment(SwingConstants.CENTER);
		messages.setHorizontalAlignment(SwingConstants.CENTER);
		messages.setPreferredSize(new Dimension(175, 10));
		add(messages);
		
		layout.putConstraint(SpringLayout.NORTH, title, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, title, -5, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.NORTH, messages, 10, SpringLayout.SOUTH, title);
		layout.putConstraint(SpringLayout.WEST, messages, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, messages, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, messages, -6, SpringLayout.SOUTH, this);
	}
	
}	
