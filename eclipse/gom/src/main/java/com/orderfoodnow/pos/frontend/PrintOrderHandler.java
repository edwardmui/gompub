package com.orderfoodnow.pos.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.print.PrintService;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.CouponVariantType;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.Subdish;
import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.CustomizerTable;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.staff.Employee;

public class PrintOrderHandler extends PrinterBase {
	public PrintOrderHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public void print(PrintOrderType printOrderType, FontSizeType fontSizeType, Client clientGui, Cart cart,
			Calculator calculator) {
		logger.finest("Entered");
		setFont(fontSizeType);
		try {
			out.write(init);
			out.write(alignCenter);

			if (printOrderType == PrintOrderType.PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE) {
				out.write(font12x24_W2H2);
				out.write("===ON HOLD===\n".getBytes());
				out.write(font09x17_W1H1);
				out.write("(see requested time)\n\n".getBytes());
			}

			if (printOrderNumberOnOrder) {
				printOrderNumber(cart);
			}

			out.write("\n\n".getBytes());

			printRestaurantLogo();

			out.write(disAllowChineseFontWxHx);
			out.write(init); // clear the alignCenter command

			printOrderTypeHeading(cart);

			switch (cart.getOrder().getType()) {
			case DELIVERY:
				out.write(init); // clear the alignCenter command
				out.write(font12x24_W1H1);
				if (cart.getDeliveryInfo().isDriverValid()) {
					Employee driver = Client.idToEmployee.get(cart.getDeliveryInfo().getDriverId());
					out.write(("\tDriver: " + driver.getNickname() + '\n').getBytes());
				}

				if (cart.getDeliveryInfo().getDeliveryCharge() == 0) {
					out.write("THIS DELIVERY HAS NO DELIVERY CHARGE\n".getBytes());
				}

				if (cart.getDeliveryInfo().isUpdateCustomerProfileNeeded()) {
					out.write("Customer profile needs update\n".getBytes());
				}
			default:
				break;
			}

			if (cart.getToGoInfo().getPhoneNumber().isEmpty() == false) {
				printPhoneNumber(cart);
			}

			printOrderTypeSpecifics(cart);

			Order order = cart.getOrder();
			if (order.getType() == OrderType.DELIVERY) {
				printDeliverySpecifics(cart);

				// Credit Card Number will only printed on the Print Order copy
				String creditCartNumber = cart.getToGoInfo().getCreditCardNumber();
				if (creditCartNumber.isEmpty() == false) {

					int creditCartNumberLength = creditCartNumber.length();
					if (creditCartNumberLength > 4) {
						out.write(font09x17_W1H1);
						out.write("Card:  ".getBytes());
						out.write(font12x24_W1H1);
						out.write(
								(creditCartNumber.substring(creditCartNumberLength - 4, creditCartNumberLength) + '\n')
										.getBytes());
					}
				}
			}

			out.write(init);
			out.write("\n".getBytes());
			out.write(font12x24_W2H1);

			printDayOfTheWeek(cart);

			out.write(font09x17_W1H1);
			out.write(font12x24_W2H1);
			if (printCheckNumberOnOrder) {
				out.write((" Ck:" + order.getCheckNumberPrefix()).getBytes());
				out.write(font12x24_W2H1);
				out.write(order.getCheckNumber().getBytes());
				out.write(font09x17_W1H1);
			}

			printDateAndTime(cart);

			out.write(init); // clear the alignCenter

			if (cart.getToGoInfo().getRequestedTime() != 0) {
				printRequestedTime(cart);
			}

			// printWidth up to 42 columns
			boolean includeVoided = false;
			List<OrderItem> unvoidedOrderItems = calculator.getOrderItemsCombinedWithCondiments(includeVoided);
			Collections.sort(unvoidedOrderItems, OrderItem.getDishComparator(Client.menu));
			logger.fine("printOrderType=" + printOrderType + " unvoidedOrderItems=" + unvoidedOrderItems);
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
					String printingDishName = printOrderType == PrintOrderType.PRINT_ORDER ? dish.getShortName()
							: dish.getEnglishAndChineseNames();
					String chineseName = dish.getChineseName();

					switch (printOrderType) {
					case PRINT_ORDER_IN_CHINESE:
						printingDishName = chineseName;
						// fall thru
					case PRINT_ORDER:
					case PRINT_ORDER_IN_ENGLISH_AND_CHINESE:
					case PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE:
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
					case PRINT_ORDER_IN_ENGLISH_AND_CHINESE_2:
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
						throw new RuntimeException("Unhandled printOrderType=" + printOrderType);
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
										+ getSubdishName(printOrderType, subdish, cart) + '\n')
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
									+ getSubdishName(printOrderType, subdish, cart) + '\n').getBytes(CHINESE_ENCODING));
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
							out.write(getCustomizerName(printOrderType, customizer, cart).getBytes(CHINESE_ENCODING));
							out.write(font09x17_W1H1);
							out.write(customizer.getFormattedPrice().getBytes()); // only W1H1
							out.write(customizerFont);
							out.write(")\n".getBytes());
							customizerGrandTotal += customizer.getPrice();
							break;
						case VOIDED:
							// Mixed-in the voided and valid as it needs the orderItem context. Voided is
							// expected to be rare.
							String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
									+ getCancelledString(printOrderType) + " *** "
									+ getCustomizerName(printOrderType, customizer, cart);
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
							out.write(getExchangerName(printOrderType, exchangerDish).getBytes(CHINESE_ENCODING));
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
									+ getCancelledString(printOrderType) + " *** "
									+ getExchangerName(printOrderType, exchangerDish);
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
				String dishName = printOrderType == PrintOrderType.PRINT_ORDER ? dish.getShortName()
						: dish.getEnglishAndChineseNames();
				String largeQuantityPlusPadding = voidedOrderItem.getLargeQuantityWithPadding();
				String smallQuantityPlusPadding = voidedOrderItem.getSmallQuantityWithPadding();
				String voidedName = largeQuantityPlusPadding + smallQuantityPlusPadding + "*** "
						+ getCancelledString(printOrderType) + " *** " + dishName;
				printedVoided(voidedName, voidedOrderItem.getVoidedById(), voidedOrderItem.getVoidedTime());
			}

			if (cart.getOrder().getNote().isEmpty() == false) {
				printOrderNote(cart);
			}

			// condiment will be printed on the carry out type in the Print Order copy only
			if (order.getType() != OrderType.DINE_IN) {
				Map<String, Float> orderCondimentNameToQuantity = calculator
						.getOrderCondimentNameToQuantityLessMatchedOrderItem();
				for (String condimentName : orderCondimentNameToQuantity.keySet()) {
					float condimentQuantity = orderCondimentNameToQuantity.get(condimentName);
					out.write(font12x24_W1H1);
					out.write(("   " + Util.formatDouble(condimentQuantity, 1, 0) + " " + condimentName + '\n').getBytes());
				}

				for (OrderItem orderItem : unvoidedOrderItems) {
					List<Customizer> customizers = cart.getCustomizerTray().getCustomizers(orderItem);
					for (Customizer customizer : customizers) {
						if (customizer.getCustomizerDishId() <= CustomizerTable.NON_MENU_CUSTOMIZER_ID) {
							String name = customizer.getName();
							out.write(("Add condiment for: '" + name + "'\n").getBytes());
						}
					}
				}
			}

			printOrderTotal(cart, calculator);

			printOrderPayment(cart, calculator);

			if (order.isVoided()) {
				printVoidedOrder(cart, calculator);
			} else {
				// customer note will be printed on the to go orders only
				if ((order.getType() != OrderType.DINE_IN) && (cart.getToGoInfo().getNote().isEmpty() == false)) {
					out.write(font09x17_W1H1);
					out.write("Customer Note:".getBytes());
					out.write(font12x24_W1H1);
					out.write(("   " + cart.getToGoInfo().getNote() + '\n').getBytes());
				}

				// printing Bag_x_ of _y_ template for packer to write in for a carry out order.
				if (order.getType() != OrderType.DINE_IN) {
					if (Configuration.getprintBagXofYtemplate()) {
						out.write(font12x24_W2H2);
						out.write("Bag___ of ___:".getBytes());
						out.write("\n\n".getBytes());
					} else {
						logger.finer(" Skipping printing printBagXofYtemplate  due to 'printBagXofYtemplate': no is specified in conf.yaml file.");
					}
				}

				// message will be printed on the Dine-in order only
				if (order.getType() == OrderType.DINE_IN) {
					if (order.getDiscountAmount() > 0) {
						out.write(font12x24_W1H1);
						out.write("Gratuity should be based on the subtotal\n".getBytes());
					}

					printGratuityGuideline(cart, calculator);

					out.write(alignCenter);
					out.write(font12x24_W1H1);
					out.write("\n".getBytes());
					List<String> diningEndingText = Configuration.getRestaurantDiningEndingText();
					for (String line : diningEndingText) {
						out.write((line + '\n').getBytes());
					}

					List<String> operationHours = Configuration.getRestaurantOperationHours();
					for (String line : operationHours) {
						out.write((line + '\n').getBytes());
					}
					out.write(font09x17_W1H1);
				}
			}

			out.write("\n\n\n\n".getBytes());
			out.write(cut);
			out.write("\n".getBytes());
			out.write(init);
			out.write(printerAlarm);
			out.write(init); // to clear the openDrawer function
			out.flush();

			// coupon will be printed only if this is a carry out order.
			if (order.getType() != OrderType.DINE_IN) {
				for (CouponVariantType couponVariantType : CouponVariantType.values()) {
					if (Configuration.getCouponPrint(couponVariantType)) {
						PrintCouponHandler.print(couponVariantType, out);
					} else {
						logger.finer(" Skipping printing " + couponVariantType
								+ " due to 'print': no is specified in conf.yaml file.");
					}
				}
			}

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
		case SMALL:
			dishNameFont = font12x24_W1H1;
			engCharSpacing = engCharSpacingInCSV;
			customizerFont = font12x24_W1H1;
			chiDishNameFont = disAllowChineseFontWxHx;
			chiCharSpacing = chiCharSpacingInCSV;
			chiCustomizerFont = disAllowChineseFontWxHx;
			orderItemTotalFont = font09x17_W1H1;
			lineSpacing = lineSpacingPrtOrdInCSV;
			break;
		default:
			// set to medium
			dishNameFont = font12x24_W1H2;
			engCharSpacing = engCharSpacingInCSV;
			customizerFont = font12x24_W1H2;
			chiDishNameFont = allowChineseFontW1H2;
			chiCharSpacing = chiCharSpacingInCSV;
			chiCustomizerFont = allowChineseFontW1H2;
			orderItemTotalFont = font12x24_W1H1;
			lineSpacing = lineSpacingPrtOrdInCSV;
			break;
		}
	}

	private String getCancelledString(PrintOrderType printOrderType) {
		logger.finest("Entered");
		switch (printOrderType) {
		case PRINT_ORDER:
			return CANCELLED;
		case PRINT_ORDER_IN_CHINESE:
			return CANCELLED_IN_CHINESE;
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE_2:
		case PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE:
			return CANCELLED_ENGLISH_AND_CHINESE;
		default:
			throw new RuntimeException("Unhandled printOrderType=" + printOrderType);
		}
	}

	private String getCustomizerName(PrintOrderType printOrderType, Customizer customizer, Cart cart) {
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
		switch (printOrderType) {
		case PRINT_ORDER:
			break;
		case PRINT_ORDER_IN_CHINESE:
			if (dish != null) {
				customizerName = customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE_2:
		case PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE:
			if (dish != null) {
				customizerName = customizerName + " " + customizerChineseName;
			} else {
				customizerName = FeConstDefs.SEE_ENGLISH_IN_CHINESE + "-->" + customizerName;
			}
			break;
		default:
			throw new RuntimeException("Unhandled printOrderType=" + printOrderType);
		}

		return customizerName;
	}

	private String getExchangerName(PrintOrderType printOrderType, Dish exchangerDish) {
		logger.finest("Entered");
		switch (printOrderType) {
		case PRINT_ORDER:
			return exchangerDish.getShortName();
		case PRINT_ORDER_IN_CHINESE:
			return exchangerDish.getChineseName();
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE_2:
		case PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE:
			return exchangerDish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printOrderType=" + printOrderType);
		}
	}

	private String getSubdishName(PrintOrderType printOrderType, Subdish subdish, Cart cart) {
		logger.finest("Entered");
		Dish dish = Client.menu.getActiveDish(subdish.getName());
		switch (printOrderType) {
		case PRINT_ORDER:
			return dish.getShortName();
		case PRINT_ORDER_IN_CHINESE:
			return dish.getChineseName();
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE:
		case PRINT_ORDER_IN_ENGLISH_AND_CHINESE_2:
		case PRINT_ORDER_HELD_CONFIRMATION_IN_ENGLISH_AND_CHINESE:
			return dish.getEnglishAndChineseNames();
		default:
			throw new RuntimeException("Unhandled printOrderType=" + printOrderType);
		}
	}
}
