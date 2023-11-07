package com.orderfoodnow.pos.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.order.Customizer;
import com.orderfoodnow.pos.shared.order.OrderItem;

public class CustomizerTray implements Serializable {
	private List<Customizer> customizers = new ArrayList<>();

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CustomizerTray.class.getName());

	private boolean isValidFormat(String orderItemIdCustomizerIdString) {
		logger.finest("Entered");
		if (orderItemIdCustomizerIdString == null) {
			return false;
		}

		orderItemIdCustomizerIdString = orderItemIdCustomizerIdString.trim();
		if (orderItemIdCustomizerIdString.isEmpty()) {
			return false;
		}

		// The string format is x.y where x is the orderItemId, y is the customizerId
		if (orderItemIdCustomizerIdString.indexOf('.') == -1) {
			return false;
		}

		// The period match any character in regular expression, need to escape it.
		// The first \ is to escape the second \ which is escaping the period
		String[] orderItemIdAndCustomizerIdArray = orderItemIdCustomizerIdString.split("\\.");

		if (orderItemIdAndCustomizerIdArray.length < 1) {
			return false;
		}

		return true;
	}

	public List<Customizer> getCustomizers() {
		return customizers;
	}

	public void setCustomizer(List<Customizer> customizers) {
		this.customizers = customizers;
	}

	public void addCustomizer(Customizer customizer) {
		logger.finest("Entered");
		customizers.add(customizer);
	}

	public void deleteIf(OrderItem orderItem) {
		logger.finest("Entered");
		customizers.removeIf(customizer -> customizer.getOrderItemId() == orderItem.getOrderItemId());
	}

	public Customizer getCustomizer(String orderItemIdCustomizerIdString) {
		logger.finest("Entered");
		if (isValidFormat(orderItemIdCustomizerIdString) == false) {
			return null;
		}

		String[] orderItemIdAndCustomizerIdArray = orderItemIdCustomizerIdString.split("\\.");

		// minus 1 to adjust for 1 based displaying and 0 id numbering
		int orderItemId = Integer.valueOf(orderItemIdAndCustomizerIdArray[0]) - 1;
		int customizerId = Integer.valueOf(orderItemIdAndCustomizerIdArray[1]) - 1;

		for (Customizer customizer : customizers) {
			if (customizer.getOrderItemId() == orderItemId && customizer.getCustomizerId() == customizerId) {
				return customizer;
			}
		}

		return null;
	}

	public void deleteCustomizer(Customizer customizer) {
		logger.finest("Entered");
		if (customizer != null) {
			customizers.remove(customizer);
		}
	}

	public void setIf(OrderItem orderItem, int newOrderItemId) {
		logger.finest("Entered");
		for (Customizer customizer : customizers) {
			if (customizer.getOrderItemId() == orderItem.getOrderItemId()) {
				customizer.setOrderItemId(newOrderItemId);
			}
		}
	}

	public boolean hasCustomizer(OrderItem orderItem) {
		logger.finest("Entered");
		if (orderItem.isVoided() == false && customizers != null) {
			for (Customizer customizer : customizers) {
				if (customizer.getOrderItemId() == orderItem.getOrderItemId()) {
					return true;
				}
			}
		}

		return false;
	}

	public List<Customizer> getCustomizers(OrderItem orderItem) {
		logger.finest("Entered");
		List<Customizer> orderItemSpecificCustomizers = new ArrayList<>();
		if (orderItem.isVoided() == false && customizers != null) {
			for (Customizer customizer : customizers) {
				if (customizer.getOrderItemId() == orderItem.getOrderItemId()) {
					orderItemSpecificCustomizers.add(customizer);
				}
			}
		}

		return orderItemSpecificCustomizers;
	}

	public int getCustomizerTotal(OrderItem orderItem) {
		logger.finest("Entered");
		int customizerTotal = 0;
		if (orderItem.isVoided() == false && customizers != null) {
			for (Customizer customizer : customizers) {
				if (customizer.getOrderItemId() == orderItem.getOrderItemId()
						&& customizer.getStatus() == SubOrderItemStatus.VALID) {
					customizerTotal += customizer.getPrice();
				}
			}
		}

		return customizerTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customizers == null) ? 0 : customizers.hashCode());
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
		CustomizerTray other = (CustomizerTray) obj;
		if (customizers == null) {
			if (other.customizers != null) {
				return false;
			}
		} else if (!customizers.equals(other.customizers)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return CustomizerTray.class.getSimpleName() +
				"[" +
				"customizers=" + customizers.toString() +
				"]";
		// @formatter:on
	}
}
