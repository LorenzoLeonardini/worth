package dev.leonardini.worth.client.gui.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.MalformedURLException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.gui.assets.AssetsManager;

/**
 * Simple panel displaying a loading animation
 */
public class LoadingPanel extends JPanel {

	private static final long serialVersionUID = -6294564042217993485L;

	public LoadingPanel() {
		super.setLayout(new GridLayout(1, 1));
		try {
			super.add(new LoadingImagePanel());
		}
		catch (MalformedURLException e) {
			JLabel loading = new JLabel("Caricamento...");
			loading.setVerticalAlignment(SwingConstants.CENTER);
			loading.setHorizontalAlignment(SwingConstants.CENTER);
		}
		super.setVisible(true);
	}
	
	// http://www.java2s.com/Tutorials/Java/Swing_How_to/JLabel/Show_animated_GIF_without_using_a_JLabel.htm
	class LoadingImagePanel extends JPanel {

		private static final long serialVersionUID = 4631829787543283610L;
		Image image;

		public LoadingImagePanel() throws MalformedURLException {
			super();
			image = Toolkit.getDefaultToolkit().createImage(AssetsManager.LOADING_IMAGE);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				Dimension size = this.getSize();
				g.drawImage(image, size.width / 2 - 50, size.height / 2 - 50, this);
			}
		}

	}

}
