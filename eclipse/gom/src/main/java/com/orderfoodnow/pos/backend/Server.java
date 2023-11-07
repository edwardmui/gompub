package com.orderfoodnow.pos.backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.CustomerStatus;
import com.orderfoodnow.pos.shared.CustomizerTray;
import com.orderfoodnow.pos.shared.DataSource;
import com.orderfoodnow.pos.shared.ExchangerTray;
import com.orderfoodnow.pos.shared.NotificationMessage;
import com.orderfoodnow.pos.shared.NotificationType;
import com.orderfoodnow.pos.shared.OrderItemTray;
import com.orderfoodnow.pos.shared.OrderStatus;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PaymentTray;
import com.orderfoodnow.pos.shared.SharedConstDefs;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.Menu;
import com.orderfoodnow.pos.shared.order.CustomerProfile;
import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.CustomizerTable;
import com.orderfoodnow.pos.shared.order.DeliveryInfo;
import com.orderfoodnow.pos.shared.order.DeliveryInfoTable;
import com.orderfoodnow.pos.shared.order.DineInInfo;
import com.orderfoodnow.pos.shared.order.DineInInfoTable;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.ExchangerTable;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.order.OrderItemTable;
import com.orderfoodnow.pos.shared.order.OrderTable;
import com.orderfoodnow.pos.shared.order.Payment;
import com.orderfoodnow.pos.shared.order.PaymentTable;
import com.orderfoodnow.pos.shared.order.Settle;
import com.orderfoodnow.pos.shared.order.ToGoInfo;
import com.orderfoodnow.pos.shared.order.ToGoInfoTable;
import com.orderfoodnow.pos.shared.staff.Employee;
import com.orderfoodnow.pos.shared.staff.EmployeeTable;

public class Server implements ServerInterface {
	public static final String RMI_NAME_STRING = "Server";
	// IANA suggests the range 49152 to 65535 (215+214 to 216−1) for dynamic or
	// private ports
	public static final int SERVER_TCP_PORT = 65168;
	private static ServerSocket serverSocket;
	private static List<Socket> clientSockets = new ArrayList<>();

	private static Menu menu;
	private static List<Cart> unsettledCarts; // all unsettled carts
	private static Map<String, Cart> stashedCarts = new HashMap<>(); // all stashed carts, not persisted in db.
	private static Map<Integer, Employee> idToEmployee = new HashMap<>();
	private static Map<String, Employee> initialsToActiveEmployee = new HashMap<>();

	// Record the latest time of the orderItemQuantity was calculated
	private static long latestOrderItemQuantityTally;
	// Result of the latest orderItemQuantity
	private static int[] allOrderIntervalTotals = new int[ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE];

	// One idea of orderNumber is to just use the last 3 digits of the orderId,
	// Another idea is to only have 3 digits to the orderNumber and take the last 3
	// digit of the orderId is the offset and subtract it when the server starts.
	// If the offset approach is used, that's the same as setting the orderNumber to
	// 0 as below.
	private static int orderNumber;

	private static boolean running = true; // flag to control the server is running
	private static Registry registry;

