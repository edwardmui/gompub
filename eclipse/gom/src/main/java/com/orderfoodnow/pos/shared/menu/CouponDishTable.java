package com.orderfoodnow.pos.shared.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

public class CouponDishTable {

	public static final String TABLE_NAME = "coupon_dish";

	// @formatter:off
	// column names
	public static final String DISH_ID				= "dish_id";				// key to this table
	public static final String NAME					= "name";					// coupon dish name for customer facing
	public static final String DISH1_NAME			= "dish1_name";				// actual dish1 name for kitchen facing
	public static final String DISH1_LARGE_QUANTITY	= "dish1_large_quantity";	// dish1 large quantity of the actual dish to be made for the coupon
	public static final String DISH1_SMALL_QUANTITY	= "dish1_small_quantity";	// dish1 small quantity of the actual dish to be made for the coupon
	public static final String DISH2_NAME			= "dish2_name";				// actual dish1 name for kitchen facing
	public static final String DISH2_LARGE_QUANTITY	= "dish2_large_quantity";	// dish1 large quantity of the actual dish to be made for the coupon
	public static final String DISH2_SMALL_QUANTITY	= "dish2_small_quantity";	// dish1 small quantity of the actual dish to be made for the coupon
	public static final String MINIMUM_FOOD_TOTAL	= "minimum_food_total";		// minimum food total to allow this coupon

	// column VARCHAR size
	public static final int NAME_SIZE = 50;
	public static final int DISH1_NAME_SIZE = 50;
	public static final int DISH2_NAME_SIZE = 50;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ DISH_ID,				"INTEGER not null", KEY },
			{ NAME,					"VARCHAR(" + NAME_SIZE + ") not null" },
			{ DISH1_NAME,			"VARCHAR(" + DISH1_NAME_SIZE + ") not null" },
			{ DISH1_LARGE_QUANTITY,	"INTEGER not null" },
			{ DISH1_SMALL_QUANTITY,	"INTEGER not null" },
			{ DISH2_NAME,			"VARCHAR(" + DISH2_NAME_SIZE + ") not null" },
			{ DISH2_LARGE_QUANTITY,	"INTEGER not null" },
			{ DISH2_SMALL_QUANTITY,	"INTEGER not null" },
			{ MINIMUM_FOOD_TOTAL,	"INTEGER not null" },
			};
	// @formatter:off

	public static final int NUM_COLUMNS = COLUMN_DEFINITIONS.length;
	public static int NUM_KEY_COLUMNS = 0;
	public static String CREATE_TABLE_DEFINITION = "";
	static {
		String columNameAndDataType = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			columNameAndDataType = columNameAndDataType + columnDefinition[0] + " " + columnDefinition[1] + ",";
		}

		String primaryKeys = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			if (columnDefinition.length >= 3 && columnDefinition[2].equals(KEY)) {
				primaryKeys = primaryKeys + columnDefinition[0] + ",";
				NUM_KEY_COLUMNS++;
			}
		}
		if (primaryKeys.lastIndexOf(',') > 0) {
			primaryKeys = primaryKeys.substring(0, primaryKeys.length() - 1);
		}

		CREATE_TABLE_DEFINITION = TABLE_NAME + "(" + columNameAndDataType + " primary key (" + primaryKeys + "))";
	}

	public static String ALL_COLUMNS = "";
	static {
		String columNames = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			columNames = columNames + columnDefinition[0] + ",";
		}

		if (columNames.lastIndexOf(',') > 0) {
			columNames = columNames.substring(0, columNames.length() - 1);
		}

		String questionmarks = "";
		for (@SuppressWarnings("unused")
		String[] columnDefinition : COLUMN_DEFINITIONS) {
			questionmarks = questionmarks + "?" + ",";
		}

		if (questionmarks.lastIndexOf(',') > 0) {
			questionmarks = questionmarks.substring(0, questionmarks.length() - 1);
		}

		ALL_COLUMNS = TABLE_NAME + "(" + columNames + ") VALUES (" + questionmarks + ")";
	}

	public static String TABLENAME_ANDING_ALL_KEYS = "";
	public static String ANDING_ALL_KEYS = "";
	static {
		String primaryKeys = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			if (columnDefinition.length >= 3 && columnDefinition[2].equals(KEY)) {
				primaryKeys = primaryKeys + columnDefinition[0] + "=? AND ";
			}
		}
		if (primaryKeys.lastIndexOf('?') > 0) {
			primaryKeys = primaryKeys.substring(0, primaryKeys.length() - 5); // truncate the extra " AND " at the end
		}

		ANDING_ALL_KEYS = " WHERE " + primaryKeys;
		TABLENAME_ANDING_ALL_KEYS = TABLE_NAME + ANDING_ALL_KEYS;
	}

	private static List<CouponDish> coupondishes;

	private static List<CouponDish> readAllOf(Connection connection) throws SQLException {
		if (coupondishes == null) {
			coupondishes = new ArrayList<CouponDish>();
			String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DISH_ID;
			try (ResultSet resultSet = DataSource.executeQuery(connection, sql)) {
				if (resultSet != null) {
					while (resultSet.next()) {
						coupondishes.add(new CouponDish(resultSet));
					}
				}
			}
		}

		return coupondishes;
	}

	public static CouponDish read(Connection connection, int dishId) throws SQLException {
		List<CouponDish> couponDishes = readAllOf(connection);
		for (CouponDish couponDish : couponDishes) {
			if (couponDish.getDishId() == dishId) {
				return couponDish;
			}
		}

		return null;
	}
}
