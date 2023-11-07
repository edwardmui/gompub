package com.orderfoodnow.pos.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.orderfoodnow.pos.backend.Server;
import com.orderfoodnow.pos.backend.ServerInterface;
import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.CouponVariantType;
import com.orderfoodnow.pos.shared.CustomerStatus;
import com.orderfoodnow.pos.shared.DishSize;
import com.orderfoodnow.pos.shared.NotificationMessage;
import com.orderfoodnow.pos.shared.OrderItemStatus;
import com.orderfoodnow.pos.shared.OrderItemTray;
import com.orderfoodnow.pos.shared.OrderStatus;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PaymentType;
import com.orderfoodnow.pos.shared.SharedConstDefs;
import com.orderfoodnow.pos.shared.Street;
import com.orderfoodnow.pos.shared.SubOrderItemStatus;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.CouponDish;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.Menu;
import com.orderfoodnow.pos.shared.menu.Subdish;
import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.CustomizerTable;
import com.orderfoodnow.pos.shared.order.DeliveryInfo;
import com.orderfoodnow.pos.shared.order.DineInInfo;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.order.Payment;
import com.orderfoodnow.pos.shared.order.ToGoInfo;
import com.orderfoodnow.pos.shared.staff.Employee;

public class Client extends JPanel implements ActionListener {
	static Socket serverSocket;
	static boolean running = true;
	static Dish[] dishes; // dish array contains all the dish from the menu
	static List<Dish> sortedByCodeActiveDishes;
	static PrintDefaultHandler printDefaultHandler;
	static PrintService defaultPrintService;
	static JFrame mainFrame; // main frame of the client GUI
	static ServerInterface server; // connection to the server
	static Map<Integer, Employee> idToEmployee; // map of empoyeeId to current or past employees
	static Map<String, Employee> initialsToActiveEmployee; // map of employee initials to an active employee;
	static Employee authenticatedEmployee; // the employee whose login on to this client GUI.
	static Menu menu; // menu from the server that is cached at the client
	static final int customizerCategory = Configuration.getDishCategoryNameToIntegerValue()
			.get(ConfigKeyDefs.customizer);
	static int exchangeCondimentCategory = Configuration.getDishCategoryNameToIntegerValue()
			.get(ConfigKeyDefs.exchangeCondiment);
	static int allNoIngredientCategory = Configuration.getDishCategoryNameToIntegerValue()
			.get(ConfigKeyDefs.allNoIngredient);

	Cart cart; // shopping cart as a workspace while the order is been entered.
	Calculator calculator; // calculating order total based on menu and cart content
	int targetDishId; // this dishId been worked on
	List<List<String>> dishTableRows; // a dish table row, it is a List of List
	JTable dishTableView;
	JTable orderTableView;
	JTable businessTableView;
	JTable arrivalTableView;
	JTextField inputTextField;
	JRadioButton phoneInRadioButton;
	JRadioButton deliveryRadioButton;
	JRadioButton walkInRadioButton;
	JRadioButton dineInRadioButton;

	private static final String PAYMENT_LABEL = "Pymt "; // Used for displaying, then read-in for parsing
	private static final long serialVersionUID = 1L;
	private static Map<String, Runnable> nonPrintingActionToRunable = new HashMap<>(); // nonPrinting to functions
	private List<List<String>> orderTableRows; // an order table row
	private List<List<String>> businessTableRows; // a business table row, it is a List of List
	private List<List<String>> arrivalTableRows; // a customer arrived to picked order, it is a List of List
	private List<Cart> orderHistory = new ArrayList<>();
	private DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	JLabel inputFeedBack;
	private Rectangle visibleRectangle = new Rectangle(0, 0, 0, 0);
	private JButton cashButton;
	private JButton creditCardButton;
	private JButton checkButton;
	private JPanel eastPanel;

