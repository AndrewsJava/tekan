package harlequinmettle.finance.technicalanalysis.sqlitedatabasebuilders;

import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.FileTools;
import harlequinmettle.utils.filetools.sqlite.SQLiteTools;
import harlequinmettle.utils.timetools.TimeRecord;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class TechnicalDBSQLiteBlobsBuilder {

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		TechnicalDBSQLiteBlobsBuilder buildDB = new TechnicalDBSQLiteBlobsBuilder();
		buildDB.buildDB();
		System.out.println("--time: " + (System.currentTimeMillis() - time)
				/ 1000);
	}

	public  void buildDB() {
		Connection cn = SQLiteTools
				.establishSQLiteConnection("BLOBTECHNICALSDATABASE");
		if (cn != null) {
			String tableName = "technicaldatatable";
			String[] columnEntries = { "ticker", "data" };
			String[] types = { "String", "float[][]" };

			// Statement used for query
			Statement stat = SQLiteTools.reinitializeTable(cn, tableName,
					columnEntries, types);

			String rootq = new ChooseFilePrompterPathSaved("application_settings","databasebuilter")
					.getSetting("path to technicals csv files");
			ArrayList<File> allCSVDataFiles = new ArrayList<File>();
			allCSVDataFiles.addAll(Arrays.asList(new File(rootq).listFiles()));
			restrictToCSVFiles(allCSVDataFiles);
			int NUMBER_ENTRIES = columnEntries.length;

			ArrayList<Integer> sqlStorageTypes = new ArrayList<Integer>();
			// ticker
			sqlStorageTypes.add(SQLiteTools.SQL_STRING_ADD);
			// float[][] as byte[]
			sqlStorageTypes.add(SQLiteTools.SQL_BYTES_ADD);
			for (int i = 0; i < allCSVDataFiles.size(); i++) {
				// for (int i = 0; i < 5; i++) {
				ArrayList<Object> dbEntry = new ArrayList<Object>();
				String fileName = allCSVDataFiles.get(i).getName();

				dbEntry.add(fileName.substring(0, fileName.length() - 4));
				dbEntry.add(buildDataForDatabaseTable(allCSVDataFiles.get(i)));
				PreparedStatement prep = SQLiteTools.initPreparedStatement(cn,
						NUMBER_ENTRIES, tableName);
				SQLiteTools.buildSQLStatement(prep, dbEntry, sqlStorageTypes);
				SQLiteTools.executeStatement(cn, prep);
			}

		}
	}

	private float[][] buildDataForDatabaseTable(File file) {
		float[][] technicalData = null;
		String rawTechnicalData = FileTools.tryToReadFileToString(file, null);
		if (rawTechnicalData == null)
			return null;
		technicalData = convertStringToFloatArray(rawTechnicalData);
		return technicalData;
	}

	private float[][] convertStringToFloatArray(String rawTechnicalData) {
		String[] dailyEntries = rawTechnicalData.split(System.lineSeparator());
		// csv daily data is seven columns: Date Open High Low Close Volume Adj
		float[][] technicalData = new float[dailyEntries.length - 1][7];
		for (int i = 1; i < dailyEntries.length; i++) {
			technicalData[i - 1] = convertLineToNumbers(dailyEntries[i]);
		}
		return technicalData;
	}

	private float[] convertLineToNumbers(String string) {
		String[] data = string.split(",");
		float[] dayData = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			dayData[i] = tryConvertStringToNumber(data[i].trim());
		}
		return dayData;
	}

	private float tryConvertStringToNumber(String trim) {
		float data = Float.NaN;
		// if its a date parse it and add it
		try {

			return TimeRecord.dayNumber(new SimpleDateFormat("yyyy-MM-dd")
					.parse(trim).getTime());

		} catch (Exception e) {
		}
		// if its a number valueof and add it
		try {
			return (Float.valueOf(trim));
		} catch (Exception e) {
		}
		return data;
	}

	private void restrictToCSVFiles(ArrayList<File> allSmallDBFiles) {
		ArrayList<File> toRemove = new ArrayList<File>();
		for (File f : allSmallDBFiles) {
			if (f.isDirectory() || !f.getName().endsWith("csv"))
				toRemove.add(f);
		}
		allSmallDBFiles.removeAll(toRemove);
	}

}
