package dev.leonardini.worth.client.gui.assets;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class AssetsManager {

	public final static URL LOADING_IMAGE = AssetsManager.class.getResource("loading.gif");
	public final static ImageIcon COG = load("cog.png");
	
	private static ImageIcon load(String path) {
		try {
			return new ImageIcon(ImageIO.read(AssetsManager.class.getResource(path)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return new ImageIcon();
	}
	
}
