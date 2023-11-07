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
 * Customizer is extra enhancers that's added to the order item to customize it.
 * Therefore, a customizer must be associated with an order item. When an order
 * item is voided, all the customizer for that order item is not used to
 * calculate the item price. If the order item is unvoided. The previous status
 * of the customizer takes effect.
 * 
 * The name and price of the customizer are stored directly in the customizer
 * table which is part of an order. A customizer could be a non-menu item which
 * is 'make-up' at order time by a customer and the employee specified a price.
 * These non menu item has a dishId < 0.
 * 
 * Most (if not all) customizers are in the menu with its own category. For the
 * menu customizers, Each customizer is a dish from the menu object, therefore
 * it has a positive dish_id called customizer_dish_id. To support non-menu
 * customizers, a negative customizer_dish_id is use with different sequential
 * negative number to support more than one.
 * 
 */
public class Customizer implements Serializable, Comparable<Customizer> {
	private int orderId;
	private int orderItemId;
	private int customizerId;
	private int customizerDishId; // < 0 for non-menu customizers
	private String name;
	private int price;
	private long orderedTime = System.currentTimeMillis();
	SubOrderItemStatus status = SubOrderItemStatus.VALID;
	private long voidedTime;
	private int voidedById;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Customizer.class.getName());

	public Customizer() {
	}

	public Customizer(int orderItemId, int customizerDishId, String name, int price) {
		logger.finest("Entered");
		this.orderItemId = orderItemId;
		this.customizerDishId = customizerDishId;
		this.name = name;
		this.price = price;
	}

	public Customizer(int orderItemId, Dish dish) {
		logger.finest("Entered");
		this.orderItemId = orderItemId;
		this.customizerDishId = dish.getDishId();
		this.name = dish.getShortName();
		this.price = dish.getLargePrice();
	}

	public Customizer(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(CustomizerTable.ORDER_ID);
		orderItemId = resultSet.getInt(CustomizerTable.ORDER_ITEM_ID);
		customizerId = resultSet.getInt(CustomizerTable.CUSTOMIZER_ID);
		customizerDishId = resultSet.getInt(CustomizerTable.CUSTOMIZER_DISH_ID);
		name = resultSet.getString(CustomizerTable.NAME);
		price = resultSet.getInt(CustomizerTable.PRICE);
		orderedTime = resultSet.getLong(CustomizerTable.ORDERED_TIME);
		status = SubOrderItemStatus.values()[resultSet.getInt(CustomizerTable.STATUS)];
		voidedTime = resultSet.getLong(CustomizerTable.VOIDED_TIME);
		voidedById = resultSet.getInt(CustomizerTable.VOIDED_BY_ID);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);
		prepStmt.setInt(++parameterIndex, orderItemId);
		prepStmt.setInt(++parameterIndex, customizerId);
		prepStmt.setInt(++parameterIndex, customizerDishId);

		int expectedIndex = CustomizerTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setInt(++parameterIndex, customizerId);
		prepStmt.setInt(++parameterIndex, customizerDishId);
		prepStmt.setString(++parameterIndex, name);
		prepStmt.setInt(++parameterIndex, price);
		prepStmt.setLong(++parameterIndex, orderedTime);
		prepStmt.setInt(++parameterIndex, status.ordinal());
		prepStmt.setLong(++parameterIndex, voidedTime);
		prepStmt.setInt(++parameterIndex, voidedById);

		if (parameterIndex != CustomizerTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ CustomizerTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + CustomizerTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + CustomizerTable.TABLENAME_ANDING_ALL_KEYS)) {
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
				.prepareStatement("UPDATE " + CustomizerTable.ALL_COLUMNS_WITH_SET + CustomizerTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, CustomizerTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + CustomizerTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
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

	public int getCustomizerId() {
		return customizerId;
	}

	public void setCustomizerId(int customizerId) {
		this.customizerId = customizerId;
	}

	public int getCustomizerDishId() {
		return customizerDishId;
	}

	public void setCustomizerDishId(int customizerDishId) {
		this.customizerDishId = customizerDishId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
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

	public int getIdPlus1() {
		return customizerId + 1;
	}

	public boolean hasSamePrimaryKey(Customizer customizer) {
		logger.finest("Entered");
		if (orderId == customizer.getOrderId() && orderItemId == customizer.getOrderItemId()
				&& customizerId == customizer.getCustomizerId()
				&& customizerDishId == customizer.getCustomizerDishId()) {
			return true;
		}

		return false;
	}

	public boolean isInCustomizerList(List<Customizer> customizers) {
		logger.finest("Entered");
		for (Customizer customizer : customizers) {
			if (hasSamePrimaryKey(customizer)) {
				return true;
			}
		}

		return false;
	}

	public String getFormattedPrice() {
		return price == 0 ? "" : " +" + String.format("%4.2f", price/100.0);
	}

	@Override
	public int compareTo(Customizer thatCustomizer) {
		return Comparator.comparing(Customizer::getOrderId).thenComparing(Customizer::getOrderItemId)
				.thenComparing(Customizer::getCustomizerId).compare(this, thatCustomizer);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + customizerId;
		result = prime * result + customizerDishId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + orderId;
		result = prime * result + orderItemId;
		result = prime * result + (int) (orderedTime ^ (orderedTime >>> 32));
		result = prime * result + price;
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
		Customizer other = (Customizer) obj;
		if (customizerId != other.customizerId) {
			return false;
		}
		if (customizerDishId != other.customizerDishId) {
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
		if (price != other.price) {
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
		return Customizer.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", orderItemId=" + orderItemId +
				", customizerId=" + customizerId +
				", customizerDishId=" + customizerDishId +
				", name=" + name +
				", price=" + price +
				", orderedTime=" + Util.formatEpochToLocal(orderedTime) +
				", status=" + status +
				", voidedTime=" + Util.formatEpochToLocal(voidedTime) +
				", voidedById=" + voidedById +
				"]";
		// @formatter:on
	}
}
