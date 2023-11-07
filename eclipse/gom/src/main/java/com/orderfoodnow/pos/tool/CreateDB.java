package com.orderfoodnow.pos.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.DataSource;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.AbbreviationTable;
import com.orderfoodnow.pos.shared.menu.CondimentTable;
import com.orderfoodnow.pos.shared.menu.CouponDishTable;
import com.orderfoodnow.pos.shared.menu.DishTable;
import com.orderfoodnow.pos.shared.menu.SubdishTable;
import com.orderfoodnow.pos.shared.order.CustomerProfile;
import com.orderfoodnow.pos.shared.order.CustomerProfileTable;
import com.orderfoodnow.pos.shared.order.CustomizerTable;
import com.orderfoodnow.pos.shared.order.DeliveryInfoTable;
import com.orderfoodnow.pos.shared.order.DineInInfoTable;
import com.orderfoodnow.pos.shared.order.ExchangerTable;
import com.orderfoodnow.pos.shared.order.OrderItemTable;
import com.orderfoodnow.pos.shared.order.OrderTable;
import com.orderfoodnow.pos.shared.order.PaymentTable;
import com.orderfoodnow.pos.shared.order.SettleTable;
import com.orderfoodnow.pos.shared.order.ToGoInfoTable;
import com.orderfoodnow.pos.shared.staff.Employee;
import com.orderfoodnow.pos.shared.staff.EmployeeTable;
import com.orderfoodnow.pos.shared.staff.ProficiencyLevel;
import com.orderfoodnow.pos.shared.staff.Role;
import com.orderfoodnow.pos.shared.staff.RoleTable;
import com.orderfoodnow.pos.shared.staff.RoleType;

public class CreateDB {
	private static final Logger logger = Logger.getLogger(CreateDB.class.getName());

	public static void createDB(String databaseName) throws SQLException {
		logger.finest("Entered");
		String createDBSql = "CREATE DATABASE " + databaseName +
				" CHARACTER SET = 'utf8' " +
				" COLLATE = 'utf8_bin'";
		try (Connection connection = DataSource.getConnectionWithoutDb();
				ResultSet resultSet = DataSource.executeSqlWithoutDb(connection, createDBSql)) {
		}
	}

	public static void insertDefaults() throws Exception {
		logger.finest("Entered");
		Connection connection = DataSource.getConnection();
		connection.setAutoCommit(false);

		logger.fine("Creating tables.");
		createTables(connection);

		logger.fine("Inserting employees.");
		insertDefaultEmployees(connection);

		logger.fine("Inserting restaurant as a customer profile.");
		insertRestaurantAsACustomerProfile(connection);

		connection.commit();
		connection.setAutoCommit(true);
	}

	private static void createTables(Connection connection) throws SQLException {
		logger.finest("Entered");
		// @formatter:off
		String[] createTableSqlStmts =
			{
			AbbreviationTable.CREATE_TABLE_DEFINITION,
			CondimentTable.CREATE_TABLE_DEFINITION,
			DishTable.CREATE_TABLE_DEFINITION,
			SubdishTable.CREATE_TABLE_DEFINITION,
			CouponDishTable.CREATE_TABLE_DEFINITION,
			CustomerProfileTable.CREATE_TABLE_DEFINITION,
			OrderItemTable.CREATE_TABLE_DEFINITION,
			CustomizerTable.CREATE_TABLE_DEFINITION,
			ExchangerTable.CREATE_TABLE_DEFINITION,
			OrderTable.CREATE_TABLE_DEFINITION,
			ToGoInfoTable.CREATE_TABLE_DEFINITION,
			DineInInfoTable.CREATE_TABLE_DEFINITION,
			DeliveryInfoTable.CREATE_TABLE_DEFINITION,
			PaymentTable.CREATE_TABLE_DEFINITION,
			EmployeeTable.CREATE_TABLE_DEFINITION,
			RoleTable.CREATE_TABLE_DEFINITION,
			SettleTable.CREATE_TABLE_DEFINITION,
			};
		// @formatter:off

		for (String createTableSql : createTableSqlStmts) {
			try (PreparedStatement prepStmt = connection.prepareStatement("CREATE TABLE " + createTableSql)) {
				logger.info(prepStmt.toString());
				prepStmt.executeUpdate();
			}
		}
	}

