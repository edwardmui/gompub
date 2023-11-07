package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Util;

/**
 * This class records the settle entry into the database.
 */
public class Settle implements Serializable {
	private int settleId; // assigned by DB auto increment at insert time
	private long settledTime = System.currentTimeMillis();
	private int settledById;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Settle.class.getName());

	public Settle() {
	}

	public Settle(int settledById) {
		logger.finest("Entered");
		this.settledById = settledById;
	}

	public Settle(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		settleId = resultSet.getInt(SettleTable.SETTLE_ID);
		settledTime = resultSet.getLong(SettleTable.SETTLED_TIME);
		settledById = resultSet.getInt(SettleTable.SETTLED_BY_ID);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, settleId);

		int expectedIndex = SettleTable.NUM_KEY_COLUMNS + startIndex;
		if (parameterIndex != expectedIndex) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect=" + expectedIndex
					+ " parameters. Actual=" + parameterIndex);
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

		prepStmt.setLong(++parameterIndex, settledTime);
		prepStmt.setInt(++parameterIndex, settledById);

		int expectedParameterCount = SettleTable.NUM_COLUMNS - SettleTable.NUM_AUTOINC_COLUMNS + startIndex;
		if (parameterIndex != expectedParameterCount) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ expectedParameterCount + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement(
				"INSERT INTO " + SettleTable.ALL_COLUMNS_MINUS_AUTOINC, Statement.RETURN_GENERATED_KEYS)) {
			fillPreparedStatementWithAllFieldsMinusAutoInc(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
			ResultSet resultSet = prepStmt.getGeneratedKeys();
			resultSet.next();
			settleId = resultSet.getInt(1);
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + SettleTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public static Settle readById(Connection connection, int settleId) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + SettleTable.SETTLE_ID + " FROM "
				+ SettleTable.TABLE_NAME + " WHERE " + SettleTable.SETTLE_ID + "=?")) {
			prepStmt.setInt(1, settleId);
			logger.finer(prepStmt.toString());
			prepStmt.executeQuery();
			Settle settle = new Settle();
			settle.setSettleId(settleId);
			settle.read(connection);
			return settle;
		} catch (SQLException e) {
			logger.finer("Unable to read by settleId: " + settleId);
			logger.finer(e.getMessage());
			return null;
		}
	}

	public int getSettleId() {
		return settleId;
	}

	public void setSettleId(int settleId) {
		this.settleId = settleId;
	}

	public long getSettledTime() {
		return settledTime;
	}

	public void setSettledTime(long settledTime) {
		this.settledTime = settledTime;
	}

	public int getSettledById() {
		return settledById;
	}

	public void setSettledById(int settledById) {
		this.settledById = settledById;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + settleId;
		result = prime * result + settledById;
		result = prime * result + (int) (settledTime ^ (settledTime >>> 32));
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
		Settle other = (Settle) obj;
		if (settleId != other.settleId) {
			return false;
		}
		if (settledById != other.settledById) {
			return false;
		}
		if (settledTime != other.settledTime) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Settle.class.getSimpleName() +
				"[" +
				"settleId=" + settleId +
				", settledTime=" + Util.formatEpochToLocal(settledTime) +
				", settledById=" + settledById +
				"]";
		// @formatter:on
	}
}
