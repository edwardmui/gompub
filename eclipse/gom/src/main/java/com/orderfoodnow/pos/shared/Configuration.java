package com.orderfoodnow.pos.shared;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class Configuration {
	private static final Logger logger = Logger.getLogger(Configuration.class.getName());

	// When adding getter methods like the ones below, the casting type is likely
	// documented in
	// https://bitbucket.org/asomov/snakeyaml/wiki/Documentation
	// Methods below give some good examples..
	// The 'System.out.println(nameToConfigurationObject)' in the constructor
	// outputs the entire map
	// object has details of the data structure for casting.
	// Run main() in this class provides the print out.
	// Example: List uses [], Map uses {}

	// Start of getters methods based on the parameter key in the conf.yaml file.
	// ================= Required configuration parameters section==========

	public static String getRestaurantName() {
		logger.finest("Entered");
		String restaurantName = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantName);
		return restaurantName == null ? "" : restaurantName;
	}

	public static String getRestaurantChineseName() {
		logger.finest("Entered");
		String restaurantChineseName = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantChineseName);
		return restaurantChineseName == null ? "" : restaurantChineseName;
	}

	public static String getRestaurantAddress() {
		logger.finest("Entered");
		String restaurantAddress = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantAddress);
		return restaurantAddress == null ? "" : restaurantAddress;
	}

	public static String getRestaurantCity() {
		logger.finest("Entered");
		String restaurantCity = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantCity);
		return restaurantCity == null ? "" : restaurantCity;
	}

	public static String getRestaurantState() {
		logger.finest("Entered");
		String restaurantState = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantState);
		return restaurantState == null ? "" : restaurantState;
	}

	public static String getRestaurantZip() {
		logger.finest("Entered");
		String restaurantZip = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantZip);
		return restaurantZip == null ? "" : restaurantZip;
	}

	public static String getRestaurantCityStateZip() {
		logger.finest("Entered");
		String restaurantCity = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantCity);
		String restaurantState = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantState);
		String restaurantZip = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantZip);
		return restaurantCity + ", " + restaurantState + " " + restaurantZip;
	}

	public static String getRestaurantNextToLandmark() {
		logger.finest("Entered");
		String restaurantNextToLandmark = (String) nameToConfigurationObject
				.get(ConfigKeyDefs.restaurantNextToLandmark);
		return restaurantNextToLandmark == null ? "" : restaurantNextToLandmark;
	}

	public static double getRestaurantLatitude() {
		logger.finest("Entered");
		return (double) nameToConfigurationObject.get(ConfigKeyDefs.restaurantLatitude);
	}

	public static double getRestaurantLongitude() {
		logger.finest("Entered");
		return (double) nameToConfigurationObject.get(ConfigKeyDefs.restaurantLongitude);
	}

	public static String getRestaurantPhone() {
		logger.finest("Entered");
		String restaurantPhone = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantPhone);
		return restaurantPhone == null ? "" : restaurantPhone;
	}

	public static String getRestaurantAreaCode() {
		logger.finest("Entered");
		String restaurantAreaCode = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantAreaCode);
		return restaurantAreaCode == null ? "" : String.valueOf(restaurantAreaCode);
	}

	public static String getRestaurantUrl() {
		logger.finest("Entered");
		String restaurantUrl = (String) nameToConfigurationObject.get(ConfigKeyDefs.restaurantUrl);
		return restaurantUrl == null ? "" : restaurantUrl;
	}

	public static String getDatasourceUrl() {
		logger.finest("Entered");
		String datasourceUrl = null;
		String datasourceUrlBase = "jdbc:mariadb://localhost:";
		// -DDBPORT=3306  //Provide this command line  VM argument as follow to override the value in conf.yaml file
		String dbport = System.getProperty("DBPORT");
		if (dbport == null) {
			datasourceUrl = (String) nameToConfigurationObject.get(ConfigKeyDefs.datasourceUrl);
			if (datasourceUrl == null) {
				datasourceUrl = datasourceUrlBase + "3306/";
			}
		} else {
			datasourceUrl = datasourceUrlBase + dbport + "/";
		}

		logger.config("datasourceUrl=" + datasourceUrl);

		return datasourceUrl;
	}

	public static String getDatasourceUser() {
		logger.finest("Entered");
		String datasourceUser = (String) nameToConfigurationObject.get(ConfigKeyDefs.datasourceUser);
		return datasourceUser == null ? "root" : datasourceUser;
	}

	public static String getDatasourcePasword() {
		logger.finest("Entered");
		String datasourcePasword = (String) nameToConfigurationObject.get(ConfigKeyDefs.datasourcePassword);
		return datasourcePasword == null ? "password123" : datasourcePasword;
	}

	// database name must be in lower case
	private static final String GOM_DATABASE_NAME = "gom";
	public static String getDatasourceDatabase() {
		logger.finest("Entered");
		String datasourceDatabase = (String) nameToConfigurationObject.get(ConfigKeyDefs.datasourceDatabase);
		return datasourceDatabase == null ? GOM_DATABASE_NAME : datasourceDatabase;
	}

	private static List<String> restaurantOperationHours;

	public static List<String> getRestaurantOperationHours() {
		logger.finest("Entered");
		if (restaurantOperationHours == null) {
			restaurantOperationHours = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.restaurantOperationHours);
			if (restaurantOperationHours == null) {
				// not in conf.yaml, provide default
				restaurantOperationHours = List.of("Tue-Thu 11:00 am -  9:00 pm", "Fri     11:00 am - 10:00 pm",
						"Sat     12:00 pm - 10:00 pm", "Sun     12:00 pm -  9:00 pm", "Mon     Closed");
			}
		}
		return restaurantOperationHours;
	}

	private static List<String> restaurantReceiptEndingText;

	public static List<String> getRestaurantReceiptEndingText() {
		logger.finest("Entered");
		if (restaurantReceiptEndingText == null) {
			restaurantReceiptEndingText = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.restaurantReceiptEndingText);
			if (restaurantReceiptEndingText == null) {
				// not in conf.yaml, provide default
				restaurantReceiptEndingText = List.of("Thank you for choosing us", "It's our pleasure serving you.",
						"See You Again Soon!", "", "-------------------------------");
			}
		}
		return restaurantReceiptEndingText;
	}

	private static List<String> restaurantDiningEndingText;

	public static List<String> getRestaurantDiningEndingText() {
		logger.finest("Entered");
		if (restaurantDiningEndingText == null) {
			restaurantDiningEndingText = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.restaurantDiningEndingText);
			if (restaurantDiningEndingText == null) {
				// not in conf.yaml, provide default
				restaurantDiningEndingText = List.of("Thank you for dining us and", "for being a part of our family.",
						"It's our pleasure serving you.", "Please Come Again!", "", "-------------------------------");
			}
		}
		return restaurantDiningEndingText;
	}

	public static Float getRestaurantTaxRate() {
		logger.finest("Entered");
		Double restaurantTaxRate = (Double) nameToConfigurationObject.get(ConfigKeyDefs.restaurantTaxRate);
		if (restaurantTaxRate == null) {
			return Float.valueOf(0); // no taxRate specified in conf.yaml file. Not a good idea.
		}
		return (float) (restaurantTaxRate / 100.0);
	}

	// ================= Optional configuration parameters section==========
	// Optional configuration parameters where default values are provided in each
	// method. Specified value in conf.yaml file override the default in the method.
	private static Integer lowestDeliveryCost;
	private static List<Integer> deliveryCosts;
	private static List<Integer> deliveryCostsDefault = Arrays.asList(0, 300, 350, 400, 450, 500, 550, 600, 650, 700,
			750, 800);

	public static Integer getLowestDeliveryCost() {
		logger.finest("Entered");
		if (lowestDeliveryCost == null) {
			deliveryCosts = (List<Integer>) nameToConfigurationObject.get(ConfigKeyDefs.deliveryCosts);
			if (deliveryCosts == null) {
				deliveryCosts = deliveryCostsDefault;
			}
			Collections.sort(deliveryCosts);

			for (Integer deliveryCost : deliveryCosts) {
				if (deliveryCost > 0) {
					lowestDeliveryCost = deliveryCost;
					break;
				}
			}
		}

		return lowestDeliveryCost;
	}

	public static int getDeliveryZoneCount() {
		logger.finest("Entered");
		if (deliveryCosts == null) {
			getDeliveryCostAt(0); // force a population of deliveryCosts
		}

		return deliveryCosts.size();
	}

	public static Integer getDeliveryCostAt(int index) {
		logger.finest("Entered");
		if (deliveryCosts == null) {
			deliveryCosts = (List<Integer>) nameToConfigurationObject.get(ConfigKeyDefs.deliveryCosts);
			if (deliveryCosts == null) {
				deliveryCosts = deliveryCostsDefault;
			}
			Collections.sort(deliveryCosts);
		}

		if (index >= deliveryCosts.size()) {
			int highest = deliveryCosts.get(deliveryCosts.size() - 1);
			String msg = "The specified index of " + index + " is index out of bound for a list of size "
					+ deliveryCosts.size() + " Returning the highest value of " + highest;
			logger.warning(msg);
			return highest;
		} else if (index < 0) {
			String msg = "The specified index of " + index + " is negative.  Returning the 0";
			logger.warning(msg);
			return 0;
		} else {
			return deliveryCosts.get(index);
		}
	}

	private static Integer minimumSubtotalForDeliveryInCent;

	public static Integer getMinimumSubtotalForDeliveryInCent() {
		logger.finest("Entered");
		if (minimumSubtotalForDeliveryInCent == null) {
			minimumSubtotalForDeliveryInCent = (Integer) nameToConfigurationObject
					.get(ConfigKeyDefs.minimumSubtotalForDeliveryInCent);
			if (minimumSubtotalForDeliveryInCent == null) {
				minimumSubtotalForDeliveryInCent = 0;
			}
		}
		return minimumSubtotalForDeliveryInCent;
	}

	private static Integer maxHistoryOrderDisplayCount;

	public static Integer getMaxHistoryOrderDisplayCount() {
		logger.finest("Entered");
		if (maxHistoryOrderDisplayCount == null) {
			maxHistoryOrderDisplayCount = (Integer) nameToConfigurationObject
					.get(ConfigKeyDefs.maxHistoryOrderDisplayCount);
			if (maxHistoryOrderDisplayCount == null) {
				maxHistoryOrderDisplayCount = 3;
			}
		}
		return maxHistoryOrderDisplayCount;
	}

	private static Boolean deliveryOrderMustHavePhoneNumber;

	public static Boolean getDeliveryOrderMustHavePhoneNumber() {
		logger.finest("Entered");
		if (deliveryOrderMustHavePhoneNumber == null) {
			deliveryOrderMustHavePhoneNumber = (Boolean) nameToConfigurationObject
					.get(ConfigKeyDefs.deliveryOrderMustHavePhoneNumber);
			if (deliveryOrderMustHavePhoneNumber == null) {
				deliveryOrderMustHavePhoneNumber = true;
			}
		}
		return deliveryOrderMustHavePhoneNumber;
	}

	private static Boolean phoneInOrderMustHavePhoneNumber;

	public static Boolean getPhoneInOrderMustHavePhoneNumber() {
		logger.finest("Entered");
		if (phoneInOrderMustHavePhoneNumber == null) {
			phoneInOrderMustHavePhoneNumber = (Boolean) nameToConfigurationObject
					.get(ConfigKeyDefs.phoneInOrderMustHavePhoneNumber);
			if (phoneInOrderMustHavePhoneNumber == null) {
				phoneInOrderMustHavePhoneNumber = true;
			}
		}
		return phoneInOrderMustHavePhoneNumber;
	}

	public static Integer getRestaurantCapability() {
		logger.finest("Entered");
		int dayOfTheWeek = Util.getDayOfWeek(System.currentTimeMillis());
		return getRestaurantCapabilities().get(dayOfTheWeek - 1); // -1 to adjust dayOfTheWeek is 1 based where
																	// Calendar.SUNDAY = 1
	}

	public static List<Integer> getRestaurantCapabilities() {
		logger.finest("Entered");
		List<Integer> restaurantCapabilities = (List<Integer>) nameToConfigurationObject
				.get(ConfigKeyDefs.restaurantCapabilities);
		if (restaurantCapabilities == null) {
			restaurantCapabilities = Arrays.asList(100, 100, 100, 100, 100, 125, 125);
		}
		return restaurantCapabilities;
	}

	public static Integer getDayOfTheWeekBaseKitchenQueueTimeMinutes() {
		logger.finest("Entered");
		int dayOfTheWeek = Util.getDayOfWeek(System.currentTimeMillis());
		return getWeeklyBaseKitchenQueueTimeMinutes().get(dayOfTheWeek - 1); // -1 to adjust dayOfTheWeek is 1 based
																				// where Calendar.SUNDAY = 1
	}

	public static List<Integer> getWeeklyBaseKitchenQueueTimeMinutes() {
		logger.finest("Entered");
		List<Integer> kitchenQueueTimeBasesInMinute = (List<Integer>) nameToConfigurationObject
				.get(ConfigKeyDefs.weeklyBaseKitchenQueueTimeMinutes);
		if (kitchenQueueTimeBasesInMinute == null) {
			kitchenQueueTimeBasesInMinute = Arrays.asList(10, 10, 10, 10, 10, 15, 15);
		}
		return kitchenQueueTimeBasesInMinute;
	}

	private static List<Double> kitchenQueueTimeFactors;

	public static List<Double> getKitchenQueueTimeFactors() {
		logger.finest("Entered");
		if (kitchenQueueTimeFactors == null) {
			kitchenQueueTimeFactors = (List<Double>) nameToConfigurationObject
					.get(ConfigKeyDefs.kitchenQueueTimeFactors);
			if (kitchenQueueTimeFactors == null) {
				kitchenQueueTimeFactors = List.of(2.0, 4.0, 6.0, 8.0);
			}
		}
		if (kitchenQueueTimeFactors.size() != ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE) {
			throw new RuntimeException("kitchenQueueTimeFactors must be " + ConfigKeyDefs.KITCHEN_QUEUE_TIME_FACTOR_SIZE
					+ ". It size is " + kitchenQueueTimeFactors.size());
		}

		return kitchenQueueTimeFactors;
	}

	private static Boolean roundToNickel;

	public static Boolean getRoundToNickel() {
		logger.finest("Entered");
		if (roundToNickel == null) {
			roundToNickel = (Boolean) nameToConfigurationObject.get(ConfigKeyDefs.roundToNickel);
			if (roundToNickel == null) {
				roundToNickel = true;
			}
		}
		return roundToNickel;
	}

	private static Boolean printOrderNumberOnOrder;

	public static Boolean getPrintOrderNumberOnOrder() {
		logger.finest("Entered");
		if (printOrderNumberOnOrder == null) {
			printOrderNumberOnOrder = (Boolean) nameToConfigurationObject.get(ConfigKeyDefs.printOrderNumberOnOrder);
			if (printOrderNumberOnOrder == null) {
				printOrderNumberOnOrder = true;
			}
		}
		return printOrderNumberOnOrder;
	}

	private static Boolean printCheckNumberOnOrder;

	public static Boolean getPrintCheckNumberOnOrder() {
		logger.finest("Entered");
		if (printCheckNumberOnOrder == null) {
			printCheckNumberOnOrder = (Boolean) nameToConfigurationObject.get(ConfigKeyDefs.printCheckNumberOnOrder);
			if (printCheckNumberOnOrder == null) {
				printCheckNumberOnOrder = true;
			}
		}
		return printCheckNumberOnOrder;
	}

	private static Boolean printOrderNumberOnReceipt;

	public static Boolean getPrintOrderNumberOnReceipt() {
		logger.finest("Entered");
		if (printOrderNumberOnReceipt == null) {
			printOrderNumberOnReceipt = (Boolean) nameToConfigurationObject
					.get(ConfigKeyDefs.printOrderNumberOnReceipt);
			if (printOrderNumberOnReceipt == null) {
				printOrderNumberOnReceipt = false;
			}
		}
		return printOrderNumberOnReceipt;
	}

	private static Integer chineseCharacterSpacing;

	public static Integer getChineseCharacterSpacing() {
		logger.finest("Entered");
		if (chineseCharacterSpacing == null) {
			Map<?, ?> spacingNameToValue = (Map<?, ?>) nameToConfigurationObject.get(ConfigKeyDefs.characterSpacings);
			List<Integer> options = (List<Integer>) spacingNameToValue.get(ConfigKeyDefs.characterSpacings_options);
			chineseCharacterSpacing = (Integer) spacingNameToValue.get(ConfigKeyDefs.characterSpacings_forChinese);
			if (chineseCharacterSpacing == null || options.contains(chineseCharacterSpacing) == false) {
				if (options.contains(chineseCharacterSpacing) == false) {
					logger.warning("chineseCharacterSpacing invalid option " + chineseCharacterSpacing
							+ " specified. Options are: " + options);
				}
				chineseCharacterSpacing = 4;
			}
		}

		return chineseCharacterSpacing;
	}

	private static Integer englishCharacterSpacing;

	public static Integer getEnglishCharacterSpacing() {
		logger.finest("Entered");
		if (englishCharacterSpacing == null) {
			Map<?, ?> spacingNameToValue = (Map<?, ?>) nameToConfigurationObject.get(ConfigKeyDefs.characterSpacings);
			List<Integer> options = (List<Integer>) spacingNameToValue.get(ConfigKeyDefs.characterSpacings_options);
			englishCharacterSpacing = (Integer) spacingNameToValue.get(ConfigKeyDefs.characterSpacings_forEnglish);
			if (englishCharacterSpacing == null || options.contains(englishCharacterSpacing) == false) {
				if (options.contains(englishCharacterSpacing) == false) {
					logger.warning("englishCharacterSpacing option value " + englishCharacterSpacing
							+ " specified. Options are: " + options);
				}
				englishCharacterSpacing = 2;
			}
		}

		return englishCharacterSpacing;
	}

	private static Integer orderLineSpacing;

	public static Integer getOrderLineSpacing() {
		logger.finest("Entered");
		if (orderLineSpacing == null) {
			Map<?, ?> spacingNameToValue = (Map<?, ?>) nameToConfigurationObject.get(ConfigKeyDefs.lineSpacings);
			List<Integer> options = (List<Integer>) spacingNameToValue.get(ConfigKeyDefs.lineSpacings_options);
			orderLineSpacing = (Integer) spacingNameToValue.get(ConfigKeyDefs.lineSpacings_forOrder);
			if (orderLineSpacing == null || options.contains(orderLineSpacing) == false) {
				if (options.contains(orderLineSpacing) == false) {
					logger.warning("orderLineSpacing option value " + orderLineSpacing + " specified. Options are: "
							+ options);
				}
				orderLineSpacing = 2;
			}
		}

		return orderLineSpacing;
	}

	private static Integer receiptLineSpacing;

	public static Integer getReceiptLineSpacing() {
		logger.finest("Entered");
		if (receiptLineSpacing == null) {
			Map<?, ?> spacingNameToValue = (Map<?, ?>) nameToConfigurationObject.get(ConfigKeyDefs.lineSpacings);
			List<Integer> options = (List<Integer>) spacingNameToValue.get(ConfigKeyDefs.lineSpacings_options);
			receiptLineSpacing = (Integer) spacingNameToValue.get(ConfigKeyDefs.lineSpacings_forReceipt);
			if (receiptLineSpacing == null || options.contains(receiptLineSpacing) == false) {
				if (options.contains(receiptLineSpacing) == false) {
					logger.warning("receiptLineSpacing option value " + receiptLineSpacing + " specified. Options are: "
							+ options);
				}
				receiptLineSpacing = 1;
			}
		}

		return receiptLineSpacing;
	}

	private static String localClientWindowResolution;

	public static String getLocalClientWindowResolution() {
		logger.finest("Entered");
		if (localClientWindowResolution == null) {
			Map<?, ?> guiWindowResolutionNameToValue = (Map<?, ?>) nameToConfigurationObject
					.get(ConfigKeyDefs.clientWindowResolutions);
			List<String> options = (List<String>) guiWindowResolutionNameToValue
					.get(ConfigKeyDefs.clientWindowResolutions_options);
			localClientWindowResolution = (String) guiWindowResolutionNameToValue
					.get(ConfigKeyDefs.clientWindowResolutions_forLocalClient);
			if (localClientWindowResolution == null || options.contains(localClientWindowResolution) == false) {
				if (options.contains(localClientWindowResolution) == false) {
					logger.warning("localClientWindowResolution option value " + localClientWindowResolution
							+ " specified. Options are: " + options);
				}

				localClientWindowResolution = "XVGA";
			}
		}
		return localClientWindowResolution;
	}

	private static List<String> roleOptions;

	public static List<String> getRoleOptions() {
		logger.finest("Entered");
		if (roleOptions == null) {
			roleOptions = (List<String>) nameToConfigurationObject.get(ConfigKeyDefs.roleOptions);
			if (roleOptions == null) {
				// not in conf.yaml, provide default
				roleOptions = List.of("owner", "manager", "cashier", "server", "driver", "packer", "busser");
			}
		}
		return roleOptions;
	}

	private static List<String> permissionRequiredActionOptions;

	public static List<String> getPermissionRequiredActionOptions() {
		logger.finest("Entered");
		if (permissionRequiredActionOptions == null) {
			permissionRequiredActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.permissionRequiredActionOptions);
			if (permissionRequiredActionOptions == null) {
				// not in conf.yaml, provide default
				permissionRequiredActionOptions = List.of("close", "shutdownServer", "printReport", "printSummary",
						"printDetail", "voidOrder", "voidOrderItem", "voidCustomizer", "voidExchanger");
			}
		}
		return permissionRequiredActionOptions;
	}

	private static void validateActionPermission(List<Map<String, List<String>>> actionPermission) {
		logger.finest("Entered");
		if (actionPermission == null) {
			return;
		}

		for (Map<String, List<String>> actions : actionPermission) {
			for (String actionKey : actions.keySet()) {
				List<String> permissionRequiredActionOptions = getPermissionRequiredActionOptions();
				if (permissionRequiredActionOptions.contains(actionKey) == false) {
					throw new RuntimeException("Invalid permissionRequiredAction specified: '" + actionKey
							+ "'. If not a typo, add it to permissionRequiredActionOptions and develop and handler.");
				}

				List<String> roleOptions = getRoleOptions();
				for (String role : actions.get(actionKey)) {
					if (roleOptions.contains(role) == false) {
						throw new RuntimeException("Invalid role specified: '" + role
								+ "'. If not a typo, add it to roleOptions check if there programming logic check it.");
					}
				}
			}
		}
	}

	private static List<Map<String, List<String>>> actionPermission;

	public static List<Map<String, List<String>>> getActionPermission() {
		logger.finest("Entered");
		if (actionPermission == null) {
			actionPermission = (List<Map<String, List<String>>>) nameToConfigurationObject
					.get(ConfigKeyDefs.actionPermission);
			validateActionPermission(actionPermission);
			if (actionPermission == null) {
				// not in conf.yaml, provide default
				// @formatter:off
				actionPermission = List.of(
						Map.of("close",
								List.of("manager"), "shutdownServer",
								List.of("manager"),	"printReport",
								List.of("owner", "manager"), "printSummary",
								List.of("owner", "manager"),"printDetail",
								List.of("owner", "manager"), "voidOrder",
								List.of("owner", "manager", "cashier", "server"), "voidOrderItem",
								List.of("owner", "manager", "cashier", "server"), "voidCustomizer",
								List.of("owner", "manager", "cashier", "server"), "voidExchanger",
								List.of("owner", "manager", "cashier", "server")));
				// @formatter:off
			}
		}
		return actionPermission;
	}

	private static List<String> nonPrintingActionOptions;

	public static List<String> getNonPrintingActionOptions() {
		logger.finest("Entered");
		if (nonPrintingActionOptions == null) {
			nonPrintingActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.nonPrintingActionOptions);
			if (nonPrintingActionOptions == null) {
				// not in conf.yaml, provide default
				nonPrintingActionOptions = List.of(ConfigKeyDefs.noAction, ConfigKeyDefs.showMap,
						ConfigKeyDefs.showHelp, ConfigKeyDefs.toggleDishSize, ConfigKeyDefs.clearOrder,
						ConfigKeyDefs.commitOrder, ConfigKeyDefs.refreshUnpaidOrders,
						ConfigKeyDefs.refreshUnsettledOrders, ConfigKeyDefs.prepareForNextOrder);
			}
		}
		return nonPrintingActionOptions;
	}

	private static List<String> printWaitingNoticeActionOptions;

	public static List<String> getPrintWaitingNoticeActionOptions() {
		logger.finest("Entered");
		if (printWaitingNoticeActionOptions == null) {
			printWaitingNoticeActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printWaitingNoticeActionOptions);
			if (printWaitingNoticeActionOptions == null) {
				// not in conf.yaml, provide default
				printWaitingNoticeActionOptions = List.of("printWaitingNotice");
			}
		}
		return printWaitingNoticeActionOptions;
	}

	private static List<String> printCouponActionOptions;

	public static List<String> getPrintCouponActionOptions() {
		logger.finest("Entered");
		if (printCouponActionOptions == null) {
			printCouponActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printCouponActionOptions);
			if (printCouponActionOptions == null) {
				// not in conf.yaml, provide default
				printCouponActionOptions = List.of("printCoupon1", "printCoupon2", "printCoupon3");
			}
		}
		return printCouponActionOptions;
	}

	private static List<String> printToGoPerStationActionOptions;

	public static List<String> getPrintToGoPerStationActionOptions() {
		logger.finest("Entered");
		if (printToGoPerStationActionOptions == null) {
			printToGoPerStationActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printToGoPerStationActionOptions);
			if (printToGoPerStationActionOptions == null) {
				// not in conf.yaml, provide default
				printToGoPerStationActionOptions = List.of("printToGoPerStation", "printToGoPerStationInChinese",
						"printToGoPerStationInEnglishAndChinese", "printToGoPerStationInEnglishAndChineseInTwoLines");
			}
		}
		return printToGoPerStationActionOptions;
	}

	private static List<String> printDineInPerStationActionOptions;

	public static List<String> getPrintDineInPerStationActionOptions() {
		logger.finest("Entered");
		if (printDineInPerStationActionOptions == null) {
			printDineInPerStationActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printDineInPerStationActionOptions);
			if (printDineInPerStationActionOptions == null) {
				// not in conf.yaml, provide default
				printDineInPerStationActionOptions = List.of("printDineInPerStation", "printDineInPerStationInChinese",
						"printDineInPerStationInEnglishAndChinese",
						"printDineInPerStationInEnglishAndChineseInTwoLines");
			}
		}
		return printDineInPerStationActionOptions;
	}

	private static void validatePerStationsActionOptions(
			Map<String, Map<String, String>> printToGoPerStationActionOptions) {
		logger.finest("Entered");
		if (printToGoPerStationActionOptions == null) {
			return;
		}

		for (String printerName : printToGoPerStationActionOptions.keySet()) {
			Map<String, String> attributes = printToGoPerStationActionOptions.get(printerName);
			for (String attribute : attributes.keySet()) {
				if (attribute.equals(ConfigKeyDefs.printerLocationToPrinter_dishWorkstationId)) {
					Object dishWorkstationIdObject = attributes.get(attribute);
					if (dishWorkstationIdObject instanceof Integer) {
						int dishWorkstationId = (Integer) dishWorkstationIdObject;
						if (dishWorkstationId <= 0) {
							throw new RuntimeException("Invalid dishWorkstationId: " + dishWorkstationId
									+ ". It must be a positive integer.");
						}
					} else {
						throw new RuntimeException("Invalid dishWorkstationId: " + dishWorkstationIdObject
								+ ". It must be a positive integer without quote around it in the yaml file.");
					}
				}
			}
		}
	}

	private static List<String> printOrderActionOptions;

	public static List<String> getPrintOrderActionOptions() {
		logger.finest("Entered");
		if (printOrderActionOptions == null) {
			printOrderActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printOrderActionOptions);
			if (printOrderActionOptions == null) {
				// not in conf.yaml, provide default
				printOrderActionOptions = List.of("printOrder", "printOrderInEnglish", "printOrderInChinese",
						"printOrderInEnglishAndChinese", "printOrderInEnglishAndChineseInTwoLines");
			}
		}
		return printOrderActionOptions;
	}

	private static List<String> printOrderNoPriceActionOptions;

	public static List<String> getPrintOrderNoPriceActionOptions() {
		logger.finest("Entered");
		if (printOrderNoPriceActionOptions == null) {
			printOrderNoPriceActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printOrderNoPriceActionOptions);
			if (printOrderNoPriceActionOptions == null) {
				// not in conf.yaml, provide default
				printOrderNoPriceActionOptions = List.of("printOrderNoPrice", "printOrderNoPriceInEnglish",
						"printOrderNoPriceInChinese", "printOrderNoPriceInEnglishAndChinese",
						"printOrderNoPriceInEnglishAndChineseInTwoLines");
			}
		}
		return printOrderNoPriceActionOptions;
	}

	private static List<String> printReceiptActionOptions;

	public static List<String> getPrintReceiptActionOptions() {
		logger.finest("Entered");
		if (printReceiptActionOptions == null) {
			printReceiptActionOptions = (List<String>) nameToConfigurationObject
					.get(ConfigKeyDefs.printReceiptActionOptions);
			if (printReceiptActionOptions == null) {
				// not in conf.yaml, provide default
				printReceiptActionOptions = List.of("printReceipt", "printReceiptInEnglish", "printReceiptInChinese",
						"printReceiptInEnglishAndChinese", "printReceiptInEnglishAndChineseInTwoLines");
			}
		}
		return printReceiptActionOptions;
	}

	private static Map<String, Map<String, String>> toGoPrinterLocationToPrinter;

	public static Map<String, Map<String, String>> getToGoPrinterLocationToPrinter() {
		logger.finest("Entered");
		if (toGoPrinterLocationToPrinter == null) {
			toGoPrinterLocationToPrinter = (Map<String, Map<String, String>>) nameToConfigurationObject
					.get(ConfigKeyDefs.toGoPrinterLocationToPrinter);
			validatePerStationsActionOptions(toGoPrinterLocationToPrinter);
		}
		return toGoPrinterLocationToPrinter;
	}

	private static Map<Integer, String> toGoDishWorkstationIdToPrinterLocation;

	public static Map<Integer, String> getToGoDishWorkstationIdToPrinterLocation() {
		logger.finest("Entered");
		if (toGoDishWorkstationIdToPrinterLocation == null) {
			toGoDishWorkstationIdToPrinterLocation = new HashMap<Integer, String>();
			Map<String, Map<String, String>> tmpToGoPrinterLocationToPrinter = getToGoPrinterLocationToPrinter();
			for (String printerLocation : tmpToGoPrinterLocationToPrinter.keySet()) {
				Map<String, String> printerAttributes = tmpToGoPrinterLocationToPrinter.get(printerLocation);
				Object dishWorkstataionIdObject = printerAttributes
						.get(ConfigKeyDefs.printerLocationToPrinter_dishWorkstationId);
				toGoDishWorkstationIdToPrinterLocation.put((Integer) dishWorkstataionIdObject, printerLocation);
			}
		}
		return toGoDishWorkstationIdToPrinterLocation;
	}

	private static Map<String, Map<String, String>> dineInPrinterLocationToPrinter;

	public static Map<String, Map<String, String>> getDineInPrinterLocationToPrinter() {
		logger.finest("Entered");
		if (dineInPrinterLocationToPrinter == null) {
			dineInPrinterLocationToPrinter = (Map<String, Map<String, String>>) nameToConfigurationObject
					.get(ConfigKeyDefs.dineInPrinterLocationToPrinter);
			validatePerStationsActionOptions(dineInPrinterLocationToPrinter);
		}
		return dineInPrinterLocationToPrinter;
	}

	private static Map<Integer, String> dineInDishWorkstationIdToPrinterLocation;

	public static Map<Integer, String> getDineInDishWorkstationIdToPrinterLocation() {
		logger.finest("Entered");
		if (dineInDishWorkstationIdToPrinterLocation == null) {
			dineInDishWorkstationIdToPrinterLocation = new HashMap<Integer, String>();
			Map<String, Map<String, String>> tmpDineInPrinterLocationToPrinter = getDineInPrinterLocationToPrinter();
			for (String printerLocation : tmpDineInPrinterLocationToPrinter.keySet()) {
				Map<String, String> printerAttributes = tmpDineInPrinterLocationToPrinter.get(printerLocation);
				Object dishWorkstataionIdObject = printerAttributes
						.get(ConfigKeyDefs.printerLocationToPrinter_dishWorkstationId);
				dineInDishWorkstationIdToPrinterLocation.put((Integer) dishWorkstataionIdObject, printerLocation);
			}
		}
		return dineInDishWorkstationIdToPrinterLocation;
	}

	private static Map<String, Integer> dishCategoryNameToIntegerValue;

	public static Map<String, Integer> getDishCategoryNameToIntegerValue() {
		//logger.finest("Entered");
		if (dishCategoryNameToIntegerValue == null) {
			dishCategoryNameToIntegerValue = (Map<String, Integer>) nameToConfigurationObject
					.get(ConfigKeyDefs.dishCategories);
			if (dishCategoryNameToIntegerValue == null) {
				// not in conf.yaml, provide default
				// due to some unknown limitation on Map.of <=10 elements, only the followings
				// are defined by default
				// @formatter:off
				dishCategoryNameToIntegerValue = Map.of(
						"regularDishStart", 100,
						"regularDishEnd", 900,
						"couponDish", 910,
						"condiment", 930,
						"customDish", 950,
						"softDrink", 975,
						"alcoholDrink", 980,
						"allNoIngredient", 995,
						"exchangeCondiment", 996,
						"customizer", 1000
						);
				// @formatter:off
			}
		}
		return dishCategoryNameToIntegerValue;
	}

	private static List<String> fontSizeOptions;

	public static List<String> getFontSizeOptions() {
		logger.finest("Entered");
		if (fontSizeOptions == null) {
			fontSizeOptions = (List<String>) nameToConfigurationObject.get(ConfigKeyDefs.fontSizeOptions);
			if (fontSizeOptions == null) {
				// not in conf.yaml, provide default
				fontSizeOptions = List.of("small", "medium", "large");
			}
		}
		return fontSizeOptions;
	}

	private static List<String> getAllActionOptions() {
		logger.finest("Entered");
		List<String> allActionOptions = new ArrayList<>();
		allActionOptions.addAll(getNonPrintingActionOptions());
		allActionOptions.addAll(getPrintWaitingNoticeActionOptions());
		allActionOptions.addAll(getPrintCouponActionOptions());
		allActionOptions.addAll(getPermissionRequiredActionOptions());
		allActionOptions.addAll(getPrintToGoPerStationActionOptions());
		allActionOptions.addAll(getPrintDineInPerStationActionOptions());
		allActionOptions.addAll(getPrintOrderActionOptions());
		allActionOptions.addAll(getPrintOrderNoPriceActionOptions());
		allActionOptions.addAll(getPrintReceiptActionOptions());

		return allActionOptions;
	}

	private static Map<String, List<Map<String, String>>> configuredEventToActionAttributes;

	private static void validateEvents(Map<String, List<Map<String, String>>> eventToFunctionKey) {
		logger.finest("Entered");
		if (eventToFunctionKey == null) {
			return;
		}

		for (String event : eventToFunctionKey.keySet()) {
			List<Map<String, String>> actionList = eventToFunctionKey.get(event);
			for (Map<String, String> actionMap : actionList) {
				for (String actionMapKey : actionMap.keySet()) {
					switch (actionMapKey) {
					case ConfigKeyDefs.configuredEventToActionAttributes_action:
						String action = actionMap.get(actionMapKey);
						if (getAllActionOptions().contains(action) == false) {
							throw new RuntimeException("Invalid action option: " + action);
						}
						break;
					case ConfigKeyDefs.configuredEventToActionAttributes_fontSize:
						String fontSize = actionMap.get(actionMapKey);
						if (getFontSizeOptions().contains(fontSize) == false) {
							throw new RuntimeException("Invalid fontSize option: " + fontSize);
						}
						break;
					case ConfigKeyDefs.configuredEventToActionAttributes_location:
						String location = actionMap.get(actionMapKey);
						if (getToGoPrinterLocationToPrinter().keySet().contains(location) == false
								&& getDineInPrinterLocationToPrinter().keySet().contains(location) == false) {
							throw new RuntimeException("Invalid printer location option: " + location);
						}
						break;
					default:
						throw new RuntimeException("Unsupport actionMapKey: " + actionMapKey);
					}
				}
			}
		}
	}

	public static Map<String, List<Map<String, String>>> getConfiguredEventToActionAttributes() {
		logger.finest("Entered");
		if (configuredEventToActionAttributes == null) {
			configuredEventToActionAttributes = (Map<String, List<Map<String, String>>>) nameToConfigurationObject
					.get(ConfigKeyDefs.configuredEventToActionAttributes);
			validateEvents(configuredEventToActionAttributes);
			if (configuredEventToActionAttributes == null) {
				// not in conf.yaml, provide default
				// Note: there is a limit of 10 on using Map.of(). see
				// http://openjdk.java.net/jeps/269
				// For more than 10, use Map.Entries(
				// @formatter:off
				// Eclipse is really have a hard time keep parsing and building this static
				// initialization of this complicated Map. So comment it out for now and have
				// it.
				// The worse case is make this a required configuration field in config.yaml and
				// no default provided.
				/*
				configuredEventToActionAttributes = Map.ofEntries(
				 entry("f1", List.of(Map.of("action", "showMap")))
				,entry("f2", List.of(Map.of("action", "clearOrder"), Map.of("action", "refreshOrder")))
				,entry("f3", List.of(Map.of("action", "toggleDishSize")))
				,entry("f4", List.of(Map.of("action", "commitOrder")))
				,entry("f5", List.of(Map.of("action", "refreshAllOrders")))
				,entry("f6", List.of(Map.of("action", "printWaitingNotice", "fontSize","large", "location","packingStation")))
				,entry("f7", List.of(Map.of("action", "printReceipt", "fontSize","small", "location","defaultStation")))
				,entry("f8", List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","large", "location","defaultStation")))
				,entry("f9", List.of(Map.of("action", "printDrinksInEnglishAndChinese", "fontSize","large", "location","cashierStation")))
				,entry("f10",List.of(Map.of("action", "printAppetizersInChinese", "fontSize","xlarge", "location","appetizerStation")))
				,entry("f11",List.of(Map.of("action", "printEntreesInChinese", "fontSize","xlarge", "location","entreeStation")))
				,entry("f12",List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","xlarge", "location","packingStation") ,Map.of("action", "printAppetizersInEnglishAndChinese", "fontSize","xlarge", "location","appetizerStation") ,Map.of("action", "printOrderNoPriceInEnglishAndChinese","fontSize","xlarge","location","entreeStation")))
				,entry("altF1", List.of(Map.of("action","showMap")))
				,entry("altF2", List.of(Map.of("action", "noAction"), Map.of("action", "refreshOrder")))
				,entry("altF3", List.of(Map.of("action", "noAction")))
				,entry("altF4", List.of(Map.of("action", "noAction")))
				,entry("altF5", List.of(Map.of("action", "noAction")))
				,entry("altF6", List.of(Map.of("action", "noAction")))
				,entry("altF7", List.of(Map.of("action", "printReceiptInEnglishAndChinese",	"fontSize","large", "location","defaultStation")))
				,entry("altF8",	List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","large", "location","defaultStation")))
				,entry("altF9", List.of(Map.of("action", "printDrinksInEnglishAndChinese", "fontSize","large", "location","cashierStation")))
				,entry("altF10", List.of(Map.of("action","printAppetizersInChinese", "fontSize","xlarge", "location","appetizerStation")))
				,entry("altF11", List.of(Map.of("action","printEntreesInChinese", "fontSize","xlarge", "location","entreeStation")))
				,entry("altF12", List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","xlarge", "location","packingStation") ,Map.of("action", "printAppetizersInEnglishAndChinese", "fontSize","xlarge", "location","appetizerStation") ,Map.of("action", "printOrderNoPriceInEnglishAndChinese", "fontSize","xlarge", "location","entreeStation")))
				,entry("shiftF1", List.of(Map.of("action", "noAction")))
				,entry("shiftF2", List.of(Map.of("action", "noAction")))
				,entry("shiftF3", List.of(Map.of("action", "noAction")))
				,entry("shiftF4", List.of(Map.of("action", "noAction")))
				,entry("shiftF5", List.of(Map.of("action", "noAction")))
				,entry("shiftF6", List.of(Map.of("action", "noAction")))
				,entry("shiftF7", List.of(Map.of("action", "printReceiptInChinese", "fontSize","large", "location","defaultStation")))
				,entry("shiftF8", List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","xlarge", "location","defaultStation")))
				,entry("shiftF9", List.of(Map.of("action", "printDrinksInEnglishAndChinese", "fontSize","xlarge", "location","cashierStation")))
				,entry("shiftF10", List.of(Map.of("action", "printAppetizersInChinese", "fontSize","xlarge", "location","appetizerStation")))
				,entry("shiftF11", List.of(Map.of("action", "printEntreesInChinese", "fontSize","xlarge", "location","entreeStation")))
				,entry("shiftF12", List.of(Map.of("action", "printOrder", "fontSize","xlarge", "location","packingStation")))
				,entry("paidPrintOrderToDefault", List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","xlarge", "location","defaultStation"))) ,entry("paidPrintOrderToKitchen", List.of(Map.of("action", "printOrderInEnglishAndChinese", "fontSize","xlarge", "location","packingStation") ,Map.of("action", "printAppetizersInEnglishAndChinese", "fontSize","large", "location","appetizerStation") ,Map.of("action", "printOrderNoPriceInEnglishAndChinese","fontSize","large", "location","entreeStation"))) ,entry("paidPrinterNoticeToLocal", List.of(Map.of("action", "printWaitingNotice", "fontSize","large", "location","defaultStation"))) ,entry("paidPrinterNoticeToPacking", List.of(Map.of("action", "printWaitingNotice", "fontSize","large", "location","packingStation"))) );
				*/
				// @formatter:off
			}
		}
		return configuredEventToActionAttributes;
	}

	private static void validateCoupon(Map<String, Object> coupon) {
		logger.finest("Entered");
		if (coupon == null) {
			return;
		}

		Boolean print = (Boolean) coupon.get(ConfigKeyDefs.coupon_print);
		if (print == null) {
			throw new RuntimeException("Coupon must have a '" + ConfigKeyDefs.coupon_print + ": yes' defined");
		}

		Integer expiredInDays = (Integer) coupon.get(ConfigKeyDefs.coupon_expiredInDays);
		if (expiredInDays == null) {
			throw new RuntimeException("Coupon must have an '" + ConfigKeyDefs.coupon_expiredInDays + ": 45' defined");
		}

		List<String> content = (List<String>) coupon.get(ConfigKeyDefs.coupon_content);
		if (content == null) {
			throw new RuntimeException(
					"Coupon must have an '" + ConfigKeyDefs.coupon_content + ": [<list of string>]' defined");
		}

		for (String line : content) {
			if (line.contains(ConfigKeyDefs.coupon___ctrl__)) {
				if (line.contains(ConfigKeyDefs.coupon_content_separator) == false) {
					throw new RuntimeException(ConfigKeyDefs.coupon___ctrl__ + " must contain "
							+ ConfigKeyDefs.coupon_content_separator + ". Invalid line: " + line);
				}
				String[] controlParts = line.split(ConfigKeyDefs.coupon_content_separator);
				if (controlParts.length <= 1) {
					throw new RuntimeException(ConfigKeyDefs.coupon___ctrl__ + " must contain a value from the "
							+ ConfigKeyDefs.couponControlOptions + ". Invalid line: " + line);
				} else {
					String controlKeyword = controlParts[1].trim();
					if (getCouponControlOptions().contains(controlKeyword) == false) {
						throw new RuntimeException(ConfigKeyDefs.coupon___ctrl__ + " must contain a value from the "
								+ ConfigKeyDefs.couponControlOptions + ". Invalid line: " + line);
					}
				}
			}

			if (line.contains(ConfigKeyDefs.coupon___expiration__)) {
				if (line.contains(ConfigKeyDefs.coupon_content_separator) == false) {
					throw new RuntimeException(ConfigKeyDefs.coupon___expiration__ + " must contain "
							+ ConfigKeyDefs.coupon_content_separator + ". Invalid line: " + line);
				}
			}
		}
	}

	private static List<String> couponHeader;

	public static List<String> getCouponHeader() {
		logger.finest("Entered");
		if (couponHeader == null) {
			couponHeader = (List<String>) nameToConfigurationObject.get(ConfigKeyDefs.couponHeader);
			if (couponHeader == null) {
				// not in conf.yaml, provide default
				couponHeader = List.of("__ctrl__: font12x24_W2H2", "www.chinabistro.com", "(847) 854-6823",
						"__ctrl__: font12x24_W2H2", "__ctrl__: whiteBlackReverseOn", "    COUPONS!    ",
						"__ctrl__: whiteBlackReverseOff", "__ctrl__: font09x17_W1H1", "__expiration__: Exp.");
			}
		}
		return couponHeader;
	}

	private static Map<String, Object> getCoupon(CouponVariantType couponVariantType) {
		logger.finest("Entered");
		String couponKey;
		switch (couponVariantType) {
		case COUPON1:
			couponKey = ConfigKeyDefs.coupon1;
			break;
		case COUPON2:
			couponKey = ConfigKeyDefs.coupon2;
			break;
		case COUPON3:
			couponKey = ConfigKeyDefs.coupon3;
			break;
		default:
			throw new RuntimeException("Invalid couponVariantType=" + couponVariantType);
		}

		// There is no default for coupon1 content. caller needs to check for null in
		// case coupon1 is not configured in conf.yaml
		Map<String, Object> coupon = (Map<String, Object>) nameToConfigurationObject.get(couponKey);
		if (coupon != null) {
			validateCoupon(coupon);
		}

		return coupon;
	}

	public static boolean getCouponPrint(CouponVariantType couponVariantType) {
		logger.finest("Entered");
		Map<String, Object> coupon = getCoupon(couponVariantType);
		if (coupon == null) {
			return false;
		}

		return (Boolean) (coupon.get(ConfigKeyDefs.coupon_print));
	}

	public static int getCouponExpiredInDays(CouponVariantType couponVariantType) {
		logger.finest("Entered");
		Map<String, Object> coupon = getCoupon(couponVariantType);
		if (coupon == null) {
			return -1;
		}
		return (Integer) coupon.get(ConfigKeyDefs.coupon_expiredInDays);
	}

	public static List<String> getCouponContent(CouponVariantType couponVariantType) {
		logger.finest("Entered");
		Map<String, Object> coupon = getCoupon(couponVariantType);
		if (coupon == null) {
			return null;
		}
		return (List<String>) coupon.get(ConfigKeyDefs.coupon_content);
	}

	private static List<String> couponControlOptions;

	public static List<String> getCouponControlOptions() {
		logger.finest("Entered");
		if (couponControlOptions == null) {
			couponControlOptions = (List<String>) nameToConfigurationObject.get(ConfigKeyDefs.couponControlOptions);
			if (couponControlOptions == null) {
				// not in conf.yaml, provide default
				couponControlOptions = List.of("font12x24_W1H1", "font12x24_W1H2", "font12x24_W2H1", "font12x24_W2H2",
						"font09x17_W1H1", "font09x17_W2H2", "whiteBlackReverseOff", "whiteBlackReverseOn", "alignLeft",
						"alignCenter", "alignRight");
			}
		}
		return couponControlOptions;
	}

	private static List<String> deliveryCities;

	public static List<String> getDeliveryCities() {
		logger.finest("Entered");
		if (deliveryCities == null) {
			deliveryCities = new ArrayList<String>();
			for (String city : (List<String>) nameToAddressObject.get(ConfigKeyDefs.deliveryCities)) {
				if (deliveryCities.contains(city) == false) {
					deliveryCities.add(city);
				} else {
					throw new RuntimeException("Detected duplicate city=" + city);
				}
			}
		}

		return deliveryCities;
	}

	private static List<String> deliveryStates;

	public static List<String> getDeliveryStates() {
		logger.finest("Entered");
		if (deliveryStates == null) {
			deliveryStates = new ArrayList<String>();
			for (String state : (List<String>) nameToAddressObject.get(ConfigKeyDefs.deliveryStates)) {
				if (deliveryStates.contains(state) == false) {
					deliveryStates.add(state);
				} else {
					throw new RuntimeException("Detected duplicate state=" + state);
				}
			}
		}

		return deliveryStates;
	}

	private static List<String> deliveryZips;

	public static List<String> getDeliveryZips() {
		logger.finest("Entered");
		if (deliveryZips == null) {
			deliveryZips = new ArrayList<String>();
			List<Map<String, List<String>>> deliveryStreetPerZips = (List<Map<String, List<String>>>) nameToAddressObject
					.get(ConfigKeyDefs.deliveryStreetsPerZip);
			for (Map<String, List<String>> zips : deliveryStreetPerZips) {
				for (String zip : zips.keySet()) {
					if (deliveryZips.contains(zip) == false) {
						deliveryZips.add(zip);
					} else {
						throw new RuntimeException("Detected duplicate zip code=" + zip);
					}
				}
			}
		}

		return deliveryZips;
	}

	private static Map<String, List<String>> deliveryZipToStreets;

	public static List<String> getDeliveryStreets(String zip) {
		logger.finest("Entered");
		if (deliveryZipToStreets == null) {
			deliveryZipToStreets = new HashMap<String, List<String>>();
			List<Map<String, List<String>>> deliveryStreetPerZips = (List<Map<String, List<String>>>) nameToAddressObject
					.get(ConfigKeyDefs.deliveryStreetsPerZip);
			for (Map<String, List<String>> zips : deliveryStreetPerZips) {
				for (String zipKey : zips.keySet()) {
					deliveryZipToStreets.put(zipKey, zips.get(zipKey));
				}
			}
		}

		return deliveryZipToStreets.get(zip);
	}

	private static Map<String, Street> deliveryStreets;

	public static Map<String, Street> getDeliveryStreets() {
		logger.finest("Entered");
		if (deliveryStreets == null) {
			deliveryStreets = new TreeMap<String, Street>();
			List<Map<String, Object>> deliveryStreetsPerZips = (List<Map<String, Object>>) nameToAddressObject
					.get(ConfigKeyDefs.deliveryStreetsPerZip);
			for (Map<String, Object> streetsPerZip : deliveryStreetsPerZips) {
				for (String zipCode : streetsPerZip.keySet()) {
					// System.out.println("zipCode=" + zipCode);
					List<Object> streetObjects = (List<Object>) streetsPerZip.get(zipCode);
					// System.out.println("streetObject=" + streetObjects);
					for (Object streetObject : streetObjects) {
						if (streetObject instanceof Map<?, ?>) {
							Map<String, List<Integer>> streetMap = (Map<String, List<Integer>>) streetObject;
							String streetName = streetMap.keySet().stream().findFirst().get();
							List<Integer> houseNumberRange = streetMap.get(streetName);
							if (houseNumberRange.size() == 2) {
								int houseNumberMin = houseNumberRange.get(0);
								int houseNumberMax = houseNumberRange.get(1);
								if (deliveryStreets.containsKey(streetName)) {
									Street street = deliveryStreets.get(streetName);
									if (houseNumberMin < street.getHouseNumberMin()) {
										street.setHouseNumberMin(houseNumberMin);
									}
									if (houseNumberMax > street.getHouseNumberMax()) {
										street.setHouseNumberMax(houseNumberMax);
									}
									deliveryStreets.put(streetName, street);
								} else {
									deliveryStreets.put(streetName, new Street(streetName, houseNumberMin, houseNumberMax, zipCode));
								}
							} else {
								System.out.println("Incorrect configuration for streetMap=" + streetMap);
							}
						} else if (streetObject instanceof String) {
							String streetName = (String) streetObject;
							if (deliveryStreets.containsKey(streetName) == false) {
								deliveryStreets.put(streetName, new Street(streetName, 0, 0, zipCode));
							}
						} else {
							System.out.println("Incorrect configuration for streetObject=" + streetObject);
						}
					}
				}
			}
			//Collections.sort(deliveryStreets); // sort based on street name alphabetical order
		}

		return deliveryStreets;
	}

	private static Map<String, Integer> tableNumbers;

	public static Map<String, Integer> getTableNumbers() {
		logger.finest("Entered");
		if (tableNumbers == null) {
			tableNumbers = new TreeMap<String, Integer>(); // TreeMap to provided sorted KeySet
			List<Map<String, Integer>> tmpTableNumbers = (List<Map<String, Integer>>) nameToAddressObject
					.get(ConfigKeyDefs.tableNumbers);
			for (Map<String, Integer> tableNumber : tmpTableNumbers) {
				String tableNumberKey = tableNumber.keySet().stream().findFirst().get();
				Integer tableCapacity = tableNumber.get(tableNumberKey);
				tableNumbers.put(tableNumberKey, tableCapacity);
			}
		}

		return tableNumbers;
	}

	private static Boolean printBagXofYtemplate;

	public static Boolean getprintBagXofYtemplate() {
		logger.finest("Entered");
		if (printBagXofYtemplate == null) {
			printBagXofYtemplate = (Boolean) nameToConfigurationObject
					.get(ConfigKeyDefs.printBagXofYtemplate);
			if (printBagXofYtemplate == null) {
				printBagXofYtemplate = true;
			}
		}
		return printBagXofYtemplate;
	}

	public static List<Integer> getPrintGratuityPercentages() {
		logger.finest("Entered");
		List<Integer> printGratuityPercentages = (List<Integer>) nameToConfigurationObject
				.get(ConfigKeyDefs.printGratuityPercentages);
		if (printGratuityPercentages != null) {
			int sum = 0;
			for (Integer percentage : printGratuityPercentages) {
				sum += percentage;
			}
			if (sum == 0) {
				//none of the element is > 0.
				printGratuityPercentages = null;
			}
		}
		return printGratuityPercentages;
	}

	// ======================================================

	// These files must be in the resources folder
	public static final String CONFIG_YAML_FILE = "conf/conf.yaml";
	public static final String ADDRESS_YAML_FILE = "conf/address.yaml";

	// private constructor
	private Configuration() throws IOException {
		logger.finest("Entered");
		Yaml yaml = new Yaml();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		try (InputStream input = classloader.getResourceAsStream(CONFIG_YAML_FILE);) {
			nameToConfigurationObject = (Map<String, Object>) yaml.load(input);
		}

		try (InputStream input = classloader.getResourceAsStream(ADDRESS_YAML_FILE);) {
			nameToAddressObject = (Map<String, Object>) yaml.load(input);
		}
	}

	// static block initialization for exception handling
	static {
		try {
			new Configuration();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occured in creating Configuration singleton instance.");
		}
	}

	// nameToConfigurationObject is a static object and populated at class loading
	// time in the static
	// block
	private static Map<String, Object> nameToConfigurationObject;
	private static Map<String, Object> nameToAddressObject;

	private static Map<String, Object> getNameToConfigurationObject() {
		logger.finest("Entered");
		return nameToConfigurationObject;
	}

	public static void main(String[] argc) {
		logger.finest("Entered");
		// Can do some quick testing here by calling the public static functions.
		// It is better to add a test case in ConfigurationTest.java.
		System.out.println(nameToConfigurationObject);
		System.out.println(ConfigKeyDefs.restaurantTaxRate + "="
				+ (Double) Configuration.getNameToConfigurationObject().get(ConfigKeyDefs.restaurantTaxRate));
		System.out.println("chineseCharacterSpacing=" + getChineseCharacterSpacing());

		System.out.println("restaurantChineseName=" + getRestaurantChineseName());
		System.out.println("restaurantAreaCode=" + getRestaurantAreaCode());
		System.out.println("restaurantCapabilities=" + getRestaurantCapabilities());

		System.out.println("datasourceUser=" + getDatasourceUser());

		Map<String, Map<String, String>> toGoPrinterLocationToPrinterOptions = (Map<String, Map<String, String>>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.toGoPrinterLocationToPrinter);
		System.out.println("toGoPrinterLocationToPrinterOptions=" + toGoPrinterLocationToPrinterOptions);
		System.out.println("printer cashierStation=" + toGoPrinterLocationToPrinterOptions.get("cashierStation"));

		Map<String, Integer> dishCategories = getDishCategoryNameToIntegerValue();
		System.out.println("dishCategoryNameToIntegerValue=" + dishCategories);
		System.out.println("softDrinkCategory=" + dishCategories.get(ConfigKeyDefs.softDrink));
		System.out.println("alcoholDrinkCategory=" + dishCategories.get(ConfigKeyDefs.alcoholDrink));

		System.out.println("yaml: nonPrintingActionOptions=" + (List<String>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.nonPrintingActionOptions));
		System.out.println("default: nonPrintingActionOptions=" + getNonPrintingActionOptions());

		System.out.println("yaml:    printWaitingNoticeActionOptions=" + (List<String>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.printWaitingNoticeActionOptions));
		System.out.println("default: printWaitingNoticeActionOptions=" + getPrintWaitingNoticeActionOptions());

		System.out.println("yaml:    permissionRequiredActionOptions=" + (List<String>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.permissionRequiredActionOptions));
		System.out.println("default: permissionRequiredActionOptions=" + getPermissionRequiredActionOptions());

		System.out.println("yaml:    toGoPrinterLocationToPrinter=" + (Map<String, Map<String, String>>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.toGoPrinterLocationToPrinter));
		System.out.println("default: toGoPrinterLocationToPrinter=" + getToGoPrinterLocationToPrinter());

		System.out.println("yaml:    dineInPrinterLocationToPrinter=" + (Map<String, Map<String, String>>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.dineInPrinterLocationToPrinter));
		System.out.println("default: dineInPrinterLocationToPrinter=" + getDineInPrinterLocationToPrinter());

		System.out.println("yaml:    printOrderActionOptions=" + (List<String>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.printOrderActionOptions));
		System.out.println("default: printOrderActionOptions=" + getPrintOrderActionOptions());

		System.out.println("yaml:    printOrderNoPriceActionOptions=" + (List<String>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.printOrderNoPriceActionOptions));
		System.out.println("default: printOrderNoPriceActionOptions=" + getPrintOrderNoPriceActionOptions());

		System.out.println("yaml:    printReceiptActionOptions=" + (List<String>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.printReceiptActionOptions));
		System.out.println("default: printReceiptActionOptions=" + getPrintReceiptActionOptions());

		Map<String, List<Map<String, String>>> configuredEventToActions = (Map<String, List<Map<String, String>>>) Configuration
				.getNameToConfigurationObject().get(ConfigKeyDefs.configuredEventToActionAttributes);
		System.out.println("yaml:	configuredEventToActionAttributes=" + configuredEventToActions);
		System.out.println("default:configuredEventToActionAttributes=" + getConfiguredEventToActionAttributes());

		System.out.println("yaml:    toGoDishWorkstationIdToPrinterLocation="
				+ Configuration.getToGoDishWorkstationIdToPrinterLocation());
		System.out.println("yaml:    dineInDishWorkstationIdToPrinterLocation="
				+ Configuration.getDineInDishWorkstationIdToPrinterLocation());

		List<Map<String, String>> f12 = (List<Map<String, String>>) configuredEventToActions.get("f12");
		System.out.println("f12=" + f12);
		Map<String, String> f12ActionDef_1 = f12.get(0);
		System.out.println("f12ActionDef_1=" + f12ActionDef_1);
		String f12ActionDef_1_action = f12ActionDef_1.get(ConfigKeyDefs.configuredEventToActionAttributes_action);
		System.out.println("f12ActionDef_1_action=" + f12ActionDef_1_action);

		for (int i = 1; i <= 12; ++i) {
			String functionKey = "f" + i;
			System.out.println(functionKey + ": " + configuredEventToActions.get(functionKey));
		}
		for (int i = 1; i <= 12; ++i) {
			String functionKey = "shiftF" + i;
			System.out.println(functionKey + ": " + configuredEventToActions.get(functionKey));
		}
		for (int i = 1; i <= 12; ++i) {
			String functionKey = "altF" + i;
			System.out.println(functionKey + ": " + configuredEventToActions.get(functionKey));
		}

		String functionKey = "altF12";
		List<Map<String, String>> altF12Maps = configuredEventToActions.get(functionKey);
		for (Map<String, String> altF12Map : altF12Maps) {
			for (String key : altF12Map.keySet()) {
				System.out.println("key: " + key + ", value: " + altF12Map.get(key));
			}
			System.out.println("action: " + altF12Map.get(ConfigKeyDefs.configuredEventToActionAttributes_action));
			System.out.println("fontSize: " + altF12Map.get(ConfigKeyDefs.configuredEventToActionAttributes_fontSize));
			System.out.println("location: " + altF12Map.get(ConfigKeyDefs.configuredEventToActionAttributes_location));
		}

		for (CouponVariantType couponVariantType : CouponVariantType.values()) {
			System.out.println("couponVariantType:" + couponVariantType);
			Map<String, Object> coupon = getCoupon(couponVariantType);
			System.out.println(ConfigKeyDefs.coupon_print + "=" + (Boolean) coupon.get(ConfigKeyDefs.coupon_print));
			System.out.println(ConfigKeyDefs.coupon_expiredInDays + "="
					+ (Integer) coupon.get(ConfigKeyDefs.coupon_expiredInDays));
			List<String> content = (List<String>) coupon.get(ConfigKeyDefs.coupon_content);
			System.out.println(ConfigKeyDefs.coupon_content + "=" + content);
			for (String line : content) {
				System.out.println("line: " + line);
			}
		}

		List<Map<String, List<String>>> actionPermission = getActionPermission();
		System.out.println("actionPermission" + actionPermission);
		for (Map<String, List<String>> actions : actionPermission) {
			System.out.println("actions=" + actions);
			for (String actionKey : actions.keySet()) {
				System.out.println("actionKey=" + actionKey);
				for (String role : actions.get(actionKey)) {
					System.out.println("role=" + role);
				}
			}
		}

		System.out.println("deliveryCities=" + getDeliveryCities());
		System.out.println("deliveryStates=" + getDeliveryStates());
		System.out.println("deliveryZips=" + getDeliveryZips());

		for (String zip : getDeliveryZips()) {
			System.out.println("\nzip=" + zip);
			StringBuilder sb = new StringBuilder();
			for (Object streetObject : getDeliveryStreets(zip)) {
				if (streetObject instanceof Map<?, ?>) {
					sb.append((Map<String, List<Integer>>) streetObject).append(',');
				} else if (streetObject instanceof String) {
					sb.append((String) streetObject).append(',');
				}
				if (sb.length() > 500) {
					System.out.println(sb.toString());
					sb.setLength(0);
				}
			}
		}

		Map<String, Street> deliveryStreets = getDeliveryStreets();
		int i = 0;
		for (Street deliveryStreet : deliveryStreets.values()) {
			System.out.println(deliveryStreet);
			if (++i > 20) {
				break;
			}
		}

		Map<String, Integer> tableNumbers = getTableNumbers();
		for (String tableNumber : getTableNumbers().keySet()) {
			System.out.println("tableNumber=" + tableNumber + " tableCapacity=" + tableNumbers.get(tableNumber));
		}

		System.out.println("\n==========Last Line========");
	}
}
