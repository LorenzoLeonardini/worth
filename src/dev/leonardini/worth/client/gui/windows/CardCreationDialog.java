package dev.leonardini.worth.client.gui.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.GuiUtils;
import dev.leonardini.worth.client.gui.panels.LoadingPanel;

/**
 * Popup window used to create a card in a project
 */
public class CardCreationDialog extends JDialog {

	private static final long serialVersionUID = 5676070794513778122L;

	private LoadingPanel loadingPanel = new LoadingPanel();
	private JPanel mainPanel;
	private JLabel errorMessage;

	/**
	 * Initiate the object
	 * 
	 * @param projectName the project this card will be added to
	 */
	public CardCreationDialog(String projectName) {
		setSize(300, 410);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Crea card");
		setAlwaysOnTop(true);
		mainPanel = (JPanel) getContentPane();
		mainPanel.setLayout(null);
		
		JLabel createTitle = new JLabel("Crea una nuova card");
		createTitle.setFont(GuiUtils.USERNAME_FONT);
		createTitle.setVerticalAlignment(SwingConstants.CENTER);
		createTitle.setHorizontalAlignment(SwingConstants.CENTER);
		createTitle.setBounds(0, 45, 300, 40);
		mainPanel.add(createTitle);
		
		JLabel cardNameLabel = new JLabel("Nome:");
		cardNameLabel.setVerticalAlignment(SwingConstants.CENTER);
		cardNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		cardNameLabel.setBounds(25, 90, 250, 40);
		mainPanel.add(cardNameLabel);
		
		JTextField cardName = new JTextField();
		cardName.setBounds(30, 120, 240, 30);
		mainPanel.add(cardName);
		
		JLabel cardDescriptionLabel = new JLabel("Descrizione:");
		cardDescriptionLabel.setVerticalAlignment(SwingConstants.CENTER);
		cardDescriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		cardDescriptionLabel.setBounds(25, 150, 250, 40);
		mainPanel.add(cardDescriptionLabel);
		
		JTextArea cardDescription = new JTextArea();
		cardDescription.setBounds(30, 180, 240, 80);
		cardDescription.setBorder(BorderFactory.createLineBorder(Color.gray));
		cardDescription.setLineWrap(true);
		cardDescription.setWrapStyleWord(true);
		mainPanel.add(cardDescription);
		
		errorMessage = new JLabel();
		errorMessage.setBounds(30, 260, 240, 30);
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
				addCard(projectName, cardName.getText(), cardDescription.getText());
			}
		});
		save.setBounds(100, 290, 100, 30);
		mainPanel.add(save);
	}
	
	private void addCard(String projectName, String cardName, String cardDescription) {
		setContentPane(loadingPanel);
		invalidate();
		validate();
		repaint();
		new Thread(() -> {
			errorMessage.setVisible(false);
			if(ClientAPI.get().addCard(projectName, cardName, cardDescription)) {
				dispose();
			} else {
				errorMessage.setText(ClientAPI.get().getMessage());
				errorMessage.setVisible(true);
				setContentPane(mainPanel);
				invalidate();
				validate();
				repaint();
			}
		}).start();
	}
	
}
