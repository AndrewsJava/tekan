package harlequinmettle.finance.technicalanalysis.model.db;

import harlequinmettle.finance.database.DataUtil;
import harlequinmettle.finance.technicalanalysis.datatest.FundamentalsDBSQLiteBuilder;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FundamentalsBlobDBSQLiteBuilder {

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		FundamentalsBlobDBSQLiteBuilder buildDB = new FundamentalsBlobDBSQLiteBuilder();
		buildDB.buildDB();
		System.out.println("--time: " + (System.currentTimeMillis() - time)
				/ 1000);
	}

	private void buildDB() {
		Connection cn = SQLiteTools
				.establishSQLiteConnection("BLOBFUNDAMENTALSDATABASE");
		if (cn != null) {
			String tableName = "fundamentals";
			String[] columnEntries = { "datenumber", "ticker", "data" };
			String[] types = { "dayN", "T", "float[]" };

			// Statement used for query
			Statement stat = SQLiteTools.reinitializeTable(cn, tableName,
					columnEntries, types);

			String rootq = new ChooseFilePrompterPathSaved("databasebuilter")
					.getSetting("path to q");
			String rooty = new ChooseFilePrompterPathSaved("databasebuilter")
					.getSetting("path to y");
			ArrayList<File> allSmallDBFiles = new ArrayList<File>();
			allSmallDBFiles.addAll(Arrays.asList(new File(rootq).listFiles()));
			allSmallDBFiles.addAll(Arrays.asList(new File(rooty).listFiles()));
			int NUMBER_ENTRIES = columnEntries.length;

			ArrayList<Integer> sqlStorageTypes = new ArrayList<Integer>();
			//day number
			sqlStorageTypes.add(SQLiteTools.SQL_FLOAT_ADD);
			//ticker
			sqlStorageTypes.add(SQLiteTools.SQL_STRING_ADD);
			//float[] as byte[]
			sqlStorageTypes.add(SQLiteTools.SQL_BYTES_ADD);
	 	for (int i = 0; i < allSmallDBFiles.size(); i++) {
			//	  for (int i = 0; i <5; i++) {
				ArrayList<ArrayList<Object>> allValues = buildDataForDatabaseTable(allSmallDBFiles
						.get(i));
				PreparedStatement prep = SQLiteTools.initPreparedStatement(cn,
						NUMBER_ENTRIES, tableName);
				for (ArrayList<Object> values : allValues)
					SQLiteTools.buildSQLStatement(prep, values,sqlStorageTypes);
				SQLiteTools.executeStatement(cn, prep);
			}

		}
	}

	private ArrayList<ArrayList<Object>> buildDataForDatabaseTable(File file) {
		ArrayList<ArrayList<Object>> fileResults = new ArrayList<ArrayList<Object>>();
		try {
			String dayNumber = file.getName().replace("nas_", "")
					.replace("ny_", "").replace(".txt", "");
			String fileData = FileUtils.readFileToString(file);
			String[] weeklyEntries = fileData.split("\\n");
			for (String entry : weeklyEntries) {
				ArrayList<Object> data = new ArrayList<Object>();
				data.add(dayNumber);
				String[] tickerDataPair = entry.split("\\^");
				String ticker = tickerDataPair[0];
				data.add(ticker);
				float[] rawData = extractFundamentalData(tickerDataPair[1]);
				 
				 data.add( (rawData));
			 
				fileResults.add(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileResults;
	}

	private float[] extractFundamentalData(String data) { 

		final float[] values = new float[CurrentFundamentalsDatabase.labels.length];
		float[] rawData = 		DataUtil.validSmallDataSet(data,
				null);
		
			for (int k = 0; k < 82; k++) {
				values[k] = rawData[k];
			}
			values[82] = rawData[172];
			values[83] = rawData[173];
			values[84] = rawData[174];
 
 
return values;
	}

}
