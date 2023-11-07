package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.CustomerStatus;
import com.orderfoodnow.pos.shared.Util;

/**
 * A delivery order is a to-go order that placed mostly over the phone that's
 * required a delivery. A complete address is required. If an existing customer
 * record is found, the order taker verifies the address on record with the
 * customer. If no customer record found, ask the customer for the complete
 * address and a phone number. A new customer record will be created for future
 * reference.
 */
public class ToGoInfo implements Serializable {
	private int orderId;
	private int customerId = INVALID_CUSTOMER_ID;
	private String phoneNumber = "";
	private String customerName = "";
	private long requestedTime;
	private int queueTimeMinutes;
	private CustomerStatus customerStatus = CustomerStatus.ORDERED;
	private long arrivalTime;
	private String creditCardNumber = "";
	private String note = "";

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ToGoInfo.class.getName());

	public static final int INVALID_CUSTOMER_ID = -1;

	public ToGoInfo() {
	}

	public ToGoInfo(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		orderId = resultSet.getInt(ToGoInfoTable.ORDER_ID);
		customerId = resultSet.getInt(ToGoInfoTable.CUSTOMER_ID);
		phoneNumber = resultSet.getString(ToGoInfoTable.PHONE_NUMBER);
		customerName = resultSet.getString(ToGoInfoTable.CUSTOMER_NAME) == null ? ""
				: resultSet.getString(ToGoInfoTable.CUSTOMER_NAME);
		requestedTime = resultSet.getLong(ToGoInfoTable.CUSTOMER_REQUESTED_TIME);
		queueTimeMinutes = resultSet.getInt(ToGoInfoTable.QUEUE_TIME_MINUTES);
		customerStatus = CustomerStatus.values()[resultSet.getInt(ToGoInfoTable.CUSTOMER_STATUS)];
		arrivalTime = resultSet.getLong(ToGoInfoTable.CUSTOMER_ARRIVAL_TIME);
		creditCardNumber = resultSet.getString(ToGoInfoTable.CREDIT_CARD_NUMBER) == null ? ""
				: resultSet.getString(ToGoInfoTable.CREDIT_CARD_NUMBER);
		note = resultSet.getString(ToGoInfoTable.NOTE) == null ? "" : resultSet.getString(ToGoInfoTable.NOTE);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);

		int expectedIndex = ToGoInfoTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setInt(++parameterIndex, customerId);
		prepStmt.setString(++parameterIndex, phoneNumber);
		prepStmt.setString(++parameterIndex, customerName);
		prepStmt.setLong(++parameterIndex, requestedTime);
		prepStmt.setInt(++parameterIndex, queueTimeMinutes);
		prepStmt.setInt(++parameterIndex, customerStatus.ordinal());
		prepStmt.setLong(++parameterIndex, arrivalTime);
		prepStmt.setString(++parameterIndex, creditCardNumber);
		prepStmt.setString(++parameterIndex, note);

		if (parameterIndex != ToGoInfoTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ ToGoInfoTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + ToGoInfoTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public static ToGoInfo readById(Connection connection, int orderId) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + ToGoInfoTable.ORDER_ID + " FROM "
				+ ToGoInfoTable.TABLE_NAME + " WHERE " + ToGoInfoTable.ORDER_ID + "=?")) {
			prepStmt.setInt(1, orderId);
			logger.finer(prepStmt.toString());
			prepStmt.executeQuery();
			ToGoInfo toGoInfo = new ToGoInfo();
			toGoInfo.setOrderId(orderId);
			toGoInfo.read(connection);
			return toGoInfo;
		} catch (SQLException e) {
			logger.finer("Unable to read order by orderId: " + orderId);
			logger.finer(e.getMessage());
			return null;
		}
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + ToGoInfoTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("UPDATE " + ToGoInfoTable.ALL_COLUMNS_WITH_SET + ToGoInfoTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, ToGoInfoTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + ToGoInfoTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0); // keyStartingIndex = 0;
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public String getFormattedRequestedTime() {
		logger.finest("Entered");
		return Util.formatTimeNoSeconds(requestedTime);
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public long getRequestedTime() {
		return requestedTime;
	}

	public void setRequestedTime(long requestedTime) {
		this.requestedTime = requestedTime;
	}

	public int getQueueTimeMinutes() {
		return queueTimeMinutes;
	}

	public void setQueueTimeMinutes(int queueTimeMinutes) {
		this.queueTimeMinutes = queueTimeMinutes;
	}

	public CustomerStatus getCustomerStatus() {
		return customerStatus;
	}

	public void setCustomerStatus(CustomerStatus customerStatus) {
		this.customerStatus = customerStatus;
	}

	public long getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getFormattedPhoneNumber() {
		logger.finest("Entered");
		if (phoneNumber.isEmpty() == false) {
			String areaCode = phoneNumber.substring(0, 3);
			String prefix = phoneNumber.substring(3, 6);
			String number = phoneNumber.substring(6);
			String formattedPhoneNumber = "(" + areaCode + ") " + prefix + "-" + number;
			return formattedPhoneNumber;
		} else
			return phoneNumber;
	}

	public void clearCustomerProfile() {
		logger.finest("Entered");
		customerId = INVALID_CUSTOMER_ID;
		phoneNumber = "";
		customerName = "";
		note = "";
	}

	public void populate(CustomerProfile customerProfile) {
		logger.finest("Entered");
		customerId = customerProfile.getCustomerId();
		phoneNumber = customerProfile.getPhoneNumber();

		if (customerName.isEmpty()) {
			customerName = customerProfile.getName();
		}

		if (note.isEmpty()) {
			note = customerProfile.getNote();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (arrivalTime ^ (arrivalTime >>> 32));
		result = prime * result + ((creditCardNumber == null) ? 0 : creditCardNumber.hashCode());
		result = prime * result + customerId;
		result = prime * result + ((customerName == null) ? 0 : customerName.hashCode());
		result = prime * result + ((customerStatus == null) ? 0 : customerStatus.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result + orderId;
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + queueTimeMinutes;
		result = prime * result + (int) (requestedTime ^ (requestedTime >>> 32));
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
		ToGoInfo other = (ToGoInfo) obj;
		if (arrivalTime != other.arrivalTime) {
			return false;
		}
		if (creditCardNumber == null) {
			if (other.creditCardNumber != null) {
				return false;
			}
		} else if (!creditCardNumber.equals(other.creditCardNumber)) {
			return false;
		}
		if (customerId != other.customerId) {
			return false;
		}
		if (customerName == null) {
			if (other.customerName != null) {
				return false;
			}
		} else if (!customerName.equals(other.customerName)) {
			return false;
		}
		if (customerStatus != other.customerStatus) {
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
		if (phoneNumber == null) {
			if (other.phoneNumber != null) {
				return false;
			}
		} else if (!phoneNumber.equals(other.phoneNumber)) {
			return false;
		}
		if (queueTimeMinutes != other.queueTimeMinutes) {
			return false;
		}
		if (requestedTime != other.requestedTime) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return ToGoInfo.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", customerId=" + customerId +
				", phoneNumber=" + phoneNumber +
				", customerName=" + customerName +
				", requestedTime=" + Util.formatEpochToLocal(requestedTime) +
				", queueTimeMinutes=" + Util.formatEpochToLocal(queueTimeMinutes) +
				", customerStatus=" + customerStatus +
				", arrivalTime=" + Util.formatEpochToLocal(arrivalTime) +
				//sensitive information ", creditCardNumber=" + creditCardNumber + 
				", note=" + note +
				"]";
		// @formatter:on
	}
}
