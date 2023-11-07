package com.orderfoodnow.pos.shared.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

public class SubdishTable {

	public static final String TABLE_NAME = "subdish";

	// @formatter:off
	// column names
	public static final String DISH_ID			= "dish_id";		// key 1 to this table
	public static final String SUBDISH_ID		= "subdish_id";		// key 2 to this table
	public static final String NAME				= "name";			// name of the subdish
	public static final String QUANTITY			= "quantity";		// number of regular size for this subdish

	// column VARCHAR size
	public static final int NAME_SIZE = 50;

	public static final int ID_START_VALUE = 0;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ DISH_ID, "INTEGER not null", KEY },
			{ SUBDISH_ID, "INTEGER not null", KEY },
			{ NAME, "VARCHAR(" + NAME_SIZE + ") not null" },
			{ QUANTITY, "REAL not null" },
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

	// Better to cache all the subdishes (iso do db read per dishId) as there are
	// less than 10 of them.
	private static List<Subdish> subdishes;

	private static List<Subdish> readAllOf(Connection connection) throws SQLException {
		if (subdishes == null) {
			subdishes = new ArrayList<Subdish>();
			String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DISH_ID;
			try (ResultSet resultSet = DataSource.executeQuery(connection, sql)) {
				if (resultSet != null) {
					while (resultSet.next()) {
						subdishes.add(new Subdish(resultSet));
					}
				}
			}
			Collections.sort(subdishes);
		}

		return subdishes;
	}

	public static List<Subdish> readAllOf(Connection connection, int dishId) throws SQLException {
		List<Subdish> matchedSubdishes = new ArrayList<>();
		List<Subdish> dbSubdishes = readAllOf(connection);
		for (Subdish subdish : dbSubdishes) {
			if (subdish.getDishId() == dishId) {
				matchedSubdishes.add(subdish);
			}
		}

		// return null for dishes (99%) who don't have subdishes to save some memory.
		return matchedSubdishes.size() == 0 ? null : matchedSubdishes;
	}
}
