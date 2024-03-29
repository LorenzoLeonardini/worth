package dev.leonardini.worth.client.gui.windows;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * Popup window used to display the history of a card
 */
public class CardHistoryDialog extends JDialog {

	private static final long serialVersionUID = 6378209988325009361L;

	@SuppressWarnings("unchecked")
	/**
	 * Display the history inside the parameter list
	 * @param history
	 */
	public CardHistoryDialog(List<String> history) {
		setSize(600, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setAlwaysOnTop(true);
		getContentPane().setLayout(null);
		setTitle("Storia della card");

		@SuppressWarnings("rawtypes")
		JList historyList = new JList();
		historyList.setListData(history.toArray());
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(historyList);
		scrollPane.setBounds(30, 30, 540, 300);
		getContentPane().add(scrollPane);
	}
	
}
