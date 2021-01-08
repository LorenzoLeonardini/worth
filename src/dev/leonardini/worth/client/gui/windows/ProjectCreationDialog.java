package dev.leonardini.worth.client.gui.windows;

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

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.GuiUtils;
import dev.leonardini.worth.client.gui.panels.LoadingPanel;
import dev.leonardini.worth.client.gui.panels.ProjectListPanel;

/**
 * Popup window used to create a project
 */
public class ProjectCreationDialog extends JDialog {

	private static final long serialVersionUID = 5676070794513778122L;

	private LoadingPanel loadingPanel = new LoadingPanel();
	private JPanel mainPanel;
	private JLabel errorMessage;
	
	private ProjectListPanel projectList;

	/**
	 * @param projectList this is used to refresh the project list after creating the project
	 */
	public ProjectCreationDialog(ProjectListPanel projectList) {
		this.projectList = projectList;
		setSize(300, 310);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Crea progetto");
		setAlwaysOnTop(true);
		mainPanel = (JPanel) getContentPane();
		mainPanel.setLayout(null);
		
		JLabel createTitle = new JLabel("Crea un nuovo progetto");
		createTitle.setFont(GuiUtils.USERNAME_FONT);
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
	
	/**
	 * Ask the server to create a new project
	 * @param projectName
	 */
	private void createProject(String projectName) {
		if(projectName.trim().length() == 0) return;
		
		setContentPane(loadingPanel);
		revalidate();
		repaint();
		new Thread(() -> {
			errorMessage.setVisible(false);
			if(ClientAPI.get().createProject(projectName)) {
				projectList.refresh();
				dispose();
			} else {
				errorMessage.setText(ClientAPI.get().getMessage());
				errorMessage.setVisible(true);
				setContentPane(mainPanel);
				revalidate();
				repaint();
			}
		}).start();
	}
	
}
