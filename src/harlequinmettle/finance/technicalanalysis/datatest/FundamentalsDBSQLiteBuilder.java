package harlequinmettle.finance.technicalanalysis.datatest;

import harlequinmettle.finance.database.DataUtil;
import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsDatabase;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FundamentalsDBSQLiteBuilder {

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		FundamentalsDBSQLiteBuilder buildDB = new FundamentalsDBSQLiteBuilder();
		buildDB.buildDB();
		System.out.println("--time: "+(System.currentTimeMillis()-time)/1000);
	}

	private void buildDB() {
		Connection cn = SQLiteTools
				.establishSQLiteConnection("FUNDAMENTALSDATABASE");
		if (cn != null) {
			String tableName = "fundamentals";
			String[] columnEntries = doColumnentries();
			String[] types = new String[columnEntries.length];
			Arrays.fill(types, "real");
			ArrayList<Integer> sqlStorageTypes = new ArrayList<Integer>();
			//day number
			sqlStorageTypes.add(SQLiteTools.SQL_FLOAT_ADD);
			//ticker
			sqlStorageTypes.add(SQLiteTools.SQL_STRING_ADD);
			//ALL THE REST FLOAT
			for(int i = 0; i<200; i++){ 
				sqlStorageTypes.add(SQLiteTools.SQL_FLOAT_ADD);
				
			}
			Statement stat = SQLiteTools.reinitializeTable(cn, tableName,
					columnEntries, types);

			String rootq = new ChooseFilePrompterPathSaved("databasebuilter")
					.getSetting("path to q");
			String rooty = new ChooseFilePrompterPathSaved("databasebuilter")
					.getSetting("path to y");
			ArrayList<File> allSmallDBFiles = new ArrayList<File>();
			allSmallDBFiles.addAll(Arrays.asList(new File(rootq).listFiles()));
			allSmallDBFiles.addAll(Arrays.asList(new File(rooty).listFiles()));
			int NUMBER_ENTRIES = CurrentFundamentalsDatabase.forDisplaying.length + 2;

		 	for (int i = 0; i < allSmallDBFiles.size(); i++) {
		//		for (int i = 0; i <5; i++) {
				ArrayList<ArrayList<String>> allValues = buildDataForDatabaseTable(allSmallDBFiles
						.get(i));
				PreparedStatement prep = SQLiteTools.initPreparedStatement(cn,
						NUMBER_ENTRIES, tableName);
				for (ArrayList<String> values : allValues)
					SQLiteTools.buildSQLStatement(prep, values,sqlStorageTypes);
				SQLiteTools.executeStatement(cn, prep);
			}
 

		}
	}

	private ArrayList<ArrayList<String>> buildDataForDatabaseTable(File file) {
		ArrayList<ArrayList<String>> fileResults = new ArrayList<ArrayList<String>>();
		try {
			String dayNumber = file.getName().replace("nas_", "").replace("ny_", "")
					.replace(".txt", "");
			String fileData = FileUtils.readFileToString(file);
			String[] weeklyEntries = fileData.split("\\n");
			for (String entry : weeklyEntries) {
				ArrayList<String> data = new ArrayList<String>();
				data.add(dayNumber);
				data.addAll(makeListOfSomeData(entry));
				fileResults.add(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileResults;
	}

	private ArrayList<String> makeListOfSomeData(String entry) {
		 
		ArrayList<String> data = new ArrayList<String>();

		List<String> labels = Arrays.asList(CurrentFundamentalsDatabase.labels);
		String[] tickerDataPair = entry.split("\\^");
		String ticker = tickerDataPair[0];
		data.add(ticker);		
		float[] rawData = DataUtil.validSmallDataSet(tickerDataPair[1],
				null);
		if (rawData.length != 175)
			return data; 
	//	System.out.println(Arrays.toString(rawData));
		//String[] allData = tickerDataPair[1].split(" ");
		// System.out.println(allData.length+"      "+Arrays.toString(allData));
		for (String s : CurrentFundamentalsDatabase.forDisplaying) {
			int index = labels.indexOf(s);
			if (index < rawData.length){
				//if(rawData[index] == rawData[index])
					data.add(""+rawData[index]);
		 
		
			}
			}

		return data;
	}

	private String[] doColumnentries() {
		String entries = "dateOfCollection ticker ";
		for (String s : CurrentFundamentalsDatabase.forDisplaying) {
			entries += s.replaceAll("[^A-Za-z0-9]", "_") + " ";
		}
		return entries.trim().split(" ");
	}

}
