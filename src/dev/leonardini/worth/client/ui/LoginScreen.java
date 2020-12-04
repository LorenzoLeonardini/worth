package dev.leonardini.worth.client.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import dev.leonardini.worth.client.ServerConnection;

public class LoginScreen extends JFrame {
	
	private static final long serialVersionUID = 1613627539419806782L;
	
	private ServerConnection serverConnection;
	
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
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	private void switchToLoading() {
		this.setContentPane(loadingPanel);
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	public LoginScreen(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
		this.setSize(320, 455);
		this.setLocationRelativeTo(null);
		this.setTitle("WORkTogetHer - Login");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
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
		login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
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
		signup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
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
			serverConnection.estabilish(server.getText());
			if (serverConnection.register(username.getText(), new String(password.getPassword()))) {
				message.setText(serverConnection.getMessage());
			} else {
				username.setBorder(redBorder);
				password.setBorder(redBorder);
				message.setText(serverConnection.getMessage());
				message.setForeground(Color.red);
			}
			message.setVisible(true);
			switchToMain();
		});
	}
	
	private void login() {
		System.out.println("Logging in...");
		load(() -> {
			serverConnection.estabilish(server.getText());
			if (serverConnection.login(username.getText(), new String(password.getPassword()))) {
				MainScreen ms = new MainScreen(username.getText());
				ms.setVisible(true);
				dispose();
			} else {
				username.setBorder(redBorder);
				password.setBorder(redBorder);
				message.setText(serverConnection.getMessage());
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
