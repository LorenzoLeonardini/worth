package dev.leonardini.worth.client.gui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.components.CardColumn;
import dev.leonardini.worth.client.gui.components.CardLabel;
import dev.leonardini.worth.client.gui.components.MyDropTargetListener;
import dev.leonardini.worth.client.gui.windows.CardCreationDialog;
import dev.leonardini.worth.client.gui.windows.ProjectDeletionDialog;
import dev.leonardini.worth.client.gui.windows.ProjectMembersDialog;
import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.server.data.Project.CardLocation;

/**
 * Main panel for a project view.
 * 
 * Manages all the cards, columns and actions related to a project
 */
public class ProjectPanel extends JPanel {
	
	private static final long serialVersionUID = 3204062925071718780L;

	private SpringLayout layout;
	private CardColumn todoArea, inprogressArea, toberevisedArea, doneArea;
	private String projectName;

	/**
	 * Initiate the object
	 * @param project the name of the project
	 * @param mainPanel used in order to go back to the main panel when exiting the project view
	 */
	public ProjectPanel(String project, MainPanel mainPanel) {
		this.projectName = project;
		layout = new SpringLayout();
		setLayout(layout);
		
		JButton back = new JButton("<- Indietro");
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainPanel.backToProjectList();
			}
		});
		back.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, back, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, back, 6, SpringLayout.WEST, this);
		add(back);

		JButton newCard = new JButton("Nuova card");
		newCard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog f = new CardCreationDialog(project);
				f.setVisible(true);
			}
		});
		newCard.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, newCard, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, newCard, -10, SpringLayout.HORIZONTAL_CENTER, this);
		add(newCard);
		
		JButton members = new JButton("Membri");
		members.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ProjectMembersDialog(project).setVisible(true);
			}
		});
		members.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, members, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, members, 10, SpringLayout.HORIZONTAL_CENTER, this);
		add(members);
		
		JButton delete = new JButton("Elimina");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ProjectDeletionDialog(project, mainPanel).setVisible(true);
			}
		});
		delete.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, delete, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, delete, -6, SpringLayout.EAST, this);
		add(delete);
		
		JLabel leftSide = new JLabel();
		layout.putConstraint(SpringLayout.WEST, leftSide, 6, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, leftSide, -3, SpringLayout.HORIZONTAL_CENTER, this);
		JLabel rightSide = new JLabel();
		layout.putConstraint(SpringLayout.WEST, rightSide, 3, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.EAST, rightSide, -6, SpringLayout.EAST, this);
		
		todoArea = new CardColumn("Todo:", CardLocation.TODO);
		new MyDropTargetListener(todoArea, project);
		JScrollPane todoScroll = wrapInScrollPane(todoArea);
		layout.putConstraint(SpringLayout.NORTH, todoScroll, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, todoScroll, 6, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, todoScroll, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, todoScroll, -3, SpringLayout.HORIZONTAL_CENTER, leftSide);
		add(todoScroll);
		
		addSeparator(leftSide);
		
		inprogressArea = new CardColumn("In progress:", CardLocation.IN_PROGRESS);
		new MyDropTargetListener(inprogressArea, project);
		JScrollPane inprogressScroll = wrapInScrollPane(inprogressArea);
		layout.putConstraint(SpringLayout.NORTH, inprogressScroll, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, inprogressScroll, 3, SpringLayout.HORIZONTAL_CENTER, leftSide);
		layout.putConstraint(SpringLayout.SOUTH, inprogressScroll, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, inprogressScroll, -3, SpringLayout.HORIZONTAL_CENTER, this);
		add(inprogressScroll);
		
		addSeparator(this);
		
		toberevisedArea = new CardColumn("To be revised:", CardLocation.TO_BE_REVISED);
		new MyDropTargetListener(toberevisedArea, project);
		JScrollPane toberevisedScroll = wrapInScrollPane(toberevisedArea);
		layout.putConstraint(SpringLayout.NORTH, toberevisedScroll, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, toberevisedScroll, 3, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.SOUTH, toberevisedScroll, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, toberevisedScroll, -3, SpringLayout.HORIZONTAL_CENTER, rightSide);
		add(toberevisedScroll);
		
		addSeparator(rightSide);
		
		doneArea = new CardColumn("Done:", CardLocation.DONE);
		new MyDropTargetListener(doneArea, project);
		JScrollPane doneScroll = wrapInScrollPane(doneArea);
		layout.putConstraint(SpringLayout.NORTH, doneScroll, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, doneScroll, 3, SpringLayout.HORIZONTAL_CENTER, rightSide);
		layout.putConstraint(SpringLayout.SOUTH, doneScroll, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, doneScroll, -6, SpringLayout.EAST, this);
		add(doneScroll);
		
		// Ask the server for all the cards of this project
		List<String> cards = ClientAPI.get().showCards(project);
		for(String card : cards) {
			// Ask the server for the info related to this card
			CardInfo info = ClientAPI.get().showCard(project, card);
			if(info == null) {
				System.err.println(card + ": " + ClientAPI.get().getMessage());
				continue;
			}
			CardLabel c = new CardLabel(projectName, info.name, info.description);
			
			// Add the card to the proper column
			locationToColumn(info.list).addCard(c);
		}
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSize();
			}
		});
	}
	
	private JScrollPane wrapInScrollPane(CardColumn column) {
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(column);
		scroll.setAutoscrolls(true);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(12);
		return scroll;
	}

	/**
	 * Add a new card to the "todo" column
	 * @param cardLabel
	 */
	public void addCard(CardLabel cardLabel) {
		todoArea.addCard(cardLabel);
		updateUI();
	}
	
	private void addSeparator(JComponent left) {
		JSeparator separator1 = new JSeparator(SwingConstants.VERTICAL);
		separator1.setPreferredSize(new Dimension(1, 1));
		separator1.setForeground(Color.gray);
		layout.putConstraint(SpringLayout.NORTH, separator1, 42, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, separator1, 0, SpringLayout.HORIZONTAL_CENTER, left);
		layout.putConstraint(SpringLayout.SOUTH, separator1, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, separator1, 1, SpringLayout.WEST, separator1);
		add(separator1);
	}
	
	private void updateSize() {
		if(getParent() != null && getParent().getParent() != null) {
			Dimension parentSize = getParent().getParent().getPreferredSize();
			setPreferredSize(new Dimension(parentSize.width, parentSize.height));
		}
		updateUI();
	}
	
	/**
	 * Move a card from a column to the other. If this is not possible (like if the card
	 * does not exist in the 'from' column), an error message is displayed.
	 * 
	 * @param card
	 * @param from
	 * @param to
	 */
	protected void moveCard(String card, CardLocation from, CardLocation to) {
		try {
			locationToColumn(to).addCard(locationToColumn(from).removeCard(card));
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane optionPane = new JOptionPane("Errore di sincronizzazione, chiudi e riapri il progetto", JOptionPane.ERROR_MESSAGE);    
			JDialog dialog = optionPane.createDialog("Errore");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		}
	}
	
	private CardColumn locationToColumn(CardLocation loc) {
		if(loc == CardLocation.TODO) return todoArea;
		if(loc == CardLocation.IN_PROGRESS) return inprogressArea;
		if(loc == CardLocation.TO_BE_REVISED) return toberevisedArea;
		if(loc == CardLocation.DONE) return doneArea;
		return null;
	}
	
}
