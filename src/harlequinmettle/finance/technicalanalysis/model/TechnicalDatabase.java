package harlequinmettle.finance.technicalanalysis.model;

import harlequinmettle.utils.TimeRecord;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.finance.TickerSetWithETFs;
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
	public static final String[] elements = {d,o,h,l,c,v,a};
	public static final int NUM_YRS = 2;
	public static final int NUM_DAYS = 365 * NUM_YRS;
	public static final int TODAY = (int) TimeRecord.dayNumber(System
			.currentTimeMillis());
	public static String settingsFileName = ".technical_database_settings";
	private static Settings settings;
	// String ticker maps to time series map daynumber->value
	// public static final TreeMap<String, TreeMap<Float, float[]>>
	// PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, TreeMap<Float,
	// float[]>>();
	// <ticker, [day][technical data]>
	public static TreeMap<String, float[][]> PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, float[][]>();
	public static TreeMap<String,TreeMap<String,String>> PER_TICKER_DIVIDEND_DAY_MAP = new TreeMap<String,TreeMap<String,String>>();

	long time = System.currentTimeMillis();
	static {
		long time = System.currentTimeMillis();

		for (String ticker : TickerSetWithETFs.TICKERS) {
			PER_TICKER_PER_DAY_TECHNICAL_DATA.put(ticker,
					new float[NUM_DAYS][]);
			PER_TICKER_DIVIDEND_DAY_MAP.put(ticker, new TreeMap<String,String>());
		} 
		System.out.println("time: " + (System.currentTimeMillis() - time));
	}

	public TechnicalDatabase() {
		int count = 0;
		restoreSettings();
		while (settings.rootPath == null || settings.rootPath.length() < 1) {
			JFrame pathToDB = new JFrame("set path to ticker technical csv files");
			pathToDB.setSize(new Dimension(600,100));
			pathToDB.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			pathToDB.setVisible(true);
			settings.rootPath = dbRootPathChooser();
			SerializationTool.serialize(settings, settingsFileName);
		}
		if (new File("TECHNICAL_DATA/OBJECTS/TECHDATAOBJ" + NUM_DAYS)
				.exists()) {
			PER_TICKER_PER_DAY_TECHNICAL_DATA = SerializationTool.deserialize(
					PER_TICKER_PER_DAY_TECHNICAL_DATA.getClass(),
					"TECHNICAL_DATA/OBJECTS/TECHDATAOBJ" + NUM_DAYS);
		} else {
			File[] files = new File(settings.rootPath).listFiles();
			for (File file : files) {
				// / if(count>110)break;
				System.out.println(count++ + "     " + file.getName());
				if (file.isDirectory() && file.getName().equals("DIVIDENDS")) {
					loadDividends(file);
				} else if (file.isFile()) {
					loadTechnicalData(file);
				}
			}
			SerializationTool.serialize(PER_TICKER_PER_DAY_TECHNICAL_DATA,
					"TECHNICAL_DATA/OBJECTS/TECHDATAOBJ" + NUM_DAYS);

		}
		System.out
				.println("technical database successfully created with up to: "
						+ NUM_YRS + "  years of data");
		System.out.println("in : " + (System.currentTimeMillis() - time));

	}

	private void restoreSettings() {
		settings = SerializationTool.deserialize(Settings.class,
				settingsFileName);
		if (settings == null)
			settings = new Settings();
	}

	private void loadTechnicalData(File file) {
		// break off the .csv from the filename
		String ticker = file.getName()
				.substring(0, file.getName().length() - 4);
		try {
			String data = FileUtils.readFileToString(file);
			for (String dayData : data.split(System.lineSeparator())) {
				addDaysDataToDatabase(ticker, dayData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void addDaysDataToDatabase(String ticker, String dayData) {
		String[] data = dayData.split(",");
		ArrayList<Float> numbers = new ArrayList<Float>();
		for (String numString : data) {
			tryToAddData(numString, numbers);
		}
		// csv daily data is seven columns: Date Open High Low Close Volume Adj
		// Close
		// System.out.println(numbers);
		if (numbers.size() == 7) {
			float date = numbers.get(0);
			if (date < TODAY - NUM_DAYS)
				return;
			float open = numbers.get(1);
			float high = numbers.get(2);
			float low = numbers.get(3);
			float close = numbers.get(4);
			float volume = numbers.get(5);
			float adjclose = numbers.get(6);

			float[] dataForDay = { date, open, high, low, close, volume,
					adjclose };
			PER_TICKER_PER_DAY_TECHNICAL_DATA.get(ticker)[(int) date
					- (TODAY - NUM_DAYS)] = dataForDay;
		}
	}

	private void tryToAddData(String numString, ArrayList<Float> numbers) {
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
public static TreeMap<Float,Float> makeGetDataToDateMap(String ticker,int datapoint){
	TreeMap<Float,Float> map = new TreeMap<Float,Float>();
	float[][] tickerdata = PER_TICKER_PER_DAY_TECHNICAL_DATA.get(ticker);
	for(float[] day: tickerdata){
		if(day!=null)
		map.put(day[0], day[datapoint]);
	}
	return map;
}
	private void loadDividends(File file) {
		File[] files = file.listFiles();
		// TODO: LOAD DIVIDNED DATA FOR EACH FILE
	}

	public static void main(String[] arg) {
		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		smu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TechnicalDatabase quotes = new TechnicalDatabase();

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
