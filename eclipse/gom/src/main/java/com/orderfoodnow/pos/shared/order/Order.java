package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.OrderStatus;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PlacementType;
import com.orderfoodnow.pos.shared.Util;

/**
 * A phone-in order is a to-go order that placed over the phone. A phone number
 * is inquired first, if a customer record exist in the DB, the customer info,
 * such as the name can be used to populate the order. If no customer record is
 * found based on the phone number, it is optional to ask for the customer name
 * to be associated with the order. A new customer record with along with the
 * name are used to create a customer record for future use.
 * 
 * A walk-in order is a to-go order that is placed face-to-face at the counter.
 * CustomerProfile name and phone number are optional and usually inquired to
 * entered in the order. An order number can be printed to give to the customer
 * during busy time where more than a few customers are waiting.
 */
public class Order implements Serializable, Comparable<Order> {
	private int orderId = 0; // order ID, 0 for new order. It's assigned by DB auto increment at insert time
	private int orderNumber;
	private OrderType type = OrderType.PHONE_IN;
	private long orderedTime; // set when the order is first time commit.
	private int takenById;
	private PlacementType placementType = PlacementType.PHONE;
	private int cashPaidAmount;
	private long paidTime;
	private int cashierId;
	private OrderStatus status = OrderStatus.MAKING;
	private long committedTime; // set when the order it committed or updated.
	private int settledId = 0; // 0 for settled orders. settleId(SettleTable.SETTLE_ID) at settle time
	private long voidedTime;
	private int voidedById;
	private int discountPercent;
	private int additionalPercent;
	private int discountAmount;
	private int additionalAmount;
	private float taxRate = Configuration.getRestaurantTaxRate();
	private String note = "";
	private int printVersion = 1;
	private long orderedTimestamp = System.currentTimeMillis();

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Order.class.getName());

	public Order() {
	}

	public Order(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(OrderTable.ORDER_ID);
		orderNumber = resultSet.getInt(OrderTable.ORDER_NUMBER);
		type = OrderType.values()[resultSet.getInt(OrderTable.TYPE)];
		orderedTime = resultSet.getLong(OrderTable.ORDERED_TIME);
		takenById = resultSet.getInt(OrderTable.TAKEN_BY_ID);
		placementType = PlacementType.values()[resultSet.getInt(OrderTable.PLACEMENT_TYPE)];
		cashPaidAmount = resultSet.getInt(OrderTable.CASH_PAID_AMOUNT);
		paidTime = resultSet.getLong(OrderTable.PAID_TIME);
		cashierId = resultSet.getInt(OrderTable.CASHIER_ID);
		status = OrderStatus.values()[resultSet.getInt(OrderTable.STATUS)];
		committedTime = resultSet.getLong(OrderTable.COMMITTED_TIME);
		settledId = resultSet.getInt(OrderTable.SETTLE_ID);
		voidedTime = resultSet.getLong(OrderTable.VOIDED_TIME);
		voidedById = resultSet.getInt(OrderTable.VOIDED_BY_ID);
		discountPercent = resultSet.getInt(OrderTable.DISCOUNT_PERCENT);
		additionalPercent = resultSet.getInt(OrderTable.ADDITIONAL_PERCENT);
		discountAmount = resultSet.getInt(OrderTable.DISCOUNT_AMOUNT);
		additionalAmount = resultSet.getInt(OrderTable.ADDITIONAL_AMOUNT);
		taxRate = resultSet.getFloat(OrderTable.TAX_RATE);
		note = resultSet.getString(OrderTable.NOTE) == null ? "" : resultSet.getString(OrderTable.NOTE);
		printVersion = resultSet.getInt(OrderTable.PRINT_VERSION);
		orderedTimestamp = resultSet.getTimestamp(OrderTable.ORDERED_TIMESTAMP).getTime();
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);

		int expectedIndex = OrderTable.NUM_KEY_COLUMNS + startIndex;
		if (parameterIndex != expectedIndex) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect=" + expectedIndex
					+ " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatementWithAllFields(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, orderId);

		fillPreparedStatement(prepStmt, parameterIndex);

		if (parameterIndex != OrderTable.NUM_AUTOINC_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ OrderTable.NUM_AUTOINC_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatementWithAllFieldsMinusAutoInc(PreparedStatement prepStmt)
			throws SQLException {
		logger.finest("Entered");
		fillPreparedStatement(prepStmt, 0);

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatement(PreparedStatement prepStmt, int startIndex) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderNumber);
		prepStmt.setInt(++parameterIndex, type.ordinal());
		prepStmt.setLong(++parameterIndex, orderedTime);
		prepStmt.setInt(++parameterIndex, takenById);
		prepStmt.setInt(++parameterIndex, placementType.ordinal());
		prepStmt.setInt(++parameterIndex, cashPaidAmount);
		prepStmt.setLong(++parameterIndex, paidTime);
		prepStmt.setInt(++parameterIndex, cashierId);
		prepStmt.setInt(++parameterIndex, status.ordinal());
		prepStmt.setLong(++parameterIndex, committedTime);
		prepStmt.setInt(++parameterIndex, settledId);
		prepStmt.setLong(++parameterIndex, voidedTime);
		prepStmt.setInt(++parameterIndex, voidedById);
		prepStmt.setInt(++parameterIndex, discountPercent);
		prepStmt.setInt(++parameterIndex, additionalPercent);
		prepStmt.setInt(++parameterIndex, discountAmount);
		prepStmt.setInt(++parameterIndex, additionalAmount);
		prepStmt.setFloat(++parameterIndex, taxRate);
		prepStmt.setString(++parameterIndex, note);
		prepStmt.setInt(++parameterIndex, printVersion);
		prepStmt.setTimestamp(++parameterIndex, new Timestamp(orderedTimestamp));

		int expectedParameterCount = OrderTable.NUM_COLUMNS - OrderTable.NUM_AUTOINC_COLUMNS + startIndex;
		if (parameterIndex != expectedParameterCount) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ expectedParameterCount + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement(
				"INSERT INTO " + OrderTable.ALL_COLUMNS_MINUS_AUTOINC, Statement.RETURN_GENERATED_KEYS)) {
			fillPreparedStatementWithAllFieldsMinusAutoInc(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
			ResultSet resultSet = prepStmt.getGeneratedKeys();
			resultSet.next();
			orderId = resultSet.getInt(1);
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + OrderTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public static Order readById(Connection connection, int orderId) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + OrderTable.ORDER_ID + " FROM "
				+ OrderTable.TABLE_NAME + " WHERE " + OrderTable.ORDER_ID + "=?")) {
			prepStmt.setInt(1, orderId);
			logger.finer(prepStmt.toString());
			prepStmt.executeQuery();
			Order order = new Order();
			order.setOrderId(orderId);
			order.read(connection);
			return order;
		} catch (SQLException e) {
			logger.finer("Unable to read order by orderId: " + orderId);
			logger.finer(e.getMessage());
			return null;
		}
	}

	public static List<Order> readHistoryOrders(Connection connection, List<Integer> orderIds) throws SQLException {
		logger.finest("Entered");
		// Must add L to one of the number else default to int resulting a number
		// overflow
		long lastYearToday = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L); // ONE_YR_IN_MS
		List<Order> orders = new ArrayList<>();
		for (int orderId : orderIds) {
			Order order = new Order();
			order.setOrderId(orderId);
			order.read(connection);
			if (order.getOrderedTime() > lastYearToday) {
				orders.add(order);
			}
		}

		return orders;
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("UPDATE " + OrderTable.ALL_COLUMNS_WITH_SET + OrderTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, OrderTable.NUM_COLUMNS);
			prepStmt.executeUpdate();
		}
	}

	public String getLast4DigitsOfOrderId() {
		logger.finest("Entered");
		String idString = Long.valueOf(orderId).toString();
		int len = idString.length();

		if (len == 1) {
			idString = "000" + idString;
		} else if (len == 2) {
			idString = "00" + idString;
		} else if (len == 3) {
			idString = "0" + idString;
		}

		if (len > 4) {
			idString = idString.substring(len - 4, len);
		}

		return idString;
	}

	public String getCheckNumberPrefix() {
		logger.finest("Entered");
		String seq = "000";
		String orderTime = Long.toString(orderedTime);
		int len = orderTime.length();
		if (len >= 11) {
			seq = orderTime.substring(len - 11, len - 8);
		}

		return seq;
	}

	public String getCheckNumber() {
		logger.finest("Entered");
		String seq = "00000";
		String orderTime = Long.toString(orderedTime);
		int len = orderTime.length();
		if (len >= 8) {
			seq = orderTime.substring(len - 8, len - 3);
		}

		return seq;
	}

	public void incrementPrintVersion() {
		++printVersion;
	}

	public boolean isVoided() {
		return (status == OrderStatus.VOIDED);
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public long getOrderedTime() {
		return orderedTime;
	}

	public void setOrderedTime(long orderedTime) {
		this.orderedTime = orderedTime;
	}

	public int getTakenById() {
		return takenById;
	}

	public void setTakenById(int takenById) {
		this.takenById = takenById;
	}

	public PlacementType getPlacementType() {
		return placementType;
	}

	public void setPlacementType(PlacementType placementType) {
		this.placementType = placementType;
	}

	public int getCashPaidAmount() {
		return cashPaidAmount;
	}

	public void setCashPaidAmount(int cashPaidAmount) {
		this.cashPaidAmount = cashPaidAmount;
	}

	public long getPaidTime() {
		return paidTime;
	}

	public void setPaidTime(long paidTime) {
		this.paidTime = paidTime;
	}

	public int getCashierId() {
		return cashierId;
	}

	public void setCashierId(int cashierId) {
		this.cashierId = cashierId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public long getCommittedTime() {
		return committedTime;
	}

	public void setCommittedTime(long committedTime) {
		this.committedTime = committedTime;
	}

	public int getSettledId() {
		return settledId;
	}

	public void setSettledId(int settledId) {
		this.settledId = settledId;
	}

	public long getVoidedTime() {
		return voidedTime;
	}

	public void setVoidedTime(long voidedTime) {
		this.voidedTime = voidedTime;
	}

	public long getVoidedById() {
		return voidedById;
	}

	public void setVoidedById(int voidedById) {
		this.voidedById = voidedById;
	}

	public int getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}

	public int getAdditionalPercent() {
		return additionalPercent;
	}

	public void setAdditionalPercent(int additionalPercent) {
		this.additionalPercent = additionalPercent;
	}

	public int getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(int discountAmount) {
		this.discountAmount = discountAmount;
	}

	public int getAdditionalAmount() {
		return additionalAmount;
	}

	public void setAdditionalAmount(int additionalAmount) {
		this.additionalAmount = additionalAmount;
	}

	public float getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(float taxRate) {
		this.taxRate = taxRate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getPrintVersion() {
		return printVersion;
	}

	public void setPrintVersion(int printVersion) {
		this.printVersion = printVersion;
	}

	public long getOrderedTimestamp() {
		return orderedTimestamp;
	}

	public void setOrderedTimestamp(long orderedTimestamp) {
		this.orderedTimestamp = orderedTimestamp;
	}

	@Override
	public int compareTo(Order thatOrder) {
		return Comparator.comparing(Order::getOrderId).compare(this, thatOrder);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + additionalAmount;
		result = prime * result + additionalPercent;
		result = prime * result + cashPaidAmount;
		result = prime * result + cashierId;
		result = prime * result + (int) (committedTime ^ (committedTime >>> 32));
		result = prime * result + discountAmount;
		result = prime * result + discountPercent;
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result + orderId;
		result = prime * result + orderNumber;
		result = prime * result + (int) (orderedTime ^ (orderedTime >>> 32));
		result = prime * result + (int) (orderedTimestamp ^ (orderedTimestamp >>> 32));
		result = prime * result + (int) (paidTime ^ (paidTime >>> 32));
		result = prime * result + ((placementType == null) ? 0 : placementType.hashCode());
		result = prime * result + printVersion;
		result = prime * result + settledId;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + takenById;
		result = prime * result + Float.floatToIntBits(taxRate);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + voidedById;
		result = prime * result + (int) (voidedTime ^ (voidedTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Order other = (Order) obj;
		if (additionalAmount != other.additionalAmount) {
			return false;
		}
		if (additionalPercent != other.additionalPercent) {
			return false;
		}
		if (cashPaidAmount != other.cashPaidAmount) {
			return false;
		}
		if (cashierId != other.cashierId) {
			return false;
		}
		if (committedTime != other.committedTime) {
			return false;
		}
		if (discountAmount != other.discountAmount) {
			return false;
		}
		if (discountPercent != other.discountPercent) {
			return false;
		}
		if (note == null) {
			if (other.note != null) {
				return false;
			}
		} else if (!note.equals(other.note)) {
			return false;
		}
		if (orderId != other.orderId) {
			return false;
		}
		if (orderNumber != other.orderNumber) {
			return false;
		}
		if (orderedTime != other.orderedTime) {
			return false;
		}
		if (orderedTimestamp != other.orderedTimestamp) {
			return false;
		}
		if (paidTime != other.paidTime) {
			return false;
		}
		if (placementType != other.placementType) {
			return false;
		}
		if (printVersion != other.printVersion) {
			return false;
		}
		if (settledId != other.settledId) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (takenById != other.takenById) {
			return false;
		}
		if (Float.floatToIntBits(taxRate) != Float.floatToIntBits(other.taxRate)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (voidedById != other.voidedById) {
			return false;
		}
		if (voidedTime != other.voidedTime) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Order.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", orderNumber=" + orderNumber +
				", type=" + type + 
				", orderedTime=" + Util.formatEpochToLocal(orderedTime) +
				", takenById=" + takenById +
				", placementType=" + placementType +
				", cashPaidAmount=" + cashPaidAmount +
				", paidTime=" + Util.formatEpochToLocal(paidTime) +
				", cashierId=" + cashierId +
				", status=" + status + 
				", committedTime=" + Util.formatEpochToLocal(committedTime) +
				", settledId=" + settledId +
				", voidedTime=" + Util.formatEpochToLocal(voidedTime) +
				", voidedById=" + voidedById +
				", discountPercent=" + discountPercent +
				", additionalPercent=" + additionalPercent +
				", taxRate=" + taxRate +
				", note=" + note +
				", printVersion=" + printVersion +
				", orderedTimestamp=" + Util.formatEpochToLocal(orderedTimestamp) +
				"]";
		// @formatter:on
	}
}
