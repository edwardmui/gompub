package com.orderfoodnow.pos.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.DataSource;
import com.orderfoodnow.pos.shared.menu.Abbreviation;
import com.orderfoodnow.pos.shared.menu.AbbreviationTable;
import com.orderfoodnow.pos.shared.menu.Condiment;
import com.orderfoodnow.pos.shared.menu.CondimentTable;
import com.orderfoodnow.pos.shared.menu.CouponDish;
import com.orderfoodnow.pos.shared.menu.CouponDishTable;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.DishTable;
import com.orderfoodnow.pos.shared.menu.Subdish;
import com.orderfoodnow.pos.shared.menu.SubdishTable;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.OrderTable;

// This class parses the menu.csv file, cross check, the and insert and/or update the dishes in the DB.
public class ImportMenu {
	public static final String MENU_CSV_FILE = "conf/menu.csv"; // must be in a resources folder. e.g. src/test/resources

	private static final String YES = "Yes";
	private static final String NO = "No";
	private static final String NONE = "None";

	private static Dish[] dbDishes;
	private static List<Dish> csvDishes = new ArrayList<>();

	private static Map<String, Integer> dbDishNameToId;
	private static Map<String, Integer> csvDishNameToId = new HashMap<>();
	private static Map<String, Integer> csvDishFullNameToId = new HashMap<>();
	private static Map<String, Integer> dishNameToId = new HashMap<>(); // dishes from db and csv

