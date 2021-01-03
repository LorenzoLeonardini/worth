package dev.leonardini.worth.client.gui.assets;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * This class manages user propics. It allows to load them from Gravatar and retrieve
 * them at any moment without the need of downloading them again.
 * 
 * Note that images are not cached locally, and this class requires an Internet conenction
 */
public abstract class PropicManager {

	private static Map<String, ImageIcon> Images = new HashMap<String, ImageIcon>();
	
	private static ImageIcon fallback = null;
	
	public static final int SIZE = 30;
	
	/**
	 * Load and save the propic linked to a user.
	 * 
	 * @param username the user to which the propic is linked to
	 * @param key the gravatar key (i.e. the email hash) associated to the pic
	 */
	public static void addPropic(String username, String key) {
		try {
		    ImageIcon ii = getImage(key);
		    Images.put(username, new ImageIcon(ii.getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_FAST)));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the propic of a user.
	 * @param username
	 * @return a JLabel object using the propic as ImageIcon
	 */
	public static JLabel get(String username) {
		if(Images.containsKey(username))
			return new JLabel(Images.get(username));
		return getFallback(1);
	}
	
	private static JLabel getFallback(int type) {
		if(fallback == null) {
			try {
				ImageIcon ii = getImage("00000000000000000000000000000000");
				fallback = new ImageIcon(ii.getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_FAST));
			}
			catch (IOException e) {
				fallback = new ImageIcon();
				e.printStackTrace();
			}	
		}
		if(type == 0)
			return new JLabel(fallback);
		return new JLabel(fallback);
	}
	
	private static ImageIcon getImage(String key) throws IOException {
		String path = "https://www.gravatar.com/avatar/" + key;
	    URL url = new URL(path);
	    BufferedImage image = ImageIO.read(url);
	    ImageIcon ii = new ImageIcon(image);
	    return ii;
	}
	
}
