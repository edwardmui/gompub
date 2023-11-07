package com.orderfoodnow.pos.frontend;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class GuiTableModel extends AbstractTableModel {

	public GuiTableModel(List<List<String>> tableEntries, String[] columnNames) {
		this.tableEntries = tableEntries;
		this.columnNames = columnNames;
	}

	private static final long serialVersionUID = 1L;
	private List<List<String>> tableEntries;
	private String[] columnNames;

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return tableEntries.size();
	}

	@Override
	public String getValueAt(int row, int col) {
		if ((row >= 0) && (col >= 0) && (getRowCount() > 0) && (getColumnCount() > 0)) {
			return tableEntries.get(row).get(col);
		} else {
			return null;
		}
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
