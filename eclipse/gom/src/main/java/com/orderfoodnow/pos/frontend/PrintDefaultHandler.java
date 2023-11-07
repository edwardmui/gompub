package com.orderfoodnow.pos.frontend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.print.PrintService;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PaymentType;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.staff.Employee;

public class PrintDefaultHandler extends PrinterBase {
	public PrintDefaultHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public String getPrinterName() {
		return printService.getName();
	}

	public boolean print(Client clientGui, PermissionRequiredType reportType, List<Cart> carts) {
		logger.finest("Entered");
		switch (reportType) {
		case PRINT_SUMMARY:
			return printSummary(clientGui, carts);
		case PRINT_DETAIL:
			return printDetail(clientGui, carts);
		case PRINT_REPORT:
			return printReport(clientGui, carts);
		default:
			throw new RuntimeException("Unhandled reportType: " + reportType);
		}
	}

	public boolean printSummary(Client clientGui, List<Cart> carts) {
		logger.finest("Entered");
		try {
			out.write(init);
			out.write(font09x17_W1H1);

			// Always print All Orders section header as to response to request even there's
			// no orders
			out.write(("\tDate:" + new Date() + '\n').getBytes());
			out.write("\n".getBytes());
			out.write("   Columns: 1=number of orders, 2=order number,\n".getBytes());
			out.write("   Columns: 3=check number, 4=order total\n".getBytes());
			out.write("   c=Cash, r=Credit Card, k=Check\n".getBytes());
			out.write("   m=Multipayment, p=Partial Paid, u=unpaid, v=Void\n".getBytes());

			// All order section
			out.write(font12x24_W2H2);
			out.write(" -------------------\n".getBytes());
			out.write("  All Orders:\n".getBytes());
			out.write(font09x17_W1H1);
			addOrderTotalSectionToOutStream(carts);
			out.write("\n".getBytes());

			for (OrderType orderType : OrderType.values()) {
				List<Cart> tmpCarts = new ArrayList<>();
				for (Cart cart : carts) {
					if (cart.getOrder().getType() == orderType) {
						tmpCarts.add(cart);
					}
				}

				switch (orderType) {
				case PHONE_IN:
				case WALK_IN:
					if (tmpCarts.size() > 0) {
						out.write(font12x24_W2H2);
						out.write(" ===================\n".getBytes());
						out.write(("  " + orderType.getDisplayName() + " Orders:\n").getBytes());
						out.write(font09x17_W1H1);
						addOrderTotalSectionToOutStream(tmpCarts);
						out.write("\n".getBytes());
					}
					break;

				case DELIVERY:
					// Print one section for each driver, and a section for non-specified driver
					List<Employee> drivers = new ArrayList<>();
					List<Cart> unspecifiedDriverCarts = new ArrayList<>();
					for (Cart cart : tmpCarts) {
						if (cart.getDeliveryInfo().isDriverValid()) {
							int driverId = cart.getDeliveryInfo().getDriverId();
							Employee driver = Client.idToEmployee.get(driverId);
							if (drivers.contains(driver) == false) {
								drivers.add(driver);
							}
						} else {
							unspecifiedDriverCarts.add(cart);
						}
					}
					Collections.sort(drivers); // sort by employee nickname alphabetically

					for (Employee driver : drivers) {
						int ordersTotal = 0;
						int deliveryChargeTotal = 0;
						List<Cart> perDriverCarts = new ArrayList<>();
						for (Cart cart : tmpCarts) {
							Calculator calculator = new Calculator(cart, Client.menu);
							if (cart.getDeliveryInfo().getDriverId() == driver.getEmployeeId()) {
								deliveryChargeTotal += cart.getDeliveryInfo().getDeliveryCharge();
								ordersTotal += calculator.getTotal();
								perDriverCarts.add(cart);
							}
						}
						int salesTotal = ordersTotal - deliveryChargeTotal;

						out.write(font09x17_W1H1);
						out.write("     Driver: ".getBytes());
						out.write(font12x24_W2H1);
						out.write((driver.getNickname() + "\n").getBytes());
						out.write(font09x17_W1H1);
						addOrderTotalSectionToOutStream(perDriverCarts);
						out.write(("      food: " + String.format("%8.2f\n", salesTotal/100.0)).getBytes());
						out.write(("     D-chg: " + String.format("%8.2f\n", deliveryChargeTotal/100.0)).getBytes());
						out.write(font09x17_W1H1);
						out.write("      --------------------------------------------\n".getBytes());
					}

					if (unspecifiedDriverCarts.size() > 0) {
						int ordersTotal = 0;
						int deliveryChargeTotal = 0;
						for (Cart cart : unspecifiedDriverCarts) {
							Calculator calculator = new Calculator(cart, Client.menu);
							deliveryChargeTotal += cart.getDeliveryInfo().getDeliveryCharge();
							ordersTotal += calculator.getTotal();
						}
						int salesTotal = ordersTotal - deliveryChargeTotal;
						out.write(font09x17_W1H1);
						out.write("     Driver: ".getBytes());
						out.write(font12x24_W2H1);
						out.write(("Unspecified\n").getBytes());
						out.write(font09x17_W1H1);
						addOrderTotalSectionToOutStream(unspecifiedDriverCarts);
						out.write(("      food: " + String.format("%8.2f\n", salesTotal/100.0)).getBytes());
						out.write(("     D-chg: " + String.format("%8.2f\n", deliveryChargeTotal/100.0)).getBytes());
						out.write(font09x17_W1H1);
						out.write("      --------------------------------------------\n".getBytes());
					}
					break;

				case DINE_IN:
					// Print one section for each server, and a section for non-specified server
					List<Employee> servers = new ArrayList<>();
					List<Cart> unspecifiedServerCarts = new ArrayList<>();
					for (Cart cart : tmpCarts) {
						if (cart.getDineInInfo().isServerValid()) {
							int serverId = cart.getDineInInfo().getServerId();
							Employee server = Client.idToEmployee.get(serverId);
							if (servers.contains(server) == false) {
								servers.add(server);
							}
						} else {
							unspecifiedServerCarts.add(cart);
						}
					}
					Collections.sort(servers); // sort by employee nickname alphabetically

					for (Employee server : servers) {
						int ordersTotal = 0;
						int tipTotal = 0;
						List<Cart> perServerCarts = new ArrayList<>();
						for (Cart cart : tmpCarts) {
							if (cart.getDineInInfo().getServerId() == server.getEmployeeId()) {
								tipTotal += cart.getDineInInfo().getTip();
								Calculator calculator = new Calculator(cart, Client.menu);
								ordersTotal += calculator.getTotal();
								perServerCarts.add(cart);
							}
						}
						int salesTotal = ordersTotal - tipTotal;

						out.write(font09x17_W1H1);
						out.write("     Server: ".getBytes());
						out.write(font12x24_W2H1);
						out.write((server.getNickname() + "\n").getBytes());
						out.write(font09x17_W1H1);
						addOrderTotalSectionToOutStream(perServerCarts);
						out.write(("      food: " + String.format("%8.2f\n", salesTotal/100.0)).getBytes());
						out.write(("      tips: " + String.format("%8.2f\n", tipTotal/100.0)).getBytes());
						out.write(font09x17_W1H1);
						out.write("      --------------------------------------------\n".getBytes());
					}

					if (unspecifiedServerCarts.size() > 0) {
						int ordersTotal = 0;
						int tipTotal = 0;
						for (Cart cart : unspecifiedServerCarts) {
							tipTotal += cart.getDineInInfo().getTip();
							Calculator calculator = new Calculator(cart, Client.menu);
							ordersTotal += calculator.getTotal();
						}
						int salesTotal = ordersTotal - tipTotal;
						out.write(font09x17_W1H1);
						out.write("     Server: ".getBytes());
						out.write(font12x24_W2H1);
						out.write(("Unspecified\n").getBytes());
						out.write(font09x17_W1H1);
						addOrderTotalSectionToOutStream(unspecifiedServerCarts);
						out.write(("      food: " + String.format("%8.2f\n", salesTotal/100.0)).getBytes());
						out.write(("      tips: " + String.format("%8.2f\n", tipTotal/100.0)).getBytes());
						out.write(font09x17_W1H1);
						out.write("      --------------------------------------------\n".getBytes());
					}
					break;

				default:
					throw new RuntimeException("Unsupport orderType=" + orderType);
				}
			}

			out.write(font09x17_W1H1);
			sendOutputStreamToPrinter();
			printReport(clientGui, carts);

			clientGui.clearInputText();
			clientGui.setInfoFeedback("Order SUMMARY printed to local printer");
		} catch (Exception e) {
			logger.warning("Print SUMMARY failed. error: " + e.getMessage());
			clientGui.setWarningFeedback("Order SUMMARY was not printed. Check printer and try again...");
			return false;
		}

		return true;
	}

