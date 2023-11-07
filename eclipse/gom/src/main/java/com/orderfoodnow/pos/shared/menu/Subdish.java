package com.orderfoodnow.pos.shared.menu;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Util;

// This class is for one dish name that consists of multiple (sub)dishes.
// Example are the Dinner for N where N is from 2 to 8. Those suite dishes are
// defined in the menu. Those suite dishes can be consider as super dish that consists of regular dishes.
public class Subdish implements Serializable, Comparable<Subdish> {

	private int dishId;
	private int subdishId;
	private String name;
	private float quantity;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Subdish.class.getName());

	public Subdish() {
	}

	public Subdish(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		dishId = resultSet.getInt(SubdishTable.DISH_ID);
		subdishId = resultSet.getInt(SubdishTable.SUBDISH_ID);
		name = resultSet.getString(SubdishTable.NAME);
		quantity = resultSet.getFloat(SubdishTable.QUANTITY);
	}

	public Subdish(int dishId, int subdishId, String name, float quantity) {
		logger.finest("Entered");
		this.dishId = dishId;
		this.subdishId = subdishId;
		this.name = name;
		this.quantity = quantity;
	}

	public PreparedStatement fillPreparedStatement(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, dishId);
		prepStmt.setInt(++parameterIndex, subdishId);
		prepStmt.setString(++parameterIndex, name);
		prepStmt.setFloat(++parameterIndex, quantity);

		if (parameterIndex != SubdishTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ SubdishTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public static Subdish findActiveSubdish(int dishId, int subdishId, List<Subdish> subdishes, Dish[] dishes) {
		logger.finest("Entered");
		for (Subdish subdish : subdishes) {
			if (subdish.getDishId() == dishId && subdish.getSubdishId() == subdishId && dishes[dishId].isActive()) {
				return subdish;
			}
		}

		return null;
	}

	public int getDishId() {
		return dishId;
	}

	public void setDishId(int dishId) {
		this.dishId = dishId;
	}

	public int getSubdishId() {
		return subdishId;
	}

	public void setSubdishId(int subdishId) {
		this.subdishId = subdishId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getQuantity() {
		return quantity;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
	}

	public String getQuantityInString(int largeQuantity, int smallQuantity) {
		double subdishQuantity = (double) (largeQuantity + smallQuantity / 2.0) * quantity;
		String subdishQuantityInString = "";
		if (subdishQuantity > 0) {
			subdishQuantityInString = Util.formatDouble(subdishQuantity, 1, 0);
		}

		return subdishQuantityInString;
	}

	@Override
	public int compareTo(Subdish thatSubdish) {
		return Comparator.comparing(Subdish::getDishId).thenComparing(Subdish::getSubdishId).compare(this, thatSubdish);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dishId;
		result = prime * result + subdishId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Float.floatToIntBits(quantity);
		return result;
	}

	public boolean equalsIncludeKeys(Object obj) {
		Subdish other = (Subdish) obj;
		if (dishId != other.dishId) {
			return false;
		}
		if (subdishId != other.subdishId) {
			return false;
		}

		return equals(obj);
	}

	// This equal ignores keys comparison. The java List<Subdish> equals calls this
	// element equals. If keys compare is needed, use equalsIncludeKeys
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
		Subdish other = (Subdish) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (Float.floatToIntBits(quantity) != Float.floatToIntBits(other.quantity)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Subdish.class.getSimpleName() +
				"[" +
				"dishId=" + dishId +
				", subdishId=" + subdishId +
				", name=" + name +
				", quantity=" + quantity +
				"]";
		// @formatter:on
	}
}