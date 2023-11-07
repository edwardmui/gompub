package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PrintOrderNoPriceType {
	// @formatter:off
	PRINT_ORDER_NO_PRICE("printOrderNoPrice", "Print Order No Price"),
	PRINT_ORDER_NO_PRICE_IN_CHINESE("printOrderNoPriceInChinese", "Print Order No Price in Chinese"),
	PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE("printOrderNoPriceInEnglishAndChinese", "Print Order No Price in English and Chinese"),
	PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2("printOrderNoPriceInEnglishAndChineseInTwoLines", "Print Order No Price in English and Chinese in two lines"),
	;
	// @formatter:on

	PrintOrderNoPriceType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<String, PrintOrderNoPriceType> actionNameToPrintType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PrintOrderNoPriceType type : EnumSet.allOf(PrintOrderNoPriceType.class)) {
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

	public static PrintOrderNoPriceType getEnum(String actionName) {
		return actionNameToPrintType.get(actionName);
	}

	public static PrintOrderNoPriceType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PrintOrderNoPriceType.values().length ? null
				: PrintOrderNoPriceType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PrintOrderNoPriceType.class.getSimpleName());
		for (PrintOrderNoPriceType type : EnumSet.allOf(PrintOrderNoPriceType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToPrintType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PrintOrderNoPriceType.getEnum(actionName));
		}
	}
}
