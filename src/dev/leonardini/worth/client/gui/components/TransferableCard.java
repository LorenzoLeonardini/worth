package dev.leonardini.worth.client.gui.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class TransferableCard implements Transferable {
	private static DataFlavor cardFlavor;
	private final CardLabel card;
	private final String s;

	public TransferableCard(CardLabel card) {
		this.card = card;
		this.s = card.getText();
	}
	
	public static DataFlavor getFlavor() {
		try {
			if(cardFlavor == null)
				cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + CardLabel.class.getName() + "\"");
			return cardFlavor;
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { TransferableCard.cardFlavor, DataFlavor.stringFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(TransferableCard.cardFlavor) || flavor.equals(DataFlavor.stringFlavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(cardFlavor)) {
			return card;
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			return s;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}