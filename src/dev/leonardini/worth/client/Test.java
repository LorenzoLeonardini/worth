package dev.leonardini.worth.client;

import java.io.IOException;

import javax.swing.JFrame;

import dev.leonardini.worth.client.ui.LoginScreen;
import dev.leonardini.worth.client.ui.assets.PropicManager;

public class Test {

	public static void main(String[] args) throws IOException {
		PropicManager.addPropic("pianka", "c52b80d41ad1985056dc755b4cc4777d");
		
		JFrame frame = new LoginScreen();
		frame.setVisible(true);
		
//		String path = "https://www.gravatar.com/avatar/c52b80d41ad1985056dc755b4cc4777d";
//	    URL url = new URL(path);
//	    BufferedImage image = ImageIO.read(url);
//	    JLabel label = new JLabel(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
//	    JFrame f = new JFrame();
//	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	    f.getContentPane().add(label);
//	    f.pack();
//	    f.setLocation(200,200);
//	    f.setVisible(true);
	}
	
}
