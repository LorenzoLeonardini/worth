package dev.leonardini.worth.server.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.leonardini.worth.server.data.Project.CardLocation;

/**
 * A card object in the server database. Contains general information about the
 * card and its history. Does not contain information about its current position
 * (even if it can be retrieved by the history)
 * 
 * This object is mainly just a container for data
 */
public class Card implements Serializable {

	private static final long serialVersionUID = 5197429374757398688L;
	
	public final String name;
	public final String description;
	
	private List<HistoryEntry> history = new ArrayList<HistoryEntry>();
	
	/**
	 * Initialize a card. Specify its name, its description and the user
	 * who created it.
	 * @param name
	 * @param description
	 * @param user
	 */
	public Card(String name, String description, String user) {
		this.name = name;
		this.description = description;
		history.add(new HistoryEntry(CardLocation.TODO, user, System.currentTimeMillis()));
	}
	
	/**
	 * Call this method when the card gets moved in order to update its history
	 * @param where
	 * @param by
	 */
	public void moved(CardLocation where, String by) {
		history.add(new HistoryEntry(where, by, System.currentTimeMillis()));
	}
	
	/**
	 * Load a Card object from a serialized file
	 * @param projectName
	 * @param cardName
	 * @return
	 */
	protected static Card loadFromFile(String projectName, String cardName) {
		String filepath = "projects/" + toFileName(projectName) + "/" + toFileName(cardName);
		try {
			FileInputStream fis = new FileInputStream(filepath);
			ObjectInputStream in = new ObjectInputStream(fis);
			Card c = (Card) in.readObject();
			in.close();
			fis.close();
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Card(projectName, "ERROR", "system");
	}
	
	/**
	 * Serialize this card to a file
	 * @param projectName
	 */
	protected void saveToFile(String projectName) {
		String filepath = "projects/" + toFileName(projectName) + "/" + toFileName(this.name);
		new File("projects/" + toFileName(projectName) + "/").mkdirs();
		try {
			FileOutputStream fos = new FileOutputStream(filepath);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String toFileName(String name) {
		return name.toLowerCase().replace(' ', '_');
	}
	
	public List<HistoryEntry> getHistory() {
		return this.history;
	}
	
	public class HistoryEntry implements Serializable {
		private static final long serialVersionUID = -5040512360595773172L;
		
		public final CardLocation location;
		public final String user;
		public final long timestamp;
		
		public HistoryEntry(CardLocation location, String user, long timestamp) {
			this.location = location;
			this.user = user;
			this.timestamp = timestamp;
		}
	}
	
}
