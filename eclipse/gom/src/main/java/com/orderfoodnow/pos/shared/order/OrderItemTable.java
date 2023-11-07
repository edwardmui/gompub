package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;
import com.orderfoodnow.pos.shared.menu.DishTable;

public class OrderItemTable {

	public static final String TABLE_NAME = "order_item";

	// @formatter:off
	// column names
	public static final String ORDER_ID			= "order_id";		// key 1 to this table. Foreign key to OrderTable.ID
	public static final String ORDER_ITEM_ID	= "order_item_id";	// key 2 to this table. Identifies this order item
	public static final String DISH_ID			= "dish_id";		// identifies the dish in the menu
	public static final String DISH_SHORT_NAME	= "dish_short_name";// dish short name from menu. Stored for history as dish_id can change with new menu.
	public static final String LARGE_QUANTITY	= "large_quantity";	// number of large size for this order item
	public static final String SMALL_QUANTITY	= "small_quantity";	// number of small size for this order item
	public static final String ORDERED_TIME		= "ordered_time";	// time when this order item was entered
	public static final String STATUS			= "status";			// status of this order item, a value from the OrderItemStatus enum
	public static final String VOIDED_TIME		= "voided_time";	// time stamp of order item when it was voided. 0 for non-voided order item
	public static final String VOIDED_BY_ID		= "voided_by_id";	// employeeId who voided this order item

	// column VARCHAR size
	public static final int DISH_NAME_SIZE = DishTable.NAME_SIZE;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ ORDER_ID,			"INTEGER not null", KEY },
			{ ORDER_ITEM_ID,	"INTEGER not null", KEY },
			{ DISH_ID,			"INTEGER not null" },
			{ DISH_SHORT_NAME,	"VARCHAR(" + DISH_NAME_SIZE + ") not null" },
			{ LARGE_QUANTITY,	"INTEGER not null" },
			{ SMALL_QUANTITY,	"INTEGER not null" },
			{ ORDERED_TIME,		"BIGINT" },
			{ STATUS,			"TINYINT" },
			{ VOIDED_TIME,		"BIGINT" },
			{ VOIDED_BY_ID,		"INTEGER" },
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
		TABLENAME_ANDING_ALL_KEYS = TABLE_NAME + " WHERE " + primaryKeys;
	}

	public static List<OrderItem> readAllOf(Connection connection, int orderId) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<OrderItem> orderItems = new ArrayList<>();
		;
		if (resultSet != null && resultSet.next()) {
			do {
				try {
					orderItems.add(new OrderItem(resultSet));
				} catch (SQLException e) {
					throw e;
				}
			} while (resultSet.next());
		}

		return orderItems;
	}

	public static int deleteAllOf(Connection connection, int orderId) throws SQLException {
		String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId;

		return DataSource.executeUpdate(connection, sql);
	}
}
