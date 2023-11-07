package com.orderfoodnow.pos.shared.staff;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orderfoodnow.pos.shared.DataSource;

public class EmployeeTable {

	public static final String TABLE_NAME = "employee";

	// @formatter:off
	// column names
	public static final String EMPLOYEE_ID		= "employee_id";		// key to this table. Auto increment
	public static final String INITIALS			= "initials";			// first name and last name initials
	public static final String NICKNAME			= "nickname";			// shorter name for display and printing for staff to know
	public static final String PHONE_NUMBER		= "phone_number";
	public static final String FIRST_NAME		= "first_name";
	public static final String LAST_NAME		= "last_name";
	public static final String STREET			= "street";
	public static final String CITY				= "city";
	public static final String STATE			= "state";
	public static final String ZIP				= "zip";
	public static final String HIRE_DATE		= "hire_date";
	public static final String ACTIVE			= "active";				// employee still on payroll
	public static final String USER_ID			= "user_id";
	public static final String PASSWORD			= "password";
	public static final String SALT				= "salt";
	public static final String EMAIL			= "email";
	public static final String NOTE				= "note";

	public static final int INITIALS_SIZE = 2; // must be two characters for input parsing purpose
	public static final int SECURITY_CODE_SIZE = 20;
	public static final int NICK_NAME_SIZE = 20;
	public static final int PHONE_NUMBER_SIZE = 10;
	public static final int FIRST_NAME_SIZE = 20;
	public static final int LAST_NAME_SIZE = 20;
	public static final int STREET_SIZE = 50;
	public static final int CITY_SIZE = 30;
	public static final int STATE_SIZE = 20;
	public static final int ZIP_SIZE = 10;
	public static final int USER_ID_SIZE = 20;
	public static final int PASSWORD_SIZE = 20;
	public static final int SALT_SIZE = 20;
	public static final int EMAIL_SIZE = 50;
	public static final int NOTE_SIZE = 255;

	private static final String KEY = "key";
	private static final String AUTO_INC = "AUTO_INCREMENT";
	private static final String[][] COLUMN_DEFINITIONS = new String[][]
			{
			{ EMPLOYEE_ID,	"INTEGER not null " + AUTO_INC, KEY },
			{ INITIALS,		"VARCHAR(" + INITIALS_SIZE + ")" },
			{ NICKNAME,		"VARCHAR(" + NICK_NAME_SIZE + ")" },
			{ PHONE_NUMBER,	"VARCHAR(" + PHONE_NUMBER_SIZE + ")" },
			{ FIRST_NAME,	"VARCHAR(" + FIRST_NAME_SIZE + ")" },
			{ LAST_NAME,	"VARCHAR(" + LAST_NAME_SIZE + ")" },
			{ STREET,		"VARCHAR(" + STREET_SIZE + ")" },
			{ CITY,			"VARCHAR(" + CITY_SIZE + ")" },
			{ STATE,		"VARCHAR(" + STATE_SIZE + ")" },
			{ ZIP,			"VARCHAR(" + ZIP_SIZE + ")" },
			{ HIRE_DATE,	"DATE not null" }, { ACTIVE, "TINYINT not null" },
			{ USER_ID,		"VARCHAR(" + USER_ID_SIZE + ")" },
			{ PASSWORD,		"VARCHAR(" + PASSWORD_SIZE + ")" },
			{ SALT,			"VARBINARY(" + SALT_SIZE + ")" },
			{ EMAIL,		"VARCHAR(" + EMAIL_SIZE + ")" },
			{ NOTE,			"VARCHAR(" + NOTE_SIZE + ")" },
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

	public static Employee read(Connection connection, int id) throws SQLException {
		String sql = "SELECT *  FROM " + TABLE_NAME + " WHERE " + EMPLOYEE_ID + "=" + id;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		if (resultSet != null && resultSet.first()) {
			return (new Employee(resultSet));
		}

		return new Employee();
	}

	public static List<Employee> readAllOf(Connection connection) throws SQLException {
		String sql = "SELECT *  FROM " + TABLE_NAME;

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<Employee> employees = new ArrayList<>();
		if (resultSet != null && resultSet.next()) {
			do {
				try {
					employees.add(new Employee(resultSet));
				} catch (SQLException e) {
					throw e;
				}
			} while (resultSet.next());
		}

		return employees;
	}

	public static List<Employee> readAllOfActive(Connection connection) throws SQLException {
		String sql = "SELECT *  FROM " + TABLE_NAME + " WHERE active=true";

		ResultSet resultSet = DataSource.executeQuery(connection, sql);
		List<Employee> employees = new ArrayList<>();
		if (resultSet != null && resultSet.next()) {
			do {
				try {
					Employee employee = new Employee(resultSet);
					List<Role> roles = RoleTable.readAllOf(connection, employee.getEmployeeId());
					employee.setRoles(roles);
					employees.add(employee);
				} catch (SQLException e) {
					throw e;
				}
			} while (resultSet.next());
		}

		return employees;
	}
}
