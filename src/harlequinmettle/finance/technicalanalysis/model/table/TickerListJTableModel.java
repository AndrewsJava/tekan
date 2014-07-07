package harlequinmettle.finance.technicalanalysis.model.table;

import harlequinmettle.finance.technicalanalysis.applications.TechnicalDatabaseViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

public class TickerListJTableModel extends AbstractTableModel {
	private String[] columnNames;
	private Object[][] data;

	public TickerListJTableModel(List<String> tickerSubset) {

		columnNames = setColumnNamesFromPreferences();
		data = setDataForTable(tickerSubset);
	}

	private Object[][] setDataForTable(List<String> tickerSubset) {

		Object[][] d = new Object[tickerSubset.size()][columnNames.length];
		int i = 0;
		for (String ticker : tickerSubset) {
			d[i][0] = ticker;
			TreeMap<String, Float> fundamentals = TechnicalDatabaseViewer.FDB.CURRENT_TICKER_TO_LABEL_DATA_MAPING.get(ticker);

			int J = 1;
			boolean first = true;
			for (String indicator : columnNames) {
				if(first){
					first=false;
					continue;
				}
				if (fundamentals != null)
					d[i][J] = fundamentals.get(indicator);
				else
					d[i][J] = new Float(Float.NaN);
				J++;
			}
			i++;
		}

		return d;
	}

	private String[] setColumnNamesFromPreferences() {
		ArrayList<String> preferredLabels = new ArrayList<String>();
		preferredLabels.add("Ticker");
		for (Entry<String, Boolean> pref : TechnicalDatabaseViewer.displayPreferences.entrySet()) {
			if (pref.getValue())
				preferredLabels.add(pref.getKey());
		}
		return preferredLabels.toArray(new String[preferredLabels.size()]);
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

}