	public boolean printDetail(Client clientGui, List<Cart> carts) {
		logger.finest("Entered");
		try {
			for (Cart cart : carts) {
				Order order = cart.getOrder();
				int orderNumber = order.getOrderNumber();
				String statusStr = "";
				if (order.isVoided()) {
					statusStr = "v";
				}
				String orderedTime = Util.formatTime(order.getOrderedTime());
				out.write(("Order Number: " + orderNumber + statusStr + "     at " + orderedTime + '\n').getBytes());

				List<OrderItem> orderItems = cart.getOrderItemTray().getOrderItems();
				Collections.sort(orderItems, OrderItem.getDishComparator(Client.menu));

				Calculator calculator = new Calculator(cart, Client.menu);
				for (OrderItem orderItem : orderItems) {
					// Truncate shortName start at max char. First number is min length, second
					// number is max allowed
					String line = String.format("%30.30s %10s\n", Client.dishes[orderItem.getDishId()].getShortName(),
							calculator.getOrderItemTotalInString(orderItem));
					out.write((line).getBytes());
				}

				int total = calculator.getTotal();
				int tax = calculator.getTaxAmount();
				int cost = total - tax;
				out.write((String.format("Cost: %4.2f    Tax: %4.2f    Total: %4.2f", cost/100.0, tax/100.0, total/100.0)).getBytes());

				out.write("----------------------------------------\n".getBytes());
			}

			sendOutputStreamToPrinter();
			printReport(clientGui, carts);

			clientGui.clearInputText();
			clientGui.setInfoFeedback("Order DETAIL printed to local printer");
		} catch (Exception e) {
			logger.warning("Print DETAIL failed. error: " + e.getMessage());
			clientGui.setWarningFeedback("Order DETAIL was not printed. Check printer and try again...");
			return false;
		}

		return true;
	}

