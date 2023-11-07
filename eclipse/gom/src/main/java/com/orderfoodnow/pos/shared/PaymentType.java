package com.orderfoodnow.pos.shared;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum PaymentType {
	// @formatter:off
	CASH("Cash",		"Cash",			"Can have multiple cash payments.",														KeyEvent.VK_C, 'C'),
	CREDIT_CARD("Card", "Credit Card",	"Can only have one cash payment. If multiple payments, use other payment types first.", KeyEvent.VK_R, 'R'),
	CHECK("Check",		"Check",		"Can have multiple check payments.",													KeyEvent.VK_K, 'K'),
	;
	// @formatter:on

	PaymentType(String displayName, String fullName, String toolTipText, int mnemonic, Character shortHandChar) {
		this.displayName = displayName;
		this.fullName = fullName;
		this.toolTipText = toolTipText;
		this.mnemonic = mnemonic;
		this.shortHandChar = shortHandChar;
	}

	private final String displayName;
	private final String fullName;
	private final String toolTipText;
	private final int mnemonic;
	private static final List<String> stringMnemonics = new ArrayList<>();
	private final Character shortHandChar;
	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Character, PaymentType> shortHandCharToPaymentType = new HashMap<>();
	private static final Map<Integer, PaymentType> mnemonicToPaymentType = new HashMap<>();
	private static final List<String> stringMnemonicsAndDisplayName = new ArrayList<>();

	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (PaymentType type : EnumSet.allOf(PaymentType.class)) {
			enumNames.add(type.toString());
			if (type.shortHandChar != null) {
				if (shortHandCharToPaymentType.containsKey(type.shortHandChar)) {
					throw new RuntimeException("The shortHandChar value of " + type.shortHandChar + " is duplicated");
				}
				shortHandCharToPaymentType.put(type.shortHandChar, type);
				enumNames.add(type.toString());
				stringMnemonics.add(String.valueOf((char) type.mnemonic));

				if (mnemonicToPaymentType.containsKey(type.mnemonic)) {
					throw new RuntimeException("The mnemonic value of " + type.mnemonic + " is duplicated");
				}
				mnemonicToPaymentType.put(type.mnemonic, type);
				stringMnemonicsAndDisplayName.add(String.valueOf((char) type.mnemonic) + "(" + type.displayName + ")");
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public int getMnemonic() {
		return mnemonic;
	}

	public Character getShortHandChar() {
		return shortHandChar;
	}

	public static PaymentType getEnum(Character shortHandChar) {
		return shortHandCharToPaymentType.get(shortHandChar);
	}

	public static PaymentType getEnumWithMnemonic(Integer mnemonic) {
		return mnemonicToPaymentType.get(mnemonic);
	}

	public static List<String> getStringMnemonics() {
		return stringMnemonics;
	}

	public static List<String> getStringMnemonicsAndDisplayName() {
		return stringMnemonicsAndDisplayName;
	}

	public static PaymentType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= PaymentType.values().length ? null
				: PaymentType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + PaymentType.class.getSimpleName());
		for (PaymentType type : EnumSet.allOf(PaymentType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (Character ch : shortHandCharToPaymentType.keySet()) {
			System.out.println("ch=" + ch + " type=" + PaymentType.getEnum(ch));
		}
	}
}
