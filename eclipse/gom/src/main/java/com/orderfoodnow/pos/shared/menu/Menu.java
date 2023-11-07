package com.orderfoodnow.pos.shared.menu;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.DataSource;

//Because Static methods cannot be accessed via RMI. Menu methods access by client are defined as non static. 
public class Menu implements Serializable {
	private Dish[] dishes; // Array contains all the dishes in the menu
	private Map<String, Integer> dishNameToId;
	private Map<String, Integer> activeDishNameToId;

	// Number workstations that dish has configured in menu.csv.
	// It's a runtime derived value and not stored in DB.
	private int toGoStationCount; // for to go stations
	private int dineInStationCount; // for dine-in stations

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Menu.class.getName());

	public Menu(Connection connection) throws SQLException {
		logger.finest("Entered");
		dishes = DishTable.readAllOf(connection);
		dishNameToId = DishTable.readAllOfAsMap(connection);
		activeDishNameToId = DishTable.readAllActiveOfAsMap(connection);

		long maxToGoStationBits = 0;
		long maxDineInStationBits = 0;
		for (Dish dish : dishes) {
			if (dish.getToGoStationBits() > maxToGoStationBits) {
				maxToGoStationBits = dish.getToGoStationBits();
				maxDineInStationBits = dish.getDineInStationBits();
			}
		}

		toGoStationCount = Long.SIZE - Long.numberOfLeadingZeros(maxToGoStationBits);
		dineInStationCount = Long.SIZE - Long.numberOfLeadingZeros(maxDineInStationBits);
	}

	public Dish[] getDishes() {
		return dishes;
	}

	public Dish getDish(int dishId) {
		if (dishId < 0) {
			return null;
		}

		return (dishes[dishId]);
	}

	public Dish getDish(String name) {
		if (name == null) {
			return null;
		}

		Integer dishId = dishNameToId.get(name.trim());

		return (dishId == null) ? null : dishes[dishId];
	}

	public Dish getActiveDish(String name) {
		if (name == null) {
			return null;
		}

		Integer dishId = activeDishNameToId.get(name.trim());

		return (dishId == null) ? null : dishes[dishId];
	}

	public int getToGoStationCount() {
		return toGoStationCount;
	}

	public int getDineInStationCount() {
		return dineInStationCount;
	}

	@Override
	public String toString() {
		return Menu.class.getSimpleName() + "[" + "dishes=" + dishes + ", dishNameToId=" + dishNameToId + "]";
	}

	public static void main(String argv[]) {
		try (Connection connection = DataSource.getConnection()) {
			Menu menu = new Menu(connection);

			for (Dish dish : menu.getDishes()) {
				System.out.println(dish);
			}

			System.out.println("toGoStationCount=" + menu.getToGoStationCount());

			System.out.println("Dish.STATION_MASK=" + Dish.STATION_MASK);

			System.out.println("=====Last Line=====");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(
					"Conneting to DB connectin failed. Is DB server is running or DB tables are created with CreateDB and ImportMenu. "
							+ e.getMessage());
			System.exit(-10);
		}
	}
}
