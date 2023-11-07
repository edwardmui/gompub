package com.orderfoodnow.pos.shared;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum CouponVariantType {
	// @formatter:off
	// The first parameter is the actionName. The string must match the printCouponActionOptions values in conf.yaml.
	COUPON1("printCoupon1"),
	COUPON2("printCoupon2"),
	COUPON3("printCoupon3"),
	;
	// @formatter:on

	CouponVariantType(String actionName) {
		this.actionName = actionName;
	}

	private final String actionName;

	public String getActionName() {
		return actionName;
	}

	// to ensure actionName uniqueness and easy paramId look up
	private static final Map<String, CouponVariantType> actionNameToCouponVariantType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (CouponVariantType type : EnumSet.allOf(CouponVariantType.class)) {
			enumNames.add(type.toString());
			if (type.actionName != null) {
				if (actionNameToCouponVariantType.containsKey(type.actionName)) {
					throw new RuntimeException("The shortHandChar value of " + type.actionName + " is duplicated");
				}
				actionNameToCouponVariantType.put(type.actionName, type);
				enumNames.add(type.toString());
			}
		}
	}

	public static CouponVariantType getEnum(String actionName) {
		return actionNameToCouponVariantType.get(actionName);
	}

	public static CouponVariantType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= CouponVariantType.values().length ? null
				: CouponVariantType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + CouponVariantType.class.getSimpleName());
		for (CouponVariantType type : EnumSet.allOf(CouponVariantType.class)) {
			System.out.println("actionName=" + type.getActionName());
		}

		for (String actionName : actionNameToCouponVariantType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + CouponVariantType.getEnum(actionName));
		}
	}
}
