package com.orderfoodnow.pos.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.OrderItem;

public class ExchangerTray implements Serializable {
	private List<Exchanger> exchangers = new ArrayList<>();

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ExchangerTray.class.getName());

	private boolean isValidFormat(String orderItemIdExchangeIdString) {
		logger.finest("Entered");
		if (orderItemIdExchangeIdString == null) {
			return false;
		}

		orderItemIdExchangeIdString = orderItemIdExchangeIdString.trim();
		if (orderItemIdExchangeIdString.isEmpty()) {
			return false;
		}

		// The string format is x.y where x is the orderItemId, y is the exchangeId,
		if (orderItemIdExchangeIdString.indexOf('.') == -1) {
			return false;
		}

		// The y portion EX appended to distinguish from customizer
		if (orderItemIdExchangeIdString.indexOf(Exchanger.EX) == -1) {
			return false;
		}

		// The period match any character in regular expression, need to escape it.
		// The first \ is to escape the second \ which is escaping the period
		String[] orderItemIdAndExchangeIdArray = orderItemIdExchangeIdString.split("\\.");

		if (orderItemIdAndExchangeIdArray.length < 1) {
			return false;
		}

		return true;
	}

	public List<Exchanger> getExchangers() {
		return exchangers;
	}

	public void setExchangers(List<Exchanger> exchangers) {
		this.exchangers = exchangers;
	}

	public boolean isExchangerAlreadyAdded(Exchanger exchanger) {
		logger.finest("Entered");
		for (Exchanger existExchanger : exchangers) {
			if (existExchanger.isVoided() == false) {
				if (existExchanger.getOrderItemId() == exchanger.getOrderItemId()
						&& existExchanger.getExchangerDishId() == exchanger.getExchangerDishId()) {
					return true;
				}
			}
		}

		return false;
	}

	public void addExchanger(Exchanger exchanger) {
		logger.finest("Entered");
		exchangers.add(exchanger);
	}

	public void deleteIf(OrderItem orderItem) {
		logger.finest("Entered");
		exchangers.removeIf(exchanger -> exchanger.getOrderItemId() == orderItem.getOrderItemId());
	}

	public Exchanger getExchanger(String orderItemIdExchangerIdString) {
		logger.finest("Entered");
		if (isValidFormat(orderItemIdExchangerIdString) == false) {
			return null;
		}

		orderItemIdExchangerIdString = orderItemIdExchangerIdString.replace(Exchanger.EX, "");

		String[] orderItemIdAndExchangerIdArray = orderItemIdExchangerIdString.split("\\.");

		// minus 1 to adjust for 1 based displaying and 0 id numbering
		int orderItemId = Integer.valueOf(orderItemIdAndExchangerIdArray[0]) - 1;
		int exchangerId = Integer.valueOf(orderItemIdAndExchangerIdArray[1]) - 1;

		for (Exchanger exchanger : exchangers) {
			if (exchanger.getOrderItemId() == orderItemId && exchanger.getExchangerId() == exchangerId) {
				return exchanger;
			}
		}

		return null;
	}

	public void deleteExchanger(Exchanger exchanger) {
		logger.finest("Entered");
		if (exchanger != null) {
			exchangers.remove(exchanger);
		}
	}

	public void setIf(OrderItem orderItem, int newOrderItemId) {
		logger.finest("Entered");
		for (Exchanger exchanger : exchangers) {
			if (exchanger.getOrderItemId() == orderItem.getOrderItemId()) {
				exchanger.setOrderItemId(newOrderItemId);
			}
		}
	}

	public boolean hasExchanger(OrderItem orderItem) {
		logger.finest("Entered");
		if (orderItem.isVoided() == false && exchangers != null) {
			for (Exchanger exchanger : exchangers) {
				if (exchanger.getOrderItemId() == orderItem.getOrderItemId()) {
					return true;
				}
			}
		}

		return false;
	}

	public Exchanger getMatchedExchanger(OrderItem orderItem, String condimentName) {
		logger.finest("Entered");
		for (Exchanger exchanger : exchangers) {
			if (orderItem.getOrderItemId() != exchanger.getOrderItemId()) {
				continue;
			}

			String fromName = exchanger.getFromName();
			if (fromName != null && condimentName != null && fromName.equals(condimentName)
					&& exchanger.isVoided() == false) {
				return exchanger;
			}
		}

		return null;
	}

	public List<Exchanger> getExchangers(OrderItem orderItem) {
		logger.finest("Entered");
		List<Exchanger> orderItemSpecificExchangers = new ArrayList<>();
		if (orderItem.isVoided() == false && exchangers != null) {
			for (Exchanger exchanger : exchangers) {
				if (exchanger.getOrderItemId() == orderItem.getOrderItemId()) {
					orderItemSpecificExchangers.add(exchanger);
				}
			}
		}

		return orderItemSpecificExchangers;
	}

	public int getExchangerTotal(OrderItem orderItem, Dish[] dishes) {
		logger.finest("Entered");
		int exchangerTotal = 0;
		if (orderItem.isVoided() == false && exchangers != null) {
			for (Exchanger exchanger : exchangers) {
				int exchangerDishId = exchanger.getExchangerDishId();
				Dish exchangerDish = dishes[exchangerDishId];
				if (exchanger.getOrderItemId() == orderItem.getOrderItemId()
						&& exchanger.getStatus() == SubOrderItemStatus.VALID) {
					int total = orderItem.getLargeQuantity() * exchangerDish.getLargePrice()
							+ orderItem.getSmallQuantity() * exchangerDish.getSmallPrice();
					exchangerTotal += total;
				}
			}
		}

		return exchangerTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exchangers == null) ? 0 : exchangers.hashCode());
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
		ExchangerTray other = (ExchangerTray) obj;
		if (exchangers == null) {
			if (other.exchangers != null) {
				return false;
			}
		} else if (!exchangers.equals(other.exchangers)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return ExchangerTray.class.getSimpleName() +
				"[" +
				"exchangers=" + exchangers.toString() +
				"]";
		// @formatter:on
	}
}
