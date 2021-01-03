package dev.leonardini.worth.client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.gui.assets.GuiUtils;
import dev.leonardini.worth.client.gui.windows.CardHistoryDialog;

/**
 * This component represents a card in the CardColumn component
 * 
 * It handles part of the dragging and movement of a card
 */
public class CardLabel extends JLabel implements Serializable {

	private static final long serialVersionUID = 3169641733663285333L;
	
	public final String cardName, description;

	/**
	 * Initiate the object
	 * 
	 * @param projectName the project this card is part of
	 * @param cardName the card name
	 * @param description the card description
	 */
	public CardLabel(String projectName, String cardName, String description) {
		this.cardName = cardName;
		this.description = description;
		
		// Basic rendering 
		setText("<html><body style='padding: 8px 4px; width: 100%'><div style='font-size: 1.1em; width: 100%'>" + cardName + "</div><div style='font-weight: normal; margin-top: 4px; font-size: 0.95em; width: 100%'>" + description + "</div></body></html>");
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setOpaque(true);
		setBackground(GuiUtils.RANDOM_COLOR());
		setBorder(BorderFactory.createLineBorder(Color.black));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		// Drag n drop implementation
		// credits to http://zetcode.com/javaswing/draganddrop/
		setTransferHandler(new TransferHandler("card"));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JComponent c = (JComponent) e.getSource();
				TransferHandler handler = c.getTransferHandler();
	            handler.exportAsDrag(c, e, TransferHandler.COPY);
	        }
			@Override
			// When clicking on a card, a popup appears with its history
			public void mouseClicked(MouseEvent e) {
				List<String> history = ClientAPI.get().getCardHistory(projectName, cardName);
				if(history == null) {
					JOptionPane optionPane = new JOptionPane(ClientAPI.get().getMessage(), JOptionPane.ERROR_MESSAGE);    
					JDialog dialog = optionPane.createDialog("Errore");
					dialog.setAlwaysOnTop(true);
					dialog.setVisible(true);
				}
				else {
					new CardHistoryDialog(history).setVisible(true);
				}
			}
		});
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DragGestureListener() {
			@Override
			public void dragGestureRecognized(DragGestureEvent event) {
				Cursor cursor = Cursor.getDefaultCursor();
				CardLabel label = (CardLabel) event.getComponent();

				if (event.getDragAction() == DnDConstants.ACTION_MOVE) {
					cursor = DragSource.DefaultCopyDrop;
				}

				event.startDrag(cursor, new TransferableCard(label));
			}
		});
	}
	
}
