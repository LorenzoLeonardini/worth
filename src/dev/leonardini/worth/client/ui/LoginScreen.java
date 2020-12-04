package dev.leonardini.worth.client.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LoginScreen extends JFrame {
	
	private static final long serialVersionUID = 1613627539419806782L;
	
	private JPanel mainPanel;
	private JPanel loadingPanel;
	
	private void switchToMain() {
		this.setContentPane(mainPanel);
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	private void switchToLoading() {
		this.setContentPane(loadingPanel);
		SwingUtilities.updateComponentTreeUI(this);
		Timer t = new Timer(1000, new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	JFrame f = new MainScreen();
		    	f.setVisible(true);
		    	dispose();
		    }
		});
		t.setInitialDelay(1000);
		t.setRepeats(false);
		t.restart();
	}
	
	public LoginScreen() {
		this.setSize(320, 400);
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
		
		int base_y = 51;
		
		JLabel loginTitle = new JLabel("Accesso");
		loginTitle.setBounds(18, base_y, 280, 40);
		loginTitle.setHorizontalAlignment(SwingConstants.CENTER);
		loginTitle.setFont(new Font("Arial", Font.BOLD, 24));
		mainPanel.add(loginTitle);
		
		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setBounds(58, base_y + 45, 200, 30); // 145
		mainPanel.add(usernameLabel);
		
		JTextField username = new JTextField();
		username.setBounds(58, base_y + 70, 200, 30);
		mainPanel.add(username);
		
		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setBounds(58, base_y + 100, 200, 30);
		mainPanel.add(passwordLabel);
		
		JPasswordField password = new JPasswordField();
		password.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					switchToLoading();
				}
			}
		});
		password.setBounds(58, base_y + 125, 200, 30);
		mainPanel.add(password);
		
		JButton login = new JButton("Accedi");
		login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				switchToLoading();
			}
		});
		login.setBounds(58, base_y + 165, 200, 30);
		mainPanel.add(login);
		
		JLabel oppure = new JLabel("oppure");
		oppure.setHorizontalAlignment(SwingConstants.CENTER);
		oppure.setBounds(58, base_y + 190, 200, 30);
		oppure.setFont(new Font("Dialog", Font.PLAIN, 11));
		mainPanel.add(oppure);
		
		JButton signup = new JButton("Registrati");
		signup.setBounds(58, base_y + 215, 200, 30);
		mainPanel.add(signup);
		
		this.setVisible(true);
	}

}
