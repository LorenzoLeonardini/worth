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
import dev.leonardini.worth.data.CardLocation;
import dev.leonardini.worth.server.data.Card;
import dev.leonardini.worth.server.data.Card.HistoryEntry;
import dev.leonardini.worth.server.data.Project;

/**
 * ProjectDB is an abstract class for the management of all the projects and the project file
 * database.
 */
public abstract class ProjectDB {
	
	private static Map<String, Project> projects;

	@SuppressWarnings("unchecked")
	/**
	 * Load a project db file. Projects are saved used Java internal
	 * Serialization process, but with a custom serializer in order to comply
	 * with the specifics
	 * 
	 * @param filename
	 */
	protected synchronized static void loadProjectsFile(String filename) {
		Logger.Log("Loading project db");
		
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			projects = (Map<String, Project>) in.readObject(); 
			in.close();
			fis.close();
			return;
		} catch (FileNotFoundException e) {
			Logger.Log("No project db file. New file will be created");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Error("Cannot read project db file");
		}
		projects = new HashMap<String, Project>();
	}
	
	/**
	 * Save the project database file, using Java internal serialization
	 * process.
	 */
	protected synchronized static void saveProjectsFile(String filename) {
		Logger.Log("Saving project db");
		
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
	
	/**
	 * Create a new project, and add it to the database
	 * @param name project name
	 * @param user who created the project
	 * @throws Exception in case of any error (already existing project)
	 */
	protected synchronized static void createProject(String name, String user) throws Exception {
		String filename = Card.toFileName(name);
		if(projects.containsKey(filename)) 
			throw new Exception("Progetto già esistente");
		projects.put(filename, new Project(name, user));
	}
	
	private static Project getProject(String projectName) throws Exception {
		Project p = projects.get(Card.toFileName(projectName));
		if(p == null)
			throw new Exception("Il progetto non esiste");
		return p;
	}
	
	/**
	 * Retrieve a list containing all the members of a project
	 * @param projectName
	 * @return list of strings
	 * @throws Exception if the project does not exist
	 */
	protected synchronized static List<String> getProjectMembers(String projectName) throws Exception {
		return getProject(projectName).getMembers();
	}
	
	/**
	 * Use this function to check if a user is a member of a project.
	 * The function returns nothing, if no exception is raised than the user is a member
	 * @param username
	 * @param projectName
	 * @throws Exception if project does not exist or user not member
	 */
	protected synchronized static void checkProjectMembership(String username, String projectName) throws Exception {
		if(!getProject(projectName).isMember(username))
			throw new Exception("Non fai parte del progetto");
	}
	
	/**
	 * Retrieve a list of projects of which the given user is a member
	 * @param username
	 * @return a list of strings
	 */
	protected synchronized static List<Project> listUserProjects(String username) {
		List<Project> user_projects = new ArrayList<Project>();
		for(Project p : projects.values()) {
			if(!p.isDeleted() && p.isMember(username))
				user_projects.add(p);
		}
		return user_projects;
	}

	/**
	 * Add a member to the project. Username correctness isn't checked here
	 * 
	 * @param projectName
	 * @param user
	 * @throws Exception if project does not exist or user is already member
	 */
	protected synchronized static void addMember(String projectName, String user) throws Exception {
		getProject(projectName).addMember(user);
	}

	/**
	 * Get all the cards in a project
	 * 
	 * @param projectName
	 * @return a list of strings defining cards name
	 * @throws Exception if project does not exist
	 */
	protected synchronized static List<String> getCards(String projectName) throws Exception {
		return getProject(projectName).getCards();
	}

	/**
	 * Retrieve information about a card
	 * 
	 * @param projectName
	 * @param cardName
	 * @return CardInfo object
	 * @throws Exception if project or card don't exist
	 */
	protected synchronized static CardInfo getCard(String projectName, String cardName) throws Exception {
		return getProject(projectName).getCard(cardName);
	}
	
	/**
	 * Retrieve all the history related to a card
	 * @param projectName
	 * @param cardName
	 * @return a list of HistoryEntry objects
	 * @throws Exception if project or card don't exist
	 */
	protected synchronized static List<HistoryEntry> getCardHistory(String projectName, String cardName) throws Exception {
		return getProject(projectName).getCardHistory(cardName);
	}

	/**
	 * Add a card to a project
	 * 
	 * @param projectName
	 * @param cardName
	 * @param cardDescription
	 * @param user
	 * @throws Exception when project does not exist/is deleted or card already exists
	 */
	protected synchronized static void addCard(String projectName, String cardName, String cardDescription, String user) throws Exception {
		getProject(projectName).addCard(new Card(cardName, cardDescription, user));
	}

	/**
	 * Move a card from a list to another
	 * 
	 * @param projectName
	 * @param cardName
	 * @param src
	 * @param dst
	 * @param username
	 * @throws Exception in case of invalid project or card or when movement constraints are violated
	 */
	protected synchronized static void moveCard(String projectName, String cardName, CardLocation src, CardLocation dst, String username) throws Exception {
		getProject(projectName).moveCard(cardName, src, dst, username);
	}

	/**
	 * Delete a project
	 * 
	 * @param projectName
	 * @param username
	 * @throws Exception when project is not valid or when constraints are violated
	 */
	protected synchronized static void deleteProject(String projectName, String username) throws Exception {
		getProject(projectName).delete();
	}
	
	/**
	 * Get a list of all the project names
	 */
	protected synchronized static List<String> getProjects() {
		return new ArrayList<String>(projects.keySet());
	}
	
}
