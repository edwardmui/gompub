package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PermissionRequiredType {
	// @formatter:off
	// The first parameter is the actionName. The string must match the
	// permissionRequiredActionOptions values in conf.yaml.
	PRINT_REPORT("printReport"),
	PRINT_SUMMARY("printSummary"),
	PRINT_DETAIL("printDetail"),
	VOID_ORDER("voidOrder"),
	VOID_ORDER_ITEM("voidOrderItem"),
	VOID_CUSTOMIZER("voidCustomizer"),
	VOID_EXCHANGER("voidExchanger"),
	CLOSE("close"),
	SHUTDOWN_SERVER("shutdownServer"),
	;
	// @formatter:on

	PermissionRequiredType(String actionName) {
		this.actionName = actionName;
	}

	private final String actionName;

	public String getActionName() {
		return actionName;
	}

	// to ensure actionName uniqueness and easy paramId look up
	private static final Map<String, PermissionRequiredType> actionNameToReportType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PermissionRequiredType type : EnumSet.allOf(PermissionRequiredType.class)) {
			enumNames.add(type.toString());
			if (type.actionName != null) {
				if (actionNameToReportType.containsKey(type.actionName)) {
					throw new RuntimeException("The shortHandChar value of " + type.actionName + " is duplicated");
				}
				actionNameToReportType.put(type.actionName, type);
				enumNames.add(type.toString());
			}
		}
	}

	public static PermissionRequiredType getEnum(String actionName) {
		return actionNameToReportType.get(actionName);
	}

	public static PermissionRequiredType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PermissionRequiredType.values().length ? null
				: PermissionRequiredType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PermissionRequiredType.class.getSimpleName());
		for (PermissionRequiredType type : EnumSet.allOf(PermissionRequiredType.class)) {
			System.out.println("actionName=" + type.getActionName());
		}

		for (String actionName : actionNameToReportType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + PermissionRequiredType.getEnum(actionName));
		}
	}
}
