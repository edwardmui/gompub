package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.SubOrderItemStatus;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.Dish;

/**
 * Condiment exchanger is to exchange one condiment with another. An upgrade
 * price might be associate with it for large or for small. Condiment
 * calculation has to take into account of these exchangers Therefore, an
 * exchanger must be associated with an order item. The associated order item
 * large quantity and small quantity apply directly to exchanger during
 * calculation. When an order item is voided, all the exchanger for that order
 * item is not used to calculate the item price. If the order item is unvoided.
 * The previous status of the exchanger takes effect.
 * 
 * All exchanger must be available in the menu.csv file. The exchanger's name
 * and price are stored in the table for settled order history reference when
 * the menu it was ordered from is no longer available.
 */
public class Exchanger implements Serializable, Comparable<Exchanger> {
	private int orderId;
	private int orderItemId;
	private int exchangerId;
	private int exchangerDishId;
	private String name;
	private long orderedTime = System.currentTimeMillis();
	SubOrderItemStatus status = SubOrderItemStatus.VALID;
	private long voidedTime;
	private int voidedById;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Exchanger.class.getName());

	// append to the exchangerId in orderTable for display purpose. And removed at
	// parsing to get the exchangerId
	public static final String EX = "ex";
	public static final String NONE = "None"; // exchange condiment to nothing
	public static final String FROM_TO_NAME_DELMITER = "=>";
	private String action = "";
	private String fromName = "";
	private String toName = "";

	public Exchanger(int orderItemId, Dish exchangerDish) {
		logger.finest("Entered");
		this.orderItemId = orderItemId;
		this.exchangerDishId = exchangerDish.getDishId();
		this.name = exchangerDish.getShortName();

		// See conf.yaml file for description of the exchangeCondiment category
		// description and examples on the parsing
		final String namePattern = "action from condiment name => to name";
		String[] exchangerParts = name.split(" ", 2); // only split once on the first space.
		if (exchangerParts.length == 2) {
			action = exchangerParts[0];
			String[] nameParts = exchangerParts[1].split(FROM_TO_NAME_DELMITER, 2);
			if (nameParts.length == 2) {
				fromName = nameParts[0];
				toName = nameParts[1];
			} else {
				throw new RuntimeException(
						"Unexpected format for exchanger name='" + name + "'. format must be: '" + namePattern + "'");
			}
		} else {
			throw new RuntimeException(
					"Unexpected format for exchanger name='" + name + "'. format must be: '" + namePattern + "'");
		}
	}

	public Exchanger(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(ExchangerTable.ORDER_ID);
		orderItemId = resultSet.getInt(ExchangerTable.ORDER_ITEM_ID);
		exchangerId = resultSet.getInt(ExchangerTable.EXCHANGER_ID);
		exchangerDishId = resultSet.getInt(ExchangerTable.EXCHANGER_DISH_ID);
		name = resultSet.getString(ExchangerTable.NAME);
		orderedTime = resultSet.getLong(ExchangerTable.ORDERED_TIME);
		status = SubOrderItemStatus.values()[resultSet.getInt(ExchangerTable.STATUS)];
		voidedTime = resultSet.getLong(ExchangerTable.VOIDED_TIME);
		voidedById = resultSet.getInt(ExchangerTable.VOIDED_BY_ID);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);
		prepStmt.setInt(++parameterIndex, orderItemId);
		prepStmt.setInt(++parameterIndex, exchangerId);
		prepStmt.setInt(++parameterIndex, exchangerDishId);

		int expectedIndex = ExchangerTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setInt(++parameterIndex, orderItemId);
		prepStmt.setInt(++parameterIndex, exchangerId);
		prepStmt.setInt(++parameterIndex, exchangerDishId);
		prepStmt.setString(++parameterIndex, name);
		prepStmt.setLong(++parameterIndex, orderedTime);
		prepStmt.setInt(++parameterIndex, status.ordinal());
		prepStmt.setLong(++parameterIndex, voidedTime);
		prepStmt.setInt(++parameterIndex, voidedById);

		if (parameterIndex != ExchangerTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ ExchangerTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + ExchangerTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + ExchangerTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			ResultSet resultSet = prepStmt.executeQuery();
			logger.finer(prepStmt.toString());
			resultSet.first();
			init(resultSet);
		}
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("UPDATE " + ExchangerTable.ALL_COLUMNS_WITH_SET + ExchangerTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, ExchangerTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + ExchangerTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public boolean isVoided() {
		return (status == SubOrderItemStatus.VOIDED);
	}

	public String getQuotedName() {
		return "'" + name + "'";
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(int orderItemId) {
		this.orderItemId = orderItemId;
	}

	public int getExchangerId() {
		return exchangerId;
	}

	public void setExchangerId(int exchangerId) {
		this.exchangerId = exchangerId;
	}

	public int getExchangerDishId() {
		return exchangerDishId;
	}

	public void setExchangerDishId(int exchangerDishId) {
		this.exchangerDishId = exchangerDishId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getOrderedTime() {
		return orderedTime;
	}

	public void setOrderedTime(long orderedTime) {
		this.orderedTime = orderedTime;
	}

	public SubOrderItemStatus getStatus() {
		return status;
	}

	public void setStatus(SubOrderItemStatus status) {
		this.status = status;
	}

	public long getVoidedTime() {
		return voidedTime;
	}

	public void setVoidedTime(long voidedTime) {
		this.voidedTime = voidedTime;
	}

	public int getVoidedById() {
		return voidedById;
	}

	public void setVoidedById(int voidedById) {
		this.voidedById = voidedById;
	}

	public String getIdPlus1WithPostfix() {
		return String.valueOf(exchangerId + 1) + EX;
	}

	public String getAction() {
		return action;
	}

	public String getFromName() {
		return fromName;
	}

	public String getToName() {
		return toName;
	}

	public boolean isToNameNone() {
		return toName.equals(NONE);
	}

	public boolean hasSamePrimaryKey(Exchanger exchanger) {
		if (orderId == exchanger.getOrderId() && orderItemId == exchanger.getOrderItemId()
				&& exchangerId == exchanger.getExchangerId() && exchangerDishId == exchanger.getExchangerDishId()) {
			return true;
		}

		return false;
	}

	public boolean isInExchangerList(List<Exchanger> exchangers) {
		for (Exchanger exchanger : exchangers) {
			if (hasSamePrimaryKey(exchanger)) {
				return true;
			}
		}

		return false;
	}

	public int getPrice(OrderItem orderItem, Dish exchangerDish) {
		int price = 0;
		if (orderItemId == orderItem.getOrderItemId()) {
			price = orderItem.getLargeQuantity() * exchangerDish.getLargePrice()
					+ orderItem.getSmallQuantity() * exchangerDish.getSmallPrice();
		}

		return price;
	}

	public String getFormattedPrice(OrderItem orderItem, Dish exchangerDish) {
		int price = getPrice(orderItem, exchangerDish);

		return price == 0 ? "" : " +" + String.format("%4.2f", price/100.0);
	}

	@Override
	public int compareTo(Exchanger thatExchanger) {
		return Comparator.comparing(Exchanger::getOrderId).thenComparing(Exchanger::getOrderItemId)
				.thenComparing(Exchanger::getExchangerId).compare(this, thatExchanger);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + exchangerDishId;
		result = prime * result + exchangerId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + orderId;
		result = prime * result + orderItemId;
		result = prime * result + (int) (orderedTime ^ (orderedTime >>> 32));
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		Exchanger other = (Exchanger) obj;
		if (exchangerDishId != other.exchangerDishId) {
			return false;
		}
		if (exchangerId != other.exchangerId) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (orderId != other.orderId) {
			return false;
		}
		if (orderItemId != other.orderItemId) {
			return false;
		}
		if (orderedTime != other.orderedTime) {
			return false;
		}
		if (status != other.status) {
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
		return Exchanger.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", orderItemId=" + orderItemId +
				", exchangerId=" + exchangerId +
				", exchangerDishId=" + exchangerDishId +
				", name=" + name +
				", orderedTime=" + Util.formatEpochToLocal(orderedTime) +
				", status=" + status +
				", voidedTime=" + Util.formatEpochToLocal(voidedTime) +
				", voidedById=" + voidedById +
				"]";
		// @formatter:on
	}
}
