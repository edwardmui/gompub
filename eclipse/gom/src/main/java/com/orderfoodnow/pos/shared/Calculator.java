package com.orderfoodnow.pos.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.menu.CouponDish;
import com.orderfoodnow.pos.shared.menu.Dish;
import com.orderfoodnow.pos.shared.menu.Menu;
import com.orderfoodnow.pos.shared.order.Exchanger;
import com.orderfoodnow.pos.shared.order.OrderItem;

public class Calculator {
	private Menu menu;
	private Cart cart;

	private static final Logger logger = Logger.getLogger(Calculator.class.getName());

	public Calculator(Cart cart, Menu menu) {
		logger.finest("Entered");
		this.menu = menu;
		this.cart = cart;
	}

	public static int roundToNearestNickel(int amount) {
		int base = amount / 10 * 10;
		int unit = amount - base;

		if (unit <= 2) {
			return base;
		} else if (unit <= 7) {
			return base + 5;
		} else {
			return base + 10;
		}
	}

	// This function models the kitchen queue time that's based on the order totals
	// and configurable queue time factors per interval
	public static int estimateKitchenQueueTimeMinutes(int[] allOrderIntervalTotals, int orderTotal) {
		logger.finest("Entered");
		int kitchenQueueTimeMinutes = Configuration.getDayOfTheWeekBaseKitchenQueueTimeMinutes();

		try {
			List<Double> kitchenQueueTimeFactors = Configuration.getKitchenQueueTimeFactors();
			for (int i = 0; i < allOrderIntervalTotals.length; ++i) {
				double kitchenQueueTimeFactor = kitchenQueueTimeFactors.get(i);
				logger.finer("kitchenQueueTimeFactor=" + kitchenQueueTimeFactor);
				if (kitchenQueueTimeFactor != 0) { // ok to compare double with not equal to 0 as no arithmetic has done
													// on the
					// double value. It is a straight read from config.yaml file
					double orderIntervalTotalFactoredInDollar = allOrderIntervalTotals[i] * kitchenQueueTimeFactor
							/ 100;
					logger.finer("orderIntervalTotalFactored=" + orderIntervalTotalFactoredInDollar);
					if (i == 0) {
						logger.finer("Calculation based on dollar. In cent current orderTotal=" + orderTotal);
						// add in the current order which is not committed to be counted in
						// allOrderIntervalTotals[]
						orderIntervalTotalFactoredInDollar += (orderTotal / 100);
					}
					logger.finer("orderIntervalTotalFactored=" + orderIntervalTotalFactoredInDollar);
					// need to be > 1 to avoid negative/NaN for intervalDelayInMinute due
					// Math.log(orderIntervalTotalFactored). Furthermore orderTotal of 1 dollar
					// is negligible for calculation.
					if (orderIntervalTotalFactoredInDollar > 1) {
						double intervalDelayInMinute = Math.log(5 * orderIntervalTotalFactoredInDollar)
								* Math.sqrt(orderIntervalTotalFactoredInDollar)
								/ Configuration.getRestaurantCapability() * 15;
						kitchenQueueTimeMinutes += intervalDelayInMinute;
						logger.finer("allOrderIntervalTotals[" + i + "]=" + allOrderIntervalTotals[i]
								+ " kitchenQueueTimeFactor=" + kitchenQueueTimeFactor + " intervalDelayInMinute="
								+ intervalDelayInMinute + " kitchenQueueTime=" + kitchenQueueTimeMinutes);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.fine("Exception msg=" + e.getMessage());
		}

		logger.finer("kitchenQueueTime=" + kitchenQueueTimeMinutes);

		return kitchenQueueTimeMinutes;
	}

	public Map<String, Float> getSumOfOrderItemCondimentNameToQuantity(OrderItem orderItem) {
		logger.finest("Entered");
		Map<String, List<Float>> orderItemCondimentNameToQuantities = menu.getDish(orderItem.getDishId())
				.getCondimentNameToQuantities();
		if (orderItemCondimentNameToQuantities == null || orderItem.isVoided()) {
			return null;
		}

		Map<String, Float> sumOfOrderItemCondimentNameToQuantity = new HashMap<>();
		for (String condimentName : orderItemCondimentNameToQuantities.keySet()) {
			List<Float> condimentQuantities = orderItemCondimentNameToQuantities.get(condimentName);
			float largeSizeQuantity = condimentQuantities.get(0);
			float smallSizeQuantity = condimentQuantities.get(1);
			float orderItemCondimentQuantity = largeSizeQuantity * orderItem.getLargeQuantity()
					+ smallSizeQuantity * orderItem.getSmallQuantity();
			Exchanger matchedExchanger = cart.getExchangerTray().getMatchedExchanger(orderItem, condimentName);
			if (matchedExchanger == null || matchedExchanger.isVoided()) {
				sumOfOrderItemCondimentNameToQuantity.put(condimentName, Float.valueOf(orderItemCondimentQuantity));
			} else {
				String toName = matchedExchanger.getToName();
				if (toName.equals(Exchanger.NONE)) {
					logger.fine(
							orderItem.getDishShortName() + ", exchange " + condimentName + " with " + Exchanger.NONE);
				} else if (matchedExchanger.getStatus() == SubOrderItemStatus.VALID) {
					logger.fine(orderItem.getDishShortName() + ", exchange " + condimentName + " with " + toName
							+ " for quantity=" + orderItemCondimentQuantity);
					sumOfOrderItemCondimentNameToQuantity.put(toName, Float.valueOf(orderItemCondimentQuantity));
				}
			}
		}

		return sumOfOrderItemCondimentNameToQuantity;
	}

	public Map<String, Float> getOrderCondimentNameToQuantity() {
		logger.finest("Entered");
		Map<String, Float> orderCondimentNameToQuantity = new HashMap<>();
		for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
			Map<String, Float> sumOfOrderItemCondimentNameToQuantity = getSumOfOrderItemCondimentNameToQuantity(
					orderItem);
			if (sumOfOrderItemCondimentNameToQuantity == null) {
				continue;
			}

			for (String condimentName : sumOfOrderItemCondimentNameToQuantity.keySet()) {
				float orderItemCondimentQuantity = sumOfOrderItemCondimentNameToQuantity.get(condimentName);
				if (orderCondimentNameToQuantity.containsKey(condimentName)) {
					float orderCondimentQuantity = orderCondimentNameToQuantity.get(condimentName);
					float newOrderCondimentQuantity = orderCondimentQuantity + orderItemCondimentQuantity;
					orderCondimentNameToQuantity.remove(condimentName);
					orderCondimentNameToQuantity.put(condimentName, Float.valueOf(newOrderCondimentQuantity));
				} else {
					orderCondimentNameToQuantity.put(condimentName, Float.valueOf(orderItemCondimentQuantity));
				}
			}
		}

		return orderCondimentNameToQuantity;
	}

	public Map<String, Float> getOrderCondimentNameToQuantityLessMatchedOrderItem() {
		logger.finest("Entered");
		Map<String, Float> orderCondimentNameToQuantityLessMatchedOrderItem = new HashMap<>();
		for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
			Map<String, Float> sumOfOrderItemCondimentNameToQuantity = getSumOfOrderItemCondimentNameToQuantity(
					orderItem);
			if (sumOfOrderItemCondimentNameToQuantity == null) {
				continue;
			}

			for (String condimentName : sumOfOrderItemCondimentNameToQuantity.keySet()) {
				if (cart.getOrderItemTray().containsUnvoidedDishShortName(condimentName)) {
					continue;
				}

				float orderItemCondimentQuantity = sumOfOrderItemCondimentNameToQuantity.get(condimentName);
				if (orderCondimentNameToQuantityLessMatchedOrderItem.containsKey(condimentName)) {
					float orderCondimentQuantity = orderCondimentNameToQuantityLessMatchedOrderItem.get(condimentName);
					float newOrderCondimentQuantity = orderCondimentQuantity + orderItemCondimentQuantity;
					orderCondimentNameToQuantityLessMatchedOrderItem.remove(condimentName);
					orderCondimentNameToQuantityLessMatchedOrderItem.put(condimentName,
							Float.valueOf(newOrderCondimentQuantity));
				} else {
					orderCondimentNameToQuantityLessMatchedOrderItem.put(condimentName,
							Float.valueOf(orderItemCondimentQuantity));
				}
			}
		}

		return orderCondimentNameToQuantityLessMatchedOrderItem;
	}

	public List<OrderItem> getOrderItemsCombinedWithCondiments(boolean includeVoided) {
		logger.finest("Entered");
		List<OrderItem> orderItemsCombinedWithCondiments = new ArrayList<>();
		for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
			if (includeVoided || orderItem.isVoided() == false) {
				OrderItem tmpOrderItem = new OrderItem(orderItem);
				Map<String, Float> orderCondimentNameToQuantity = getOrderCondimentNameToQuantity();
				Float orderCondimentQuantity = orderCondimentNameToQuantity.get(tmpOrderItem.getDishShortName());
				if (orderCondimentQuantity != null) {
					tmpOrderItem.addLargeQuantity((int) Math.ceil(orderCondimentQuantity));
				}
				orderItemsCombinedWithCondiments.add(tmpOrderItem);
			}
		}

		return orderItemsCombinedWithCondiments;
	}

