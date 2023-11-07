package com.orderfoodnow.pos.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.order.Payment;

public class PaymentTray implements Serializable {
	protected List<Payment> payments = new ArrayList<>();
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PaymentTray.class.getName());

	public void addPayment(Payment payment) {
		logger.finest("Entered");
		payment.setPaymentId(payments.size());
		payments.add(payment);
	}

	public boolean deletePayment(String paymentDisplayString) {
		logger.finest("Entered");
		if (paymentDisplayString == null) {
			return false;
		}

		paymentDisplayString = paymentDisplayString.trim();
		if (paymentDisplayString.isEmpty()) {
			return false;
		}

		// expected format is "Pymt paymentId" where paymentId is 1 based
		String[] stringArray = paymentDisplayString.split(" ");
		if (stringArray.length < 1) {
			return false;
		}

		int paymentId;
		try {
			paymentId = Integer.valueOf(stringArray[1]) - 1;
		} catch (Exception e) {
			return false;
		}

		return deletePayment(paymentId);
	}

	public boolean deletePayment(int paymentId) {
		logger.finest("Entered");
		return payments.removeIf(payment -> payment.getPaymentId() == paymentId);
	}

	public int getPaymentCount(PaymentType paymentType) {
		logger.finest("Entered");
		int count = 0;
		for (Payment payment : payments) {
			if (payment.getType() == paymentType) {
				count++;
			}
		}
		return count;
	}

	public int getTotalTendered() {
		logger.finest("Entered");
		int total = 0;
		for (Payment payment : payments) {
			total += payment.getAmount();
		}
		return total;
	}

	public int getTotalTendered(PaymentType paymentType) {
		logger.finest("Entered");
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getType() == paymentType)
				total += payment.getAmount();
		}
		return total;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((payments == null) ? 0 : payments.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PaymentTray other = (PaymentTray) obj;
		if (payments == null) {
			if (other.payments != null) {
				return false;
			}
		} else if (!payments.equals(other.payments)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return PaymentTray.class.getSimpleName() +
				"[" +
				"payments=" + payments.toString() +
				"]";
		// @formatter:on
	}
}
