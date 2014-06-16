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
		// 1M = 35MB disk (entry = random float)
		// 1K = 40kb disk ( 10 char string)
		// 100000 Strings ~100 bytes = 10Mb cache set to 200Mb
		SystemMemoryUseDisplay mem = new SystemMemoryUseDisplay();
		for (int i = 0; i < 10; i++)
			runSQLiteQueries(10000 , 1 * i);
		System.out.println("DONE ");
	}

	private static void runSQLiteQueries(int entries, int queries)
			throws Exception {
		long time = System.currentTimeMillis();
		Class.forName("org.sqlite.JDBC");

		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
		//initPragmas(conn, true, true, true);
		Statement stat = reinitializeTable(conn);
		PreparedStatement prep = conn
				.prepareStatement("insert into people values (?, ?);");
		prepareASQLiteStatement(prep, entries);
		executeSQLiteStatement(conn, prep);
		queryDatabaseSome(stat, queries);

		// System.out.println("      size = " + results.size());

		conn.close();

		System.out.println("time: " + (System.currentTimeMillis() - time));
	}

	private static void queryDatabaseSome(Statement stat, int queryCount)
			throws Exception {

		for (int i = 0; i < queryCount; i++) {
			ResultSet rs = stat.executeQuery("select * from people;");
			// ArrayList<String> results = new ArrayList<String>( 2*1000000);
			while (rs.next()) {
				String next = rs.getString("name");
				// results.add(next);
				// System.out.println("name = " + rs.getString("name"));
				// System.out.println("job = " + rs.getString("occupation"));
			}
			rs.close();
		}
	}

	private static Statement reinitializeTable(Connection conn)
			throws Exception {
		Statement stat = conn.createStatement();
		stat.executeUpdate("drop table if exists people;");
		stat.executeUpdate("create table people (name, occupation);");
		return stat;
	}

	private static void executeSQLiteStatement(Connection conn,
			PreparedStatement prep) throws Exception {
		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);
	}

	private static void prepareASQLiteStatement(PreparedStatement prep,
			int numStatements) throws Exception {

		for (int i = 0; i < numStatements; i++) {
			String random = randomString(10000 );
			prep.setString(1, random);
			prep.setString(2, " " + (i + Math.random()));
			prep.addBatch();
			Thread.yield();

		}
	}

	private static String randomString(int stringLength) {
		String random = "";
		for (int i = 0; i < stringLength/100; i++) {

			//random += (char) (26 * Math.random() + 'a');
			random += "aaaaaaaaaajjjjjjjjjjaaaaaaaaaaajjjjjjjjjaaaaaaaaaajjjjjjjjjjaaaaaaaaajjjjjjjjjjaaaaaaaaaajjjjjjjjj";
		}
		return random+Math.random();
	}

	private static void initPragmas(Connection connection,
			boolean journal_mode, boolean synchronousOFF, boolean cachesize)
			throws SQLException {
//WAL, NORMAL, OFF, MEMORY   journal_mode
	 	connection.prepareStatement("PRAGMA temp_store = MEMORY;").execute();
	 	 connection.prepareStatement("PRAGMA auto_vacuum = NONE;").execute();
	 	connection.prepareStatement("PRAGMA synchronous = OFF").execute();
  //	connection.prepareStatement("PRAGMA journal_mode = OFF").execute();
 	connection.prepareStatement("PRAGMA cache_size = 200000;").execute();
	}
}
