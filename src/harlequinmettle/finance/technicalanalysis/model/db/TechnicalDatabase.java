package harlequinmettle.finance.technicalanalysis.model.db;

import harlequinmettle.utils.TimeRecord;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.finance.TickerSetWithETFsOptimized;
import harlequinmettle.utils.systemtools.SystemMemoryUseDisplay;

import java.awt.Dimension;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;

public class TechnicalDatabase {
	public static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static final int DATE = 0;
	public static final int OPEN = 1;
	public static final int HIGH = 2;
	public static final int LOW = 3;
	public static final int CLOSE = 4;
	public static final int VOLUME = 5;
	public static final int ADJCLOSE = 6;

	public static final String d = "date";
	public static final String o = "open";
	public static final String h = "high";
	public static final String l = "low";
	public static final String c = "close";
	public static final String v = "vol";
	public static final String a = "adjcls";
	public static final String[] elements = { d, o, h, l, c, v, a };
	// defaults
	public static  int NUM_YRS_START = 5;
	public static  int NUM_DAYS_START = 365 * NUM_YRS_START;
	public static  int NUM_YRS_END = 0;
	public static  int NUM_DAYS_END = 365 * NUM_YRS_END;
	public static  int TOTAL_NUM_DAYS = (NUM_DAYS_START - NUM_DAYS_END);
	public static   int COLLECTION_DAY_NUMBER = (int) TimeRecord.dayNumber(1402664533867l);

 
	public   float PERCENT_TO_DISCARD = 0.01f;
	// String ticker maps to time series map daynumber->value
	// public static final TreeMap<String, TreeMap<Float, float[]>>
	// PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, TreeMap<Float,
	// float[]>>();
	// <ticker, [day][technical data]>
	public static TreeMap<String, float[][]> PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, float[][]>();

	long time = System.currentTimeMillis();
	public   String pathToObj = "technical_database_settings";
	String pathtodata = "path to technicals price data csv";
	String rootPathToTechnicals = "path not set";
	public TechnicalDatabase(int oldestYearsAgo, int newestYearsAgo) {

		NUM_YRS_START = oldestYearsAgo;
		NUM_DAYS_START = 365 * NUM_YRS_START;
		NUM_YRS_END = newestYearsAgo;
		NUM_DAYS_END = 365 * NUM_YRS_END;
		TOTAL_NUM_DAYS = (NUM_DAYS_START - NUM_DAYS_END);
		allocateMemory();
		int count = 0;
		restoreSettings();
 
		
		if (new File("TECHNICAL_DATA/OBJECTS/TECHDATAOBJ_" + NUM_DAYS_START
				+ "_" + NUM_DAYS_END).exists()) {
			PER_TICKER_PER_DAY_TECHNICAL_DATA = SerializationTool.deserialize(
					PER_TICKER_PER_DAY_TECHNICAL_DATA.getClass(),
					"TECHNICAL_DATA/OBJECTS/TECHDATAOBJ_" + NUM_DAYS_START
							+ "_" + NUM_DAYS_END);
		} else {
			File[] files = new File(rootPathToTechnicals).listFiles();
			for (File file : files) {
				// / if(count>110)break;
				System.out.println(count++ + "     " + file.getName());
			  if (file.isFile() && file.getName().endsWith(".csv")) {
					loadTechnicalData(file);
				}
			}
			SerializationTool.serialize(PER_TICKER_PER_DAY_TECHNICAL_DATA,
					"TECHNICAL_DATA/OBJECTS/TECHDATAOBJ_" + NUM_DAYS_START
							+ "_" + NUM_DAYS_END);

		}
		System.out
				.println("technical database successfully created with up to: "
						+ TOTAL_NUM_DAYS + "  days of data");
		System.out.println("in : " + (System.currentTimeMillis() - time));
		System.out.println("memory used : "
				+ (Runtime.getRuntime().totalMemory() / 1000000) + "   MB");

	}

