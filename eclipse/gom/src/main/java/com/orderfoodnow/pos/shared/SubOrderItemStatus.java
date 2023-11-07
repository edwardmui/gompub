package com.orderfoodnow.pos.shared;

import java.util.EnumSet;

public enum SubOrderItemStatus {
	// @formatter:off
	VALID("valid"),
	VOIDED("voided"),
	;
	// @formatter:on

	SubOrderItemStatus(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	public String getDisplayName() {
		return displayName;
	}

	public static SubOrderItemStatus getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= SubOrderItemStatus.values().length ? null
				: SubOrderItemStatus.values()[ordinalValue]);
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + SubOrderItemStatus.class.getSimpleName());
		for (SubOrderItemStatus orderType : EnumSet.allOf(SubOrderItemStatus.class)) {
			System.out.println("displayName=" + orderType.getDisplayName());
		}
	}
}
