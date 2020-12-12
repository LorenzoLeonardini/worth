package dev.leonardini.worth.server;

import java.util.List;
import java.util.Map;

import dev.leonardini.worth.data.Card;
import dev.leonardini.worth.data.Project;

public class ServerService {

	public void register(String username, String password) {
		
	}
	
	public void login(String username, String password) {
		
	}
	
	public void logout(String username) {
		
	}
	
	public Map<String, Boolean> listUsers() {
		return null;
	}
	
	public List<String> listOnlineUsers() {
		return null;
	}
	
	public List<Project> listProjects(String username) {
		return null;
	}
	
	public void createProject(String name) {
		
	}
	
	public void addMember(String project, String username) {
		
	}
	
	public List<String> showMembers(String project) {
		return null;
	}
	
	public List<Card> showCards(String project) {
		return null;
	}
	
	public Card showCard(String project, String card) {
		return null;
	}
	
	public void addCard(String project, Card card) {
		
	}
	
	public void moveCard(String project, String card, Project.CardLocation src, Project.CardLocation dest) {
		
	}
	
	public void getCardHistory(String project, String card) {
		
	}
	
	public void readChat(String project) {
		
	}
	
	public void sendChatMsg(String project, String message) {
		
	}
	
	public void cancelProject(String project) {
		
	}
	
}