	private static String loggingConfigFile;
	private static Logger logger;
	static {
		// if the logging properties file is not provided in the command line as a VM
		// argument as follow, then use the one in the resources folder
		// -Djava.util.logging.config.file="src/test/resources/conf/clientLogging.properties"
		loggingConfigFile = System.getProperty("java.util.logging.config.file");
		if (loggingConfigFile == null) {
			loggingConfigFile = Client.class.getClassLoader().getResource("conf/clientLogging.properties").getFile();
			try {
				LogManager.getLogManager().readConfiguration(new FileInputStream(loggingConfigFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger = Logger.getLogger(Client.class.getName());
	}

	private Client() throws Exception {
		logger.finest("Entered");
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create Main Frame and it's title
		mainFrame = new JFrame(Configuration.getRestaurantName() + " " + Configuration.getRestaurantChineseName() + " "
				+ Configuration.getRestaurantPhone());

		// Do nothing, instead, add custom handling pop up confirmation before exiting
		// See client.addClientClosingListener(client)
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.magenta), // main frame color
				BorderFactory.createEmptyBorder(5, 5, 5, 5))); // main frame border

		// client start up window size
		int width;
		int height;
		String windowSize = Configuration.getLocalClientWindowResolution(); // default is VGA
		if (windowSize == null || windowSize.isEmpty()) { // 780 x 540 for 800 x 640
			width = 780;
			height = 540;
		} else if (windowSize.equals("XVGA")) { // 1260 x 940 for 1280 x 960
			width = 1260;
			height = 940;
		} else if (windowSize.equals("SVGA")) { // 1004 x 708 for 1024 x 768
			width = 1004;
			height = 708;
		} else { // 780 x 540 for 800 x 640
			width = 780;
			height = 540;
		}
		setPreferredSize(new Dimension(width, height));

		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		showNorthBorder();
		showWestBorder();
		showSouthBorder();
		showCenterBorder();
		// The eastBorder is different than the others, the arrived table is part of it
		initArrivalTable();
		prepareForNextOrder();
		buildBusinessTable(getOutstandingCarts(), Color.blue, true);

		setOpaque(true); // content panes must be opaque
		mainFrame.setContentPane(this);

		// Display the window.
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	// ==============public
	@Override
	public void actionPerformed(ActionEvent e) {
		logger.finest("Entered");
		String actionCommand = e.getActionCommand();
		logger.finer("actionCommand=" + actionCommand);

		if (OrderType.getEnumNames().contains(actionCommand)) {
			OrderType orderType = OrderType.valueOf(actionCommand);
			setPromptFeedback(orderType.getDisplayName() + ":");
			cart.getOrder().setType(orderType);

			switch (orderType) {
			case WALK_IN:
				cart.handleCustomerArrival(getServerTimestamp());
				break;
			default:
				cart.resetCustomerArrival();
				break;
			}

			updateEstimatedTime(0);
			refreshTotalPanel();
			buildOrderTable();
		} else if (PaymentType.getEnumNames().contains(actionCommand)) {
			PaymentType paymentType = PaymentType.valueOf(actionCommand);
			int balanceDue = calculator.getBalanceDue();
			if (balanceDue > 0) {
				switch (paymentType) {
				case CASH:
					setInputText("$" + paymentType.getShortHandChar());
					setPromptFeedback(" " + paymentType.getFullName() + ": $");
					break;
				case CHECK:
				case CREDIT_CARD:
					setInputText("$" + paymentType.getShortHandChar() + balanceDue);
					setPromptFeedback(String.format("%s: $%4.2f",paymentType.getFullName(), balanceDue / 100.0));
					break;
				default:
					logger.warning("paymentType not handle: " + paymentType);
					break;
				}
			} else {
				setPromptFeedback("This order has no outstanding balance");
			}
		} else {
			logger.warning("Button not handle: " + actionCommand);
		}

		inputTextField.requestFocus();
	}

	// =========accessible by any class within the same java package as this class
	static Employee getEmployee(int employeeId) {
		// employeeId == 0 is default to no employee specified.
		if (employeeId == 0) {
			return null;
		}

		Employee employee = idToEmployee.get(employeeId);
		if (employee == null) {
			// null could happen if a newly added employee in the DB but server has not
			// restarted since. Make another request to the server to for a refresh
			logger.info("employeeId=" + employeeId);
			logger.info("idToEmployee=" + idToEmployee);

			try {
				idToEmployee = server.refreshIdToEmployee();
				return (idToEmployee.get(employeeId));
			} catch (RemoteException | SQLException e) {
				e.printStackTrace();
				logger.warning("Exception: " + e.getMessage() + ". employeeId=" + employeeId);
				return null;
			}
		}
		return employee;
	}

	void prepareForNextOrder() {
		logger.finest("Entered");
		cart = new Cart();
		if (authenticatedEmployee != null) {
			int employeeId = authenticatedEmployee.getEmployeeId();
			cart.getOrder().setTakenById(employeeId);
			cart.getDineInInfo().setServerId(employeeId);
		}
		calculator = new Calculator(cart, Client.menu);
		phoneInRadioButton.setSelected(true);
		orderTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		orderTableRows.clear();
		orderHistory.clear();
		updateEstimatedTime(0); // populate estimate time in the new order
		buildOrderTable();
		showFullMenu(0, 0); // to overwrite the orderHhistory on display
	}

	void handleConfiguredEvent(String configuredEvent) throws Exception {
		logger.finest("Entered");
		logger.finer("configuredEvent=" + configuredEvent);

		Map<String, List<Map<String, String>>> configuredEventToActionAttributes = Configuration
				.getConfiguredEventToActionAttributes();
		List<Map<String, String>> actionAttributes = configuredEventToActionAttributes.get(configuredEvent);
		logger.fine("configuredEvent=" + configuredEvent + " actionAttributes=" + actionAttributes);
		if (actionAttributes == null) {
			// All of the f keys, i.e f1-f12, altF1-altF12, shiftF1-ShiftF12 are defined,
			// There are custom configured function are defined in in conf.yaml
			throw new Exception("Undefined event for the configuredEvent: " + configuredEvent
					+ ". Check if it is defined in the config.yaml file.");
		}

		// going thru each map in the List<Map<String,String>>,
		// f2 example: [{action: clearOrder}, {action: refreshOrder}]
		for (Map<String, String> actionAttribute : actionAttributes) {
			String actionName = actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_action);
			logger.fine("actionName=" + actionName);
			if (Configuration.getNonPrintingActionOptions().contains(actionName)) {
				logger.finer("nonPrinting actionName=" + actionName);
				if (actionName.equals(ConfigKeyDefs.commitOrder)) {
					if (sendCartToServer() == false) {
						break;
					}
				} else if (actionName.equals(ConfigKeyDefs.stashOrder)) {
					if (stashCartToServer() == false) {
						break;
					}
				} else if (actionName.equals(ConfigKeyDefs.clearOrder)) {
					if (clearOrder() == false) {
						break;
					}
				} else if (actionName.equals(ConfigKeyDefs.showMap)) {
					if (showMap() == false) {
						break;
					}
				} else {
					nonPrintingActionToRunable.get(actionName).run();
				}
			} else {
				Map<String, Map<String, String>> toGoPrinterLocationToPrinter = Configuration
						.getToGoPrinterLocationToPrinter();
				String printerLocation = actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_location);
				PrintService printService = printerLocation == null ? defaultPrintService
						: AvailablePrintService.getPrintService(toGoPrinterLocationToPrinter.get(printerLocation)
								.get(ConfigKeyDefs.printerLocationToPrinter_name));

				if (Configuration.getPrintCouponActionOptions().contains(actionName)) {
					logger.fine("handling PrintCouponActionOptions=" + actionName);
					PrintCouponHandler printCouponHandler = new PrintCouponHandler(printerLocation, printService);
					printCouponHandler.print(CouponVariantType.getEnum(actionName), this);
				} else if (Configuration.getPermissionRequiredActionOptions().contains(actionName)) {
					logger.fine("handling getPermissionRequiredActionOptions: " + actionName);
					PermissionRequiredType permissionRequiredType = PermissionRequiredType.getEnum(actionName);
					switch (permissionRequiredType) {
					case PRINT_DETAIL:
					case PRINT_REPORT:
					case PRINT_SUMMARY:
						Client.printDefaultHandler.print(this, permissionRequiredType, getUnsettledCarts());
						break;
					case VOID_ORDER:
						voidOrder();
						break;
					case VOID_ORDER_ITEM:
					case VOID_CUSTOMIZER:
					case VOID_EXCHANGER:
						logger.fine(
								"voiding order item, modifer, or exchanger is done in the context of selecting a row in the order table.");
						// No function key
						break;
					case SHUTDOWN_SERVER:
						shutdown();
						break;
					case CLOSE:
						close();
						break;
					default:
						throw new RuntimeException("Unhandled permissionRequiredType=" + permissionRequiredType);
					}
				} else {
					if (isNewCartOrEmptyCartToWarn(actionName)) {
						continue;
					}
					if (Configuration.getPrintWaitingNoticeActionOptions().contains(actionName)) {
						logger.fine("handling PrintWaitingNoticeActionOptions=" + actionName);
						PrintWaitingNoticeHandler printWaitingNoticeHandler = new PrintWaitingNoticeHandler(
								printerLocation, printService);
						printWaitingNoticeHandler.print(this, cart, calculator);
						cart.handleCustomerArrival(server.getTimestamp());
					} else if (Configuration.getPrintOrderActionOptions().contains(actionName)) {
						logger.fine("handling PrintOrderActionOptions=" + actionName + " printerLocation="
								+ printerLocation);
						PrintOrderType printOrderType = PrintOrderType.getEnum(actionName);
						if (printOrderType == PrintOrderType.PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE) {
							if (cart.getOrder().getStatus() != OrderStatus.HOLDING) {
								logger.fine("Order is not in holding status. Skip PrintOrderActionOptions=" + actionName
										+ " printerLocation=" + printerLocation + " printOrderType=" + printOrderType);
								continue;
							}
						} else {
							if (cart.getOrder().getStatus() == OrderStatus.HOLDING) {
								logger.fine("Order is in holding status. Skip PrintOrderActionOptions=" + actionName
										+ " printerLocation=" + printerLocation + " printOrderType=" + printOrderType);
								continue;
							}
						}
						PrintOrderHandler printOrderHandler = new PrintOrderHandler(printerLocation, printService);
						printOrderHandler.print(printOrderType,
								FontSizeType.getEnum(
										actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
								this, cart, calculator);
					} else if (Configuration.getPrintOrderNoPriceActionOptions().contains(actionName)) {
						logger.fine("handling PrintOrderNoPriceActionOptions=" + actionName + " printerLocation="
								+ printerLocation);
						if (cart.getOrder().getStatus() == OrderStatus.HOLDING) {
							logger.fine("Order in holding status. Skip PrintOrderNoPriceActionOptions=" + actionName
									+ " printerLocation=" + printerLocation);
						} else {
							PrintOrderNoPriceHandler printOrderNoPriceHandler = new PrintOrderNoPriceHandler(
									printerLocation, printService);
							printOrderNoPriceHandler.print(PrintOrderNoPriceType.getEnum(actionName),
									FontSizeType.getEnum(actionAttribute
											.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
									this, cart, calculator);
						}
					} else if (Configuration.getPrintReceiptActionOptions().contains(actionName)) {
						logger.fine("handling PrintReceiptActionOptions=" + actionName + " printerLocation="
								+ printerLocation);
						PrintReceiptHandler printReceiptHandler = new PrintReceiptHandler(printerLocation,
								printService);
						printReceiptHandler.print(PrintReceiptType.getEnum(actionName),
								FontSizeType.getEnum(
										actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
								this, cart, calculator);
					} else if (Configuration.getPrintToGoPerStationActionOptions().contains(actionName)
							|| Configuration.getPrintDineInPerStationActionOptions().contains(actionName)) {
						if (cart.getOrder().getType() == OrderType.DINE_IN
								&& Configuration.getPrintDineInPerStationActionOptions().contains(actionName)) {
							logger.fine("handling PrintDineInPerStationActionOptions=" + actionName);

							for (int dishWorkstationId = 1; dishWorkstationId <= menu
									.getDineInStationCount(); ++dishWorkstationId) {
								boolean dishWorkstationHasWork = false;
								for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
									Dish dish = dishes[orderItem.getDishId()];
									if (dish.getDineInStationHasWork(dishWorkstationId)) {
										// voided orderItems are printed to notify of their cancellation.
										logger.finer("There's work for dishWorkstationId=" + dishWorkstationId
												+ " dish=" + dish);
										dishWorkstationHasWork = true;
										break;
									}
								}

								if (dishWorkstationHasWork) {
									printerLocation = Configuration.getDineInDishWorkstationIdToPrinterLocation()
											.get(dishWorkstationId);
									if (printerLocation == null) {
										throw new RuntimeException("Missing printerLocation for DineIn workstation id: "
												+ dishWorkstationId);
									}
									Map<String, Map<String, String>> dineInPrinterLocationToPrinter = Configuration
											.getDineInPrinterLocationToPrinter();
									PrintService dineInPrintService = AvailablePrintService
											.getPrintService(dineInPrinterLocationToPrinter.get(printerLocation)
													.get(ConfigKeyDefs.printerLocationToPrinter_name));
									PrintDineInPerStationHandler printDineInPerStationHandler = new PrintDineInPerStationHandler(
											printerLocation, dineInPrintService);
									PrintDineInPerStationType printDineInPerStationType = PrintDineInPerStationType
											.getEnum(actionName);
									logger.fine("handling PrintDineInPerStationActionOptions=" + actionName
											+ ", printDineInPerStationType=" + printDineInPerStationType
											+ " printerName=" + printService.getName() + " printerLocation="
											+ printerLocation);
									printDineInPerStationHandler.print(printDineInPerStationType,
											FontSizeType.getEnum(actionAttribute
													.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
											this, cart, calculator);
								} else {
									logger.finer("No printing needed for printerLocation=" + printerLocation
											+ " as it has no work from this order.");
								}
							}
						} else if (Configuration.getPrintToGoPerStationActionOptions().contains(actionName)) {
							if (cart.getOrder().getStatus() == OrderStatus.HOLDING) {
								logger.fine("Order in holding status. Skip PrintToGoPerStationActionOptions="
										+ actionName + " printerLocation=" + printerLocation);
								continue;
							}

							for (int dishWorkstationId = 1; dishWorkstationId <= menu
									.getToGoStationCount(); ++dishWorkstationId) {
								boolean dishWorkstationHasWork = false;
								for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
									Dish dish = dishes[orderItem.getDishId()];
									if (dish.getToGoStationHasWork(dishWorkstationId)) {
										// voided orderItems are printed to notify of their cancellation.
										logger.fine("There's work for dishWorkstationId=" + dishWorkstationId + " dish="
												+ dish);
										dishWorkstationHasWork = true;
										break;
									}
								}

								if (dishWorkstationHasWork) {
									printerLocation = Configuration.getToGoDishWorkstationIdToPrinterLocation()
											.get(dishWorkstationId);
									if (printerLocation == null) {
										throw new RuntimeException("Missing printerLocation for ToGo workstation id: "
												+ dishWorkstationId);
									}
									PrintService toGoPrintService = AvailablePrintService
											.getPrintService(toGoPrinterLocationToPrinter.get(printerLocation)
													.get(ConfigKeyDefs.printerLocationToPrinter_name));
									PrintToGoPerStationHandler printToGoPerStationHandler = new PrintToGoPerStationHandler(
											printerLocation, toGoPrintService);
									PrintToGoPerStationType printToGoPerStationType = PrintToGoPerStationType
											.getEnum(actionName);
									if (printService == null) {
										logger.warning("No printService to handle PrintToGoPerStationActionOptions: " + actionName
												+ ", printToGoPerStationType: " + printToGoPerStationType + " dishWorkstationId=" + dishWorkstationId);
									} else {
										logger.fine("handling PrintToGoPerStationActionOptions: " + actionName
												+ ", printToGoPerStationType: " + printToGoPerStationType + " printerName="
												+ printService.getName() + " printerLocation=" + printerLocation
												+ " dishWorkstationId=" + dishWorkstationId);
									}
									printToGoPerStationHandler.print(printToGoPerStationType,
											FontSizeType.getEnum(actionAttribute
													.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
											this, cart, calculator);
								} else {
									logger.finer("No printing needed for printerLocation: " + printerLocation
											+ " as it has no work from this order.");
								}
							}
						}
					} else {
						throw new RuntimeException("Unexpected action: '" + actionName
								+ "' is not handled. Check config.yaml and Configuration.java that the action is a valid action.");
					}
				}
			}
		}
	}

	boolean voidOrder() {
		logger.finest("Entered");
		if (PermissionUtil.hasPermission(PermissionRequiredType.VOID_ORDER, authenticatedEmployee) == false) {
			setWarningFeedback("Insufficient Permission to void order");
			orderTableView.requestFocus(); // take away focus on the inputTextField so to keep warning showing
			return false;
		}

		if (cart.isEmpty()) {
			setWarningFeedback("Cannot void an empty order");
			return false;
		}

		if (cart.isNew()) {
			setWarningFeedback("Use clear to abandon a new order instead of void.");
			return false;
		}

		switch (cart.getOrder().getStatus()) {
		case HOLDING:
		case MAKING:
			if (JOptionPane.showConfirmDialog(mainFrame, "Void Order?", "Confirm Message",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				cart.getOrder().setStatus(OrderStatus.VOIDED);
				cart.getOrder().setPaidTime(getServerTimestamp());

				if (sendCartToServer() == false) {
					setWarningFeedback("Order commit to kitchen failed, try again...");
					return false;
				}
				prepareForNextOrder();
			}
			return true;
		case PAID:
			setWarningFeedback("Order is no longer voidable");
			return false;
		case VOIDED:
			setWarningFeedback("Order already voided");
			return false;
		default:
			logger.warning("Unhandled orderStatus=" + cart.getOrder().getStatus());
			return false;
		}
	}

	void close() {
		logger.finest("Entered");
		if (PermissionUtil.hasPermission(PermissionRequiredType.CLOSE, authenticatedEmployee) == false) {
			setWarningFeedback("Insufficient Permission to perform close");
			orderTableView.requestFocus(); // take away focus on the inputTextField so to keep warning showing
			return;
		}

		boolean continueClosing = true;
		if (JOptionPane.showConfirmDialog(this, "Do you want to print a sales summary ?", "Confirm Message",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			if (Client.printDefaultHandler.printSummary(this, getUnsettledCarts()) == false) {
				continueClosing = false;
			}
		}

		try {
			if (continueClosing) {
				Client.server.settleCarts(authenticatedEmployee.getEmployeeId());
				setInfoFeedback("All Orders are closed");
				clearInputText();
			}
		} catch (RemoteException | SQLException e) {
			setWarningFeedback("Settle Failed. Try again...");
		}
	}

	void shutdown() {
		logger.finest("Entered");
		clearInputAndFeedback();

		if (PermissionUtil.hasPermission(PermissionRequiredType.SHUTDOWN_SERVER, authenticatedEmployee) == false) {
			setWarningFeedback("Insufficient Permission to shutdown");
			orderTableView.requestFocus(); // take away focus on the inputTextField so to keep warning showing
			return;
		}

		if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure to shutdown server?", "Confirm Message",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			try {
				server.shutdown();
				System.exit(0);
			} catch (RemoteException e) {
				setWarningFeedback("Shutdown Server Failed. Try again...");
			}
		} else {
			setInfoFeedback("Server was NOT shutdown");
		}
	}

	boolean sendCartToServer() {
		logger.finest("Entered");

		// Util.printStackTrace(); // show call stack for debugging

		OrderType orderType = cart.getOrder().getType();
		switch (orderType) {
		case DELIVERY:
			int minimumSubtotalForDeliveryInCent = Configuration.getMinimumSubtotalForDeliveryInCent();
			if (minimumSubtotalForDeliveryInCent != 0 && calculator.getSubtotal() < minimumSubtotalForDeliveryInCent) {
				setWarningFeedback("Subtotal must be at least "
					+ String.format("$%4.2f", minimumSubtotalForDeliveryInCent / 100.0) + " for delivery");
				return false;
			}

			if (Configuration.getDeliveryOrderMustHavePhoneNumber() && cart.getToGoInfo().getPhoneNumber().isEmpty()) {
				setWarningFeedback(
						"Enter missing phone number or use `" + SharedConstDefs.DEFAULT_PHONE_NUMBER + " as default");
				return false;
			}

			if (cart.getDeliveryInfo().getDeliveryCharge() == 0) {
				setInputText("=d1");
				setWarningFeedback("Enter missing delivery charge, then try again.");
				return false;
			}

			if (cart.getDeliveryInfo().getAddress().isEmpty()) {
				String customerName = cart.getToGoInfo().getCustomerName();
				if (customerName.isEmpty()) {
					setInputText("`name,street,city,state,zip");
					setWarningFeedback("Enter missing name and address, then try again.");
				} else {
					setInputText("`" + customerName + ",street,city,state,zip");
					setWarningFeedback("Enter missing address, then try again.");
				}

				return false;
			}

			String street = cart.getDeliveryInfo().getStreet();
			if (street.equals("street") || street.isEmpty()) {
				setInputText("@");
				setWarningFeedback("Enter missing street name for delivery, then try again.");
				return false;
			}

			String state = cart.getDeliveryInfo().getState();
			if (state.equals("state") || state.isEmpty()) {
				if (Configuration.getDeliveryStates().size() == 1) {
					cart.getDeliveryInfo().setState(Configuration.getDeliveryStates().get(0));
				} else {
					setWarningFeedback("Enter missing state name for delivery, then try again.");
				}
				return false;
			}

			// Could add isRequestedTimeTooEarly() check here for delivery orders. Need to
			// figure out delivery queue time though.
			break;
		case PHONE_IN:
			if (Configuration.getPhoneInOrderMustHavePhoneNumber() && cart.getToGoInfo().getPhoneNumber().isEmpty()) {
				setWarningFeedback(
						"Enter missing phone number or use `" + SharedConstDefs.DEFAULT_PHONE_NUMBER + " as default");
				return false;
			}

			if (cart.isNew()) {
				if (isRequestedTimeTooEarly()) {
					setWarningFeedback("Request time at " + cart.getToGoInfo().getFormattedRequestedTime()
							+ " is too early. Need at least " + cart.getToGoInfo().getQueueTimeMinutes() + " minutes.");
					return false;
				}
			}
			break;
		case DINE_IN:
			break;
		default:
			break;
		}

		if (cart.isNew()) {
			logger.fine("Inserting new cart. " + cart);
			if (cart.isEmpty()) {
				setWarningFeedback("There are no dishes in the order to commit");
				return false;
			}

			// commented out as resetting the phone number back to empty prevents the server
			// from removing the phone from the stashedCarts list.
			// if (cart.getToGoInfo().getPhoneNumber().equals(DEFAULT_PHONE_NUMBER)) {
			// cart.getToGoInfo().setPhoneNumber("");
			// }
		} else {
			logger.fine("Updating existing cart. " + cart);
		}

		try {
			cart = server.processCart(cart); // get back the cart where the server populate with data. e.g. orderNumber
		} catch (RemoteException | SQLException e) {
			setWarningFeedback("Commit failed. Try again...");
			e.printStackTrace();
			logger.severe("Exception: " + e.getMessage());
			return false;
		}

		cart.getOrder().incrementPrintVersion();

		setInfoFeedback("Order committed to kitchen");

		logger.fine("Order Committed to Kitchen: " + cart);

		return true;
	}

	boolean stashCartToServer() {
		logger.finest("Entered");
		logger.fine("stashing: " + cart);
		if (cart.isEmpty()) {
			logger.fine("Cart is not stashed as it has no orderItems. " + cart);
			setWarningFeedback("There's no dish in the order to stash");
			return false;
		}

		if (cart.getOrder().getOrderNumber() > 0) {
			logger.warning("Cannot stash a committed. " + cart);
			setWarningFeedback("Cannot stash a committed order");
			return false;
		}

		if (cart.getToGoInfo().getPhoneNumber().isEmpty()) {
			logger.fine("Phone number must be provided before stashing. " + cart);
			setWarningFeedback("Enter a phone number before stashing the order");
			return false;
		}

		try {
			server.stashCart(cart);
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.severe("Exception: " + e.getMessage());
			return false;
		}

		logger.fine("Stashed. " + cart);

		return true;
	}

	void showFullMenu(int beginSelectionIndex, int endSelectionIndex) {
		logger.finest("Entered");
		dishTableRows.clear();
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // need this
		dishTableView.setShowHorizontalLines(false);
		dishTableView.setShowVerticalLines(true);
		dishTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		dishTableView.setForeground(Color.blue);
		dishTableView.setBackground(Color.lightGray);
		dishTableView.setGridColor(Color.gray);
		dishTableView.setRowHeight(25);

		targetDishId = 0; // reset back to default;
		logger.finer("Reset targetDishId back to 0. targetDishId=" + targetDishId);

		for (Dish dish : sortedByCodeActiveDishes) {
			dishTableRows.add(Util.formatDishAttributes(dish));
		}

		if (endSelectionIndex >= dishes.length) {
			logger.warning("Unexpected end row=" + endSelectionIndex);
			return;
		}

		dishTableView.setRowSelectionInterval(beginSelectionIndex, endSelectionIndex);
		int columnCount = dishTableView.getColumnCount();
		for (int column = 0; column < columnCount; ++column) {
			if (dishTableView.getColumnName(column).equals(FeConstDefs.DISHVIEW_NAME)) {
				dishTableView.setColumnSelectionInterval(column, column);
				break;
			}
		}

		// scroll to the top of the table
		visibleRectangle.setBounds(0, 0, 1, 1); // x,y,width,length)
		dishTableView.scrollRectToVisible(visibleRectangle);

		clearInputText();
		inputTextField.requestFocus();
	}

	void abbreviationFiltered(String abbrev) {
		logger.finest("Entered");
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // needs this
		dishTableRows.clear();
		Map<Integer, String> abbreviations;
		String primaryAbbreviation;
		boolean foundTargetedDish = false;
		for (Dish dish : sortedByCodeActiveDishes) {
			abbreviations = dish.getIdToAbbreviation();
			primaryAbbreviation = abbreviations.get(0);
			if (primaryAbbreviation.startsWith(abbrev)) {
				dishTableRows.add(Util.formatDishAttributes(dish));
				if (foundTargetedDish == false) {
					foundTargetedDish = true;
					targetDishId = dish.getDishId();
					logger.finer("primary abbreviation sets targetDishId=" + targetDishId);
				}
			}
		}

		// compare the rest of the abbreviations in a dish
		for (Dish dish : sortedByCodeActiveDishes) {
			for (String abbreviation : dish.getIdToAbbreviation().values()) { // compare every possible match
				if (abbreviation.startsWith(abbrev)
						&& dishTableRows.contains(Util.formatDishAttributes(dish)) == false) {
					dishTableRows.add(Util.formatDishAttributes(dish));
					if (foundTargetedDish == false) {
						foundTargetedDish = true;
						targetDishId = dish.getDishId();
						logger.finer("rest of abbreviation sets targetDishId=" + targetDishId);
					}
				}
			}
		}
	}

	int codeFiltered(String codeString) {
		logger.finest("Entered");
		int firstDishId = -1;
		int code = Util.parseInt(codeString);
		if (code == -1) {
			return firstDishId;
		}

		int low = 0;
		int high = 0;

		if (code > 0 && code <= 9) {
			low = code * 100;
			high = low + 99;
		} else if (code >= 10 && code <= 99) {
			low = code * 10;
			high = low + 9;
		} else if (code >= 100 && code <= 999) {
			low = code;
			high = code;
		} else { // cannot filter
			logger.finer("Quantity has been specified as part of the code=" + code);
		}

		dishTableRows.clear();
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // needs this to refresh

		boolean foundDish = false;
		for (Dish dish : sortedByCodeActiveDishes) {
			int dishCode = dish.getCode();
			if (low <= dishCode && dishCode <= high) {
				if (foundDish == false) {
					foundDish = true;
					firstDishId = dish.getDishId();
					targetDishId = firstDishId;
				}
				dishTableRows.add(Util.formatDishAttributes(dish));
			}
		}

		return firstDishId;
	}

	void historyFiltered(String phoneNumber) {
		logger.finest("Entered");
		int maxHistoryOrderDisplayCount = Configuration.getMaxHistoryOrderDisplayCount();
		if (maxHistoryOrderDisplayCount <= 0) {
			logger.fine(
					"display order history is disabled. To enable set maxHistoryOrderDisplayCount to a positive integer.");
			return;
		}

		try {
			orderHistory = server.getOrderingHistory(phoneNumber, maxHistoryOrderDisplayCount);
		} catch (SQLException | RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
		}

		if (orderHistory.isEmpty()) {
			logger.fine("No ordering history for phoneNumber=" + phoneNumber);
			return;
		}

		dishTableRows.clear();
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // needs this
		dishTableView.setShowHorizontalLines(false);
		dishTableView.setShowVerticalLines(true);
		dishTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		dishTableView.setForeground(Color.red);
		dishTableView.setBackground(Color.lightGray);
		dishTableView.setGridColor(Color.blue);

		int row = 0;
		for (Cart historyCart : orderHistory) {
			Calculator calculator = new Calculator(historyCart, Client.menu);
			if (calculator.getFoodTotal() == 0) {
				// Order could have all voided orderItem hence 0 foodTotal.
				// This is to prevent adding the entire order where all items are voided
				continue;
			}
			Order order = historyCart.getOrder();
			++row;

			List<String> orderHeaderRow = new ArrayList<>();
			orderHeaderRow.add(" L    S");
			orderHeaderRow.add("# " + row + ".   " + Util.formatDateAndTimeNoSeconds(order.getOrderedTime()));
			orderHeaderRow.add("  ea.");
			orderHeaderRow.add("  ea.");
			orderHeaderRow.add("# " + row + ".   " + Util.formatDateAndTimeNoSeconds(order.getOrderedTime()));

			dishTableRows.add(orderHeaderRow);
			for (OrderItem orderItem : historyCart.getOrderItemTray().getOrderItems()) {

				int historyDishId = orderItem.getDishId();
				Dish historyDish = menu.getDish(historyDishId);
				if (historyDish == null) {
					continue;
				}

				List<String> dishAttributes = Util.formatDishAttributes(historyDish);
				String quantityString;
				if (orderItem.isVoided()) {
					quantityString = "voided";
				} else {
					quantityString = String.format("%2s + %-2s", orderItem.getLargeQuantity(),
							orderItem.getSmallQuantity());
				}
				dishAttributes.set(0, quantityString);

				if (historyDish.getLargePrice() != historyDish.getLargePrice()) {
					String displayingLargePrice = historyDish.getLargePrice() + "(New)";
					dishAttributes.set(2, displayingLargePrice);
				}
				if (historyDish.getSmallPrice() != historyDish.getSmallPrice()) {
					String displayingSmallPrice = historyDish.getSmallPrice() + "(New)";
					dishAttributes.set(3, displayingSmallPrice);
				}
				dishTableRows.add(dishAttributes);

				List<Customizer> customizers = historyCart.getCustomizerTray().getCustomizers(orderItem);
				int customizerDisplayId = 0;
				for (Customizer customizer : customizers) {
					customizer.setCustomizerId(customizerDisplayId++);
					int customizerPrice = customizer.getPrice();
					String customizerPriceString = String.format("%4.2f", customizerPrice/100.0);
					String customizerName = customizer.getName();
					Dish dishAsCustomizer = menu.getDish(customizerName);
					String customizerChineseName = (dishAsCustomizer == null) ? FeConstDefs.SEE_ENGLISH_IN_CHINESE
							: dishAsCustomizer.getChineseName();
					List<String> orderTotalCustomizerRow = new ArrayList<>();
					orderTotalCustomizerRow.add("");
					orderTotalCustomizerRow.add(orderItem.getOrderItemIdPlus1() + "." + customizer.getIdPlus1()
							+ " -> [" + customizerName + "]");
					orderTotalCustomizerRow.add(customizerPriceString);
					orderTotalCustomizerRow.add("");
					orderTotalCustomizerRow.add(customizerChineseName);
					dishTableRows.add(orderTotalCustomizerRow);
				}

				List<Exchanger> exchangers = historyCart.getExchangerTray().getExchangers(orderItem);
				int exchangerDisplayId = 0;
				for (Exchanger exchanger : exchangers) {
					exchanger.setExchangerId(exchangerDisplayId++);
					Dish exchangerDish = dishes[exchanger.getExchangerDishId()];
					List<String> orderTotalExchangerRow = new ArrayList<>();
					orderTotalExchangerRow.add("");
					orderTotalExchangerRow.add(orderItem.getOrderItemIdPlus1() + "." + exchanger.getIdPlus1WithPostfix()
							+ " => [" + exchanger.getName() + "]");
					orderTotalExchangerRow.add(String.format("%4.2f", exchangerDish.getLargePrice()/100.0));
					orderTotalExchangerRow.add(String.format("%4.2f", exchangerDish.getSmallPrice()/100.0));
					orderTotalExchangerRow.add(exchangerDish.getChineseName());
					dishTableRows.add(orderTotalExchangerRow);
				}
			}

			List<String> orderTotalRow = new ArrayList<>();
			orderTotalRow.add("<Dbl click dish row add dish>");
			orderTotalRow.add("<Dbl click header add order>");
			orderTotalRow.add("Total");
			orderTotalRow.add(String.format("$%4.2f",calculator.getTotal()/100.0));
			orderTotalRow.add("");
			dishTableRows.add(orderTotalRow);

			List<String> dividerRow = new ArrayList<>();
			dividerRow.add("---------");
			dividerRow.add("---------------------------");
			dividerRow.add("--------");
			dividerRow.add("--------");
			dividerRow.add("---------------------------");
			dishTableRows.add(dividerRow);
		}

		validate();
	}

	// e.g.: '123 Main Street', 123 is separated by space(s) and will be treated as
	// house number and ignored as a street name filter
	// hijacking the dishTable view to display street names for selection.
	void showStreetNameStartsWithIgnoreLeadingHouseNumber(String filter) {
		logger.finest("Entered");
		dishTableRows.clear();
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dishTableView.setShowHorizontalLines(false);
		dishTableView.setShowVerticalLines(true);
		dishTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		dishTableView.setForeground(Color.DARK_GRAY);
		dishTableView.setBackground(Color.lightGray);
		dishTableView.setGridColor(Color.blue);

		String filterLowerCase = "";
		if (filter != null && filter.trim().isEmpty() == false) {
			String[] fields = filter.replaceAll("\\s+", "\n").split("\n"); // collapse white space occurrence into one
																			// new line for splitting
			if (fields.length > 1) {
				String houseNumber = fields[0];
				if (houseNumber != null && houseNumber.trim().isEmpty() == false) {
					char firstChar = houseNumber.charAt(0);
					if (Character.isDigit(firstChar)) {
						// if there are at least 2 fields delimited by whitespace(s), take the second
						// field as the filter when the first field(house number) starts with a digit
						// house number can contain character such as '15A 1st ave' where 'A' is the
						// subfix or '25w15' where the direction is part of the house number.
						filterLowerCase = fields[1].trim().toLowerCase();
					}
				}
			}
		}

		logger.finer("filterLowerCase=" + filterLowerCase);

		int rowCount = 0;
		for (Street street : Configuration.getDeliveryStreets().values()) {
			String streetNameLowerCase = street.getName().toLowerCase();
			if (streetNameLowerCase.startsWith(filterLowerCase)) {
				List<String> row = new ArrayList<>();
				row.add(String.valueOf(RowType.STREET.getCharacterCode()) + (++rowCount));
				row.add(street.getName());
				row.add(String.valueOf(street.getHouseNumberMin()));
				row.add(String.valueOf(street.getHouseNumberMax()));
				row.add(street.getZip());
				dishTableRows.add(row);
			}
		}

		validate();
	}

	void showCityNames(String filter) {
		logger.finest("Entered");
		dishTableRows.clear();
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dishTableView.setShowHorizontalLines(false);
		dishTableView.setShowVerticalLines(true);
		dishTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		dishTableView.setForeground(Color.DARK_GRAY);
		dishTableView.setBackground(Color.lightGray);
		dishTableView.setGridColor(Color.blue);

		String filterLowerCase = "";
		if (filter != null && filter.trim().isEmpty() == false) {
			filterLowerCase = filter.toLowerCase();
		}

		logger.finer("filterLowerCase=" + filterLowerCase);

		int rowCount = 0;
		for (String city : Configuration.getDeliveryCities()) {
			if (city.toLowerCase().startsWith(filterLowerCase)) {
				List<String> row = new ArrayList<>();
				row.add(String.valueOf(RowType.CITY.getCharacterCode()) + (++rowCount));
				row.add(city);
				row.add("");
				row.add("");
				row.add("");
				dishTableRows.add(row);
			}
		}

		validate();
	}

	int showAllTableNumbers() {
		logger.finest("Entered");
		return showTableNumbers("");
	}

	int showTableNumbers(String filter) {
		logger.finest("Entered");
		dishTableRows.clear();
		dishTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dishTableView.setShowHorizontalLines(false);
		dishTableView.setShowVerticalLines(true);
		dishTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		dishTableView.setForeground(Color.DARK_GRAY);
		dishTableView.setBackground(Color.lightGray);
		dishTableView.setGridColor(Color.blue);

		String filterLowerCase = "";
		if (filter != null && filter.trim().isEmpty() == false) {
			filterLowerCase = filter.toLowerCase();
		}

		logger.finer("filterLowerCase=" + filterLowerCase);

		Map<String, Integer> tableNumber2Capacity = Configuration.getTableNumbers();
		int rowCount = 0;
		for (String tableNumber : tableNumber2Capacity.keySet()) {
			if (tableNumber.toLowerCase().startsWith(filterLowerCase)) {
				List<String> row = new ArrayList<>();
				row.add(String.valueOf(RowType.TABLE_NUMBER.getCharacterCode()) + (++rowCount));
				row.add(tableNumber);
				row.add(String.valueOf(tableNumber2Capacity.get(tableNumber)));
				row.add("");
				row.add("");
				dishTableRows.add(row);
			}
		}

		validate();

		return rowCount;
	}

	int addCustomizerToOrderTableSelectedOrderItem(String userEnteredCustomizerString) {
		logger.finest("Entered");
		int price = 0;
		String customizerName = userEnteredCustomizerString;
		int customizerDishId = CustomizerTable.NON_MENU_CUSTOMIZER_ID;
		// no match in menu, default price=0 unless user specified
		if (dishTableRows.size() <= 0) {// no match on dish
			if (userEnteredCustomizerString.length() == 0) {
				logger.warning("Expect customizer length greater than 0");
			}

			int lastSpaceIndex = userEnteredCustomizerString.lastIndexOf(' ');
			if (lastSpaceIndex >= 0) {
				String maybePrice = userEnteredCustomizerString.substring(lastSpaceIndex + 1);
				try {// assuming last word in customizer is a valid price
					price = Integer.valueOf(maybePrice);
					customizerName = userEnteredCustomizerString.substring(0, lastSpaceIndex);
					if (customizerName.length() > CustomizerTable.NAME_SIZE) {
						customizerName.substring(0, CustomizerTable.NAME_SIZE - 1);
						setWarningFeedback("Customizer name has been truncated to: '" + customizerName + "'");
					}
				} catch (NumberFormatException e) {
				} // ignore exception, price and name are set above
			}
		} else {// customizer found in menu
			Dish dish = dishes[targetDishId];
			customizerName = dish.getShortName();
			price = dish.getLargePrice();
			customizerDishId = targetDishId;
		}

		OrderItem orderItem = cart.getOrderItemTray()
				.getOrderItem(getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO));
		if (orderItem == null) {
			return -1;
		}

		Customizer customizer = new Customizer(orderItem.getOrderItemId(), customizerDishId, customizerName, price);
		cart.getCustomizerTray().addCustomizer(customizer);
		return orderItem.getDishId(); // highlighting this orderItem in the orderTableView
	}

	void buildOrderTable() {
		logger.finest("Entered");
		orderTableRows.clear();
		orderTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // AUTO_RESIZE_ALL_COLUMNS
		orderTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		orderTableView.setRowHeight(25);
		orderTableView.setShowVerticalLines(true);
		orderTableView.setShowHorizontalLines(true);
		OrderStatus orderStatus = cart.getOrder().getStatus();
		if (cart.isNew()) {
			// new order is red color
			switch (orderStatus) {
			case HOLDING:
				orderTableView.setBackground(Color.yellow);
				break;
			default:
				orderTableView.setBackground(Color.white);
				break;
			}
			orderTableView.setForeground(Color.red);
			orderTableView.setGridColor(Color.lightGray);
		} else {
			switch (orderStatus) {
			case PAID:
				orderTableView.setBackground(Color.white);
				orderTableView.setForeground(Color.magenta);
				orderTableView.setGridColor(Color.gray);
				break;
			case VOIDED:
				orderTableView.setBackground(Color.black);
				orderTableView.setForeground(Color.red);
				orderTableView.setGridColor(Color.gray);
				break;
			case HOLDING:
				orderTableView.setBackground(Color.yellow);
				orderTableView.setForeground(Color.red);
				orderTableView.setGridColor(Color.gray);
				break;
			default:
				orderTableView.setBackground(Color.white);
				orderTableView.setForeground(Color.blue);
				orderTableView.setGridColor(Color.gray);
				break;
			}
		}

		boolean includeVoided = true;
		List<OrderItem> orderItems = calculator.getOrderItemsCombinedWithCondiments(includeVoided);
		int orderItemCount = orderItems.size();
		if (orderItemCount > 0) {
			int rowIndex = -1;
			for (OrderItem orderItem : orderItems) {
				int largeQuantity = orderItem.getLargeQuantity();
				int smallQuantity = orderItem.getSmallQuantity();

				Dish dish = dishes[orderItem.getDishId()];
				String dishName = dish.getShortName();
				String chineseName = dish.getChineseName();
				int orderItemTotal = (dish.getLargePrice() * orderItem.getLargeQuantity()
						+ dish.getSmallPrice() * orderItem.getSmallQuantity()
						+ cart.getCustomizerTray().getCustomizerTotal(orderItem)
						+ cart.getExchangerTray().getExchangerTotal(orderItem, dishes));

				String orderItemTotalString = "";
				if (orderItem.isVoided()) {
					orderItemTotalString = "Voided";
				} else if (orderItemTotal == 0) {
					orderItemTotalString = "-";
				} else {
					orderItemTotalString = String.format("%4.2f", orderItemTotal/100.0);
				}

				addOrderTableRow(" " + (orderItem.getOrderItemIdPlus1()) + ".", String.valueOf(largeQuantity),
						String.valueOf(smallQuantity), " " + dishName, orderItemTotalString, "   " + chineseName);
				++rowIndex;

				if (orderItem.getDishId() == targetDishId) {
					orderTableView.setRowSelectionInterval(rowIndex, rowIndex);
				}

				List<Customizer> customizers = cart.getCustomizerTray().getCustomizers(orderItem);
				int customizerDisplayId = 0;
				for (Customizer customizer : customizers) {
					customizer.setCustomizerId(customizerDisplayId++);
					int customizerPrice = customizer.getPrice();
					String customizerPriceString = "";

					// Non uniform padding due to non fixed width font
					if (customizer.getStatus() == SubOrderItemStatus.VOIDED) {
						customizerPriceString = SubOrderItemStatus.VOIDED.getDisplayName();
					} else if (customizerPrice == 0) {
						customizerPriceString = "-";
					} else {
						customizerPriceString = String.format("%4.2f", customizerPrice/100.0);
					}
					String customizerName = customizer.getName();
					Dish dishAsCustomizer = menu.getActiveDish(customizerName);
					String customizerChineseName = (dishAsCustomizer == null) ? FeConstDefs.SEE_ENGLISH_IN_CHINESE
							: dishAsCustomizer.getChineseName();
					addOrderTableRow(" " + orderItem.getOrderItemIdPlus1() + "." + customizer.getIdPlus1(), "", "",
							" -> [" + customizerName + "]", customizerPriceString,
							" ->  [" + customizerChineseName + "]");
					++rowIndex;
				}

				List<Exchanger> exchangers = cart.getExchangerTray().getExchangers(orderItem);
				int exchangerDisplayId = 0;
				for (Exchanger exchanger : exchangers) {
					exchanger.setExchangerId(exchangerDisplayId++);
					int exchangerDishId = exchanger.getExchangerDishId();
					Dish exchangerDish = Client.dishes[exchangerDishId];
					int exchangerPrice = exchangerDish.getLargePrice() * orderItem.getLargeQuantity()
							+ exchangerDish.getSmallPrice() * orderItem.getSmallQuantity();
					String exchangerPriceString = "";

					// Non uniform padding due to non fixed width font
					if (exchanger.getStatus() == SubOrderItemStatus.VOIDED) {
						exchangerPriceString = SubOrderItemStatus.VOIDED.getDisplayName();
					} else if (exchangerPrice == 0) {
						exchangerPriceString = "-";
					} else {
						exchangerPriceString = String.format("%4.2f", exchangerPrice/100.0);
					}
					String exchangerName = exchanger.getName();
					String exchangerChineseName = (exchangerDish == null) ? FeConstDefs.SEE_ENGLISH_IN_CHINESE
							: exchangerDish.getChineseName();
					addOrderTableRow(" " + orderItem.getOrderItemIdPlus1() + "." + exchanger.getIdPlus1WithPostfix(),
							"", "", " => [" + exchangerName + "]", exchangerPriceString,
							" =>  [" + exchangerChineseName + "]");
					++rowIndex;
				}

				List<Subdish> subdishes = dish.getSubdishes();
				if (subdishes != null && orderItem.isVoided() == false) {
					for (Subdish subdish : subdishes) {
						String subdishName = subdish.getName();
						Dish subdishInMenu = menu.getActiveDish(subdishName);

						String subDishChineseName;
						double subdishQuantity = (largeQuantity + smallQuantity / 2.0) * subdish.getQuantity();
						if (subdishInMenu == null) {
							logger.warning("subdishname = " + subdishName + " for dish " + dish.getShortName()
									+ " is not found as a dish in the menu. Check exact spelling of the subdish name.");
							subDishChineseName = subdishName;
						} else {
							subDishChineseName = subdishInMenu.getChineseName();
						}
						++rowIndex;
						addOrderTableRow("", "", "", "-> " + Util.formatDouble(subdishQuantity, 1, 0) + " " + subdishName,
								"", "-> " + Util.formatDouble(subdishQuantity, 1, 0) + " " + subDishChineseName);

					}
				}
			}
			addOrderTableRow("", "", "", "", "", ""); // empty row
		}

		if (cart.getOrder().getNote().isEmpty() == false) {
			addOrderTableRow("ONote:", "******", "******", " " + cart.getOrder().getNote(), "************", "");
		}

		OrderType orderType = cart.getOrder().getType();
		if (orderType != OrderType.DINE_IN) {
			Map<String, Float> condiments = calculator.getOrderCondimentNameToQuantityLessMatchedOrderItem();
			for (String condimentName : condiments.keySet()) {
				//Allow 4 width flush right
				addOrderTableRow("", "   -", String.format("%4s",Util.formatDouble(condiments.get(condimentName), 1, 0)),
						" " + condimentName, "", "");
			}
		}

		for (OrderItem orderItem : orderItems) {
			List<Customizer> customizers = cart.getCustomizerTray().getCustomizers(orderItem);
			for (Customizer customizer : customizers) {
				if (customizer.getCustomizerDishId() <= CustomizerTable.NON_MENU_CUSTOMIZER_ID) {
					String name = customizer.getName();
					addOrderTableRow("Custom Item", "", "", name, "", "Add condidments");
				}
			}
		}

		if (orderItemCount > 0) {
			addOrderTableRow("----------", "------", "------", "----------------------------------", "------------",
					"----------------------------------");
		}

		long currentTimeMillis = System.currentTimeMillis();
		String currentTimeString = Util.formatTimeNoSeconds(currentTimeMillis);
		String orderedTimeString = "";
		String orderStatusString = "New Order";

		if (orderItemCount > 0) {
			orderedTimeString = currentTimeString;
		}

		if (cart.isNew() == false) {
			orderedTimeString = Util.formatTimeNoSeconds(cart.getOrder().getOrderedTime());
			if (orderedTimeString.length() == 7) {
				orderedTimeString = "  " + orderedTimeString;
			}
			orderStatusString = "   " + orderStatus;
		}

		DineInInfo dineInInfo = cart.getDineInInfo();
		switch (orderType) {
		case DELIVERY:
			deliveryRadioButton.setSelected(true);
			Employee driverEmployee = getEmployee(cart.getDeliveryInfo().getDriverId());
			String driverNickname = driverEmployee == null ? "" : driverEmployee.getNickname();
			addOrderTableRow("Delivery", "", "", " Driver:  " + driverNickname, "",
					" " + FeConstDefs.DRIVER_INCHINESE + ": " + driverNickname);
			break;
		case PHONE_IN:
			phoneInRadioButton.setSelected(true);
			addOrderTableRow("Pickup:", "", "", "              " + orderType.getDisplayName(), "",
					" ---   " + orderType.getChineseName() + "   ---");
			break;
		case WALK_IN:
			walkInRadioButton.setSelected(true);
			addOrderTableRow("Pickup:", "", "", "                " + orderType.getDisplayName(), "",
					" ---   " + orderType.getChineseName() + "   ---");
			break;
		case DINE_IN:
			dineInRadioButton.setSelected(true);
			String tableNumber = dineInInfo.getTableNumber();
			int guestCount = dineInInfo.getGuestCount();
			String guestCountStr = (guestCount == 0) ? "" : String.valueOf(guestCount);
			addOrderTableRow("Dinein:", "", "", " Table: " + tableNumber + "     Guest: " + guestCountStr, "",
					" " + FeConstDefs.TABLE_INCHINESE + ": " + tableNumber + "     " + FeConstDefs.GUEST_INCHINESE
							+ ": " + guestCountStr);
			break;
		default:
			logger.warning("Unexpected order type=" + orderType);
			break;
		}

		addOrderTableRow("Order:", "   In:", "", "", orderedTimeString, "");

		String requestedTimeString = "";
		if (cart.getToGoInfo().getRequestedTime() != 0) {
			requestedTimeString = cart.getToGoInfo().getFormattedRequestedTime();
			if (requestedTimeString.length() == 7) {
				requestedTimeString = "  " + requestedTimeString;
			}
			addOrderTableRow("", "Req:", " ", " Requested Time", requestedTimeString, "");
		}

		int currentQueueTimeMinutes = 0;
		try {
			currentQueueTimeMinutes = Calculator.estimateKitchenQueueTimeMinutes(server.getAllOrderIntervalTotals(),
					calculator.getFoodTotal());
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
		}

		long currentQueueTimeMillis = currentQueueTimeMinutes * 60 * 1000;
		long estimatedTime = currentTimeMillis + currentQueueTimeMillis;
		int fromCartQueueTimeMinutes = cart.getToGoInfo().getQueueTimeMinutes();
		String estimatedTimeString = Util.formatTimeNoSeconds(Util.roundToNearestFiveMinutes(estimatedTime));

		long estimatedArrivalTime = cart.getDeliveryInfo().getEstimatedArrivalTime();
		String estimatedArrivalTimeString = Util.formatTimeNoSeconds(estimatedArrivalTime);

		int drivingDurationMinutes = cart.getDeliveryInfo().getDrivingDurationMinutes();
		if (cart.isNew()) {
			switch (orderType) {
			case DELIVERY:
				if (estimatedArrivalTime == 0) {
					if (drivingDurationMinutes == 0) {
						addOrderTableRow("", "Est:", "",
								currentTimeString + " + " + currentQueueTimeMinutes + " + dr time  + drv avail time",
								"?", "");
					} else {
						estimatedTime += (drivingDurationMinutes * 60 * 1000);
						estimatedTimeString = Util.formatTimeNoSeconds(Util.roundToNearestFiveMinutes(estimatedTime));
						addOrderTableRow(
								"", "Est:", "", currentTimeString + " + " + currentQueueTimeMinutes + " kit + "
										+ drivingDurationMinutes + " dr time + drv avail time",
								estimatedTimeString + "?", "");
					}
				} else {
					addOrderTableRow("", "Est:", "", " Estimated Arrival Time", estimatedArrivalTimeString, "");
				}
				break;
			case PHONE_IN:
				String pickupTimePrepend = "";
				if (cart.getToGoInfo().getRequestedTime() > 0) {
					pickupTimePrepend = " Earliest";
				}
				addOrderTableRow("", "Est:", "", pickupTimePrepend + " Pickup Time (" + currentTimeString + " + "
						+ currentQueueTimeMinutes + "min)", estimatedTimeString, "");

				break;
			case WALK_IN:
				addOrderTableRow("", "Est:", "",
						" Ready Time (" + currentTimeString + " + " + currentQueueTimeMinutes + "min)",
						estimatedTimeString, "");
				break;
			default:
				break;
			}
		} else {
			switch (orderType) {
			case DELIVERY:
				if (drivingDurationMinutes == 0) {
					addOrderTableRow("", "Est:", "",
							" " + currentTimeString + " + " + currentQueueTimeMinutes + "min + (missing driving time)",
							"?", "");
				} else {
					estimatedTime += (drivingDurationMinutes * 60 * 1000);
					estimatedTimeString = Util.formatTimeNoSeconds(Util.roundToNearestFiveMinutes(estimatedTime));
					addOrderTableRow(
							"", "Est:", "", "" + " " + currentTimeString + " + " + currentQueueTimeMinutes
									+ "min (kit) + " + drivingDurationMinutes + "min (dr time)",
							estimatedTimeString, "");
				}
				if (estimatedArrivalTime > 0) {
					addOrderTableRow("", "Est:", "", " Estimated Arrival Time", estimatedArrivalTimeString, "");
				}
				break;
			case PHONE_IN:
			case WALK_IN:
				if (fromCartQueueTimeMinutes == 0) {
					addOrderTableRow("", "Est:", "",
							" " + currentTimeString + " + " + fromCartQueueTimeMinutes + " min", estimatedTimeString,
							"");
				} else {
					long orderTime = cart.getOrderItemTray().getOrderItemCount() > 0
							? cart.getOrderItemTray().getOrderItemAt(0).getOrderedTime()
							: cart.getOrder().getOrderedTime();
					estimatedTime = orderTime + currentQueueTimeMillis;
					estimatedTimeString = Util.formatTimeNoSeconds(estimatedTime);
					if (estimatedTimeString.length() == 7) {
						estimatedTimeString = "  " + estimatedTimeString;
					}
					addOrderTableRow("", "", "", (orderType == OrderType.PHONE_IN ? "Pickup" : "Ready") + " Time",
							estimatedTimeString, "");
				}
				break;
			default:
				break;
			}
		}

		// TakeBy - Order taken by
		Employee takenByEmployee = getEmployee(cart.getOrder().getTakenById());
		String takenByNickname = takenByEmployee == null ? "" : takenByEmployee.getNickname();
		addOrderTableRow("By:", "", "", " " + takenByNickname, "", "");

		// Server
		if (cart.getOrder().getType() == OrderType.DINE_IN) {
			Employee serverEmployee = getEmployee(dineInInfo.getServerId());
			String serverNickname = serverEmployee == null ? "" : serverEmployee.getNickname();
			addOrderTableRow("Server:", "", "", " " + serverNickname, "", "");
		}

		// Cashier
		String paidTime = Util.formatTimeNoSeconds(cart.getOrder().getPaidTime());
		if (paidTime.length() == 7) {
			paidTime = "  " + paidTime;
		}
		// as long as the tender > 0 then display the Cashier

		if (cart.getPaymentTray().getTotalTendered() > 0 || orderStatus == OrderStatus.VOIDED) {
			Employee cashierEmployee = getEmployee(cart.getOrder().getCashierId());
			String cashierNickname = cashierEmployee == null ? "" : cashierEmployee.getNickname();
			addOrderTableRow("Cashier", "", "", " " + cashierNickname + "", "" + paidTime, "");
		}

		String padding = "                ";
		for (Payment payment : cart.getPaymentTray().getPayments()) {
			// Now payments display based on the order it was entered and number from 1 to
			// n. Previously it was based on payment type and numbered per type.
			// This way is easier to implement and also preserved the sequence the payments
			// were entered and stored.
			// The "Pymt paymentId" with a space format is parsed to delete payment. Any
			// change needs corresponding parsing change in PaymentTray.delete(String)
			// change
			addOrderTableRow(PAYMENT_LABEL + payment.getPaymentIdPlus1(), "", "",
					padding + payment.getType().getDisplayName() + ":", String.format("%4.2f", payment.getAmount()/100.0), "");
		}

		if (cart.getPaymentTray().getTotalTendered() > 0) {
			if (cart.getPaymentTray().getTotalTendered() - calculator.getTotal() > 0) {
				addOrderTableRow("", "", "", padding + "CHANGE:",
						String.format("%4.2f", (cart.getPaymentTray().getTotalTendered() - calculator.getTotal())/100.0), "");
			}
		}

		if (orderStatus == OrderStatus.PAID || orderStatus == OrderStatus.VOIDED) {
			addOrderTableRow("", "", "", "        " + orderStatusString, paidTime, "");
		}

		// CustomerProfile Information
		if (orderType != OrderType.DINE_IN) {
			addOrderTableRow("", "", "", "", "", ""); // empty row
			addOrderTableRow(" Phone:", "", "", " " + cart.getToGoInfo().getFormattedPhoneNumber(), "", "");
			addOrderTableRow(" Name:", "", "", " " + cart.getToGoInfo().getCustomerName(), "", "");
			addOrderTableRow(" Addr:", "", "", " " + cart.getDeliveryInfo().getAddress(), "", "");
		}

		if (orderType == OrderType.DELIVERY) {
			double latitude = cart.getDeliveryInfo().getLatitude();
			double longitude = cart.getDeliveryInfo().getLongitude();
			if (latitude != 0 || longitude != 0) {
				addOrderTableRow(" Coordinates", "", "",
						" " + String.format("%-10.6f", latitude) + ", " + String.format("%11.6f", longitude), "", "");
			}

			String drivingDirection = cart.getDeliveryInfo().getDrivingDirection();
			if (drivingDirection.isEmpty() == false) {
				addOrderTableRow(" Direction", "", "", " " + drivingDirection, "", "");
			}

			if (drivingDurationMinutes > 0) {
				addOrderTableRow(" DrTime", "", "", " " + drivingDurationMinutes + " minutes", "", "");
			}

			float drivingDistance = cart.getDeliveryInfo().getDrivingDistance();
			if (drivingDistance > 0) {
				addOrderTableRow(" Dist", "", "", " " + drivingDistance + " miles", "", "");
			}
		}

		if (orderType != OrderType.DINE_IN) {
			String customerNote = cart.getToGoInfo().getNote();
			if (customerNote.isEmpty() == false) {
				addOrderTableRow(" CNote:", "", "", " " + customerNote, "", "");
			}
		}

		if (orderType == OrderType.DELIVERY && cart.getToGoInfo().getCreditCardNumber().isEmpty() == false) {
			addOrderTableRow(" Card:", "", "", " " + cart.getToGoInfo().getCreditCardNumber(), "", "");
		}

		addOrderTableRow("", "", "", "", "", ""); // empty row
		addOrderTableRow("", "", "", "", "", ""); // empty row

		visibleRectangle.setBounds(0, orderTableView.getSelectedRow() * 16 + orderTableView.getSelectedRow() / 2, 1, 5);
		orderTableView.scrollRectToVisible(visibleRectangle);

		boolean butttonState;
		switch (orderStatus) {
		case HOLDING:
		case MAKING:
			butttonState = true;
			break;
		default:
			butttonState = false;
			break;
		}
		phoneInRadioButton.setEnabled(butttonState);
		deliveryRadioButton.setEnabled(butttonState);
		walkInRadioButton.setEnabled(butttonState);
		dineInRadioButton.setEnabled(butttonState);
		cashButton.setEnabled(butttonState);
		creditCardButton.setEnabled(butttonState);
		checkButton.setEnabled(butttonState);

		refreshTotalPanel();
	}

	synchronized void buildBusinessTable(List<Cart> carts, Color foregroundColor, boolean clearFeedbackText) {
		logger.finest("Entered");
		if (clearFeedbackText) {
			clearFeedbackText();
		}

		businessTableRows.clear();
		businessTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // must have
		businessTableView.setForeground(foregroundColor);

		for (Cart cart : carts) {
			Order order = cart.getOrder();
			OrderItemTray orderItemTray = cart.getOrderItemTray();
			ToGoInfo toGoInfo = cart.getToGoInfo();
			DeliveryInfo deliveryInfo = cart.getDeliveryInfo();
			DineInInfo dineInInfo = cart.getDineInInfo();
			Calculator calculator = new Calculator(cart, Client.menu);

			List<String> businessTableRow = new ArrayList<>();
			businessTableRow.add("   " + order.getOrderNumber());
			String nameColumn = " ";
			String serverNickname = "";
			String driverNickname = "";
			switch (order.getType()) {
			case DINE_IN:
				if (dineInInfo.getGuestCount() > 0) {
					nameColumn += dineInInfo.getTableNumber() + "  Guests: " + dineInInfo.getGuestCount();
				} else {
					nameColumn += dineInInfo.getTableNumber();
				}
				Employee serverEmployee = getEmployee(dineInInfo.getServerId());
				serverNickname = serverEmployee == null ? "" : serverEmployee.getNickname();
				break;
			case DELIVERY:
				Employee driverEmployee = getEmployee(deliveryInfo.getDriverId());
				driverNickname = driverEmployee == null ? "" : driverEmployee.getNickname();
				// fall through
			default: // if not DINE_IN, then it's a toGo order, i.e: walk-in,phone-in,delivery
				nameColumn += toGoInfo.getCustomerName();
			}
			businessTableRow.add(nameColumn);
			businessTableRow.add(" " + toGoInfo.getFormattedPhoneNumber());
			businessTableRow.add(getOrderStatusString(cart));
			businessTableRow.add(String.format("%5.2f", calculator.getTotal() / 100.0));
			businessTableRow.add(" " + order.getType().getDisplayName());
			businessTableRow.add(" " + serverNickname);
			businessTableRow.add(" " + driverNickname);
			//businessTableRow.add(String.format(" %1$TI:%1$TM", order.getOrderedTimestamp())); //Another option
			businessTableRow.add(String.format(" %9s", order.getOrderedTime() == 0 ? "" : Util.formatTimeNoSeconds(order.getOrderedTime())));

			long outTime = 0;
			if (order.getStatus() == OrderStatus.PAID) {
				outTime = order.getPaidTime();
			} else {
				switch (order.getType()) {
				case PHONE_IN:
				case WALK_IN:
					outTime = cart.getToGoInfo().getRequestedTime();
					break;
				case DELIVERY:
					outTime = cart.getToGoInfo().getRequestedTime();
					if (outTime == 0) {
						outTime = cart.getDeliveryInfo().getEstimatedArrivalTime();
					}
					if (outTime != 0) {
						outTime = outTime - (cart.getDeliveryInfo().getDrivingDurationMinutes() * 60 * 1000);
					}
					break;
				default:
					break;
				}
			}

			String outColumn = " " + (outTime == 0 ? "" : Util.formatTimeNoSeconds(outTime));
			if (outColumn.length() == 7) {
				outColumn = "  " + outColumn;
			}
			businessTableRow.add(outColumn);

			Employee cashierEmployee = getEmployee(order.getCashierId());
			String cashierNickname = cashierEmployee == null ? "" : cashierEmployee.getNickname();
			businessTableRow.add(" " + cashierNickname);
			businessTableRow.add(order.getDiscountAmount() == 0 ? "" : " -" + String.format("%4.2f", order.getDiscountAmount()/100.0));
			businessTableRow.add(order.getDiscountPercent() == 0 ? ""
					: "  -" + order.getDiscountPercent() + "%" + "  =  - "
							+ String.format("%4.2f", calculator.getPercentDiscountAmount()/100.0).trim());
			businessTableRow.add(orderItemTray.getVoidedOrderItemCount() == 0 ? ""
					: "      " + orderItemTray.getVoidedOrderItemCount());
			businessTableRow.add(order.getNote() == null ? "" : " " + order.getNote());
			businessTableRow.add(order.getType() == OrderType.DELIVERY ? " " + deliveryInfo.getAddress() : "");

			businessTableRows.add(businessTableRow);
		}

		// one way to scroll to the bottom
		visibleRectangle.setBounds(0, carts.size() * 50, 1, 1);
		businessTableView.scrollRectToVisible(visibleRectangle);
		businessTableView.repaint();
		businessTableView.clearSelection();
		inputTextField.requestFocus();
		// validate();
		repaint();
	}

	synchronized void buildArrivalTable(List<Cart> carts, Color foregroundColor) {
		logger.finest("Entered");
		arrivalTableRows.clear();
		arrivalTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // must have
		arrivalTableView.setForeground(foregroundColor);

		for (Cart cart : carts) {
			ToGoInfo toGoInfo = cart.getToGoInfo();
			List<String> arrivalTableRow = new ArrayList<String>();
			arrivalTableRow.add(String.valueOf(cart.getOrder().getOrderNumber()));
			arrivalTableRow.add(toGoInfo.getCustomerName());
			arrivalTableRow.add(" " + toGoInfo.getFormattedPhoneNumber());
			arrivalTableRow.add(getOrderStatusString(cart));
			String arrivalColumn = " " + Util.formatTimeNoSeconds(toGoInfo.getArrivalTime());
			if (arrivalColumn.length() == 7) {
				arrivalColumn = "  " + arrivalColumn;
			}
			arrivalTableRow.add(arrivalColumn);

			arrivalTableRows.add(arrivalTableRow);
		}

		// one way to scroll to the bottom
		visibleRectangle.setBounds(0, carts.size() * 50, 1, 1);
		arrivalTableView.scrollRectToVisible(visibleRectangle);
		arrivalTableView.repaint();
		arrivalTableView.clearSelection();
		inputTextField.requestFocus();
		// validate();
		repaint();
	}

	boolean isModifyingOrderAllowed() {
		logger.finest("Entered");
		switch (cart.getOrder().getStatus()) {
		case HOLDING:
		case MAKING:
			break;
		case PAID:
			setWarningFeedback("Cannot modify order any more as it is paid");
			return false;
		case VOIDED:
			setWarningFeedback("Cannot modify order any more as it is voided");
			return false;
		default:
			break;
		}

		return true;
	}

	boolean addSelectedRowToOrder(int selectedRow) {
		logger.finest("Entered");
		if (isModifyingOrderAllowed() == false) {
			inputTextField.requestFocus();
			return false;
		}

		List<Dish> skippedDishes = new ArrayList<Dish>();

		int columnCount = dishTableView.getColumnCount();
		int selectedColumn = dishTableView.getSelectedColumn();
		Dish dish = getDishViewHighlightedDish(selectedRow);
		logger.fine("dish=" + dish);
		if (dish == null) {// must been an order header that was selected
			int codeColumnIndex;
			for (codeColumnIndex = 0; codeColumnIndex < columnCount; ++codeColumnIndex) {
				if (dishTableView.getColumnName(codeColumnIndex).equals(FeConstDefs.DISHVIEW_NAME)) {
					break;
				}
			}

			// Parsing this for the number after #: "# 1. 2018-08-18 2:32 PM"
			String indexInHistoryListAndDate = (String) dishTableView.getValueAt(selectedRow, codeColumnIndex);
			if (indexInHistoryListAndDate.contains("#") == false) {
				setWarningFeedback("Order item is invalid or no longer available");
				return false; // not the head column that was selected. Ignore selection
			}

			String[] parts = indexInHistoryListAndDate.split("\\."); // split at period
			String indexPart = parts[0].substring(2); // drop first character, i.e. '#'
			int index = -1;
			try {
				index = Integer.valueOf(indexPart);
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("Exception: " + e.getMessage());
				return false;
			}

			if (index == -1) {
				return false;
			}

			setInfoFeedback("Re-ordering order " + indexInHistoryListAndDate);

			Cart historyCart = orderHistory.get(index - 1);

			for (OrderItem orderItem : historyCart.getOrderItemTray().getOrderItems()) {
				if (orderItem.isVoided()) {
					continue;
				}
				int historyDishId = orderItem.getDishId();
				Dish historyDish = menu.getDish(historyDishId);
				if (historyDish == null) {
					continue;
				}

				Dish targetDish;
				if (historyDish.isActive()) {
					targetDish = historyDish;
				} else {
					Dish activeDish = menu.getActiveDish(historyDish.getShortName());
					if (activeDish == null) {
						logger.fine("Skipping non active history dish " + historyDish);
						skippedDishes.add(historyDish);
						continue;
					}
					logger.fine("Found new active dish for history dish. Active dish " + activeDish);
					targetDish = activeDish;
				}

				logger.finer("targetDish " + targetDish);
				// Don't add back coupon dishes from history order
				if (historyDish.getCategory() != Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.couponDish)) {
					cart.getOrderItemTray().addOrderItem(targetDish, DishSize.LARGE, orderItem.getLargeQuantity(),
							cart);
					logger.fine("Adding dish to order. " + targetDish);
					cart.getOrderItemTray().addOrderItem(targetDish, DishSize.SMALL, orderItem.getSmallQuantity(),
							cart);
				}

				updateEstimatedTime(orderItem.getLargeQuantity() + orderItem.getSmallQuantity() / 2);

				for (Customizer customizer : historyCart.getCustomizerTray().getCustomizers(orderItem)) {
					cart.getCustomizerTray().addCustomizer(customizer);
				}

				for (Exchanger exchanger : historyCart.getExchangerTray().getExchangers(orderItem)) {
					cart.getExchangerTray().addExchanger(exchanger);
				}
			}
		} else {
			// A dish after a change is marked as inactive in the database, try to find it
			// by name in the
			if (dish.isActive() == false) {
				String inactiveDishName = dish.getShortName();
				Dish activeDish = menu.getActiveDish(inactiveDishName);
				if (activeDish == null) {
					skippedDishes.add(dish);
				} else {
					dish = activeDish;
				}
			}

			if (dish.isActive()) {
				DishSize dishSize = DishSize.LARGE;
				if (dish.getSmallPrice() != 0
						&& dishTableView.getColumnName(selectedColumn).equals(FeConstDefs.DISHVIEW_SMALL)) {
					dishSize = DishSize.SMALL;
				}

				int quantity = 1;
				targetDishId = dish.getDishId();
				logger.finer("targetDishId=" + targetDishId);
				if (dish.getCategory() == Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.customizer)) {
					String s = inputTextField.getText();
					if (s.trim().isEmpty() == false && s.trim().charAt(0) == '['
							|| isOrderTableSelectedRowAnOrderItem()) {
						OrderItem orderItem = cart.getOrderItemTray()
								.getOrderItem(getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO));
						if (orderItem != null) {
							if (orderItem.isVoided()) {
								setWarningFeedback("Cannot add " + dish.getQuotedShortName() + " to voided item '"
										+ orderItem.getDishShortName() + "'");
								return false;
							} else {
								Customizer customizer = new Customizer(orderItem.getOrderItemId(),
										dishes[targetDishId]);
								cart.getCustomizerTray().addCustomizer(customizer);
							}
						}
					} else {
						setWarningFeedback(dish.getQuotedShortName()
								+ " by itself is not an orderable item. Select a dish and try again");
						return false;
					}
				} else if (dish.getCategory() == Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.exchangeCondiment)) {
					if (isOrderTableSelectedRowAnOrderItem()) {
						OrderItem orderItem = cart.getOrderItemTray()
								.getOrderItem(getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO));
						if (orderItem != null) {
							if (orderItem.isVoided()) {
								setWarningFeedback("Cannot add " + dish.getQuotedShortName() + " to voided item '"
										+ orderItem.getDishShortName() + "'");
								return false;
							} else {
								Exchanger exchanger = new Exchanger(orderItem.getOrderItemId(), dishes[targetDishId]);
								cart.getExchangerTray().addExchanger(exchanger);
							}
						}
					} else {
						setWarningFeedback(dish.getQuotedShortName()
								+ " by itself is not an orderable item. Select a dish and try again");
						return false;
					}
				} else {
					if (isValidToAddCouponDish(dish) == false) {
						return false;
					}
					cart.getOrderItemTray().addOrderItem(dish, dishSize, quantity, cart);
					updateEstimatedTime(quantity);
				}
			} else {
				skippedDishes.add(dish);
			}
		}

		String skippedDishNames = "";
		for (Dish skippedDish : skippedDishes) {
			skippedDishNames = skippedDishNames + skippedDish.getShortName() + ", ";
		}
		if (skippedDishNames.isEmpty() == false) {
			skippedDishNames = skippedDishNames.trim();
			skippedDishNames.substring(0, skippedDishNames.length() - 1);
			setWarningFeedback("Skipped nonavailable dish(es): '" + skippedDishNames + "'");
			return false;
		}

		buildOrderTable();

		return true;
	}

	void updateEstimatedTime(int quantity) {
		logger.finest("Entered");
		// new order or adding more than 5 orderItems to an exiting order, then
		// recalculate.
		if (cart.isNew() || quantity > 5) {
			try {
				cart.getToGoInfo().setQueueTimeMinutes(Calculator.estimateKitchenQueueTimeMinutes(
						server.getAllOrderIntervalTotals(), new Calculator(cart, menu).getFoodTotal()));
			} catch (RemoteException e) {
				e.printStackTrace();
				logger.warning("Exception: " + e.getMessage());
			}
		}
	}

	boolean isValidToAddCouponDish(Dish dish) {
		logger.finest("Entered");
		int couponDishCategory = Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.couponDish);
		if (dish.getCategory() == couponDishCategory) {
			CouponDish couponDish = dish.getCouponDish();
			if (couponDish == null) {
				setWarningFeedback("Coupon " + dish.getQuotedShortName() + " does not exist in the internal menu");
				return false;
			} else if (calculator.getFoodTaxableAmount() < couponDish.getMinimumFoodTotal()) { 
				// change if check of calculator.getFoodTaxableAmount() to use calculator.getFoodTotal() if want to include tax
				setWarningFeedback("To use coupon " + dish.getQuotedShortName() + " minimum food total must be "
						+ String.format("$%4.2f", couponDish.getMinimumFoodTotal() / 100.0));
				return false;
			}
			logger.fine("calculator.getFoodTotal()=" + calculator.getFoodTotal());

			for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
				Dish tmpDish = Client.dishes[orderItem.getDishId()];
				// No order can use two non-voided coupons. Later code logic depends it.
				if (orderItem.isVoided() == false && tmpDish.getCategory() == couponDishCategory) {
					setWarningFeedback(
							"Cannot use two coupons per order. Coupon '" + tmpDish.getShortName() + "' is in effect.");
					return false;
				}
			}
		}

		return true;
	}

	boolean isOrderTableSelectedRowAnOrderItem() {
		logger.finest("Entered");
		String infoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
		return (infoColumnValue.endsWith(".")); // expects xx. format
	}

	void deleteSelectedRow() {
		logger.finest("Entered");
		if (isModifyingOrderAllowed() == false) {
			return;
		}

		if (isOrderTableSelectedRowAnOrderItem()) {// Deleting an orderItem
			String orderTableInfoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
			OrderItem orderItem = cart.getOrderItemTray().getOrderItem(orderTableInfoColumnValue);
			if (orderItem != null) {
				if (cart.isNew()) {
					// allow to delete any orderItem as the order is not committed yet
					cart.getOrderItemTray().deleteOrderItem(orderItem, cart);
					setInfoFeedback("Deleted " + orderItem.getQuotedDishShortName());
					// high light the row immediate above the deleted dish entry
					if (cart.getOrderItemTray().getOrderItemCount() > 0) {
						int targetOrderItemId = orderItem.getOrderItemIdMinus1() - 1;
						if (targetOrderItemId < 0) {
							targetOrderItemId = 0;
						}
						OrderItem targetOrderItem = cart.getOrderItemTray().getOrderItemAt(targetOrderItemId);
						targetDishId = targetOrderItem.getDishId();
						logger.fine("targetOrderItem=" + targetOrderItem);
					}
				} else { // a committed order - allow void existing orderItem or delete newly added
					String orderItemFeedbackInfo = "Order Item " + orderTableInfoColumnValue + " "
							+ dishes[orderItem.getDishId()].getQuotedShortName();
					OrderItemStatus orderItemStatus = orderItem.getStatus();
					switch (orderItemStatus) {
					case WAITING:
						if (orderItem.getOrderedTime() > cart.getOrder().getCommittedTime()) {
							// Allow to delete as this orderItem as the order is not committed yet
							// or the orderItem was added after the last order committed time
							logger.fine("Deleting orderItem: " + orderItem + " orderItemOrderedTime="
									+ orderItem.getOrderedTime() + " orderCommittedTime="
									+ cart.getOrder().getCommittedTime());
							cart.getOrderItemTray().deleteOrderItem(orderItem, cart);
							setInfoFeedback("Deleted " + dishes[orderItem.getDishId()].getQuotedShortName());
						} else {
							if (PermissionUtil.hasPermission(PermissionRequiredType.VOID_ORDER_ITEM,
									authenticatedEmployee)) {
								orderItem.setStatus(OrderItemStatus.VOIDED);
								orderItem.setVoidedTime(getServerTimestamp());
								int employeeId = authenticatedEmployee.getEmployeeId();
								orderItem.setVoidedById(employeeId);
								setInfoFeedback("Voided " + orderItemFeedbackInfo);
							} else {
								setInfoFeedback(authenticatedEmployee.getNickname() + " is not allowed to void "
										+ orderItemFeedbackInfo);
							}
						}
						break;
					case VOIDED:
						if (orderItem.getVoidedTime() > cart.getOrder().getCommittedTime()) {
							// toggle back to valid state as order has not been (re)committed yet
							orderItem.setStatus(OrderItemStatus.WAITING);
							orderItem.setVoidedTime(0);
							setInfoFeedback("Unvoided " + orderItemFeedbackInfo);
							logger.fine("Allow to unvoid orderItem: " + orderItem);
						} else {
							setWarningFeedback(
									orderItemFeedbackInfo + " is voided, committed, and cannot be modified anymore");
						}
						break;
					case COOKING:
					case EATING:
					default:
						break;
					}
				}
			}
		} else if (isOrderTableSelectedRowAcustomizer()) {
			String orderTableInfoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
			Customizer customizer = cart.getCustomizerTray().getCustomizer(orderTableInfoColumnValue);
			if (cart.isNew()) {
				logger.fine("Deleting customizer: " + customizer);
				cart.getCustomizerTray().deleteCustomizer(customizer);
				setInfoFeedback("Deleted " + customizer.getQuotedName());
			} else {
				String customizerFeedbackInfo = "Customizer " + orderTableInfoColumnValue + " "
						+ customizer.getQuotedName();
				if (customizer.getPrice() > 0) {
					// Allow double click to toggle between voided and valid.
					// The toggling should only be allowed when the user just 'voided' in the
					// customizer but has not commit the cart again yet.
					SubOrderItemStatus customizerStatus = customizer.getStatus();
					switch (customizerStatus) {
					case VALID:
						if (customizer.getOrderedTime() > cart.getOrder().getCommittedTime()) {
							logger.fine("Deleting customizer: " + customizer);
							cart.getCustomizerTray().deleteCustomizer(customizer);
							setInfoFeedback("Deleted " + customizer.getQuotedName());
						} else {
							if (PermissionUtil.hasPermission(PermissionRequiredType.VOID_CUSTOMIZER,
									authenticatedEmployee)) {
								customizer.setStatus(SubOrderItemStatus.VOIDED);
								customizer.setVoidedTime(getServerTimestamp());
								int employeeId = authenticatedEmployee.getEmployeeId();
								customizer.setVoidedById(employeeId);
								setInfoFeedback("Voided " + customizerFeedbackInfo);
								logger.fine("Voided priced customizer: " + customizer);
							} else {
								setInfoFeedback(authenticatedEmployee.getNickname() + " is not allowed to void "
										+ customizerFeedbackInfo);
							}
						}
						break;
					case VOIDED:
						if (customizer.getVoidedTime() > cart.getOrder().getCommittedTime()) {
							// toggle back to valid state as order has not been (re)committed yet
							customizer.setStatus(SubOrderItemStatus.VALID);
							customizer.setVoidedTime(0);
							setInfoFeedback("Unvoided " + customizerFeedbackInfo);
							logger.fine("Allow to unvoid priced customizer: " + customizer);
						} else {
							setWarningFeedback(
									customizerFeedbackInfo + " is voided, commited, and cannot be modified anymore");
						}
						break;
					default:
						setWarningFeedback(
								"Program Error: Customizer status of " + customizerStatus + " is not supported");
						break;
					}
				} else {
					logger.fine("Deleting non-priced customizer: " + customizer);
					cart.getCustomizerTray().deleteCustomizer(customizer);
				}
			}
		} else if (isOrderTableSelectedRowAnExchanger()) {
			String orderTableInfoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
			Exchanger exchanger = cart.getExchangerTray().getExchanger(orderTableInfoColumnValue);
			if (cart.isNew()) {
				logger.fine("Deleting exchanger: " + exchanger);
				cart.getExchangerTray().deleteExchanger(exchanger);
				setInfoFeedback("Deleted " + exchanger.getQuotedName());
			} else {
				String exchangerFeedbackInfo = "Exchanger " + orderTableInfoColumnValue + " '" + exchanger.getName()
						+ "'";
				Dish exchangerDish = dishes[exchanger.getExchangerDishId()];
				if (exchangerDish.getLargePrice() > 0 || exchangerDish.getSmallPrice() > 0) {
					// Allow double click to toggle between voided and valid.
					// The toggling should only be allowed when the user just 'voided' in the
					// exchanger but has not commit the cart again yet.
					SubOrderItemStatus exchangerStatus = exchanger.getStatus();
					switch (exchangerStatus) {
					case VALID:
						if (exchanger.getOrderedTime() > cart.getOrder().getCommittedTime()) {
							logger.fine("Deleting exchanger: " + exchanger);
							cart.getExchangerTray().deleteExchanger(exchanger);
							setInfoFeedback("Deleted " + exchanger.getQuotedName());
						} else {
							if (PermissionUtil.hasPermission(PermissionRequiredType.VOID_EXCHANGER,
									authenticatedEmployee)) {
								exchanger.setStatus(SubOrderItemStatus.VOIDED);
								exchanger.setVoidedTime(getServerTimestamp());
								int employeeId = authenticatedEmployee.getEmployeeId();
								exchanger.setVoidedById(employeeId);
								setInfoFeedback("Voided " + exchangerFeedbackInfo);
								logger.fine("Voided priced exchanger: " + exchanger);
							} else {
								setInfoFeedback(authenticatedEmployee.getNickname() + " is not allowed to void "
										+ exchangerFeedbackInfo);
							}
						}
						break;
					case VOIDED:
						if (exchanger.getVoidedTime() > cart.getOrder().getCommittedTime()) {
							// toggle back to valid state as order has not been (re)committed yet
							exchanger.setStatus(SubOrderItemStatus.VALID);
							exchanger.setVoidedTime(0);
							setInfoFeedback("Unvoided " + exchangerFeedbackInfo);
							logger.fine("Allow to unvoid priced customizer: " + exchanger);
						} else {
							setWarningFeedback(
									exchangerFeedbackInfo + " is voided, commited, and cannot be modified anymore");
						}
						break;
					default:
						setWarningFeedback(
								"Program Error: Exchanger status of " + exchangerStatus + " is not supported");
						break;
					}
				} else {
					logger.fine("Deleting non-priced customizer: " + exchanger);
					cart.getExchangerTray().deleteExchanger(exchanger);
				}
			}
		} else if (isOrderTableSelectedRowApayment()) {// Deleting payment
			if (cart.getPaymentTray().deletePayment(getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO))) {
				logger.fine("Deleted payment");
			} else {
				setWarningFeedback("Payment was not deleted"); // some kind of program error indication here
			}
		}

		buildOrderTable();
	}

	void handleArrivalTableUpdate() {
		logger.finest("Entered");
		int selectedRow = arrivalTableView.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}

		int orderNumberColumnIndex = getArrivalTableViewColumnIndex(FeConstDefs.ARRIVALVIEW_ORDERNUMBER);
		String orderNumberString = (String) arrivalTableView.getValueAt(selectedRow, orderNumberColumnIndex);
		int orderNumber;
		try {
			orderNumber = Integer.parseInt(orderNumberString);
		} catch (Exception e) {
			logger.warning("orderNumberString=" + orderNumberString + " selectedRow=" + selectedRow
					+ " orderNumberColumnIndex=" + orderNumberColumnIndex + " msg:" + e.getMessage());
			return;
		}

		List<Cart> carts = null;
		try {
			carts = Client.server.findCartByOrderNumber(orderNumber);
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception. orderNumber=" + orderNumber + " msg:" + e.getMessage());
			return;
		}

		if (carts.size() == 0) {
			logger.warning("Server return empty when seacher for orderNumber=" + orderNumber);
			return;
		}

		Cart cart = carts.get(0);
		cart.getToGoInfo().setCustomerStatus(CustomerStatus.PICKED_UP);
		try {
			Client.server.processCart(cart);
		} catch (RemoteException | SQLException e) {
			e.printStackTrace();
			logger.warning("Exception. cart=" + cart + " msg:" + e.getMessage());
		}
	}

	String getOrderTableColumnValue(String columnName) {
		logger.finest("Entered");
		int selectedRow = orderTableView.getSelectedRow();
		if (selectedRow == -1) {
			return ""; // no row selected
		}

		int columnIndex = getOrderTableViewColumnIndex(columnName);
		String columnValueString = (String) orderTableView.getValueAt(selectedRow, columnIndex);
		return columnValueString.trim();
	}

	Dish getDishViewHighlightedDish(int selectedRow) {
		logger.finest("Entered");
		return menu.getActiveDish(getDishViewHighlightedColumnValue(selectedRow, FeConstDefs.DISHVIEW_NAME));
	}

	boolean isIndicatedRowSelected(int selectedRow, String rowIndicator) {
		logger.finest("Entered");
		String codeColumnValue = getDishViewHighlightedColumnValue(selectedRow, FeConstDefs.DISHVIEW_CODE);
		return (codeColumnValue.startsWith(rowIndicator));
	}

	void handlRowTypeSelection(RowType rowType, int selectedRow) {
		logger.finest("Entered");
		switch (rowType) {
		case STREET:
			addSelectedStreetToInputText(selectedRow);
			break;
		case CITY:
			addSelectedCityToInputText(selectedRow);
			break;
		case TABLE_NUMBER:
			addSelectedTableNumberToInputText(
					getDishViewHighlightedColumnValue(selectedRow, FeConstDefs.DISHVIEW_NAME));
			break;
		default:
			break;
		}
	}

	void addSelectedStreetToInputText(int selectedRow) {
		logger.finest("Entered");
		String originalInputText = inputTextField.getText().trim();
		int lastDelimiterIndex = originalInputText.lastIndexOf(' ');
		String replacementText = (lastDelimiterIndex >= 0 ? originalInputText.substring(0, lastDelimiterIndex).trim()
				: originalInputText);
		String streetName = getDishViewHighlightedColumnValue(selectedRow, FeConstDefs.DISHVIEW_NAME);
		setInputText(replacementText + ' ' + streetName);
		inputTextField.requestFocus();
	}

	void addSelectedCityToInputText(int selectedRow) {
		logger.finest("Entered");
		String originalInputText = inputTextField.getText().trim();
		int lastDelimiterIndex = originalInputText.lastIndexOf(SharedConstDefs.DELIMITER);
		String replacementText = (lastDelimiterIndex >= 0 ? originalInputText.substring(0, lastDelimiterIndex).trim()
				: originalInputText);
		String columnValue = getDishViewHighlightedColumnValue(selectedRow, FeConstDefs.DISHVIEW_NAME);
		setInputText(replacementText + SharedConstDefs.DELIMITER + columnValue);
		inputTextField.requestFocus();
	}

	void addSelectedTableNumberToInputText(String inputText) {
		logger.finest("Entered");
		setInputText("`" + inputText + SharedConstDefs.DELIMITER);
		setPromptFeedback("Enter number of guests");
		inputTextField.requestFocus();
	}

	String getDishViewHighlightedColumnValue(int selectedRow, String columnName) {
		logger.finest("Entered");
		int columnCount = dishTableView.getColumnCount();
		int nameColumnIndex;
		for (nameColumnIndex = 0; nameColumnIndex < columnCount; ++nameColumnIndex) {
			if (dishTableView.getColumnName(nameColumnIndex).equals(columnName)) {
				break;
			}
		}

		return (String) dishTableView.getValueAt(selectedRow, nameColumnIndex);
	}

	int getDishTableViewColumnIndex(String columnName) {
		logger.finest("Entered");
		int columnIndex = 0;
		while (dishTableView.getColumnName(columnIndex).equals(columnName) == false) {
			++columnIndex;
		}

		return columnIndex;
	}

	int getOrderTableViewColumnIndex(String columnName) {
		logger.finest("Entered");
		int columnIndex = 0;
		while (orderTableView.getColumnName(columnIndex).equals(columnName) == false) {
			++columnIndex;
		}

		return columnIndex;
	}

	int getBusinessTableViewColumnIndex(String columnName) {
		logger.finest("Entered");
		int columnIndex = 0;
		while (businessTableView.getColumnName(columnIndex).equals(columnName) == false) {
			++columnIndex;
		}

		return columnIndex;
	}

	int getArrivalTableViewColumnIndex(String columnName) {
		logger.finest("Entered");
		int columnIndex = 0;
		while (arrivalTableView.getColumnName(columnIndex).equals(columnName) == false) {
			++columnIndex;
		}

		return columnIndex;
	}

	void showSelectedOrder(int selectedOrderNumber) {
		logger.finest("Entered");
		if (cart.isNew() && isCartEmpty() == false) {
			setWarningFeedback("Order is not 'DONE'. Print to finish or CLEAR to cancel order");
		} else {
			if (selectedOrderNumber > 0) {
				try {
					List<Cart> carts = server.findCartByOrderNumber(selectedOrderNumber);
					if (cart == null) {
						setWarningFeedback("All orders have been closed and settled");
						prepareForNextOrder();
					} else {
						this.cart = carts.get(0); // There should be only one match
						calculator = new Calculator(cart, Client.menu);
						buildOrderTable();
						clearInputAndFeedback();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					logger.warning("Exception: " + e.getMessage());
					return;
				}
			} else if (selectedOrderNumber == 0) {
				// All stashed orders have orderNumber 0.
				try {
					String selectedPhoneNumber = getSelectedPhoneNumberInBusinessTable();
					Cart cart = server.findStashedCartByPhoneNumbe(selectedPhoneNumber);
					if (cart == null) {
						setWarningFeedback("Stashed order not found by phone number " + selectedPhoneNumber);
						prepareForNextOrder();
					} else {
						this.cart = cart; // There should be only one match
						calculator = new Calculator(cart, Client.menu);
						buildOrderTable();
						clearInputAndFeedback();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					logger.warning("Exception: " + e.getMessage());
					return;
				}
			} else {
				logger.fine("Invalid order number=" + selectedOrderNumber);
			}
		}

		// disable this requestFocus, so up down arrow keys can be used to browse orders
		// inputTextField.requestFocus();
	}

	List<Cart> findCarts(String s) {
		logger.finest("Entered");
		if (s == null || s.isEmpty()) {
			return null;
		}

		if (s.matches("[\\d]+")) {
			// all digits
			int len = s.length();
			if (len < 4) {
				// treat as order number
				try {
					return server.findCartByOrderNumber(Integer.parseInt(s));
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Exception: " + e.getMessage());
					return null;
				}
			} else {
				// treat as ending digits phone number
				try {
					return server.findCartByPhoneNumber(s);
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Exception: " + e.getMessage());
					return null;
				}
			}
		} else {
			try {
				return server.findCartByCustomerName(s);
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("Exception: " + e.getMessage());
				return null;
			}
		}
	}

	List<Cart> getUnsettledCarts() {
		logger.finest("Entered");
		List<Cart> carts = new ArrayList<Cart>();
		try {
			carts = server.getUnsettledCarts();
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
		}

		return carts;
	}

	List<Cart> getStashedCarts() {
		logger.finest("Entered");
		try {
			return new ArrayList<>(server.getStashedCarts().values());
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	long getServerTimestamp() {
		logger.finest("Entered");
		try {
			return server.getTimestamp();
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
			return 0;
		}
	}

	Employee getEmployee(String initials) {
		logger.finest("Entered");
		Employee employee = initialsToActiveEmployee.get(initials);
		if (employee == null) {
			// null could happen if a newly added employee in the DB but server has not
			// restarted since. Make another request to the server to for a refresh
			try {
				initialsToActiveEmployee = server.refreshInitialsToActiveEmployee();
				return (initialsToActiveEmployee.get(initials));
			} catch (RemoteException | SQLException e) {
				e.printStackTrace();
				logger.warning("Exception: " + e.getMessage());
				return null;
			}
		}
		return employee;
	}

	int getSelectedOrderNumberInBusinessTable() {
		logger.finest("Entered");
		int orderNumberColumnIndex = getBusinessTableViewColumnIndex(FeConstDefs.BUSINESSVIEW_ORDERNUMBER);
		int selectedRow = businessTableView.getSelectedRow();
		return Util.parseInt((String) businessTableView.getValueAt(selectedRow, orderNumberColumnIndex));
	}

	int getSelectedOrderNumberInArrivalTable() {
		logger.finest("Entered");
		int orderNumberColumnIndex = getArrivalTableViewColumnIndex(FeConstDefs.ARRIVALVIEW_ORDERNUMBER);
		int selectedRow = arrivalTableView.getSelectedRow();
		return Util.parseInt((String) arrivalTableView.getValueAt(selectedRow, orderNumberColumnIndex));
	}

	void setPromptFeedback(Dish dish, DishSize dishSize, int quantity) {
		logger.finest("Entered");
		String promptFeedback = "nothing here";
		int dishCategory = dish.getCategory();
		if (dishCategory == customizerCategory || dishCategory == exchangeCondimentCategory
				|| dishCategory == allNoIngredientCategory) {
			if (dishSize == DishSize.SMALL && dish.isSmallSizeValid() == false) {
				promptFeedback = DishSize.SMALL.getDisplayName() + " is not available for " + dish.getQuotedShortName();
			} else if (quantity > 1) {
				promptFeedback = quantity + " " + dish.getQuotedShortName()
						+ " is specified. Will only accept one at a time.";
			} else {
				int price = (dishSize == DishSize.LARGE ? dish.getLargePrice() : dish.getSmallPrice());
				promptFeedback = dish.getQuotedShortName() + " -->" + String.format("$%4.2f", price * quantity / 100.0);
			}
		} else {
			if (dishSize == DishSize.SMALL && dish.isSmallSizeValid() == false) {
				promptFeedback = DishSize.SMALL.getDisplayName() + " is not available for " + dish.getQuotedShortName();
			} else {
				promptFeedback = quantity + " " + dishSize.getDisplayName() + " " + dish.getQuotedShortName();
				int price = (dishSize == DishSize.LARGE ? dish.getLargePrice() : dish.getSmallPrice());
				promptFeedback = promptFeedback + " -->" + String.format("$%4.2f",price * quantity / 100.0);
			}
		}

		if (dish.getChineseName() != null) {
			promptFeedback = promptFeedback + "     " + dish.getChineseName();
		}

		setPromptFeedback(promptFeedback);
	}

	boolean isCartEmpty() {
		return (cart.getOrderItemTray().getOrderItemCount() == 0);
	}

	void setInputText(String text) {
		clearInputText();
		inputTextField.setText(text);
	}

	void clearInputText() {
		inputTextField.selectAll();
		inputTextField.replaceSelection("");
	}

	void setPromptFeedback(String info) {
		inputFeedBack.setForeground(Color.blue);
		inputFeedBack.setFont(new Font("Helvetica", Font.PLAIN, 18));
		inputFeedBack.setText(" " + info);
		logger.finer(info);
		validate();
	}

	void setInfoFeedback(String info) {
		inputFeedBack.setForeground(Color.blue);
		inputFeedBack.setFont(new Font("Helvetica", Font.PLAIN, 16));
		inputFeedBack.setText(" " + info);
		logger.fine(info);
		validate();
	}

	void setWarningFeedback(String warning) {
		inputFeedBack.setForeground(Color.red);
		inputFeedBack.setFont(new Font("Helvetica", Font.PLAIN, 16));
		inputFeedBack.setText(" " + warning);
		logger.warning(warning);
		validate();
	}

	void clearFeedbackText() {
		inputFeedBack.setText("");
		validate();
	}

	void clearInputAndFeedback() {
		clearInputText();
		clearFeedbackText();
		validate();
	}

	void showHelp() {
		String sb = HelpText.showHelp(authenticatedEmployee);
		JLabel label = new JLabel(sb.toString());
		label.setFont(new Font("Arial", Font.BOLD, 10));
		JOptionPane.showMessageDialog(this, label, "Application Help", JOptionPane.OK_OPTION);
	}

	// =============class private methods===============
	private static void connectToServerForNotification(String host) throws UnknownHostException, IOException {
		logger.config("Connecting to host=" + host);

		// if host==null, socket tries to connect to localhost
		serverSocket = new Socket(host, Server.SERVER_TCP_PORT);

		logger.config("Connected to host=" + host + " socket=" + serverSocket);
	}

	private boolean authenticateClientUser(JFrame mainFrame) throws RemoteException, SQLException {
		logger.finest("Entered");
		int authenticateRetryCount = 0;
		while (authenticatedEmployee == null) {
			JPanel authenticationPanel = new JPanel(new BorderLayout(5, 5));

			JPanel leftsideLabels = new JPanel(new GridLayout(0, 1, 2, 2));
			leftsideLabels.add(new JLabel("User ID", SwingConstants.RIGHT));
			leftsideLabels.add(new JLabel("Password", SwingConstants.RIGHT));
			authenticationPanel.add(leftsideLabels, BorderLayout.WEST);

			JPanel gridPanel = new JPanel(new GridLayout(0, 1, 2, 2));
			JTextField userIdTextField = new JTextField();
			gridPanel.add(userIdTextField);
			JPasswordField passwordField = new JPasswordField();
			gridPanel.add(passwordField);
			authenticationPanel.add(gridPanel, BorderLayout.CENTER);

			String prompt = authenticateRetryCount == 0 ? "login" : "Incorrect userId or password. Try again...";
			if (JOptionPane.showConfirmDialog(mainFrame, authenticationPanel, prompt,
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
				logger.fine("authentication cancelled");
				return false;
			}

			String userId = userIdTextField.getText();
			logger.finer("authenticating userId=" + userId);
			authenticatedEmployee = server.authenticateUser(userId, new String(passwordField.getPassword()));

			if (++authenticateRetryCount >= 3) {
				logger.severe("Failed to autenticate after authenticateRetryCount=" + authenticateRetryCount);
				return false;
			}
		}

		logger.fine("authenticatedEmployee=" + authenticatedEmployee);
		return true;
	}

	private ImageIcon createImageIcon(String imageFile) {
		logger.finest("Entered");
		// images are resources that can be accessed from the root of the classes
		// folder. In eclipse IDE, bin is the default classes output.
		// After the src/main/resources folder is specified as source folder in the
		// classpath,
		// During build, all the content in the resources folder is copied to the bin
		// directory.
		// At run time, these images are searched according to the classpath
		// order. This works when the caller specified the images/whatever.rpg as
		// the imgFile param java.net.URL imgURL = getClass().getResource("/" +
		// imgFile);

		// Another way is to just specify the file, and add the images as a source
		// folder to the buildpath and let it search for as as a resource.
		URL imageUrl = getClass().getClassLoader().getResource("images/" + imageFile);
		if (imageUrl != null) {
			return new ImageIcon(imageUrl);
		} else {
			throw new RuntimeException("Couldn't find image file: " + imageFile);
		}
	}

	private JRadioButton createJRadioButton(OrderType orderType, ButtonGroup orderTypeButtonGroup,
			JPanel orderTypePanel) {
		logger.finest("Entered");
		JRadioButton rb = new JRadioButton(orderType.getDisplayName(), createImageIcon("rb.jpg"));
		rb.setToolTipText(orderType.getToolTipText());
		rb.setMnemonic(orderType.getMnemonic());
		rb.setActionCommand(orderType.toString());
		rb.setPressedIcon(createImageIcon("rbp.jpg"));
		rb.setRolloverIcon(createImageIcon("rbr.jpg"));
		rb.setRolloverSelectedIcon(createImageIcon("rbrs.jpg"));
		rb.setSelectedIcon(createImageIcon("rbs.jpg"));
		rb.setFocusPainted(true);
		rb.setBorderPainted(true);
		rb.setContentAreaFilled(true);
		rb.setMargin(new Insets(5, 5, 5, 5));
		rb.setVerticalTextPosition(AbstractButton.CENTER);
		// rb.setHorizontalTextPosition(AbstractButton.CENTER);
		rb.addActionListener(this);
		rb.addKeyListener(new GuiKeyAdapter(this));

		orderTypeButtonGroup.add(rb);
		orderTypePanel.add(rb);

		return rb;
	}

	private JButton createJButton(PaymentType paymentType, ButtonGroup paymentTypeButtonGroup,
			JPanel paymentTypePanel) {
		logger.finest("Entered");
		JButton button = new JButton(paymentType.getDisplayName());
		button.setToolTipText(paymentType.getToolTipText());
		button.setMnemonic(paymentType.getMnemonic());
		button.setActionCommand(paymentType.toString());
		button.setVerticalTextPosition(AbstractButton.CENTER);
		button.setHorizontalTextPosition(AbstractButton.CENTER);
		button.addActionListener(this);
		button.addKeyListener(new GuiKeyAdapter(this));

		paymentTypeButtonGroup.add(button);
		paymentTypePanel.add(button);

		return button;
	}

	private void showNorthBorder() {
		logger.finest("Entered");
		JPanel northPanel = new JPanel(new GridLayout(2, 2)); // 2 row by 2 columns
		add(northPanel, BorderLayout.NORTH);
		northPanel.setPreferredSize(new Dimension(170, 80));
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.cyan),
				BorderFactory.createEmptyBorder(5, 5, 0, 5)));
		inputTextField = new JTextField();
		inputTextField.setFont(new Font("Helvetica", Font.PLAIN, 24));
		inputTextField.addKeyListener(new GuiKeyAdapter(this));
		inputTextField.addMouseListener(new GuiMouseAdapter(this));
		inputTextField.setFocusTraversalKeysEnabled(false); // disable tab.
															// https://stackoverflow.com/questions/8275204/how-can-i-listen-to-a-tab-key-pressed-typed-in-java
		northPanel.add(inputTextField);

		JPanel orderTypePanel = new JPanel(new GridLayout(1, 4));// 1 row x 4 columns
		northPanel.add(orderTypePanel);
		ButtonGroup orderTypeButtonGroup = new ButtonGroup();

		deliveryRadioButton = createJRadioButton(OrderType.DELIVERY, orderTypeButtonGroup, orderTypePanel);
		phoneInRadioButton = createJRadioButton(OrderType.PHONE_IN, orderTypeButtonGroup, orderTypePanel);
		walkInRadioButton = createJRadioButton(OrderType.WALK_IN, orderTypeButtonGroup, orderTypePanel);
		dineInRadioButton = createJRadioButton(OrderType.DINE_IN, orderTypeButtonGroup, orderTypePanel);

		inputFeedBack = new JLabel();
		northPanel.add(inputFeedBack);

		JPanel paymentTypePanel = new JPanel(new GridLayout(1, 4));
		northPanel.add(paymentTypePanel);
		ButtonGroup paymentTypeButtonGroup = new ButtonGroup();
		cashButton = createJButton(PaymentType.CASH, paymentTypeButtonGroup, paymentTypePanel);
		creditCardButton = createJButton(PaymentType.CREDIT_CARD, paymentTypeButtonGroup, paymentTypePanel);
		checkButton = createJButton(PaymentType.CHECK, paymentTypeButtonGroup, paymentTypePanel);
	}

	private void showWestBorder() {
		logger.finest("Entered");
		JPanel dishTablePanel = new JPanel(new GridLayout(1, 0));
		initDishTable();
		JScrollPane dishTableScrollPane = new JScrollPane();
		dishTableScrollPane.getViewport().add(dishTableView);
		dishTablePanel.add(dishTableScrollPane);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(297, 280)); // 780 x 540 (800 x 600)
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		panel.add(dishTablePanel, BorderLayout.CENTER);

		add(panel, BorderLayout.WEST);
		showFullMenu(0, 0);
	}

	private void showSouthBorder() {
		logger.finest("Entered");
		JPanel panel = new JPanel(new GridLayout(1, 0));
		panel.setPreferredSize(new Dimension(387, 125)); // 125 to display 5 rows for (800 x 600) and (1024 x 768)

		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.magenta),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		initBusinessTable();
		JScrollPane businessTableScrollPane = new JScrollPane();
		businessTableScrollPane.getViewport().add(businessTableView);
		panel.add(businessTableScrollPane);
		add(panel, BorderLayout.SOUTH);
	}

	private void showCenterBorder() {
		logger.finest("Entered");
		JPanel panel = new JPanel(new GridLayout(1, 0));
		panel.setPreferredSize(new Dimension(350, 350));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		initOrderTable();
		JScrollPane orderTableScrollPane = new JScrollPane();
		orderTableScrollPane.getViewport().add(orderTableView);
		panel.add(orderTableScrollPane);
		add(panel, BorderLayout.CENTER);
	}

	private void initDishTable() {
		logger.finest("Entered");
		dishTableRows = new ArrayList<List<String>>();
		dishTableView = new JTable(new GuiTableModel(dishTableRows, FeConstDefs.DISHVIEW_COLUMN_NAMES));
		dishTableView.addMouseListener(new GuiMouseAdapter(this));
		dishTableView.addKeyListener(new GuiKeyAdapter(this));
		dishTableView.getColumn(FeConstDefs.DISHVIEW_CODE).setPreferredWidth(50);
		dishTableView.getColumn(FeConstDefs.DISHVIEW_NAME).setPreferredWidth(140);
		dishTableView.getColumn(FeConstDefs.DISHVIEW_LARGE).setPreferredWidth(45);
		dishTableView.getColumn(FeConstDefs.DISHVIEW_LARGE).setCellRenderer(rightRenderer);
		dishTableView.getColumn(FeConstDefs.DISHVIEW_SMALL).setPreferredWidth(45);
		dishTableView.getColumn(FeConstDefs.DISHVIEW_SMALL).setCellRenderer(rightRenderer);
		dishTableView.getColumn(FeConstDefs.DISHVIEW_CHINESE).setPreferredWidth(140);

		dishTableView.setCellSelectionEnabled(true);
	}

	private void initOrderTable() {
		logger.finest("Entered");
		orderTableRows = new ArrayList<List<String>>();
		orderTableView = new JTable(new GuiTableModel(orderTableRows, FeConstDefs.ORDERVIEW_COLUMN_NAMES));
		orderTableView.addMouseListener(new GuiMouseAdapter(this));
		orderTableView.addKeyListener(new GuiKeyAdapter(this));
		orderTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_INFO).setPreferredWidth(55);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_LARGE).setPreferredWidth(35);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_LARGE).setCellRenderer(rightRenderer);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_SMALL).setPreferredWidth(35);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_SMALL).setCellRenderer(rightRenderer);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_NAME).setPreferredWidth(275);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_TOTAL).setPreferredWidth(65);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_TOTAL).setCellRenderer(rightRenderer);
		orderTableView.getColumn(FeConstDefs.ORDERVIEW_CHINESE).setPreferredWidth(175);
	}

	private void initBusinessTable() {
		logger.finest("Entered");
		businessTableRows = new ArrayList<List<String>>();
		businessTableView = new JTable(new GuiTableModel(businessTableRows, FeConstDefs.BUSVIEW_COLUMN_NAMES));
		businessTableView.addMouseListener(new GuiMouseAdapter(this));
		businessTableView.addKeyListener(new GuiKeyAdapter(this));
		businessTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // AUTO_RESIZE_ALL_COLUMNS

		businessTableView.setShowVerticalLines(true);
		businessTableView.setFont(new Font("Helvetica", Font.PLAIN, 14));
		businessTableView.setRowHeight(18);

		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_ORDERNUMBER).setPreferredWidth(50);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_ORDERNUMBER).setCellRenderer(rightRenderer);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_NAME).setPreferredWidth(140);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_NAME).setCellRenderer(rightRenderer);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_PHONE).setPreferredWidth(120);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_STATUS).setPreferredWidth(70);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_TOTAL).setPreferredWidth(70);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_TOTAL).setCellRenderer(rightRenderer);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_TYPE).setPreferredWidth(70);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_SERVER).setPreferredWidth(75);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_DRIVER).setPreferredWidth(75);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_IN).setPreferredWidth(75);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_IN).setCellRenderer(rightRenderer);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_OUT).setPreferredWidth(75);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_OUT).setCellRenderer(rightRenderer);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_CASHIER).setPreferredWidth(75);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_COUPON).setPreferredWidth(60);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_PERCENTOFF).setPreferredWidth(60);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_VOIDEDITEM).setPreferredWidth(60);
		businessTableView.getColumn(FeConstDefs.BUSINESVIEW_NOTE).setPreferredWidth(125);
		businessTableView.getColumn(FeConstDefs.BUSINESSVIEW_ADDRESS).setPreferredWidth(250);
	}

	private void initArrivalTable() {
		logger.finest("Entered");
		arrivalTableRows = new ArrayList<List<String>>();
		arrivalTableView = new JTable(new GuiTableModel(arrivalTableRows, FeConstDefs.ARIVALVIEW_COLUMN_NAMES));
		arrivalTableView.addMouseListener(new GuiMouseAdapter(this));
		arrivalTableView.addKeyListener(new GuiKeyAdapter(this));
		arrivalTableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // AUTO_RESIZE_ALL_COLUMNS
		arrivalTableView.setShowVerticalLines(true);
		arrivalTableView.setFont(new Font("Helvetica", Font.PLAIN, 16));
		arrivalTableView.setRowHeight(22);

		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_ORDERNUMBER).setPreferredWidth(35);
		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_ORDERNUMBER).setCellRenderer(rightRenderer);
		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_NAME).setPreferredWidth(140);
		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_NAME).setCellRenderer(rightRenderer);
		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_PHONE).setPreferredWidth(120);
		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_STATUS).setPreferredWidth(80);
		arrivalTableView.getColumn(FeConstDefs.ARRIVALVIEW_ARRIVED).setPreferredWidth(75);
	}

	private static String getOrderStatusString(Cart cart) {
		logger.finest("Entered");
		Calculator calculator = new Calculator(cart, Client.menu);
		String statusString = "";

		switch (cart.getOrder().getStatus()) {
		case HOLDING:
		case MAKING:
			statusString = calculator.isBalanceDue() ? "  Bal Due" : "    Open";
			break;
		case PAID:
			statusString = "  Paid - " + calculator.getPayMethod();
			break;
		case VOIDED:
			statusString = "  Void - " + calculator.getPayMethod();
			break;
		default:
			logger.warning("Unhandled order status type=" + cart.getOrder().getStatus());
		}

		return statusString;
	}

	// odd function :). well, it's invoked via a runnable map which needs a function
	// definition.
	private void noAction() {
		logger.finest("Entered");
		logger.finer("noAction was invoked.");
	}

	private boolean isNewCartOrEmptyCartToWarn(String action) {
		logger.finest("Entered");
		if (cart.isNew()) {
			if (cart.getOrderItemTray().getOrderItemQuantity() > 0) {
				setWarningFeedback("Please commit order before " + action);
			} else {
				setWarningFeedback("Please select an order for " + action);
			}
			return true;
		}
		return false;
	}

	// lose unsaved cart content
	private boolean clearOrder() {
		logger.finest("Entered");
		clearInputAndFeedback();

		if (cart.isNew() == false || isCartEmpty()) {
			prepareForNextOrder();
		} else {
			if (JOptionPane.showConfirmDialog(mainFrame, "Clear Order?", "Confirm Message",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				prepareForNextOrder();
			} else {
				setInfoFeedback("Order was NOT cleared");
				return false;
			}
		}

		return true;
	}

	// If want to enhance to show direction from the restaurant to the delivery
	// address, see the link below.
	// https://developers.google.com/maps/documentation/urls/guide
	private boolean showMap() {
		logger.finest("Entered");
		final String WIN_PATH = "rundll32";
		final String WIN_FLAG = "url.dll,FileProtocolHandler";
		final String GOOGLE_MAP_URL = "http://maps.google.com/maps?f=q&hl=en&q=";
		final String GOOGLE_MAP_LEVEL = "&ie=UTF8&z=16&om=1&iwloc=addr";

		String deliveryAddressInUrlFormat = cart.getDeliveryInfo().getGoogleMapUrl();
		if (deliveryAddressInUrlFormat.isEmpty()) {
			setWarningFeedback("No address was specified in this order to display map");
			return false;
		}

		String cmd = WIN_PATH + " " + WIN_FLAG + " " + GOOGLE_MAP_URL + deliveryAddressInUrlFormat + GOOGLE_MAP_LEVEL;
		try {
			Runtime.getRuntime().exec(cmd);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			setWarningFeedback("Unable to display Google map with URL=" + cmd);
			logger.warning(e.getMessage());
			return false;
		}
	}

	private void showOrderHistory() {
		logger.finest("Entered");
		String phoneNumber = cart.getToGoInfo().getPhoneNumber();
		if (phoneNumber.isEmpty() == false && phoneNumber.equals(SharedConstDefs.DEFAULT_PHONE_NUMBER) == false) {
			historyFiltered(phoneNumber);
		} else {
			setWarningFeedback("Enter phone number to find the customer order history");
		}
	}

	private void toggleLargeSmall() {
		logger.finest("Entered");
		if (isModifyingOrderAllowed() == false) {
			// feedback is done in the isModifyingOrderAllowed()
			return;
		}

		if (isOrderTableSelectedRowAnOrderItem() == false) {
			setWarningFeedback("Select a dish item to toggle size");
			return;
		}

		String orderTableInfoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
		OrderItem orderItem = cart.getOrderItemTray().getOrderItem(orderTableInfoColumnValue);
		int dishId = orderItem.getDishId();
		Dish dish = dishes[dishId];

		String orderItemFeedbackInfo = "Order Item " + orderTableInfoColumnValue + " " + dish.getQuotedShortName();
		if (orderItem.getStatus() == OrderItemStatus.VOIDED) {
			setWarningFeedback(orderItemFeedbackInfo + " has been voided and cannot be modified anymore");
			return;
		}

		if (dish.isSmallSizeValid() == false) {
			setWarningFeedback(orderItemFeedbackInfo + " does not have a small to toggle the dish size");
			return;
		}

		if (orderItem.isQuantityValidToToggle() == false) {
			setWarningFeedback(
					orderItemFeedbackInfo + " does not have valid quantity (L=1 or S=1) to toggle the dish size");
			return;
		}

		if (cart.isNew() == false) {
			if (orderItem.getOrderedTime() < cart.getOrder().getCommittedTime()) {
				setWarningFeedback("Please void " + orderItemFeedbackInfo + ", then add the new size instead");
				return;
			} // else newly added orderItem, toggling dish size done below
		}

		orderItem.toggleSize();
		setPromptFeedback("Toggled " + orderItemFeedbackInfo);

		targetDishId = orderItem.getDishId();
		logger.finer("targetDishId=" + targetDishId);

		buildOrderTable();
	}

	private void addOrderTableRow(String rowNumber, String largeQuantity, String smallQuantity, String name,
			String total, String chineseName) {
		logger.finest("Entered");
		List<String> row = new ArrayList<String>();
		row.add(rowNumber);
		row.add(largeQuantity);
		row.add(smallQuantity);
		row.add(name);
		row.add(total);
		row.add(chineseName);
		orderTableRows.add(row);
	}

	private void refreshTotalPanel() {
		logger.finest("Entered");
		if (eastPanel != null) {
			remove(eastPanel);
		}

		eastPanel = new JPanel();

		int total = calculator.getTotal();
		logger.finer("total=" + total);

		List<Cart> arrivedCarts = null;
		try {
			arrivedCarts = server.getCustomerArrivedCarts();
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
		}

		if (total == 0 && cart.isEmpty()) {
			if (arrivedCarts != null && arrivedCarts.size() > 0) {
				eastPanel.setPreferredSize(new Dimension(500, 400));
				JScrollPane arrivedTableScrollPane = new JScrollPane();
				arrivedTableScrollPane.getViewport().add(arrivalTableView);
				eastPanel.add(arrivedTableScrollPane);
				buildArrivalTable(arrivedCarts, Color.BLUE);
			} else {
				eastPanel.setPreferredSize(new Dimension(200, 300));
				eastPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
						BorderFactory.createEmptyBorder(0, 5, 0, 5)));

				JLabel welcomeLabel = new JLabel("Welcome to");
				welcomeLabel.setFont(new Font("Helvetica", Font.PLAIN, 24));
				welcomeLabel.setForeground(Color.red);
				welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				eastPanel.add(welcomeLabel);

				JLabel restaurantNameLabel = new JLabel(Configuration.getRestaurantName());
				restaurantNameLabel.setFont(new Font("Helvetica", Font.PLAIN, 24));
				restaurantNameLabel.setForeground(Color.blue);
				restaurantNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
				eastPanel.add(restaurantNameLabel);

				JLabel restaurantChineseNameLabel = new JLabel(Configuration.getRestaurantChineseName());
				restaurantChineseNameLabel.setFont(new Font("Helvetica", Font.PLAIN, 32));
				restaurantChineseNameLabel.setForeground(Color.blue);
				restaurantChineseNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
				eastPanel.add(restaurantChineseNameLabel);

				JLabel restaurantPhoneLabel = new JLabel(Configuration.getRestaurantPhone());
				restaurantPhoneLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
				restaurantPhoneLabel.setForeground(Color.blue);
				restaurantPhoneLabel.setHorizontalAlignment(SwingConstants.CENTER);
				eastPanel.add(restaurantPhoneLabel);

				eastPanel.setLayout(new GridLayout(6, 0));
			}
		} else {
			int rowCount = 3; // food cost, tax, total
			eastPanel.setPreferredSize(new Dimension(200, 300));
			eastPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
					BorderFactory.createEmptyBorder(0, 0, 0, 0)));
			String padding = "     ";

			if (cart.isNew() == false) {
				JLabel orderNumberLabel = new JLabel(" Ord:  " + cart.getOrder().getOrderNumber());
				orderNumberLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
				orderNumberLabel.setForeground(Color.blue);
				eastPanel.add(orderNumberLabel);
				JLabel orderStatusLabel = new JLabel(cart.getOrder().getStatus().toString() + padding);
				orderStatusLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
				orderStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				orderStatusLabel.setForeground(Color.red);
				eastPanel.add(orderStatusLabel);
				rowCount++;
			} else {
				JLabel newLabel = new JLabel("New  ");
				newLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
				newLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				newLabel.setForeground(Color.red);
				eastPanel.add(newLabel);
				JLabel orderLabel = new JLabel(" Order" + padding);
				orderLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
				orderLabel.setHorizontalAlignment(SwingConstants.LEFT);
				orderLabel.setForeground(Color.red);
				eastPanel.add(orderLabel);
				rowCount++;
			}

			JLabel subtotalLabel = new JLabel(padding + "Subtotal:");
			subtotalLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
			JLabel subtotalLabelAmount = new JLabel(String.format("%4.2f", calculator.getSubtotal()/100.0) + padding);
			subtotalLabelAmount.setHorizontalAlignment(SwingConstants.RIGHT);
			subtotalLabelAmount.setFont(new Font("Helvetica", Font.PLAIN, 18));
			eastPanel.add(subtotalLabel);
			eastPanel.add(subtotalLabelAmount);

			if (cart.getOrder().getDiscountPercent() > 0) {
				JLabel percentDiscountLabel = new JLabel(padding + "Off: " + calculator.getPercentOffInString());
				percentDiscountLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				percentDiscountLabel.setForeground(Color.red);
				eastPanel.add(percentDiscountLabel);
				JLabel percentDiscountAmountLabel = new JLabel(String.format("-%4.2f", calculator.getPercentDiscountAmount()/100.0) + padding);
				percentDiscountAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				percentDiscountAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				percentDiscountAmountLabel.setForeground(Color.red);
				eastPanel.add(percentDiscountAmountLabel);
				rowCount++;

				JLabel taxableLabel = new JLabel(padding + "Taxable:");
				taxableLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				eastPanel.add(taxableLabel);
				JLabel taxableAmountLabel = new JLabel(String.format("%4.2f", (calculator.getSubtotal() - calculator.getPercentDiscountAmount()/100.0)) + padding);
				taxableAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				taxableAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				eastPanel.add(taxableAmountLabel);
				rowCount++;
			}

			JLabel taxLabel = new JLabel(padding + "Tax:");
			taxLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
			eastPanel.add(taxLabel);
			JLabel taxAmountLabel = new JLabel(String.format("%4.2f", calculator.getTaxAmount()/100.0) + padding);
			taxAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			taxAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
			eastPanel.add(taxAmountLabel);

			if (cart.getDeliveryInfo().getDeliveryCharge() > 0 && cart.getOrder().getType() == OrderType.DELIVERY) {
				JLabel deliveryChargeLabel = new JLabel(padding + "Del Charge:");
				deliveryChargeLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				eastPanel.add(deliveryChargeLabel);
				JLabel deliveryChargeAmountLabel = new JLabel(
						String.format("%4.2f", cart.getDeliveryInfo().getDeliveryCharge()/100.0) + padding);
				deliveryChargeAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				deliveryChargeAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
				eastPanel.add(deliveryChargeAmountLabel);
				rowCount++;
			}

			if (cart.getOrder().getAdditionalAmount() > 0) {
				JLabel additionalChargeLabel = new JLabel(padding + "Mis Charge:");
				additionalChargeLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				eastPanel.add(additionalChargeLabel);
				JLabel additionalChargeAmountLabel = new JLabel(
						String.format("%4.2f", cart.getOrder().getAdditionalAmount()/100.0) + padding);
				additionalChargeAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				additionalChargeAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
				eastPanel.add(additionalChargeAmountLabel);
				rowCount++;
			}

			if (cart.getOrder().getDiscountAmount() > 0) {
				JLabel discountLabel = new JLabel(padding + "Coupon:");
				discountLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				discountLabel.setForeground(Color.red);
				eastPanel.add(discountLabel);
				JLabel discountAmountLabel = new JLabel(String.format("-%4.2f", cart.getOrder().getDiscountAmount()/100.0) + padding);
				discountAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				discountAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				discountAmountLabel.setForeground(Color.red);
				eastPanel.add(discountAmountLabel);
				rowCount++;
			}

			JLabel totalLabel = new JLabel(" TOTAL:");
			totalLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
			totalLabel.setForeground(Color.blue);
			eastPanel.add(totalLabel);
			String formattedTotal = String.format("%4.2f", calculator.getTotal()/100.0);
			JLabel totalAmountLabel;
			int fontSize;
			if (formattedTotal.length() >= 6) {
				fontSize = 18;
			} else {
				fontSize = 28;
			}
			if (formattedTotal.length() > 5) {
				totalAmountLabel = new JLabel(formattedTotal);
			} else {
				totalAmountLabel = new JLabel(formattedTotal + "  ");
			}
			totalAmountLabel.setHorizontalAlignment(SwingConstants.LEFT);
			totalAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
			totalAmountLabel.setForeground(Color.blue);
			eastPanel.add(totalAmountLabel);

			int cashTendered = cart.getPaymentTray().getTotalTendered(PaymentType.CASH);
			if (cashTendered > 0) {
				JLabel cashTenderedLabel = new JLabel(padding + PaymentType.CASH.getDisplayName() + ":");
				cashTenderedLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				cashTenderedLabel.setForeground(Color.blue);
				eastPanel.add(cashTenderedLabel);
				JLabel cashTenderedAmountLabel = new JLabel(String.format("%4.2f", cashTendered/100.0) + padding);
				cashTenderedAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				cashTenderedAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				cashTenderedAmountLabel.setForeground(Color.blue);
				eastPanel.add(cashTenderedAmountLabel);
				rowCount++;
			}

			int creditCartTendered = cart.getPaymentTray().getTotalTendered(PaymentType.CREDIT_CARD);
			if (creditCartTendered > 0) {
				JLabel creditCartTenderedLabel = new JLabel(padding + PaymentType.CREDIT_CARD.getDisplayName() + ":");
				creditCartTenderedLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				creditCartTenderedLabel.setForeground(Color.blue);
				eastPanel.add(creditCartTenderedLabel);
				JLabel creditCartTenderedAmountLabel = new JLabel(String.format("%4.2f", creditCartTendered/100.0) + padding);
				creditCartTenderedAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				creditCartTenderedAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				creditCartTenderedAmountLabel.setForeground(Color.blue);
				eastPanel.add(creditCartTenderedAmountLabel);
				rowCount++;
			}

			int checkTendered = cart.getPaymentTray().getTotalTendered(PaymentType.CHECK);
			if (checkTendered > 0) {
				JLabel checkTenderedLabel = new JLabel(padding + PaymentType.CHECK.getDisplayName() + ":");
				checkTenderedLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
				checkTenderedLabel.setForeground(Color.blue);
				eastPanel.add(checkTenderedLabel);
				JLabel checkTenderedAmountLabel = new JLabel(String.format("%4.2f", checkTendered/100.0) + padding);
				checkTenderedAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				checkTenderedAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				checkTenderedAmountLabel.setForeground(Color.blue);
				eastPanel.add(checkTenderedAmountLabel);
				rowCount++;
			}

			if (cart.getPaymentTray().getTotalTendered() > 0) {
				JLabel changeLabel, changeAmountLabel;
				if (cart.getPaymentTray().getTotalTendered() - calculator.getTotal() < 0) { // Balance Due
					changeLabel = new JLabel(" Bal-Due:");
					changeLabel.setForeground(Color.green);
					changeAmountLabel = new JLabel(String.format("%4.2f", (calculator.getTotal() - cart.getPaymentTray().getTotalTendered())/100.0));
					changeAmountLabel.setForeground(Color.green);
				} else { // No Change
					changeLabel = new JLabel(" CHANGE:");
					changeLabel.setForeground(Color.red);
					String changeAmountString = String.format("%4.2f", (cart.getPaymentTray().getTotalTendered() - calculator.getTotal())/100.0);
					if (cart.getPaymentTray().getTotalTendered() - calculator.getTotal() != 0) {
						changeAmountString += "-"; // with Change
					}
					changeAmountLabel = new JLabel(changeAmountString);
					changeAmountLabel.setForeground(Color.red);
				}

				changeLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
				eastPanel.add(changeLabel);
				changeAmountLabel.setHorizontalAlignment(SwingConstants.LEFT);
				changeAmountLabel.setFont(new Font("Helvetica", Font.PLAIN, 28));
				eastPanel.add(changeAmountLabel);
				rowCount++;
			}

			eastPanel.setLayout(new GridLayout(rowCount, 2));
		}

		add(eastPanel, BorderLayout.EAST);
		validate();
	}

	private boolean isOrderTableSelectedRowAcustomizer() {
		logger.finest("Entered");
		// expect xx.yy format without Exchanger.EX
		String infoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
		int periodIndex = infoColumnValue.indexOf('.');
		return ((periodIndex > 0) && (periodIndex + 1 < infoColumnValue.length())
				&& infoColumnValue.endsWith(Exchanger.EX) == false);
	}

	private boolean isOrderTableSelectedRowAnExchanger() {
		logger.finest("Entered");
		// expect xx.yy format with Exchanger.EX
		String infoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);
		int periodIndex = infoColumnValue.indexOf('.');
		return ((periodIndex > 0) && (periodIndex + 1 < infoColumnValue.length())
				&& infoColumnValue.endsWith(Exchanger.EX));
	}

	private boolean isOrderTableSelectedRowApayment() {
		logger.finest("Entered");
		String infoColumnValue = getOrderTableColumnValue(FeConstDefs.ORDERVIEW_INFO);

		if (infoColumnValue.startsWith(PAYMENT_LABEL)) {
			// expected format is "Pymt paymentId" where paymentId is 1 based
			String[] stringArray = infoColumnValue.split(" ");
			if (stringArray.length == 2) {
				String paymentId = stringArray[1];
				if (paymentId.matches("[\\d]+")) {
					return true;
				}
			}
		}

		return false;
	}

	private String getSelectedPhoneNumberInBusinessTable() {
		logger.finest("Entered");
		int phoneColumnIndex = getBusinessTableViewColumnIndex(FeConstDefs.BUSINESSVIEW_PHONE);
		int selectedRow = businessTableView.getSelectedRow();
		String phoneNumber = (String) businessTableView.getValueAt(selectedRow, phoneColumnIndex);

		return phoneNumber.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "").trim();
	}

	private List<Cart> getOutstandingCarts() {
		logger.finest("Entered");
		try {
			return server.getOutstandingCarts();
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
			return null;
		}
	}

	private void setNonPrintingActionHandlers() throws Exception {
		logger.finest("Entered");
		nonPrintingActionToRunable.put(ConfigKeyDefs.noAction, () -> noAction());
		nonPrintingActionToRunable.put(ConfigKeyDefs.showMap, () -> showMap());
		nonPrintingActionToRunable.put(ConfigKeyDefs.showHelp, () -> showHelp());
		nonPrintingActionToRunable.put(ConfigKeyDefs.showOrderHistory, () -> showOrderHistory());
		nonPrintingActionToRunable.put(ConfigKeyDefs.toggleDishSize, () -> toggleLargeSmall());
		nonPrintingActionToRunable.put(ConfigKeyDefs.clearOrder, () -> clearOrder());
		nonPrintingActionToRunable.put(ConfigKeyDefs.commitOrder, () -> sendCartToServer());
		nonPrintingActionToRunable.put(ConfigKeyDefs.stashOrder, () -> stashCartToServer());
		nonPrintingActionToRunable.put(ConfigKeyDefs.refreshUnpaidOrders,
				() -> buildBusinessTable(getOutstandingCarts(), Color.blue, false));
		nonPrintingActionToRunable.put(ConfigKeyDefs.refreshUnsettledOrders,
				() -> buildBusinessTable(getUnsettledCarts(), Color.red, true));
		nonPrintingActionToRunable.put(ConfigKeyDefs.refreshStashedOrders,
				() -> buildBusinessTable(getStashedCarts(), Color.blue, false));
		nonPrintingActionToRunable.put(ConfigKeyDefs.prepareForNextOrder, () -> prepareForNextOrder());

		for (String action : nonPrintingActionToRunable.keySet()) {
			if (Configuration.getNonPrintingActionOptions().contains(action) == false) {
				String err = "Client is handling Action: '" + action + "' but is not configured. Please update "
						+ Configuration.CONFIG_YAML_FILE;
				logger.severe(err);
				throw new Exception(err);
			}
		}

		for (String action : Configuration.getNonPrintingActionOptions()) {
			if (nonPrintingActionToRunable.keySet().contains(action) == false) {
				String err = "Configured Action: '" + action
						+ "' is not been handled by the Client. Add to the nonPrintingActionToRunable map above.";
				logger.warning(err);
				throw new Exception(err);
			}
		}
	}

	private void handleNotification(NotificationMessage<?> notificationMessage) throws Exception {
		var notificationType = notificationMessage.getType();
		switch (notificationType) {
		case REFRESH_UNPAID_ORDER:
			refreshUnpaidOrders();
			break;
		case PRINT_HELD_ORDER:
			Integer orderNumber = (Integer) notificationMessage.getPayload();
			printHeldOrder(orderNumber);
			break;
		case SERVER_SHUTTINGDOWN:
			running = false;
			JOptionPane.showMessageDialog(this, "Server has shutdown. Exiting", "Information", JOptionPane.OK_OPTION);
			System.exit(0);
			break;
		default:
			logger.warning("Unhandled. notificationMessage=" + notificationMessage);
			break;
		}
	}

	private void refreshUnpaidOrders() throws RemoteException {
		logger.finest("Entered");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				buildBusinessTable(getOutstandingCarts(), Color.blue, false);

				List<Cart> arrivedCarts = null;
				try {
					arrivedCarts = server.getCustomerArrivedCarts();
				} catch (RemoteException e) {
					e.printStackTrace();
					logger.warning("Exception: " + e.getMessage());
					return;
				}
				buildArrivalTable(arrivedCarts, Color.blue);
			}
		});
	}

	private void printHeldOrder(Integer orderNumber) throws Exception {
		logger.finest("Entered");
		List<Cart> carts = server.findCartByOrderNumber(orderNumber);
		if (carts.isEmpty()) {
			throw new RuntimeException("Server unable to find held orderNumber= " + orderNumber);
		}

		if (carts.size() > 1) {
			logger.info("carts=" + carts.toString());
			throw new RuntimeException("Expect one order but server found " + carts.size() + " orders with orderNumber= " + orderNumber);
		}

		Cart heldCart = carts.get(0);
		logger.info("heldCart=" + heldCart);

		Calculator calculator = new Calculator(heldCart, Client.menu);

		String configuredEvent = ConfigKeyDefs.paidPrintOrderToKitchen;
		Map<String, List<Map<String, String>>> configuredEventToActionAttributes = Configuration
				.getConfiguredEventToActionAttributes();
		List<Map<String, String>> actionAttributes = configuredEventToActionAttributes.get(configuredEvent);
		logger.fine("configuredEvent=" + configuredEvent + " actionAttributes=" + actionAttributes);
		if (actionAttributes == null) {
			throw new Exception("Undefined event for the configuredEvent: " + configuredEvent
					+ ". Check if it is defined in the config.yaml file.");
		}

		for (Map<String, String> actionAttribute : actionAttributes) {
			String actionName = actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_action);
			logger.fine("actionName=" + actionName);

			Map<String, Map<String, String>> toGoPrinterLocationToPrinter = Configuration
					.getToGoPrinterLocationToPrinter();
			String printerLocation = actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_location);
			PrintService printService = printerLocation == null ? defaultPrintService
					: AvailablePrintService.getPrintService(toGoPrinterLocationToPrinter.get(printerLocation)
							.get(ConfigKeyDefs.printerLocationToPrinter_name));

			if (Configuration.getPrintOrderActionOptions().contains(actionName)) {
				logger.fine("handling PrintOrderActionOptions=" + actionName + " printerLocation=" + printerLocation);
				PrintOrderType printOrderType = PrintOrderType.getEnum(actionName);
				PrintOrderHandler printOrderHandler = new PrintOrderHandler(printerLocation, printService);
				printOrderHandler.print(printOrderType,
						FontSizeType
								.getEnum(actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
						this, heldCart, calculator);
			} else if (Configuration.getPrintOrderNoPriceActionOptions().contains(actionName)) {
				logger.fine("handling PrintOrderNoPriceActionOptions=" + actionName + " printerLocation="
						+ printerLocation);
				PrintOrderNoPriceHandler printOrderNoPriceHandler = new PrintOrderNoPriceHandler(printerLocation,
						printService);
				printOrderNoPriceHandler.print(PrintOrderNoPriceType.getEnum(actionName),
						FontSizeType
								.getEnum(actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
						this, heldCart, calculator);
			} else if (Configuration.getPrintToGoPerStationActionOptions().contains(actionName)) {
				for (int dishWorkstationId = 1; dishWorkstationId <= menu.getToGoStationCount(); ++dishWorkstationId) {
					boolean dishWorkstationHasWork = false;
					for (OrderItem orderItem : heldCart.getOrderItemTray().getOrderItems()) {
						Dish dish = dishes[orderItem.getDishId()];
						if (dish.getToGoStationHasWork(dishWorkstationId)) {
							// voided orderItems are printed to notify of their cancellation.
							logger.fine("There's work for dishWorkstationId=" + dishWorkstationId + " dish=" + dish);
							dishWorkstationHasWork = true;
							break;
						}
					}

					if (dishWorkstationHasWork) {
						printerLocation = Configuration.getToGoDishWorkstationIdToPrinterLocation()
								.get(dishWorkstationId);
						if (printerLocation == null) {
							throw new RuntimeException(
									"Missing printerLocation for ToGo workstation id: " + dishWorkstationId);
						}
						PrintService toGoPrintService = AvailablePrintService
								.getPrintService(toGoPrinterLocationToPrinter.get(printerLocation)
										.get(ConfigKeyDefs.printerLocationToPrinter_name));
						PrintToGoPerStationHandler printToGoPerStationHandler = new PrintToGoPerStationHandler(
								printerLocation, toGoPrintService);
						PrintToGoPerStationType printToGoPerStationType = PrintToGoPerStationType.getEnum(actionName);
						logger.fine("handling PrintToGoPerStationActionOptions: " + actionName
								+ ", printToGoPerStationType: " + printToGoPerStationType + " printerName="
								+ printService.getName() + " printerLocation=" + printerLocation + " dishWorkstationId="
								+ dishWorkstationId);
						printToGoPerStationHandler.print(printToGoPerStationType,
								FontSizeType.getEnum(
										actionAttribute.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize)),
								this, heldCart, calculator);
					} else {
						logger.finer("No printing needed for printerLocation: " + printerLocation
								+ " as it has no work from this order.");
					}
				}
			} else {
				throw new RuntimeException("Unexpected action: '" + actionName
						+ "' is not handled. Check config.yaml and Configuration.java that the action is a valid action.");
			}
		}
	}

	private void addClientClosingListener(Client client) {
		logger.finest("Entered");
		mainFrame.addWindowListener(new WindowAdapter() {
			// Take action when window is closing
			public void windowClosing(WindowEvent e) {
				// commented out this pop-up as they can hide behind other windows and cannot
				// get to easily.
				// if (JOptionPane.showConfirmDialog(client, "Are you sure you want to exit?",
				// "Confirm Message", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				running = false;

				mainFrame.setVisible(false);
				mainFrame.dispose();
				String exitingMsg = "Client exiting at " + LocalDateTime.now();
				logger.info(exitingMsg);
				System.out.println(exitingMsg);
				System.exit(0);
				// }
			}
		});
	}

	private boolean isRequestedTimeTooEarly() {
		logger.finest("Entered");
		updateEstimatedTime(0);
		int adjustmentMinutes = 5;
		long adjustedEstimatedTime = (cart.getToGoInfo().getQueueTimeMinutes() - adjustmentMinutes) * 60 * 1000
				+ System.currentTimeMillis();
		long requestedTime = cart.getToGoInfo().getRequestedTime();

		if (requestedTime > 0) {
			return adjustedEstimatedTime > requestedTime;
		} else {
			return false;
		}
	}

	private static void logAvailablePrintServices() {
		logger.finest("Entered");
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		if (printServices.length == 0) {
			logger.severe("No printing service found. Will not be able to print.");
		} else {
			for (PrintService printService : printServices) {
				logger.config("Available printing service name: " + printService.getName());
			}
		}
	}

	private static void setDefaultPrintService(String args[]) {
		logger.finest("Entered");
		String defaultPrinterName = args.length >= 2 ? args[1] : "";
		if (defaultPrinterName == null || defaultPrinterName.isEmpty()) {
			Map<String, Map<String, String>> toGoPrinterLocationToPrinter = Configuration
					.getToGoPrinterLocationToPrinter();
			if (toGoPrinterLocationToPrinter == null) {
				defaultPrintService = AvailablePrintService.getDefaultPrintService();
				if (defaultPrintService == null) {
					logger.warning("Unable to get OS default printer for default printing.");
				} else {
					logger.fine("Using OS default printer for default printing.");
				}
			} else {
				defaultPrinterName = toGoPrinterLocationToPrinter.get(ConfigKeyDefs.defaultStation)
						.get(ConfigKeyDefs.printerLocationToPrinter_name);
				defaultPrintService = AvailablePrintService.getPrintService(defaultPrinterName);
				if (defaultPrintService == null) {
					logger.warning("Unable to get conf.yaml configured default printer for default printing.");
				} else {
					logger.fine("Using conf.yaml configured default printer for default printing.");
				}
			}
		} else {
			defaultPrintService = AvailablePrintService.getPrintService(defaultPrinterName);
			if (defaultPrintService == null) {
				logger.warning("Unable to get client command line start up default printer for default printing.");
			} else {
				logger.fine("Using client command line default printer for default printing");
			}
		}
	}

	public static void main(String args[]) throws Exception {
		logger.finest("Entered");
		logger.info("Starting Client for " + Configuration.getRestaurantName() + " "
				+ Configuration.getRestaurantChineseName());
		logger.config("classpath=" + System.getProperty("java.class.path"));
		logger.config("logging.properties file=" + loggingConfigFile);

		for (String arg : args) {
			logger.config("Client command line argument=" + arg);
		}

		String host = (args.length < 1) ? null : args[0];
		if (host != null) {
			logger.config("Host='" + host + "' is used to locate registry.");
		}

		Registry registry = null;
		try {
			// Returns a reference to the remote object Registry on the specified host on
			// the default registry port of 1099. If host is null, then local host is used.
			registry = LocateRegistry.getRegistry(host);
		} catch (RemoteException e) {
			e.printStackTrace();
			String errorMessage = "Exiting. Connect to RMI sever failed. Make sure server is running. Exception: " + e.getMessage();
			System.out.println(errorMessage);
			logger.severe(errorMessage);
			System.exit(-10);
		}

		logger.info("Located server registry=" + registry);

		try {
			server = (ServerInterface) registry.lookup(Server.RMI_NAME_STRING);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			logger.severe("Exiting. Registry lookup failed. Make sure server is running. Exception: " + e.getMessage());
			System.exit(-20);
		}

		logger.config("Client found serverInterface server=" + server);

		int retryCounter = 0;
		int maxRetries = 5;
		while (retryCounter < maxRetries) {
			try {
				logger.config("Getting data from server to prepare Client GUI. retryCounter=" + retryCounter);
				menu = server.getMenu();
			} catch (RemoteException e) {
				e.printStackTrace();
				logger.warning("retryCounter=" + retryCounter + " Exception: " + e.getMessage());
				retryCounter++;
				continue;
			}

			dishes = menu.getDishes();
			sortedByCodeActiveDishes = new ArrayList<Dish>(dishes.length);
			for (Dish dish : dishes) {
				if (dish.isActive()) {
					sortedByCodeActiveDishes.add(dish);
				}
			}
			Collections.sort(sortedByCodeActiveDishes);

			logger.config("Client has the menu and related data from server. retryCounter=" + retryCounter);

			// When a new employee is added, the server does not need to be restarted.
			// Restart of the client which invoke a refresh will update to include all the
			// employees. The getEmployee(int) function is called
			// when building the GUI order table which refreshes the idToEmployee
			// for any newly added employee and used.
			// When idToEmployee is passed in to Printer,
			// it should have all the employees.
			// Note idToEmployee contains all employees including inactive ones.
			// It is expected the number of employee is small so that when print any
			// historical order, the employee name still shows properly.
			try {
				idToEmployee = server.refreshIdToEmployee();
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("retryCounter=" + retryCounter + " refreshIdToEmployee() Exception: " + e.getMessage());
				retryCounter++;
				continue;
			}
			logger.config("Client has the all the employees. retryCounter=" + retryCounter);

			try {
				initialsToActiveEmployee = server.refreshInitialsToActiveEmployee();
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("retryCounter=" + retryCounter + " refreshInitialsToActiveEmployee() Exception: "
						+ e.getMessage());
				retryCounter++;
				continue;
			}
			logger.config("Client has all the employee initials mapping to employee. retryCounter=" + retryCounter);

			if (retryCounter >= maxRetries) {
				logger.severe("Exceed maxRetries=" + maxRetries);
				System.exit(-30);
			} else {
				break; // Got all the needed data from server. Can break out of the loop now.
			}
		}

		logger.config("Client has all the needed data from server. Starting the GUI main frame.");

		Client client = new Client();

		if (client.authenticateClientUser(mainFrame) == false) {
			logger.config("Authentication canceled or failed.");
			System.exit(-40);
		}

		// Set the first order's takenById. It's too early at prepareForNextOrder()
		// when first called at the Client() constructor.
		client.cart.getOrder().setTakenById(authenticatedEmployee.getEmployeeId());
		client.setPromptFeedback(client.cart.getOrder().getType().getDisplayName() + ":");
		mainFrame.setTitle("(login: " + authenticatedEmployee.getNickname() + ") " + mainFrame.getTitle());

		client.addClientClosingListener(client);

		client.setNonPrintingActionHandlers();

		logAvailablePrintServices();

		setDefaultPrintService(args);

		printDefaultHandler = new PrintDefaultHandler(ConfigKeyDefs.defaultStation, defaultPrintService);

		try {
			connectToServerForNotification(host);
			logger.config("Client is ready for use.");
		} catch (Exception e) {
			logger.config("First time connectToServerForNotification Exception: " + e.getMessage());
			// Retry in the while (running) loop
		}

		while (running) {
			logger.config("In loop waiting for notification from server.");
			try {
				var inputStream = serverSocket.getInputStream();
				var objectInputStream = new ObjectInputStream(inputStream);
				logger.info("Waiting for server notifications.");
				var notificationMessage = (NotificationMessage<?>) objectInputStream.readObject();
				logger.info("Received notifications from server. notificationMessage=" + notificationMessage);
				client.handleNotification(notificationMessage);
			} catch (Exception e) {
				logger.info("Waiting for notification Exception: " + e.getMessage());
				if (running) {
					logger.info("Client is still running. Try to connect to server for notification again");
					try {
						connectToServerForNotification(host);
					} catch (Exception ee) {
						logger.info("Failed to connect to server for notification. host=" + host + ". Exception=" + ee.getMessage());
						running = false;
						JOptionPane.showMessageDialog(client, "Connection to server for notification is not available. Exiting", "Information", JOptionPane.OK_OPTION);
						System.exit(0);
					}
				} else {
					try {
						serverSocket.close();
					} catch (IOException ex) {
					}
					logger.info("Closed: " + serverSocket);
				}
			}
		}
	}
}
