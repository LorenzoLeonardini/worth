package dev.leonardini.worth.client.ui;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class CardHistoryScreen extends JDialog {

	private static final long serialVersionUID = 6378209988325009361L;

	@SuppressWarnings("unchecked")
	public CardHistoryScreen(List<String> history) {
		setSize(600, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		getContentPane().setLayout(null);
		setTitle("Storia della card");

		@SuppressWarnings("rawtypes")
		JList historyList = new JList();
		historyList.setListData(history.toArray());
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(historyList);
		scrollPane.setBounds(30, 30, 540, 300);
		getContentPane().add(scrollPane);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
}
