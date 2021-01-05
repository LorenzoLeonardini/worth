package dev.leonardini.worth.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.server.data.Card;
import dev.leonardini.worth.server.data.Project;
import dev.leonardini.worth.server.data.Card.HistoryEntry;
import dev.leonardini.worth.server.data.Project.CardLocation;
import dev.leonardini.worth.server.data.Project.InvalidCardException;
import dev.leonardini.worth.server.data.Project.InvalidCardMovementException;
import dev.leonardini.worth.server.data.Project.ProjectUndeletableException;

public class ProjectDB {
	
	private static Map<String, Project> projects;

	@SuppressWarnings("unchecked")
	public static void load(String filename) {
		Logger.Trace("Loading project db");
		
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			projects = (Map<String, Project>) in.readObject(); 
			in.close();
			fis.close();
			return;
		} catch (FileNotFoundException e) {
			Logger.Trace("No project db file. New file will be created");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Error("Cannot read project db file");
		}
		projects = new HashMap<String, Project>();
	}
	
	public static void save(String filename) {
		Logger.Trace("Saving project db");
		
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(projects);
			out.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean createProject(String name, String user) {
		String filename = Card.toFileName(name);
		if(projects.containsKey(filename)) {
			return false;
		}
		Project p = new Project(name, user);
		projects.put(filename, p);
		return true;
	}
	
	public static List<Project> listUserProjects(String username) {
		List<Project> user_projects = new ArrayList<Project>();
		for(Project p : projects.values()) {
			if(!p.isDeleted() && p.isMember(username))
				user_projects.add(p);
		}
		return user_projects;
	}
	
	public static List<String> getMembers(String projectName) {
		if(!projects.containsKey(Card.toFileName(projectName))) {
			return null;
		}
		return projects.get(Card.toFileName(projectName)).getMembers();
	}

	public static void addMember(String projectName, String user) {
		projects.get(Card.toFileName(projectName)).addMember(user);
	}

	public static List<String> getCards(String projectName) {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null) return null;
		return p.getCards();
	}

	public static CardInfo getCard(String projectName, String cardName) {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null) return null;
		return p.getCard(cardName);
	}
	
	public static List<HistoryEntry> getCardHistory(String projectName, String cardName) {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null) return null;
		return p.getCardHistory(cardName);
	}

	public static String addCard(String projectName, String cardName, String cardDescription, String user) {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null) return "Progetto non esistente";
		try {
			p.addCard(new Card(cardName, cardDescription, user));
			return null;
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	public static boolean moveCard(String projectName, String cardName, CardLocation src, CardLocation dst, String username) throws InvalidCardException, InvalidCardMovementException {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null) return false;
		p.moveCard(cardName, src, dst, username);
		return true;
	}

	public static boolean deleteProject(String projectName, String username) throws ProjectUndeletableException {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null) return false;
		p.delete();
		return true;
	}
	
	public static List<String> getProjects() {
		return new ArrayList<String>(projects.keySet());
	}
	
}
