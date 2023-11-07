package com.orderfoodnow.pos.shared;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum OrderType {
	// @formatter:off
	DELIVERY("Delivery",	"外                  送",	"=D or TAB once and use the space bar to select",							KeyEvent.VK_D),
	PHONE_IN("Phone-in",	"外                  卖",	"=P or TAB twice and use the space bar to select",							KeyEvent.VK_P),
	WALK_IN("Walk-in",		"外                  等",	"=W or the order type will set to walk-in if you paid the new order",		KeyEvent.VK_W),
	DINE_IN("Dine-in",		"堂      食",		"=I or the order type will set to Dining if you enter the table number",	KeyEvent.VK_I),
	;
	// @formatter:on

	OrderType(String displayName, String chineseName, String toolTipText, int mnemonic) {
		this.displayName = displayName;
		this.chineseName = chineseName;
		this.toolTipText = toolTipText;
		this.mnemonic = mnemonic;
	}

	private final String displayName;
	private final String chineseName;
	private final String toolTipText;
	private final int mnemonic;

	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Integer, OrderType> mnemonicToOrderType = new HashMap<>();

	private static final Set<String> enumNames = new HashSet<>();
	private static final List<Integer> mnemonics = new ArrayList<>();
	private static final List<String> stringMnemonics = new ArrayList<>();
	private static final List<String> stringMnemonicsAndDisplayName = new ArrayList<>();
	static {
		for (OrderType type : EnumSet.allOf(OrderType.class)) {
			if (mnemonicToOrderType.containsKey(type.mnemonic)) {
				throw new RuntimeException("The mnemonic value of " + type.mnemonic + " is duplicated");
			}
			mnemonicToOrderType.put(type.mnemonic, type);
			enumNames.add(type.toString());
			mnemonics.add(type.mnemonic);
			stringMnemonics.add(String.valueOf((char) type.mnemonic));
			stringMnemonicsAndDisplayName.add(String.valueOf((char) type.mnemonic) + "(" + type.displayName + ")");
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getChineseName() {
		return chineseName;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public int getMnemonic() {
		return mnemonic;
	}

	public static OrderType getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= OrderType.values().length ? null
				: OrderType.values()[ordinalValue]);
	}

	public static OrderType getEnumByMnemonic(int mnemonic) {
		return (mnemonicToOrderType.get(mnemonic));
	}

	public static List<Integer> getMnemonics() {
		return mnemonics;
	}

	public static List<String> getStringMnemonics() {
		return stringMnemonics;
	}

	public static List<String> getStringMnemonicsAndDisplayName() {
		return stringMnemonicsAndDisplayName;
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + OrderType.class.getSimpleName());
		for (OrderType orderType : EnumSet.allOf(OrderType.class)) {
			System.out.println("displayName=" + orderType.getDisplayName());
			System.out.println("mnemonic=" + (char) orderType.getMnemonic());
		}
		try {
			System.out.println(OrderType.valueOf("DELIVERY1"));
		} catch (IllegalArgumentException e) {
			System.out.println("String is not correct. Should be DELIVERY");
		}

		for (String displayName : enumNames) {
			System.out.println("displayName=" + displayName);

		}

		System.out.println("mnemonics.toString()=" + mnemonics.toString());

		if (enumNames.contains("DELIVERY")) {
			System.out.println("found DELIVERY");
		}
	}
}