	// It's expected at most one couponDish in the orderItems
	public CouponDish getCouponDish(boolean voided) {
		logger.finest("Entered");
		for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
			if (orderItem.isVoided() == voided) {
				Dish dish = menu.getDishes()[orderItem.getDishId()];
				if (dish.getCategory() == Configuration.getDishCategoryNameToIntegerValue()
						.get(ConfigKeyDefs.couponDish)) {
					return dish.getCouponDish();
				}
			}
		}

		return null;
	}

	public int getSubtotal() {
		logger.finest("Entered");
		return (getSoftDrinkTaxableAmount() + getAlcoholDrinkTaxableAmount() + getFoodTaxableAmount());
	}

	public int getFoodTaxableAmount() {
		logger.finest("Entered");
		// -1 is for categories other than soft-drink and alcohol drink. The other
		// categories is just call food.
		return getTaxableAmount(-1);
	}

	public int getSoftDrinkTaxableAmount() {
		logger.finest("Entered");
		return getTaxableAmount(Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.softDrink));
	}

	public int getAlcoholDrinkTaxableAmount() {
		logger.finest("Entered");
		return getTaxableAmount(Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.alcoholDrink));
	}

	public String getPercentOffInString() {
		logger.finest("Entered");
		return (Integer.valueOf(cart.getOrder().getDiscountPercent())).toString() + "%";
	}

	public int getPercentDiscountAmount() {
		logger.finest("Entered");
		int subTotal = (int) Math.round(cart.getOrder().getDiscountPercent() / 100.0 * getSubtotal());
		if (Configuration.getRoundToNickel()) {
			return roundToNearestNickel(subTotal);
		} else {
			return subTotal;
		}
	}

	public int getTaxAmount() {
		logger.finest("Entered");
		return getFoodTaxAmount() + getSoftDrinkTaxAmount() + getAlcoholDrinkTaxAmount();
	}

	public int getFoodTaxAmount() {
		logger.finest("Entered");
		double taxableAmount = getFoodTaxableAmount() * (1 - cart.getOrder().getDiscountPercent() / 100.0);
		int taxAmount = (int) Math.round(taxableAmount * cart.getOrder().getTaxRate());
		if (Configuration.getRoundToNickel()) {
			return roundToNearestNickel(taxAmount);
		} else {
			return taxAmount;
		}
	}

	public int getSoftDrinkTaxAmount() {
		logger.finest("Entered");
		double taxableAmount = getSoftDrinkTaxableAmount() * (1 - cart.getOrder().getDiscountPercent() / 100.0);
		int taxAmount = (int) Math.round(taxableAmount * cart.getOrder().getTaxRate());
		if (Configuration.getRoundToNickel()) {
			return roundToNearestNickel(taxAmount);
		} else {
			return taxAmount;
		}
	}

	public int getAlcoholDrinkTaxAmount() {
		logger.finest("Entered");
		double taxableAmount = getAlcoholDrinkTaxableAmount() * (1 - cart.getOrder().getDiscountPercent() / 100.0);
		int taxAmount = (int) Math.round(taxableAmount * cart.getOrder().getTaxRate());
		if (Configuration.getRoundToNickel()) {
			return roundToNearestNickel(taxAmount);
		} else {
			return taxAmount;
		}
	}

	public int getTotal() {
		logger.finest("Entered");
		int total = getSubtotal() + getFoodTaxAmount() + getSoftDrinkTaxAmount() + getAlcoholDrinkTaxAmount()
				+ cart.getOrder().getAdditionalAmount() - getPercentDiscountAmount()
				- cart.getOrder().getDiscountAmount();

		if (cart.getOrder().getType() == OrderType.DELIVERY) {
			total += cart.getDeliveryInfo().getDeliveryCharge();
		}

		return total;
	}

	public int getFoodTotal() {
		logger.finest("Entered");
		return getFoodTaxableAmount() + getFoodTaxAmount();
	}

	public int getSoftDrinkTotal() {
		logger.finest("Entered");
		return getSoftDrinkTaxableAmount() + getSoftDrinkTaxAmount();
	}

	public int getAlcoholDrinkTotal() {
		logger.finest("Entered");
		return getAlcoholDrinkTaxableAmount() + getAlcoholDrinkTaxAmount();
	}

	public boolean isBalanceDue() {
		logger.finest("Entered");
		return (getBalanceDue() > 0);
	}

	public int getBalanceDue() {
		logger.finest("Entered");
		return (getTotal() - cart.getPaymentTray().getTotalTendered());
	}

	public char getPayMethod() {
		logger.finest("Entered");
		int nonZeroPaymentTypeCount = 0;
		PaymentType lastPaymentTypeWithCount = null;
		for (PaymentType paymentType : PaymentType.values()) {
			if (cart.getPaymentTray().getPaymentCount(paymentType) > 0) {
				nonZeroPaymentTypeCount++;
				lastPaymentTypeWithCount = paymentType;
			}
		}

		if (nonZeroPaymentTypeCount == 0) {
			return 'u'; // unpaid
		}

		if (getBalanceDue() > 0) {
			return 'p'; // balance was partially paid
		} else {
			if (nonZeroPaymentTypeCount == 1) {
				return lastPaymentTypeWithCount.getShortHandChar();
			} else {
				return 'm'; // multiple payment types
			}
		}
	}

	public int getOrderItemTotal(OrderItem orderItem) {
		logger.finest("Entered");
		Dish dish = menu.getDish(orderItem.getDishId());
		int orderItemTotal = 0;
		if (orderItem.isVoided() == false) {
			orderItemTotal = (dish.getLargePrice() * orderItem.getLargeQuantity()
					+ dish.getSmallPrice() * orderItem.getSmallQuantity());
		}

		return (orderItemTotal);
	}

	public String getOrderItemTotalInString(OrderItem orderItem) {
		logger.finest("Entered");
		int orderItemTotal = getOrderItemTotal(orderItem);
		String orderItemTotalInString = " -";
		if (orderItemTotal != 0) {
			orderItemTotalInString = String.format("%4.2f", orderItemTotal/100.0);
		}

		return orderItemTotalInString;
	}

	public List<OrderItem> getOrderItemsWithSubstitutedCouponDish(CouponDish couponDish, Dish dish1, Dish dish2,
			boolean voided, int lessDishCategory) {
		logger.finest("Entered");
		List<OrderItem> orderItemsWithSubstitutedCouponDish = new ArrayList<>();
		if (couponDish == null) {
			for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems(voided)) {
				if (menu.getDishes()[orderItem.getDishId()].getCategory() != lessDishCategory) {
					orderItemsWithSubstitutedCouponDish.add(orderItem);
				}
			}
		} else {
			boolean couponDish1Added = false;
			boolean couponDish2Added = false;
			// add coupon dish to an existing regular(has no customizer),
			// coupon dish can have up to 2 regular dishes
			// each could have large or small
			for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems(voided)) {
				if (menu.getDishes()[orderItem.getDishId()].getCategory() != lessDishCategory) {
					OrderItem tmpOrderItem = new OrderItem(orderItem);
					// add quantity to a tmpOrderItem that matches the couponDish, then add it to
					// the returning list
					if (cart.getCustomizerTray().hasCustomizer(tmpOrderItem) == false
							&& cart.getExchangerTray().hasExchanger(tmpOrderItem) == false) {
						if (tmpOrderItem.getDishShortName().equals(couponDish.getDish1Name())) {
							tmpOrderItem.addLargeQuantity(couponDish.getDish1LargeQuantity());
							tmpOrderItem.addSmallQuantity(couponDish.getDish1SmallQuantity());
							couponDish1Added = true;
						} else if (tmpOrderItem.getDishShortName().equals(couponDish.getDish2Name())) {
							tmpOrderItem.addLargeQuantity(couponDish.getDish2LargeQuantity());
							tmpOrderItem.addSmallQuantity(couponDish.getDish2SmallQuantity());
							couponDish2Added = true;
						}
					}

					// filter out the couponDish from orderItem
					if (tmpOrderItem.getDishId() != couponDish.getDishId()) {
						orderItemsWithSubstitutedCouponDish.add(tmpOrderItem);
					}
				}
			}

			// dish1 orderItem does not exit, create a new orderItem for it
			if (couponDish1Added == false && dish1 != null) {
				OrderItem newOrderItemForDish1 = new OrderItem();
				newOrderItemForDish1.setDishId(dish1.getDishId());
				newOrderItemForDish1.setDishShortName(dish1.getShortName());
				newOrderItemForDish1.setLargeQuantity(couponDish.getDish1LargeQuantity());
				newOrderItemForDish1.setSmallQuantity(couponDish.getDish1SmallQuantity());
				orderItemsWithSubstitutedCouponDish.add(newOrderItemForDish1);
			}

			// dish2 orderItem does not exit, create a new orderItem for it
			if (couponDish2Added == false && dish2 != null) {
				OrderItem newOrderItemForDish2 = new OrderItem();
				newOrderItemForDish2.setDishId(dish2.getDishId());
				newOrderItemForDish2.setDishShortName(dish2.getShortName());
				newOrderItemForDish2.setLargeQuantity(couponDish.getDish2LargeQuantity());
				newOrderItemForDish2.setSmallQuantity(couponDish.getDish2SmallQuantity());
				orderItemsWithSubstitutedCouponDish.add(newOrderItemForDish2);
			}
		}

		return orderItemsWithSubstitutedCouponDish;
	}

	private int getTaxableAmount(int category) {
		logger.finest("Entered");
		int foodTotal = 0; // in penny amount
		int softDrinkTotal = 0;
		int alcoholDrinkTotal = 0;

		for (OrderItem orderItem : cart.getOrderItemTray().getOrderItems()) {
			Dish dish = menu.getDish(orderItem.getDishId());
			int dishCategory = dish.getCategory();
			int orderItemTotal = 0;
			if (orderItem.isVoided() == false) {

				orderItemTotal = (dish.getLargePrice() * orderItem.getLargeQuantity()
						+ dish.getSmallPrice() * orderItem.getSmallQuantity()
						+ cart.getCustomizerTray().getCustomizerTotal(orderItem)
						+ cart.getExchangerTray().getExchangerTotal(orderItem, menu.getDishes()));
			}

			if (dishCategory == Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.softDrink)) {
				softDrinkTotal += orderItemTotal;
			} else if (dishCategory == Configuration.getDishCategoryNameToIntegerValue()
					.get(ConfigKeyDefs.alcoholDrink)) {
				alcoholDrinkTotal += orderItemTotal;
			} else {
				foodTotal += orderItemTotal;
			}
		}

		if (category == Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.softDrink)) {
			return softDrinkTotal;
		} else if (category == Configuration.getDishCategoryNameToIntegerValue().get(ConfigKeyDefs.alcoholDrink)) {
			return alcoholDrinkTotal;
		} else {
			return foodTotal;
		}
	}

}
