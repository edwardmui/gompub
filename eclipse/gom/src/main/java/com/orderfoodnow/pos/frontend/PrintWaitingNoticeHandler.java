package com.orderfoodnow.pos.frontend;

import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.OrderStatus;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PaymentType;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.order.DeliveryInfo;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.ToGoInfo;
//import com.orderfoodnow.pos.shared.staff.Employee;

public class PrintWaitingNoticeHandler extends PrinterBase {
	public PrintWaitingNoticeHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public void print(Client clientGui, Cart cart, Calculator calculator) {
		logger.finest("Entered");
		if (cart.getOrderItemTray().getOrderItemCount() == 0) {
			clientGui.setWarningFeedback("Please select an order to notify the kitchen");
			return;
		}

		if (cart.getOrder().isVoided()) {
			clientGui.setWarningFeedback("Order has been voided. No waiting notice was printed");
			return;
		}

		Order order = cart.getOrder();
		ToGoInfo toGoInfo = cart.getToGoInfo();
		String formattedString;

		try {
			out.write(init);
			out.write(font09x17_W1H1);
			out.write("Check#: ".getBytes());
			out.write(font12x24_W2H1);
			out.write((order.getCheckNumber() + '\n').getBytes());
			out.write(font09x17_W1H1);
			out.write("Order#: ".getBytes());
			out.write(font12x24_W2H2);
			out.write(String.valueOf(order.getOrderNumber()).getBytes());
			out.write(font09x17_W1H1);
			out.write("  time:  ".getBytes());
			out.write(font12x24_W2H2);
			out.write((Util.formatTimeNoSeconds(System.currentTimeMillis()) + '\n').getBytes());
			out.write(init);

			if (toGoInfo.getCustomerName().isEmpty() == false) {
				out.write(font09x17_W1H1);
				out.write("Name:  ".getBytes());
				out.write(font12x24_W2H2);
				out.write((toGoInfo.getCustomerName() + '\n').getBytes());
			}

			if (toGoInfo.getPhoneNumber().isEmpty() == false) {
				out.write(font09x17_W1H1);
				out.write("Phone: ".getBytes());
				out.write(font12x24_W2H1);
				out.write((toGoInfo.getFormattedPhoneNumber() + '\n').getBytes());
			}

			DeliveryInfo deliveryInfo = cart.getDeliveryInfo();
			if (order.getType() == OrderType.DELIVERY && deliveryInfo.getAddress().isEmpty() == false) {
				out.write(font09x17_W1H1);
				out.write("Addr:  ".getBytes());
				out.write(font09x17_W1H1);
				out.write((deliveryInfo.getAddress() + '\n').getBytes());
			}

			formattedString = String.format("     Total:%10.2f\n", calculator.getTotal() / 100.0);
			out.write(init);
			out.write(font12x24_W2H1);
			out.write(formattedString.getBytes());
			out.write(font12x24_W1H1);

			int totalTendered = 0;
			for (PaymentType paymentType : PaymentType.values()) {
				if (cart.getPaymentTray().getPaymentCount(paymentType) > 0) {
					int totalTenderedByType = cart.getPaymentTray().getTotalTendered(paymentType);
					totalTendered += totalTenderedByType;
					formattedString = String.format("%20s:%19.2f\n", paymentType.getDisplayName(),
							totalTenderedByType/100.0);
					out.write(formattedString.getBytes());
				}
			}

			if (totalTendered > 0) {
				formattedString = String.format("%20s:%19.2f\n", "Total Tendered", totalTendered/100.0);
				out.write(formattedString.getBytes());
			}

			int balanceDue = calculator.getBalanceDue();
			if (balanceDue > 0) {
				formattedString = String.format("%s:$%6.2f\n", "BALANCE DUE", balanceDue/100.0);
				out.write(font12x24_W2H2);
				out.write(formattedString.getBytes());
			} else if (balanceDue == 0) {
				formattedString = String.format("%20s:%19s\n", "CHANGE", "0.00");
				out.write(formattedString.getBytes());
			} else {
				formattedString = String.format("%20s:%20.2f-\n", "CHANGE", -balanceDue/100.0);
				out.write(formattedString.getBytes());
			}
			out.write(init);

			if (order.getStatus() == OrderStatus.PAID) {
				out.write(font12x24_W2H1);
				List<String> paidWithPaymentTypes = new ArrayList<>();
				for (PaymentType paymentType : PaymentType.values()) {
					if (cart.getPaymentTray().getPaymentCount(paymentType) > 0) {
						paidWithPaymentTypes.add(paymentType.getDisplayName());
					}
				}
				out.write(("Paid with: " + String.join(", ", paidWithPaymentTypes)).getBytes());
			}

			// @formatter:off
			// Uncomment these lines to print the staff's name on the waiting notice to be
			// more customer friendly.
			// Employee takenByEmployee = Client.getEmployee(cart.getOrder().getTakenById());
			// String takenByNickname = takenByEmployee == null ? "" : takenByEmployee.getNickname();
			// out.write(("Your server today was " + takenByNickname).getBytes());
			// @formatter:on

			out.write("\n\n\n\n\n".getBytes());
			out.write(cut);
			out.write("\n".getBytes());

			sendOutputStreamToPrinter();

			clientGui.setInfoFeedback("Waiting Notice printed to " + location);
		} catch (Exception e) {
			logger.warning("Print Waiting Notice failed. error: " + e.getMessage());
			clientGui.setWarningFeedback("Waiting Notice was not printed. Check printer and try again...");
		}
	}
}
