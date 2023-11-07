package com.orderfoodnow.pos.shared.order;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.SharedConstDefs;
import com.orderfoodnow.pos.shared.Util;

/**
 * This class contains information related to a delivery order. A delivery order
 * is a to-go order that placed mostly over the phone that's required a driver
 * to delivery. A complete address is required. If an existing customer record
 * is found, the order taker verifies the address on record with the customer.
 * If no customer record found, the order taker asks the customer for the
 * complete address and a phone number. Using the collected information, a new
 * customer profile record will be created for future reference. The estimated
 * arrival time is solely based on this one order.
 */
// It would be nice if other delivery orders can be combined
// to form a delivery route and have estimate time for each drop.
// That would require lots of map data and grouping orders into routes
// based on the number of drivers.
public class DeliveryInfo implements Serializable {
	private int orderId;
	private int deliveryCharge;
	private int driverId;
	private String street = "";
	private String city = "";
	private String state = "";
	private String zip = "";
	private double latitude;
	private double longitude;
	private String drivingDirection = "";
	private int drivingDurationMinutes;
	private float drivingDistance;
	private long estimatedArrivalTime;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DeliveryInfo.class.getName());
	private boolean updateCustomerProfileNeeded = false;

	public DeliveryInfo() {
	}

	public DeliveryInfo(ResultSet resultSet) throws SQLException {
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

		orderId = resultSet.getInt(DeliveryInfoTable.ORDER_ID);
		deliveryCharge = resultSet.getInt(DeliveryInfoTable.DELIVERY_CHARGE);
		driverId = resultSet.getInt(DeliveryInfoTable.DRIVER_ID);
		street = resultSet.getString(DeliveryInfoTable.STREET) == null ? ""
				: resultSet.getString(DeliveryInfoTable.STREET);
		city = resultSet.getString(DeliveryInfoTable.CITY) == null ? "" : resultSet.getString(DeliveryInfoTable.CITY);
		state = resultSet.getString(DeliveryInfoTable.STATE) == null ? ""
				: resultSet.getString(DeliveryInfoTable.STATE);
		zip = resultSet.getString(DeliveryInfoTable.ZIP) == null ? "" : resultSet.getString(DeliveryInfoTable.ZIP);
		latitude = resultSet.getFloat(DeliveryInfoTable.LATITUDE);
		longitude = resultSet.getFloat(DeliveryInfoTable.LONGITUDE);
		drivingDirection = resultSet.getString(DeliveryInfoTable.DRIVING_DIRECTION);
		drivingDurationMinutes = resultSet.getInt(DeliveryInfoTable.DRIVING_DURATION_MINUTES);
		drivingDistance = resultSet.getInt(DeliveryInfoTable.DRIVING_DISTANCE);
		estimatedArrivalTime = resultSet.getLong(DeliveryInfoTable.ESTIMATED_TIME_ARRIVAL);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, orderId);

		int expectedIndex = DeliveryInfoTable.NUM_KEY_COLUMNS + startIndex;
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
		prepStmt.setInt(++parameterIndex, deliveryCharge);
		prepStmt.setInt(++parameterIndex, driverId);
		prepStmt.setString(++parameterIndex, street);
		prepStmt.setString(++parameterIndex, city);
		prepStmt.setString(++parameterIndex, state);
		prepStmt.setString(++parameterIndex, zip);
		prepStmt.setDouble(++parameterIndex, latitude);
		prepStmt.setDouble(++parameterIndex, longitude);
		prepStmt.setString(++parameterIndex, drivingDirection);
		prepStmt.setInt(++parameterIndex, drivingDurationMinutes);
		prepStmt.setFloat(++parameterIndex, drivingDistance);
		prepStmt.setLong(++parameterIndex, estimatedArrivalTime);

		if (parameterIndex != DeliveryInfoTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ DeliveryInfoTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + DeliveryInfoTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public static DeliveryInfo readById(Connection connection, int orderId) {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("SELECT " + DeliveryInfoTable.ORDER_ID + " FROM "
				+ DeliveryInfoTable.TABLE_NAME + " WHERE " + DeliveryInfoTable.ORDER_ID + "=?")) {
			prepStmt.setInt(1, orderId);
			logger.finer(prepStmt.toString());
			prepStmt.executeQuery();
			DeliveryInfo deliveryInfo = new DeliveryInfo();
			deliveryInfo.setOrderId(orderId);
			deliveryInfo.read(connection);
			return deliveryInfo;
		} catch (SQLException e) {
			logger.finer("Unable to read order by orderId: " + orderId);
			logger.finer(e.getMessage());
			return null;
		}
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + DeliveryInfoTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement(
				"UPDATE " + DeliveryInfoTable.ALL_COLUMNS_WITH_SET + DeliveryInfoTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, DeliveryInfoTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + DeliveryInfoTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0); // keyStartingIndex = 0;
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	// http://maps.google.com/maps?f=q&hl=en&q=4944+N+Lowell+Chicago+IL+60640
	// latitude and longitude can be used to on google map also
	public String getGoogleMapUrl() {
		logger.finest("Entered");
		if (isAddressEmpty()) {
			return "";
		}

		String url = street + " " + city + " " + state + " " + zip;
		url = url.trim();
		return url.replace(' ', '+');
	}

	public boolean isAddressEmpty() {
		logger.finest("Entered");
		return (street.isEmpty() && city.isEmpty() && state.isEmpty() && zip.isEmpty());
	}

	public boolean isDriverValid() {
		return driverId > 0;
	}

	public String getAddress() {
		logger.finest("Entered");
		String address = "";

		if (street != null && street.isEmpty() == false) {
			address += street;
		}

		if (city != null && city.isEmpty() == false) {
			address = address + SharedConstDefs.DELIMITER + " " + city;
		}

		if (state != null && state.isEmpty() == false) {
			address = address + SharedConstDefs.DELIMITER + " " + state;
		}

		if (zip != null && zip.isEmpty() == false) {
			address = address + SharedConstDefs.DELIMITER + " " + zip;
		}

		return address;
	}

	public String getAddressNoSpace() {
		logger.finest("Entered");
		String address = "";

		if (street != null && street.isEmpty() == false) {
			address += street;
		}

		if (city != null && city.isEmpty() == false) {
			address = address + SharedConstDefs.DELIMITER + city;
		}

		if (state != null && state.isEmpty() == false) {
			address = address + SharedConstDefs.DELIMITER + state;
		}

		if (zip != null && zip.isEmpty() == false) {
			address = address + SharedConstDefs.DELIMITER + zip;
		}

		return address;
	}

	public void populate(CustomerProfile customerProfile) {
		logger.finest("Entered");
		// Always populate with value in customerProfile regardless the current delivery information.
		// If not, a wrong customer profile could have been pulled up, populated the information.
		// then the correct customer could inherit the wrong customer's information 

		deliveryCharge = Configuration.getDeliveryCostAt(customerProfile.getDeliveryZone());
		street = customerProfile.getStreet();
		city = customerProfile.getCity();
		state = customerProfile.getState();
		zip = customerProfile.getZip();
		latitude = customerProfile.getLatitude();
		longitude = customerProfile.getLongitude();
		drivingDirection = customerProfile.getDrivingDirection();
		drivingDurationMinutes = customerProfile.getDrivingDurationMinutes();
		drivingDistance = customerProfile.getDrivingDistance();
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getDeliveryCharge() {
		return deliveryCharge;
	}

	public void setDeliveryCharge(int deliveryCharge) {
		this.deliveryCharge = deliveryCharge;
	}

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
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

	public long getEstimatedArrivalTime() {
		return estimatedArrivalTime;
	}

	public void setEstimatedArrivalTime(long estimatedArrivalTime) {
		this.estimatedArrivalTime = estimatedArrivalTime;
	}

	public boolean isUpdateCustomerProfileNeeded() {
		return updateCustomerProfileNeeded;
	}

	public void setUpdateCustomerProfileNeeded(boolean updateCustomerProfileNeeded) {
		this.updateCustomerProfileNeeded = updateCustomerProfileNeeded;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + deliveryCharge;
		result = prime * result + driverId;
		result = prime * result + ((drivingDirection == null) ? 0 : drivingDirection.hashCode());
		result = prime * result + Float.floatToIntBits(drivingDistance);
		result = prime * result + drivingDurationMinutes;
		result = prime * result + (int) (estimatedArrivalTime ^ (estimatedArrivalTime >>> 32));
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + orderId;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + (updateCustomerProfileNeeded ? 1231 : 1237);
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
		DeliveryInfo other = (DeliveryInfo) obj;
		if (city == null) {
			if (other.city != null) {
				return false;
			}
		} else if (!city.equals(other.city)) {
			return false;
		}
		if (deliveryCharge != other.deliveryCharge) {
			return false;
		}
		if (driverId != other.driverId) {
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
		if (estimatedArrivalTime != other.estimatedArrivalTime) {
			return false;
		}
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude)) {
			return false;
		}
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude)) {
			return false;
		}
		if (orderId != other.orderId) {
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
		if (updateCustomerProfileNeeded != other.updateCustomerProfileNeeded) {
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
		return DeliveryInfo.class.getSimpleName() +
				"[" +
				"orderId=" + orderId +
				", deliveryCharge=" + deliveryCharge +
				", driverId=" + driverId +
				", street=" + street +
				", city=" + city +
				", state=" + state +
				", zip=" + zip +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", drivingDirection=" + drivingDirection +
				", drivingDurationMinutes=" + drivingDurationMinutes +
				", drivingDistance=" + drivingDistance +
				", estimatedArrivalTime=" + Util.formatEpochToLocal(estimatedArrivalTime) +
				", updateCustomerProfileNeeded=" + updateCustomerProfileNeeded +
				"]";
		// @formatter:on
	}
}
