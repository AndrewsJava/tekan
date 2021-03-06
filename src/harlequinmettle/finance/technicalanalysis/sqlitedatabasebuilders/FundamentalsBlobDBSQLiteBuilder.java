package harlequinmettle.finance.technicalanalysis.sqlitedatabasebuilders;

import harlequinmettle.finance.database.DataUtil;
import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

public class FundamentalsBlobDBSQLiteBuilder {

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		FundamentalsBlobDBSQLiteBuilder buildDB = new FundamentalsBlobDBSQLiteBuilder();
		buildDB.buildDB();
		System.out.println("--time: " + (System.currentTimeMillis() - time) / 1000);
	}

	private void buildDB() {
		Connection cn = SQLiteTools.establishSQLiteConnection("BLOBFUNDAMENTALSDATABASE");
		if (cn != null) {
			String tableName = "fundamentals";
			String[] columnEntries = { "datenumber", "ticker", "data" };
			String[] types = { "dayN", "T", "float[]" };

			// Statement used for query
			Statement stat = SQLiteTools.reinitializeTable(cn, tableName, columnEntries, types);

			ArrayList<File> allSmallDBFiles = new ArrayList<File>();

			String rootq = new ChooseFilePrompterPathSaved("application_settings", "databasebuilter").getSetting("path to q");
			allSmallDBFiles.addAll(Arrays.asList(new File(rootq).listFiles()));

			String rooty = new ChooseFilePrompterPathSaved("application_settings", "databasebuilter").getSetting("path to y");
			allSmallDBFiles.addAll(Arrays.asList(new File(rooty).listFiles()));

			String roots = new ChooseFilePrompterPathSaved("application_settings", "databasebuilter").getSetting("path to suplementals");
			allSmallDBFiles.addAll(Arrays.asList(new File(roots).listFiles()));

			int NUMBER_ENTRIES = columnEntries.length;

			ArrayList<Integer> sqlStorageTypes = new ArrayList<Integer>();
			// day number
			sqlStorageTypes.add(SQLiteTools.SQL_FLOAT_ADD);
			// ticker
			sqlStorageTypes.add(SQLiteTools.SQL_STRING_ADD);
			// float[] as byte[]
			sqlStorageTypes.add(SQLiteTools.SQL_BYTES_ADD);
			for (int i = 0; i < allSmallDBFiles.size(); i++) {
				// for (int i = 0; i <5; i++) {
				ArrayList<ArrayList<Object>> allValues = buildDataForDatabaseTable(allSmallDBFiles.get(i));
				PreparedStatement prep = SQLiteTools.initPreparedStatement(cn, NUMBER_ENTRIES, tableName);
				for (ArrayList<Object> values : allValues)
					SQLiteTools.buildSQLStatement(prep, values, sqlStorageTypes);
				SQLiteTools.executeStatement(cn, prep);
			}

		}
	}

	private ArrayList<ArrayList<Object>> buildDataForDatabaseTable(File file) {
		ArrayList<ArrayList<Object>> fileResults = new ArrayList<ArrayList<Object>>();
		try {
			String dayNumber = file.getName().split("_")[1].replace(".txt", "");
			String fileData = FileUtils.readFileToString(file);
			String[] weeklyEntries = fileData.split("\\n");
			for (String entry : weeklyEntries) {
				ArrayList<Object> data = new ArrayList<Object>();
				data.add(dayNumber);
				String[] tickerDataPair = entry.split("\\^");
				//System.out.println(Arrays.toString(tickerDataPair));
				String ticker = tickerDataPair[0];
				data.add(ticker);
				float[] rawData = extractFundamentalData(tickerDataPair[1]);
				data.add((rawData));

				fileResults.add(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileResults;
	}

	private float[] extractFundamentalData(String data) {
		final float[] values = new float[CurrentFundamentalsSQLiteDatabase.labels.length];
		float[] rawData = DataUtil.validSmallDataSet(data, null);

		try {
			for (int k = 0; k < 82; k++) {
				values[k] = rawData[k];
			}
			values[82] = rawData[172];
			values[83] = rawData[173];
			values[84] = rawData[174];
		} catch (Exception e) {
		}
		return values;
	}

}
