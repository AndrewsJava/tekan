package harlequinmettle.finance.technicalanalysis.model.db;

import harlequinmettle.utils.TimeRecord;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;
import harlequinmettle.utils.guitools.FilterPanel;

import java.awt.geom.Point2D;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class CurrentFundamentalsSQLiteDatabase implements
		FundamentalsDatabaseInterface {
	public static ArrayList<String> allLabels = new ArrayList<String>(
			Arrays.asList(labels));
	public static ArrayList<String> subsetLabels = new ArrayList<String>(
			Arrays.asList(forDisplaying));
	public static TreeMap<String, TreeMap<String, Float>> CURRENT_TICKER_TO_LABEL_DATA_MAPING = new TreeMap<String, TreeMap<String, Float>>();
	public String pathToObj = "sqlite_blob_based_technical_database_settings";
	String pathtodata = "path to small sqlite database";

	public static void main(String[] args) {

		CurrentFundamentalsSQLiteDatabase sqlitedb = new CurrentFundamentalsSQLiteDatabase();
		// for (Entry<String, TreeMap<String, Float>> ent :
		// CURRENT_TICKER_TO_LABEL_DATA_MAPING
		// .entrySet()) {
		// System.out.println(ent.getKey() + "    " + ent.getValue());
		// }

	}

	public CurrentFundamentalsSQLiteDatabase() {
		long time = System.currentTimeMillis();
		init();
		System.out.println("current fundamentals sql load -time: "
				+ (System.currentTimeMillis() - time) / 1000 + "  sec");
	}

	private void init() {
		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
				"application_settings", pathToObj);
		String root = settingssaver.getSetting(pathtodata);

		Connection cn = SQLiteTools.establishSQLiteConnection(new File(root));
		if (cn != null) {
			String tableName = "fundamentals";
			System.out.println("db connection eastablished: " + cn.toString());
			loadDatabase(cn, tableName);
		}
	}

	void loadDatabase(Connection conn, String tableName) {
		load1YrAgoCnn(conn, tableName);
		recentDataExceptCnnForecast(conn, tableName);
	}

	private void recentDataExceptCnnForecast(Connection conn, String tableName) {
		PreparedStatement stmt = null;
		try {
			// last month
			float recent = TimeRecord.dayNumber(System.currentTimeMillis()) - 53;
			// String sqlstatement = "SELECT * FROM " + tableName
			// + " WHERE db_datenumber > " + recentDateNumber;
			String sqlstatement = "SELECT * FROM " + tableName
					+ " WHERE db_datenumber > " + recent + ";";

			System.out.println("PREPARING STATEMENT WITH QUERY: "
					+ sqlstatement);
			stmt = conn.prepareStatement(sqlstatement);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {

				String ticker = rs.getString("db_ticker");
				float datenumber = rs.getFloat("db_datenumber");
				float[] data = (float[]) SQLiteTools.deserialize(rs
						.getBytes("db_data"));
		//		System.out.println("\n\n"+ticker);
			//	System.out.println(datenumber);
			//	System.out.println(Arrays.toString(data));
				for (int i = 0; i < data.length; i++) {
					if (i == 1)
						continue;
					addToDatabase(ticker, i, data[i]);
				}
				// System.out.println("\n\n" );
				// System.out.println("name = " + ticker);
				// System.out.println("date  = " + datenumber);
				// System.out.println("data  = " + Arrays.toString(data));
			}
			rs.close();
		} catch (SQLException e) {
		} finally {
			// clean up resources
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	private void load1YrAgoCnn(Connection conn, String tableName) {
		PreparedStatement stmt = null;
		try {
			// last month
			float over1yr = TimeRecord.dayNumber(System.currentTimeMillis()) - 390;
			float under1yr = TimeRecord.dayNumber(System.currentTimeMillis()) - 340;
			// String sqlstatement = "SELECT * FROM " + tableName
			// + " WHERE db_datenumber > " + recentDateNumber;
			String sqlstatement = "SELECT * FROM " + tableName
					+ " WHERE db_datenumber BETWEEN " + over1yr + " AND "
					+ under1yr + ";";

			System.out.println("PREPARING STATEMENT WITH QUERY: "
					+ sqlstatement);
			stmt = conn.prepareStatement(sqlstatement);
			int i = 0;
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				i++;
				String ticker = rs.getString("db_ticker");
				float datenumber = rs.getFloat("db_datenumber");
				float[] data = (float[]) SQLiteTools.deserialize(rs
						.getBytes("db_data"));
				addToDatabase(ticker, 1, data[1]);
				// System.out.println("\n\n" + i);
				// System.out.println("name = " + ticker);
				// System.out.println("date  = " + datenumber);
			}
			rs.close();
		} catch (SQLException e) {
		} finally {
			// clean up resources
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	private void addToDatabase(String ticker, int labelIndex, float value) {
		String originalLable = allLabels.get(labelIndex);
		// if(labelIndex!=1){
		// System.out.println("label  = " + originalLable);
		// System.out.println("value  = " + value);
		// }
		if (!subsetLabels.contains(originalLable))
			return;
		if (CURRENT_TICKER_TO_LABEL_DATA_MAPING.containsKey(ticker)) {
			TreeMap<String, Float> tickersData = CURRENT_TICKER_TO_LABEL_DATA_MAPING
					.get(ticker);
			if (tickersData.containsKey(originalLable)) {
				float currentData = tickersData.get(originalLable);
				if (currentData != currentData)
					tickersData.put(originalLable, value);
			} else {
				tickersData.put(originalLable, value);
			}
		} else {
			TreeMap<String, Float> tickerData = new TreeMap<String, Float>();
			tickerData.put(originalLable, value);
			CURRENT_TICKER_TO_LABEL_DATA_MAPING.put(ticker, tickerData);
		}
	}

	public TreeMap<String, String> getFilterResults(FilterPanel[] searchFilters) {
		TreeMap<String, String> results = new TreeMap<String, String>();
		int i = 0;
		for (String ticker : CURRENT_TICKER_TO_LABEL_DATA_MAPING.keySet()) {
			boolean qualifies = true;
			String reasonForQualification = "";
			for (FilterPanel filter : searchFilters) {
				if (!filter.shouldFilterBeApplied())
					continue;

				int id = filter.getId();
				float low = filter.getLow();
				float high = filter.getHigh();
				if (!CURRENT_TICKER_TO_LABEL_DATA_MAPING.get(ticker)
						.containsKey(subsetLabels.get(id))) {

					qualifies = false;
					continue;
				}
				float dataPoint = CURRENT_TICKER_TO_LABEL_DATA_MAPING.get(
						ticker).get(subsetLabels.get(id));
				if (dataPoint != dataPoint || dataPoint > high
						|| dataPoint < low)
					qualifies = false;
				else
					reasonForQualification += "  " + subsetLabels.get(id)
							+ "  :  [" + dataPoint + "] ";
			}

			if (qualifies)
				results.put(ticker, reasonForQualification);
			i++;
		}
		return results;
	}

	public static Point2D.Float getMinMaxForIndicator(String indicator) {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		ArrayList<Float> values = new ArrayList<Float>();
		// ////////////////////////////////
		for (String ticker : CURRENT_TICKER_TO_LABEL_DATA_MAPING.keySet()) {

			if (!CURRENT_TICKER_TO_LABEL_DATA_MAPING.get(ticker).containsKey(
					indicator)) {
				continue;
			}
			float dataPoint = CURRENT_TICKER_TO_LABEL_DATA_MAPING.get(ticker)
					.get(indicator);
			if (dataPoint != dataPoint || Float.isInfinite(dataPoint))
				continue;

			values.add(dataPoint);
			if (dataPoint > max)
				max = dataPoint;
			if (dataPoint < min)
				min = dataPoint;
		}
		// //////////////////////////////
		
		System.out.println(values.size()+"      -"+values);
		return new Point2D.Float(min, max);
	}
}
