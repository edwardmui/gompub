package com.orderfoodnow.pos.shared.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orderfoodnow.pos.shared.DataSource;

public class DishTable {

	public static final String TABLE_NAME = "dish";

	// @formatter:off
	// column names
	public static final String DISH_ID				= "dish_id";				// key to this table.
	public static final String ACTIVE				= "active";					// Should the dish be active for selection
	public static final String CATEGORY				= "category";				// dish category use on printing the order
	public static final String CODE					= "code";					// dish code on the printed menu that customers see
	public static final String SHORT_NAME			= "short_name";				// dish short name
	public static final String CHINESE_NAME			= "chinese_name";			// Chinese name of the dish for the kitchen
	public static final String LARGE_PRICE			= "large_price";			// price of a large size of the dish
	public static final String SMALL_PRICE			= "small_price";			// price of a small size of the dish, 0 means no small
	public static final String TO_GO_STATION_BITS	= "to_go_station_bits";		// bitmap of which toGo workstation has work, for printing/display
	public static final String DINE_IN_STATION_BITS	= "dine_in_station_bits";	// bitmap of which toGo workstation has work, for printing/display
	public static final String AVAILABLE_ONLINE		= "available_online";		// available for ordering online/web 
	public static final String FULL_NAME			= "full_name";				// dish name on the menu
	public static final String DESCRIPTION			= "description";			// dish description

	// column VARCHAR size
	public static final int NAME_SIZE = 40;
	public static final int CHINESE_NAME_SIZE = 60;
	public static final int FULL_NAME_SIZE = 100;
	public static final int DESCRIPTION_SIZE = 511;

	public static final int ID_START_VALUE = 0;
	public static final int INVALID_ID = ID_START_VALUE - 1;

	private static final String KEY = "key";
	private static final String AUTO_INC = "AUTO_INCREMENT";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ DISH_ID,				"INTEGER not null", KEY },
			{ ACTIVE,				"BOOL" },
			{ CATEGORY,				"INTEGER" },
			{ CODE,					"INTEGER" },
			{ SHORT_NAME,			"VARCHAR(" + NAME_SIZE + ") not null" },
			{ CHINESE_NAME,			"VARCHAR(" + CHINESE_NAME_SIZE + ")" },
			{ LARGE_PRICE,			"INTEGER" },
			{ SMALL_PRICE,			"INTEGER" },
			{ TO_GO_STATION_BITS,	"BIGINT" },
			{ DINE_IN_STATION_BITS,	"BIGINT" },
			{ AVAILABLE_ONLINE,		"BOOL" },
			{ FULL_NAME,			"VARCHAR(" + FULL_NAME_SIZE + ") not null" },
			{ DESCRIPTION,			"VARCHAR(" + DESCRIPTION_SIZE + ")" },
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

	public static String ALL_COLUMNS_WITH_SET = "";
	static {
		String columNames = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			columNames = columNames + columnDefinition[0] + "=?,";
		}

		if (columNames.lastIndexOf(',') > 0) {
			columNames = columNames.substring(0, columNames.length() - 1);
		}

		ALL_COLUMNS_WITH_SET = TABLE_NAME + " SET " + columNames;
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

	public static String ALL_COLUMNS_MINUS_AUTOINC = "";
	static {
		String columNames = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			if (columnDefinition[1].contains(AUTO_INC) == false) {
				columNames = columNames + columnDefinition[0] + ",";
			}
		}

		if (columNames.lastIndexOf(',') > 0) {
			columNames = columNames.substring(0, columNames.length() - 1);
		}

		String questionmarks = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			if (columnDefinition[1].contains(AUTO_INC) == false) {
				questionmarks = questionmarks + "?" + ",";
			}
		}

		if (questionmarks.lastIndexOf(',') > 0) {
			questionmarks = questionmarks.substring(0, questionmarks.length() - 1);
		}

		ALL_COLUMNS_MINUS_AUTOINC = TABLE_NAME + "(" + columNames + ") VALUES (" + questionmarks + ")";
	}

	public static Dish[] readAllOf(Connection connection) throws SQLException {
		List<Dish> dishes = readAllOfAsList(connection);
		int dishCount = dishes.size();
		return dishes.toArray(new Dish[dishCount]);
	}

	public static Map<String, Integer> readAllOfAsMap(Connection connection) throws SQLException {
		Map<String, Integer> dishNameToId = new HashMap<>();

		for (Dish dish : readAllOfAsList(connection)) {
			// for dish with same shortName, the higher/newer dishId will replaces the lower/older dishId 
			dishNameToId.put(dish.getShortName(), dish.getDishId());
		}

		return dishNameToId;
	}

	public static Map<String, Integer> readAllActiveOfAsMap(Connection connection) throws SQLException {
		Map<String, Integer> activeDishNameToId = new HashMap<>();

		for (Dish dish : readAllActiveOf(connection)) {
			activeDishNameToId.put(dish.getShortName(), dish.getDishId());
		}

		return activeDishNameToId;
	}

	public static List<Dish> readAllActiveOf(Connection connection) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ACTIVE + "=true ORDER BY " + DISH_ID;
		return readAllOf(connection, sql);
	}

	private static List<Dish> readAllOfAsList(Connection connection) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DISH_ID;
		return readAllOf(connection, sql);
	}

	private static List<Dish> readAllOf(Connection connection, String sql) throws SQLException {
		List<Dish> dishes = new ArrayList<>();
		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		if (resultSet != null && resultSet.next()) {
			do {
				try {
					Dish dish = new Dish(resultSet);
					int dishId = dish.getDishId();
					dish.setCondimentNameToQuantities(CondimentTable.readAllOf(connection, dishId));
					dish.setIdToAbbreviation(AbbreviationTable.readAllOf(connection, dishId));
					dish.setSubdishes(SubdishTable.readAllOf(connection, dishId));
					dish.setCouponDish(CouponDishTable.read(connection, dishId));
					dishes.add(dish);
				} catch (SQLException e) {
					throw e;
				}
			} while (resultSet.next());
		}

		return dishes;
	}

	public static Integer getNextDishId(Connection connection) throws SQLException {
		String sql = "SELECT MAX(" + DISH_ID + ") FROM " + TABLE_NAME;
		try (ResultSet resultSet = DataSource.executeQuery(connection, sql)) {
			resultSet.first();
			return resultSet.getInt(1) + 1;
		} catch (SQLException e) {
			throw e;
		}
	}
}
