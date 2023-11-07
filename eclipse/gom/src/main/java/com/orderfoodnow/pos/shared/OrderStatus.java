package com.orderfoodnow.pos.shared;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum OrderStatus {
	// @formatter:off
	HOLDING("holding", 'H'), 		// Order placed and requested to be picked up in more than than 60 minutes from time of ordering
	MAKING("making", 'M'), 			// Order placed and immediately sent to kitchen for making
	// BAGGED("bagged", 'B'),		// Future? requires packer to mark the order packed
	PAID("paid", 'P'),				// Order is paid
	VOIDED("voided", 'V'),			// Order has been voided
	// REFUNDED("refunded", 'R'),	// Future? Need to handle user selection and check for cases all proper handling including summary report
	;
	// @formatter:on

	OrderStatus(String displayName, Character shortHandChar) {
		this.displayName = displayName;
		this.shortHandChar = shortHandChar;
	}

	private final String displayName;
	private final Character shortHandChar;
	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Character, OrderStatus> shortHandCharToOrderStatus = new HashMap<>();

	static {
		for (OrderStatus type : EnumSet.allOf(OrderStatus.class)) {
			if (type.shortHandChar != null) {
				if (shortHandCharToOrderStatus.containsKey(type.shortHandChar)) {
					throw new RuntimeException("The paramId value of " + type.shortHandChar + " is duplicated");
				}
				shortHandCharToOrderStatus.put(type.shortHandChar, type);
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public Character getShortHandChar() {
		return shortHandChar;
	}

	public OrderStatus getEnum(Character shortHandChar) {
		return shortHandCharToOrderStatus.get(shortHandChar);
	}

	public static OrderStatus getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= OrderStatus.values().length ? null
				: OrderStatus.values()[ordinalValue]);
	}
}
