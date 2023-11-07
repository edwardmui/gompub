package com.orderfoodnow.pos.shared;

import java.util.EnumSet;

public enum OrderItemStatus {
	// @formatter:off
	WAITING("waiting"),
	COOKING("cooking"),
	EATING("eating"),
	VOIDED("voided"),
	;
	// @formatter:on
	OrderItemStatus(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	public String getDisplayName() {
		return displayName;
	}

	public static OrderItemStatus getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= OrderItemStatus.values().length ? null
				: OrderItemStatus.values()[ordinalValue]);
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + OrderItemStatus.class.getSimpleName());
		for (OrderItemStatus orderType : EnumSet.allOf(OrderItemStatus.class)) {
			System.out.println("displayName=" + orderType.getDisplayName());
		}
	}
}
