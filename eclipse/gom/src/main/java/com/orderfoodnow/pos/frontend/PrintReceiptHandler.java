package com.orderfoodnow.pos.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.print.PrintService;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.Subdish;
import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.OrderItem;

public class PrintReceiptHandler extends PrinterBase {
	public static final String CUSTOMERCOPY = "Customer Copy";
	public static final String CUSTOMERCOPY_IN_CHINESE = "顾客单";

	public PrintReceiptHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public void print(PrintReceiptType printReceiptType, FontSizeType fontSizeType, Client clientGui, Cart cart,
			Calculator calculator) {
		logger.finest("Entered");
		setFont(fontSizeType);

		try {
			out.write(init);
			out.write(alignCenter);

			if (printOrderNumberOnReceipt) {
				printOrderNumber(cart);
			}

			switch (printReceiptType) {
			case PRINT_RECEIPT:
				out.write(font12x24_W2H1);
				out.write((CUSTOMERCOPY + "\n\n").getBytes());
				break;

			case PRINT_RECEIPT_IN_CHINESE:
				out.write(disAllowChineseFontWxHx);
				out.write((CUSTOMERCOPY_IN_CHINESE + "\n\n").getBytes(CHINESE_ENCODING));
				break;

			case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE:
			case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2:
				out.write(font12x24_W1H1);
				out.write(disAllowChineseFontWxHx);
				out.write((CUSTOMERCOPY + " " + CUSTOMERCOPY_IN_CHINESE + "\n\n").getBytes(CHINESE_ENCODING));
				break;

			default:
				throw new RuntimeException("Unhandled printReceiptType=" + printReceiptType);
			}

			printRestaurantLogo();

			out.write(disAllowChineseFontWxHx);
			out.write(init); // clear the alignCenter command

			printOrderTypeHeading(cart);

			if (cart.getToGoInfo().getPhoneNumber().isEmpty() == false) {
				printPhoneNumber(cart);
			}

			printOrderTypeSpecifics(cart);

			if (cart.getOrder().getType() == OrderType.DELIVERY) {
				printDeliverySpecifics(cart);
			}

			out.write(init);
			out.write("\n".getBytes());
			out.write(font12x24_W2H1);

			printDayOfTheWeek(cart);

			printDateAndTime(cart);

			if (cart.getToGoInfo().getRequestedTime() != 0) {
				printRequestedTime(cart);
			}

			// printWidth up to 42 columns
			List<OrderItem> unvoidedOrderItems = cart.getOrderItemTray().getOrderItems(false);
			Collections.sort(unvoidedOrderItems, OrderItem.getDishComparator(Client.menu));
			for (OrderItem unvoidedOrderItem : unvoidedOrderItems) {
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
					String orderItemTotalInString = calculator.getOrderItemTotalInString(unvoidedOrderItem);
					String printingDishName = printReceiptType == PrintReceiptType.PRINT_RECEIPT ? dish.getShortName()
							: dish.getEnglishAndChineseNames();
					String chineseName = dish.getChineseName();

					switch (printReceiptType) {
					case PRINT_RECEIPT_IN_CHINESE:
						printingDishName = chineseName;
						// fall thru
					case PRINT_RECEIPT:
					case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE:
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
							out.write((" $" + orderItemTotalInString + '\n').getBytes());
						} else {
							out.write((printingDishName + '\n').getBytes(CHINESE_ENCODING));
							out.write(defaultEngCharSpacingIs00); // clear the char spacing
							out.write(defaultChiCharSpacingIs00);
						}
						break;
					case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2:
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
							out.write(font09x17_W1H1);
							out.write((" $" + orderItemTotalInString + '\n').getBytes());

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
						throw new RuntimeException("Unhandled printReceiptType=" + printReceiptType);
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
										+ getSubdishName(printReceiptType, subdish, cart) + '\n')
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
									+ getSubdishName(printReceiptType, subdish, cart) + '\n')
											.getBytes(CHINESE_ENCODING));
						}
					}

					List<Customizer> customizers = cart.getCustomizerTray().getCustomizers(unvoidedOrderItem);
					int customizerGrandTotal = 0;
					for (Customizer customizer : customizers) {
						switch (customizer.getStatus()) {
						case VALID:
							out.write(customizerFont); // W1H1 or W1H2
							out.write(chiCustomizerFont);// W1H1 or W1H2
							out.write("        (".getBytes());
							out.write(getCustomizerName(printReceiptType, customizer, cart).getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(customizer.getFormattedPrice().getBytes()); // only W1H1
							out.write(customizerFont);
							out.write(")\n".getBytes());
							customizerGrandTotal += customizer.getPrice();
							break;
						case VOIDED:
							// Mix-in the voided and valid as it needs the orderItem context. Voided is
							// expected to be rare.
							String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
									+ getCancelledString(printReceiptType) + " *** "
									+ getCustomizerName(printReceiptType, customizer, cart);
							printedVoided(voidedName, customizer.getVoidedById(), customizer.getVoidedTime());
							break;
						}
					}

					List<Exchanger> exchangers = cart.getExchangerTray().getExchangers(unvoidedOrderItem);
					// the exchanger will not be printed, if the orderItem is voided
					int exchangerGrandTotal = 0;
					for (Exchanger exchanger : exchangers) {
						Dish exchangerDish = Client.dishes[exchanger.getExchangerDishId()];

						switch (exchanger.getStatus()) {
						case VALID:
							out.write(customizerFont); // W1H1 or W1H2
							out.write(chiCustomizerFont);// W1H1 or W1H2
							out.write("        (".getBytes());
							out.write(getExchangerName(printReceiptType, exchangerDish).getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(exchanger.getFormattedPrice(unvoidedOrderItem, exchangerDish).getBytes()); // only
																													// W1H1
							out.write(customizerFont);
							out.write(")\n".getBytes());
							exchangerGrandTotal = exchangerGrandTotal
									+ exchanger.getPrice(unvoidedOrderItem, exchangerDish);
							break;
						case VOIDED:
							// Mix-in the voided and valid as it needs the orderItem context. Voided is
							// expected to be rare.
							String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
									+ getCancelledString(printReceiptType) + " *** "
									+ getExchangerName(printReceiptType, exchangerDish);
							printedVoided(voidedName, exchanger.getVoidedById(), exchanger.getVoidedTime());
							break;
						default:
							throw new RuntimeException("Unhandled exchanger=" + exchanger.getStatus());
						}
					}

					int orderItemGrandTotal = calculator.getOrderItemTotal(unvoidedOrderItem) + customizerGrandTotal
							+ exchangerGrandTotal;
					String orderItemGrandTotalStr = "-";
					if (orderItemGrandTotal != 0) {
						orderItemGrandTotalStr = String.format("%4.2f", orderItemGrandTotal/100.0);
					}
					out.write(alignRight);
					out.write(orderItemTotalFont);
					out.write((orderItemGrandTotalStr + '\n').getBytes());
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
				String dishName = printReceiptType == PrintReceiptType.PRINT_RECEIPT ? dish.getShortName()
						: dish.getEnglishAndChineseNames();
				String largeQuantityPlusPadding = voidedOrderItem.getLargeQuantityWithPadding();
				String smallQuantityPlusPadding = voidedOrderItem.getSmallQuantityWithPadding();
				String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
						+ getCancelledString(printReceiptType) + " *** " + dishName;
				printedVoided(voidedName, voidedOrderItem.getVoidedById(), voidedOrderItem.getVoidedTime());
			}

			if (cart.getOrder().getNote().isEmpty() == false) {
				printOrderNote(cart);
			}

			if (cart.getToGoInfo().getNote().isEmpty() == false) {
				printCustomerNote(cart);
			}

			printOrderTotal(cart, calculator);

			printOrderPayment(cart, calculator);

			if (cart.getOrder().isVoided()) {
				printVoidedOrder(cart, calculator);
			} else {
				if ((cart.getOrder().getType() != OrderType.DINE_IN)
						&& (cart.getDeliveryInfo().getDrivingDirection().isEmpty() == false)) {
					printDrivingDirection(cart);
				}

				// Receipt ending text
				out.write(alignCenter);
				out.write(font12x24_W1H1);
				out.write("\n".getBytes());
				List<String> receiptEndingText = Configuration.getRestaurantReceiptEndingText();
				for (String line : receiptEndingText) {
					out.write((line + '\n').getBytes());
				}
				out.write(font09x17_W1H1);

				List<String> operationHours = Configuration.getRestaurantOperationHours();
				for (String line : operationHours) {
					out.write((line + '\n').getBytes());
				}
				out.write(font09x17_W1H1);
			}

			out.write("\n\n\n\n".getBytes());
			out.write(cut);
			out.write("\n".getBytes());
			out.write(init);
			out.write(printerAlarm);
			out.write(init);
			out.flush();

			sendOutputStreamToPrinter();
			clientGui.setInfoFeedback("Receipt printed to " + location);
		} catch (Exception e) {
			logger.warning("Print Receipt failed. error: " + e.getMessage());
			e.printStackTrace();
			clientGui.setWarningFeedback("Receipt was not printed. Check printer and try again...");
		}
	}

	private void setFont(FontSizeType fontSizeType) {
		logger.finest("Entered");
		switch (fontSizeType) {
		case MEDIUM:
			dishNameFont = font12x24_W1H2;
			customizerFont = font12x24_W1H2;
			chiDishNameFont = allowChineseFontW1H2;
			chiCustomizerFont = allowChineseFontW1H2;
			orderItemTotalFont = font12x24_W1H1;
			break;
		default:
			dishNameFont = font12x24_W1H1;
			customizerFont = font12x24_W1H1;
			chiDishNameFont = disAllowChineseFontWxHx;
			chiCustomizerFont = disAllowChineseFontWxHx;
			orderItemTotalFont = font09x17_W1H1;
			break;
		}
	}

	private String getCancelledString(PrintReceiptType printReceiptType) {
		logger.finest("Entered");
		switch (printReceiptType) {
		case PRINT_RECEIPT:
			return CANCELLED;
		case PRINT_RECEIPT_IN_CHINESE:
			return CANCELLED_IN_CHINESE;
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE:
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2:
			return CANCELLED_ENGLISH_AND_CHINESE;
		default:
			throw new RuntimeException("Unhandled printReceiptType=" + printReceiptType);
		}
	}

	private String getCustomizerName(PrintReceiptType printReceiptType, Customizer customizer, Cart cart) {
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
		// engAndChi, engAndChiIn2Line) are printed in one line
		switch (printReceiptType) {
		case PRINT_RECEIPT:
			break;
		case PRINT_RECEIPT_IN_CHINESE:
			if (dish != null) {
				customizerName = customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE:
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2:
			if (dish != null) {
				customizerName = customizerName + " " + customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		default:
			throw new RuntimeException("Unhandled printReceiptType=" + printReceiptType);
		}

		return customizerName;
	}

	private String getExchangerName(PrintReceiptType printReceiptType, Dish exchangerDish) {
		logger.finest("Entered");
		switch (printReceiptType) {
		case PRINT_RECEIPT:
			return exchangerDish.getShortName();
		case PRINT_RECEIPT_IN_CHINESE:
			return exchangerDish.getChineseName();
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE:
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2:
			return exchangerDish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printReceiptType=" + printReceiptType);
		}
	}

	private String getSubdishName(PrintReceiptType printReceiptType, Subdish subdish, Cart cart) {
		logger.finest("Entered");
		Dish dish = Client.menu.getActiveDish(subdish.getName());
		switch (printReceiptType) {
		case PRINT_RECEIPT:
			return dish.getShortName();
		case PRINT_RECEIPT_IN_CHINESE:
			return dish.getChineseName();
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE:
		case PRINT_RECEIPT_IN_ENGLISH_AND_CHINESE_2:
			return dish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printReceiptType=" + printReceiptType);
		}
	}
}
