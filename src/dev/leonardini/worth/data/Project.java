package dev.leonardini.worth.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {

	private List<String> members = new ArrayList<String>();
	private List<Map<String, Card>> cards = new ArrayList<Map<String, Card>>(4);

	public Project() {
		for(int i = 0; i < 4; i++) {
			cards.set(i, new HashMap<String, Card>());
		}
	}
	
	public void moveCard(String name, Location src, Location dest) throws InvalidCardException, InvalidCardMovementException {
		if(name == null || src == null || dest == null)
			throw new NullPointerException();
		Card c;
		if((c = cards.get(src.ordinal()).get(name)) == null)
			throw new InvalidCardException();
		if((src == Location.TODO && dest != Location.IN_PROGRESS)
				|| (src == Location.IN_PROGRESS && dest != Location.TO_BE_REVISED && dest != Location.DONE)
				|| (src == Location.TO_BE_REVISED && dest != Location.IN_PROGRESS && dest != Location.DONE)
				|| (src == Location.DONE))
			throw new InvalidCardMovementException();
		
		cards.get(src.ordinal()).remove(name);
		cards.get(dest.ordinal()).put(name, c);
	}
	
	public void addCard(Card c) {
		if(c == null)
			throw new NullPointerException();
		cards.get(Location.TODO.ordinal()).put(c.name, c);
	}
	
	public void addMember(String username) {
		if(username == null)
			throw new NullPointerException();
		if(members.contains(username)) return;
		members.add(username);
	}
	
	public List<String> getMembers() {
		return new ArrayList<String>(members);
	}
	
	public enum Location {
		TODO,
		IN_PROGRESS,
		TO_BE_REVISED,
		DONE
	}
	
	class InvalidCardException extends Exception {
		private static final long serialVersionUID = -7171796209580548641L;
	}
	
	class InvalidCardMovementException extends Exception {
		private static final long serialVersionUID = 3226045727166174031L;
	}
	
}
