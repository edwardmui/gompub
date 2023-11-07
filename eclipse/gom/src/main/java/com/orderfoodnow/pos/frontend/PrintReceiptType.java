package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PrintReceiptType {
	// @formatter:off
	PRINT_RECEIPT("printReceipt", "Print Receipt"),
	PRINT_RECEIPT_IN_CHINESE("printReceiptInChinese", "Print Receipt In Chinese"),
	PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE("printReceiptInEnglishAndChinese", "Print Receipt In English and Chinese"),
	PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2("printReceiptInEnglishAndChineseInTwoLines", "Print Receipt in English and Chinese in two lines"),
	;
	// @formatter:on

	PrintReceiptType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;

	// to ensure uniqueness and easy paramId look up
	private static final Map<String, PrintReceiptType> actionNameToPrintType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PrintReceiptType type : EnumSet.allOf(PrintReceiptType.class)) {
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

	public static PrintReceiptType getEnum(String actionName) {
		return actionNameToPrintType.get(actionName);
	}

	public static PrintReceiptType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PrintReceiptType.values().length ? null
				: PrintReceiptType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PrintReceiptType.class.getSimpleName());
		for (PrintReceiptType type : EnumSet.allOf(PrintReceiptType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToPrintType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PrintReceiptType.getEnum(actionName));
		}
	}
}
