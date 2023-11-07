package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

public class CustomizerTable {

	public static final String TABLE_NAME = "customizer";

	// @formatter:off
	// column names
	public static final String ORDER_ID				= "order_id";			// key 1 to this table, foreign key to OrderTable.ORDER_ID
	public static final String ORDER_ITEM_ID		= "order_item_id";		// key 2 to this table. foreign key to OrderItemTable.ORDER_ITEM_ID
	public static final String CUSTOMIZER_ID		= "customizer_id";		// key 3 to this table.
	public static final String CUSTOMIZER_DISH_ID	= "customizer_dish_id";	// key 4 to this table. foreign to DishTable.DISH_ID, used as array index into the Dish[].  < 0 for non-menu customizers
	public static final String NAME					= "name";				// Customizer name, could be a non menu customizer which is created by the user taking the order.
	public static final String PRICE				= "price";				// price of the customizer
	public static final String ORDERED_TIME			= "ordered_time";		// time the customizer was entered
	public static final String STATUS				= "status";				// status of this customizer take on the value of SubOrderItemStatus
	public static final String VOIDED_TIME			= "voided_time";		// time stamp when the customizer of price > 0 was voided
	public static final String VOIDED_BY_ID			= "voided_by_id";		// employee ID who voided this customizer with price > 0. foreign to Employee.EMPLOYEE_ID

	// column VARCHAR size
	public static final int NAME_SIZE = 50;

	public static final int NON_MENU_CUSTOMIZER_ID = -1;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ ORDER_ID,				"INTEGER not null", KEY },
			{ ORDER_ITEM_ID,		"INTEGER not null", KEY },
			{ CUSTOMIZER_ID,		"INTEGER not null", KEY },
			{ CUSTOMIZER_DISH_ID,	"INTEGER not null", KEY },
			{ NAME,					"VARCHAR(" + NAME_SIZE + ")" },
			{ PRICE,				"INTEGER not null" },
			{ ORDERED_TIME,			"BIGINT" },
			{ STATUS,				"TINYINT" },
			{ VOIDED_TIME,			"BIGINT" },
			{ VOIDED_BY_ID,			"INTEGER" },
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

	public static List<Customizer> readAllOf(Connection connection, int orderId, int orderItemId) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId + " AND " + ORDER_ITEM_ID
				+ "=" + orderItemId;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<Customizer> customizers = null;
		if (resultSet != null && resultSet.next()) {
			customizers = new ArrayList<Customizer>();
			do {
				Customizer customizer = null;
				try {
					customizer = new Customizer(resultSet);
				} catch (SQLException e) {
					throw e;
				}
				customizers.add(customizer);
			} while (resultSet.next());
		}
		return customizers;
	}

	public static int deleteAllOf(Connection connection, int orderId) throws SQLException {
		String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId;

		return DataSource.executeUpdate(connection, sql);
	}
}
