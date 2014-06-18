package harlequinmettle.finance.technicalanalysis.datatest;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsDatabase;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;

public class FundamentalsDBSQLiteBuilder {

	public static void main(String[] args) {
		FundamentalsDBSQLiteBuilder buildDB = new FundamentalsDBSQLiteBuilder();
		buildDB.buildDB();
	}

	private void buildDB() {
		Connection cn = SQLiteTools
				.establishSQLiteConnection("FUNDAMENTALSDATABASE");
		if (cn != null) {
			String tableName = "fundamentals";
			String[] columnEntries = doColumnentries();
			String[] types = new String[columnEntries.length];
			Arrays.fill(types, "real");
			// Statement used for query
			Statement stat = SQLiteTools.reinitializeTable(cn, tableName,
					columnEntries,types);
			
			// TODO:FIXME: SEE SQLiteTester for SQLite statement exectution
			//
			// PreparedStatement prep = cn
			// .prepareStatement("insert into "+tableName+" values (?, ?);");
			// //for float[] s of data
			// SQLiteTools.enterRecord(stat, String[]);
			//
			// SQLiteTools.executeSQLiteStatement(cn, );
			//

		}
	}

	private String[] doColumnentries() {
		String entries = "ticker dateOfCollection ";
	for(String s: CurrentFundamentalsDatabase.forDisplaying){
		entries+=s.replaceAll("[^A-Za-z0-9]", "_")+" ";
	}
	return entries.trim().split(" ");
	}

}
