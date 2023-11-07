package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PrintOrderType {
	// @formatter:off
	PRINT_ORDER("printOrder", "Print Order"),
	PRINT_ORDER_IN_CHINESE("printOrderInChinese", "Print Order in Chinese"),
	PRINT_ORDER_IN_ENGLISH_AND_CHINESE("printOrderInEnglishAndChinese", "Print Order in English and Chinese"),
	PRINT_ORDER_IN_ENGLISH_AND_CHINESE_2("printOrderInEnglishAndChineseInTwoLines", "Print Order in English and Chinese in two lines"),
	PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE("printOrderHeldConfirmationInEnglishAndChinese", "Print Order Held in English and Chinese"),
	;
	// @formatter:on

	PrintOrderType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<String, PrintOrderType> actionNameToPrintType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PrintOrderType type : EnumSet.allOf(PrintOrderType.class)) {
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

	public static PrintOrderType getEnum(String actionName) {
		return actionNameToPrintType.get(actionName);
	}

	public static PrintOrderType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PrintOrderType.values().length ? null
				: PrintOrderType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PrintOrderType.class.getSimpleName());
		for (PrintOrderType type : EnumSet.allOf(PrintOrderType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToPrintType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PrintOrderType.getEnum(actionName));
		}
	}
}
