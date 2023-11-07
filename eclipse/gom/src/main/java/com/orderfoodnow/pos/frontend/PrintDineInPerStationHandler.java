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

public class PrintDineInPerStationHandler extends PrinterBase {
	public PrintDineInPerStationHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public void print(PrintDineInPerStationType printDineInPerStationType, FontSizeType fontSizeType, Client clientGui,
			Cart cart, Calculator calculator) {
		logger.finest("Entered");
		setFont(fontSizeType);
		try {
			out.write(init);
			out.write(alignCenter);

			if (printOrderNumberOnOrder) {
				printOrderNumber(cart);
			}

			int dishWorkstationId = (Integer) (Object) Configuration.getDineInPrinterLocationToPrinter().get(location)
					.get(ConfigKeyDefs.printerLocationToPrinter_dishWorkstationId);
			String dishWorkstationName = Configuration.getDineInPrinterLocationToPrinter().get(location)
					.get(ConfigKeyDefs.printerLocationToPrinter_dishWorkstationName);
			String dishWorkstationChineseName = Configuration.getDineInPrinterLocationToPrinter().get(location)
					.get(ConfigKeyDefs.printerLocationToPrinter_dishWorkstationChineseName);
			dishWorkstationName = dishWorkstationName == null ? "" : dishWorkstationName;
			dishWorkstationChineseName = dishWorkstationChineseName == null ? "" : dishWorkstationChineseName;
			switch (printDineInPerStationType) {
			case PRINT_DINE_IN_PER_STATION:
				out.write(font12x24_W2H2);
				out.write((dishWorkstationName + "\n\n").getBytes());
				break;

			case PRINT_DINE_IN_PER_STATTION_IN_CHINESE:
				out.write(allowChineseFontW2H2);
				out.write((dishWorkstationChineseName + "\n\n").getBytes(CHINESE_ENCODING));
				break;

			case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE:
			case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2:
				out.write(font12x24_W1H2);
				out.write(allowChineseFontW2H2);
				out.write((dishWorkstationName + " " + dishWorkstationChineseName + "\n\n").getBytes(CHINESE_ENCODING));
				break;

			default:
				throw new RuntimeException("Unhandled printDineInPerStationType=" + printDineInPerStationType);
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

			CouponDish unvoidedCouponDish = calculator.getCouponDish(false);
			Dish dish1 = unvoidedCouponDish == null ? null
					: Client.menu.getActiveDish(unvoidedCouponDish.getDish1Name());
			Dish dish2 = unvoidedCouponDish == null ? null
					: Client.menu.getActiveDish(unvoidedCouponDish.getDish2Name());
			logger.fine("couponDish:" + unvoidedCouponDish + "dish1:" + dish1 + "dish2:" + dish2);
			List<OrderItem> unvoidedOrderItemsWithSubstitutedCouponDish = calculator
					.getOrderItemsWithSubstitutedCouponDish(unvoidedCouponDish, dish1, dish2, false,
							Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.condiment));
			logger.fine("printDineInPerStationType=" + printDineInPerStationType
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
				} else if (dish.getDineInStationHasWork(dishWorkstationId)) {
					int largeQuantity = unvoidedOrderItem.getLargeQuantity();
					int smallQuantity = unvoidedOrderItem.getSmallQuantity();
					String largeQuantityPlusPadding = unvoidedOrderItem.getLargeQuantityWithPadding();
					String smallQuantityPlusPadding = unvoidedOrderItem.getSmallQuantityWithPadding();
					String printingDishName = printDineInPerStationType == PrintDineInPerStationType.PRINT_DINE_IN_PER_STATION
							? dish.getShortName()
							: dish.getEnglishAndChineseNames();
					String chineseName = dish.getChineseName();

					switch (printDineInPerStationType) {
					case PRINT_DINE_IN_PER_STATTION_IN_CHINESE:
						printingDishName = chineseName;
						// fall thru
					case PRINT_DINE_IN_PER_STATION:
					case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE:
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
					case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2:
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
						throw new RuntimeException("Unhandled printDineInPerStationType=" + printDineInPerStationType);
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
										+ getSubdishName(printDineInPerStationType, subdish, cart) + '\n')
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
									+ getSubdishName(printDineInPerStationType, subdish, cart) + '\n')
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
							out.write(getCustomizerName(printDineInPerStationType, customizer, cart)
									.getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(customizerFont);
							out.write(")\n".getBytes());
							break;
						case VOIDED:
							// Mix-in the voided and valid as it needs the orderItem context. Voided is
							// expected to be rare.
							// @formatter:off
							// String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
							//		+ getCancelledString(printDineInPerStationType) + " *** "
							//		+ getCustomizerName(printDineInPerStationType, customizer, cart);
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
							out.write(getExchangerName(printDineInPerStationType, exchangerDish)
									.getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(customizerFont);
							out.write(")\n".getBytes());
							break;
						case VOIDED:
							// Mix-in the voided and valid as it needs the orderItem context. Voided is
							// expected to be rare.
							// @formatter:off
							// String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
							//		+ getCancelledString(printDineInPerStationType) + " *** "
							//		+ getExchangerName(printDineInPerStationType, exchangerDish);
							//printedVoided(voidedName, exchanger.getVoidedById(), exchanger.getVoidedTime());
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
				if (dish.getDineInStationHasWork(dishWorkstationId)) {
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
					String dishName = printDineInPerStationType == PrintDineInPerStationType.PRINT_DINE_IN_PER_STATION
							? dish.getShortName()
							: dish.getEnglishAndChineseNames();
					String largeQuantityPlusPadding = voidedOrderItem.getLargeQuantityWithPadding();
					String smallQuantityPlusPadding = voidedOrderItem.getSmallQuantityWithPadding();
					String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
							+ getCancelledString(printDineInPerStationType) + " *** " + dishName;
					printedVoided(voidedName, voidedOrderItem.getVoidedById(), voidedOrderItem.getVoidedTime());
				}
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
		case MEDIUM:
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
			break;
		}
	}

	private String getCancelledString(PrintDineInPerStationType printDineInPerStationType) {
		logger.finest("Entered");
		switch (printDineInPerStationType) {
		case PRINT_DINE_IN_PER_STATION:
			return CANCELLED;
		case PRINT_DINE_IN_PER_STATTION_IN_CHINESE:
			return CANCELLED_IN_CHINESE;
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE:
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2:
			return CANCELLED_ENGLISH_AND_CHINESE;
		default:
			throw new RuntimeException("Unhandled printDineInPerStationType=" + printDineInPerStationType);
		}
	}

	private String getCustomizerName(PrintDineInPerStationType printDineInPerStationType, Customizer customizer,
			Cart cart) {
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
		switch (printDineInPerStationType) {
		case PRINT_DINE_IN_PER_STATION:
			break;
		case PRINT_DINE_IN_PER_STATTION_IN_CHINESE:
			if (dish != null) {
				customizerName = customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE:
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2:
			if (dish != null) {
				customizerName = customizerName + " " + customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		default:
			throw new RuntimeException("Unhandled printDineInPerStationType=" + printDineInPerStationType);
		}

		return customizerName;
	}

	private String getExchangerName(PrintDineInPerStationType printDineInPerStationType, Dish exchangerDish) {
		logger.finest("Entered");
		switch (printDineInPerStationType) {
		case PRINT_DINE_IN_PER_STATION:
			return exchangerDish.getShortName();
		case PRINT_DINE_IN_PER_STATTION_IN_CHINESE:
			return exchangerDish.getChineseName();
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE:
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2:
			return exchangerDish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printDineInPerStationType=" + printDineInPerStationType);
		}
	}

	private String getSubdishName(PrintDineInPerStationType printDineInPerStationType, Subdish subdish, Cart cart) {
		logger.finest("Entered");
		Dish dish = Client.menu.getActiveDish(subdish.getName());
		switch (printDineInPerStationType) {
		case PRINT_DINE_IN_PER_STATION:
			return dish.getShortName();
		case PRINT_DINE_IN_PER_STATTION_IN_CHINESE:
			return dish.getChineseName();
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE:
		case PRINT_DINE_IN_PER_STATION_IN_ENGLISH_AND_CHINESE_2:
			return dish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printDineInPerStationType=" + printDineInPerStationType);
		}
	}
}
