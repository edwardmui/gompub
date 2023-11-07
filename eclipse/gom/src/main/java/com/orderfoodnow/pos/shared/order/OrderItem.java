package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.DishSize;
import com.orderfoodnow.pos.shared.OrderItemStatus;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.Menu;

public class OrderItem implements Serializable, Comparable<OrderItem> {
	private int orderId;
	private int orderItemId;
	private int dishId;
	private String dishShortName = "";
	private int largeQuantity;
	private int smallQuantity;
	private long orderedTime = System.currentTimeMillis();
	OrderItemStatus status = OrderItemStatus.WAITING;
	private long voidedTime;
	private int voidedById;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(OrderItem.class.getName());

	public OrderItem() {
	}

	public OrderItem(OrderItem orderItem) {
		logger.finest("Entered");
		this.orderId = orderItem.dishId;
		this.orderItemId = orderItem.orderItemId;
		this.dishId = orderItem.dishId;
		this.dishShortName = new String(orderItem.dishShortName);
		this.largeQuantity = orderItem.largeQuantity;
		this.smallQuantity = orderItem.smallQuantity;
		this.orderedTime = orderItem.orderedTime;
		this.status = orderItem.status;
		this.voidedTime = orderItem.voidedTime;
		this.voidedById = orderItem.voidedById;
	}

	public OrderItem(Dish dish, DishSize dishSize, int quantity) {
		logger.finest("Entered");
		this.dishId = dish.getDishId();
		this.dishShortName = dish.getShortName();
		addQuantity(dishSize, quantity);
	}

	public OrderItem(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(OrderItemTable.ORDER_ID);
		orderItemId = resultSet.getInt(OrderItemTable.ORDER_ITEM_ID);
		dishId = resultSet.getInt(OrderItemTable.DISH_ID);
		dishShortName = resultSet.getString(OrderItemTable.DISH_SHORT_NAME);
		largeQuantity = resultSet.getInt(OrderItemTable.LARGE_QUANTITY);
		smallQuantity = resultSet.getInt(OrderItemTable.SMALL_QUANTITY);
		orderedTime = resultSet.getLong(OrderItemTable.ORDERED_TIME);
		status = OrderItemStatus.values()[resultSet.getInt(OrderItemTable.STATUS)];
		voidedTime = resultSet.getLong(OrderItemTable.VOIDED_TIME);
		voidedById = resultSet.getInt(OrderItemTable.VOIDED_BY_ID);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);
		prepStmt.setInt(++parameterIndex, orderItemId);

		int expectedIndex = OrderItemTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setInt(++parameterIndex, dishId);
		prepStmt.setString(++parameterIndex, dishShortName);
		prepStmt.setInt(++parameterIndex, largeQuantity);
		prepStmt.setInt(++parameterIndex, smallQuantity);
		prepStmt.setLong(++parameterIndex, orderedTime);
		prepStmt.setInt(++parameterIndex, status.ordinal());
		prepStmt.setLong(++parameterIndex, voidedTime);
		prepStmt.setInt(++parameterIndex, voidedById);

