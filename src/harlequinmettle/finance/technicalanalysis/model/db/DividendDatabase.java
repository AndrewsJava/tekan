package harlequinmettle.finance.technicalanalysis.model.db;

import harlequinmettle.utils.TimeRecord;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.systemtools.SystemMemoryUseDisplay;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

public class DividendDatabase {
	public static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");
	int count = 0;	
	public   String pathToObj = "technical_database_settings";
	String pathtodata = "path to dividends";
	String divRoot = "path not set";
	public static TreeMap<String, TreeMap<Float, Float>> PER_TICKER_DIVIDEND_DAY_MAP = new TreeMap<String, TreeMap<Float, Float>>();
	

	public DividendDatabase( ) {
		init();
	}

	private void init() {
		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
				pathToObj);
		divRoot = settingssaver.getSetting(pathtodata);
		loadDividendData();		
		System.out.println(PER_TICKER_DIVIDEND_DAY_MAP.size());
//		for(TreeMap<Float,Float> ent: PER_TICKER_DIVIDEND_DAY_MAP.values()){
//			System.out.println(ent);
//		}
	}

	private void loadDividendData() {

		if (new File("TECHNICAL_DATA/OBJECTS/DIVDATAOBJ_").exists()) {
			PER_TICKER_DIVIDEND_DAY_MAP = SerializationTool.deserialize(
					PER_TICKER_DIVIDEND_DAY_MAP.getClass(),
					"TECHNICAL_DATA/OBJECTS/DIVDATAOBJ_");
		} else {
			File[] files = new File(divRoot).listFiles();
			for (File file : files) {
				// / if(count>110)break;
				System.out.println(count++ + "     " + file.getName());
				if (file.isFile() && file.getName().endsWith(".csv")) {
					loadDividendData(file);
				}
			}
			SerializationTool.serialize(PER_TICKER_DIVIDEND_DAY_MAP,
					"TECHNICAL_DATA/OBJECTS/DIVDATAOBJ_");

		}
	}

	private void loadDividendData(File file) {

		// break off the .csv from the filename
		String ticker = file.getName().replaceAll(".csv", "");
	 

		int daysOfValidData = 0;
		try {
			String data = FileUtils.readFileToString(file);
			for (String dayData : data.split(System.lineSeparator())) {
				daysOfValidData += addDaysDataToDatabase(ticker, dayData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// OPTION CHECK PROFILE FOR RECNET YEAR MAYBE KEEP IN DATA
		// if (daysOfValidData < (PERCENT_TO_DISCARD * TOTAL_NUM_DAYS)) {
		// PER_TICKER_PER_DAY_TECHNICAL_DATA.remove(ticker);
		// }
	}

	private int addDaysDataToDatabase(String ticker, String dayData) {
		String[] data = dayData.split(",");
		ArrayList<Float> numbers = new ArrayList<Float>();
		for (String numString : data) {
			tryToAddData(numString, numbers);
		}
		// csv daily data is 2 columns: Date Dividend
		if (numbers.size() == 2) {
			float date = numbers.get(0);
			// if (date < COLLECTION_DAY_NUMBER - NUM_DAYS_START)
			// return 0;
			// if (date >= COLLECTION_DAY_NUMBER - NUM_DAYS_END)
			// return 0;
			float div = numbers.get(1);
			if (PER_TICKER_DIVIDEND_DAY_MAP.containsKey(ticker)) {
				PER_TICKER_DIVIDEND_DAY_MAP.get(ticker).put(date, div);
			} else {
				PER_TICKER_DIVIDEND_DAY_MAP.put(ticker,
						new TreeMap<Float, Float>());
				PER_TICKER_DIVIDEND_DAY_MAP.get(ticker).put(date, div);
			}
			return 1;
		}
		return 0;
	}

	private static void tryToAddData(String numString, ArrayList<Float> numbers) {
		// if its a date parse it and add it
		try {
			float daynumber = TimeRecord.dayNumber(REPORT_DATE_FORMAT.parse(
					numString).getTime());
			numbers.add(daynumber);
		} catch (Exception e) {
		}
		// if its a number valueof and add it
		try {
			numbers.add(Float.valueOf(numString));
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		DividendDatabase ddb = new DividendDatabase( );
 
	}

}
