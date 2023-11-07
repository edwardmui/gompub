package com.orderfoodnow.pos.shared;

import java.util.EnumSet;

public enum PlacementType {
	// @formatter:off
	APP("App"),
	WEB("Web"),
	PHONE("Phone"),
	IN_PERSON("In-Person"),
	;
	// @formatter:on

	PlacementType(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	public String getDisplayName() {
		return displayName;
	}

	public static PlacementType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PlacementType.values().length ? null
				: PlacementType.values()[ordinalValue]);
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PlacementType.class.getSimpleName());
		for (PlacementType type : EnumSet.allOf(PlacementType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}
	}
}
