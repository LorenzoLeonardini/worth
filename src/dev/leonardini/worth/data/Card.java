package dev.leonardini.worth.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.leonardini.worth.data.Project.CardLocation;

public class Card implements Serializable {

	private static final long serialVersionUID = 5197429374757398688L;
	
	public final String name;
	public final String description;
	
	private List<HistoryEntry> history = new ArrayList<HistoryEntry>();
	
	public Card(String name, String description, String by) {
		this.name = name;
		this.description = description;
		history.add(new HistoryEntry(CardLocation.TODO, by, System.currentTimeMillis()));
	}
	
	public void moved(CardLocation where, String by) {
		history.add(new HistoryEntry(where, by, System.currentTimeMillis()));
	}
	
	protected static Card loadFromFile(String projectName, String cardName) {
		String filepath = "projects/" + toFileName(projectName) + "/" + toFileName(cardName);
		try {
			FileInputStream fis = new FileInputStream(filepath);
			ObjectInputStream in = new ObjectInputStream(fis);
			Card c = (Card) in.readObject();
			in.close();
			fis.close();
			return c;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new Card(projectName, "ERROR", "system");
	}
	
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
