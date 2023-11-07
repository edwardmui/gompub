package com.orderfoodnow.pos.shared.order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.orderfoodnow.pos.shared.DataSource;

/**
 * The word Profile is appended to Customer is to make the distinction that the
 * attributes are collected and updated as a by product information collected
 * during order taking.
 */
public class CustomerProfileTable {

	public static final String TABLE_NAME = "customer_profile";

	// @formatter:off
	// column names
	public static final String CUSTOMER_ID				= "customer_id";				// customer ID, key to the table, assigned by DB auto increment during insert
	public static final String PHONE_NUMBER				= "phone_number";				// CustomerProfileTable.TABLE_NAME is indexed by phoneNumber to help search
	public static final String NAME						= "name";						// name provided by the customer, could be first name, last name, combination, etc.
	public static final String STREET					= "street";						// postal street
	public static final String CITY						= "city";						// postal city
	public static final String STATE					= "state";						// postal state
	public static final String ZIP						= "zip";						// postal zip code
	public static final String LATITUDE					= "latitude";					// GPS coordinate latitude in decimal notation. e.g. restaurantLatitude: 42.175146
	public static final String LONGITUDE				= "longitude";					// GPS coordinate longitude in decimal notation e.g. restaurantLongitude: -88.368105
	public static final String EMAIL					= "email";						// email address
	public static final String DELIVERY_ZONE			= "delivery_zone";				// delivery zone, use to index to the delivery cost array defined in conf.yaml
	public static final String DRIVING_DIRECTION		= "driving_direction";			// driving direction from the restaurant
	public static final String DRIVING_DURATION_MINUTES	= "driving_duration_minutes";	// driving time in minutes one way. Would be nice if there's on line API to get this.
	public static final String DRIVING_DISTANCE			= "driving_distance";			// driving distance in miles.
	public static final String NOTE						= "note";						// note related to this customer
	public static final String FIRST_ORDER_DATE			= "first_order_date";			// date of the first order when it was placed
	public static final String LATEST_ORDER_DATE		= "latest_order_date";			// date of the most recent order
	// future public static final String ROYALTY_POINT	= "royalty_point";				// royalty
	// point for customer to redeem free items. Implement in a separate table index
	// by customer_id and manage it.

	// Create a separate customer-ordering table key by the above customer_id to
	// handle order via mobile app or web page that are to be developed when retired
	// from full time job :)
	// future public static final String CREDIT = "credit" // credit owe to
	// customer. combine with gift certificate? Or have user to find the order,
	// select the missing dish(es) and quantity and store the orderId and
	// orderItemId?
	// future public static final String USER_ID = "user_id";
	// future public static final String PASSWORD = "password";
	// future public static final String FIRST_NAME = "first_name";
	// future public static final String LAST_NAME = "last_name";

	// column VARCHAR size
	public static final int PHONE_NUMBER_SIZE = 10;
	public static final int NAME_SIZE = 20;
	public static final int STREET_SIZE = 50;
	public static final int CITY_SIZE = 30;
	public static final int STATE_SIZE = 20;
	public static final int ZIP_SIZE = 10;
	public static final int EMAIL_SIZE = 50;
	public static final int DRIVING_DIRECTION_SIZE = 255;
	public static final int NOTE_SIZE = 255;

	// indexed non key column
	public static final String INDEX_NAME = "index_by_phone";

	private static final String KEY = "key";
	private static final String AUTO_INC = "AUTO_INCREMENT";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ CUSTOMER_ID,				"INTEGER not null " + AUTO_INC, KEY },
			{ PHONE_NUMBER,				"VARCHAR(" + PHONE_NUMBER_SIZE + ")" },
			{ NAME,						"VARCHAR(" + NAME_SIZE + ")" },
			{ STREET,					"VARCHAR(" + STREET_SIZE + ")" },
			{ CITY,						"VARCHAR(" + CITY_SIZE + ")" },
			{ STATE,					"VARCHAR(" + STATE_SIZE + ")" },
			{ ZIP,						"VARCHAR(" + ZIP_SIZE + ")" },
			{ LATITUDE,					"DOUBLE" },
			{ LONGITUDE,				"DOUBLE" },
			{ EMAIL,					"VARCHAR(" + EMAIL_SIZE + ")" },
			{ DELIVERY_ZONE,			"INTEGER" },
			{ DRIVING_DIRECTION,		"VARCHAR(" + DRIVING_DIRECTION_SIZE + ")" },
			{ DRIVING_DURATION_MINUTES,	"INTEGER" },
			{ DRIVING_DISTANCE,			"FLOAT" },
			{ NOTE,						"VARCHAR(" + NOTE_SIZE + ")" },
			{ FIRST_ORDER_DATE,			"DATE" },
			{ LATEST_ORDER_DATE,		"DATE" },
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

		CREATE_TABLE_DEFINITION = TABLE_NAME + "(" + columNameAndDataType + " INDEX " + INDEX_NAME + " (" + PHONE_NUMBER
				+ ")," + " primary key (" + primaryKeys + "))";
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

	// SET column1=?, column2=?
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

	public static CustomerProfile read(Connection connection, int customerId) throws SQLException {
		String sql = "SELECT *  FROM " + TABLE_NAME + " WHERE " + CUSTOMER_ID + "=" + customerId;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		if (resultSet != null && resultSet.first()) {
			return (new CustomerProfile(resultSet));
		}

		return new CustomerProfile();
	}
}
