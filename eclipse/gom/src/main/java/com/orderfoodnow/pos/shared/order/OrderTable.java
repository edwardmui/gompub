package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

public class OrderTable {

	public static final String TABLE_NAME = "ord"; // 'ord' is used instead of 'order' because it's a SQL keyword

	// @formatter:off
	// column names
	public static final String ORDER_ID				= "order_id";			// key to the table, an AUTO_INCREMENT column
	public static final String ORDER_NUMBER			= "order_number";		// order number starting at 1 for a settledTime period
	public static final String TYPE					= "type";				// orderType from the OrderType enum
	public static final String ORDERED_TIME			= "ordered_time";		// time the order was placed
	public static final String TAKEN_BY_ID			= "taken_by_id";		// employee ID who took/entered the order into the computer
	public static final String PLACEMENT_TYPE		= "placement_type";		// the mean the order was placed by the customer
	public static final String CASH_PAID_AMOUNT		= "cash_paid_amount";	// the amount of cash this order received as payment
	public static final String PAID_TIME			= "paid_time";			// time stamp when order was paid
	public static final String CASHIER_ID			= "cashier_id";			// employee ID who received the payment. Foreign key EmployeeTable.EMPLOYEE_ID
	public static final String STATUS				= "status";				// order status taking on the value of OrderStatus enum value
	public static final String COMMITTED_TIME		= "committed_time";		// time stamp of the last order committed
	public static final String SETTLE_ID			= "settle_id";			// foreign key to SettleTable.SETTLE_ID. It has settle time and settle by id.
	public static final String VOIDED_TIME			= "voided_time";		// time stamp of the order when it was voided
	public static final String VOIDED_BY_ID			= "voided_by_id";		// employee ID who voided the order. Foreign key EmployeeTable.ID
	public static final String DISCOUNT_PERCENT		= "discount_percent";	// user input percent of discount taken off from the order
	public static final String ADDITIONAL_PERCENT	= "additional_percent";	// user input percent of addition to the order. e.g. percent of tip
	public static final String DISCOUNT_AMOUNT		= "discount_amount";	// user input amount in cents to take off
	public static final String ADDITIONAL_AMOUNT	= "additional_amount";	// miscellaneous charges, e.g. extra delivery charge
	public static final String TAX_RATE				= "tax_rate";			// state sales tax rate when the order was taken
	public static final String NOTE					= "note";				// note for this order. (not for future order), usage example: order to be picked up by whom
	public static final String PRINT_VERSION		= "print_version";		// number of print this order has gone thru printOrder
	public static final String ORDERED_TIMESTAMP	= "ordered_timestamp";	// time stamp of the order was placed using TIMESTAMP data type.

	// CAUSTION: TIMESTAMP data type is a 32-bit int which has a Y2038 problem.
	// The ordered_timestamp column is added to aid user to look for an order
	// directly in database with easier readable timestamp.
	// DO NOT use ordered_timestamp for any logically calculation to avoid Y2038
	// problem.
	// Use can use sql function FROM_UNIXTIME to convert the time fields for easier
	// reading. Below is an example
	// SELECT from_unixtime(floor(ordered_time/1000)) FROM ord;

	// column VARCHAR size
	public static final int NOTE_SIZE = 255;

	private static final String KEY = "key";
	private static final String AUTO_INC = "AUTO_INCREMENT";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ ORDER_ID,				"INTEGER not null " + AUTO_INC, KEY },
			{ ORDER_NUMBER,			"INTEGER" }, { TYPE, "TINYINT" },
			{ ORDERED_TIME,			"BIGINT" },
			{ TAKEN_BY_ID,			"INTEGER" },
			{ PLACEMENT_TYPE,		"TINYINT" },
			{ CASH_PAID_AMOUNT,		"INTEGER" },
			{ PAID_TIME,			"BIGINT" },
			{ CASHIER_ID,			"INTEGER" },
			{ STATUS,				"TINYINT" },
			{ COMMITTED_TIME,		"BIGINT" },
			{ SETTLE_ID,			"INTEGER" },
			{ VOIDED_TIME,			"BIGINT" },
			{ VOIDED_BY_ID,			"INTEGER" },
			{ DISCOUNT_PERCENT,		"TINYINT" },
			{ ADDITIONAL_PERCENT,	"TINYINT" },
			{ DISCOUNT_AMOUNT,		"INTEGER" },
			{ ADDITIONAL_AMOUNT,	"INTEGER" },
			{ TAX_RATE,				"REAL not null" },
			{ NOTE,					"VARCHAR(" + NOTE_SIZE + ")" },
			{ PRINT_VERSION,		"TINYINT" },
			{ ORDERED_TIMESTAMP,	"TIMESTAMP" },
			};
	// @formatter:off

	public static final int NUM_COLUMNS = COLUMN_DEFINITIONS.length;
	public static int NUM_KEY_COLUMNS = 0;
	public static int NUM_AUTOINC_COLUMNS = 0;
	public static String CREATE_TABLE_DEFINITION = "";
	static {
		String columNameAndDataType = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			columNameAndDataType = columNameAndDataType + columnDefinition[0] + " " + columnDefinition[1] + ",";
		}

		String primaryKeys = "";
		for (String[] columnDefinition : COLUMN_DEFINITIONS) {
			if (columnDefinition.length >= 3 && columnDefinition[1].contains(AUTO_INC)) {
				NUM_AUTOINC_COLUMNS++;
			}
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

	public static List<Order> readAllUnsettled(Connection connection) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + SETTLE_ID + "=0" + " ORDER BY " + ORDER_ID;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<Order> orders = new ArrayList<>();

		if (resultSet != null && resultSet.next()) {
			do {
				try {
					orders.add(new Order(resultSet));
				} catch (SQLException e) {
					throw e;
				}
			} while (resultSet.next());
		}

		return orders;
	}

	public static void settleAllUnsettled(Connection connection, int settleId) throws SQLException {

		String sql = "UPDATE " + TABLE_NAME + " SET " + SETTLE_ID + "=" + settleId + " WHERE " + SETTLE_ID + "=0";

		DataSource.executeQuery(connection, sql);
	}

	public static int readNextOrderNumber(Connection connection) throws SQLException {
		String sql = "SELECT MAX(" + ORDER_NUMBER + ") as maxOrderNumber FROM " + TABLE_NAME + " WHERE " + SETTLE_ID
				+ "=0";

		ResultSet resultSet = DataSource.executeQuery(connection, sql);

		if (resultSet != null && resultSet.next()) {
			int maxOrderNumber = resultSet.getInt("maxOrderNumber");
			return (maxOrderNumber);
		}

		return 1;
	}
}
