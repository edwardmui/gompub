package com.orderfoodnow.pos.shared;

import java.util.Comparator;

public class Street implements Comparable<Street> {
	private String name;
	private int houseNumberMin; // house number starting
	private int houseNumberMax; // house number ending
	private String zip;

	public Street(String name, int houseNumberMin, int houseNumberMax, String zip) {
		this.name = name;
		this.houseNumberMin = houseNumberMin;
		this.houseNumberMax = houseNumberMax;
		this.zip = zip;
	}

	public boolean isInRange(int houseNumber) {
		return houseNumber >= houseNumberMin && houseNumber <= houseNumberMax;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getHouseNumberMin() {
		return houseNumberMin;
	}

	public void setHouseNumberMin(int houseNumberMin) {
		this.houseNumberMin = houseNumberMin;
	}

	public int getHouseNumberMax() {
		return houseNumberMax;
	}

	public void setHouseNumberMax(int houseNumberMax) {
		this.houseNumberMax = houseNumberMax;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Override
	public int compareTo(Street thatStreet) {
		return Comparator.comparing(Street::getName).compare(this, thatStreet);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + houseNumberMax;
		result = prime * result + houseNumberMin;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Street other = (Street) obj;
		if (houseNumberMax != other.houseNumberMax) {
			return false;
		}
		if (houseNumberMin != other.houseNumberMin) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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
		return Street.class.getSimpleName() +
				"[" + "name=" + name +
				", houseNumberMin=" + houseNumberMin +
				", houseNumberMax=" + houseNumberMax +
				", zip=" + zip +
				"]";
		// @formatter:on
	}
}
