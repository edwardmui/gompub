package com.orderfoodnow.pos.frontend;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.DishSize;
import com.orderfoodnow.pos.shared.menu.Dish;

public class GuiKeyAdapter extends KeyAdapter {

	private static final Logger logger = Logger.getLogger(GuiKeyAdapter.class.getName());
	protected Client gui;

	private GuiTextFieldHandler guiTextFieldHandler;

	public GuiKeyAdapter(Client gui) {
		logger.finest("Entered");
		this.gui = gui;
		guiTextFieldHandler = new GuiTextFieldHandler(gui);
	}

	public void keyPressed(KeyEvent event) {
		logger.finest("Entered");
		int pressedKeyCode = event.getKeyCode();
		logger.finer("pressedKeyCode=" + pressedKeyCode);

		switch (pressedKeyCode) {
		case KeyEvent.VK_O: // letter O
			if (event.isControlDown()) {
				try {
					Client.printDefaultHandler.openDrawer();
				} catch (Exception e) {
					logger.warning("Opening draw failed. error: " + e.getMessage());
					gui.setWarningFeedback("Opening draw failed. Check printer and try again...");
				}
			}
			break;
//No need to handle these case as the corresponding button is clicked and handled
//		case KeyEvent.VK_C:
//		case KeyEvent.VK_R:
//		case KeyEvent.VK_K:
//			if (event.isAltDown()) {
//				int balanceDue = gui.calculator.getBalanceDue();
//				if (balanceDue > 0) {
//					PaymentType paymentType = PaymentType.getEnumWithMnemonic(pressedKeyCode);
//					String inputText = "$" + paymentType.getShortHandChar();
//					if (paymentType != PaymentType.CASH) {
//						inputText += balanceDue;
//					}
//					gui.setInputText(inputText);
//				} else {
//					gui.setWarningFeedback("This order has no outstanding balance");
//				}
//			}
//			break;
		}
	}

	public void keyReleased(KeyEvent event) {
		logger.finest("Entered");
		int releasedKeyCode = event.getKeyCode();
		logger.finer("releasedKeyCode=" + releasedKeyCode);

		if (releasedKeyCode == KeyEvent.VK_ALT || releasedKeyCode == KeyEvent.VK_SHIFT || releasedKeyCode == KeyEvent.VK_CONTROL) {
			// Ignore ALT and SHIFT key release because for ALT-Fx and SHIFT-Fx
			// ALT/SHIFT releasing has undesired side effects such as clearing prompt text
			return;
		}

		if (releasedKeyCode >= KeyEvent.VK_F1 && releasedKeyCode <= KeyEvent.VK_F12) {
			// calling handleConfiguredEvent from keyPressed() does not work well because
			// holding on to an F key a bit too long resulting in multiple events been
			// generated.
			String prefix = "f";
			if (event.isShiftDown()) {
				prefix = "shiftF";
			} else if (event.isAltDown()) {
				prefix = "altF";
			}
			String functionKey = prefix + (releasedKeyCode - (KeyEvent.VK_F1 - 1));
			try {
				gui.handleConfiguredEvent(functionKey);
			} catch (Exception e) {
				logger.warning("handleFunctionKey failed. error: " + e.getMessage());
				e.printStackTrace();
			}

			gui.inputTextField.requestFocus();

			// The event.consume() is to disable/avoid the following predefined
			// functionality disable ALT+F4 where Windows closes the application
			// disable ALT+F6 where Windows closes the app
			// prevents the F10 key from popping up a Windows pull down menu
			// prevent the F8 key to do editing on Swing JTable when focus and does not give
			// up focus with Esc key. Only clicking get back the focus.
			// https://stackoverflow.com/questions/17294192/keystroke-action-with-f8-not-working
			event.consume();

		} else {
			Object object = event.getSource();
			if (object == gui.inputTextField || object == gui.dishTableView) {
				logger.finer("inputTextField or dishTableView in focus.");
				if (releasedKeyCode == KeyEvent.VK_UP || releasedKeyCode == KeyEvent.VK_DOWN) {
					handleNavigationKey();
					gui.dishTableView.requestFocus();

					if (releasedKeyCode == KeyEvent.VK_UP && gui.dishTableView.getSelectedRow() == 0) {
						gui.inputTextField.requestFocus();
					}
					return;
				} else if (object == gui.dishTableView
						&& (releasedKeyCode == KeyEvent.VK_LEFT || releasedKeyCode == KeyEvent.VK_RIGHT)) {
					handleNavigationKey();
					gui.dishTableView.requestFocus();
				}
			}

			if (object == gui.inputTextField) {
				logger.finer("inputTextField in focus.");
				guiTextFieldHandler.processKeyReleased(event);
			} else if (object == gui.orderTableView) {
				logger.finer("orderTableView in focus.");
				handleOrderTableViewKeyReleased(event);
			} else if (object == gui.businessTableView) {
				logger.finer("businessTableView in focus.");
				gui.showSelectedOrder(gui.getSelectedOrderNumberInBusinessTable());
			} else if (object == gui.arrivalTableView) {
				logger.finer("arrivalTableView in focus.");
				handleArrivalTableViewKeyReleased(event);
			} else if (object == gui.dishTableView) {
				logger.finer("dishTableView in focus.");
				if (releasedKeyCode == KeyEvent.VK_ENTER) {
					int rowCount = gui.dishTableView.getModel().getRowCount();

					if (rowCount > 0) {
						int selectedRow = gui.dishTableView.getSelectedRow();
						int targetedRow = selectedRow - 1; // because ENTER advance to next row/cell also
						if (selectedRow == 0) {
							targetedRow = rowCount - 1;
						}

						String codeColumnValue = gui.getDishViewHighlightedColumnValue(selectedRow,
								FeConstDefs.DISHVIEW_CODE);
						RowType rowType = RowType.getEnum(codeColumnValue.charAt(0));
						if (rowType != null) {
							gui.handlRowTypeSelection(rowType, targetedRow);
						} else if (gui.addSelectedRowToOrder(targetedRow)) {
							handleNavigationKey();
						}
					}
				}
			}

			if (releasedKeyCode == KeyEvent.VK_ESCAPE) {
				gui.showFullMenu(0, 0);
			}
		}
	}

	void handleOrderTableViewKeyReleased(KeyEvent event) {
		logger.finest("Entered");
		int releasedKey = event.getKeyCode();
		if (releasedKey == KeyEvent.VK_DELETE) {
			gui.deleteSelectedRow();
		}
	}

	void handleArrivalTableViewKeyReleased(KeyEvent event) {
		logger.finest("Entered");
		int releasedKey = event.getKeyCode();
		if (releasedKey == KeyEvent.VK_DELETE) {
			gui.handleArrivalTableUpdate();
		}
	}

	boolean handleNavigationKey() {
		logger.finest("Entered");
		int selectedRow = gui.dishTableView.getSelectedRow();
		if (selectedRow < 0) {
			return false; // no dish is selected
		}

		Dish dish = gui.getDishViewHighlightedDish(selectedRow);
		if (dish != null) {
			DishSize dishSize = DishSize.LARGE;
			int selectedCol = gui.dishTableView.getSelectedColumn();
			if (dish.getSmallPrice() != 0
					&& gui.dishTableView.getColumnName(selectedCol).equals(FeConstDefs.DISHVIEW_SMALL)) {
				dishSize = DishSize.SMALL;
			}

			gui.setPromptFeedback(dish, dishSize, 1);
		}
		return true;
	}
}
