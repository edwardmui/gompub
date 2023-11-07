package com.orderfoodnow.pos.shared.menu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class Condiment {
	private int dishId;
	private int condimentId;
	private String name;
	private float largeSizeQuantity;
	private float smallSizeQuantity;


	private static final Logger logger = Logger.getLogger(Condiment.class.getName());

	public Condiment(int dishId, int condimentId, List<Float> quantities, String name) {
		logger.finest("Entered");
		this.dishId = dishId;
		this.condimentId = condimentId;
		this.name = name;
		this.largeSizeQuantity = quantities.get(0);
		this.smallSizeQuantity = quantities.get(1);

	}

	public Condiment(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		dishId = resultSet.getInt(CondimentTable.DISH_ID);
		condimentId = resultSet.getInt(CondimentTable.CONDIMENT_ID);
		name = resultSet.getString(CondimentTable.NAME);
		largeSizeQuantity = resultSet.getFloat(CondimentTable.LARGE_SIZE_QUANTITY);
		smallSizeQuantity = resultSet.getFloat(CondimentTable.SMALL_SIZE_QUANTITY);

	}

	public PreparedStatement fillPreparedStatement(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, dishId);
		prepStmt.setInt(++parameterIndex, condimentId);
		prepStmt.setString(++parameterIndex, name);
		prepStmt.setFloat(++parameterIndex, largeSizeQuantity);
		prepStmt.setFloat(++parameterIndex, smallSizeQuantity);

		if (parameterIndex != CondimentTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ CondimentTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public int getDishId() {
		return dishId;
	}

	public void setDishId(int dishId) {
		this.dishId = dishId;
	}

	public int getCondimentId() {
		return condimentId;
	}

	public void setCondimentId(int condimentId) {
		this.condimentId = condimentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getLargeSizeQuantity() {
		return largeSizeQuantity;
	}

	public void setLargeSizeQuantity(float largeSizeQuantity) {
		this.largeSizeQuantity = largeSizeQuantity;
	}

	public float getSmallSizeQuantity() {
		return smallSizeQuantity;
	}

	public void setSmallSizeQuantity(float smallSizeQuantity) {
		this.smallSizeQuantity = smallSizeQuantity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dishId;
		result = prime * result + condimentId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Float.floatToIntBits(largeSizeQuantity);
		result = prime * result + Float.floatToIntBits(smallSizeQuantity);
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
		Condiment other = (Condiment) obj;
		if (dishId != other.dishId) {
			return false;
		}
		if (condimentId != other.condimentId) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (Float.floatToIntBits(largeSizeQuantity) != Float.floatToIntBits(other.largeSizeQuantity)) {
			return false;
		}
		if (Float.floatToIntBits(smallSizeQuantity) != Float.floatToIntBits(other.smallSizeQuantity)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Condiment.class.getSimpleName() +
				"[" + "dishId=" + dishId +
				", condimentId=" + condimentId +
				", name=" + name +
				", largeSizeQuantity=" + largeSizeQuantity +
				", smallSizeQuantity=" + smallSizeQuantity +
				"]";
		// @formatter:on
	}
}
