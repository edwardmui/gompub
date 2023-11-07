package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

public class ToGoInfoTable {

	public static final String TABLE_NAME = "to_go_info";

	// @formatter:off
	// column names
	public static final String ORDER_ID					= "order_id";			// Key to this table and foreign key to OrderTable.ID
	public static final String CUSTOMER_ID				= "customer_id";		// customer id, foreign key to CustomerProfile.ID
	public static final String PHONE_NUMBER				= "phone_number";		// customer phone number, populate to/from CustomerProfileTable.PHONE_NUMBER. Stored on each to-go order. It is duplicate unmodifiable stored data. Storing it with the order for easier access to display and print.
	public static final String CUSTOMER_NAME			= "customer_name";		// customer name that's stored as part of the order where the name in CustomerProfile can change based on the latest order from the referenced phone number.
	public static final String CUSTOMER_REQUESTED_TIME	= "requested_time";		// the time the customer requested the order to be picked up
	public static final String QUEUE_TIME_MINUTES		= "queue_time_minute";	// the kitchen queue time in minutes. It's the number of minutes from now till the order is ready to be picked up. It's also based on the order status. e.g. Making vs Holding.
	public static final String CREDIT_CARD_NUMBER		= "credit_card_number";	// customer credit card number used for this order if needs it to be stored. Not clear on all the use cases.
	public static final String CUSTOMER_STATUS			= "customer_status";	// customer status, i.e. is the customer waiting on premise
	public static final String CUSTOMER_ARRIVAL_TIME	= "arrival_time";		// the time the customer arrived on premise to pickup the order
	public static final String NOTE						= "note";				// customer note for to go orders that updates customer profile and used on each order for this customer.

	// column VARCHAR size
	public static final int PHONE_NUMBER_SIZE = CustomerProfileTable.PHONE_NUMBER_SIZE;
	public static final int CUSTOMER_NAME_SIZE = CustomerProfileTable.NAME_SIZE;
	public static final int NOTE_SIZE = CustomerProfileTable.NOTE_SIZE;
	public static final int CREDIT_CARD_NUMBER_SIZE = 30;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ ORDER_ID,					"INTEGER not null", KEY },
			{ CUSTOMER_ID,				"INTEGER" },
			{ PHONE_NUMBER,				"CHAR(" + PHONE_NUMBER_SIZE + ")" },
			{ CUSTOMER_NAME,			"VARCHAR(" + CUSTOMER_NAME_SIZE + ")" },
			{ CUSTOMER_REQUESTED_TIME,	"BIGINT" },
			{ QUEUE_TIME_MINUTES,		"INTEGER" },
			{ CUSTOMER_STATUS,			"TINYINT" },
			{ CUSTOMER_ARRIVAL_TIME,	"BIGINT" },
			{ CREDIT_CARD_NUMBER,		"VARCHAR(" + CREDIT_CARD_NUMBER_SIZE + ")" },
			{ NOTE,						"VARCHAR(" + NOTE_SIZE + ")" },
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

	public static ToGoInfo read(Connection connection, int orderId) throws SQLException {
		String sql = "SELECT *  FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		if (resultSet != null && resultSet.first()) {
			return (new ToGoInfo(resultSet));
		}

		return new ToGoInfo();
	}

	public static List<Integer> readHistory(Connection connection, String phoneNumber, int orderCount) throws SQLException {
		String sql = "SELECT " + ORDER_ID + " FROM " + TABLE_NAME + " WHERE " + PHONE_NUMBER + "='" + phoneNumber
				+ "' ORDER BY " + ORDER_ID + " DESC LIMIT " + orderCount;
		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<Integer> orderIds = new ArrayList<>();

		if (resultSet != null && resultSet.next()) {
			do {
				try {
					int orderId = resultSet.getInt(ORDER_ID);
					orderIds.add(orderId);
				} catch (SQLException e) {
					throw e;
				}
			} while (resultSet.next());
		}

		return orderIds;
	}

	public static void eraseCreditCardNumber(Connection connection) throws SQLException {

		String sql = "UPDATE " + TABLE_NAME + " SET " + CREDIT_CARD_NUMBER + "='' WHERE " + CREDIT_CARD_NUMBER + "<>''";

		DataSource.executeQuery(connection, sql);
	}
}
