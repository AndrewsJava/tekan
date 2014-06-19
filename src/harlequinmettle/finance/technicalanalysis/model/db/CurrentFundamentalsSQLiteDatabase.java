package harlequinmettle.finance.technicalanalysis.model.db;

import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrentFundamentalsSQLiteDatabase {

	public String pathToObj = "technical_database_settings";
	String pathtodata = "path to small sqlite database";

	public static void main(String[] args) {
		CurrentFundamentalsSQLiteDatabase sqlitedb = new CurrentFundamentalsSQLiteDatabase();
	}

	public CurrentFundamentalsSQLiteDatabase() {
		init();
	}

	private void init() {
		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
				pathToObj);
		String root = settingssaver.getSetting(pathtodata);

		Connection cn = SQLiteTools.establishSQLiteConnection(new File(root));
		if (cn != null) {

			String tableName = "fundamentals";
			System.out.println("db connection eastablished: " + cn.toString());
			queryDatabase(cn,tableName);
		}
	}

	void queryDatabase(Connection conn, String tableName) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("SELECT * FROM " + tableName);

			ResultSet rs = stmt.executeQuery();
			// ArrayList<String> results = new ArrayList<String>( 2*1000000);
			while (rs.next()) {
				String next = rs.getString("tr_ticker");

				System.out.println("name = " + next);
				// System.out.println("job = " + rs.getString("occupation"));
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
}
