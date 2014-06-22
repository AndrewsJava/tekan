package harlequinmettle.finance.technicalanalysis.model.db;

import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

public class TechnicalDatabaseSQLite implements TechnicalDatabaseInterface {

	static String pathToDatabase = "";
	public static boolean loadingDatabase = true;

	public static TreeMap<String, float[][]> SQLITE_PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, float[][]>();
	public static int tradingDaysLimit = 1000;
	static long time = System.currentTimeMillis();

	public static void main(String[] args) {
		TechnicalDatabaseSQLite db = new TechnicalDatabaseSQLite();

	}

	public TechnicalDatabaseSQLite() {
		init();
	}

	public TechnicalDatabaseSQLite(int alternateSize) {
		tradingDaysLimit = alternateSize;
		init();
	}

	private void init() {
		pathToDatabase = new ChooseFilePrompterPathSaved("application_settings","databasesettings")
				.getSetting("path to sqlite technical database");

		new DBLoadThread().start();
	}

	private class DBLoadThread extends Thread {

		@Override
		public void run() {
			loadAllDataFromSQLiteDB();
			loadingDatabase = false;
			System.out.println("technical database loading time taken: "
					+ (System.currentTimeMillis() - time) / 1000 + " sec");
			System.out.println("memory used : "
					+ (Runtime.getRuntime().totalMemory() / 1000000) + "   MB");
		}

	}

	private void loadAllDataFromSQLiteDB() {

		System.out
				.println("Technical Database Loading Thread Started ..........");
		pathToDatabase = new ChooseFilePrompterPathSaved("application_settings","databasesettings")
				.getSetting("path to sqlite technical database");
		Connection cnxn = SQLiteTools.establishSQLiteConnection(new File(
				pathToDatabase));
		String tableName = "technicaldatatable";
		PreparedStatement stmt = null;

		try {

			String sqlstatement = "SELECT * FROM " + tableName
			// + " WHERE db_ticker = " + ticker
					+ ";";

			System.out.println("PREPARING STATEMENT WITH QUERY: "
					+ sqlstatement);
			stmt = cnxn.prepareStatement(sqlstatement);
			int counter = 0;
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {

				String t = rs.getString("db_ticker");
				if (counter++ % 1000 == 0)
					System.out.println(counter + "   getting blob for:  	" + t);
				float[][] data = (float[][]) SQLiteTools.deserialize(rs
						.getBytes("db_data"));

				if (data.length > tradingDaysLimit) {

					float[][] part2 = new float[tradingDaysLimit][];

//					System.arraycopy(data, data.length - tradingDaysLimit, part2,
//							0, part2.length);
					System.arraycopy(data, 0, part2,
							0, part2.length);

					data = part2;
				}
				SQLITE_PER_TICKER_PER_DAY_TECHNICAL_DATA.put(t, data);
				Thread.yield();
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
		try {
			cnxn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out
				.println(".........   done loading technical database from sqlite");
	}

	public float[][] queryTechnicalDatabase(String ticker) {
		pathToDatabase = new ChooseFilePrompterPathSaved("application_settings","databasesettings")
				.getSetting("path to sqlite technical database");
		Connection cnxn = SQLiteTools.establishSQLiteConnection(new File(
				pathToDatabase));
		String tableName = "technicaldatatable";
		float[][] technicalData = getDataFromSQLiteDatabase(cnxn, tableName,
				ticker);
		try {
			cnxn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return technicalData;
	}

	public static float[][] getDataFromSQLiteDatabase(Connection conn,
			String tableName, String ticker) {
		PreparedStatement stmt = null;
		float[][] data = null;
		try {

			String sqlstatement = "SELECT * FROM " + tableName
			// + " WHERE db_ticker = " + ticker
					+ ";";

			System.out.println("PREPARING STATEMENT WITH QUERY: "
					+ sqlstatement);
			stmt = conn.prepareStatement(sqlstatement);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {

				String t = rs.getString("db_ticker");
				if (t.equals(ticker)) {

					data = (float[][]) SQLiteTools.deserialize(rs
							.getBytes("db_data"));
					break;
				}
				// System.out.println("\n\n" );
				// System.out.println("query for = " + ticker);
				// System.out.println("results     = " + t);
				// if(data!=null)
				// System.out.println("data  = " + Arrays.toString(data[0]));

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
		return data;
	}
}
