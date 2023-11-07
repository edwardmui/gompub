package com.orderfoodnow.pos.shared.staff;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class Employee implements Serializable, Comparable<Employee> {
	private int employeeId;
	private String initials = "";
	private String nickname = "";
	private String phoneNumber = "";
	private String firstName = "";
	private String lastName = "";
	private String street = "";
	private String city = "";
	private String state = "";
	private String zip = "";
	private Date hireDate = new Date(System.currentTimeMillis());
	private boolean active = true;
	private String userId = "";
	private String password = "";
	private byte[] salt;
	private String email = "";
	private String note = "";

	private List<Role> roles; // populate via setter, not resultSet constructor
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Employee.class.getName());

	public Employee() {
	}

	public Employee(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		init(resultSet);
	}

	private void init(ResultSet resultSet) throws SQLException {
		logger.finest("Entered");
		if (resultSet == null) {
			throw new RuntimeException("ResultSet is null");
		}

		employeeId = resultSet.getInt(EmployeeTable.EMPLOYEE_ID);
		initials = resultSet.getString(EmployeeTable.INITIALS);
		nickname = resultSet.getString(EmployeeTable.NICKNAME);
		phoneNumber = resultSet.getString(EmployeeTable.PHONE_NUMBER);
		firstName = resultSet.getString(EmployeeTable.FIRST_NAME);
		lastName = resultSet.getString(EmployeeTable.LAST_NAME);
		street = resultSet.getString(EmployeeTable.STREET);
		city = resultSet.getString(EmployeeTable.CITY);
		state = resultSet.getString(EmployeeTable.STATE);
		zip = resultSet.getString(EmployeeTable.ZIP);
		hireDate = resultSet.getDate(EmployeeTable.HIRE_DATE);
		active = resultSet.getInt(EmployeeTable.ACTIVE) > 0;
		userId = resultSet.getString(EmployeeTable.USER_ID);
		password = resultSet.getString(EmployeeTable.PASSWORD);
		salt = resultSet.getBytes(EmployeeTable.SALT);
		email = resultSet.getString(EmployeeTable.EMAIL);
		note = resultSet.getString(EmployeeTable.NOTE);
	}

	private PreparedStatement fillPreparedStatementWithKeys(PreparedStatement prepStmt, int startIndex)
			throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setInt(++parameterIndex, employeeId);

		int expectedIndex = EmployeeTable.NUM_KEY_COLUMNS + startIndex;
		if (parameterIndex != expectedIndex) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect=" + expectedIndex
					+ " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatementWithAllFieldsMinusAutoInc(PreparedStatement prepStmt)
			throws SQLException {
		logger.finest("Entered");
		fillPreparedStatement(prepStmt, 0);

		return prepStmt;
	}

	private PreparedStatement fillPreparedStatement(PreparedStatement prepStmt, int startIndex) throws SQLException {
		logger.finest("Entered");
		int parameterIndex = startIndex;

		prepStmt.setString(++parameterIndex, initials);
		prepStmt.setString(++parameterIndex, nickname);
		prepStmt.setString(++parameterIndex, phoneNumber);
		prepStmt.setString(++parameterIndex, firstName);
		prepStmt.setString(++parameterIndex, lastName);
		prepStmt.setString(++parameterIndex, street);
		prepStmt.setString(++parameterIndex, city);
		prepStmt.setString(++parameterIndex, state);
		prepStmt.setString(++parameterIndex, zip);
		prepStmt.setDate(++parameterIndex, hireDate);
		prepStmt.setInt(++parameterIndex, active ? 1 : 0);
		prepStmt.setString(++parameterIndex, userId);
		prepStmt.setString(++parameterIndex, password);
		prepStmt.setBytes(++parameterIndex, salt);
		prepStmt.setString(++parameterIndex, email);
		prepStmt.setString(++parameterIndex, note);

		int expectedParameterCount = EmployeeTable.NUM_COLUMNS - EmployeeTable.NUM_AUTOINC_COLUMNS + startIndex;
		if (parameterIndex != expectedParameterCount) {
			throw new RuntimeException("PreparedStatement mismatch of number of arguments. Expect="
					+ expectedParameterCount + " parameters. Actual=" + parameterIndex);
		}

		return prepStmt;
	}

	public void insert(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection.prepareStatement(
				"INSERT INTO " + EmployeeTable.ALL_COLUMNS_MINUS_AUTOINC, Statement.RETURN_GENERATED_KEYS)) {
			fillPreparedStatementWithAllFieldsMinusAutoInc(prepStmt);
			logger.fine(prepStmt.toString());
			prepStmt.executeUpdate();
			ResultSet resultSet = prepStmt.getGeneratedKeys();
			resultSet.next();
			employeeId = resultSet.getInt(1);
		}
	}

	public void read(Connection connection) throws SQLException {
		logger.finest("Entered");
		try (PreparedStatement prepStmt = connection
				.prepareStatement("SELECT * FROM " + EmployeeTable.TABLENAME_ANDING_ALL_KEYS)) {
			fillPreparedStatementWithKeys(prepStmt, 0);
			logger.finer(prepStmt.toString());
			ResultSet resultSet = prepStmt.executeQuery();
			resultSet.first();
			init(resultSet);
		}
	}

	// @formatter:off
