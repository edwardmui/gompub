package com.orderfoodnow.pos.shared.menu;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

// This class is for coupon dish.
// Coupon dish are mostly free dish(es) and the name said free.
// Kitchen needs to know the real name of the dish (without the word 'free') to combine with existing same dish to cook together.
public class CouponDish implements Serializable {
	private int dishId;
	private String name;
	private String dish1Name = "";
	private int dish1LargeQuantity;
	private int dish1SmallQuantity;
	private String dish2Name = "";
	private int dish2LargeQuantity;
	private int dish2SmallQuantity;
	private int minimumFoodTotal;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CouponDish.class.getName());

	public CouponDish() {
	}

	public CouponDish(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		dishId = resultSet.getInt(CouponDishTable.DISH_ID);
		name = resultSet.getString(CouponDishTable.NAME);
		dish1Name = resultSet.getString(CouponDishTable.DISH1_NAME);
		dish1LargeQuantity = resultSet.getInt(CouponDishTable.DISH1_LARGE_QUANTITY);
		dish1SmallQuantity = resultSet.getInt(CouponDishTable.DISH1_SMALL_QUANTITY);
		dish2Name = resultSet.getString(CouponDishTable.DISH2_NAME);
		dish2LargeQuantity = resultSet.getInt(CouponDishTable.DISH2_LARGE_QUANTITY);
		dish2SmallQuantity = resultSet.getInt(CouponDishTable.DISH2_SMALL_QUANTITY);
		minimumFoodTotal = resultSet.getInt(CouponDishTable.MINIMUM_FOOD_TOTAL);
	}

	public CouponDish(int dishId, String name, String dish1Name, int dish1LargeQuantity, int dish1SmallQuantity,
			String dish2Name, int dish2LargeQuantity, int dish2SmallQuantity, int minimumFoodTotal) {
		logger.finest("Entered");
		this.dishId = dishId;
		this.name = name;
		this.dish1Name = dish1Name;
		this.dish1LargeQuantity = dish1LargeQuantity;
		this.dish1SmallQuantity = dish1SmallQuantity;
		this.dish2Name = dish2Name;
		this.dish2LargeQuantity = dish2LargeQuantity;
		this.dish2SmallQuantity = dish2SmallQuantity;
		this.minimumFoodTotal = minimumFoodTotal;
	}

	public PreparedStatement fillPreparedStatement(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, dishId);
		prepStmt.setString(++parameterIndex, name);
		prepStmt.setString(++parameterIndex, dish1Name);
		prepStmt.setInt(++parameterIndex, dish1LargeQuantity);
		prepStmt.setInt(++parameterIndex, dish1SmallQuantity);
		prepStmt.setString(++parameterIndex, dish2Name);
		prepStmt.setInt(++parameterIndex, dish2LargeQuantity);
		prepStmt.setInt(++parameterIndex, dish2SmallQuantity);
		prepStmt.setInt(++parameterIndex, minimumFoodTotal);

		if (parameterIndex != CouponDishTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ CouponDishTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public int getDishId() {
		return dishId;
	}

	public void setDishId(int dishId) {
		this.dishId = dishId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDish1Name() {
		return dish1Name;
	}

	public void setDish1Name(String dish1Name) {
		this.dish1Name = dish1Name;
	}

	public int getDish1LargeQuantity() {
		return dish1LargeQuantity;
	}

	public void setDish1LargeQuantity(int dish1LargeQuantity) {
		this.dish1LargeQuantity = dish1LargeQuantity;
	}

	public int getDish1SmallQuantity() {
		return dish1SmallQuantity;
	}

	public void setDish1SmallQuantity(int dish1SmallQuantity) {
		this.dish1SmallQuantity = dish1SmallQuantity;
	}

	public String getDish2Name() {
		return dish2Name;
	}

	public void setDish2Name(String dish2Name) {
		this.dish2Name = dish2Name;
	}

	public int getDish2LargeQuantity() {
		return dish2LargeQuantity;
	}

	public void setDish2LargeQuantity(int dish2LargeQuantity) {
		this.dish2LargeQuantity = dish2LargeQuantity;
	}

	public int getDish2SmallQuantity() {
		return dish2SmallQuantity;
	}

	public void setDish2SmallQuantity(int dish2SmallQuantity) {
		this.dish2SmallQuantity = dish2SmallQuantity;
	}

	public int getMinimumFoodTotal() {
		return minimumFoodTotal;
	}

	public void setMinimumFoodTotal(int minimumFoodTotal) {
		this.minimumFoodTotal = minimumFoodTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dish1LargeQuantity;
		result = prime * result + ((dish1Name == null) ? 0 : dish1Name.hashCode());
		result = prime * result + dish1SmallQuantity;
		result = prime * result + dish2LargeQuantity;
		result = prime * result + ((dish2Name == null) ? 0 : dish2Name.hashCode());
		result = prime * result + dish2SmallQuantity;
		result = prime * result + dishId;
		result = prime * result + minimumFoodTotal;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		CouponDish other = (CouponDish) obj;
		if (dishId != other.dishId) {
			return false;
		}

		return equalsIgnoreKeys(obj);
	}

	public boolean equalsIgnoreKeys(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CouponDish other = (CouponDish) obj;
		if (dish1LargeQuantity != other.dish1LargeQuantity) {
			return false;
		}
		if (dish1Name == null) {
			if (other.dish1Name != null) {
				return false;
			}
		} else if (!dish1Name.equals(other.dish1Name)) {
			return false;
		}
		if (dish1SmallQuantity != other.dish1SmallQuantity) {
			return false;
		}
		if (dish2LargeQuantity != other.dish2LargeQuantity) {
			return false;
		}
		if (dish2Name == null) {
			if (other.dish2Name != null) {
				return false;
			}
		} else if (!dish2Name.equals(other.dish2Name)) {
			return false;
		}
		if (dish2SmallQuantity != other.dish2SmallQuantity) {
			return false;
		}
		if (minimumFoodTotal != other.minimumFoodTotal) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return CouponDish.class.getSimpleName() +
				"[" +
				"dishId=" + dishId +
				", name=" + name +
				", dish1Name=" + dish1Name +
				", dish1LargeQuantity=" + dish1LargeQuantity +
				", dish1SmallQuantity=" + dish1SmallQuantity +
				", dish2Name=" + dish2Name +
				", dish2LargeQuantity=" + dish2LargeQuantity +
				", dish2SmallQuantity=" + dish2SmallQuantity +
				", minimumFoodTotal=" + minimumFoodTotal +
				"]";
		// @formatter:on
	}
}