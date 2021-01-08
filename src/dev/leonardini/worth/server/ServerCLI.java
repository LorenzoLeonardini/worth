package dev.leonardini.worth.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * ServerCLI is the server components which manages the server command line,
 * used to interface and check in with the server, and, most importantly, to
 * close gracefully, making sure to save the updates to the databases
 */
public class ServerCLI extends Thread {
	
	private Scanner scanner;
	
	private Map<String, Command> commands = new HashMap<String, Command>();
	
	protected ServerCLI(ServerTCP serverTCP) {
		super("ServerCLI Thread");
		
		add("help", "get a list of commands", () -> {
			for(Command c : commands.values())
				System.out.println(c.command + c.tabs + c.description);
			System.out.println();
		});
		
		add("exit", "gracefully close the server", () -> {
			System.out.println("Goodbye");
			serverTCP.cleanUp();
			scanner.close();
			System.exit(0);
		});
		
		add("verbose", "enable verbose mode -> all logs are shown in console", () -> {
			System.out.println("Toggled verbose");
			Logger.verbose(true);
		});
		
		add("silent", "disable verbose mode", () -> {
			System.out.println("Toggled verbose");
			Logger.verbose(false);
		});
		
		add("list users", "get a list of all the users", () -> {
			Map<String, Boolean> map = UserManager.get().getUsersStatusMap();
			for(String user : map.keySet()) {
				System.out.println("\t- " + user);
			}
		});
		
		add("list users status", "get a list of all the users and their status", () -> {
			Map<String, Boolean> map = UserManager.get().getUsersStatusMap();
			for(String user : map.keySet()) {
				System.out.println("\t- " + user + " -> " + (map.get(user) ? "online" : "offline"));
			}
		});
		
		add("list online users", "get a list of all the users which are online", () -> {
			Map<String, Boolean> map = UserManager.get().getUsersStatusMap();
			for(String user : map.keySet()) {
				if(map.get(user))
					System.out.println("\t- " + user);
			}
		});
		
		add("list offline users", "get a list of all the users which are offline", () -> {
			Map<String, Boolean> map = UserManager.get().getUsersStatusMap();
			for(String user : map.keySet()) {
				if(!map.get(user))
					System.out.println("\t- " + user);
			}
		});
		
		add("list projects", "get a list of all the projects", () -> {
			for(String project : ProjectDB.getProjects()) {
				System.out.println("\t- " + project);
			}
		});
	}
	
	@Override
	public void run() {
		System.out.println("\nType 'help' for a list of commands\n");
		scanner = new Scanner(System.in);
		String line;
		while((line = scanner.nextLine()) != null) {
			line = line.trim();
			Command c = commands.get(line);
			if(c != null) {
				c.exec.run();
			} else {
				System.out.println("Command not found");
			}
		}
	}
	
	private void add(String command, String description, Runnable exec) {
		commands.put(command, new Command(command, description, exec));
	}

	class Command {
		protected final String command;
		protected final String description;
		protected final Runnable exec;
		protected final String tabs;
		
		protected Command(String command, String description, Runnable exec) {
			this.command = command;
			this.description = description;
			this.exec = exec;
			String tabs = "";
			for(int i = 0; i < 3 - Math.ceil(command.length() / 8); i++)
				tabs += "\t";
			this.tabs = tabs;
		}
	}
	
}
