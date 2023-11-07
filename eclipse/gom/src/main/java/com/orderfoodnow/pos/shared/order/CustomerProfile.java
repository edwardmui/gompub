package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.Util;

/**
 * The CustomerProfile class's main purpose is to collect customer information
 * over time from the Order There's no formal CustomerProfile profile creation.
 * As such CustomerProfile information is mainly collected during customer
 * ordering.
 *
 * CustomerProfile is searched by phone number and it is used to populate
 * customer information at order time. During order time, if any changed in
 * customer related attributes, those changes will be used to update the
 * customer profile.
 */
public class CustomerProfile implements Serializable {
	private int customerId;
	private String phoneNumber = "";
	private String name = "";
	private String street = "";
	private String city = "";
	private String state = "";
	private String zip = "";
	private double latitude;
	private double longitude;
	private String email = "";
	private int deliveryZone;
	private String drivingDirection = "";
	private int drivingDurationMinutes;
	private float drivingDistance;
	private String note = "";
	private long firstOrderTime;
	private long latestOrderTime;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CustomerProfile.class.getName());

	public CustomerProfile() {
	}

	public CustomerProfile(Cart cart, CustomerProfile customerProfile) throws SQLException {
		logger.finest("Entered");
		Order order = cart.getOrder();
		ToGoInfo toGoInfo = cart.getToGoInfo();
		DeliveryInfo deliveryInfo = cart.getDeliveryInfo();

		customerId = toGoInfo.getCustomerId();
		phoneNumber = toGoInfo.getPhoneNumber();
		name = toGoInfo.getCustomerName();
		street = deliveryInfo.getStreet();
		city = deliveryInfo.getCity();
		state = deliveryInfo.getState();
		zip = deliveryInfo.getZip();
		if (customerProfile != null) {
			latitude = customerProfile.getLatitude();
			longitude = customerProfile.getLongitude();
			email = customerProfile.getEmail();
			deliveryZone = customerProfile.getDeliveryZone();
			drivingDirection = customerProfile.getDrivingDirection();
			drivingDurationMinutes = customerProfile.getDrivingDurationMinutes();
			drivingDistance = customerProfile.getDrivingDistance();
		}
		note = toGoInfo.getNote();
		firstOrderTime = order.getOrderedTime();
		latestOrderTime = order.getOrderedTime();
	}

	public CustomerProfile(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		customerId = resultSet.getInt(CustomerProfileTable.CUSTOMER_ID);
		phoneNumber = resultSet.getString(CustomerProfileTable.PHONE_NUMBER);
		name = resultSet.getString(CustomerProfileTable.NAME);
		street = resultSet.getString(CustomerProfileTable.STREET);
		city = resultSet.getString(CustomerProfileTable.CITY);
		state = resultSet.getString(CustomerProfileTable.STATE);
		zip = resultSet.getString(CustomerProfileTable.ZIP);
		latitude = resultSet.getDouble(CustomerProfileTable.LATITUDE);
		longitude = resultSet.getDouble(CustomerProfileTable.LONGITUDE);
		email = resultSet.getString(CustomerProfileTable.EMAIL);
		deliveryZone = resultSet.getInt(CustomerProfileTable.DELIVERY_ZONE);
		drivingDirection = resultSet.getString(CustomerProfileTable.DRIVING_DIRECTION);
		drivingDurationMinutes = resultSet.getInt(CustomerProfileTable.DRIVING_DURATION_MINUTES);
		drivingDistance = resultSet.getFloat(CustomerProfileTable.DRIVING_DISTANCE);
		note = resultSet.getString(CustomerProfileTable.NOTE);
		firstOrderTime = resultSet.getDate(CustomerProfileTable.FIRST_ORDER_DATE) == null ? 0
				: resultSet.getDate(CustomerProfileTable.FIRST_ORDER_DATE).getTime();
		latestOrderTime = resultSet.getDate(CustomerProfileTable.LATEST_ORDER_DATE) == null ? 0
				: resultSet.getDate(CustomerProfileTable.LATEST_ORDER_DATE).getTime();
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, customerId);

		int expectedIndex = CustomerProfileTable.NUM_KEY_COLUMNS + startIndex;
		if (parameterIndex != expectedIndex) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect=" + expectedIndex
					+ " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatementWithAllFields(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, customerId);

		fillPreparedStatement(prepStmt, parameterIndex);

		if (parameterIndex != OrderTable.NUM_AUTOINC_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ OrderTable.NUM_AUTOINC_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatement(PreparedStatement prepStmt, int startIndex) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setString(++parameterIndex, phoneNumber);
		prepStmt.setString(++parameterIndex, name);
		prepStmt.setString(++parameterIndex, street);
		prepStmt.setString(++parameterIndex, city);
		prepStmt.setString(++parameterIndex, state);
		prepStmt.setString(++parameterIndex, zip);
		prepStmt.setDouble(++parameterIndex, latitude);
		prepStmt.setDouble(++parameterIndex, longitude);
		prepStmt.setString(++parameterIndex, email);
		prepStmt.setInt(++parameterIndex, deliveryZone);
		prepStmt.setString(++parameterIndex, drivingDirection);
		prepStmt.setInt(++parameterIndex, drivingDurationMinutes);
		prepStmt.setFloat(++parameterIndex, drivingDistance);
		prepStmt.setString(++parameterIndex, note);
		prepStmt.setDate(++parameterIndex, firstOrderTime == 0 ? null : new Date(firstOrderTime));
		prepStmt.setDate(++parameterIndex, latestOrderTime == 0 ? null : new Date(latestOrderTime));

		int expectedParameterCount = CustomerProfileTable.NUM_COLUMNS - CustomerProfileTable.NUM_AUTOINC_COLUMNS
				+ startIndex;
		if (parameterIndex != expectedParameterCount) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ expectedParameterCount + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatementWithAllFieldsMinusAutoInc(PreparedStatement prepStmt)
			throws SQLException {
		logger.finest("Entered");
		fillPreparedStatement(prepStmt, 0);

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement(
				"INSERT INTO " + CustomerProfileTable.ALL_COLUMNS_MINUS_AUTOINC, Statement.RETURN_GENERATED_KEYS)) {
			fillPreparedStatementWithAllFieldsMinusAutoInc(prepStmt);
			logger.fine(prepStmt.toString());
			prepStmt.executeUpdate();
			ResultSet resultSet = prepStmt.getGeneratedKeys();
			resultSet.next();
			customerId = resultSet.getInt(1); // save the customerId that was assigned by DB's auto increment
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + CustomerProfileTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public static CustomerProfile readByPhone(Connection connection, String phoneNumber) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + CustomerProfileTable.CUSTOMER_ID
				+ " FROM " + CustomerProfileTable.TABLE_NAME + " USE INDEX (" + CustomerProfileTable.INDEX_NAME
				+ ") WHERE " + CustomerProfileTable.PHONE_NUMBER + "=?")) {
			prepStmt.setString(1, phoneNumber);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			CustomerProfile customer = new CustomerProfile();
			resultSet.first();
			customer.setCustomerId(resultSet.getInt(CustomerProfileTable.CUSTOMER_ID));
			customer.read(connection);
			return customer;
		} catch (SQLException e) {
			logger.finer("Unable to read customer by phoneNumber: " + phoneNumber);
			logger.finer(e.getMessage());
			return null;
		}
	}

	public static CustomerProfile readById(Connection connection, int customerId) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + CustomerProfileTable.CUSTOMER_ID
				+ " FROM " + CustomerProfileTable.TABLE_NAME + " WHERE " + CustomerProfileTable.CUSTOMER_ID + "=?")) {
			prepStmt.setInt(1, customerId);
			logger.finer(prepStmt.toString());
			prepStmt.executeQuery();
			CustomerProfile customer = new CustomerProfile();
			customer.setCustomerId(customerId);
			customer.read(connection);
			return customer;
		} catch (SQLException e) {
			logger.finer("Unable to read customer by customerId: " + customerId);
			logger.finer(e.getMessage());
			return null;
		}
	}

	// @formatter:off
