package com.orderfoodnow.pos.shared.staff;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This specifies an employee to one or more roles and the proficiency level at
 * that role. Role value is the RoleType
 */
public class Role implements Serializable {
	private int employeeId;
	private RoleType roleType;
	private ProficiencyLevel proficiencyLevel = ProficiencyLevel.BEGINNER;

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Role.class.getName());

	public Role() {
	}

	public Role(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		employeeId = resultSet.getInt(RoleTable.EMPLOYEE_ID);
		roleType = RoleType.values()[resultSet.getInt(RoleTable.ROLE_TYPE)];
		proficiencyLevel = ProficiencyLevel.values()[resultSet.getInt(RoleTable.PROFICENCY_LEVEL)];
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, employeeId);
		prepStmt.setInt(++parameterIndex, roleType.ordinal());

		int expectedIndex = RoleTable.NUM_KEY_COLUMNS + startIndex;
		if (parameterIndex != expectedIndex) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect=" + expectedIndex
					+ " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatementWithAllFields(PreparedStatement prepStmt) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = 0;

		prepStmt.setInt(++parameterIndex, employeeId);
		prepStmt.setInt(++parameterIndex, roleType.ordinal());
		prepStmt.setInt(++parameterIndex, proficiencyLevel.ordinal());
		if (parameterIndex != RoleTable.NUM_COLUMNS) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ RoleTable.NUM_COLUMNS + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO " + RoleTable.ALL_COLUMNS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			logger.fine(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + RoleTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	public void update(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("UPDATE " + RoleTable.ALL_COLUMNS_WITH_SET + RoleTable.ANDING_ALL_KEYS)) {
			fillPreparedStatementWithAllFields(prepStmt);
			fillPreparedStatementWithKeys(prepStmt, RoleTable.NUM_COLUMNS);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public void delete(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("DELETE FROM " + RoleTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			prepStmt.executeUpdate();
		}
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	public ProficiencyLevel getProficiencyLevel() {
		return proficiencyLevel;
	}

	public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
		this.proficiencyLevel = proficiencyLevel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + employeeId;
		result = prime * result + ((proficiencyLevel == null) ? 0 : proficiencyLevel.hashCode());
		result = prime * result + ((roleType == null) ? 0 : roleType.hashCode());
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
		Role other = (Role) obj;
		if (employeeId != other.employeeId) {
			return false;
		}
		if (proficiencyLevel != other.proficiencyLevel) {
			return false;
		}
		if (roleType != other.roleType) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Role.class.getSimpleName() +
				"[" +
				"employeeId=" + employeeId +
				", roleType=" + roleType +
				", proficiencyLevel=" + proficiencyLevel +
				"]";
		// @formatter:on
	}
}
