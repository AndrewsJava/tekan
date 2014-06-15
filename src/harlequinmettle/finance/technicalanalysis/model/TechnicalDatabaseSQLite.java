package harlequinmettle.finance.technicalanalysis.model;

import harlequinmettle.utils.systemtools.SystemMemoryUseDisplay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class TechnicalDatabaseSQLite {
	public static void main(String[] args) throws Exception {
		SystemMemoryUseDisplay mem = new SystemMemoryUseDisplay();
		for (int i = 0; i < 20; i++)
			runSQLiteQueries();
		System.out.println("DONE ");
	}

	private static void runSQLiteQueries() throws Exception {
		long time = System.currentTimeMillis();
		Class.forName("org.sqlite.JDBC");

		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
		initPragmas(conn);
		Statement stat = conn.createStatement();
		 stat.executeUpdate("drop table if exists people;");
		 stat.executeUpdate("create table people (name, occupation);");
		PreparedStatement prep = conn
				.prepareStatement("insert into people values (?, ?);");
		prep.setString(1, "Gandhi");
		prep.setString(2, "politics");
		prep.addBatch();
		prep.setString(1, "Turing");
		prep.setString(2, "computers");
		prep.addBatch();
		prep.setString(1, "Wittgenstein");
		prep.setString(2, "smartypants");
		prep.addBatch();
		// 1M = 35MB disk
		// 100000 Strings ~100 bytes = 10Mb cache set to 200Mb
		// time: 25957
		// time: 25736
		// time: 24091
		// no change to memecache
		// time: 28204
		// time: 27562
		// time: 29240
		for (int i = 0; i < 3*3000000; i++) {

			prep.setString(1, " " + i);
			prep.setString(2, " " + (i + Math.random()));
			prep.addBatch();
			Thread.yield();

		}
		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

		for (int i = 0; i < 10; i++) {
			ResultSet rs = stat.executeQuery("select * from people;");
			// ArrayList<String> results = new ArrayList<String>( 2*1000000);
			while (rs.next()) {
				String next = rs.getString("name");
				// results.add(next);
//				System.out.println("name = " + rs.getString("name"));
//				System.out.println("job = " + rs.getString("occupation"));
			}
			rs.close();
		}

		// System.out.println("      size = " + results.size());

		conn.close();

		System.out.println("time: " + (System.currentTimeMillis() - time));
	}

	private static void initPragmas(Connection connection) throws SQLException {

		connection.prepareStatement("PRAGMA cache_size = 200000;").execute();
	}
}
