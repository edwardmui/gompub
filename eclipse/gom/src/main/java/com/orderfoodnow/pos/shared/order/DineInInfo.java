package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This class class contains information related to a dine-in order. A dine-in
 * order is placed by a serverId and to be served on premise.
 */
public class DineInInfo implements Serializable {
	private int orderId;
	private String tableNumber = "";
	private int guestCount;
	private int serverId;
	private int tip;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DineInInfo.class.getName());

	public DineInInfo() {
	}

	public DineInInfo(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(DineInInfoTable.ORDER_ID);
		tableNumber = resultSet.getString(DineInInfoTable.TABLE_NUMBER) == null ? ""
				: resultSet.getString(DineInInfoTable.TABLE_NUMBER);
		guestCount = resultSet.getInt(DineInInfoTable.GUEST_COUNT);
		serverId = resultSet.getInt(DineInInfoTable.SERVER_ID);
		tip = resultSet.getInt(DineInInfoTable.TIP);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);

		int expectedIndex = DineInInfoTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setString(++parameterIndex, tableNumber);
		prepStmt.setInt(++parameterIndex, guestCount);
		prepStmt.setInt(++parameterIndex, serverId);
		prepStmt.setInt(++parameterIndex, tip);

		if (parameterIndex != DineInInfoTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ DineInInfoTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + DineInInfoTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + DineInInfoTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public static DineInInfo readById(Connection connection, int orderId) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + DineInInfoTable.ORDER_ID + " FROM "
				+ DineInInfoTable.TABLE_NAME + " WHERE " + DineInInfoTable.ORDER_ID + "=?")) {
			prepStmt.setInt(1, orderId);
			logger.finer(prepStmt.toString());
			prepStmt.executeQuery();
			DineInInfo dineIn = new DineInInfo();
			dineIn.setOrderId(orderId);
			dineIn.read(connection);
			return dineIn;
		} catch (SQLException e) {
			logger.finer("Unable to read order by orderId: " + orderId);
			logger.finer(e.getMessage());
			return null;
		}
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("UPDATE " + DineInInfoTable.ALL_COLUMNS_WITH_SET + DineInInfoTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, DineInInfoTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public boolean isEqualInDB(Connection connection, int orderId) {
		logger.finest("Entered");
		return equals(readById(connection, orderId));
	}

	public boolean isServerValid() {
		return serverId > 0;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(String tableNumber) {
		this.tableNumber = tableNumber;
	}

	public int getGuestCount() {
		return guestCount;
	}

	public void setGuestCount(int guestCount) {
		this.guestCount = guestCount;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getTip() {
		return tip;
	}

	public void setTip(int tip) {
		this.tip = tip;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + guestCount;
		result = prime * result + orderId;
		result = prime * result + serverId;
		result = prime * result + tip;
		result = prime * result + ((tableNumber == null) ? 0 : tableNumber.hashCode());
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
		DineInInfo other = (DineInInfo) obj;
		if (guestCount != other.guestCount) {
			return false;
		}
		if (orderId != other.orderId) {
			return false;
		}
		if (serverId != other.serverId) {
			return false;
		}
		if (tip != other.tip) {
			return false;
		}
		if (tableNumber == null) {
			if (other.tableNumber != null) {
				return false;
			}
		} else if (!tableNumber.equals(other.tableNumber)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return DineInInfo.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", tableNumber=" + tableNumber +
				", guestCount=" + guestCount +
				", serverId=" + serverId +
				", tip=" + tip +
				"]";
		// @formatter:on
	}
}
