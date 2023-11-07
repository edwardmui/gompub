package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.orderfoodnow.pos.shared.DataSource;

public class DeliveryInfoTable {

	public static final String TABLE_NAME = "delivery_info";

	// @formatter:off
	// column names
	public static final String ORDER_ID					= "order_id";									// Key to this table and foreign key to OrderTable.ID
	public static final String DELIVERY_CHARGE			= "delivery_charge";							// delivery charge for this order delivery. From CustomerProfile delivery zone index to config.yaml file.
	public static final String DRIVER_ID				= "driver_id";									// employee ID who delivers the order. Foreign key EmployeeTable.ID
	public static final String STREET					= CustomerProfileTable.STREET;					// postal street, populate from CustomerProfileTable or at order time
	public static final String CITY						= CustomerProfileTable.CITY;					// postal city, populate from CustomerProfileTable or at order time
	public static final String STATE					= CustomerProfileTable.STATE;					// postal state, populate from CustomerProfileTable or at order time
	public static final String ZIP						= CustomerProfileTable.ZIP;						// postal zip code, populate from CustomerProfileTable or at order time
	public static final String LATITUDE					= CustomerProfileTable.LATITUDE;				// GPS coordinate latitude, populate from CustomerProfileTable or at order time
	public static final String LONGITUDE				= CustomerProfileTable.LONGITUDE;				// GPS longitude latitude, populate from CustomerProfileTable or at order time
	public static final String DRIVING_DIRECTION		= CustomerProfileTable.DRIVING_DIRECTION;		// manually entered driving direction
	public static final String DRIVING_DURATION_MINUTES	= CustomerProfileTable.DRIVING_DURATION_MINUTES;// driving duration in minutes
	public static final String DRIVING_DISTANCE			= CustomerProfileTable.DRIVING_DISTANCE;		// driving distances in decimal miles
	public static final String ESTIMATED_TIME_ARRIVAL	= "estimated_time_arrival";						// Estimated Time of Arrival for the delivery order to arrive at the customer's door.

	// column VARCHAR size
	public static final int STREET_SIZE = CustomerProfileTable.STREET_SIZE;
	public static final int CITY_SIZE = CustomerProfileTable.CITY_SIZE;
	public static final int STATE_SIZE = CustomerProfileTable.STATE_SIZE;
	public static final int ZIP_SIZE = CustomerProfileTable.ZIP_SIZE;
	public static final int DRIVING_DIRECTION_SIZE = CustomerProfileTable.DRIVING_DIRECTION_SIZE;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ ORDER_ID,					"INTEGER not null", KEY },
			{ DELIVERY_CHARGE,			"INTEGER" },
			{ DRIVER_ID,				"INTEGER" },
			{ STREET,					"VARCHAR(" + STREET_SIZE + ")" },
			{ CITY,						"VARCHAR(" + CITY_SIZE + ")" },
			{ STATE,					"VARCHAR(" + STATE_SIZE + ")" },
			{ ZIP,						"VARCHAR(" + ZIP_SIZE + ")" },
			{ LATITUDE,					"FLOAT" },
			{ LONGITUDE,				"FLOAT" },
			{ DRIVING_DIRECTION,		"VARCHAR(" + DRIVING_DIRECTION_SIZE + ")" },
			{ DRIVING_DURATION_MINUTES,	"INTEGER" },
			{ DRIVING_DISTANCE,			"FLOAT" },
			{ ESTIMATED_TIME_ARRIVAL,	"BIGINT" },
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

	public static DeliveryInfo read(Connection connection, int orderId) throws SQLException {
		String sql = "SELECT *  FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "=" + orderId;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		if (resultSet != null && resultSet.first()) {
			return (new DeliveryInfo(resultSet));
		}

		return new DeliveryInfo();
	}
}
