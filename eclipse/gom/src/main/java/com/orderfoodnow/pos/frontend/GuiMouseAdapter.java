package com.orderfoodnow.pos.frontend;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.orderfoodnow.pos.shared.Cart;

public class GuiMouseAdapter extends MouseAdapter {

	private static final Logger logger = Logger.getLogger(GuiMouseAdapter.class.getName());
	private Client gui;

	public GuiMouseAdapter(Client gui) {
		this.gui = gui;
	}

	public void mouseClicked(MouseEvent event) {
		logger.finest("Entered");
		Object object = event.getSource();
		if (object == gui.inputTextField) {
			gui.inputTextField.requestFocus();
		} else if (object == gui.dishTableView) {
			if (event.getClickCount() == 2) {
				int selectedRow = gui.dishTableView.getSelectedRow();
				String codeColumnValue = gui.getDishViewHighlightedColumnValue(selectedRow, FeConstDefs.DISHVIEW_CODE);
				RowType rowType = RowType.getEnum(codeColumnValue.charAt(0));
				if (rowType != null) {
					gui.handlRowTypeSelection(rowType, selectedRow);
				} else if (gui.addSelectedRowToOrder(selectedRow)) {
					gui.clearFeedbackText(); // need to clear the Dish Name on the Feedback
				}
			}
		} else if (object == gui.orderTableView) {
			gui.orderTableView.requestFocus();
			if (event.getClickCount() == 2) {
				gui.deleteSelectedRow();
				gui.inputTextField.requestFocus();
			}
		} else if (object == gui.businessTableView) {
			gui.businessTableView.requestFocus();
			if (event.getClickCount() == 2) {
				return;
			}

			// Warn user if cart has been modifed before clicking away to another order
			int orderNumberInOrderTableView = gui.cart.getOrder().getOrderNumber();
			if (orderNumberInOrderTableView != 0) {
				try {
					List<Cart> carts = Client.server.findCartByOrderNumber(orderNumberInOrderTableView);
					if (carts.isEmpty() == false) {
						Cart cartInServer = carts.get(0);
						if (cartInServer.equals(gui.cart) == false) {
							if (JOptionPane.showConfirmDialog(gui,
									"Order has been changed and not saved? Display selected order anyway?",
									"Confirm Message", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
								gui.setInfoFeedback("Please save changes before continue.");
								return;
							}
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					logger.warning("server.findCartByOrderNumber() failed. error: " + e.getMessage());
				}
			}

			gui.showSelectedOrder(gui.getSelectedOrderNumberInBusinessTable());
		} else if (object == gui.arrivalTableView) {
			if (event.getClickCount() == 2) {
				gui.showSelectedOrder(gui.getSelectedOrderNumberInArrivalTable());
			}
		}
	}
}
