package dev.leonardini.worth.client.ui;

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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.ui.assets.PropicManager;

public class MembersScreen extends JDialog {

	private static final long serialVersionUID = 1155003498356191669L;

	private ClientAPI serverConnection;
	private JPanel panel;
	private LoadingPanel loadingPanel = new LoadingPanel();
	private int y_position = 60;
	private String projectName;
	private JLabel errorMessage;
	private JScrollPane scrollPane;

	public MembersScreen(String projectName, ClientAPI serverConnection) {
		this.projectName = projectName;
		this.serverConnection = serverConnection;
		setSize(220, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Lista membri");
		setAlwaysOnTop(true);
		panel = new JPanel();
		panel.setLayout(null);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
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
		
		List<String> members = serverConnection.showMembers(projectName);
		for(String member : members) {
			displayUser(member);
		}
	}
	
	private void displayUser(String username) {
		System.out.println("adding");
		
		UsersPanelElement u = new UsersPanelElement(username);
		u.setBounds(0, y_position, 150, PropicManager.SIZE);
		y_position += PropicManager.SIZE + 5;
		panel.add(u);
		
		SwingUtilities.updateComponentTreeUI(this);
		if(panel.getParent() != null)
			panel.setPreferredSize(new Dimension(200, Math.max(y_position, getHeight())));
		else
			panel.setPreferredSize(new Dimension(200, y_position));
	}
	
	private void addMember(String username) {
		scrollPane.setViewportView(loadingPanel);
		invalidate();
		validate();
		repaint();
		new Thread(() -> {
			errorMessage.setVisible(false);
			if(serverConnection.addMember(projectName, username)) {
				displayUser(username);
			} else {
				errorMessage.setText(serverConnection.getMessage());
				errorMessage.setVisible(true);
			}
			scrollPane.setViewportView(panel);
			invalidate();
			validate();
			repaint();
		}).start();
	}
	
}