	private static String loggingConfigFile;
	private static Logger logger;
	static {
		// if the logging properties file is not provided in the command line as a VM
		// argument as follow,
		// then use the one in the resources folder
		// -Djava.util.logging.config.file="src/test/resources/conf/serverLogging.properties"
		loggingConfigFile = System.getProperty("java.util.logging.config.file");
		if (loggingConfigFile == null) {
			URL url = Server.class.getClassLoader().getResource("conf/serverLogging.properties");
			if (url == null) {
				System.out.println("Fail to read the default conf/serverLogging.properties file. ");
				System.exit(-1);
			}
			loggingConfigFile = url.getFile();
			try {
				LogManager.getLogManager().readConfiguration(new FileInputStream(loggingConfigFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger = Logger.getLogger(Server.class.getName());
	}

	public Server(Connection connection) throws RemoteException, SQLException {
		logger.finest("Entered");
		orderNumber = OrderTable.readNextOrderNumber(connection); // get the biggest order numbers that's unsettled.
		unsettledCarts = getUnsettledOrdersFromDB(connection);
		idToEmployee = refreshIdToEmployee(connection);
		initialsToActiveEmployee = refreshInitialsToActiveEmployee(connection);
	}

	public Employee authenticateUser(String userId, String password) throws RemoteException, SQLException {
		logger.finest("Entered");
		try (Connection connection = DataSource.getConnection()) {
			for (Employee employee : EmployeeTable.readAllOfActive(connection)) {
				if (userId.equals(employee.getUserId())) {
					if (password.equals(employee.getPassword())) {
						return employee;
					}
				}
			}

		}
		return null;
	}

	public Menu getMenu() throws RemoteException {
		logger.finest("Entered");
		return menu;
	}

	public long getTimestamp() throws RemoteException {
		logger.finest("Entered");
		return System.currentTimeMillis();
	}

	public Map<Integer, Employee> refreshIdToEmployee() throws RemoteException, SQLException {
		logger.finest("Entered");
		try (Connection connection = DataSource.getConnection()) {
			refreshIdToEmployee(connection);
		}

		return idToEmployee;
	}

	public Map<String, Employee> refreshInitialsToActiveEmployee() throws SQLException {
		logger.finest("Entered");
		try (Connection connection = DataSource.getConnection()) {
			refreshInitialsToActiveEmployee(connection);
		}

		return initialsToActiveEmployee;
	}

	public synchronized Cart processCart(Cart cart) throws RemoteException, SQLException {
		logger.finest("Entered");
		logger.finer(cart.toString());
		try (Connection connection = DataSource.getConnection()) {
			// set autoCommit to off to allow atomic commit of all data in the cart. Any
			// failure results in no commit to database.
			connection.setAutoCommit(false);

			String phoneNumber = cart.getToGoInfo().getPhoneNumber();
			if (phoneNumber.isEmpty() == false) {
				// new customerId is auto generated by DB and only available after
				// customer_profile insert.
				int customerId = harmonizeCustomerProfile(connection, cart);
				if (customerId != ToGoInfo.INVALID_CUSTOMER_ID && customerId != cart.getToGoInfo().getCustomerId()) {
					cart.getToGoInfo().setCustomerId(customerId); // set new customerId before inserting
				}

				if (stashedCarts.containsKey(phoneNumber)) {
					stashedCarts.remove(phoneNumber);
				}
			}

			Order order = cart.getOrder();
			long currentTimeMillis = System.currentTimeMillis();
			order.setCommittedTime(currentTimeMillis);

			if (cart.isNew()) {
				order.setOrderedTime(currentTimeMillis);
				order.setOrderNumber(++orderNumber);
				cart.insert(connection);
			} else {
				int orderId = cart.getOrder().getOrderId();
				Order orderInDB = Order.readById(connection, orderId);
				Cart cartInDB = fillCart(connection, orderInDB);
				logger.finer("cartInDB: " + cartInDB);
				logger.finer("cartCurr: " + cart);
				if (cart.equals(cartInDB) == false) {
					cart.update(connection);
					logger.fine("updated cart.");
				} else {
					logger.fine("no cart updated needed.");
				}
			}

			connection.commit();
			connection.setAutoCommit(true);
			logger.fine("committed: " + cart.toString());

			updateCachedOrders(cart);
			logger.finer("updated cached orders");

			notifyClients(new NotificationMessage<String>(NotificationType.REFRESH_UNPAID_ORDER, ""));
			logger.finer("updated all clients");

			return cart;
		}
	}

	public synchronized void stashCart(Cart cart) throws RemoteException {
		logger.finest("Entered");
		// Can only have one stashed cart per phone number
		String phoneNumber = cart.getToGoInfo().getPhoneNumber();
		if (phoneNumber.isEmpty() == false) {
			Cart tmpCart = stashedCarts.put(phoneNumber, cart);
			if (tmpCart == null) {
				logger.fine("stashed new cart: " + cart.toString());
			} else {
				logger.fine("replace stashed cart: " + cart.toString());
			}
		}
	}

	public List<Cart> findCartByOrderNumber(int orderNumber) throws RemoteException {
		logger.finest("Entered");
		// Even though there is is only one matching cart,
		// returns a list to easier handling along with other findCartBy* methods.
		List<Cart> carts = new ArrayList<>();

		for (Cart cart : unsettledCarts) {
			if (cart.getOrder().getOrderNumber() == orderNumber) {
				carts.add(cart);
				break;
			}
		}

		return carts;
	}

	public List<Cart> findCartByCustomerName(String searchingName) throws RemoteException {
		logger.finest("Entered");
		logger.fine("searchingName=" + searchingName);
		List<Cart> carts = new ArrayList<>();

		for (Cart cart : unsettledCarts) {
			if (cart.getToGoInfo().getCustomerName().equalsIgnoreCase(searchingName)) {
				carts.add(cart);
			}
		}

		if (carts.size() == 0) {
			// no exact match. Look for dine-in orders by table name
			for (Cart cart : unsettledCarts) {
				if (cart.getOrder().getType() == OrderType.DINE_IN) {
					switch (cart.getOrder().getStatus()) {
					case MAKING:
					case HOLDING:
						if (cart.getDineInInfo().getTableNumber().equalsIgnoreCase(searchingName)) {
							carts.add(cart);
						}
						break;
					default:
						break;
					}
				}
			}
		}

		if (carts.size() == 0) {
			// no exact match. Try partial ignore case
			String searchingNameUpper = searchingName.toUpperCase();
			for (Cart cart : unsettledCarts) {
				String customerNameUpper = cart.getToGoInfo().getCustomerName();
				if (customerNameUpper.contains(searchingNameUpper)) {
					carts.add(cart);
				}
			}
		}

		// oldest order first
		Collections.sort(carts);
		logger.fine("Found carts: " + carts.toString());

		return carts;
	}

	// Find orders by the specified partial or complete phone number.
	public List<Cart> findCartByPhoneNumber(String searchingPhoneNumber) throws RemoteException {
		logger.finest("Entered");
		logger.fine("searchingPhoneNumber=" + searchingPhoneNumber);
		int searchingPhoneNumberLength = searchingPhoneNumber.length();
		List<Cart> carts = new ArrayList<>();
		for (Cart cart : unsettledCarts) {
			String orderPhoneNumber = cart.getToGoInfo().getPhoneNumber();
			int orderPhoneNumberLength = orderPhoneNumber.length();
			if (orderPhoneNumberLength >= searchingPhoneNumberLength) {
				String last4Digit = orderPhoneNumber.substring(orderPhoneNumberLength - searchingPhoneNumberLength,
						orderPhoneNumberLength);
				if (last4Digit.equals(searchingPhoneNumber)) {
					carts.add(cart);
				}
			}
		}

		if (carts.size() == 0) {
			// no exact match. Try partial
			for (Cart cart : unsettledCarts) {
				if (cart.getToGoInfo().getPhoneNumber().contains(searchingPhoneNumber)) {
					carts.add(cart);
				}
			}
		}

		// oldest order first
		Collections.sort(carts);

		// Collections.sort(carts, Collections.reverseOrder());

		logger.fine("Found carts: " + carts.toString());

		return carts;
	}

	public Cart findStashedCartByPhoneNumbe(String searchingPhoneNUmber) throws RemoteException {
		logger.finest("Entered");
		logger.fine("searchingPhoneNUmber=" + searchingPhoneNUmber);
		return stashedCarts.get(searchingPhoneNUmber);
	}

	public List<Cart> getOrderingHistory(String phoneNumber, int orderCount) throws RemoteException, SQLException {
		logger.finest("Entered");
		List<Cart> carts = new ArrayList<>();
		try (Connection connection = DataSource.getConnection()) {
			List<Integer> orderIds = ToGoInfoTable.readHistory(connection, phoneNumber, orderCount);

			logger.finer("Order history orderIds=" + orderIds);

			List<Order> orders = Order.readHistoryOrders(connection, orderIds);

			logger.finer("Order history orders=" + orders);

			for (Order order : orders) {
				Cart cart = fillCart(connection, order);
				carts.add(cart);
			}
		}

		return carts;
	}

	public CustomerProfile findCustomer(String phoneNumber) throws RemoteException, SQLException {
		logger.finest("Entered");
		try (Connection connection = DataSource.getConnection()) {
			return CustomerProfile.readByPhone(connection, phoneNumber);
		}
	}

	public List<Cart> getUnsettledCarts() {
		logger.finest("Entered");
		return unsettledCarts;
	}

	public Map<String, Cart> getStashedCarts() {
		logger.finest("Entered");
		return stashedCarts;
	}

	public List<Cart> getOutstandingCarts() {
		logger.finest("Entered");
		List<Cart> outstandingCarts = new ArrayList<>();
		for (Cart cart : unsettledCarts) {
			switch (cart.getOrder().getStatus()) {
			case HOLDING:
			case MAKING:
				outstandingCarts.add(cart);
				break;
			default:
				break;
			}
		}

		return outstandingCarts;
	}

	public List<Cart> getCustomerArrivedCarts() {
		logger.finest("Entered");
		List<Cart> customerArrivedCarts = new ArrayList<>();
		// Return the last one hour arrival to keep the list short if users are too busy
		// marking the order has been picked up.
		long earlier = System.currentTimeMillis() - (60 * 60 * 1000);
		for (Cart cart : unsettledCarts) {
			ToGoInfo toGoInfo = cart.getToGoInfo();
			if (toGoInfo.getCustomerStatus() == CustomerStatus.ARRIVED && toGoInfo.getArrivalTime() > earlier) {
				customerArrivedCarts.add(cart);
			}
		}

		Collections.sort(customerArrivedCarts, Cart.getCustomerArrivalTimeComparator());
		return customerArrivedCarts;
	}

	// Returns the all the total of all the orders for each 15-minutes interval for
	// ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE buckets
	public int[] getAllOrderIntervalTotals() {
		logger.finest("Entered");
		long currentTime = System.currentTimeMillis();
		final long quantityCalculateFrequency = 60 * 1000; // perform the expensive calculation only once every minute
		if (currentTime - latestOrderItemQuantityTally < quantityCalculateFrequency) {
			return allOrderIntervalTotals;
		}

		latestOrderItemQuantityTally = currentTime;

		Arrays.fill(allOrderIntervalTotals, 0);
		final long bucketSize = 15 * 60 * 1000; // 15 minutes
		long timeMinus[] = new long[ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE];
		timeMinus[0] = System.currentTimeMillis() - bucketSize;
		logger.finer("timeMinus[0]=" + Util.formatTimeNoSeconds(timeMinus[0]));
		for (int i = 1; i < timeMinus.length; ++i) {
			timeMinus[i] = timeMinus[i - 1] - bucketSize;
			logger.finer("timeMinus[" + i + "]=" + Util.formatTimeNoSeconds(timeMinus[i]));
		}

		for (Cart cart : unsettledCarts) {
			long effectiveTime = cart.getToGoInfo().getRequestedTime() == 0 ? cart.getOrder().getOrderedTime()
					: cart.getToGoInfo().getRequestedTime();
			logger.finer("effectiveTime=" + Util.formatTimeNoSeconds(effectiveTime));
			if (effectiveTime < timeMinus[ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE - 1]
					|| cart.getOrder().isVoided()) {
				logger.finer(
						"Not used to calculate effectiveTime=" + Util.formatTimeNoSeconds(effectiveTime) + " " + cart);
				continue;
			}

			for (int i = 0; i < ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE; ++i) {
				if (effectiveTime < timeMinus[i] || (effectiveTime > timeMinus[0])) {
					logger.finer(
							"Used to calculate effectiveTime=" + Util.formatTimeNoSeconds(effectiveTime) + " " + cart);
					allOrderIntervalTotals[i] += new Calculator(cart, menu).getFoodTotal();
					logger.finer("allOrderIntervalTotals[" + i + "]=" + allOrderIntervalTotals[i]);
					break;
				}
			}
		}

		for (int i = 0; i < ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE; ++i) {
			logger.finer("allOrderIntervalTotals[" + i + "]=" + allOrderIntervalTotals[i]);
		}

		return allOrderIntervalTotals;
	}

	synchronized void checkHeldOrder() {
		logger.finest("Entered");
		logger.finer("unsettledCarts.size()=" + unsettledCarts.size());
		for (Cart cart : unsettledCarts) {
			OrderStatus orderStatus = cart.getOrder().getStatus();
			if (orderStatus == OrderStatus.HOLDING) {
				// 0 iso (new Calculator(cart, menus).getTotal()) for the second parameter
				// as HOLDING order is already committed with a requestedTime
				// which is already included in in getAllOrderIntervalTotals()
				int queueTimeMinutes = Calculator.estimateKitchenQueueTimeMinutes(getAllOrderIntervalTotals(), 0);
				long requestedTime = cart.getToGoInfo().getRequestedTime();
				long drivingDurationMinutes = cart.getOrder().getType() == OrderType.DELIVERY
						? cart.getDeliveryInfo().getDrivingDurationMinutes()
						: 0;
				long currentTime = System.currentTimeMillis();
				long countDownInMinutes = ((requestedTime - currentTime) / 1000 / 60) - queueTimeMinutes
						- drivingDurationMinutes;
				String logMsg = "Ready to make order in" + " minutes=" + countDownInMinutes + " base on"
						+ " requestedTime=" + Util.formatTimeNoSeconds(requestedTime) + " drivingDurationMinutes="
						+ (drivingDurationMinutes) + " and kitchen queueTimeMinutes=" + (queueTimeMinutes)
						+ " currentTime=" + Util.formatTimeNoSeconds(currentTime) + " for " + cart;
				if (countDownInMinutes <= 0) {
					logger.fine(logMsg);

					if (clientSockets.isEmpty()) {
						logger.warning("There is not connected client to print the held order. cart=" + cart);
						continue;
					} else {
						Integer orderNumber = cart.getOrder().getOrderNumber();
						@SuppressWarnings({ "rawtypes", "unchecked" })
						NotificationMessage notificationMessage = new NotificationMessage(
								NotificationType.PRINT_HELD_ORDER, orderNumber);
						for (Socket clientSocket : Server.clientSockets) {
							logger.info("Notifying to clientSockt=" + clientSocket + ". msg="
									+ notificationMessage.toString());
							try {
								var outputStream = clientSocket.getOutputStream();
								ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
								objectOutputStream.writeObject(notificationMessage);
								// no exception on sending msg to client. Assume printing was success, proceed
								// to update cart
								logger.info("Notified to clientSockt=" + clientSocket + ". msg="
										+ notificationMessage.toString());
								cart.getOrder().setStatus(OrderStatus.MAKING);
								cart.getToGoInfo().setQueueTimeMinutes(queueTimeMinutes);
								try {
									processCart(cart);
								} catch (SQLException e) {
									e.printStackTrace();
									logger.info("Process cart Exception: " + e.getMessage());
								}
								break;
							} catch (IOException e) {
								// Continue to try the next client.
								logger.info("Fail to notify clientSocket=" + clientSocket + " for msg="
										+ notificationMessage.toString());
							}
						}
					}
				} else {
					logger.fine("NOT " + logMsg);
				}
			}
		}
	}

	public void settleCarts(int employeeId) throws RemoteException, SQLException {
		logger.finest("Entered");
		Settle settle = new Settle(employeeId);
		try (Connection connection = DataSource.getConnection()) {
			settle.insert(connection);

			OrderTable.settleAllUnsettled(connection, settle.getSettleId());

			ToGoInfoTable.eraseCreditCardNumber(connection);

			unsettledCarts.clear();

			notifyClients(new NotificationMessage<String>(NotificationType.REFRESH_UNPAID_ORDER, ""));

			logger.fine("Settled all non-settled carts");
		}
	}

	public void shutdown() throws RemoteException {
		logger.finest("Entered");

		// empty pay load
		notifyClients(new NotificationMessage<String>(NotificationType.SERVER_SHUTTINGDOWN, ""));

		running = false;

		try {
			registry.unbind(RMI_NAME_STRING);
			logger.info("registry.unbind() success");
		} catch (NotBoundException e) {
			e.printStackTrace();
			logger.info("Server shutown exception " + e.getMessage());
			logger.info("Continue exiting. ");
			System.exit(-100);
		}
		UnicastRemoteObject.unexportObject(this, true);

		logger.info("closing socket.");
		if (serverSocket != null && serverSocket.isClosed() == false) {
			try {
				serverSocket.close();
				logger.info("socket.close() success");
			} catch (IOException e) {
				e.printStackTrace();
				logger.info("Server shutown exception " + e.getMessage());
				logger.info("Continue exiting.");
				System.exit(-200);
			}
		}

		logger.info("Server gracefully shutdown.");
	}

	private Map<Integer, Employee> refreshIdToEmployee(Connection connection) throws RemoteException, SQLException {
		logger.finest("Entered");
		idToEmployee.clear();
		// number of employees in a restaurant is expected to be small.
		// read them all in to make things easier.
		List<Employee> employees = EmployeeTable.readAllOf(connection);
		for (Employee employee : employees) {
			idToEmployee.put(employee.getEmployeeId(), employee);
		}

		return idToEmployee;
	}

	private Map<String, Employee> refreshInitialsToActiveEmployee(Connection connection) throws SQLException {
		logger.finest("Entered");
		initialsToActiveEmployee.clear();
		List<Employee> activeEmployees = EmployeeTable.readAllOfActive(connection);
		for (Employee employee : activeEmployees) {
			String initials = employee.getInitials();
			if (initialsToActiveEmployee.containsKey(initials)) {
				logger.severe("Initials of '" + initials
						+ "' is used by more than one employees. Please choose a unique initials for each active employee");
				continue;
			}
			initialsToActiveEmployee.put(initials, employee);
		}

		return initialsToActiveEmployee;
	}

	private synchronized int harmonizeCustomerProfile(Connection connection, Cart cart) throws SQLException {
		logger.finest("Entered");
		int customerId = cart.getToGoInfo().getCustomerId();
		if (customerId == ToGoInfo.INVALID_CUSTOMER_ID) {
			if (cart.getToGoInfo().getPhoneNumber().equals(SharedConstDefs.DEFAULT_PHONE_NUMBER) == false) {
				String phoneNumber = cart.getToGoInfo().getPhoneNumber();
				try {
					CustomerProfile customerProfile = findCustomer(phoneNumber);
					if (customerProfile != null) {
						logger.severe(
								"Should not be here. Invalid customerId should have null customerProfile. customerProfile="
										+ customerProfile);
						Util.printStackTrace();
						return ToGoInfo.INVALID_CUSTOMER_ID; // return out instead of adding a new customer profile with
																// an existing phone
						// number.
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					logger.severe("Exception: " + e.getMessage());
					return ToGoInfo.INVALID_CUSTOMER_ID;
				}

				CustomerProfile customerProfileFromCart = new CustomerProfile(cart, null);
				customerProfileFromCart.insert(connection);

				return customerProfileFromCart.getCustomerId();
			} else {
				return ToGoInfo.INVALID_CUSTOMER_ID;
			}
		} else {
			CustomerProfile customerProfileFromDB = CustomerProfile.readById(connection, customerId);
			CustomerProfile customerProfileFromCart = new CustomerProfile(cart, customerProfileFromDB);
			if (customerProfileFromDB == null) {
				logger.fine("CustomerProfile not found in DB. Inserting new customerProfileFromCart: "
						+ customerProfileFromCart);
				customerProfileFromCart.insert(connection); // insert updates customerId

			} else {
				if (customerProfileFromCart.equals(customerProfileFromDB) == false) {
					logger.fine("DB customerProfile needs update.");
					customerProfileFromCart.update(connection);
				} else {
					logger.fine("No customerProfile update needed.");
				}
				logger.fine("FromDB:   " + customerProfileFromDB);
				logger.fine("FromCart: " + customerProfileFromCart);
			}
			return customerProfileFromCart.getCustomerId();
		}
	}

	private List<Cart> getUnsettledOrdersFromDB(Connection connection) throws SQLException {
		logger.finest("Entered");
		List<Cart> carts = new ArrayList<>();
		List<Order> orders = OrderTable.readAllUnsettled(connection);
		for (Order order : orders) {
			Cart cart = fillCart(connection, order);
			logger.fine(cart.toString());
			carts.add(cart);
		}

		return carts;
	}

	private Cart fillCart(Connection connection, Order order) throws SQLException {
		logger.finest("Entered");
		int orderId = order.getOrderId();
		Cart cart = new Cart();
		cart.setOrder(order);

		List<OrderItem> orderItems = OrderItemTable.readAllOf(connection, orderId);
		OrderItemTray orderItemTray = new OrderItemTray();
		orderItemTray.setOrderItems(orderItems);
		cart.setOrderItemTray(orderItemTray);

		CustomizerTray customizerTray = new CustomizerTray();
		for (OrderItem orderItem : orderItems) {
			int orderItemId = orderItem.getOrderItemId();
			List<Customizer> customizers = CustomizerTable.readAllOf(connection, orderId, orderItemId);
			if (customizers != null) {
				for (Customizer customizer : customizers) {
					customizerTray.addCustomizer(customizer);
				}
			}
		}
		cart.setCustomizerTray(customizerTray);

		ExchangerTray exchangerTray = new ExchangerTray();
		for (OrderItem orderItem : orderItems) {
			int orderItemId = orderItem.getOrderItemId();
			List<Exchanger> exchangers = ExchangerTable.readAllOf(connection, orderId, orderItemId);
			if (exchangers != null) {
				for (Exchanger exchanger : exchangers) {
					exchangerTray.addExchanger(exchanger);
				}
			}
		}
		cart.setExchangerTray(exchangerTray);

		List<Payment> payments = PaymentTable.readAllOf(connection, orderId);
		PaymentTray paymentTray = new PaymentTray();
		paymentTray.setPayments(payments);
		cart.setPaymentTray(paymentTray);

		DeliveryInfo deliveryInfo = DeliveryInfoTable.read(connection, orderId);
		cart.setDeliveryInfo(deliveryInfo);

		DineInInfo dineInfo = DineInInfoTable.read(connection, orderId);
		cart.setDineInInfo(dineInfo);

		ToGoInfo toGoInfo = ToGoInfoTable.read(connection, orderId);
		cart.setToGoInfo(toGoInfo);

		logger.finer(cart.toString());

		return cart;
	}

	// Caller should be synchronized to get mutual exclusive lock
	private void updateCachedOrders(Cart cart) {
		logger.finest("Entered");
		Order order = cart.getOrder();
		int orderId = order.getOrderId();

		int listSize = unsettledCarts.size();

		if (listSize == 0) {
			unsettledCarts.add(cart);
		} else {
			Order lastOrder = unsettledCarts.get(listSize - 1).getOrder();
			if (orderId > lastOrder.getOrderId()) { // must be a new order, just add to the end
				unsettledCarts.add(cart);
			} else { // existing order, replace
				boolean found = false;
				for (int i = unsettledCarts.size() - 1; i >= 0; --i) {
					if (orderId == unsettledCarts.get(i).getOrder().getOrderId()) {
						found = true;
						unsettledCarts.set(i, cart);
						break;
					}
				}
				if (found == false) {
					logger.warning("Cannot find expected order in unsettledCarts. " + order);
				}
			}
		}
	}

	private synchronized void notifyClients(NotificationMessage<?> notificationMessage) {
		logger.finest("Entered");

		var brokenClientSockets = new ArrayList<Socket>();

		for (Socket clientSocket : Server.clientSockets) {
			logger.info("Notified to clientSockt=" + clientSocket + ". msg=" + notificationMessage.toString());
			try {
				var outputStream = clientSocket.getOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
				objectOutputStream.writeObject(notificationMessage);
			} catch (IOException e) {
				brokenClientSockets.add(clientSocket);
				logger.info("Fail to notify clientSocket=" + clientSocket + " for msg=" + notificationMessage + ". Exception: " + e.getMessage());
			}
		}

		for (Socket brokenClientSocket : brokenClientSockets) {
			logger.info("Removing brokenClientSocket=" + brokenClientSocket);
			Server.clientSockets.removeIf(clientSocket -> clientSocket == brokenClientSocket);
		}
	}

	// Default ConsoleHandler has info log level as output.
	// Specify a logging property by passing in a JVM parameter as follow to control
	// -Djava.util.logging.config.file="conf/serverLogging.properties"
	public static void main(String argv[]) {
		logger.finest("Entered");
		logger.info("Starting Server for " + Configuration.getRestaurantName() + " "
				+ Configuration.getRestaurantChineseName());
		// Can also use jcmd to lot more information about the JVM.
		logger.config("classpath=" + System.getProperty("java.class.path"));
		logger.config("logging.properties file=" + loggingConfigFile);

		try {
			// RMI starts a non-daemon listening thread.
			// unbind and unexport to stop it is done in the shutdown()
			registry = LocateRegistry.createRegistry(java.rmi.registry.Registry.REGISTRY_PORT);
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.warning("java RMI registry already exists. Most likely another server is aready running.");
			logger.warning(
					"This could happen if the server was not shutdown properly. Manually stop the server process or restart the server computer, then start the server again.");
			System.exit(-10); // This prevents more than one server running.
			// Could have continue to use the already in used port. But leaves the
			// possibility of having multiple server instances running.
		}
		logger.config("java RMI registry created.");

		Server serverObject = null;
		try (Connection connection = DataSource.getConnection()) {
			if (connection != null) {
				logger.config("Connection to DB established.");
			}

			try {
				serverObject = new Server(connection);
			} catch (RemoteException | SQLException e) {
				e.printStackTrace();
				logger.severe("Unable to create server object. " + e.getMessage());
				System.exit(-20);
			}
			logger.config("Server object created.");

			try {
				menu = new Menu(connection);
			} catch (SQLException e) {
				e.printStackTrace();
				logger.severe("Unable to read menu. " + e.getMessage());
				System.exit(-30);
			}

			ServerInterface serverInterfaceStub = null;
			try {
				// The java.rmi.registry.Registry.REGISTRY_PORT is used here for the
				// ServerInterface so that in a fire walled environment.
				// only one port is needed to be open.
				// This port could be different than the REGISTRY_PORT. If that's the case, it
				// has to be opened in a fire wall.
				serverInterfaceStub = (ServerInterface) UnicastRemoteObject.exportObject(serverObject,
						java.rmi.registry.Registry.REGISTRY_PORT);
			} catch (RemoteException e) {
				e.printStackTrace();
				logger.severe("Unable to export serverInterfaceStub object. " + e.getMessage());
				System.exit(-40);
			}
			logger.config("Exported serverInterfaceStub.");

			try {
				registry.rebind(RMI_NAME_STRING, serverInterfaceStub);
			} catch (Exception e) {
				e.printStackTrace();
				logger.severe("registry rebind failed. " + e.getMessage());
			}

			logger.config("Bound serverInterfaceStub to registry.");

			if (connection != null) {
				logger.config("Server has connection to database.");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			logger.severe(
					"Conneting to DB connectin failed. Is DB server is running or DB tables are created with CreateDB and ImportMenu? "
							+ e1.getMessage());
			System.exit(-50);
		}

		try {
			serverSocket = new ServerSocket(SERVER_TCP_PORT);
		} catch (IOException e) {
			logger.severe("Create ServerSocket failed. Check if another server is running. " + e.getMessage());
			System.exit(-60);
		}

		logger.config("Created ServerSocket for client to connect for notifications. serverSocket=" + serverSocket);

		// Start a daemon thread to check and print orders that are on hold from printing.
		try {
			new HeldOrderChecker(serverObject);
		} catch (Exception e) {
			logger.severe("Failed to start daemon thread to print held order. Exception: " + e.getMessage());
			System.exit(-70);
		}

		while (running) {
			logger.config("In loop waiting for client to connect for notifications.");
			try {
				Socket socket = serverSocket.accept();
				logger.config("A client connected at socket=" + socket);
				clientSockets.add(socket);
			} catch (IOException e) {
				if (running) {
					e.printStackTrace();
					logger.warning("Server still running. Unexpected Exception: " + e.getMessage());
				} else {
					logger.info("Expected Exception due to serverSocket close from shuttingdown server. Exception: " + e.getMessage());
				}
			}
		}

		logger.config("Exited forever loop. running=" + running + ". Exiting...");
		logger.config("Last log before server exiting.");
	}
}
