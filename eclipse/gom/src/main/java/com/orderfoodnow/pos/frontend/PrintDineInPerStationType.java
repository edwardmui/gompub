package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PrintDineInPerStationType {
	// @formatter:off
	PRINT_DINE_IN_PER_STATION("printDineInPerStation", "Print Dine-in Per Station"),
	PRINT_DINE_IN_PER_STATTION_IN_CHINESE("printDineInPerStationInChinese", "Print Dine-in Per Station In Chinese"),
	PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE("printDineInPerStationInEnglishAndChinese", "Print Dine-in Per Station in English and Chinese"),
	PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2("printDinePerStationInEnglishAndChineseInTwoLines", "Print Dine-in Per Station in English Chinese in two lines"),
	;
	// @formatter:on

	PrintDineInPerStationType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<String, PrintDineInPerStationType> actionNameToPrintType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PrintDineInPerStationType type : EnumSet.allOf(PrintDineInPerStationType.class)) {
			enumNames.add(type.toString());
			if (type.actionName != null) {
				if (actionNameToPrintType.containsKey(type.actionName)) {
					throw new RuntimeException("The actionName value of " + type.actionName + " is duplicated");
				}
				actionNameToPrintType.put(type.actionName, type);
				enumNames.add(type.toString());
			}
		}
	}

	public String getActionName() {
		return actionName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static PrintDineInPerStationType getEnum(String actionName) {
		return actionNameToPrintType.get(actionName);
	}

	public static PrintDineInPerStationType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PrintDineInPerStationType.values().length ? null
				: PrintDineInPerStationType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PrintDineInPerStationType.class.getSimpleName());
		for (PrintDineInPerStationType type : EnumSet.allOf(PrintDineInPerStationType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToPrintType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PrintDineInPerStationType.getEnum(actionName));
		}
	}
}
