package dev.leonardini.worth.data;

/**
 * Container for Card information: name, description, current list
 */
public class CardInfo {

	public final String name;
	public final String description;
	public final CardLocation list;
	
	public CardInfo(String name, String description, CardLocation list) {
		this.name = name;
		this.description = description;
		this.list = list;
	}
	
}
