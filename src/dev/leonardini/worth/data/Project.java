package dev.leonardini.worth.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Project implements Serializable {

	private static final long serialVersionUID = -4418794667789728106L;
	
	private String name;
	private Set<String> members;
	private Map<String, Card> todo;
	private Map<String, Card> in_progress;
	private Map<String, Card> to_be_revised;
	private Map<String, Card> done;

	public Project(String name, String creator) {
		this.name = name;
		this.members = new HashSet<String>();
		this.members.add(creator);
		this.todo = new HashMap<String, Card>();
		this.in_progress = new HashMap<String, Card>();
		this.to_be_revised = new HashMap<String, Card>();
		this.done = new HashMap<String, Card>();
		
		try {
			addCard(new Card("Welcome", "Trascina una card da una colonna all'altra", "system"));
		} catch(Exception e) {}
	}
	
	// Custom serialization process. Cards are saved separately to files
	@SuppressWarnings("unchecked")
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(name);
		oos.writeObject(members);
		List<String> cards[] = new List[4];
		for(CardLocation l : CardLocation.values()) {
			cards[l.ordinal()] = new ArrayList<String>();
			for(String c : locationToList(l).keySet()) {
				cards[l.ordinal()].add(c);
			}
		}
		oos.writeObject(cards);
	}
	
	// Custom serialization process. Cards are read separately from files
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		this.name = (String) ois.readObject();
		this.members = (Set<String>) ois.readObject();
		this.todo = new HashMap<String, Card>();
		this.in_progress = new HashMap<String, Card>();
		this.to_be_revised = new HashMap<String, Card>();
		this.done = new HashMap<String, Card>();
		
		List<String> cards[] = (List[]) ois.readObject();
		for(CardLocation l : CardLocation.values()) {
			for(String c : cards[l.ordinal()]) {
				locationToList(l).put(c, Card.loadFromFile(name, c));
			}
		}
	}
	
	public List<String> getMemebers() {
		return new ArrayList<String>(this.members);
	}
	
	public void moveCard(String name, CardLocation src, CardLocation dest, String who) throws InvalidCardException, InvalidCardMovementException {
		if(name == null || src == null || dest == null)
			throw new NullPointerException();
		Card c;
		Map<String, Card> from = locationToList(src);
		Map<String, Card> to = locationToList(dest);
		if((c = from.get(Card.toFileName(name))) == null)
			throw new InvalidCardException("Card non esistente");
		if((src == CardLocation.TODO && dest != CardLocation.IN_PROGRESS)
				|| (src == CardLocation.IN_PROGRESS && dest != CardLocation.TO_BE_REVISED && dest != CardLocation.DONE)
				|| (src == CardLocation.TO_BE_REVISED && dest != CardLocation.IN_PROGRESS && dest != CardLocation.DONE)
				|| (src == CardLocation.DONE))
			throw new InvalidCardMovementException("Vincoli di spostamento non rispettati");
		
		from.remove(Card.toFileName(name));
		to.put(Card.toFileName(name), c);
		c.moved(dest, who);
	}
	
	private Map<String, Card> locationToList(CardLocation l) {
		if(l == CardLocation.TODO) return todo;
		if(l == CardLocation.IN_PROGRESS) return in_progress;
		if(l == CardLocation.TO_BE_REVISED) return to_be_revised;
		if(l == CardLocation.DONE) return done;
		return null;
	}
	
	public void addCard(Card c) throws Exception {
		if(c == null)
			throw new NullPointerException();
		String fileName = Card.toFileName(c.name);
		if(todo.containsKey(fileName) || in_progress.containsKey(fileName) 
				|| to_be_revised.containsKey(fileName) || done.containsKey(fileName)) {
			throw new Exception("Esiste già una card con questo nome");
		}
		todo.put(fileName, c);
	}
	
	public void addMember(String username) {
		if(username == null)
			throw new NullPointerException();
		if(members.contains(username)) return;
		members.add(username);
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getMembers() {
		return new ArrayList<String>(members);
	}
	
	public boolean isMember(String user) {
		return members.contains(user);
	}
	
	public void saveCards() {
		for(CardLocation l : CardLocation.values()) {
			for(Card c : locationToList(l).values()) {
				c.saveToFile(name);
			}
		}
	}
	
	public void printCards() {
		for(CardLocation l : CardLocation.values()) {
			for(Card c : locationToList(l).values()) {
				System.out.println(c);
			}
		}
	}

	public List<String> getCards() {
		List<String> cards = new ArrayList<String>();
		for(CardLocation l : CardLocation.values()) {
			for(Card c : locationToList(l).values()) {
				cards.add(c.name);
			}
		}
		return cards;
	}

	public CardInfo getCard(String cardName) {
		String fileName = Card.toFileName(cardName);
		if(todo.containsKey(fileName))
			return new CardInfo(cardName, todo.get(fileName).description, CardLocation.TODO);
		else if(in_progress.containsKey(fileName))
			return new CardInfo(cardName, in_progress.get(fileName).description, CardLocation.IN_PROGRESS);
		else if(to_be_revised.containsKey(fileName))
			return new CardInfo(cardName, to_be_revised.get(fileName).description, CardLocation.TO_BE_REVISED);
		else if(done.containsKey(fileName))
			return new CardInfo(cardName, done.get(fileName).description, CardLocation.DONE);
		return null;
	}
	
	public enum CardLocation {
		TODO,
		IN_PROGRESS,
		TO_BE_REVISED,
		DONE
	}
	
	public static class InvalidCardException extends Exception {
		private static final long serialVersionUID = -7171796209580548641L;
		public InvalidCardException(String message) {
			super(message);
		}
	}
	
	public static class InvalidCardMovementException extends Exception {
		private static final long serialVersionUID = 3226045727166174031L;
		public InvalidCardMovementException(String message) {
			super(message);
		}
	}
	
}
