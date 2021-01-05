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
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.GuiUtils;
import dev.leonardini.worth.client.gui.panels.LoadingPanel;
import dev.leonardini.worth.client.gui.panels.MainPanel;

/**
 * Confirmation window for the action of deleting a project.
 * 
 * The user will be asked their password for security reasons in
 * order to be able to delete the project.
 */
public class ProjectDeletionDialog extends JDialog {

	private static final long serialVersionUID = 6378209988325009361L;

	private JPanel loadingPanel = new LoadingPanel();
	private JPanel panel;
	private JLabel errorMessage;
	private MainPanel mainPanel;
	
	/**
	 * Initiate the object
	 * @param projectName the project we want to delete
	 * @param mainPanel MainPanel instance. This is where the user will be redirected to
	 * 			after the deletion
	 */
	public ProjectDeletionDialog(String projectName, MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setSize(300, 310);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Conferma eliminazione");
		setAlwaysOnTop(true);
		panel = (JPanel) getContentPane();
		panel.setLayout(null);
		
		JLabel confirmTitle = new JLabel("<html><body style='text-align:center'>Inserisci la password per confermare di voler eliminare il progetto</body></html>");
		confirmTitle.setFont(GuiUtils.USER_PANEL_TITLE_FONT);
		confirmTitle.setVerticalAlignment(SwingConstants.CENTER);
		confirmTitle.setHorizontalAlignment(SwingConstants.CENTER);
		confirmTitle.setBounds(0, 35, 300, 50);
		getContentPane().add(confirmTitle);
		
		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setVerticalAlignment(SwingConstants.CENTER);
		passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
		passwordLabel.setBounds(25, 90, 250, 40);
		getContentPane().add(passwordLabel);
		
		JPasswordField password = new JPasswordField();
		password.setBounds(30, 130, 240, 30);
		password.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					deleteProject(projectName, new String(password.getPassword()));
				}
			}
		});
		getContentPane().add(password);
		
		errorMessage = new JLabel();
		errorMessage.setBounds(30, 160, 240, 30);
		errorMessage.setForeground(Color.red);
		errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
		errorMessage.setVerticalAlignment(SwingConstants.CENTER);
		errorMessage.setFont(new Font("Dialog", Font.PLAIN, 12));
		errorMessage.setVisible(false);
		panel.add(errorMessage);
		
		JButton delete = new JButton("Elimina");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteProject(projectName, new String(password.getPassword()));
			}
		});
		delete.setBounds(100, 190, 100, 30);
		getContentPane().add(delete);
	}
	
	/**
	 * Send the server the delete request
	 * @param projectName
	 * @param password
	 */
	private void deleteProject(String projectName, String password) {
		if(password.trim().length() == 0)
			return;
		
		setContentPane(loadingPanel);
		invalidate();
		validate();
		repaint();
		new Thread(() -> {
			errorMessage.setVisible(false);
			if(ClientAPI.get().cancelProject(projectName, password)) {
				mainPanel.backToProjectList();
				dispose();
			} else {
				errorMessage.setText("<html><body style='text-align: center'>" + ClientAPI.get().getMessage() + "</body></html>");
				errorMessage.setVisible(true);
				setContentPane(panel);
				invalidate();
				validate();
				repaint();
			}
		}).start();
	}
	
}
