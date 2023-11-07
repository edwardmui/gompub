package com.orderfoodnow.pos.shared;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CustomerStatus {
	// @formatter:off
	ORDERED("ordered", 'o'), // customer ordered
	ARRIVED("arrived", 'a'), // customer waiting on premise, for creating customer waiting list to display
	PICKED_UP("pickedUp", 'p'), // customer picked order
	;
	// @formatter:on

	CustomerStatus(String displayName, Character shortHandChar) {
		this.displayName = displayName;
		this.shortHandChar = shortHandChar;
	}

	private final String displayName;
	private final Character shortHandChar;
	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Character, CustomerStatus> shortHandCharToCustomerStatus = new HashMap<Character, CustomerStatus>();

	static {
		for (CustomerStatus type : EnumSet.allOf(CustomerStatus.class)) {
			if (type.shortHandChar != null) {
				if (shortHandCharToCustomerStatus.containsKey(type.shortHandChar)) {
					throw new RuntimeException("The paramId value of " + type.shortHandChar + " is duplicated");
				}
				shortHandCharToCustomerStatus.put(type.shortHandChar, type);
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public Character getShortHandChar() {
		return shortHandChar;
	}

	public CustomerStatus getEnum(Character shortHandChar) {
		return shortHandCharToCustomerStatus.get(shortHandChar);
	}

	public static CustomerStatus getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= CustomerStatus.values().length ? null
				: CustomerStatus.values()[ordinalValue]);
	}
}
