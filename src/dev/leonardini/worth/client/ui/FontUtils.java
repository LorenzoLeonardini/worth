package dev.leonardini.worth.client.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public abstract class FontUtils {

	public static final Font USER_PANEL_TITLE_FONT = new Font("Arial", Font.BOLD, 15);
	public static final Font USER_PANEL_FONT = new Font("Arial", Font.PLAIN, 13);
	
	public static final Font USERNAME_FONT = new Font("Arial", Font.BOLD, 18);
	public static final Font MAIN_TITLE_FONT = new Font("Arial", Font.BOLD | Font.ITALIC, 26);
	
	public static final Font CHAT_FONT = new Font("Arial", Font.PLAIN, 13);
	public static final Font CHAT_SYSTEM_FONT = new Font("Arial", Font.ITALIC, 11);
	public static final Font CHAT_USER_FONT = new Font("Arial", Font.BOLD, 13);
	
	public static final Color COLOR = new Color(0x4587a9);
	
	private static final Random r = new Random();
	private static final int[] random_range = { 0x00ffff, 0x008b8b, 0xa9a9a9, 0xbdb76b, 0xff8c00, 0x9932cc, 0xe9967a,//6 
			0xffd700, 0xf0e68c, 0xadd8e6, 0xe0ffff, 0x90ee90, 0xd3d3d3, 0xffb6c1, 0xffffe0, 0x00ff00, 0xffa500, //16
			0xffc0cb, 0xff2020, 0xc0c0c0, 0xffffff, 0xffff00 };
	public static Color RANDOM_COLOR() {
		return new Color(random_range[r.nextInt(random_range.length)]);
	}
	
}
