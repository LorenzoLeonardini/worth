package dev.leonardini.worth.server.data;

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

import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.server.data.Card.HistoryEntry;

/**
 * A project object in the server database. Create and manage a project and its cards.
 */
public class Project implements Serializable {

	private static final long serialVersionUID = -4418794667789728106L;
	
	private String name;
	private boolean deleted = false;
	private Set<String> members;
	private Map<String, Card> todo;
	private Map<String, Card> in_progress;
	private Map<String, Card> to_be_revised;
	private Map<String, Card> done;

	/**
	 * Create a project
	 * @param name
	 * @param creator the user who created it. Will be added to the member list
	 */
	public Project(String name, String creator) {
		this.name = name;
		this.members = new HashSet<String>();
		this.members.add(creator);
		
		this.todo = new HashMap<String, Card>();
		this.in_progress = new HashMap<String, Card>();
		this.to_be_revised = new HashMap<String, Card>();
		this.done = new HashMap<String, Card>();
		
		// Add default welcome card
		try {
			addCard(new Card("Welcome", "Trascina una card da una colonna all'altra", "system"));
		} catch(Exception e) {}
	}
	
	// Custom serialization process. Cards are saved separately to files
	@SuppressWarnings("unchecked")
	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Save basic data
		oos.writeObject(name);
		oos.writeBoolean(deleted);
		oos.writeObject(members);
		
		// Compute an array of lists, containing the cards of the project,
		// dividing them per location
		List<String> cards[] = new List[4];
		for(CardLocation l : CardLocation.values()) {
			cards[l.ordinal()] = new ArrayList<String>();
			for(Card c : locationToList(l).values()) {
				// save card
				c.saveToFile(name);
				// add card name to list
				cards[l.ordinal()].add(c.name);
			}
		}
		oos.writeObject(cards);
	}
	
	// Custom serialization process. Cards are read separately from files
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Get basic data
		this.name = (String) ois.readObject();
		this.deleted = ois.readBoolean();
		this.members = (Set<String>) ois.readObject();
		
		// Init card lists
		this.todo = new HashMap<String, Card>();
		this.in_progress = new HashMap<String, Card>();
		this.to_be_revised = new HashMap<String, Card>();
		this.done = new HashMap<String, Card>();
		
		// Load cards and add them to the lists
		List<String> cards[] = (List[]) ois.readObject();
		for(CardLocation l : CardLocation.values()) {
			for(String c : cards[l.ordinal()]) {
				locationToList(l).put(c, Card.loadFromFile(name, c));
			}
		}
	}
	
	public synchronized List<String> getMemebers() {
		return new ArrayList<String>(this.members);
	}
	
	/**
	 * Move a card form a location to another
	 * 
	 * @param name the card to move
	 * @param src
	 * @param dest
	 * @param who the user who moved the card. Needed to update card history
	 * 
	 * @throws InvalidCardException when the card does not exist in the src list
	 * @throws InvalidCardMovementException either when movement constraints are not respected or when
	 * 			project has been deleted and is therefore "blocked"
	 */
	public synchronized void moveCard(String name, CardLocation src, CardLocation dest, String who) throws InvalidCardException, InvalidCardMovementException {
		if(deleted)
			throw new InvalidCardMovementException("Progetto eliminato");
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
	
	/**
	 * Add a new card to the project. Card is automatically put into the todo list
	 * 
	 * @param card
	 * @throws Exception either when project has been deleted or when a card with the same
	 * 			name already exists
	 */
	public synchronized void addCard(Card card) throws Exception {
		if(deleted)
			throw new Exception("Progetto eliminato");
		if(card == null)
			throw new NullPointerException();
		String fileName = Card.toFileName(card.name);
		if(todo.containsKey(fileName) || in_progress.containsKey(fileName) 
				|| to_be_revised.containsKey(fileName) || done.containsKey(fileName)) {
			throw new Exception("Esiste già una card con questo nome");
		}
		todo.put(fileName, card);
	}
	
	/**
	 * Add a user to the member list. Checks for the correctness of the username
	 * must be done somewhere else.
	 * 
	 * @param username
	 */
	public synchronized void addMember(String username) {
		if(username == null)
			throw new NullPointerException();
		members.add(username);
	}
	
	public synchronized String getName() {
		return name;
	}
	
	public synchronized boolean isDeleted() {
		return deleted;
	}
	
	/**
	 * Cancel a project. In order to be able to do that, all the cards must be in the "done" list
	 * 
	 * @throws ProjectUndeletableException when some cards are in some other list
	 */
	public synchronized void delete() throws ProjectUndeletableException {
		if(todo.size() > 0 || in_progress.size() > 0 || to_be_revised.size() > 0)
			throw new ProjectUndeletableException("Tutte le card devono essere nella lista 'done'");
		deleted = true;
	}
	
	public synchronized List<String> getMembers() {
		return new ArrayList<String>(members);
	}
	
	public synchronized boolean isMember(String user) {
		return members.contains(user);
	}

	/**
	 * @return a list of the names of all the cards in this project
	 */
	public synchronized List<String> getCards() {
		List<String> cards = new ArrayList<String>();
		for(CardLocation l : CardLocation.values()) {
			for(Card c : locationToList(l).values()) {
				cards.add(c.name);
			}
		}
		return cards;
	}
	
	private CardLocation getCardLocation(String cardName) {
		for(CardLocation l : CardLocation.values()) {
			if(locationToList(l).containsKey(Card.toFileName(cardName)))
				return l;
		}
		return null;
	}

	/**
	 * Get the 'CardInfo' of a card
	 * @param cardName
	 * @return
	 */
	public synchronized CardInfo getCard(String cardName) {
		String fileName = Card.toFileName(cardName);
		CardLocation location = getCardLocation(cardName);
		Card card = locationToList(location).get(fileName);
		return new CardInfo(card.name, card.description, location);
	}
	
	public synchronized List<HistoryEntry> getCardHistory(String cardName) {
		String fileName = Card.toFileName(cardName);
		return locationToList(getCardLocation(cardName)).get(fileName).getHistory();
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
	
	public static class ProjectUndeletableException extends Exception {
		private static final long serialVersionUID = 3226045727166174031L;
		public ProjectUndeletableException(String message) {
			super(message);
		}
	}
	
}
