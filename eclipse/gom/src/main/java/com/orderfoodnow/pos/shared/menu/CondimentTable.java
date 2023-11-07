package com.orderfoodnow.pos.shared.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orderfoodnow.pos.shared.DataSource;

public class CondimentTable {

	public static final String TABLE_NAME = "condiment";

	// @formatter:off
	// column names
	public static final String DISH_ID					= "dish_id";			// key 1 to this table
	public static final String CONDIMENT_ID				= "condiment_id";		// key 2 to this table
	public static final String NAME						= "name";				// name of the condiment
	public static final String LARGE_SIZE_QUANTITY		= "large_size_quantity";// quantity of condiment for large size dish
	public static final String SMALL_SIZE_QUANTITY		= "small_size_quantity";// quantity of condiment for small size dish

	// column VARCHAR size
	public static final int NAME_SIZE = 50;

	public static final int ID_START_VALUE = 0;

	private static final String KEY = "key";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ DISH_ID,				"INTEGER not null", KEY },
			{ CONDIMENT_ID,			"INTEGER not null", KEY },
			{ NAME,					"VARCHAR(" + NAME_SIZE + ") not null" },
			{ LARGE_SIZE_QUANTITY,	"REAL not null" },
			{ SMALL_SIZE_QUANTITY,	"REAL not null" },
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

	private static Map<Integer, Map<String, List<Float>>> dishIdToCondimentQuantities = null;

	public static Map<String, List<Float>> readAllOf(Connection connection, int dishId) throws SQLException {
		if (dishIdToCondimentQuantities == null) {
			dishIdToCondimentQuantities = new HashMap<Integer, Map<String, List<Float>>>();
			String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DISH_ID;
			try (ResultSet resultSet = DataSource.executeQuery(connection, sql)) {
				if (resultSet != null) {
					Map<String, List<Float>> condimentNameToQuantities = null;
					Integer previousDishId = null;
					while (resultSet.next()) {
						int currentDishId = resultSet.getInt(DISH_ID);
						if (previousDishId == null) { // handle the first one
							previousDishId = currentDishId;
							condimentNameToQuantities = new HashMap<String, List<Float>>();
						}
						if (previousDishId != currentDishId) {
							dishIdToCondimentQuantities.put(previousDishId, condimentNameToQuantities);
							condimentNameToQuantities = new HashMap<String, List<Float>>();
							previousDishId = currentDishId;
						}
						String name = resultSet.getString(NAME);
						Float largeSizeQuantity = Float.valueOf(resultSet.getFloat(LARGE_SIZE_QUANTITY));
						Float smallSizeQuantity = Float.valueOf(resultSet.getFloat(SMALL_SIZE_QUANTITY));
						List<Float> quantities = new ArrayList<>();
						quantities.add(0, largeSizeQuantity);
						quantities.add(1, smallSizeQuantity);
						condimentNameToQuantities.put(name, quantities);
					}
					dishIdToCondimentQuantities.put(previousDishId, condimentNameToQuantities);// include the last one
				}
			}
		}
		return dishIdToCondimentQuantities.get(dishId);
	}
}
