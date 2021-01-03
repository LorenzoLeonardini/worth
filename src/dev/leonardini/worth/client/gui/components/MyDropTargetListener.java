package dev.leonardini.worth.client.gui.components;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import dev.leonardini.worth.client.ClientAPI;

/**
 * DropTargetAdaptor used for the drag and drop of cards
 *
 * See http://zetcode.com/javaswing/draganddrop/ for more info
 */
public class MyDropTargetListener extends DropTargetAdapter {

	private final CardColumn panel;
	private final String projectName;

	public MyDropTargetListener(CardColumn panel, String projectName) {
		this.panel = panel;
		this.projectName = projectName;
		new DropTarget(panel, DnDConstants.ACTION_MOVE, this, true, null);
	}

	/**
	 * When a card is dropped into another column, a request to move the card is sent to the server
	 * The card won't be moved. The server will send a system message informing of the movement, the
	 * chat manager will take care of that.
	 */
	public void drop(DropTargetDropEvent event) {
		try {
			Transferable tr = event.getTransferable();
			CardLabel card = (CardLabel) tr.getTransferData(TransferableCard.getFlavor());

			if (event.isDataFlavorSupported(TransferableCard.getFlavor())) {
				event.acceptDrop(DnDConstants.ACTION_MOVE);
				
				if(!ClientAPI.get().moveCard(projectName, card.cardName, ((CardColumn)card.getParent()).column_location, this.panel.column_location)) {
					JOptionPane optionPane = new JOptionPane(ClientAPI.get().getMessage(), JOptionPane.ERROR_MESSAGE);    
					JDialog dialog = optionPane.createDialog("Errore");
					dialog.setAlwaysOnTop(true);
					dialog.setVisible(true);
				}
				
				event.dropComplete(true);
				return;
			}

			event.rejectDrop();
		}
		catch (Exception e) {
			e.printStackTrace();
			event.rejectDrop();
		}
	}
}