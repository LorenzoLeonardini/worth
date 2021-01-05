package dev.leonardini.worth.client.gui.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.PropicManager;
import dev.leonardini.worth.client.gui.components.UsersPanelElement;
import dev.leonardini.worth.client.gui.panels.LoadingPanel;

/**
 * This window displays all the members of a project and allows to
 * add a new member to that project.
 * 
 * Note that if someone else is updating the members at the same time,
 * this view doesn't get updated.
 */
public class ProjectMembersDialog extends JDialog {

	private static final long serialVersionUID = 1155003498356191669L;

	private JPanel panel;
	private LoadingPanel loadingPanel = new LoadingPanel();
	private int y_position = 60;
	private String projectName;
	private JLabel errorMessage;
	private JScrollPane scrollPane;

	/**
	 * Init the object
	 * @param projectName the project of which we want to know the members
	 */
	public ProjectMembersDialog(String projectName) {
		this.projectName = projectName;
		setSize(220, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Lista membri");
		setAlwaysOnTop(true);
		panel = new JPanel();
		panel.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(panel);
		scrollPane.setAutoscrolls(true);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
		scrollPane.setBounds(0, 0, getWidth(), getHeight());
		getContentPane().add(scrollPane);
		
		JTextField username = new JTextField();
		username.setBounds(0, 0, 170, 30);
		username.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					addMember(username.getText());
				}
			}
		});
		panel.add(username);
		
		JButton add = new JButton("+");
		add.setBounds(170, 0, 30, 30);
		add.setMargin(new Insets(0, 0, 0, 0));
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addMember(username.getText());
			}
		});
		panel.add(add);
		
		errorMessage = new JLabel();
		errorMessage.setBounds(0, 30, getWidth(), 30);
		errorMessage.setForeground(Color.red);
		errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
		errorMessage.setVerticalAlignment(SwingConstants.CENTER);
		errorMessage.setFont(new Font("Dialog", Font.PLAIN, 12));
		errorMessage.setVisible(false);
		panel.add(errorMessage);
		
		// Retrieves the project members from the server.
		// This shouldn't generate any error, because this window should only
		// be opened for projects you are a member of and therefore you have the
		// correct permissions to perform this action.
		List<String> members = ClientAPI.get().showMembers(projectName);
		for(String member : members) {
			displayUser(member);
		}
	}
	
	private void displayUser(String username) {
		UsersPanelElement u = new UsersPanelElement(username);
		u.setBounds(0, y_position, 150, PropicManager.SIZE);
		y_position += PropicManager.SIZE + 5;
		panel.add(u);
		
		invalidate();
		validate();
		repaint();
		
		if(panel.getParent() != null)
			panel.setPreferredSize(new Dimension(200, Math.max(y_position, getHeight())));
		else
			panel.setPreferredSize(new Dimension(200, y_position));
	}
	
	/**
	 * Send the server a request to add a member to this project
	 * 
	 * @param username
	 */
	private void addMember(String username) {
		if(username.trim().length() == 0)
			return;
		
		scrollPane.setViewportView(loadingPanel);
		invalidate();
		validate();
		repaint();
		new Thread(() -> {
			errorMessage.setVisible(false);
			if(ClientAPI.get().addMember(projectName, username)) {
				displayUser(username);
			} else {
				errorMessage.setText(ClientAPI.get().getMessage());
				errorMessage.setVisible(true);
			}
			scrollPane.setViewportView(panel);
			invalidate();
			validate();
			repaint();
		}).start();
	}
	
}
