package harlequinmettle.finance.technicalanalysis.datatest;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsDatabase;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class FundamentalsDBSQLiteBuilder {

	public static void main(String[] args) {
		FundamentalsDBSQLiteBuilder buildDB = new FundamentalsDBSQLiteBuilder();
		buildDB.buildDB();
	}

	private void buildDB() {
		Connection cn = SQLiteTools
				.establishSQLiteConnection("FUNDAMENTSLSDATABASE");
		if (cn != null) {
			String tableName = "fundamentals";
			// Statement used for query
			Statement stat = SQLiteTools.reinitializeTable(cn, tableName,
					CurrentFundamentalsDatabase.forDisplaying);
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

}
