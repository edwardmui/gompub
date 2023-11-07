package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PrintType {
	// @formatter:off
	PRINT_DRINKS("printDrinks", "Print Drinks"),
	PRINT_DRINKS_IN_CHINESE("printDrinksInChinese", "Print Drinks In Chinese"),
	PRINT_DRINKS_IN_EN_N_CHI("printDrinksInEnglishAndChinese", "Print Drinks In En N Chi"),
	PRINT_DRINKS_IN_EN_N_CHI_2("printDrinksInEnglishAndChineseInTwoLines", "Print Drinks (Eng & Chi) in 2 Line"),
	PRINT_APPETIZERS("printAppetizers", "Print Appetizers"),
	PRINT_APPETIZERS_IN_CHINESE("printAppetizersInChinese", "Print Appetizers In Chinese"),
	PRINT_APPETIZERS_IN_EN_N_CHI("printAppetizersInEnglishAndChinese", "Print Appetizers In En N Chi"),
	PRINT_APPETIZERS_IN_EN_N_CHI_2("printAppetizersInEnglishAndChineseInTwoLines", "Print Appetizers (Eng & Chi) in 2 Line"),
	PRINT_ENTREES("printEntrees", "Print Entrees"),
	PRINT_ENTREES_IN_CHINESE("printEntreesInChinese", "Print Entrees In Chinese"),
	PRINT_ENTREES_IN_EN_N_CHI("printEntreesInEnglishAndChinese", "Print Entrees In En N Chi"),
	PRINT_ENTREES_IN_EN_N_CHI_2("printEntreesInEnglishAndChineseTwoLines", "Print Entrees (Eng & Chi) in 2 Line"),
	;
	// @formatter:on

	PrintType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<String, PrintType> actionNameToPrintType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PrintType type : EnumSet.allOf(PrintType.class)) {
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

	public static PrintType getEnum(String actionName) {
		return actionNameToPrintType.get(actionName);
	}

	public static PrintType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PrintType.values().length ? null
				: PrintType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PrintType.class.getSimpleName());
		for (PrintType type : EnumSet.allOf(PrintType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToPrintType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PrintType.getEnum(actionName));
		}
	}
}
