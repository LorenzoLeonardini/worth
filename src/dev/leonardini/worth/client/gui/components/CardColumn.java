package dev.leonardini.worth.client.gui.components;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import dev.leonardini.worth.data.CardLocation;

/**
 * This component represents a column of cards in the project view
 * (i.e. the todo column, the in_progress column etc.)
 * 
 * It manages the display and updating of the view
 */
public class CardColumn extends JPanel {
	
	private static final long serialVersionUID = -2613788183663876833L;
	
	private Map<String, CardLabel> cards = new HashMap<String, CardLabel>();
	private JLabel titleLabel;
	private SpringLayout layout;
	protected final CardLocation column_location;

	/**
	 * Create the object
	 * 
	 * @param title The column title (ex. "Todo"). It is displayed at the top
	 * @param location The CardLocation value to which this column is linked to. It is used
	 * 			for the proper dragging of a card from a column to the other
	 */
	public CardColumn(String title, CardLocation location) {
		this.column_location = location;
		layout = new SpringLayout();
		setLayout(layout);
		
		titleLabel = new JLabel(title);
		layout.putConstraint(SpringLayout.NORTH, titleLabel, 2, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, titleLabel, 2, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, titleLabel, 20, SpringLayout.NORTH, titleLabel);
		layout.putConstraint(SpringLayout.EAST, titleLabel, -2, SpringLayout.EAST, this);
		add(titleLabel);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				reload();
			}
		});
	}
	
	/**
	 * Recomputes all the layout constraints and redraws the component
	 */
	private void reload() {
		JLabel prev = titleLabel;
		int h = 22;
		for(JLabel card : cards.values()) {
			layout.putConstraint(SpringLayout.NORTH, card, 6, SpringLayout.SOUTH, prev);
			layout.putConstraint(SpringLayout.WEST, card, 4, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.SOUTH, card, card.getPreferredSize().height, SpringLayout.NORTH, card);
			layout.putConstraint(SpringLayout.EAST, card, -4, SpringLayout.EAST, this);
			h += 14 + card.getPreferredSize().height;
			prev = card;
		}
		setPreferredSize(new Dimension(titleLabel.getPreferredSize().width, h));
		
		revalidate();
		repaint();
	}
	
	/**
	 * Add a CardLabel object to this column
	 * @param card
	 */
	public void addCard(CardLabel card) {
		add(card);
		cards.put(card.cardName, card);
		reload();
	}
	
	/**
	 * Remove a CardLabel object from this column
	 * @param card
	 */
	public void removeCard(CardLabel card) {
		remove(card);
		cards.remove(card.cardName);
		reload();
	}
	
	/**
	 * Remove and retrieve a CardLable object from this column, given its String name
	 * @param card
	 * @return the CardLabel object removed from the column
	 * @throws Exception in case the card is not part of this column
	 */
	public CardLabel removeCard(String card) throws Exception {
		if(!cards.containsKey(card)) {
			throw new Exception("No such card");
		}
		CardLabel c = cards.remove(card);
		remove(c);
		reload();
		return c;
	}
	
}