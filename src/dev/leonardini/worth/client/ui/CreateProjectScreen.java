package dev.leonardini.worth.client.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dev.leonardini.worth.client.ClientAPI;

public class CreateProjectScreen extends JDialog {

	private static final long serialVersionUID = 5676070794513778122L;

	private ClientAPI serverConnection;
	private LoadingPanel loadingPanel = new LoadingPanel();
	private JPanel mainPanel;
	private JLabel errorMessage;
	
	private ProjectListPanel projectList;

	public CreateProjectScreen(ClientAPI serverConnection, ProjectListPanel projectList) {
		this.serverConnection = serverConnection;
		this.projectList = projectList;
		setSize(300, 310);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Crea progetto");
		mainPanel = (JPanel) getContentPane();
		mainPanel.setLayout(null);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		JLabel createTitle = new JLabel("Crea un nuovo progetto");
		createTitle.setFont(FontUtils.USERNAME_FONT);
		createTitle.setVerticalAlignment(SwingConstants.CENTER);
		createTitle.setHorizontalAlignment(SwingConstants.CENTER);
		createTitle.setBounds(0, 45, 300, 40);
		mainPanel.add(createTitle);
		
		JLabel projectNameLabel = new JLabel("Nome del progetto:");
		projectNameLabel.setVerticalAlignment(SwingConstants.CENTER);
		projectNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		projectNameLabel.setBounds(25, 90, 250, 40);
		mainPanel.add(projectNameLabel);
		
		JTextField projectName = new JTextField();
		projectName.setBounds(30, 130, 240, 30);
		projectName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					createProject(projectName.getText());
				}
			}
		});
		mainPanel.add(projectName);
		
		errorMessage = new JLabel();
		errorMessage.setBounds(30, 160, 240, 30);
		errorMessage.setForeground(Color.red);
		errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
		errorMessage.setVerticalAlignment(SwingConstants.CENTER);
		errorMessage.setFont(new Font("Dialog", Font.PLAIN, 12));
		errorMessage.setVisible(false);
		mainPanel.add(errorMessage);
		
		JButton save = new JButton("Salva");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createProject(projectName.getText());
			}
		});
		save.setBounds(100, 190, 100, 30);
		mainPanel.add(save);
	}
	
	private void createProject(String projectName) {
		setContentPane(loadingPanel);
		invalidate();
		validate();
		repaint();
		new Thread(() -> {
			errorMessage.setVisible(false);
			if(serverConnection.createProject(projectName)) {
				projectList.refresh();
				dispose();
			} else {
				errorMessage.setText(serverConnection.getMessage());
				errorMessage.setVisible(true);
				setContentPane(mainPanel);
				invalidate();
				validate();
				repaint();
			}
		}).start();
	}
	
}
