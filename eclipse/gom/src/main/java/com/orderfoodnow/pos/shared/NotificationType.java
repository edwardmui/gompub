package com.orderfoodnow.pos.shared;

public enum NotificationType {
	// @formatter:off
	REFRESH_UNPAID_ORDER,
	PRINT_HELD_ORDER,
	SERVER_SHUTTINGDOWN,
	;
	// @formatter:on

	public static NotificationType getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= NotificationType.values().length ? null
				: NotificationType.values()[ordinalValue]);
	}
}