//	public void updateName(Connection connection) throws SQLException {
//		logger.finest("Entered");
//		try (PreparedStatement prepStmt = connection.prepareStatement("UPDATE " + CustomerProfileTable.TABLE_NAME
//				+ " SET " + CustomerProfileTable.NAME + "=? " + CustomerProfileTable.ANDING_ALL_KEYS)) {
//			int parameterIndex = 0;
//			prepStmt.setString(++parameterIndex, name);
//			fillPreparedStatementWithKeys(prepStmt, parameterIndex);
//			logger.finer(prepStmt.toString());
//			prepStmt.executeUpdate();
//		}
//	}
	// @formatter:on

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement(
				"UPDATE " + CustomerProfileTable.ALL_COLUMNS_WITH_SET + CustomerProfileTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, CustomerProfileTable.NUM_COLUMNS);
			logger.fine(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + CustomerProfileTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getDeliveryZone() {
		return deliveryZone;
	}

	public void setDeliveryZone(int deliveryZone) {
		this.deliveryZone = deliveryZone;
	}

	public String getDrivingDirection() {
		return drivingDirection;
	}

	public void setDrivingDirection(String drivingDirection) {
		this.drivingDirection = drivingDirection;
	}

	public int getDrivingDurationMinutes() {
		return drivingDurationMinutes;
	}

	public void setDrivingDurationMinutes(int drivingDurationMinutes) {
		this.drivingDurationMinutes = drivingDurationMinutes;
	}

	public float getDrivingDistance() {
		return drivingDistance;
	}

	public void setDrivingDistance(float drivingDistance) {
		this.drivingDistance = drivingDistance;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public long getFirstOrderTime() {
		return firstOrderTime;
	}

	public void setFirstOrderTime(long firstOrderTime) {
		this.firstOrderTime = firstOrderTime;
	}

	public long getLatestOrderTime() {
		return latestOrderTime;
	}

	public void setLatestOrderTime(long latestOrderTime) {
		this.latestOrderTime = latestOrderTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + customerId;
		result = prime * result + deliveryZone;
		result = prime * result + ((drivingDirection == null) ? 0 : drivingDirection.hashCode());
		result = prime * result + Float.floatToIntBits(drivingDistance);
		result = prime * result + drivingDurationMinutes;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (int) (firstOrderTime ^ (firstOrderTime >>> 32));
		result = prime * result + (int) (latestOrderTime ^ (latestOrderTime >>> 32));
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
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
		CustomerProfile other = (CustomerProfile) obj;
		if (city == null) {
			if (other.city != null) {
				return false;
			}
		} else if (!city.equals(other.city)) {
			return false;
		}
		if (customerId != other.customerId) {
			return false;
		}
		if (deliveryZone != other.deliveryZone) {
			return false;
		}
		if (drivingDirection == null) {
			if (other.drivingDirection != null) {
				return false;
			}
		} else if (!drivingDirection.equals(other.drivingDirection)) {
			return false;
		}
		if (Float.floatToIntBits(drivingDistance) != Float.floatToIntBits(other.drivingDistance)) {
			return false;
		}
		if (drivingDurationMinutes != other.drivingDurationMinutes) {
			return false;
		}
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (firstOrderTime != other.firstOrderTime) {
			return false;
		}
		if (latestOrderTime != other.latestOrderTime) {
			return false;
		}
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude)) {
			return false;
		}
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (note == null) {
			if (other.note != null) {
				return false;
			}
		} else if (!note.equals(other.note)) {
			return false;
		}
		if (phoneNumber == null) {
			if (other.phoneNumber != null) {
				return false;
			}
		} else if (!phoneNumber.equals(other.phoneNumber)) {
			return false;
		}
		if (state == null) {
			if (other.state != null) {
				return false;
			}
		} else if (!state.equals(other.state)) {
			return false;
		}
		if (street == null) {
			if (other.street != null) {
				return false;
			}
		} else if (!street.equals(other.street)) {
			return false;
		}
		if (zip == null) {
			if (other.zip != null) {
				return false;
			}
		} else if (!zip.equals(other.zip)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return CustomerProfile.class.getSimpleName() +
				"[" +
				"customerId=" + customerId +
				", phoneNumber=" + phoneNumber +
				", name=" + name +
				", street=" + street +
				", city=" + city +
				", state=" + state +
				", zip=" + zip +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", email=" + email +
				", deliveryZone=" + deliveryZone +
				", drivingDirection=" + drivingDirection +
				", drivingDurationMinutes=" + drivingDurationMinutes +
				", drivingDistance=" + drivingDistance +
				", note=" + note +
				", firstOrderTime=" + Util.formatEpochToLocal(firstOrderTime) +
				", latestOrderTime=" + Util.formatEpochToLocal(latestOrderTime) +
				"]";
		// @formatter:on
	}
}