	public boolean printReport(Client clientGui, List<Cart> carts) {
		logger.finest("Entered");
		int pickupTotal = 0;
		int dineInTotal = 0;
		int dineInSoftDrinkTotal = 0;
		int dineInAlcoholDrinkTotal = 0;
		int deliveryTotal = 0;
		int deliveryChargeTotal = 0;

		int voidTotal = 0;
		int cashTotal = 0;
		int cardTotal = 0;
		int checkTotal = 0;
		int grandTotal = 0;
		for (Cart cart : carts) {
			Order order = cart.getOrder();
			OrderType type = order.getType();
			Calculator calculator = new Calculator(cart, Client.menu);
			int orderTotal = calculator.getTotal();

			if (order.isVoided()) {
				voidTotal += orderTotal;
			} else {

				if (type == OrderType.DINE_IN) {
					dineInTotal += orderTotal;
					dineInSoftDrinkTotal += calculator.getSoftDrinkTotal();
					dineInAlcoholDrinkTotal += calculator.getAlcoholDrinkTotal();
				} else if (type == OrderType.DELIVERY) {
					deliveryTotal += orderTotal;
					deliveryChargeTotal += cart.getDeliveryInfo().getDeliveryCharge();
				} else if (type == OrderType.PHONE_IN || type == OrderType.WALK_IN) {
					pickupTotal += orderTotal;
				}

				cashTotal += order.getCashPaidAmount();
				cardTotal += cart.getPaymentTray().getTotalTendered(PaymentType.CREDIT_CARD);
				checkTotal += cart.getPaymentTray().getTotalTendered(PaymentType.CHECK);
				grandTotal += orderTotal;
			}
		}

		try {
			int unpaidTotal = grandTotal - cardTotal - checkTotal - cashTotal;
			out.write(font12x24_W2H2);
			out.write(" ===================\n".getBytes());
			out.write(font09x17_W1H1);
			if (pickupTotal > 0) {
				out.write(("     Phone-in & Walk-in Total: " + String.format("%8.2f\n", pickupTotal/100.0)).getBytes());
				out.write("\n".getBytes());
			}
			if (deliveryTotal > 0) {
				out.write(("     DELIVERY           Total: " + String.format("%8.2f\n", deliveryTotal/100.0)).getBytes());
				out.write(("         Food           Total: " + String.format("%8.2f\n", (deliveryTotal - deliveryChargeTotal)/100.0)).getBytes());
				out.write(("         Fee            Total: " + String.format("%8.2f\n", deliveryChargeTotal/100.0)).getBytes());
				out.write("\n".getBytes());
			}
			if (dineInTotal > 0) {
				out.write(("     Dine-in            Total: " + String.format("%8.2f\n", dineInTotal/100.0)).getBytes());
				out.write(("        Food            Total: " + String.format("%8.2f\n", (dineInTotal - dineInSoftDrinkTotal - dineInAlcoholDrinkTotal)/100.0)).getBytes());
				out.write(("        Soft Drink      Total: " + String.format("%8.2f\n", dineInSoftDrinkTotal/100.0)).getBytes());
				out.write(("        Alcohol Drink   Total: " + String.format("%8.2f\n", dineInAlcoholDrinkTotal/100.0)).getBytes());
				out.write("\n".getBytes());
			}
			out.write("     ===================================\n".getBytes());
			out.write(("     Void  Total: " + String.format("%8.2f\n", voidTotal/100.0)).getBytes());
			out.write("     (Void Total is not part of any other total)\n".getBytes());
			out.write("\n".getBytes());
			out.write(("     Cash  Total: " + String.format("%8.2f\n", cashTotal/100.0)).getBytes());
			out.write(("     Card  Total: " + String.format("%8.2f\n", cardTotal/100.0)).getBytes());
			out.write(("     Check Total: " + String.format("%8.2f\n", checkTotal/100.0)).getBytes());
			out.write(("     Unpaid Total:" + String.format("%8.2f\n", unpaidTotal/100.0)).getBytes());
			out.write("     =========================\n".getBytes());

			out.write("    (Grand Total includes DELIVERY Charges)\n".getBytes());
			out.write(underline2dotWidth);
			out.write("| G-total |  Sales  | Dine-in |   out   |  D-chg  |\n".getBytes());
			out.write(("|" + String.format("%8.2f", (dineInTotal + pickupTotal + deliveryTotal)/100.0) + " |"
					+ String.format("%8.2f", (dineInTotal + pickupTotal + deliveryTotal - deliveryChargeTotal)/100.0) + " |"
					+ String.format("%8.2f", dineInTotal/100.0) + " |"
					+ String.format("%8.2f", (pickupTotal + deliveryTotal - deliveryChargeTotal)/100.0) + " |"
					+ String.format("%8.2f", deliveryChargeTotal/100.0) + " |\n").getBytes());
			out.write("\n".getBytes());
			out.write("| G-total |  Cash   |  card   |  check  |   tip   |\n".getBytes());
			out.write(("|" + String.format("%8.2f", (dineInTotal + pickupTotal + deliveryTotal)/100.0) + " |" + // this total should includes delivery charge and credit card, check, tips
					String.format("%8.2f", cashTotal/100.0) + " |" + String.format("%8.2f", cardTotal/100.0) + " |"
					+ String.format("%8.2f", checkTotal/100.0) + " |" + "         |\n").getBytes());

			out.write("\n".getBytes());
			if (carts.size() > 0) {
				out.write(("\tChecks # " + carts.get(0).getOrder().getOrderId()).getBytes());
				out.write(("   to   " + carts.get(carts.size() - 1).getOrder().getOrderId() + '\n').getBytes());
			}

			out.write(("\tDate:" + (new Date()).toString() + '\n').getBytes());
			out.write(init);
			out.write("     Grand Total: ".getBytes());
			out.write(font12x24_W2H2);
			out.write(("$" + String.format("%8.2f\n", grandTotal/100.0)).getBytes());
			out.write(font09x17_W1H1);
			out.write("\n".getBytes());
			out.write("     Card  Total: $".getBytes());
			out.write("____________________________\n".getBytes());
			out.write("\n".getBytes());
			out.write(font09x17_W1H1);
			out.write("     Check Total: $".getBytes());
			out.write("____________________________\n".getBytes());
			out.write("\n".getBytes());
			out.write("     ==========================================\n".getBytes());
			out.write("\n".getBytes());
			out.write(init);
			out.write("     Cash  Total: $________________\n".getBytes());
			out.write("\n".getBytes());
			out.write(font09x17_W1H1);
			out.write(" Exp 1: _______________________________________\n".getBytes());
			out.write("\n".getBytes());
			out.write(" Exp 2: _______________________________________\n".getBytes());
			out.write("\n".getBytes());
			out.write(" Exp 3: _______________________________________\n".getBytes());
			out.write("\n".getBytes());
			out.write("     ==========================================\n".getBytes());
			out.write("\n".getBytes());
			out.write(init);
			out.write(" Cash  On Hand: \n".getBytes());
			out.write("\n".getBytes());
			out.write("     $_____________________________\n".getBytes());
			out.write("\n\n\n\n\n".getBytes());
			out.write("\n\n\n\n\n".getBytes());
			out.write(font09x17_W1H1);
			out.write("\n\n\n\n\n".getBytes());
			out.write(cut);

			sendOutputStreamToPrinter();

			clientGui.clearInputText();
			clientGui.setInfoFeedback("Order REPORT printed to local printer");
		} catch (Exception e) {
			logger.warning("Print REPORT failed. error: " + e.getMessage());
			clientGui.setWarningFeedback("Order REPORT was not printed. Check printer and try again...");
			return false;
		}

		return true;
	}