	private void allocateMemory() {
		// long F = 1000000;
		// long time = System.currentTimeMillis();
		// long maxMemory = Runtime.getRuntime().maxMemory() ;
		// long proposedMemoryUse = 32 * NUM_DAYS
		// * TickerSetWithETFsOptimized.TICKERS.size() * 7 ;
		//
		// System.out.println("max memory available: " + (maxMemory));
		// System.out.println("    " +
		// (TickerSetWithETFsOptimized.TICKERS.size()));
		// System.out.println("    " + (NUM_DAYS));
		// System.out.println("database with " + NUM_DAYS +
		// " days will require: "
		// + proposedMemoryUse);

		// TreeMap<String, Float> maxDays = readNumbersFromFile();
		for (String ticker : TickerSetWithETFsOptimized.TICKERS) {
			// float numDays = maxDays.get(ticker);
			// if (numDays < 2500)
			// continue;
			PER_TICKER_PER_DAY_TECHNICAL_DATA.put(ticker,
					new float[TOTAL_NUM_DAYS][]);

		} 
		System.out.println("time: " + (System.currentTimeMillis() - time));
	}

	private TreeMap<String, Float> readNumbersFromFile() {
		TreeMap<String, Float> maxDays = new TreeMap<String, Float>();
		File sizes = new File(rootPathToTechnicals+ File.separatorChar
				+ "tickerhistorysize/tickerhistorysize.txt");

		try {
			String tickersDays = FileUtils.readFileToString(sizes);
			String[] perTicker = tickersDays.split(System.lineSeparator());
			for (String pair : perTicker) {
				String[] p = pair.split(" ");
				maxDays.put(p[0], Float.valueOf(p[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return maxDays;
	}

	private void restoreSettings() {
		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
				pathToObj);
		rootPathToTechnicals = settingssaver.getSetting(pathtodata);
	}

	private void loadTechnicalData(File file) {
		// break off the .csv from the filename
		String ticker = file.getName().replaceAll(".csv", "");
		if (!PER_TICKER_PER_DAY_TECHNICAL_DATA.containsKey(ticker))
			return;

		int daysOfValidData = 0;
		try {
			String data = FileUtils.readFileToString(file);
			for (String dayData : data.split(System.lineSeparator())) {
				daysOfValidData += addDaysDataToDatabase(ticker, dayData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//OPTION CHECK PROFILE FOR RECNET YEAR MAYBE KEEP IN DATA
		if (daysOfValidData < (PERCENT_TO_DISCARD * TOTAL_NUM_DAYS)) {
			PER_TICKER_PER_DAY_TECHNICAL_DATA.remove(ticker);
		}

	}

	private int addDaysDataToDatabase(String ticker, String dayData) {
		String[] data = dayData.split(",");
		ArrayList<Float> numbers = new ArrayList<Float>();
		for (String numString : data) {
			tryToAddData(numString, numbers);
		}
		// csv daily data is seven columns: Date Open High Low Close Volume Adj
		if (numbers.size() == 7) {
			float date = numbers.get(0);
			if (date < COLLECTION_DAY_NUMBER - NUM_DAYS_START)
				return 0;
			if (date >= COLLECTION_DAY_NUMBER - NUM_DAYS_END)
				return 0;
			float open = numbers.get(1);
			float high = numbers.get(2);
			float low = numbers.get(3);
			float close = numbers.get(4);
			float volume = numbers.get(5);
			float adjclose = numbers.get(6);

			float[] dataForDay = { date, open, high, low, close, volume,
					adjclose };
			int dayNumberIndexedToArray = (int) date
					- (COLLECTION_DAY_NUMBER - NUM_DAYS_START) ;
			PER_TICKER_PER_DAY_TECHNICAL_DATA.get(ticker)[dayNumberIndexedToArray] = dataForDay;
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

	public static TreeMap<Float, Float> makeGetDataToDateMap(String ticker,
			int datapoint) {
		TreeMap<Float, Float> map = new TreeMap<Float, Float>();
		float[][] tickerdata = PER_TICKER_PER_DAY_TECHNICAL_DATA.get(ticker);
		for (float[] day : tickerdata) {
			if (day != null)
				map.put(day[0], day[datapoint]);
		}
		System.out.println("data point count for ticker  " + ticker + "    "
				+ map.size());
		return map;
	}

	private void loadDividends(File file) {
		File[] files = file.listFiles();
		// TODO: LOAD DIVIDNED DATA FOR EACH FILE
	}

	public static void main(String[] arg) {
		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		smu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TechnicalDatabase quotes = new TechnicalDatabase(20, 10);

		System.out.println("time: "
				+ (System.currentTimeMillis() - quotes.time));
	}

	private String dbRootPathChooser() {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file.getAbsolutePath();

		}
		return null;
	}
}
