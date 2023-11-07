package com.orderfoodnow.pos.shared.menu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Abbreviation {
	private int dishId;
	private int abbreviationId;
	private String name;

	private static final Logger logger = Logger.getLogger(Abbreviation.class.getName());

	public Abbreviation(int dishId, int abbreviationId, String name) {
		logger.finest("Entered");
		this.dishId = dishId;
		this.abbreviationId = abbreviationId;
		this.name = name;
	}

	public Abbreviation(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		this.dishId = resultSet.getInt(AbbreviationTable.DISH_ID);
		this.abbreviationId = resultSet.getInt(AbbreviationTable.ABBREVIATION_ID);
		this.name = resultSet.getString(AbbreviationTable.NAME);
	}

	public PreparedStatement fillPreparedStatement(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, dishId);
		prepStmt.setInt(++parameterIndex, abbreviationId);
		prepStmt.setString(++parameterIndex, name);

		if (parameterIndex != AbbreviationTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ AbbreviationTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	// bulk delete
	public static void delete(List<Dish> dishes, Connection connection) throws SQLException {
		logger.finest("Entered");
		for (Dish dish : dishes) {
			Map<Integer, String> abbreviations = dish.getIdToAbbreviation();
			for (Integer abbreviationId : abbreviations.keySet()) {
				int parameterIndex = 0;
				try (PreparedStatement prepStmt = connection
						.prepareStatement("DELETE FROM " + AbbreviationTable.TABLENAME_ANDING_ALL_KEYS)) {
					prepStmt.setInt(++parameterIndex, dish.getDishId());
					prepStmt.setInt(++parameterIndex, abbreviationId);
					logger.fine(prepStmt.toString());
					prepStmt.executeUpdate();
				}
			}
		}
	}

	public int getDishId() {
		return dishId;
	}

	public void setDishId(int dishId) {
		this.dishId = dishId;
	}

	public int getAbbreviationId() {
		return abbreviationId;
	}

	public void setAbbreviationId(int abbreviationId) {
		this.abbreviationId = abbreviationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dishId;
		result = prime * result + abbreviationId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Abbreviation other = (Abbreviation) obj;
		if (dishId != other.dishId) {
			return false;
		}
		if (abbreviationId != other.abbreviationId) {
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
		return Abbreviation.class.getSimpleName() +
				"[" +
				"dishId=" + dishId +
				", abbreviationId=" + abbreviationId +
				", name=" + name +
				"]";
		// @formatter:on
	}
}
