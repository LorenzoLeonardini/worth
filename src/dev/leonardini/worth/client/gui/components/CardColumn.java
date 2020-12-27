package dev.leonardini.worth.client.gui.components;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import dev.leonardini.worth.data.Project.CardLocation;

public class CardColumn extends JPanel {
	
	private static final long serialVersionUID = -2613788183663876833L;
	
	private Map<String, CardLabel> cards = new HashMap<String, CardLabel>();
	private JLabel titleLabel;
	private SpringLayout layout;
	public final CardLocation column_location;

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
	}
	
	public void reload() {
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
		
		invalidate();
		validate();
		repaint();
	}
	
	public void addCard(CardLabel card) {
		add(card);
		cards.put(card.cardName, card);
		reload();
	}
	
	public void removeCard(CardLabel card) {
		remove(card);
		cards.remove(card.cardName);
		reload();
	}
	
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