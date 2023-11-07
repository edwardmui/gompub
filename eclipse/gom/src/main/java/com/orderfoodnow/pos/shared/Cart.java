package com.orderfoodnow.pos.shared;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.CustomizerTable;
import com.orderfoodnow.pos.shared.order.DeliveryInfo;
import com.orderfoodnow.pos.shared.order.DineInInfo;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.ExchangerTable;
import com.orderfoodnow.pos.shared.order.Order;
import com.orderfoodnow.pos.shared.order.OrderItem;
import com.orderfoodnow.pos.shared.order.OrderItemTable;
import com.orderfoodnow.pos.shared.order.Payment;
import com.orderfoodnow.pos.shared.order.PaymentTable;
import com.orderfoodnow.pos.shared.order.ToGoInfo;

public class Cart implements Serializable, Comparable<Cart> {

	private Order order = new Order();
	private OrderItemTray orderItemTray = new OrderItemTray();
	private CustomizerTray customizerTray = new CustomizerTray();
	private ExchangerTray exchangerTray = new ExchangerTray();
	private PaymentTray paymentTray = new PaymentTray();

	private ToGoInfo toGoInfo = new ToGoInfo();
	private DineInInfo dineInInfo = new DineInInfo();
	private DeliveryInfo deliveryInfo = new DeliveryInfo();

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Cart.class.getName());

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public OrderItemTray getOrderItemTray() {
		return orderItemTray;
	}

	public void setOrderItemTray(OrderItemTray orderItemTray) {
		this.orderItemTray = orderItemTray;
	}

	public CustomizerTray getCustomizerTray() {
		return customizerTray;
	}

	public void setCustomizerTray(CustomizerTray customizerTray) {
		this.customizerTray = customizerTray;
	}

	public ExchangerTray getExchangerTray() {
		return exchangerTray;
	}

	public void setExchangerTray(ExchangerTray exchangerTray) {
		this.exchangerTray = exchangerTray;
	}

	public PaymentTray getPaymentTray() {
		return paymentTray;
	}

	public void setPaymentTray(PaymentTray paymentTray) {
		this.paymentTray = paymentTray;
	}

	public ToGoInfo getToGoInfo() {
		return toGoInfo;
	}

	public void setToGoInfo(ToGoInfo toGoInfo) {
		this.toGoInfo = toGoInfo;
	}

	public DeliveryInfo getDeliveryInfo() {
		return deliveryInfo;
	}

	public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
		this.deliveryInfo = deliveryInfo;
	}

	public DineInInfo getDineInInfo() {
		return dineInInfo;
	}

	public void setDineInInfo(DineInInfo dineInInfo) {
		this.dineInInfo = dineInInfo;
	}

	public boolean isNew() {
		return order.getOrderId() == 0;
	}

	private void insertOrderComponents(Connection connection) throws SQLException {
		logger.finest("Entered");
		int orderId = order.getOrderId();
		List<OrderItem> orderItems = orderItemTray.getOrderItems();
		for (OrderItem orderItem : orderItems) {
			orderItem.setOrderId(orderId);
			orderItem.insert(connection);
			logger.fine("Inserted: " + orderItem);
		}

		List<Customizer> customizers = customizerTray.getCustomizers();
		for (Customizer customizer : customizers) {
			customizer.setOrderId(orderId);
			customizer.insert(connection);
			logger.fine("Inserted: " + customizer);
		}

		List<Exchanger> exchangers = exchangerTray.getExchangers();
		for (Exchanger exchanger : exchangers) {
			exchanger.setOrderId(orderId);
			exchanger.insert(connection);
			logger.fine("Inserted: " + exchanger);
		}

		List<Payment> payments = paymentTray.getPayments();
		for (Payment payment : payments) {
			payment.setOrderId(orderId);
			payment.insert(connection);
			logger.fine("Inserted: " + payment);
		}
	}

	private void deleteOrderIdKeyed(Connection connection) throws SQLException {
		logger.finest("Entered");
		int orderId = order.getOrderId();

		int orderItemRowCount = OrderItemTable.deleteAllOf(connection, orderId);
		logger.fine("Deleted orderItem row count: " + orderItemRowCount + ". orderId=" + orderId);

		int customizerRowCount = CustomizerTable.deleteAllOf(connection, orderId);
		logger.fine("Deleted customizer row count: " + customizerRowCount + ". orderId=" + orderId);

		int exchangerRowCount = ExchangerTable.deleteAllOf(connection, orderId);
		logger.fine("Deleted exchanger row count: " + exchangerRowCount + ". orderId=" + orderId);

		int paymentRowCount = PaymentTable.deleteAllOf(connection, orderId);
		logger.fine("Deleted payment row count: " + paymentRowCount + ". orderId=" + orderId);
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		order.insert(connection);

		// After order.insert() done above,
		// the order has a valid orderId from database that OrderComponents
		// can use the newly obtained orderId to insert into db
		insertOrderComponents(connection);

		OrderType orderType = order.getType();
		int orderId = order.getOrderId();
		switch (orderType) {
		case DINE_IN:
			dineInInfo.setOrderId(orderId);
			dineInInfo.insert(connection);
			break;
		case DELIVERY:
			toGoInfo.setOrderId(orderId);
			toGoInfo.insert(connection);
			deliveryInfo.setOrderId(orderId);
			deliveryInfo.insert(connection);
			break;
		case PHONE_IN:
			toGoInfo.setOrderId(orderId);
			toGoInfo.insert(connection);
			break;
		case WALK_IN:
			if (toGoInfo.getPhoneNumber().isEmpty() == false) {
				toGoInfo.setOrderId(orderId);
				toGoInfo.insert(connection);
			}
			break;
		default:
			throw new RuntimeException("Unexpected orderType: " + orderType);
		}
	}

	public void deleteAllOrderItemChildren(OrderItem orderItem) {
		logger.finest("Entered");
		customizerTray.deleteIf(orderItem);
		exchangerTray.deleteIf(orderItem);
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		order.update(connection);
		logger.finer("Updated order first. " + order.toString());

		// delete then insert to perform update
		deleteOrderIdKeyed(connection);
		insertOrderComponents(connection);

		int orderId = order.getOrderId();
		OrderType orderType = order.getType();
		switch (orderType) {
		case DINE_IN:
			if (dineInInfo.equals(DineInInfo.readById(connection, orderId)) == false) {
				dineInInfo.update(connection);
			}
			break;
		case DELIVERY:
			if (toGoInfo.equals(ToGoInfo.readById(connection, orderId)) == false) {
				toGoInfo.update(connection);
			}
			if (deliveryInfo.equals(DeliveryInfo.readById(connection, orderId)) == false) {
				deliveryInfo.update(connection);
			}
			break;
		case PHONE_IN:
			if (toGoInfo.equals(ToGoInfo.readById(connection, orderId)) == false) {
				toGoInfo.update(connection);
			}
			break;
		case WALK_IN:
			if (toGoInfo.getPhoneNumber().isEmpty() == false) {
				if (toGoInfo.equals(ToGoInfo.readById(connection, orderId)) == false) {
					toGoInfo.update(connection);
				}
			}
			break;
		default:
			throw new RuntimeException("Unexpected orderType: " + orderType);
		}
	}

	public boolean isEmpty() {
		logger.finest("Entered");
		return getOrderItemTray().getOrderItemCount() == 0;
	}

	public void handleCustomerArrival(long currentTime) {
		logger.finest("Entered");
		if (getToGoInfo().getArrivalTime() == 0) {
			getToGoInfo().setArrivalTime(currentTime);
		}

		if (getToGoInfo().getCustomerStatus() == CustomerStatus.ORDERED) {
			getToGoInfo().setCustomerStatus(CustomerStatus.ARRIVED);
		}
	}

	public void resetCustomerArrival() {
		logger.finest("Entered");
		getToGoInfo().setArrivalTime(0);
		getToGoInfo().setCustomerStatus(CustomerStatus.ORDERED);
	}

	@Override
	public int compareTo(Cart thatCart) {
		logger.finest("Entered");
		return this.getOrder().compareTo(thatCart.getOrder());
	}

	public static Comparator<Cart> getCustomerArrivalTimeComparator() {
		logger.finest("Entered");
		return new Comparator<Cart>() {
			@Override
			public int compare(Cart cart1, Cart cart2) {
				long t1 = cart1.getToGoInfo().getArrivalTime();
				long t2 = cart2.getToGoInfo().getArrivalTime();
				if (t1 == t2) {
					return 0;
				} else if (t1 > t2) {
					return 1;
				} else {
					return -1;
				}
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deliveryInfo == null) ? 0 : deliveryInfo.hashCode());
		result = prime * result + ((dineInInfo == null) ? 0 : dineInInfo.hashCode());
		result = prime * result + ((customizerTray == null) ? 0 : customizerTray.hashCode());
		result = prime * result + ((exchangerTray == null) ? 0 : exchangerTray.hashCode());
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		result = prime * result + ((orderItemTray == null) ? 0 : orderItemTray.hashCode());
		result = prime * result + ((paymentTray == null) ? 0 : paymentTray.hashCode());
		result = prime * result + ((toGoInfo == null) ? 0 : toGoInfo.hashCode());
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
		Cart other = (Cart) obj;
		if (deliveryInfo == null) {
			if (other.deliveryInfo != null) {
				return false;
			}
		} else if (!deliveryInfo.equals(other.deliveryInfo)) {
			return false;
		}
		if (dineInInfo == null) {
			if (other.dineInInfo != null) {
				return false;
			}
		} else if (!dineInInfo.equals(other.dineInInfo)) {
			return false;
		}
		if (customizerTray == null) {
			if (other.customizerTray != null) {
				return false;
			}
		} else if (!customizerTray.equals(other.customizerTray)) {
			return false;
		}
		if (exchangerTray == null) {
			if (other.exchangerTray != null) {
				return false;
			}
		} else if (!exchangerTray.equals(other.exchangerTray)) {
			return false;
		}
		if (order == null) {
			if (other.order != null) {
				return false;
			}
		} else if (!order.equals(other.order)) {
			return false;
		}
		if (orderItemTray == null) {
			if (other.orderItemTray != null) {
				return false;
			}
		} else if (!orderItemTray.equals(other.orderItemTray)) {
			return false;
		}
		if (paymentTray == null) {
			if (other.paymentTray != null) {
				return false;
			}
		} else if (!paymentTray.equals(other.paymentTray)) {
			return false;
		}
		if (toGoInfo == null) {
			if (other.toGoInfo != null) {
				return false;
			}
		} else if (!toGoInfo.equals(other.toGoInfo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Cart.class.getSimpleName() + 
				"[" +
				"toGoInfo=" + toGoInfo + 
				", order=" + order + 
				", deliveryInfo=" + deliveryInfo + 
				", dineInInfo=" + dineInInfo + 
				", orderItemTray=" + orderItemTray + 
				", customizerTray=" + customizerTray + 
				", exchangerTray=" + exchangerTray + 
				", paymentTray=" + paymentTray + 
				"]";
		// @formatter:on
	}
}
