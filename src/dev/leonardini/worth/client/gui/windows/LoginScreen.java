package dev.leonardini.worth.client.gui.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.panels.LoadingPanel;

public class LoginScreen extends JFrame {
	
	private static final long serialVersionUID = -3152910977074804913L;

	private JPanel mainPanel;
	private JPanel loadingPanel;
	
	private JTextField username;
	private JPasswordField password;
	private JTextField server;
	private JLabel message;
	private Color messageColor;
	private Border fieldBorder;
	private Border redBorder;
	
	private void switchToMain() {
		this.setContentPane(mainPanel);
		invalidate();
		validate();
		repaint();
	}
	
	private void switchToLoading() {
		this.setContentPane(loadingPanel);
		invalidate();
		validate();
		repaint();
	}
	
	public LoginScreen() {
		this.setSize(320, 455);
		this.setLocationRelativeTo(null);
		this.setTitle("WORkTogetHer - Login");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		
		loadingPanel = new LoadingPanel();
		
		mainPanel = (JPanel) this.getContentPane();
		mainPanel.setLayout(null);
		
		int base_y = 41;
		
		JLabel loginTitle = new JLabel("Accesso");
		loginTitle.setBounds(18, base_y, 280, 40);
		loginTitle.setHorizontalAlignment(SwingConstants.CENTER);
		loginTitle.setFont(new Font("Arial", Font.BOLD, 24));
		mainPanel.add(loginTitle);
		
		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setBounds(58, base_y + 45, 200, 30);
		mainPanel.add(usernameLabel);
		
		username = new JTextField();
		username.setBounds(58, base_y + 70, 200, 30);
		mainPanel.add(username);
		
		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setBounds(58, base_y + 100, 200, 30);
		mainPanel.add(passwordLabel);
		
		password = new JPasswordField();
		password.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					login();
				}
			}
		});
		password.setBounds(58, base_y + 125, 200, 30);
		mainPanel.add(password);
		
		JLabel serverLabel = new JLabel("Server:");
		serverLabel.setBounds(58, base_y + 155, 200, 30);
		mainPanel.add(serverLabel);
		
		server = new JTextField("localhost");
		server.setBounds(58, base_y + 180, 200, 30);
		mainPanel.add(server);
		
		fieldBorder = username.getBorder();
		redBorder = BorderFactory.createLineBorder(Color.red);
		
		message = new JLabel("asdasdasd");
		message.setHorizontalAlignment(SwingConstants.CENTER);
		message.setVerticalAlignment(SwingConstants.CENTER);
		message.setBounds(10, base_y + 210, 300, 28);
		message.setFont(new Font("Dialog", Font.PLAIN, 12));
		message.setVisible(false);
		messageColor = message.getForeground();
		mainPanel.add(message);
		
		JButton login = new JButton("Accedi");
		login.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
		login.setBounds(58, base_y + 240, 200, 30);
		mainPanel.add(login);
		
		JLabel oppure = new JLabel("oppure");
		oppure.setHorizontalAlignment(SwingConstants.CENTER);
		oppure.setBounds(58, base_y + 265, 200, 30);
		oppure.setFont(new Font("Dialog", Font.PLAIN, 11));
		mainPanel.add(oppure);
		
		JButton signup = new JButton("Registrati");
		signup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				register();
			}
		});
		signup.setBounds(58, base_y + 290, 200, 30);
		mainPanel.add(signup);
		
		this.setVisible(true);
	}
	
	private void register() {
		System.out.println("Registering...");
		load(() -> {
			ClientAPI.get().estabilish(server.getText());
			if (ClientAPI.get().register(username.getText(), new String(password.getPassword()))) {
				message.setText(ClientAPI.get().getMessage());
			} else {
				username.setBorder(redBorder);
				password.setBorder(redBorder);
				message.setText(ClientAPI.get().getMessage());
				message.setForeground(Color.red);
			}
			message.setVisible(true);
			switchToMain();
		});
	}
	
	private void login() {
		System.out.println("Logging in...");
		load(() -> {
			ClientAPI.get().estabilish(server.getText());
			if (ClientAPI.get().login(username.getText(), new String(password.getPassword()))) {
				MainScreen ms = new MainScreen(username.getText());
				ms.setVisible(true);
				dispose();
			} else {
				username.setBorder(redBorder);
				password.setBorder(redBorder);
				message.setText(ClientAPI.get().getMessage());
				message.setForeground(Color.red);
			}
			message.setVisible(true);
			switchToMain();
		});
	}
	
	private void load(Runnable runnable) {
		switchToLoading();
		username.setBorder(fieldBorder);
		password.setBorder(fieldBorder);
		message.setVisible(false);
		message.setForeground(messageColor);
		new Thread(runnable).start();
	}

}
