package dev.leonardini.worth.client.gui.components;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import dev.leonardini.worth.client.ClientAPI;

public class MyDropTargetListener extends DropTargetAdapter {

	private final CardColumn panel;
	private final String projectName;
	private final ClientAPI clientApi;

	public MyDropTargetListener(CardColumn panel, String projectName, ClientAPI clientApi) {
		this.panel = panel;
		this.projectName = projectName;
		this.clientApi = clientApi;
		new DropTarget(panel, DnDConstants.ACTION_MOVE, this, true, null);
	}

	public void drop(DropTargetDropEvent event) {
		try {
			Transferable tr = event.getTransferable();
			CardLabel card = (CardLabel) tr.getTransferData(TransferableCard.getFlavor());

			if (event.isDataFlavorSupported(TransferableCard.getFlavor())) {
				event.acceptDrop(DnDConstants.ACTION_MOVE);
				
				if(!clientApi.moveCard(projectName, card.cardName, ((CardColumn)card.getParent()).column_location, this.panel.column_location)) {
					JOptionPane optionPane = new JOptionPane(clientApi.getMessage(), JOptionPane.ERROR_MESSAGE);    
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