package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

//condiment exchanger exchanges one condiment for another condiment or upgrade to a dish
public class ExchangerTable {

	public static final String TABLE_NAME = "exchanger";

	// @formatter:off
	// column names
	public static final String ORDER_ID				= "order_id";			// key 1 to this table, foreign key to OrderTable.ORDER_ID
	public static final String ORDER_ITEM_ID		= "order_item_id";		// key 2 to this table. foreign key to OrderItemTable.ORDER_ITEM_ID
	public static final String EXCHANGER_ID			= "exchanger_id";		// key 3 to this table.
	public static final String EXCHANGER_DISH_ID	= "exchanger_dish_id";	// key 4 to this table. foreign to DishTable.DISH_ID, used as array index into the Dish[].
	public static final String NAME					= "name";				// Exchanger name. Saved for display when order is settled and dishId might not be valid anymore.
	public static final String ORDERED_TIME			= "ordered_time";		// time the exchanger was entered
	public static final String STATUS				= "status";				// status of this exchanger take on the value of SubOrderItemStatus
	public static final String VOIDED_TIME			= "voided_time";		// time stamp when the exchanger of price > 0 was voided
	public static final String VOIDED_BY_ID			= "voided_by_id";		// employee ID who voided this exchanger with price > 0. foreign to Employee.EMPLOYEE_ID

	// column VARCHAR size
	public static final int NAME_SIZE = 50;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ ORDER_ID,				"INTEGER not null", KEY },
			{ ORDER_ITEM_ID,		"INTEGER not null", KEY },
			{ EXCHANGER_ID,			"INTEGER not null", KEY },
			{ EXCHANGER_DISH_ID,	"INTEGER not null", KEY },
			{ NAME,					"VARCHAR(" + NAME_SIZE + ")" },
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

	public static List<Exchanger> readAllOf(Connection connection, int orderId, int orderItemId) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId + " AND " + ORDER_ITEM_ID
				+ "=" + orderItemId;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<Exchanger> exchangers = null;
		if (resultSet != null && resultSet.next()) {
			exchangers = new ArrayList<Exchanger>();
			do {
				Exchanger exchanger = null;
				try {
					exchanger = new Exchanger(resultSet);
				} catch (SQLException e) {
					throw e;
				}
				exchangers.add(exchanger);
			} while (resultSet.next());
		}
		return exchangers;
	}

	public static int deleteAllOf(Connection connection, int orderId) throws SQLException {
		String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId;

		return DataSource.executeUpdate(connection, sql);
	}
}
