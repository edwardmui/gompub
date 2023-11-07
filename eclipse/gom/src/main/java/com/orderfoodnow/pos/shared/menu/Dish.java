package com.orderfoodnow.pos.shared.menu;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Dish implements Serializable, Comparable<Dish> {
	private int dishId;
	private boolean active;
	private int category;
	private int code;
	private String shortName;
	private String chineseName;
	private int largePrice;
	private int smallPrice;
	private long toGoStationBits;
	private long dineInStationBits;
	private boolean availableOnline;
	private String fullName;
	private String description;

	// The following attributes that are not stored in the DB dish table hence not
	// in the resultSet. The setter method must be used to populate them
	// before proper dish construction.
	private Map<Integer, String> idToAbbreviation;
	private Map<String, List<Float>> condimentNameToQuantities;
	private CouponDish couponDish;
	private List<Subdish> subdishes;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Dish.class.getName());

	// Which work stations has to handle the dish work, support up to 63
	// workstations, the bit size of Long.
	public static Map<Integer, Long> STATION_MASK = new HashMap<>();
	static {
		long stationMask;
		for (int stationId = 1; stationId < 64; ++stationId) {
			stationMask = 1;
			stationMask = stationMask << (stationId - 1);
			STATION_MASK.put(stationId, stationMask); // Note, there is no stationId of 0 in this Map.
		}
	}

	public Dish() {
	}

	public Dish(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		dishId = resultSet.getInt(DishTable.DISH_ID);
		active = resultSet.getBoolean(DishTable.ACTIVE);
		category = resultSet.getInt(DishTable.CATEGORY);
		code = resultSet.getInt(DishTable.CODE);
		shortName = resultSet.getString(DishTable.SHORT_NAME);
		chineseName = resultSet.getString(DishTable.CHINESE_NAME);
		largePrice = resultSet.getInt(DishTable.LARGE_PRICE);
		smallPrice = resultSet.getInt(DishTable.SMALL_PRICE);
		toGoStationBits = resultSet.getLong(DishTable.TO_GO_STATION_BITS);
		dineInStationBits = resultSet.getLong(DishTable.DINE_IN_STATION_BITS);
		availableOnline = resultSet.getBoolean(DishTable.AVAILABLE_ONLINE);
		fullName = resultSet.getString(DishTable.FULL_NAME);
		description = resultSet.getString(DishTable.DESCRIPTION);
		// idToAbbreviation and condimentNameToQuantities are part of the dish,
		// but not in the passed in resultSet.
		// To have proper Dish construction, the caller need to invoke the
		// respected setters.
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, dishId);

		int expectedIndex = DishTable.NUM_KEY_COLUMNS + startIndex;
		if (parameterIndex != expectedIndex) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect=" + expectedIndex
					+ " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public PreparedStatement fillPreparedStatementWithAllFields(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, dishId);
		prepStmt.setBoolean(++parameterIndex, active);
		prepStmt.setInt(++parameterIndex, category);
		prepStmt.setInt(++parameterIndex, code);
		prepStmt.setString(++parameterIndex, shortName);
		prepStmt.setString(++parameterIndex, chineseName);
		prepStmt.setInt(++parameterIndex, largePrice);
		prepStmt.setInt(++parameterIndex, smallPrice);
		prepStmt.setLong(++parameterIndex, toGoStationBits);
		prepStmt.setLong(++parameterIndex, dineInStationBits);
		prepStmt.setBoolean(++parameterIndex, availableOnline);
		prepStmt.setString(++parameterIndex, fullName);
		prepStmt.setString(++parameterIndex, description);

		if (parameterIndex != DishTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ DishTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + DishTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.fine(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + DishTable.TABLENAME_ANDING_ALL_KEYS)) {
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
				.prepareStatement("UPDATE " + DishTable.ALL_COLUMNS_WITH_SET + DishTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, DishTable.NUM_COLUMNS);
			logger.fine(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	// bulk update
	public static void update(List<Dish> dishes, Connection connection) throws SQLException {
		logger.finest("Entered");
		for (Dish dish : dishes) {
			try (PreparedStatement prepStmt = connection
					.prepareStatement("UPDATE " + DishTable.ALL_COLUMNS_WITH_SET + DishTable.ANDING_ALL_KEYS)) {
				dish.fillPreparedStatementWithAllFields(prepStmt);
				dish.fillPreparedStatementWithKeys(prepStmt, DishTable.NUM_COLUMNS);
				logger.fine(prepStmt.toString());
				prepStmt.executeUpdate();
			}
		}
	}

	// @formatter:off
//	public void delete(Connection connection) throws SQLException {
//		logger.finest("Entered");
//		try (PreparedStatement prepStmt = connection.prepareStatement("DELETE FROM " + DishTable.TABLENAME_ANDING_ALL_KEYS)) {
//			fillPreparedStatementWithKeys(prepStmt, 0);
//			logger.finer(prepStmt.toString());
//			prepStmt.executeUpdate();
//		}
//	}
	// @formatter:off

	public boolean isSmallSizeValid() {
		return (smallPrice > 0);
	}

	public String getEnglishAndChineseNames() {
		if (chineseName == null) {
			return shortName;
		} else {
			return shortName + " " + chineseName;
		}
	}

	public String getQuotedShortName() {
		return "'" + shortName + "'";
	}

	public int getDishId() {
		return dishId;
	}

	public void setDishId(int dishId) {
		this.dishId = dishId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getChineseName() {
		return chineseName;
	}

	public void setChineseName(String chineseName) {
		this.chineseName = chineseName;
	}

	public int getLargePrice() {
		return largePrice;
	}

	public void setLargePrice(int largePrice) {
		this.largePrice = largePrice;
	}

	public int getSmallPrice() {
		return smallPrice;
	}

	public void setSmallPrice(int smallPrice) {
		this.smallPrice = smallPrice;
	}

	public long getToGoStationBits() {
		return toGoStationBits;
	}

	public void setToGoStationBits(long toGoStationBits) {
		this.toGoStationBits = toGoStationBits;
	}

	public long getDineInStationBits() {
		return dineInStationBits;
	}

	public void setDineInStationBits(long dineInStationBits) {
		this.dineInStationBits = dineInStationBits;
	}

	public boolean isAvailableOnline() {
		return availableOnline;
	}

	public void setAvailableOnline(boolean availableOnline) {
		this.availableOnline = availableOnline;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getToGoStationHasWork(int stationId) {
		return (toGoStationBits & STATION_MASK.get(stationId)) > 0;
	}

	public boolean getDineInStationHasWork(int stationId) {
		return (dineInStationBits & STATION_MASK.get(stationId)) > 0;
	}

	public Map<Integer, String> getIdToAbbreviation() {
		return idToAbbreviation;
	}

	public void setIdToAbbreviation(Map<Integer, String> idToAbbreviation) {
		this.idToAbbreviation = idToAbbreviation;
	}

	public Map<String, List<Float>> getCondimentNameToQuantities() {
		return condimentNameToQuantities;
	}

	public void setCondimentNameToQuantities(Map<String, List<Float>> condimentNameToQuantities) {
		this.condimentNameToQuantities = condimentNameToQuantities;
	}

	public CouponDish getCouponDish() {
		return couponDish;
	}

	public void setCouponDish(CouponDish couponDish) {
		this.couponDish = couponDish;
	}

	public List<Subdish> getSubdishes() {
		return subdishes;
	}

	public void setSubdishes(List<Subdish> subdishes) {
		this.subdishes = subdishes;
	}

	@Override
	public int compareTo(Dish thatDish) {
		return Comparator.comparing(Dish::getCategory)
				.thenComparing(Dish::getCode)
				.thenComparing(Dish::getDishId)
				//.thenComparing(Dish::getShortName) //no need to compare by short name where the dishId is numerically unique
				.compare(this, thatDish);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + category;
		result = prime * result + ((chineseName == null) ? 0 : chineseName.hashCode());
		result = prime * result + code;
		result = prime * result + ((condimentNameToQuantities == null) ? 0 : condimentNameToQuantities.hashCode());
		result = prime * result + ((couponDish == null) ? 0 : couponDish.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (int) (dineInStationBits ^ (dineInStationBits >>> 32));
		result = prime * result + dishId;
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + ((idToAbbreviation == null) ? 0 : idToAbbreviation.hashCode());
		result = prime * result + largePrice;
		result = prime * result + (availableOnline ? 1231 : 1237);
		result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result + smallPrice;
		result = prime * result + ((subdishes == null) ? 0 : subdishes.hashCode());
		result = prime * result + (int) (toGoStationBits ^ (toGoStationBits >>> 32));
		return result;
	}

	public boolean equalsIgnoreKeysIgnoreActive(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dish other = (Dish) obj;
		if (category != other.category)
			return false;
		if (chineseName == null) {
			if (other.chineseName != null)
				return false;
		} else if (!chineseName.equals(other.chineseName))
			return false;
		if (code != other.code)
			return false;
		if (condimentNameToQuantities == null) {
			if (other.condimentNameToQuantities != null)
				return false;
		} else if (!condimentNameToQuantities.equals(other.condimentNameToQuantities))
			return false;
//		if (couponDish == null) {
//			if (other.couponDish != null)
//				return false;
//		} else if (!couponDish.equals(other.couponDish))
//			return false;
//		if (description == null) {
//			if (other.description != null)
//				return false;
//		} else if (!description.equals(other.description))
//			return false;
		if (dineInStationBits != other.dineInStationBits)
			return false;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		if (idToAbbreviation == null) {
			if (other.idToAbbreviation != null)
				return false;
		} else if (!idToAbbreviation.equals(other.idToAbbreviation))
			return false;
		if (largePrice != other.largePrice)
			return false;
		if (availableOnline != other.availableOnline)
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (smallPrice != other.smallPrice)
			return false;
		if (subdishes == null) {
			if (other.subdishes != null)
				return false;
		} else if (!subdishes.equals(other.subdishes))
			return false;
		if (toGoStationBits != other.toGoStationBits)
			return false;
		return true;
	}

	public boolean equalsIgnoreKeys(Object obj) {
		Dish other = (Dish) obj;
		if (active != other.active) {
			return false;
		}

		return equalsIgnoreKeysIgnoreActive(obj);
	}

	@Override
	public boolean equals(Object obj) {
		Dish other = (Dish) obj;
		if (dishId != other.dishId) {
			return false;
		}

		return equalsIgnoreKeys(obj);
	}

	@Override
	public String toString() {
		// @formatter:off
		return Dish.class.getSimpleName() +
				"[" +
				"dishId=" + dishId +
				", active=" + active + 
				", category=" + category +
				", code=" + code +
				", shortName=" + shortName +
				", chineseName=" + chineseName +
				", largePrice=" + largePrice +
				", smallPrice=" + smallPrice +
				", toGoStationBits=" + toGoStationBits +		//for hex: String.format("0x%08x", intObject);
				", dineInStationBits=" + dineInStationBits +
				", availableOnline=" + availableOnline +
				", fullName=" + fullName +
				", description=" + description +
				", idToAbbreviation=" + (idToAbbreviation == null ? "" : idToAbbreviation.toString()) +
				", condimentNameToQuantities=" + (condimentNameToQuantities == null ? "" : condimentNameToQuantities.toString()) +
				", couponDish=" + (couponDish == null ? "" : couponDish.toString()) +
				", subdishes=" + (subdishes == null ? "" : subdishes.toString()) +
				"]";
		// @formatter:on
	}
}
