package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.PaymentType;

public class Payment implements Serializable, Comparable<Payment> {
	private int orderId;
	private int paymentId;
	private PaymentType type;
	private int amount;
	private int tip;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Payment.class.getName());

	public Payment() {
	}

	public Payment(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(PaymentTable.ORDER_ID);
		paymentId = resultSet.getInt(PaymentTable.PAYMENT_ID);
		type = PaymentType.values()[resultSet.getInt(PaymentTable.TYPE)];
		amount = resultSet.getInt(PaymentTable.AMOUNT);
		tip = resultSet.getInt(PaymentTable.TIP);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);
		prepStmt.setInt(++parameterIndex, paymentId);

		int expectedIndex = PaymentTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setInt(++parameterIndex, paymentId);
		prepStmt.setInt(++parameterIndex, type.ordinal());
		prepStmt.setInt(++parameterIndex, amount);
		prepStmt.setInt(++parameterIndex, tip);

		if (parameterIndex != PaymentTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ PaymentTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + PaymentTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + PaymentTable.TABLENAME_ANDING_ALL_KEYS)) {
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
				.prepareStatement("UPDATE " + PaymentTable.ALL_COLUMNS_WITH_SET + PaymentTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, PaymentTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + PaymentTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getPaymentIdPlus1() {
		return paymentId + 1;
	}

	public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public PaymentType getType() {
		return type;
	}

	public void setType(PaymentType type) {
		this.type = type;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getTip() {
		return tip;
	}

	public void setTip(int tip) {
		this.tip = tip;
	}

	@Override
	public int compareTo(Payment thatPayment) {
		return Comparator.comparing(Payment::getType).thenComparing(Payment::getPaymentId).compare(this, thatPayment);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + paymentId;
		result = prime * result + orderId;
		result = prime * result + tip;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Payment other = (Payment) obj;
		if (amount != other.amount) {
			return false;
		}
		if (paymentId != other.paymentId) {
			return false;
		}
		if (orderId != other.orderId) {
			return false;
		}
		if (tip != other.tip) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Payment.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", paymentId=" + paymentId +
				", type=" + type +
				", amount=" + amount +
				", tip=" + tip +
				"]";
		// @formatter:on
	}
}
