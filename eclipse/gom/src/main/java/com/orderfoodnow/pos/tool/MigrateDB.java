package com.orderfoodnow.pos.tool;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.DataSource;
import com.orderfoodnow.pos.shared.order.CustomerProfile;

public class MigrateDB {
	private static final Logger logger = Logger.getLogger(MigrateDB.class.getName());

	// private static List<List<Dish>> menus = new ArrayList<>();

	private static int deliveryCostToZoneMap(int delcost) {
		logger.finest("Entered");
		if (delcost > 800)
			return 11;
		if (delcost > 750)
			return 10;
		if (delcost > 700)
			return 9;
		if (delcost > 650)
			return 8;
		if (delcost > 600)
			return 7;
		if (delcost > 550)
			return 6;
		if (delcost > 500)
			return 5;
		if (delcost > 450)
			return 4;
		if (delcost > 400)
			return 3;
		if (delcost > 350)
			return 2;
		if (delcost > 300)
			return 1;
		return 0;
	}

	public static void main(String[] args) throws Exception {
		logger.finest("Entered");
		logger.fine("Start migrating to new DB schema");

		// migrate sales.customer to sales2.customer_profile
		String sql = "SELECT *  FROM " + "sales.customer";
		try (Connection fromDBconnection = DataSource.getConnectionWithoutDb();
				Statement statement = fromDBconnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
				Connection toDBconnection = DataSource.getConnection()) {
			toDBconnection.setAutoCommit(false);
			ResultSet resultSet = statement.executeQuery(sql);
			int count = 0;
			if (resultSet != null && resultSet.next()) {
				do {
					CustomerProfile customerProfile = new CustomerProfile();
					customerProfile.setPhoneNumber(resultSet.getString("phone"));
					customerProfile.setName(resultSet.getString("name"));
					customerProfile.setStreet(resultSet.getString("street"));
					customerProfile.setCity(resultSet.getString("city"));
					customerProfile.setState(resultSet.getString("state"));
					customerProfile.setZip(resultSet.getString("zip"));
					customerProfile.setEmail(resultSet.getString("email"));
					customerProfile.setDeliveryZone(deliveryCostToZoneMap(resultSet.getInt("delcost")));
					customerProfile.setDrivingDirection(resultSet.getString("direction"));
					customerProfile.setNote(resultSet.getString("note"));
					Date first = resultSet.getDate("first");
					Date last = resultSet.getDate("last");
					if (first != null) {
						customerProfile.setFirstOrderTime(first.getTime());
					}
					if (last != null) {
						customerProfile.setLatestOrderTime(last.getTime());
					}
					customerProfile.insert(toDBconnection);
					System.out.println(++count + " " + customerProfile);
				} while (resultSet.next());
			}
			toDBconnection.commit();
			toDBconnection.setAutoCommit(true);
		} catch (Exception e) {
			throw e;
		}
	}
}
