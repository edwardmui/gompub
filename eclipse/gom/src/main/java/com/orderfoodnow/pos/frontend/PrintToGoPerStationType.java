package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PrintToGoPerStationType {
	// @formatter:off
	PRINT_TO_GO_PER_STATION("printToGoPerStation", "Print To Go Per Station"),
	PRINT_TO_GO_PER_STATTION_IN_CHINESE("printToGoPerStationInChinese", "Print To Go Per Station In Chinese"),
	PRINT_TO_GO_PER_STATION_IN_ENGLISH_AND_CHINESE("printToGoPerStationInEnglishAndChinese", "Print To Go Per Station in English and Chinese"),
	PRINT_TO_GO_PER_STATION_IN_ENGLISH_AND_CHINESE_2("printToGoPerStationInEnglishAndChineseInTwoLines", "Print To Go Per Station in English Chinese in two lines"),
	;
	// @formatter:on

	PrintToGoPerStationType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<String, PrintToGoPerStationType> actionNameToPrintType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PrintToGoPerStationType type : EnumSet.allOf(PrintToGoPerStationType.class)) {
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

	public static PrintToGoPerStationType getEnum(String actionName) {
		return actionNameToPrintType.get(actionName);
	}

	public static PrintToGoPerStationType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PrintToGoPerStationType.values().length ? null
				: PrintToGoPerStationType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PrintToGoPerStationType.class.getSimpleName());
		for (PrintToGoPerStationType type : EnumSet.allOf(PrintToGoPerStationType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToPrintType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PrintToGoPerStationType.getEnum(actionName));
		}
	}
}
