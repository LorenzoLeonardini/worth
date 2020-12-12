package dev.leonardini.worth.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import dev.leonardini.worth.client.ClientAPI;
import dev.leonardini.worth.client.ui.ProjectPanel.CardLabel;
import dev.leonardini.worth.data.CardInfo;
import dev.leonardini.worth.data.Project.CardLocation;

public class ProjectPanel extends JPanel {
	
	private static final long serialVersionUID = 3204062925071718780L;

	private SpringLayout layout;
	private CardColumn todoArea;
	private ClientAPI clientApi;
	private String projectName;

	public ProjectPanel(ClientAPI clientApi, String project, MainPanel mainPanel) {
		this.clientApi = clientApi;
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
		
		ProjectPanel _this = this;
		JButton members = new JButton("Membri");
		members.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MembersScreen(project, clientApi).setVisible(true);
			}
		});
		members.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, members, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, members, -65, SpringLayout.HORIZONTAL_CENTER, this);
		add(members);

		JButton newCard = new JButton("Nuova card");
		newCard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog f = new CreateCardScreen(clientApi, project, _this);
				f.setVisible(true);
			}
		});
		newCard.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, newCard, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, newCard, -6, SpringLayout.EAST, this);
		add(newCard);
		
		JLabel leftSide = new JLabel();
		layout.putConstraint(SpringLayout.WEST, leftSide, 6, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, leftSide, -3, SpringLayout.HORIZONTAL_CENTER, this);
		JLabel rightSide = new JLabel();
		layout.putConstraint(SpringLayout.WEST, rightSide, 3, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.EAST, rightSide, -6, SpringLayout.EAST, this);
		
		todoArea = new CardColumn("Todo:", CardLocation.TODO);
		layout.putConstraint(SpringLayout.NORTH, todoArea, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, todoArea, 6, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, todoArea, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, todoArea, -3, SpringLayout.HORIZONTAL_CENTER, leftSide);
		add(todoArea);
		
		addSeparator(leftSide);
		
		CardColumn inprogressArea = new CardColumn("In progress:", CardLocation.IN_PROGRESS);
		layout.putConstraint(SpringLayout.NORTH, inprogressArea, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, inprogressArea, 3, SpringLayout.HORIZONTAL_CENTER, leftSide);
		layout.putConstraint(SpringLayout.SOUTH, inprogressArea, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, inprogressArea, -3, SpringLayout.HORIZONTAL_CENTER, this);
		add(inprogressArea);
		
		addSeparator(this);
		
		CardColumn toberevisedArea = new CardColumn("To be revised:", CardLocation.TO_BE_REVISED);
		layout.putConstraint(SpringLayout.NORTH, toberevisedArea, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, toberevisedArea, 3, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.SOUTH, toberevisedArea, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, toberevisedArea, -3, SpringLayout.HORIZONTAL_CENTER, rightSide);
		add(toberevisedArea);
		
		addSeparator(rightSide);
		
		CardColumn doneArea = new CardColumn("Done:", CardLocation.DONE);
		layout.putConstraint(SpringLayout.NORTH, doneArea, 6, SpringLayout.SOUTH, members);
		layout.putConstraint(SpringLayout.WEST, doneArea, 3, SpringLayout.HORIZONTAL_CENTER, rightSide);
		layout.putConstraint(SpringLayout.SOUTH, doneArea, -6, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, doneArea, -6, SpringLayout.EAST, this);
		add(doneArea);
		
		List<String> cards = clientApi.showCards(project);
		for(String card : cards) {
			CardInfo info = clientApi.showCard(project, card);
			if(info == null) {
				System.err.println(card + ": " + clientApi.getMessage());
				continue;
			}
			CardLabel c = new CardLabel(info.name, info.description);
			switch(info.list) {
				case TODO:
					todoArea.addCard(c);
					break;
				case IN_PROGRESS:
					inprogressArea.addCard(c);
					break;
				case TO_BE_REVISED:
					toberevisedArea.addCard(c);
					break;
				case DONE:
					doneArea.addCard(c);
					break;
			}
		}
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSize();
			}
		});
	}

	public void addCard(CardLabel cardLabel) {
		todoArea.addCard(cardLabel);
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
	
	class CardColumn extends JPanel {
		
		private static final long serialVersionUID = -2613788183663876833L;
		
		private List<CardLabel> cards = new ArrayList<CardLabel>();
		private JLabel titleLabel;
		private SpringLayout layout;
		public final CardLocation column_location;

		public CardColumn(String title, CardLocation location) {
			this.column_location = location;
			layout = new SpringLayout();
			setLayout(layout);
			
			new MyDropTargetListener(this);
			
			titleLabel = new JLabel(title);
			layout.putConstraint(SpringLayout.NORTH, titleLabel, 2, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.WEST, titleLabel, 2, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.SOUTH, titleLabel, 20, SpringLayout.NORTH, titleLabel);
			layout.putConstraint(SpringLayout.EAST, titleLabel, -2, SpringLayout.EAST, this);
			add(titleLabel);
		}
		
		public void reload() {
			JLabel prev = titleLabel;
			for(JLabel card : cards) {
				layout.putConstraint(SpringLayout.NORTH, card, 6, SpringLayout.SOUTH, prev);
				layout.putConstraint(SpringLayout.WEST, card, 4, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.SOUTH, card, card.getPreferredSize().height, SpringLayout.NORTH, card);
				layout.putConstraint(SpringLayout.EAST, card, -4, SpringLayout.EAST, this);
				prev = card;
			}
			SwingUtilities.updateComponentTreeUI(this);
		}
		
		public void addCard(CardLabel card) {
			add(card);
			cards.add(card);
			reload();
		}
		
		public void removeCard(CardLabel card) {
			remove(card);
			cards.remove(card);
			reload();
		}
		
	}
	
	public static class CardLabel extends JLabel implements Serializable {

		private static final long serialVersionUID = 3169641733663285333L;
		
		public final String cardName, description;

		public CardLabel(String cardName, String description) {
			this.cardName = cardName;
			this.description = description;
			setText("<html><body style='padding: 8px 4px; width: 100%'><div style='font-size: 1.1em; width: 100%'>" + cardName + "</div><div style='font-weight: normal; margin-top: 4px; font-size: 0.95em; width: 100%'>" + description + "</div></body></html>");
			setVerticalAlignment(SwingConstants.CENTER);
			setHorizontalAlignment(SwingConstants.CENTER);
			setOpaque(true);
			setBackground(FontUtils.RANDOM_COLOR());
			setBorder(BorderFactory.createLineBorder(Color.black));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setTransferHandler(new TransferHandler("card"));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
		            var c = (JComponent) e.getSource();
		            var handler = c.getTransferHandler();
		            handler.exportAsDrag(c, e, TransferHandler.COPY);
		        }
				@Override
				public void mouseClicked(MouseEvent e) {
					JOptionPane.showMessageDialog(null, "Eggs are not supposed to be green.");
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
	
	private class MyDropTargetListener extends DropTargetAdapter {

		private final CardColumn panel;

		public MyDropTargetListener(CardColumn panel) {
			this.panel = panel;
			new DropTarget(panel, DnDConstants.ACTION_MOVE, this, true, null);
		}

		public void drop(DropTargetDropEvent event) {
			try {
				Transferable tr = event.getTransferable();
				CardLabel card = (CardLabel) tr.getTransferData(TransferableCard.getFlavor());

				if (event.isDataFlavorSupported(TransferableCard.getFlavor())) {
					event.acceptDrop(DnDConstants.ACTION_MOVE);
					
					if(clientApi.moveCard(projectName, card.cardName, ((CardColumn)card.getParent()).column_location, this.panel.column_location)) {
						((CardColumn)card.getParent()).removeCard(card);
						this.panel.addCard(card);
					} else {
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

}

class TransferableCard implements Transferable {
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
