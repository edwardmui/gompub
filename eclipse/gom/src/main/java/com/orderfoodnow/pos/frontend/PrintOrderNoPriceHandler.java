package com.orderfoodnow.pos.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.print.PrintService;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.CouponDish;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.Subdish;
import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.staff.Employee;

public class PrintOrderNoPriceHandler extends PrinterBase {
	public static final String KITCHENCOPY = "KITCHEN COPY";
	public static final String KITCHENCOPY_IN_CHINESE = "厨房单";

	public PrintOrderNoPriceHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public void print(PrintOrderNoPriceType printOrderNoPriceType, FontSizeType fontSizeType, Client clientGui,
			Cart cart, Calculator calculator) {
		logger.finest("Entered");
		setFont(fontSizeType);
		try {
			out.write(init);
			out.write(alignCenter);

			if (printOrderNumberOnOrder) {
				printOrderNumber(cart);
			}

			switch (printOrderNoPriceType) {
			case PRINT_ORDER_NO_PRICE:
				out.write(font12x24_W2H2);
				out.write((KITCHENCOPY + "\n\n").getBytes());
				break;

			case PRINT_ORDER_NO_PRICE_IN_CHINESE:
				out.write(allowChineseFontW2H2);
				out.write((KITCHENCOPY_IN_CHINESE + "\n\n").getBytes(CHINESE_ENCODING));
				break;

			case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE:
			case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2:
				out.write(font12x24_W1H2);
				out.write(allowChineseFontW2H2);
				out.write((KITCHENCOPY + " " + KITCHENCOPY_IN_CHINESE + "\n\n").getBytes(CHINESE_ENCODING));
				break;

			default:
				throw new RuntimeException("Unhandled printOrderNoPriceType=" + printOrderNoPriceType);
			}

			out.write(disAllowChineseFontWxHx);
			out.write(init); // clear the alignCenter command

			printOrderTypeHeading(cart);

			printOrderTypeSpecifics(cart);

			out.write(init);
			out.write("\n".getBytes());
			out.write(font12x24_W2H1);
			Employee serverEmployee = Client.idToEmployee.get(cart.getDineInInfo().getServerId());
			String serverNickName = serverEmployee == null ? "" : serverEmployee.getNickname();
			String dayOfWeek = Util.getDayOfTheWeek(cart.getOrder().getOrderedTime()); // dow = day of the week
			if (serverNickName.isEmpty() == false) {
				out.write((dayOfWeek + ", Server: " + serverNickName).getBytes());
			} else {
				out.write((dayOfWeek + '\n').getBytes());
			}

			out.write(font09x17_W1H1);
			out.write(font12x24_W2H1);
			if (printCheckNumberOnOrder) {
				out.write((" Ck:" + cart.getOrder().getCheckNumberPrefix()).getBytes());
				out.write(font12x24_W2H1);
				out.write(cart.getOrder().getCheckNumber().getBytes());
				out.write(font09x17_W1H1);
				// out.write((" v" + order.getPrtVersion()+'\n').getVersion());
			}

			out.write(font12x24_W2H1);
			out.write(("-" + Util.formatDateAndTimeNoSeconds(cart.getOrder().getOrderedTime()) + "-").getBytes());
			out.write(font12x24_W2H1);
			out.write("\n\n".getBytes());

			out.write(init); // clear the alignCenter

			// Request time will be printed on all copy
			if (cart.getToGoInfo().getRequestedTime() != 0) {
				printRequestedTime(cart);
			}

			CouponDish unvoidedCouponDish = calculator.getCouponDish(false);
			Dish dish1 = unvoidedCouponDish == null ? null
					: Client.menu.getActiveDish(unvoidedCouponDish.getDish1Name());
			Dish dish2 = unvoidedCouponDish == null ? null
					: Client.menu.getActiveDish(unvoidedCouponDish.getDish2Name());
			logger.fine("couponDish=" + unvoidedCouponDish + " dish1=" + dish1 + " dish2=" + dish2);
			List<OrderItem> unvoidedOrderItemsWithSubstitutedCouponDish = calculator
					.getOrderItemsWithSubstitutedCouponDish(unvoidedCouponDish, dish1, dish2, false,
							Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.condiment));
			logger.fine("printOrderNoPriceType=" + printOrderNoPriceType
					+ " unvoidedOrderItemsWithSubstitutedCouponDish:" + unvoidedOrderItemsWithSubstitutedCouponDish);
			// printWidth up to 42 columns
			Collections.sort(unvoidedOrderItemsWithSubstitutedCouponDish, OrderItem.getDishComparator(Client.menu));
			for (OrderItem unvoidedOrderItem : unvoidedOrderItemsWithSubstitutedCouponDish) {
				Dish dish = Client.dishes[unvoidedOrderItem.getDishId()];
				if (dish.getCategory() == Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.allNoIngredient)) {
					if (unvoidedOrderItem.isVoided() == false) {
						printAllNoIngredient(dish.getEnglishAndChineseNames());
					}
				} else {
					int largeQuantity = unvoidedOrderItem.getLargeQuantity();
					int smallQuantity = unvoidedOrderItem.getSmallQuantity();
					String largeQuantityPlusPadding = unvoidedOrderItem.getLargeQuantityWithPadding();
					String smallQuantityPlusPadding = unvoidedOrderItem.getSmallQuantityWithPadding();
					String printingDishName = printOrderNoPriceType == PrintOrderNoPriceType.PRINT_ORDER_NO_PRICE
							? dish.getShortName()
							: dish.getEnglishAndChineseNames();
					String chineseName = dish.getChineseName();

					switch (printOrderNoPriceType) {
					case PRINT_ORDER_NO_PRICE_IN_CHINESE:
						printingDishName = chineseName;
						// fall thru
					case PRINT_ORDER_NO_PRICE:
					case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE:
						out.write(dishNameFont);
						out.write(chiDishNameFont);
						out.write((largeQuantityPlusPadding + smallQuantityPlusPadding).getBytes());
						out.write(defaultEngCharSpacingIs00);
						out.write(defaultChiCharSpacingIs00);
						if (cart.getCustomizerTray().hasCustomizer(unvoidedOrderItem)) {
							out.write(printingDishName.getBytes(CHINESE_ENCODING));
							out.write(defaultEngCharSpacingIs00); // clear the char spacing
							out.write(defaultChiCharSpacingIs00);
							out.write(font09x17_W1H1);
							out.write(("\n").getBytes());
						} else {
							out.write((printingDishName + '\n').getBytes(CHINESE_ENCODING));
							out.write(defaultEngCharSpacingIs00); // clear the char spacing
							out.write(defaultChiCharSpacingIs00);
						}
						break;
					case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2:
						// English and Chinese will be printed in a separate Line
						out.write(dishNameFont); // for English font W1H2 or W1H1
						out.write((largeQuantityPlusPadding + smallQuantityPlusPadding).getBytes());
						out.write(engCharSpacingInCSV);
						out.write(dish.getShortName().getBytes());
						out.write(defaultEngCharSpacingIs00);

						if (cart.getCustomizerTray().hasCustomizer(unvoidedOrderItem)) {
							if (chineseName != null) {
								out.write(("\n").getBytes());
								out.write(chiDishNameFont); // for Chinese font W1H2 or W1H1
								out.write(chiCharSpacingInCSV);
								out.write(("        " + chineseName).getBytes(CHINESE_ENCODING)); // the chiCharSpacing
																									// has no effect on
																									// the empty space
								out.write(defaultChiCharSpacingIs00); // clear the char spacing
							}
							out.write(("\n").getBytes());

							// orderItem with no customizer the orderItemTotal amount will be printed next
							// line of that orderItem
						} else { // the orderItemTotal will add up with the customizer total
							if (chineseName != null) {
								out.write(chiDishNameFont); // for Chinese font W1H1 or W1H2 or W2H2
								out.write(chiCharSpacingInCSV);
								out.write(("        " + chineseName + '\n').getBytes(CHINESE_ENCODING));
								out.write(defaultChiCharSpacingIs00); // clear the Chinese char spacing
							}
						}
						break;

					default:
						throw new RuntimeException("Unhandled printOrderNoPriceType=" + printOrderNoPriceType);
					}

					// English and Chinese will only printed in one line and one size on the
					// subdish. the font size for the subdish is font12x24_W1H1
					List<Subdish> dishSuiteExpandedSubdishes = dish.getSubdishes();
					if (dishSuiteExpandedSubdishes != null && unvoidedOrderItem.isVoided() == false) {
						List<Subdish> foundInMenuSubdishes = new ArrayList<>();
						for (Subdish subdish : dishSuiteExpandedSubdishes) {
							// if the subdish name is not found in the menu, than set the
							// subDishChineseName = subdishName and just print the subdish name that is
							// defined in the dish.
							if (Client.menu.getActiveDish(subdish.getName()) == null) {
								out.write(font12x24_W1H1);
								out.write(("   " + subdish.getQuantityInString(largeQuantity, smallQuantity) + "    "
										+ getSubdishName(printOrderNoPriceType, subdish, cart) + '\n')
												.getBytes(CHINESE_ENCODING));
							} else {
								foundInMenuSubdishes.add(subdish);
							}
						}

						Collections.sort(foundInMenuSubdishes);
						for (Subdish subdish : foundInMenuSubdishes) {
							out.write(font12x24_W1H1);
							out.write(disAllowChineseFontWxHx);
							out.write(("   " + subdish.getQuantityInString(largeQuantity, smallQuantity) + "    "
									+ getSubdishName(printOrderNoPriceType, subdish, cart) + '\n')
											.getBytes(CHINESE_ENCODING));
						}
					}

					List<Customizer> customizers = cart.getCustomizerTray().getCustomizers(unvoidedOrderItem);
					for (Customizer customizer : customizers) {
						switch (customizer.getStatus()) {
						case VALID:
							out.write(customizerFont); // W1H1 or W1H2
							out.write(chiCustomizerFont);// W1H1 or W1H2
							out.write("        (".getBytes());
							out.write(getCustomizerName(printOrderNoPriceType, customizer, cart)
									.getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(customizerFont);
							out.write(")\n".getBytes());
							break;
						case VOIDED:
							// Mixed-in the voided and valid as voided customizer needs the orderItem
							// context. Voided is expected to be rare.
							// @formatter:off
							// String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
							//		+ getCancelledString(PrintOrderNoPriceType.PRINT_ORDER_NO_PRICE) + " *** "
							//		+ getExchangerName(PrintOrderNoPriceType.PRINT_ORDER_NO_PRICE, dish);
							// printedVoided(voidedName, customizer.getVoidedById(), customizer.getVoidedTime());
							// @formatter:on
							break;
						}
					}

					List<Exchanger> exchangers = cart.getExchangerTray().getExchangers(unvoidedOrderItem);
					// the exchanger will not be printed, if the orderItem is voided
					for (Exchanger exchanger : exchangers) {
						Dish exchangerDish = Client.dishes[exchanger.getExchangerDishId()];

						switch (exchanger.getStatus()) {
						case VALID:
							out.write(customizerFont); // W1H1 or W1H2
							out.write(chiCustomizerFont);// W1H1 or W1H2
							out.write("        (".getBytes());
							out.write(
									getExchangerName(printOrderNoPriceType, exchangerDish).getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(customizerFont);
							out.write(")\n".getBytes());
							break;
						case VOIDED:
							// Mix-in the voided and valid as it needs the orderItem context. Voided is
							// expected to be rare.
							// @formatter:off
							// String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
							//		+ getCancelledString(printOrderNoPriceType) + " *** "
							//		+ getExchangerName(printOrderNoPriceType, exchangerDish);
							// printedVoided(voidedName, exchanger.getVoidedById(), exchanger.getVoidedTime());
							// @formatter:on
							break;
						default:
							throw new RuntimeException("Unhandled exchanger=" + exchanger.getStatus());
						}
					}

					out.write(alignRight);
					out.write(orderItemTotalFont);
					out.write(("\n").getBytes());
					out.write(lineSpacingPrtReceiptInCSV); // to set the line spacing in between each dish. do not
															// delete
					out.write(defaultLineSpacing); // reset back to the default line spacing == lineSpacing60
					out.write(alignLeft);
				}
			} // for unvoidedOrderItem

			List<OrderItem> voidedOrderItems = cart.getOrderItemTray().getOrderItems(true);
			Collections.sort(voidedOrderItems, OrderItem.getDishComparator(Client.menu));
			for (OrderItem voidedOrderItem : voidedOrderItems) {
				Dish dish = Client.dishes[voidedOrderItem.getDishId()];
				// Unlike unvoidedOrderItems where there's can only be one couponDish, the
				// voidedOrderItems can have more than one.
				// To translate the couponDish (which could have 2 dishes associated dishes) an
				// print out the correct name an quantity needs a chunk of code.
				// Just output a note for now.
				if (dish.getCategory() == Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.couponDish)) {
					out.write(font09x17_W1H1);
					out.write("See coupon name for cancelled dish\n".getBytes());
				}
				String dishName = printOrderNoPriceType == PrintOrderNoPriceType.PRINT_ORDER_NO_PRICE
						? dish.getShortName()
						: dish.getEnglishAndChineseNames();
				String largeQuantityPlusPadding = voidedOrderItem.getLargeQuantityWithPadding();
				String smallQuantityPlusPadding = voidedOrderItem.getSmallQuantityWithPadding();
				String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
						+ getCancelledString(printOrderNoPriceType) + " *** " + dishName;
				printedVoided(voidedName, voidedOrderItem.getVoidedById(), voidedOrderItem.getVoidedTime());
			}

			// Order note will be printed on all copies
			if (cart.getOrder().getNote().isEmpty() == false) {
				out.write(font09x17_W2H2);
				out.write(("   " + cart.getOrder().getNote() + '\n').getBytes());
			}

			out.write("\n\n\n\n".getBytes());
			out.write(cut);
			out.write("\n".getBytes());
			out.write(init);
			out.write(printerAlarm);
			out.write(init); // to clear the openDrawer function
			out.flush();

			sendOutputStreamToPrinter();
			clientGui.setInfoFeedback("Order printed to " + location);
		} catch (Exception e) {
			logger.warning("Print Order failed. error: " + e.getMessage());
			e.printStackTrace();
			clientGui.setWarningFeedback("Order was not printed. Check printer and try again...");
		}
	}

	private void setFont(FontSizeType fontSizeType) {
		logger.finest("Entered");
		// set the font for Print Kitchen Copy:
		switch (fontSizeType) {
		case LARGE:
			dishNameFont = font12x24_W1H2;
			engCharSpacing = engCharSpacingInCSV;
			customizerFont = font12x24_W1H2;
			chiDishNameFont = allowChineseFontW1H2;
			chiCharSpacing = chiCharSpacingInCSV;
			chiCustomizerFont = allowChineseFontW1H2;
			orderItemTotalFont = font12x24_W1H1;
			lineSpacing = lineSpacingPrtOrdInCSV;
			break;
		default:
			dishNameFont = font12x24_W1H2;
			engCharSpacing = engCharSpacingInCSV;
			customizerFont = font12x24_W1H2;
			chiDishNameFont = allowChineseFontW2H2;
			chiCharSpacing = chiCharSpacingInCSV;
			chiCustomizerFont = allowChineseFontW1H2;
			orderItemTotalFont = font12x24_W1H1;
			lineSpacing = lineSpacingPrtOrdInCSV;
		}
	}

	private String getCancelledString(PrintOrderNoPriceType printOrderNoPriceType) {
		logger.finest("Entered");
		switch (printOrderNoPriceType) {
		case PRINT_ORDER_NO_PRICE:
			return CANCELLED;
		case PRINT_ORDER_NO_PRICE_IN_CHINESE:
			return CANCELLED_IN_CHINESE;
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2:
			return CANCELLED_ENGLISH_AND_CHINESE;
		default:
			throw new RuntimeException("Unhandled printOrderNoPriceType=" + printOrderNoPriceType);
		}
	}

	private String getCustomizerName(PrintOrderNoPriceType printOrderNoPriceType, Customizer customizer, Cart cart) {
		logger.finest("Entered");
		String customizerChineseName = "";
		String customizerName = customizer.getName();
		Dish dish = Client.menu.getActiveDish(customizer.getName());
		if (dish != null) {
			customizerChineseName = dish.getChineseName();
		} else {
			customizerChineseName = customizerName;
		}

		// default customizer is in English. All customizer (English, Chinese,
		// engAndChi,
		// engAndChiIn2Line) are printed in one line
		switch (printOrderNoPriceType) {
		case PRINT_ORDER_NO_PRICE:
			break;
		case PRINT_ORDER_NO_PRICE_IN_CHINESE:
			if (dish != null) {
				customizerName = customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2:
			if (dish != null) {
				customizerName = customizerName + " " + customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		default:
			throw new RuntimeException("Unhandled printOrderNoPriceType=" + printOrderNoPriceType);
		}

		return customizerName;
	}

	private String getExchangerName(PrintOrderNoPriceType printOrderNoPriceType, Dish exchangerDish) {
		logger.finest("Entered");
		switch (printOrderNoPriceType) {
		case PRINT_ORDER_NO_PRICE:
			return exchangerDish.getShortName();
		case PRINT_ORDER_NO_PRICE_IN_CHINESE:
			return exchangerDish.getChineseName();
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2:
			return exchangerDish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printOrderNoPriceType=" + printOrderNoPriceType);
		}
	}

	private String getSubdishName(PrintOrderNoPriceType printOrderNoPriceType, Subdish subdish, Cart cart) {
		logger.finest("Entered");
		Dish dish = Client.menu.getActiveDish(subdish.getName());
		switch (printOrderNoPriceType) {
		case PRINT_ORDER_NO_PRICE:
			return dish.getShortName();
		case PRINT_ORDER_NO_PRICE_IN_CHINESE:
			return dish.getChineseName();
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_NO_PRICE_IN_ENGLISH_AND_CHINESE_2:
			return dish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printOrderNoPriceType=" + printOrderNoPriceType);
		}
	}
}
