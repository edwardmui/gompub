package com.orderfoodnow.pos.frontend;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.swing.JOptionPane;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.DishSize;
import com.orderfoodnow.pos.shared.OrderStatus;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PaymentType;
import com.orderfoodnow.pos.shared.SharedConstDefs;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.order.CustomerProfile;
import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.DeliveryInfo;
import com.orderfoodnow.pos.shared.order.DeliveryInfoTable;
import com.orderfoodnow.pos.shared.order.DineInInfoTable;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.order.OrderTable;
import com.orderfoodnow.pos.shared.order.Payment;
import com.orderfoodnow.pos.shared.order.ToGoInfoTable;
import com.orderfoodnow.pos.shared.staff.Employee;
import com.orderfoodnow.pos.shared.staff.RoleType;

public class GuiTextFieldHandler {

	private static final Logger logger = Logger.getLogger(GuiTextFieldHandler.class.getName());

	private Client gui;
	private int releasedKeyCode;

	public GuiTextFieldHandler(Client gui) {
		this.gui = gui;
	}

	void processKeyReleased(KeyEvent event) {
		logger.finest("Entered");
		gui.clearFeedbackText();
		releasedKeyCode = event.getKeyCode();

		if (releasedKeyCode == KeyEvent.VK_ESCAPE) {
			gui.showFullMenu(0, 0);
			return;
		}

		String s = gui.inputTextField.getText();
		s = s.trim().toUpperCase();
		if (s.isEmpty()) {
			gui.showFullMenu(0, 0);
			return;
		}

		int len = s.length();
		char firstChar = s.charAt(0);
		String abbrevivation = null;
		String code = null;
		int quantity = 1;

		if (s.indexOf(':') != -1) { // time input
			if ((len > 1) && ((releasedKeyCode == KeyEvent.VK_ENTER) || (releasedKeyCode == KeyEvent.VK_SPACE))) {
				if (gui.isModifyingOrderAllowed() == false) {
					// feedback is done in the isModifyingOrderAllowed()
					return;
				}

				long requestedTime = Util.parseTime(s);
				boolean clearRequestedTime = s.equals(":0");
				if (requestedTime > 0 || clearRequestedTime) {
					long previousRequestedTime = gui.cart.getToGoInfo().getRequestedTime();
					if (clearRequestedTime) {
						if (previousRequestedTime == 0) {
							gui.setWarningFeedback("Requested time not was set previously for clearing");
							return;
						} else {
							gui.cart.getToGoInfo().setRequestedTime(0);
							gui.cart.getOrder().setStatus(OrderStatus.MAKING);
							gui.setInfoFeedback("Cleared previously set request time of "
									+ Util.formatTimeNoSeconds(previousRequestedTime));
						}
					} else {
						String infoFeedback = "";
						// Note that the queueTimeMinute does NOT include the to be added dishes in this
						// order.
						// If number of dishes to be added much more, it can be an over promise. Could
						// re-check at commit time.
						int queueTimeMinutes = gui.cart.getToGoInfo().getQueueTimeMinutes();
						long earliestAvailableTime = System.currentTimeMillis() + (queueTimeMinutes * 60 * 1000L);
						logger.fine("earliestAvailableTime=" + Util.formatTimeNoSeconds(earliestAvailableTime));
						if (requestedTime < earliestAvailableTime) {
							gui.setWarningFeedback("Earliest  availabe time is in " + queueTimeMinutes + " minutes at "
									+ Util.formatTimeNoSeconds(earliestAvailableTime));
							return;
						} else {
							gui.cart.getToGoInfo().setRequestedTime(requestedTime);
							if (previousRequestedTime == 0) {
								infoFeedback = "Set requested time to " + Util.formatTimeNoSeconds(requestedTime);
							} else {
								infoFeedback = "Changed previously requested time from "
										+ Util.formatTimeNoSeconds(previousRequestedTime) + " to "
										+ Util.formatTimeNoSeconds(requestedTime);
							}
						}

						OrderStatus previousOrderStatus = gui.cart.getOrder().getStatus();
						long heldCutoffTime = 20 * 60 * 1000; // 20 minutes
						if (requestedTime - earliestAvailableTime > heldCutoffTime) {
							if (previousOrderStatus != OrderStatus.HOLDING) {
								gui.cart.getOrder().setStatus(OrderStatus.HOLDING);
								logger.fine("Setting orderStatus to OrderStatus.HOLDING state as" + " requestedTime="
										+ Util.formatTimeNoSeconds(requestedTime) + " heldCutoffTime="
										+ Util.formatTimeNoSeconds(heldCutoffTime + System.currentTimeMillis()));
								infoFeedback += " and put order on HOLD from printing";
							}
						} else {
							if (previousOrderStatus != OrderStatus.MAKING) {
								gui.cart.getOrder().setStatus(OrderStatus.MAKING);
								infoFeedback += " and put order on MAKING";
							}
						}

						gui.setInfoFeedback(infoFeedback);
					}

					gui.buildOrderTable();
					gui.clearInputText();
				} else {
					gui.setWarningFeedback(
							"Invalid time input. Valid examples: 6:45 or :15, or :0 to clear. Append A or P to specify AM/PM");
				}
			}
			return;
		}

		if (firstChar != '/' && firstChar != '{' && gui.isModifyingOrderAllowed() == false) {
			return;
		}

		switch (firstChar) {
		case '?':
			processQuestionMark();
			break;
		case '`':
			processBackTick();
			break;
		case '{':
			processOpenCurly();
			break;
		case '[':
			processOpenSquareBracket();
			break;
		case '"':
			processDoubleQuote();
			break;
		case '\'':
			processSingleQuote();
			break;
		case '+':
			processPlusSign();
			break;
		case '-':
			processMinusSign();
			break;
		case '=':
			processEqualSign();
			break;
		case '$':
			processDollarSign();
			break;
		case '#':
			processPoundSign();
			break;
		case '/':
			processSlash();
			break;
		case '@':
			processAtSign();
			break;
		case '>':
			processGreaterThan();
			break;
		default:
			DishSize dishSize;
			if (s.contains(".")) {
				dishSize = DishSize.SMALL;
				// Replace period with empty string.
				// Must have double backslash to escape the period.
				// Without the double backslashes, the period is treated as a regular expression
				// to replace each character with empty string
				s = s.replaceAll("\\.", "");
				if (s.isEmpty() == false) {
					len = s.length();
					firstChar = s.charAt(0);
				} else {
					return;
				}
			} else {
				dishSize = DishSize.LARGE;
			}

			String codeColumnValue = gui.getDishViewHighlightedColumnValue(0, FeConstDefs.DISHVIEW_CODE);
			RowType rowType = (codeColumnValue == null ? null : RowType.getEnum(codeColumnValue.charAt(0)));
			String columnName;
			if (rowType == null) {
				columnName = dishSize == DishSize.LARGE ? FeConstDefs.DISHVIEW_LARGE : FeConstDefs.DISHVIEW_SMALL;
			} else {
				columnName = FeConstDefs.DISHVIEW_NAME;
			}

			for (int column = 0; column < gui.dishTableView.getColumnCount(); ++column) {
				if (gui.dishTableView.getColumnName(column).equals(columnName)) {
					gui.dishTableView.setColumnSelectionInterval(column, column);
					break;
				}
			}

			String customizerPart = null;
			if (Character.isLetter(firstChar)) { // abbreviation input
				abbrevivation = s;
			} else if (Character.isDigit(firstChar)) {
				gui.codeFiltered(s);
				// DLLL.. or DDLLL... is quantity and abbreviation DDD, DDDD, DDDDD is dish code
				int firstDigit = Character.digit(firstChar, 10); // base 10
				quantity = firstDigit;
				if (len > 1) {
					char secondChar = s.charAt(1);
					int secondDigit = Character.digit(secondChar, 10);
					if (Character.isDigit(secondChar)) {
						quantity = firstDigit * 10 + secondDigit;
						if (len > 2) {
							char thirdChar = s.charAt(2);
							if (Character.isDigit(thirdChar)) {
								code = s.substring(len - 3); // right(3)
								quantity = 1;
								if (4 == len) {
									quantity = firstDigit;
								} else if (len == 5 && Character.isDigit(s.charAt(3))
										&& Character.isDigit(s.charAt(4))) {
									quantity = firstDigit * 10 + secondDigit;
								} else if (s.length() >= 5) {
									gui.setWarningFeedback(
											"Can only enter up to 5 characters here for quanity and dish code");
									gui.setInputText(s.substring(0, 5));
									return;
								}
							} else {
								abbrevivation = s.substring(2);
							}
						}
					} else if (Character.isLetter(secondChar)) {
						abbrevivation = s.substring(1);
					}
				}
			} else {
				gui.setWarningFeedback("Unexpected first character input is '" + firstChar + "'");
				return;
			}

			logger.finer("quantity=" + quantity);
			logger.finer("code=" + code);
			logger.finer("abbrevivation=" + abbrevivation);
			logger.finer("customizer=" + customizerPart);
			if (abbrevivation != null) {
				gui.abbreviationFiltered(abbrevivation);
			} else if (code != null && code.length() == 3) {
				gui.codeFiltered(code);
			}

			if (gui.dishTableRows.size() <= 0) {
				gui.clearFeedbackText();
				return;
			}

			Dish dish = Client.dishes[gui.targetDishId];
			gui.setPromptFeedback(dish, dishSize, quantity);

			if (releasedKeyCode == KeyEvent.VK_ENTER || releasedKeyCode == ']'
					|| releasedKeyCode == KeyEvent.VK_SPACE && s.indexOf('[') == -1) {
				if (dishSize == DishSize.SMALL && dish.isSmallSizeValid() == false) {
					gui.setWarningFeedback(dish.getQuotedShortName() + " has NO small size");
					return;
				}

				if (gui.isModifyingOrderAllowed()) {
					if (quantity <= 0) {
						gui.setWarningFeedback(s + " does not specify a valid quantity. Quantity=" + quantity);
						return;
					}

					int dishCategory = dish.getCategory();
					if (dishCategory == Client.customizerCategory || dishCategory == Client.exchangeCondimentCategory) {
						OrderItem orderItem = gui.cart.getOrderItemTray()
								.getOrderItem(gui.getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO));
						if (orderItem == null) {
							gui.setWarningFeedback(dish.getQuotedShortName()
									+ " by itself is not an orderable item. It has to be associated with a dish.");
							return;
						}

						if (orderItem.isVoided()) {
							gui.setWarningFeedback("Cannot add " + dish.getQuotedShortName() + " to voided item '"
									+ orderItem.getDishShortName() + "'");
							return;
						}

						if (dishCategory == Client.customizerCategory) {
							Customizer customizer = new Customizer(orderItem.getOrderItemId(), dish);
							gui.cart.getCustomizerTray().addCustomizer(customizer);
						} else if (dishCategory == Client.exchangeCondimentCategory) {
							Exchanger exchanger = new Exchanger(orderItem.getOrderItemId(), dish);
							if (gui.cart.getExchangerTray().isExchangerAlreadyAdded(exchanger)) {
								gui.setWarningFeedback(
										dish.getQuotedShortName() + " exchanger is already added to this order item.");
								return;
							} else {
								gui.cart.getExchangerTray().addExchanger(exchanger);
							}
						}
					} else {
						if (gui.isValidToAddCouponDish(dish) == false) {
							return;
						}
						gui.cart.getOrderItemTray().addOrderItem(dish, dishSize, quantity, gui.cart);
						logger.fine("Added dish to order. " + dish);
						gui.updateEstimatedTime(quantity);
					}

					gui.buildOrderTable();
					gui.showFullMenu(0, 0);
					gui.clearFeedbackText(); // need to clear the Dish Name on the Feedback
				}
			}
		}
	}

	private void processBackTick() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().replaceAll(",\\s+", ",");
		gui.inputTextField.setText(s);
		s = gui.inputTextField.getText().substring(1).trim(); // drop '`'
		int len = s.length();

		OrderType orderType = gui.cart.getOrder().getType();
		if (len == 0) {
			String phoneNumber = gui.cart.getToGoInfo().getPhoneNumber();
			String customerName = gui.cart.getToGoInfo().getCustomerName();
			switch (orderType) {
			case DELIVERY:
				if (phoneNumber.isEmpty()) {
					gui.setPromptFeedback(
							orderType.getDisplayName() + ": Enter customer phone number for delivery address");
				} else {
					if (gui.cart.getDeliveryInfo().isAddressEmpty()) {
						if (gui.cart.getToGoInfo().getCustomerName().isEmpty()) {
							gui.setPromptFeedback(orderType.getDisplayName() + ": Enter customer name and address");
						} else {
							gui.setPromptFeedback(orderType.getDisplayName() + ": Enter customer address");
						}
					} else {
						gui.setPromptFeedback(orderType.getDisplayName() + ": Name: " + customerName + ";  Address: "
								+ gui.cart.getDeliveryInfo().getAddressNoSpace() + " (tab to populate)");
						if (releasedKeyCode == KeyEvent.VK_TAB) {
							gui.setInputText("`" + customerName + SharedConstDefs.DELIMITER
									+ gui.cart.getDeliveryInfo().getAddressNoSpace());
							return;
						}
					}
				}
				break;
			case PHONE_IN:
				if (phoneNumber.isEmpty()) {
					gui.setPromptFeedback(orderType.getDisplayName() + ": Enter customer phone number");
				} else {
					if (customerName.isEmpty()) {
						gui.setPromptFeedback(orderType.getDisplayName() + ": Enter customer name");
					} else {
						gui.setPromptFeedback(
								orderType.getDisplayName() + ": Phone: " + phoneNumber + ";  Name: " + customerName);
					}
				}
				break;
			case WALK_IN:
				if (phoneNumber.isEmpty() || customerName.isEmpty()) {
					gui.setPromptFeedback(orderType.getDisplayName()
							+ ": Enter name or phone number for reference when order is ready");
				} else if (phoneNumber.isEmpty() == false && customerName.isEmpty() == false) {
					gui.setPromptFeedback(
							orderType.getDisplayName() + ": Phone: " + phoneNumber + ";  Name: " + customerName);
				}
				break;
			case DINE_IN:
				String tableNumber = gui.cart.getDineInInfo().getTableNumber();
				gui.showAllTableNumbers();
				if (tableNumber.isEmpty()) {
					List<String> tableNumbers = new ArrayList<>();
					tableNumbers.addAll(Configuration.getTableNumbers().keySet());
					gui.setPromptFeedback(orderType.getDisplayName() + ": Tab for auto complete table to: '"
							+ tableNumbers.get(0) + "' or select from below");
					if (releasedKeyCode == KeyEvent.VK_TAB) {
						gui.addSelectedTableNumberToInputText(tableNumbers.get(0));
						return;
					}
				} else {
					int guestCount = gui.cart.getDineInInfo().getGuestCount();
					if (guestCount == 0) {
						gui.setPromptFeedback(orderType.getDisplayName() + ": Tab for auto complete table to '"
								+ tableNumber + "' then enter number of guests.");
						if (releasedKeyCode == KeyEvent.VK_TAB) {
							gui.setInputText("`" + tableNumber + SharedConstDefs.DELIMITER);
							return;
						}
					} else {
						gui.setPromptFeedback(
								orderType.getDisplayName() + ": Table: " + tableNumber + "  Guest: " + guestCount);
					}
				}
				break;
			default:
				break;
			}

			return;
		}

		if (len == 1) {
			switch (orderType) {
			case DINE_IN:
				String filter = s;
				List<String> tableNumbers = new ArrayList<>();
				for (String tableNumber : Configuration.getTableNumbers().keySet()) {
					if (tableNumber.toLowerCase().startsWith(filter.toLowerCase())) {
						tableNumbers.add(tableNumber);
					}
				}

				if (tableNumbers.isEmpty()) {
					tableNumbers.addAll(Configuration.getTableNumbers().keySet());
				}

				if (gui.showTableNumbers(filter) > 0) {
					gui.setPromptFeedback(
							"Tab for auto complete table to: '" + tableNumbers.get(0) + "' or select from below");
				} else {
					gui.setPromptFeedback("No table name starts with '" + s + "'");
					return;
				}

				if (releasedKeyCode == KeyEvent.VK_TAB) {
					gui.addSelectedTableNumberToInputText(tableNumbers.get(0));
					return;
				}
				break;
			default:
				break;
			}
		}

		if (s.indexOf(SharedConstDefs.DELIMITER) > 0) {
			int delimiterCount = 0;
			for (int i = 0; i < len; ++i) {
				if (s.charAt(i) == SharedConstDefs.DELIMITER) {
					++delimiterCount;
				}
			}

			String[] fields = s.split(String.valueOf(SharedConstDefs.DELIMITER));
			switch (orderType) {
			case DELIVERY:
				switch (delimiterCount) {
				case 1: {
					String streetNameStartsWithString = fields.length > 1 ? fields[delimiterCount] : "";
					gui.showStreetNameStartsWithIgnoreLeadingHouseNumber(streetNameStartsWithString);

					if (streetNameStartsWithString.isEmpty()) {
						gui.setPromptFeedback("Enter house number, type street name, double click or tab to select");
					} else {
						String street = gui.getDishViewHighlightedColumnValue(0, FeConstDefs.DISHVIEW_NAME);
						gui.setPromptFeedback("Start typing street name or tab for: " + street);
					}

					if (releasedKeyCode == KeyEvent.VK_TAB) {
						gui.addSelectedStreetToInputText(0);
						return;
					}
					break;
				}
				case 2: {
					String filter = "";
					List<String> deliveryCities = new ArrayList<>();
					if (fields.length > delimiterCount) {
						filter = fields[delimiterCount];
						for (String city : Configuration.getDeliveryCities()) {
							if (city.toLowerCase().startsWith(filter.toLowerCase())) {
								deliveryCities.add(city);
							}
						}
					}

					if (deliveryCities.isEmpty()) {
						deliveryCities = Configuration.getDeliveryCities();
					}

					gui.showCityNames(filter);

					gui.setPromptFeedback(
							"Tab for auto complete city to: '" + deliveryCities.get(0) + "' or select from below");

					if (releasedKeyCode == KeyEvent.VK_TAB) {
						gui.setInputText(
								'`' + Util.buildCommaSepartedString(fields, delimiterCount) + deliveryCities.get(0));
						return;
					}
					break;
				}
				case 3: {
					List<String> deliveryStates = new ArrayList<>();
					if (fields.length > delimiterCount) {
						for (String state : Configuration.getDeliveryStates()) {
							if (state.toLowerCase().startsWith(fields[delimiterCount].toLowerCase())) {
								deliveryStates.add(state);
							}
						}
					}

					if (deliveryStates.isEmpty()) {
						deliveryStates = Configuration.getDeliveryStates();
					}

					gui.setPromptFeedback("Tab for auto complete state to first option: " + deliveryStates);

					if (releasedKeyCode == KeyEvent.VK_TAB) {
						gui.setInputText(
								'`' + Util.buildCommaSepartedString(fields, delimiterCount) + deliveryStates.get(0));
						return;
					}
					break;
				}
				case 4: {
					List<String> deliveryZips = new ArrayList<>();
					if (fields.length > delimiterCount) {
						for (String zip : Configuration.getDeliveryZips()) {
							if (zip.toLowerCase().startsWith(fields[delimiterCount].toLowerCase())) {
								deliveryZips.add(zip);
							}
						}
					}

					if (deliveryZips.isEmpty()) {
						deliveryZips = Configuration.getDeliveryZips();
					}

					gui.setPromptFeedback("Tab for auto complete zip to first option: " + deliveryZips);

					if (releasedKeyCode == KeyEvent.VK_TAB) {
						gui.setInputText(
								'`' + Util.buildCommaSepartedString(fields, delimiterCount) + deliveryZips.get(0));
						return;
					}
					break;
				}
				default:
					gui.setWarningFeedback(orderType.getDisplayName() + ": The input has too many ("
							+ SharedConstDefs.DELIMITER + ") separators");
					return;
				}
				break;
			case DINE_IN:
				switch (delimiterCount) {
				case 1:
					gui.setPromptFeedback("Enter number of guests");
					break;
				default:
					gui.setWarningFeedback(orderType.getDisplayName() + ": The input has too many ("
							+ SharedConstDefs.DELIMITER + ") separators");
					return;
				}
				break;
			default:
				break;
			}
		}

		String replaceText = "";
		if (releasedKeyCode == KeyEvent.VK_ENTER) {
			String phoneNumber = "";
			if (s.matches("\\d+")) {
				// Only digits are entered, treat as a phone number
				if (len == 10) {
					phoneNumber = s;
				} else if (len == 7) {
					phoneNumber = Configuration.getRestaurantAreaCode() + s; // default area code
				} else {
					gui.setWarningFeedback("Enter either a 7- or 10-digit phone number");
					replaceText = '`' + s; // put the original text back
				}

				if (phoneNumber.isEmpty() == false) {
					if (phoneNumber.length() > ToGoInfoTable.PHONE_NUMBER_SIZE) {
						gui.setWarningFeedback("Enter either a 7- or 10-digit phone number");
						replaceText = '`' + s; // put the original text back
					}

					gui.cart.getToGoInfo().setPhoneNumber(phoneNumber);
					if (phoneNumber.equals(SharedConstDefs.DEFAULT_PHONE_NUMBER) == false) {
						try {
							CustomerProfile customerProfile = Client.server.findCustomer(phoneNumber);
							if (customerProfile == null) {
								String customerName = gui.cart.getToGoInfo().getCustomerName();
								boolean isAddressEmpty = gui.cart.getDeliveryInfo().isAddressEmpty();
								int deliveryCharge = gui.cart.getDeliveryInfo().getDeliveryCharge();
								String note = gui.cart.getToGoInfo().getNote();
								if (customerName.isEmpty() == false || isAddressEmpty == false || deliveryCharge != 0
										|| note.isEmpty() == false) {
									String confirmMessage = "NEW CUSTOMER!\n Clear the following existing information and re-enter them?\n";
									if (customerName.isEmpty() == false) {
										confirmMessage = confirmMessage + "Name: " + customerName + "\n";
									}

									if (isAddressEmpty == false) {
										confirmMessage = confirmMessage + "Address: "
												+ gui.cart.getDeliveryInfo().getAddressNoSpace() + "\n";
									}

									if (deliveryCharge != 0) {
										confirmMessage = confirmMessage + String.format("Delivery Charge: $%4.2f\n", deliveryCharge/100.0);
									}

									if (note.isEmpty() == false) {
										confirmMessage = confirmMessage + "Customer Note: " + note + "\n";
									}

									if (JOptionPane.showConfirmDialog(gui, confirmMessage, "Confirm Message",
											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
										gui.cart.getToGoInfo().clearCustomerProfile();
										gui.cart.setDeliveryInfo(new DeliveryInfo());
									}
								}
								gui.cart.getToGoInfo().setPhoneNumber(phoneNumber);

								String feedBackString = "NEW CUSTOMER! ";
								switch (orderType) {
								case DELIVERY:
									String deliveryFeedBack = "";
									replaceText = "=d1";
									if (gui.cart.getDeliveryInfo().isAddressEmpty()) {
										if (gui.cart.getToGoInfo().getCustomerName().isEmpty()) {
											if (gui.cart.getDeliveryInfo().getDeliveryCharge() == 0) {
												deliveryFeedBack = "Enter name, address, and delivery charge. '=d1' is for zone 1 charge.";
											} else {
												deliveryFeedBack = "Enter name and address for delivery.";
											}
										} else {
											if (gui.cart.getDeliveryInfo().getDeliveryCharge() == 0) {
												deliveryFeedBack = "Verify name, then enter address and delivery charge. '=d1' is for zone 1 charge.";
											} else {
												deliveryFeedBack = "Verify name and delivery charge, then enter address for delivery.";
											}
										}
									} else {
										if (gui.cart.getToGoInfo().getCustomerName().isEmpty()) {
											if (gui.cart.getDeliveryInfo().getDeliveryCharge() == 0) {
												deliveryFeedBack = "Verify address, then enter name and delivery charge. '=d1' is for zone 1 charge.";
											} else {
												deliveryFeedBack = "Verify address and delivery charge, then enter name for delivery.";
											}
										} else {
											if (gui.cart.getDeliveryInfo().getDeliveryCharge() == 0) {
												deliveryFeedBack = "Verify name and address, then enter delivery charge. '=d1' is for zone 1 charge.";
											} else {
												deliveryFeedBack = "Verify name, address, and delivery charge.";
											}
										}
									}
									feedBackString += deliveryFeedBack;
									break;
								case PHONE_IN:
								case WALK_IN:
									String toGoFeedBack = "";
									if (gui.cart.getToGoInfo().getCustomerName().isEmpty()) {
										toGoFeedBack = "Enter name.";
									} else {
										toGoFeedBack = "Verify name.";
									}
									feedBackString += toGoFeedBack;
									break;
								case DINE_IN:
									break;
								default:
									break;
								}
								gui.setPromptFeedback(feedBackString);
							} else {
								logger.fine("Found CustomerProfile: " + customerProfile);
								gui.cart.getToGoInfo().populate(customerProfile);
								gui.cart.getDeliveryInfo().populate(customerProfile);
								gui.updateEstimatedTime(0);
								gui.buildOrderTable();
								gui.historyFiltered(phoneNumber);

								String name = gui.cart.getToGoInfo().getCustomerName().isEmpty()
										? "<Missing Name>"
										: gui.cart.getToGoInfo().getCustomerName();

								switch (orderType) {
								case DELIVERY:
									replaceText = '`' + name + "," + gui.cart.getDeliveryInfo().getAddressNoSpace();
									gui.setPromptFeedback(
											"Verify/update customer name and address for delivery, then Enter.");
									break;

								case PHONE_IN:
								case WALK_IN:
									replaceText = '`' + name;
									gui.setPromptFeedback(
											"Verify name with customer then hit Enter, or input a new name to update.");
									break;
								case DINE_IN:
									break;
								default:
									break;
								}
							}
						} catch (RemoteException | SQLException e) {
							logger.warning("findCustomer RemoteException. " + e);
						}
					}
				}
			} else {
				// name input
				StringTokenizer attr = new StringTokenizer(s, String.valueOf(SharedConstDefs.DELIMITER));
				if (attr.hasMoreTokens() == false) {
					return;
				}

				String tokenString = attr.nextToken();

				// deduce to dine-in where the input 'looks like' a table number
				if (((len > 0 && Character.isDigit(s.charAt(0))) || (len > 1 && Character.isDigit(s.charAt(1)))
						|| (len > 2 && Character.isDigit(s.charAt(2)))) && len < 7
						&& gui.cart.getToGoInfo().getPhoneNumber().isEmpty()) {
					gui.cart.getOrder().setType(OrderType.DINE_IN);
				}

				switch (gui.cart.getOrder().getType()) {
				case DINE_IN:
					String tableNumber = tokenString.replace(SharedConstDefs.DELIMITER, ' ').trim();
					if (tableNumber.length() > DineInInfoTable.TABLE_NUMBER_SIZE) {
						tableNumber = tableNumber.substring(0, DineInInfoTable.TABLE_NUMBER_SIZE - 1);
					}

					if (Configuration.getTableNumbers().keySet().contains(tableNumber) == false) {
						String tableNumberUpperCase = tableNumber.toUpperCase();
						if (Configuration.getTableNumbers().keySet().contains(tableNumberUpperCase)) {
							tableNumber = tableNumberUpperCase;
						} else {
							gui.setInfoFeedback(
									"'" + tableNumber + "' is not a pre-set Dine-in table number. Allow using it.");
						}
					}

					gui.cart.getDineInInfo().setTableNumber(tableNumber);

					if (attr.hasMoreTokens()) {
						tokenString = attr.nextToken();
						try {
							int guestCount = Integer
									.parseInt(tokenString.replace(SharedConstDefs.DELIMITER, ' ').trim());
							if (guestCount < 0) { // allow 0 so it can be set it back to the default value
								throw new Exception("Invalid number of guests entered");
							}
							gui.cart.getDineInInfo().setGuestCount(guestCount);
						} catch (Exception e) {
							gui.setWarningFeedback("'" + tokenString
									+ "' is an incorrect input for number of dine-in guest. Please enter a postive number.");
							return;
						}
					}
					break;
				default:
					String name = tokenString.replace(SharedConstDefs.DELIMITER, ' ').trim();
					if (name.length() > ToGoInfoTable.CUSTOMER_NAME_SIZE) {
						name = name.substring(0, ToGoInfoTable.CUSTOMER_NAME_SIZE - 1);
						gui.setWarningFeedback("Customer name has been truncated to '" + name + "'");
					} else {
						gui.clearFeedbackText();
					}

					replaceText = "";
					gui.cart.getToGoInfo().setCustomerName(name);
					if (attr.hasMoreTokens()) {
						String street = attr.nextToken().replace(SharedConstDefs.DELIMITER, ' ').trim();
						if (street.length() > DeliveryInfoTable.STREET_SIZE) {
							street = street.substring(0, DeliveryInfoTable.STREET_SIZE - 1);
							gui.setWarningFeedback("Street name has been truncated to '" + street + "'");
						}
						gui.cart.getDeliveryInfo().setStreet(street);
					}

					if (attr.hasMoreTokens()) {
						String city = attr.nextToken().replace(SharedConstDefs.DELIMITER, ' ').trim();
						if (city.length() > DeliveryInfoTable.CITY_SIZE) {
							city = city.substring(0, DeliveryInfoTable.CITY_SIZE - 1);
							gui.setWarningFeedback("City name has been truncated to '" + city + "'");
						}
						gui.cart.getDeliveryInfo().setCity(city);
					}

					if (attr.hasMoreTokens()) {
						String state = attr.nextToken().replace(SharedConstDefs.DELIMITER, ' ').trim();
						if (state.length() > DeliveryInfoTable.STATE_SIZE) {
							state = state.substring(0, DeliveryInfoTable.STATE_SIZE - 1);
							gui.setWarningFeedback("State name has been truncated to '" + state + "'");
						}
						gui.cart.getDeliveryInfo().setState(state);
					}

					if (attr.hasMoreTokens()) {
						String zip = attr.nextToken().replace(SharedConstDefs.DELIMITER, ' ').trim();
						if (zip.length() > DeliveryInfoTable.ZIP_SIZE) {
							zip = zip.substring(0, DeliveryInfoTable.ZIP_SIZE - 1);
							gui.setWarningFeedback("Zip code has been truncated to '" + zip + "'");
						}
						gui.cart.getDeliveryInfo().setZip(zip);
					}
					break;
				}
			}

			gui.setInputText(replaceText);
			gui.buildOrderTable();
		} // if Enter key was pressed

		for (int column = 0; column < gui.dishTableView.getColumnCount(); ++column) {
			if (gui.dishTableView.getColumnName(column).equals(FeConstDefs.DISHVIEW_NAME)) {
				gui.dishTableView.setColumnSelectionInterval(column, column);
				break;
			}
		}
	}

	private void processOpenCurly() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '{'
		if (s.isEmpty()) {
			return;
		}

		if ((releasedKeyCode == KeyEvent.VK_ENTER) || s.charAt(s.length() - 1) == '}') {
			s = s.replaceAll("}", "").trim();

			if (s.equalsIgnoreCase("summary")) {
				Client.printDefaultHandler.printSummary(gui, gui.getUnsettledCarts());
				gui.prepareForNextOrder();
			} else if (s.equalsIgnoreCase("detail")) {
				Client.printDefaultHandler.printDetail(gui, gui.getUnsettledCarts());
				gui.prepareForNextOrder();
			} else if (s.equalsIgnoreCase("report")) {
				Client.printDefaultHandler.printReport(gui, gui.getUnsettledCarts());
				gui.prepareForNextOrder();
			} else if (s.equalsIgnoreCase("SHUTDOWN")) {
				gui.shutdown();
			} else if (s.equalsIgnoreCase("close")) {
				gui.close();
			}
		}
	}

	private void processDoubleQuote() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '"'
		if (s.isEmpty()) {
			if (gui.cart.getToGoInfo().getPhoneNumber().isEmpty() == false) {
				String customerNote = gui.cart.getToGoInfo().getNote();
				if (customerNote.isEmpty()) {
					gui.setPromptFeedback("Enter a customer note for this order. It's retained for this customer.");
				} else {
					gui.setInputText('"' + customerNote);
					gui.setPromptFeedback("Customer note on record. Update or Enter to accept.");
				}
			}
			return;
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER || s.charAt(s.length() - 1) == '"') {
			s = s.replaceAll("\"", "").trim();
			if (s.length() > ToGoInfoTable.NOTE_SIZE) {
				s = s.substring(0, ToGoInfoTable.NOTE_SIZE - 1);
				gui.setWarningFeedback("The customer note has been truncated to '" + s + "'");
			}

			gui.cart.getToGoInfo().setNote(s);
			gui.buildOrderTable();
			gui.clearInputText();
		}
	}

	private void processSingleQuote() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop "'"
		if (s.isEmpty()) {
			String orderNote = gui.cart.getOrder().getNote();
			if (orderNote.isEmpty()) {
				gui.setPromptFeedback("Enter an order note for this order");
			} else {
				gui.setInputText('"' + orderNote);
				gui.setPromptFeedback("Previously entered order note. Update or Enter to accept.");
			}

			return;
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER || s.charAt(s.length() - 1) == '\'') {
			s = s.replaceAll("'", "").trim();
			if (s.length() > OrderTable.NOTE_SIZE) {
				s = s.substring(0, OrderTable.NOTE_SIZE - 1);
				gui.setWarningFeedback("The order note has been truncated to '" + s + "'");
			}

			gui.cart.getOrder().setNote(s);
			gui.buildOrderTable();
			gui.clearInputText();
		}
	}

	private void processQuestionMark() {
		logger.finest("Entered");
		gui.showHelp();
		gui.clearInputText();
	}

	private void processPoundSign() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '#'
		if (s.isEmpty()) {
			gui.setPromptFeedback("Enter credit card number");
			return;
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER || s.charAt(s.length() - 1) == '#') {
			s = s.replaceAll("#", "").trim();
			if (s.length() > ToGoInfoTable.CREDIT_CARD_NUMBER_SIZE) {
				gui.setWarningFeedback("Credit Card number cannot exceed " + ToGoInfoTable.CREDIT_CARD_NUMBER_SIZE);
			} else {
				gui.cart.getToGoInfo().setCreditCardNumber(s);
				gui.buildOrderTable();
				gui.clearInputText();
			}
		}
	}

	private void processOpenSquareBracket() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '['
		if (s.isEmpty()) {
			gui.setPromptFeedback("Enter dish customizer that is not on the menu selection");
			return;
		}

		if (gui.isOrderTableSelectedRowAnOrderItem() == false) {
			gui.setWarningFeedback("Select a dish to add the customizer to");
			return;
		}

		gui.abbreviationFiltered(s.toUpperCase());
		if (gui.dishTableRows.size() > 0) {
			// block non-customizer dish to be used as customizer
			int categroy = Client.dishes[gui.targetDishId].getCategory();
			if (categroy != Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.customizer)) {
				return;
			}
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER || s.charAt(s.length() - 1) == ']') {
			s = s.replaceAll("]", "").trim();
			if (s.isEmpty() == false) {
				gui.targetDishId = gui.addCustomizerToOrderTableSelectedOrderItem(s);
				logger.fine("Added customizer for:" + Client.dishes[gui.targetDishId]);
				gui.buildOrderTable();
				gui.showFullMenu(0, 0);
			}
			gui.clearInputText();
		}
	}

	private void processMinusSign() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '-'
		if (s.isEmpty()) {
			gui.setPromptFeedback("Enter discount amount (in penny or in percent follow by %)");
			return;
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER || s.charAt(s.length() - 1) == '%') {
			if (s.endsWith("%")) { // percent off
				int percentOff = Util.parseInt(s.substring(0, s.length() - 1));
				if (percentOff >= 0 && percentOff <= 100) {
					gui.cart.getOrder().setDiscountPercent(percentOff);
					int balanceDue = gui.calculator.getBalanceDue();
					if (balanceDue < 0) {
						gui.setWarningFeedback("Cannot offer percent discount resulting in negative balance due");
					} else {
						gui.buildOrderTable();
						gui.clearInputAndFeedback();
					}
				} else {
					gui.setWarningFeedback("Invalid Percent off input, e.g.: -10%");
				}
			} else { // cent/penny off
				int centOff = Util.parseInt(s);
				if (centOff >= 0) {
					gui.cart.getOrder().setDiscountAmount(centOff);
					int balanceDue = gui.calculator.getBalanceDue();
					if (balanceDue < 0) {
						gui.setWarningFeedback("Cannot offer discount resulting in negative balance due");
					} else {
						gui.buildOrderTable();
						gui.clearInputAndFeedback();
					}
				} else {
					gui.setWarningFeedback("Invalid amount off input, e.g.: -500 for $5");
				}
			}
		}
	}

	private void processPlusSign() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '+'
		if (s.isEmpty()) {
			gui.setPromptFeedback("Enter miscellaneous charge amount (in penny)");
			return;
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER) {
			int miscCharge = Util.parseInt(s);
			if (miscCharge >= 0) {
				gui.cart.getOrder().setAdditionalAmount(miscCharge);
				gui.buildOrderTable();
				gui.clearInputAndFeedback();
			} else {
				gui.setWarningFeedback("Invalid miscellaneous charge input, e.g.: +500 for $5");
			}
		}
	}

	private void processEqualSign() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '='
		s = s.replaceAll("\\s+", ""); // remove all white spaces
		int len = s.length();

		if (s.isEmpty()) {
			gui.setPromptFeedback(OrderStatus.VOIDED.getShortHandChar() + " to void order, or order type: "
					+ String.join(", ", OrderType.getStringMnemonicsAndDisplayName()));
			return;
		}

		if (len == 1) {
			OrderType orderType = OrderType.getEnumByMnemonic(Character.toUpperCase(s.trim().charAt(0)));
			if (orderType != null) {
				if (orderType == gui.cart.getOrder().getType()) {
					if (orderType == OrderType.DELIVERY && gui.cart.getDeliveryInfo().getDeliveryCharge() == 0) {
						gui.setPromptFeedback(
								"Enter delivery zone: [1 to " + Configuration.getDeliveryZoneCount() + "]");
					} else {
						gui.setPromptFeedback("It is already set to " + orderType.getDisplayName() + " order");
					}
				} else {
					gui.setPromptFeedback("Hit Enter to change to " + orderType.getDisplayName() + " order");
				}
			}
		}

		if (len > 0 && releasedKeyCode == KeyEvent.VK_ENTER) {
			boolean clearInputText = true;
			char type = s.charAt(0);
			type = Character.toUpperCase(type); // Internal logic uses upper
			if (type == OrderStatus.VOIDED.getShortHandChar()) {
				if (gui.voidOrder() == false) {
					// set warning is already done in voidOrder()
					clearInputText = false;
				}
			} else {
				OrderType orderType = OrderType.getEnumByMnemonic(type);
				if (orderType == null) {
					clearInputText = false;
					gui.setWarningFeedback("Invalid Order Type, Valid types: "
							+ String.join(", ", OrderType.getStringMnemonicsAndDisplayName()));
				} else {
					gui.setPromptFeedback(orderType.getDisplayName() + ":");
				}

				switch (orderType) {
				case PHONE_IN:
					gui.phoneInRadioButton.doClick();
					break;
				case DELIVERY:
					gui.deliveryRadioButton.doClick();
					String dDroppedS = s.substring(1); // drop D
					String deliveryZoneString = "";
					if (dDroppedS.matches("\\d+")) {
						deliveryZoneString = dDroppedS;
					}
					logger.fine("deliveryZoneString=" + deliveryZoneString);

					clearInputText = handleDeliveryZone(deliveryZoneString);
					gui.buildOrderTable();
					break;
				case WALK_IN:
					gui.walkInRadioButton.doClick();
					break;
				case DINE_IN:
					gui.dineInRadioButton.doClick();
					break;
				default:
					clearInputText = false;
					gui.setWarningFeedback(
							"Invalid Order Type, Valid type are: " + String.join(", ", OrderType.getStringMnemonics()));
					break;
				}

				if (clearInputText) {
					gui.clearInputText();
				}
			}
		}
	}

	private void processSlash() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '/'
		if (s.isEmpty()) {
			gui.setPromptFeedback("Search: 1-3 digits: Ord#; 4-10: phone end digit; Or by name");
			return;
		}

		if (releasedKeyCode == KeyEvent.VK_ENTER) {
			List<Cart> carts = gui.findCarts(s);
			if (carts == null || carts.size() == 0) {
				gui.setWarningFeedback("No order found with search criteria='" + s + "'");
				gui.setInputText("/" + s);
				return;
			}

			if (gui.cart.isNew() && gui.isCartEmpty() == false && JOptionPane.showConfirmDialog(gui,
					"Clear This New Order?", "Confirm Message", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				gui.clearFeedbackText();
				gui.clearInputText();
				return;
			}

			if (carts.size() == 1) {
				gui.cart = carts.get(0);
				logger.fine("Found " + gui.cart);
				gui.calculator = new Calculator(gui.cart, Client.menu);
				gui.buildOrderTable();
				gui.clearInputAndFeedback();
			} else {
				gui.setWarningFeedback(
						"Multiple matches, refine search or select an order from business table below.r");
				gui.setInputText("/" + s);
				gui.buildBusinessTable(carts, Color.GREEN, false);
			}
		}
	}

	private void processDollarSign() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '$'
		s = s.replaceAll("\\s+", ""); // remove all white spaces

		int balanceDue = gui.calculator.getTotal() - gui.cart.getPaymentTray().getTotalTendered();
		if (s.isEmpty()) {
			if (balanceDue <= 0) {
				gui.setPromptFeedback("This order has no outstanding balance");
			} else {
				gui.setPromptFeedback(
						"Enter payment(in penny amount): " + PaymentType.getStringMnemonicsAndDisplayName());
			}
			return;
		}

		if (gui.cart.getOrder().getType() == OrderType.DELIVERY) {
			int minimumSubtotalForDeliveryInCent = Configuration.getMinimumSubtotalForDeliveryInCent();
			if (minimumSubtotalForDeliveryInCent != 0 && gui.calculator.getSubtotal() < minimumSubtotalForDeliveryInCent) {
				gui.setWarningFeedback("Subtotal must be at least "
					+ String.format("$%4.2f", minimumSubtotalForDeliveryInCent / 100.0) + " for delivery");
				return;
			}
			
		}

		int len = s.length();
		if (len == 1) {
			// removing all whitespace puts the payment char at the second character
			char paymentTypeChar = Character.toUpperCase(s.charAt(0));
			PaymentType paymentType = PaymentType.getEnum(paymentTypeChar);
			if (paymentType == null) {
				gui.setWarningFeedback(paymentTypeChar + " is an invalid payment method");
				return;
			}

			switch (paymentType) {
			case CASH:
				gui.setPromptFeedback(" " + paymentType.getFullName() + ": $");
				break;
			case CREDIT_CARD:
			case CHECK:
				gui.setPromptFeedback("Tab for auto complete " + paymentType.getFullName() + ": "
						+ String.format("$%4.2f", balanceDue / 100.0)); 
				if (releasedKeyCode == KeyEvent.VK_TAB) {
					gui.setInputText("$" + paymentType.getShortHandChar() + balanceDue);
					return;
				}

				break;
			default:
				gui.setWarningFeedback(paymentType + " payment method is not used");
				return;
			}
		}

		if (s.indexOf(SharedConstDefs.DELIMITER) > 1) {
			gui.setPromptFeedback("Enter tips amount");
		}

		if (len > 1 && (releasedKeyCode == KeyEvent.VK_ENTER)) {
			char paymentTypeChar = Character.toUpperCase(s.charAt(0));

			int tendered = 0;
			int tips = 0;
			int delimiterIndex = s.indexOf(SharedConstDefs.DELIMITER);
			String tenderedPart = s.substring(1).trim();
			String tipsPart = "0";
			if (delimiterIndex != -1) {
				tenderedPart = s.substring(1, delimiterIndex).trim();
				tipsPart = s.substring(delimiterIndex + 1).trim();

				try {
					tips = Integer.parseInt(tipsPart);
				} catch (NumberFormatException e) {
					gui.setWarningFeedback("Invalid number format input for the tips amount");
					return;
				}
			}

			try {
				tendered = Integer.parseInt(tenderedPart);
			} catch (NumberFormatException e) {
				gui.setWarningFeedback("Invalid number format input for the tendered amount");
				return;
			}

			if (tendered == 0) {
				gui.setWarningFeedback("$0.00 is an invalid tendered amount");
				return;
			}

			if (balanceDue <= 0) {
				gui.setWarningFeedback("This order has no outstanding balance");
				return;
			}

			Payment payment = new Payment();
			payment.setAmount(tendered);
			payment.setTip(tips);
			PaymentType paymentType = PaymentType.getEnum(paymentTypeChar);
			if (paymentType == null) {
				gui.setWarningFeedback(paymentTypeChar + " is an invalid payment method.");
				return;
			}
			switch (paymentType) {
			case CASH:
				if (tendered < balanceDue) {
					gui.setWarningFeedback("Allow only ONE CASH payment, Please tender "
						+ String.format("$%4.2f", balanceDue / 100.0) + " or More");
					return;
				} else if (tendered > 5 * balanceDue) {
					if (JOptionPane.showConfirmDialog(gui,
							"Are you sure you want to Tender " +  String.format("$%4.2f", tendered / 100.0) + " in "
									+ paymentType.getFullName() + "? It's > 5x the balance due of "
									+ String.format("$%4.2f", balanceDue / 100.0),
							"Confirm Message", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return;
					}
				}

				if (tendered <= balanceDue) {
					gui.cart.getOrder().setCashPaidAmount(tendered);
				} else {
					gui.cart.getOrder().setCashPaidAmount(balanceDue);
				}

				break;
			case CREDIT_CARD:
			case CHECK:
				if (tendered > balanceDue) {
					gui.setWarningFeedback("Cannot Tender more than " + String.format("$%4.2f", balanceDue / 100.0)
							+ " in " + paymentType.getFullName());
					return;
				} else if (tendered < balanceDue) {
					if (JOptionPane.showConfirmDialog(gui,
							"Are you sure you want to Tender " + String.format("$%4.2f", tendered / 100.0)+ " on "
									+ paymentType.getFullName() + "? (It's not enough for the balance due of "
									+ String.format("$%4.2f", balanceDue / 100.0) + ".)",
							"Confirm Message", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return;
					}
				}
				break;
			default:
				gui.setWarningFeedback(paymentType + " payment method is not used");
				return;
			}
			payment.setType(paymentType);
			gui.cart.getPaymentTray().addPayment(payment);
			gui.cart.getOrder().setCashierId(Client.authenticatedEmployee.getEmployeeId());

			if (gui.calculator.isBalanceDue()) {
				gui.setPromptFeedback(
						"Please tender: "
						+ String.format("$%4.2f", (gui.calculator.getTotal() - gui.cart.getPaymentTray().getTotalTendered()) / 100.0)
						+ " More");
				gui.clearInputText();
				gui.buildOrderTable();
				if (JOptionPane.showConfirmDialog(gui, "(Alt+Y)   to Open The Cash Drawer?", "Confirm Message",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					try {
						Client.printDefaultHandler.openDrawer();
					} catch (Exception e) {
						logger.warning("Opening draw failed. error: " + e.getMessage());
						gui.setWarningFeedback("Opening draw failed. Check printer and try again...");
					}
				}
			} else {
				gui.cart.getOrder().setStatus(OrderStatus.PAID);
				gui.cart.getOrder().setPaidTime(gui.getServerTimestamp());
				OrderType orderType = gui.cart.getOrder().getType();
				if (gui.cart.isNew()) {
					if (orderType == OrderType.PHONE_IN) {
						// new order and paid, set to walk-in and mark waiting.
						gui.cart.getOrder().setType(OrderType.WALK_IN);
						gui.cart.handleCustomerArrival(gui.getServerTimestamp());
					}

					if (gui.sendCartToServer() == false) {
						String previousText = gui.inputFeedBack.getText();
						if (previousText.isEmpty()) {
							gui.setWarningFeedback("Order commit to kitchen failed, try again...");
						}
						return;
					}

					// Use a pop up showing the change amount before clearing things, only cash
					// payment might need to return change to customer
					if (gui.cart.getPaymentTray().getPaymentCount(PaymentType.CASH) > 0) {
						int totalTendered = gui.cart.getPaymentTray().getTotalTendered();
						int totalCashTendered = gui.cart.getPaymentTray().getTotalTendered(PaymentType.CASH);
						String changeAmount = String.format("$%4.2f",(totalTendered - gui.calculator.getTotal() / 100.0));
						JOptionPane.showMessageDialog(gui,
								"Total Tendered: " + String.format("$%4.2f", totalTendered / 100.0) + ", Cash Tendered: "
										+ String.format("$%4.2f", totalCashTendered / 100.0) + ", Change: " + changeAmount,
								"Confirm Message", JOptionPane.OK_OPTION);
					}

					gui.clearInputText();
					gui.buildOrderTable();

					if (orderType == OrderType.PHONE_IN || orderType == OrderType.WALK_IN) {
						PrintWaitingNoticeHandler printWaitingNoticeHandler = new PrintWaitingNoticeHandler(
								ConfigKeyDefs.defaultStation, Client.defaultPrintService);
						printWaitingNoticeHandler.print(gui, gui.cart, gui.calculator);
					}

					try {
						Client.printDefaultHandler.openDrawer();
					} catch (Exception e) {
						logger.warning("Opening draw failed. error: " + e.getMessage());
						gui.setWarningFeedback("Open drawer failed. Check printer and try again...");
					}

					try {
						gui.handleConfiguredEvent(ConfigKeyDefs.paidPrintOrderToKitchen);
					} catch (Exception e) {
						logger.warning(ConfigKeyDefs.paidPrintOrderToKitchen + " failed. " + e.getMessage());
						gui.setWarningFeedback(
								ConfigKeyDefs.paidPrintOrderToKitchen + " failed.  Check printer and try again...");
						e.printStackTrace();
					}
				} else {
					if (gui.sendCartToServer() == false) {
						gui.setWarningFeedback("Send order to kitchen failed, try again...");
						return;
					}
					gui.clearInputText();
					gui.buildOrderTable();
					try {
						Client.printDefaultHandler.openDrawer();
					} catch (Exception e) {
						logger.warning("Opening draw failed. error: " + e.getMessage());
						gui.setWarningFeedback("Open drawer failed. Check printer and try again...");
					}
					// option to Notify the Kitchen when paid on the Pickup or Waiting Orders.
					switch (orderType) {
					case PHONE_IN:
					case WALK_IN:
						if (JOptionPane.showConfirmDialog(gui, "    Notify Kitchen ?", "Confirm Message",
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							Map<String, Map<String, String>> toGoPrinterLocationToPrinter = Configuration
									.getToGoPrinterLocationToPrinter();
							Map<String, String> packingStationPrinterAttributes = toGoPrinterLocationToPrinter
									.get(ConfigKeyDefs.packingStation);
							String packingStationPrinterName = packingStationPrinterAttributes
									.get(ConfigKeyDefs.printerLocationToPrinter_name);
							PrintService pakcingStationPrintService = AvailablePrintService
									.getPrintService(packingStationPrinterName);
							PrintWaitingNoticeHandler printWaitingNoticeHandler = new PrintWaitingNoticeHandler(
									ConfigKeyDefs.packingStation, pakcingStationPrintService);
							printWaitingNoticeHandler.print(gui, gui.cart, gui.calculator);
						}
						break;
					case DELIVERY:
					case DINE_IN:
						// No option needed to Notify the Kitchen when paid on the DELIVERY or Dining
						// Orders.
						// But hold up so user can see the change panel
						JOptionPane.showMessageDialog(gui, "     Click OK to clear", "Confirm Message",
								JOptionPane.OK_OPTION);
						break;
					default:
						throw new RuntimeException("Unhandled orderType: " + orderType);
					}
				}
				gui.prepareForNextOrder();
			}
		}
	}

	private void processAtSign() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '@'
		int commaIndex = s.indexOf(',');
		if (commaIndex > 0) {
			s = s.substring(0, s.indexOf(',')); // chop after comma to prevent confusion of entering city, state, zip
			gui.setWarningFeedback("Enter house number and street name only. Truncated comma and after.");
			gui.inputTextField.setText('@' + s);
		}

		gui.showStreetNameStartsWithIgnoreLeadingHouseNumber(s);

		if (s.isEmpty()) {
			String promptingStreet;
			String deliveryInfoStreet = gui.cart.getDeliveryInfo().getStreet();
			if (deliveryInfoStreet.isEmpty()) {
				promptingStreet = gui.getDishViewHighlightedColumnValue(0, FeConstDefs.DISHVIEW_NAME);
				gui.setPromptFeedback("Enter house number, type street name, double click or tab to select");
			} else {
				promptingStreet = deliveryInfoStreet;
				gui.setPromptFeedback("Tab to fill '" + deliveryInfoStreet + "' for edit; Or enter/paste new value.");
			}
			if (releasedKeyCode == KeyEvent.VK_TAB) {
				gui.setInputText("@" + promptingStreet);
			}
			return;
		}

		if (releasedKeyCode == KeyEvent.VK_TAB) {
			gui.addSelectedStreetToInputText(0);
			return;
		} else if (releasedKeyCode == KeyEvent.VK_ENTER) {
			if (s.length() > DeliveryInfoTable.STREET_SIZE) {
				gui.setWarningFeedback("House number plus street name cannot exceed " + DeliveryInfoTable.STREET_SIZE);
			} else {
				gui.cart.getDeliveryInfo().setStreet(s);
				gui.buildOrderTable();
				gui.clearInputText();
			}
		}
	}

	private void processGreaterThan() {
		logger.finest("Entered");
		String s = gui.inputTextField.getText().substring(1).trim(); // drop '>'
		s = s.replaceAll("\\s+", ""); // remove all white spaces

		int len = s.length();
		if (len == 0) {
			gui.setPromptFeedback("Enter D for driver; S for server; then two-letter initials.");
			return;
		} else {
			Character roleTypeChar = Character.toUpperCase(s.charAt(0));
			RoleType roleType = RoleType.getEnum(roleTypeChar);
			if (roleType == null) {
				gui.setWarningFeedback(roleTypeChar + " is an invalid input");
				return;
			}

			if (len == 1) {
				switch (roleType) {
				case DRIVER:
				case SERVER:
					gui.setPromptFeedback("Input for " + roleType.getDisplayName());
					return;
				default:
					gui.setWarningFeedback(roleType + " is not used");
					return;
				}
			} else if (len > 2 && releasedKeyCode == KeyEvent.VK_ENTER) {
				String twoLetterInitials = s.substring(1, 3);
				Employee employee = gui.getEmployee(twoLetterInitials);
				logger.fine("driverTwoLetterInitials=" + twoLetterInitials + " employee=" + employee);
				if (employee == null) {
					gui.setWarningFeedback("Employee initials '" + twoLetterInitials + "' is not a valid employee");
					return;
				}

				int employeeId = gui.getEmployee(twoLetterInitials).getEmployeeId();
				switch (roleType) {
				case DRIVER:
					gui.cart.getDeliveryInfo().setDriverId(employeeId);
					break;
				case SERVER:
					gui.cart.getDineInInfo().setServerId(employeeId);
					break;
				default:
					gui.setWarningFeedback(roleType + " is not used");
					return;
				}
				gui.buildOrderTable();
				gui.clearInputText();
			}
		}
	}

	private boolean handleDeliveryZone(String deliveryZoneString) {
		logger.finest("Entered");
		boolean clearInputText = true;
		if (deliveryZoneString.matches("\\d+")) { // regex match string that's all digits
			int deliveryZoneIndex = Integer.parseInt(deliveryZoneString);
			// There's built-in default values at the getDeliveryCostAt();
			int userSpecifiedDeliveryCharge = Configuration.getDeliveryCostAt(deliveryZoneIndex);
			int inCartDeliveryCharge = gui.cart.getDeliveryInfo().getDeliveryCharge();
			if (userSpecifiedDeliveryCharge != inCartDeliveryCharge) {
				if (inCartDeliveryCharge == 0) {
					gui.cart.getDeliveryInfo().setDeliveryCharge(userSpecifiedDeliveryCharge);
				} else {
					if (JOptionPane.showConfirmDialog(gui,
							"Delivery Charge of " + String.format("$%4.2f", inCartDeliveryCharge / 100.0)
									+ " is already set for this order. Do you want to override it?",
							"Confirm Message", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						gui.cart.getDeliveryInfo().setDeliveryCharge(userSpecifiedDeliveryCharge);
					}
				}
			}
		} else {
			if (gui.cart.getDeliveryInfo().getDeliveryCharge() == 0
					&& gui.cart.getToGoInfo().getPhoneNumber().isEmpty() == false) {
				clearInputText = false;
				gui.setInputText("=d1");
				gui.setWarningFeedback(
						"Delivery charge is not set. Modify suggested (and update customer profile for future use).");
				// Could do a pop-up here to remind update customer profile.
			}
		}

		return clearInputText;
	}
}