	private void addOrderTotalSectionToOutStream(List<Cart> carts) throws IOException {
		logger.finest("Entered");
		// @formatter:off
		//Left then right printing
		/*
		int cartCounter = 0;
		int leftThenRightCartCount = carts.size();
		for (int i = 0; i < leftThenRightCartCount; i = i + 2) {
			String line;
			if (i + 1 < leftThenRightCartCount) {
				line = String.format("%s | %s", formattedString(++cartCounter, carts.get(i)),
						formattedString(++cartCounter, carts.get(i + 1)));
			} else {
				line = formattedString(++cartCounter, carts.get(i));
			}

			line += '\n';
			out.write((line).getBytes());
		}
		*/
		// @formatter:on

		// Down and over printing.
		int cartCount = carts.size();
		boolean odd = (cartCount % 2 == 1); // odd number of orders
		int rowCount = odd ? cartCount / 2 + 1 : cartCount / 2;
		for (int i = 0; i < rowCount; ++i) {
			String line;
			int rightCounter = i + rowCount;
			if (rightCounter < cartCount) {
				line = String.format("%s  |  %s", formattedString(i + 1, carts.get(i)),
						formattedString(rightCounter + 1, carts.get(rightCounter)));
			} else {
				line = formattedString(i + 1, carts.get(i)); // last line if odd number of orders
			}

			line += '\n';
			out.write((line).getBytes());
		}

		// easier logic to get the total in a new loop instead of the 'half' loop above
		int cartsTotal = 0;
		for (Cart cart : carts) {
			Calculator calculator = new Calculator(cart, Client.menu);
			cartsTotal += cart.getOrder().isVoided() ? 0 : calculator.getTotal();
		}
		String totalLine = String.format("     Total: %8.2f", cartsTotal/100.0);
		out.write(totalLine.getBytes());
	}

	private String formattedString(int cartCount, Cart cart) {
		logger.finest("Entered");
		Order order = cart.getOrder();
		String orderStatusIndicator = order.isVoided() ? "v" : "";
		Calculator calculator = new Calculator(cart, Client.menu);
		String formattedString = String.format("%3d. %4d  %5s %7.2f%c%s", cartCount, order.getOrderNumber(),
				order.getCheckNumber(), calculator.getTotal()/100.0, calculator.getPayMethod(),
				orderStatusIndicator);

		return formattedString;
	}
}