		if (parameterIndex != OrderItemTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ OrderItemTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + OrderItemTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + OrderItemTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("UPDATE " + OrderItemTable.ALL_COLUMNS_WITH_SET + OrderItemTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, OrderItemTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + OrderItemTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public int getOrderItemIdPlus1() {
		return orderItemId + 1;
	}

	public int getOrderItemIdMinus1() {
		return orderItemId - 1;
	}

	public boolean hasSamePrimaryKey(OrderItem orderItem) {
		if (orderId == orderItem.getOrderId() && orderItemId == orderItem.getOrderItemId()) {
			return true;
		}
		return false;
	}

	public boolean isInOrderItemList(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			if (hasSamePrimaryKey(orderItem)) {
				return true;
			}
		}
		return false;
	}

	public boolean isVoided() {
		return (status == OrderItemStatus.VOIDED);
	}

	public void addLargeQuantity(int quantity) {
		largeQuantity += quantity;
	}

	public void addSmallQuantity(int quantity) {
		smallQuantity += quantity;
	}

	public void addQuantity(DishSize dishSize, int quantity) {
		logger.finest("Entered");
		switch (dishSize) {
		case LARGE:
			largeQuantity += quantity;
			break;
		case SMALL:
			smallQuantity += quantity;
			break;
		default:
			throw new RuntimeException("Program Error: Unexpected dish size=" + dishSize);
		}
	}

	public boolean isQuantityValidToToggle() {
		logger.finest("Entered");
		if (largeQuantity == 1 && smallQuantity == 0) {
			return true;
		}

		if (smallQuantity == 1 && largeQuantity == 0) {
			return true;
		}

		return false;
	}

	public void toggleSize() {
		logger.finest("Entered");
		if (largeQuantity == 1 && smallQuantity == 0) {
			smallQuantity = largeQuantity;
			largeQuantity = 0;
		} else if (smallQuantity == 1 && largeQuantity == 0) {
			largeQuantity = smallQuantity;
			smallQuantity = 0;
		}
	}

	// return the sum of largeQuantity + smallQuantity/2 + 1 (plus 1 if odd)
	public int getOrderItemQuantity() {
		logger.finest("Entered");
		if (smallQuantity > 0) {
			int largeQquivalent = smallQuantity / 2;
			if (smallQuantity % 2 == 0) {
				return (largeQuantity + largeQquivalent);
			} else
				return (largeQuantity + largeQquivalent + 1);
		} else {
			return largeQuantity;
		}
	}

	public String getLargeQuantityWithPadding() {
		logger.finest("Entered");
		String largeQuantityPlusPadding;

		if (largeQuantity == 0) { // no large items
			largeQuantityPlusPadding = "   ";
		} else {
			if (largeQuantity < 10) { // 1 digit, most of the cases
				largeQuantityPlusPadding = largeQuantity + "  ";
			} else {
				largeQuantityPlusPadding = largeQuantity + " "; // for 2 digits. 3 digits and up would not aligned
			}
		}

		return largeQuantityPlusPadding;
	}

	public String getSmallQuantityWithPadding() {
		logger.finest("Entered");
		String smallQuantityPlusPadding;

		if (smallQuantity == 0) { // no large items
			smallQuantityPlusPadding = "     ";
		} else {
			if (smallQuantity < 10) { // 1 digit, most of the cases
				smallQuantityPlusPadding = smallQuantity + "s   ";
			} else {
				smallQuantityPlusPadding = smallQuantity + "s  "; // for 2 digits. 3 digits and up would mis-aligned
			}
		}

		return smallQuantityPlusPadding;
	}


	public String getQuotedDishShortName() {
		return "'" + dishShortName + "'";
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

	public int getDishId() {
		return dishId;
	}

	public void setDishId(int dishId) {
		this.dishId = dishId;
	}

	public String getDishShortName() {
		return dishShortName;
	}

	public void setDishShortName(String dishShortName) {
		this.dishShortName = dishShortName;
	}

	public int getLargeQuantity() {
		return largeQuantity;
	}

	public void setLargeQuantity(int largeQuantity) {
		this.largeQuantity = largeQuantity;
	}

	public int getSmallQuantity() {
		return smallQuantity;
	}

	public void setSmallQuantity(int smallQuantity) {
		this.smallQuantity = smallQuantity;
	}

	public long getOrderedTime() {
		return orderedTime;
	}

	public void setOrderedTime(long orderedTime) {
		this.orderedTime = orderedTime;
	}

	public OrderItemStatus getStatus() {
		return status;
	}

	public void setStatus(OrderItemStatus status) {
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

	public static Comparator<OrderItem> getDishComparator(Menu menu) {
		return new Comparator<OrderItem>() {

			@Override
			public int compare(OrderItem orderItem1, OrderItem orderItem2) {
				Dish dish1 = menu.getDish(orderItem1.getDishId());
				Dish dish2 = menu.getDish(orderItem2.getDishId());

				return dish1.compareTo(dish2);
			}
		};
	}

	@Override
	public int compareTo(OrderItem thatOrderItem) {
		return Comparator.comparing(OrderItem::getOrderItemId).compare(this, thatOrderItem);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (orderedTime ^ (orderedTime >>> 32));
		result = prime * result + dishId;
		result = prime * result + orderItemId;
		result = prime * result + largeQuantity;
		result = prime * result + orderId;
		result = prime * result + smallQuantity;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + (int) (voidedTime ^ (voidedTime >>> 32));
		result = prime * result + voidedById;
		result = prime * result + ((dishShortName == null) ? 0 : dishShortName.hashCode());
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
		OrderItem other = (OrderItem) obj;
		if (orderedTime != other.orderedTime) {
			return false;
		}
		if (dishId != other.dishId) {
			return false;
		}
		if (orderItemId != other.orderItemId) {
			return false;
		}
		if (largeQuantity != other.largeQuantity) {
			return false;
		}
		if (orderId != other.orderId) {
			return false;
		}
		if (smallQuantity != other.smallQuantity) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (voidedTime != other.voidedTime) {
			return false;
		}
		if (voidedById != other.voidedById) {
			return false;
		}
		if (dishShortName == null) {
			if (other.dishShortName != null) {
				return false;
			}
		} else if (!dishShortName.equals(other.dishShortName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return OrderItem.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", orderItemId=" + orderItemId + ", dishId=" + dishId +
				", dishShortName=" + dishShortName +
				", largeQuantity=" + largeQuantity +
				", smallQuantity=" + smallQuantity +
				", orderedTime=" + Util.formatEpochToLocal(orderedTime) +
				", status=" + status +
				", voidedTime=" + Util.formatEpochToLocal(voidedTime) +
				", voidedById=" + voidedById +
				"]";
		// @formatter:on
	}
}