	private static void insertDefaultEmployees(Connection connection) throws Exception {
		logger.finest("Entered");
		// Add employees for each role.
		// For now, manually edit the db to change one of the default to a real user
		for (RoleType roleType : RoleType.values()) {
			Employee employee = new Employee();
			String roleName = roleType.getConfigurationName();

			int defaultEmployeeRoleCount = 26; // limit to 26 to use the lower case alphabet (a-z)
			int defaultActiveEmployeeCount = 5;
			switch (roleType) {
			case OWNER:
				defaultEmployeeRoleCount = 5;
				defaultActiveEmployeeCount = 3;
				break;
			case MANAGER:
				defaultEmployeeRoleCount = 10;
				break;
			default:
				break;
			}

			char initialSecondLetterCounter = 'a';
			for (int i = 1, activeCounter = 0; i <= defaultEmployeeRoleCount; ++i, ++activeCounter, ++initialSecondLetterCounter) {
				employee.setFirstName(roleName + i);
				employee.setLastName(roleName + i);
				employee.setNickname(roleName + i);
				String firstLetter = String.valueOf(roleName.charAt(0));
				String initialsUserIdPassword = "password123";
				employee.setInitials(initialsUserIdPassword);
				employee.setUserId(initialsUserIdPassword);
				employee.setPassword(initialsUserIdPassword);

				employee.setActive(activeCounter < defaultActiveEmployeeCount);
				employee.insert(connection);

				int employeeId = employee.getEmployeeId();
				Role role;
				switch (roleType) {
				case OWNER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.OWNER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					// break; fall thru
				case MANAGER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.MANAGER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					// break; fall thru
				case CASHIER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.CASHIER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					// break; fall thru
				case SERVER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.SERVER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					// break; fall thru
				case DRIVER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.DRIVER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					// break; fall thru
				case PACKER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.PACKER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					// break; fall thru
				case BUSSER:
					role = new Role();
					role.setEmployeeId(employeeId);
					role.setRoleType(RoleType.BUSSER);
					role.setProficiencyLevel(ProficiencyLevel.ADVANCED);
					role.insert(connection);
					break; // lowest role, don't fall thru
				default:
					logger.severe("Unhandled roleType=" + roleType);
					throw new Exception("Unhandled roleType=" + roleType);
				}
			}
		}
	}

	private static void insertRestaurantAsACustomerProfile(Connection connection) throws Exception {
		logger.finest("Entered");
		CustomerProfile customerProfile = new CustomerProfile();
		customerProfile.setPhoneNumber(Util.parsePhoneNumber(Configuration.getRestaurantPhone()));
		customerProfile.setName(Configuration.getRestaurantName());
		customerProfile.setStreet(Configuration.getRestaurantAddress());
		customerProfile.setCity(Configuration.getRestaurantCity());
		customerProfile.setState(Configuration.getRestaurantState());
		customerProfile.setZip(Configuration.getRestaurantZip());
		customerProfile.setLatitude(Configuration.getRestaurantLatitude());
		customerProfile.setLongitude(Configuration.getRestaurantLongitude());

		// Makeup data for easier testing
		customerProfile.setDeliveryZone(3);
		customerProfile.setDrivingDirection("Driving direction. Can get them from map apps and can store here.");
		customerProfile.setDrivingDurationMinutes(20);
		customerProfile.setDrivingDistance(6.8f);
		customerProfile.setNote(
				"This customer profile is the resturant's information from the conf.yaml file. Created by default and can be use for easier testing");
		long currentTimeMills = System.currentTimeMillis();
		customerProfile.setFirstOrderTime(currentTimeMills - 60 * 24 * 60 * 60 * 1000L); // 60 days prior
		customerProfile.setLatestOrderTime(currentTimeMills);

		customerProfile.insert(connection);
	}

	public static void main(String[] argv) {
		logger.finest("Entered");

		String databaseName = Configuration.getDatasourceDatabase();
		logger.info("Starting CreateDB with name=" + databaseName);

		if (DataSource.isDatabaseExist(databaseName) == true) {
			logger.info("Database name '" + databaseName + "' already exists and expecting defaults are created. No more work to do. Exiting.");
			System.exit(0);
		}

		try {
			createDB(databaseName); // create database first. Cannot combine with atomic connection below
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("createDB: " + e.getMessage());
			System.exit(-10);
		}
		logger.info("Done creating database=" + Configuration.getDatasourceDatabase());

		try {
			insertDefaults();
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Exception: " + e.getMessage());
			System.exit(-20);
		}

		logger.info("All outstanding transctions are committed. Done Creating DB.");
	}
}
