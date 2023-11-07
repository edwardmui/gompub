package com.orderfoodnow.pos.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.order.OrderItem;

public class OrderItemTray implements Serializable {
	protected List<OrderItem> orderItems = new ArrayList<>();

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(OrderItemTray.class.getName());

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public List<OrderItem> getOrderItems(boolean voided) {
		logger.finest("Entered");
		List<OrderItem> unvoidedOrderItems = new ArrayList<>();
		for (OrderItem orderItem : orderItems) {
			if (orderItem.isVoided() == voided) {
				unvoidedOrderItems.add(orderItem);
			}
		}

		return unvoidedOrderItems;
	}

	public boolean containsUnvoidedDishShortName(String dishShortName) {
		logger.finest("Entered");
		for (OrderItem orderItem : getOrderItems(false)) {
			if (dishShortName.equals(orderItem.getDishShortName())) {
				return true;
			}
		}

		return false;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}

	public int getOrderItemCount() {
		return orderItems.size();
	}

	public int getOrderItemQuantity() {
		int orderItemQuantity = 0;
		for (OrderItem orderItem : orderItems) {
			orderItemQuantity += orderItem.getOrderItemQuantity();
		}

		return orderItemQuantity;
	}

	public OrderItem getOrderItem(String orderItemColVal) {
		logger.finest("Entered");
		if (orderItemColVal.isEmpty()) {
			return null;
		}

		int periodInd = orderItemColVal.indexOf('.');
		if (periodInd == -1) {
			return null;
		}

		String orderItemIdStr = orderItemColVal.substring(0, periodInd).trim();
		int orderItemId;

		try {
			orderItemId = Integer.parseInt(orderItemIdStr);
		} catch (NumberFormatException e) {
			return null;
		}

		if (orderItemId == -1 || orderItemId == 0) {
			return null;
		}

		// adjust for 1-based
		return getOrderItemAt(orderItemId - 1);
	}

	public OrderItem getOrderItemAt(int index) {
		return orderItems.get(index);
	}

	public int getVoidedOrderItemCount() {
		logger.finest("Entered");
		int count = 0;
		for (OrderItem orderItem : orderItems) {
			if (orderItem.isVoided()) {
				++count;
			}
		}
		return count;
	}

	public void addOrderItem(Dish dish, DishSize dishSize, int quantity, Cart cart) {
		logger.finest("Entered");
		CustomizerTray customizerTray = cart.getCustomizerTray();
		ExchangerTray exchangerTray = cart.getExchangerTray();
		int dishId = dish.getDishId();
		for (OrderItem orderItem : orderItems) {
			if (dishId == orderItem.getDishId()) {
				// Don't add to existing orderItem that has customizers or exchanger associated
				// with because adding an order item should go as it own entry rather than
				// combined.
				if (orderItem.isVoided() == false && customizerTray.hasCustomizer(orderItem) == false
						&& exchangerTray.hasExchanger(orderItem) == false) {
					orderItem.addQuantity(dishSize, quantity);
					return;
				}
			}
		}

		OrderItem orderItem = new OrderItem(dish, dishSize, quantity);
		orderItem.setOrderItemId(orderItems.size());
		orderItems.add(orderItem);
	}

	public void deleteOrderItem(OrderItem orderItem, Cart cart) {
		logger.finest("Entered");
		cart.deleteAllOrderItemChildren(orderItem);

		orderItems.removeIf(oi -> oi.getOrderItemId() == orderItem.getOrderItemId()); // safer approach

		CustomizerTray customizerTray = cart.getCustomizerTray();
		ExchangerTray exchangerTray = cart.getExchangerTray();

		int id = 0; // reset all id to have consecutive orderItemId been displayed
		for (OrderItem oi : orderItems) {
			customizerTray.setIf(oi, id);
			exchangerTray.setIf(oi, id);
			oi.setOrderItemId(id++);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orderItems == null) ? 0 : orderItems.hashCode());
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
		OrderItemTray other = (OrderItemTray) obj;
		if (orderItems == null) {
			if (other.orderItems != null) {
				return false;
			}
		} else if (!orderItems.equals(other.orderItems)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return OrderItemTray.class.getSimpleName() +
				"[" +
				"orderItems=" + orderItems.toString() +
				"]";
		// @formatter:on
	}
}