//	public void update(Connection connection) throws SQLException {
//		logger.finest("Entered");
//		try (PreparedStatement prepStmt = connection
//				.prepareStatement("UPDATE " + EmployeeTable.ALL_COLUMNS_WITH_SET + EmployeeTable.ANDING_ALL_KEYS)) {
//			fillPreparedStatementWithAllFieldsMinusAutoInc(prepStmt);
//			fillPreparedStatementWithKeys(prepStmt, EmployeeTable.NUM_COLUMNS);
//			logger.finer(prepStmt.toString());
//			prepStmt.executeUpdate();
//		}
//	}
//
//	public void delete(Connection connection) throws SQLException {
//		try (PreparedStatement prepStmt = connection
//				.prepareStatement("DELETE FROM " + EmployeeTable.TABLENAME_ANDING_ALL_KEYS)) {
//			fillPreparedStatementWithKeys(prepStmt, 0);
//			logger.finer(prepStmt.toString());
//			prepStmt.executeUpdate();
//		}
//	}
	// @formatter:on

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	@Override
	public int compareTo(Employee thatEmployee) {
		return Comparator.comparing(Employee::getNickname).thenComparing(Employee::getEmployeeId).compare(this,
				thatEmployee);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + employeeId;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((hireDate == null) ? 0 : hireDate.hashCode());
		result = prime * result + ((initials == null) ? 0 : initials.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + Arrays.hashCode(salt);
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
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
		Employee other = (Employee) obj;
		if (active != other.active) {
			return false;
		}
		if (city == null) {
			if (other.city != null) {
				return false;
			}
		} else if (!city.equals(other.city)) {
			return false;
		}
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (employeeId != other.employeeId) {
			return false;
		}
		if (firstName == null) {
			if (other.firstName != null) {
				return false;
			}
		} else if (!firstName.equals(other.firstName)) {
			return false;
		}
		if (hireDate == null) {
			if (other.hireDate != null) {
				return false;
			}
		} else if (!hireDate.equals(other.hireDate)) {
			return false;
		}
		if (initials == null) {
			if (other.initials != null) {
				return false;
			}
		} else if (!initials.equals(other.initials)) {
			return false;
		}
		if (lastName == null) {
			if (other.lastName != null) {
				return false;
			}
		} else if (!lastName.equals(other.lastName)) {
			return false;
		}
		if (nickname == null) {
			if (other.nickname != null) {
				return false;
			}
		} else if (!nickname.equals(other.nickname)) {
			return false;
		}
		if (note == null) {
			if (other.note != null) {
				return false;
			}
		} else if (!note.equals(other.note)) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (phoneNumber == null) {
			if (other.phoneNumber != null) {
				return false;
			}
		} else if (!phoneNumber.equals(other.phoneNumber)) {
			return false;
		}
		if (roles == null) {
			if (other.roles != null) {
				return false;
			}
		} else if (!roles.equals(other.roles)) {
			return false;
		}
		if (!Arrays.equals(salt, other.salt)) {
			return false;
		}
		if (state == null) {
			if (other.state != null) {
				return false;
			}
		} else if (!state.equals(other.state)) {
			return false;
		}
		if (street == null) {
			if (other.street != null) {
				return false;
			}
		} else if (!street.equals(other.street)) {
			return false;
		}
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		} else if (!userId.equals(other.userId)) {
			return false;
		}
		if (zip == null) {
			if (other.zip != null) {
				return false;
			}
		} else if (!zip.equals(other.zip)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// @formatter:off
		return Employee.class.getSimpleName() +
				"[" +
				"employeeId=" + employeeId +
				", roles=" + roles +
				", initials=" + initials +
				", nickname=" + nickname +
				", phoneNumber=" + phoneNumber +
				", firstName=" + firstName +
				", lastName=" + lastName +
				//", street=" + street +
				//", city=" + city +
				//", state=" + state +
				//", zip=" + zip +
				//", hireDate=" + hireDate +
				", active=" + active  +
				", userId=" + userId +
				//", password=" + password +
				//", salt=" +
				//", email=" + email +
				//", note=" + note +
				"]";
		// @formatter:on
	}
}