	private static String loggingConfigFile;
	private static Logger logger;
	static {
		// if the logging properties file is not provided in the command line as a VM
		// argument as follow, then use the one in the resources folder
		// -Djava.util.logging.config.file="src/test/resources/conf/serverLogging.properties"
		loggingConfigFile = System.getProperty("java.util.logging.config.file");
		if (loggingConfigFile == null) {
			loggingConfigFile = ImportMenu.class.getClassLoader().getResource("conf/toolsLogging.properties").getFile();
			try {
				LogManager.getLogManager().readConfiguration(new FileInputStream(loggingConfigFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger = Logger.getLogger(ImportMenu.class.getName());
	}
	private static void insert(List<Dish> dishes, Connection connection) throws Exception {
		logger.finest("Entered");
		try (PreparedStatement dishPrepStmt = connection.prepareStatement("INSERT INTO " + DishTable.ALL_COLUMNS);
				PreparedStatement abbreviationPrepStmt = connection
						.prepareStatement("INSERT INTO " + AbbreviationTable.ALL_COLUMNS);
				PreparedStatement condimentPrepStmt = connection
						.prepareStatement("INSERT INTO " + CondimentTable.ALL_COLUMNS);
				PreparedStatement subdishPrepStmt = connection
						.prepareStatement("INSERT INTO " + SubdishTable.ALL_COLUMNS);
				PreparedStatement couponDishPrepStmt = connection
						.prepareStatement("INSERT INTO " + CouponDishTable.ALL_COLUMNS);) {

			for (Dish dish : dishes) {
				dish.fillPreparedStatementWithAllFields(dishPrepStmt);
				logger.fine(dishPrepStmt.toString());
				dishPrepStmt.executeUpdate();

				Map<Integer, String> abbreviations = dish.getIdToAbbreviation();
				if (abbreviations != null) {
					int abbreviationId = AbbreviationTable.ID_START_VALUE;
					for (Integer abbreviationPosition : abbreviations.keySet()) {
						String abbreviationName = abbreviations.get(abbreviationPosition);
						Abbreviation abbreviation = new Abbreviation(dish.getDishId(), abbreviationId++,
								abbreviationName);
						abbreviation.fillPreparedStatement(abbreviationPrepStmt);
						logger.fine(abbreviationPrepStmt.toString());
						abbreviationPrepStmt.executeUpdate();
					}
				}

				Map<String, List<Float>> condiments = dish.getCondimentNameToQuantities();
				if (condiments != null) {
					int condimentId = CondimentTable.ID_START_VALUE;
					for (String condimentName : condiments.keySet()) {
						Condiment condiment = new Condiment(dish.getDishId(), condimentId++,
								condiments.get(condimentName), condimentName);
						condiment.fillPreparedStatement(condimentPrepStmt);
						logger.fine(condimentPrepStmt.toString());
						condimentPrepStmt.executeUpdate();
					}
				}

				List<Subdish> subdishes = dish.getSubdishes();
				if (subdishes != null) {
					for (Subdish subdish : dish.getSubdishes()) {
						subdish.fillPreparedStatement(subdishPrepStmt);
						logger.fine(subdishPrepStmt.toString());
						subdishPrepStmt.executeUpdate();
					}
				}

				CouponDish couponDish = dish.getCouponDish();
				if (couponDish != null) {
					couponDish.fillPreparedStatement(couponDishPrepStmt);
					logger.fine(couponDishPrepStmt.toString());
					couponDishPrepStmt.executeUpdate();
				}
			}
		}
	}

	private static void parseMenu() throws Exception {
		logger.finest("Entered");
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classloader.getResourceAsStream(MENU_CSV_FILE);
		InputStreamReader streamReader = new InputStreamReader(inputStream);

		// with try block, the bufferReader resource when done. finally() is not needed
		try (BufferedReader bufferedReader = new BufferedReader(streamReader)) {
			String tokenString = null;
			String currentLine;
			int dishId = (dbDishes == null) ? DishTable.ID_START_VALUE : dbDishes.length;
			int lineCount = 0;
			while ((currentLine = bufferedReader.readLine()) != null) {
				++lineCount;
				logger.finest("line " + lineCount + ": " + currentLine);

				currentLine = currentLine.replace('"', ' ').trim();

				if (currentLine.length() == 0) {
					continue; // skip all blank lines
				}

				if ('#' == currentLine.charAt(0)) {
					continue; // skip all lines start with '#' lines. They are treated as comments.
				}

				StringTokenizer attr = new StringTokenizer(currentLine, ",");

				Dish dish = new Dish();
				dish.setDishId(dishId);
				try {
					tokenString = attr.nextToken().trim();
					dish.setCategory(Integer.parseInt(tokenString));
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Dish Category must be an integer. " + e.getMessage() + " at line: "
							+ lineCount + ". Entire content: \"" + currentLine + '"');
				} catch (Exception e) {
					throw new Exception("Dish Category Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				if (dish.getCategory() < 0) {
					logger.warning("Dish Category=" + tokenString + " is negative at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					dish.setCode(Integer.parseInt(tokenString));
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Dish Code must be an integer. " + e.getMessage() + " at line: "
							+ lineCount + ". Entire content: \"" + currentLine + '"');
				} catch (Exception e) {
					throw new Exception("Dish Code Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				if (dish.getCode() < 0) {
					logger.warning("Dish Code=" + tokenString + " is negative at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
				}
	
				try {
					tokenString = attr.nextToken().trim();
					// double quotes " is used internally for constructing SQL statements. It's
					// better to not allow it in dish name.
					if (tokenString.indexOf('"') != -1) {
						throw new Exception("Dish Name CANNOT have \" (double quote) at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}

					if (tokenString.length() > DishTable.NAME_SIZE) {
						tokenString = tokenString.substring(0, DishTable.NAME_SIZE);
						logger.warning(
								"Dish Name=" + tokenString + " is truncated to max length of " + DishTable.NAME_SIZE
										+ " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
					}
					dish.setShortName(tokenString);

					if (csvDishNameToId.containsKey(tokenString)) {
						throw new Exception("CANNOT have duplicated dish name in the menu.  dish Name: "
								+ tokenString + " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
					} else {
						csvDishNameToId.put(tokenString, dish.getDishId());
					}
				} catch (Exception e) {
					throw new Exception("Dish Name Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					if (tokenString.compareToIgnoreCase("NA") != 0) {
						if (tokenString.length() > DishTable.CHINESE_NAME_SIZE) {
							//tokenString = tokenString.substring(0, DishTable.CHINESE_NAME_SIZE);
							// substring on Chinese character likely would not work. Exit instead of truncate.
							throw new Exception ("Dish Chinese Name=" + tokenString + " is truncated to max length of "
									+ DishTable.CHINESE_NAME_SIZE + " at line: " + lineCount + ". Entire content: \""
									+ currentLine + '"');
							
						}
						dish.setChineseName(tokenString);
					}
				} catch (Exception e) {
					throw new Exception("Dish Chinese Name Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					int largePrice = Integer.parseInt(tokenString);
					if (largePrice < 0) {
						throw new Exception("Cannot be negative");
					}
					dish.setLargePrice(largePrice);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Dish Large Price must be an integer. " + e.getMessage()
							+ " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
				} catch (Exception e) {
					throw new Exception("Dish Large Price Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				if (dish.getLargePrice() < 0) {
					logger.warning("Dish Large Price=" + tokenString + " is negative at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
				}
	
				try {
					tokenString = attr.nextToken().trim();
					int smallPrice = Integer.parseInt(tokenString);
					if (smallPrice < 0) {
						throw new Exception("Cannot be negative");
					}
					dish.setSmallPrice(smallPrice);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Dish Small Price must be an integer. " + e.getMessage()
							+ " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
				} catch (Exception e) {
					throw new Exception("Dish Small Price Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				if (dish.getSmallPrice() < 0) {
					logger.warning("Dish Small Price=" + tokenString + " is negative at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					if (tokenString.equals(YES)) {
						dish.setActive(true);
					} else if (tokenString.equals(NO)) {
						dish.setActive(false);
					} else {
						throw new Exception("Dish Active Unexpected value at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}
				} catch (Exception e) {
					throw new Exception("Dish Active Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					if (tokenString.equals(YES)) {
						dish.setAvailableOnline(true);
					} else if (tokenString.equals(NO)) {
						dish.setAvailableOnline(false);
					} else {
						throw new Exception("Dish Available Online Unexpected value at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}
				} catch (Exception e) {
					throw new Exception("Dish Available Online Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					// double quotes " is used internally for constructing SQL statements. It's
					// better to not allow it in dish full name.
					if (tokenString.indexOf('"') != -1) {
						throw new Exception("Dish Full Name CANNOT have \" (double quote) at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}

					if (tokenString.length() > DishTable.FULL_NAME_SIZE) {
						tokenString = tokenString.substring(0, DishTable.FULL_NAME_SIZE);
						logger.warning(
								"Dish Full Name=" + tokenString + " is truncated to max length of " + DishTable.FULL_NAME_SIZE
										+ " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
					}
					dish.setFullName(tokenString);

					if (csvDishFullNameToId.containsKey(tokenString)) {
						throw new Exception("CANNOT have duplicated dish full name in the menu.  dishFullName="
								+ tokenString + " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
					} else {
						csvDishFullNameToId.put(tokenString, dish.getDishId());
					}
				} catch (Exception e) {
					throw new Exception("Dish Full Name Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}

				try {
					tokenString = attr.nextToken().trim();
					// double quotes " is used internally for constructing SQL statements. It's
					// better to not allow it in dish description.
					if (tokenString.indexOf('"') != -1) {
						throw new Exception("Dish Description CANNOT have \" (double quote) at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}

					if (tokenString.length() > DishTable.DESCRIPTION_SIZE) {
						tokenString = tokenString.substring(0, DishTable.DESCRIPTION_SIZE);
						logger.warning(
								"Dish Description=" + tokenString + " is truncated to max length of " + DishTable.DESCRIPTION_SIZE
										+ " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
					}
					dish.setDescription(tokenString);

				} catch (Exception e) {
					throw new Exception("Dish Description Exception. " + e.getMessage() + " at line: " + lineCount
							+ ". Entire content: \"" + currentLine + '"');
				}
				// abbreviations: expect at least one abbreviation per dish and use map to
				// preserve the order of the abbreviations specified in the file as the first
				// one is considered the primary abbreviation
				Map<Integer, String> abbreviationIdToName = new HashMap<>();
				int abbreviationPosition = 0;
				while (attr.hasMoreTokens()) {
					try {
						tokenString = attr.nextToken().trim();

						if (tokenString.equals(";")) {
							break;
						}

						if (tokenString.length() > AbbreviationTable.NAME_SIZE) {
							tokenString = tokenString.substring(0, AbbreviationTable.NAME_SIZE);
							logger.warning("Dish Abbreviation Name=" + tokenString + " is truncated to max length of "
									+ AbbreviationTable.NAME_SIZE + " at line: " + lineCount + ". Entire content: \""
									+ currentLine + '"');
						}
						abbreviationIdToName.put(abbreviationPosition++, tokenString);
					} catch (Exception e) {
						throw new Exception("Dish Abbreviation Exception. " + e.getMessage() + " at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}
				}
				int abbreviationCount = abbreviationIdToName.size();
				if (abbreviationCount == 0) {
					String error = "Missing abbreviation for dish at line: " + lineCount + ". Entire content: \""
							+ currentLine + '"';
					logger.severe("Menu Error: " + error);
					throw new Exception(error);
				}
				dish.setIdToAbbreviation(abbreviationIdToName);

				if (dish.getCategory() == Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.couponDish)) {
					CouponDish couponDish = new CouponDish();
					couponDish.setDishId(dish.getDishId());
					couponDish.setName(dish.getShortName());
					try {
						tokenString = attr.nextToken().trim();
						// double quotes " is used internally for constructing SQL statements. It's
						// better to not allow it in dish name.
						if (tokenString.indexOf('"') != -1) {
							throw new Exception("Coupon Dish1 Name CANNOT have \" (double quote) at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}

						if (tokenString.length() > CouponDishTable.DISH1_NAME_SIZE) {
							tokenString = tokenString.substring(0, CouponDishTable.DISH1_NAME_SIZE);
							logger.warning("Coupon Dish1 Name=" + tokenString + " is truncated to max length of "
									+ CouponDishTable.DISH1_NAME_SIZE + " at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}
						if (tokenString.isEmpty() == false && tokenString.equals(NONE) == false) {
							couponDish.setDish1Name(tokenString);
						}
					} catch (Exception e) {
						throw new Exception("Dish1 Name Exception. " + e.getMessage() + " at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}

					try {
						tokenString = attr.nextToken().trim();
						int largeQuantity = Integer.parseInt(tokenString);
						if (largeQuantity < 0) {
							throw new Exception("Cannot be negative");
						}
						couponDish.setDish1LargeQuantity(largeQuantity);
					} catch (NumberFormatException e) {
						couponDish.setDish1LargeQuantity(0);
					} catch (Exception e) {
						throw new Exception("Coupon Dish 1 large quantity Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}

					try {
						tokenString = attr.nextToken().trim();
						int smallQuantity = Integer.parseInt(tokenString);
						if (smallQuantity < 0) {
							throw new Exception("Cannot be negative");
						}
						couponDish.setDish1SmallQuantity(smallQuantity);
					} catch (NumberFormatException e) {
						couponDish.setDish1SmallQuantity(0);
					} catch (Exception e) {
						throw new Exception("Coupon Dish 1 small quantity Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}

					try {
						tokenString = attr.nextToken().trim();
						// double quotes " is used internally for constructing SQL statements.
						// It's better to not allow it in the dish name.
						if (tokenString.indexOf('"') != -1) {
							throw new Exception("Coupon Dish2 Name CANNOT have \" (double quote) at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}

						if (tokenString.length() > CouponDishTable.DISH2_NAME_SIZE) {
							tokenString = tokenString.substring(0, CouponDishTable.DISH2_NAME_SIZE);
							logger.warning("Coupon Dish2 Name=" + tokenString + " is truncated to max length of "
									+ CouponDishTable.DISH2_NAME_SIZE + " at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}
						if (tokenString.isEmpty() == false && tokenString.equals(NONE) == false) {
							couponDish.setDish2Name(tokenString);
						}
					} catch (Exception e) {
						throw new Exception("Dish2 Name Exception. " + e.getMessage() + " at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}

					try {
						tokenString = attr.nextToken().trim();
						int largeQuantity = Integer.parseInt(tokenString);
						if (largeQuantity < 0) {
							throw new Exception("Cannot be negative");
						}
						couponDish.setDish2LargeQuantity(largeQuantity);
					} catch (NumberFormatException e) {
						couponDish.setDish2LargeQuantity(0);
					} catch (Exception e) {
						throw new Exception("Coupon Dish 2 large quantity Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}

					try {
						tokenString = attr.nextToken().trim();
						int smallQuantity = Integer.parseInt(tokenString);
						if (smallQuantity < 0) {
							throw new Exception("Cannot be negative");
						}
						couponDish.setDish2SmallQuantity(smallQuantity);
					} catch (NumberFormatException e) {
						couponDish.setDish2SmallQuantity(0);
					} catch (Exception e) {
						throw new Exception("Coupon Dish 2 small quantity Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}

					try {
						tokenString = attr.nextToken().trim();
						int minimumFoodTotal = Integer.parseInt(tokenString);
						if (minimumFoodTotal < 0) {
							throw new Exception("Cannot be negative");
						}
						couponDish.setMinimumFoodTotal(minimumFoodTotal);
					} catch (NumberFormatException e) {
						couponDish.setMinimumFoodTotal(0);
					} catch (Exception e) {
						throw new Exception("Coupon Dish minimum food total Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}

					dish.setCouponDish(couponDish);
				}

				long toGoStationBits = 0;
				int toGoStationCount = 1; // dishWorkstationId starts at 1.
				while (attr.hasMoreTokens()) {
					try {
						tokenString = attr.nextToken().trim();
						if (tokenString.equals(";")) {
							break;
						}
						if (tokenString.equals(YES)) {
							toGoStationBits |= Dish.STATION_MASK.get(toGoStationCount);
						} else if (tokenString.equals(NO)) {
							;
						} else {
							throw new Exception("ToGo Dish Workstation Unexpected value at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}
					} catch (Exception e) {
						throw new Exception("ToGO Dish Workstation Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}
					toGoStationCount++;
				}
				dish.setToGoStationBits(toGoStationBits);

				long dineInStationBits = 0;
				int dineInStationCount = 1; // dishWorkstationId starts at 1.
				while (attr.hasMoreTokens()) {
					try {
						tokenString = attr.nextToken().trim();
						if (tokenString.equals(";")) {
							break;
						}
						if (tokenString.equals(YES)) {
							dineInStationBits |= Dish.STATION_MASK.get(dineInStationCount);
						} else if (tokenString.equals(NO)) {
							;
						} else {
							throw new Exception("Dine-In Dish Workstation Unexpected value at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}
					} catch (Exception e) {
						throw new Exception("Dine-In Dish Workstation Exception. " + e.getMessage() + " at line: "
								+ lineCount + ". Entire content: \"" + currentLine + '"');
					}
					dineInStationCount++;
				}
				dish.setDineInStationBits(dineInStationBits);

				// condiments
				HashMap<String, List<Float>> condimentNameToQuantities = null;
				if (attr.hasMoreTokens()) {
					condimentNameToQuantities = new HashMap<String, List<Float>>();
				}

				while (attr.hasMoreTokens()) {
					try {
						String condimentName = attr.nextToken().trim();
						if (condimentName.equals(";")) {
							if (condimentNameToQuantities.size() == 0) {
								condimentNameToQuantities = null;
							}
							break;
						}

						if (attr.hasMoreTokens() == false) {
							throw new Exception("Dish Condiment must have a name at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}

						if (condimentNameToQuantities.containsKey(condimentName)) {
							throw new Exception("CANNOT have duplicated condiment name in the same dish at line: "
									+ lineCount + ". Entire content: \"" + currentLine + '"');
						}
						if (condimentName.length() > CondimentTable.NAME_SIZE) {
							condimentName = condimentName.substring(0, CondimentTable.NAME_SIZE);
							logger.warning("Dish Condiment name=" + condimentName + " is truncated to max length of "
									+ CondimentTable.NAME_SIZE + " at line: " + lineCount + ". Entire content: \""
									+ currentLine + '"');
						}

						tokenString = attr.nextToken().trim();
						Float condimentLargeQuantity = null;
						try {
							condimentLargeQuantity = Float.valueOf(tokenString);
						} catch (NumberFormatException e) {
							throw new NumberFormatException(
									"Dish Condiment large quantity must be a decimal number. " + e.getMessage()
											+ " at line: " + lineCount + ". Entire content: \"" + currentLine + '"');
						}

						tokenString = attr.nextToken().trim();
						Float condimentSmallQuantity = null;
						try {
							condimentSmallQuantity = Float.valueOf(tokenString);
						} catch (NumberFormatException e) {
							throw new NumberFormatException(
									"Dish Condiment small must be a decimal number. " + e.getMessage() + " at line: "
											+ lineCount + ". Entire content: \"" + currentLine + '"');
						}

						List<Float> condimentQuantities = new ArrayList<>();
						condimentQuantities.add(0, condimentLargeQuantity); // large quantity in index 0
						condimentQuantities.add(1, condimentSmallQuantity); // small quantity in index 1
						condimentNameToQuantities.put(condimentName, condimentQuantities);
					} catch (Exception e) {
						throw new Exception("Dish Condiment Exception. " + e.getMessage() + " at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}
				}
				dish.setCondimentNameToQuantities(condimentNameToQuantities);

				int subdishId = SubdishTable.ID_START_VALUE;
				List<Subdish> subdishes = new ArrayList<>();
				Set<String> subdishNames = new HashSet<>();
				while (attr.hasMoreTokens()) {
					try {

						String subdishName = attr.nextToken().trim();
						if (subdishName.length() > SubdishTable.NAME_SIZE) {
							subdishName = subdishName.substring(0, SubdishTable.NAME_SIZE);
							logger.warning("Dish Subdish Name=" + subdishName + " is truncated to max length of "
									+ SubdishTable.NAME_SIZE + " at line: " + lineCount + ". Entire content: \""
									+ currentLine + '"');
						}
						if (subdishNames.contains(subdishName)) {
							throw new Exception("CANNOT have duplicated subdish name in the same dish at line: "
									+ lineCount + ". Entire content: \"" + currentLine + '"');
						} else {
							subdishNames.add(subdishName);
						}

						if (attr.hasMoreTokens() == false) {
							throw new Exception("Dish Subdish must have a quantity at line: " + lineCount
									+ ". Entire content: \"" + currentLine + '"');
						}

						String subdishQuantityStr = attr.nextToken().trim();
						Float subdishQuantity = null;
						try {
							subdishQuantity = Float.valueOf(subdishQuantityStr);
						} catch (NumberFormatException e) {
							throw new NumberFormatException(
									"Dish Subdish amount must be a decimal number. " + e.getMessage() + " at line: "
											+ lineCount + ". Entire content: \"" + currentLine + '"');
						}

						Subdish subdish = new Subdish(dishId, subdishId++, subdishName, subdishQuantity);
						subdishes.add(subdish);
					} catch (Exception e) {
						throw new Exception("Dish Subdish Exception. " + e.getMessage() + " at line: " + lineCount
								+ ". Entire content: \"" + currentLine + '"');
					}
				}
				dish.setSubdishes(subdishes.size() == 0 ? null : subdishes);

				boolean addToCsvDishes = false;
				if (dbDishNameToId == null || dbDishNameToId.isEmpty()) {
					addToCsvDishes = true;
				} else {
					Integer dbDishId = dbDishNameToId.get(dish.getShortName());
					if (dbDishId == null || dbDishes[dbDishId].equals(dish) == false) {
						addToCsvDishes = true;
					}
				}

				if (addToCsvDishes) {
					csvDishes.add(dish);
					dishId++;
				}
			} // end while reading each line from the menu file
		} // end try-with-resources
	}

	private static void crossCheck() throws Exception {
		logger.finest("Entered");
		// populate dishNameToId with dishes from DB and Menu.csv
		for (Dish dbDish : dbDishes) {
			String dishName = dbDish.getShortName();
			if (dishNameToId.containsKey(dishName) == false) {
				dishNameToId.put(dishName, dbDish.getDishId());
			}
		}
		for (Dish dish : csvDishes) {
			if (dish.isActive()) {
				String dishName = dish.getShortName();
				if (dishNameToId.containsKey(dishName) == false) {
					dishNameToId.put(dishName, dish.getDishId());
				}
			}
		}

		for (Dish dish : csvDishes) {
			CouponDish couponDish = dish.getCouponDish();
			if (couponDish != null) {
				if (dishNameToId.get(couponDish.getDish1Name()) == null) {
					throw new Exception("Coupon Dish1Name='" + couponDish.getDish1Name()
							+ "' CANNOT be found in the menu or db. couponDish=" + couponDish.toString());
				}

				String dish2Name = couponDish.getDish2Name();
				if (dish2Name != null && dish2Name.isEmpty() == false && dishNameToId.get(dish2Name) == null) {
					throw new Exception("Coupon Dish2Name='" + couponDish.getDish2Name()
							+ "' CANNOT be found in the menu or db. couponDish=" + couponDish.toString());
				}
			}

			Map<String, List<Float>> condimentsToQuantities = dish.getCondimentNameToQuantities();
			if (condimentsToQuantities != null) {
				for (String condimentName : condimentsToQuantities.keySet()) {
					if (dishNameToId.containsKey(condimentName) == false) {
						throw new Exception("condimentName='" + condimentName
								+ "' does not have a coresponding dish. dish=" + dish.toString());
					}

					if (condimentName.equals(dish.getShortName())) {
						throw new Exception("condimentName='" + condimentName
								+ "' CANNOT use itself as a condiment. dish=" + dish.toString());
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		logger.finest("Entered");

		logger.fine("Starting ImportMenu for " + Configuration.getRestaurantName() + " " + Configuration.getRestaurantChineseName());
		String databaseName = Configuration.getDatasourceDatabase();
		logger.fine("databaseName=" + databaseName);

		if (DataSource.isDatabaseExist(databaseName) == false) {
			logger.fine("databaseName=" + databaseName + " does not exist. Creating it.");
			try {
				CreateDB.createDB(databaseName);
				CreateDB.insertDefaults();
			} catch (Exception e) {
				e.printStackTrace();
				logger.severe("Fail to create database with name=" + databaseName + ". Exception: " + e.getMessage());
				System.exit(-10);
			}
		} else {
			logger.fine("databaseName=" + databaseName + " exists. Proceed to check for menu import.");
		}

		try (Connection connection = DataSource.getConnection()) {
			dbDishes = DishTable.readAllOf(connection);
			dbDishNameToId = DishTable.readAllOfAsMap(connection);

			parseMenu();
			crossCheck();

			connection.setAutoCommit(false);

			if (dbDishes.length == 0) {
				// Insert the entire menu should be one transaction. i.e. don't commit until all
				// tables are updated. BTW if autoCommit is true,
				// it takes over 7 minutes vs 2 seconds for this function to finish.
				logger.fine("Empty menu in DB. Inserting all dishes into the database.");
				insert(csvDishes, connection);
			} else {
				logger.fine("Checking for dish changes in Menu.csv that need update to the database.");

				List<Dish> csvUpdatingDishes = new ArrayList<>();
				List<Dish> csvInsertingDishes = new ArrayList<>();
				List<Dish> dbUpdatingDishes = new ArrayList<>();
				for (Dish csvDish : csvDishes) {
					Integer dbDishId = dbDishNameToId.get(csvDish.getShortName());
					if (dbDishId == null) {
						logger.fine("New csvDish=" + csvDish);
						csvInsertingDishes.add(csvDish);
					} else {
						Dish dbDish = dbDishes[dbDishId];
						if (csvDish.equalsIgnoreKeysIgnoreActive(dbDish) == false) {
							logger.fine("Differ ignore key ignore active 1. csvDish=" + csvDish);
							logger.fine("Differ ignore key ignore active 2. dbDish =" + dbDish);
							dbDish.setActive(false);
							csvUpdatingDishes.add(dbDish);
							csvInsertingDishes.add(csvDish);
						} else if (csvDish.equalsIgnoreKeys(dbDish) == false) {
							logger.fine("Differ ignore key 1. csvDish=" + csvDish);
							logger.fine("Differ ignore key 2. dbDish =" + dbDish);
							dbDish.setActive(csvDish.isActive());
							dbUpdatingDishes.add(dbDish);
						}
					}
				}

				if (csvUpdatingDishes.isEmpty() && csvInsertingDishes.isEmpty() && dbUpdatingDishes.isEmpty()) {
					logger.fine("The DB is up to date with the Menu.csv.");
				} else {
					System.out.println(
							"Menu update to the database is needed. Please make sure a latest database backup has been done. Settle all orders before proceed. Do you want continue? (Yes/No)");
					Scanner scanner = new Scanner(System.in);
					String userInput = scanner.next();
					if (userInput.equalsIgnoreCase("y") || userInput.equalsIgnoreCase("yes")) {
						logger.fine("Got a 'yes'. Proceed with database update.");
						List<Order> orders = OrderTable.readAllUnsettled(connection);
						if (orders.isEmpty() == false) {
							System.out.println("Found unsettled orders. They must be settled before proceed. Exiting");
							System.exit(-50);
						}

						if (csvUpdatingDishes.isEmpty() == false && csvInsertingDishes.isEmpty() == false) {
							Dish.update(csvUpdatingDishes, connection);
							insert(csvInsertingDishes, connection);
							// Clean up inactive abbreviations. Optional as they'll not used anymore.
							Abbreviation.delete(csvUpdatingDishes, connection);
						} else if (csvUpdatingDishes.isEmpty() == false) {
							Dish.update(csvUpdatingDishes, connection);
							Abbreviation.delete(csvUpdatingDishes, connection); // optional clean up
						} else if (csvInsertingDishes.isEmpty() == false) {
							insert(csvInsertingDishes, connection);
						}

						if (dbUpdatingDishes.isEmpty() == false) {
							Dish.update(dbUpdatingDishes, connection);
						}
					} else {
						logger.fine("NO database update was performed.");
					}
					scanner.close();
				}
			}

			connection.commit();
			connection.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Is DB server is running or DB tables are created with CreateDB and ImportMenu. " + e.getMessage());
		}

		logger.fine("All outstanding transactions are committed. Done Importing Menu.");
//		System.out.println("Hit enter to exit. Run jcmd to dispaly information related to this jvm. e.g.: jcmd to show pid. 'jcmd <pid> VM.system_properties'");
//		try {
//			new BufferedReader(new InputStreamReader(System.in)).readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
