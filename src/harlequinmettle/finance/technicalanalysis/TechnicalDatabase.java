package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.finance.tickerset.TickerSet;
import harlequinmettle.utils.TimeRecord;
import harlequinmettle.utils.debugtools.InstanceCounter;
import harlequinmettle.utils.numbertools.math.statistics.StatInfo;
import harlequinmettle.utils.systemtools.SystemMemoryUsage;

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
	// String ticker maps to time series map daynumber->value
	public static final TreeMap<String, TreeMap<Float, Float>> OPEN = new TreeMap<String, TreeMap<Float, Float>>();
	public static final TreeMap<String, TreeMap<Float, Float>> HIGH = new TreeMap<String, TreeMap<Float, Float>>();
	public static final TreeMap<String, TreeMap<Float, Float>> LOW = new TreeMap<String, TreeMap<Float, Float>>();
	public static final TreeMap<String, TreeMap<Float, Float>> CLOSE = new TreeMap<String, TreeMap<Float, Float>>();
	public static final TreeMap<String, TreeMap<Float, Float>> VOLUME = new TreeMap<String, TreeMap<Float, Float>>();
	public static final TreeMap<String, TreeMap<Float, Float>> ADJCLOSE = new TreeMap<String, TreeMap<Float, Float>>();
	public static final TreeMap<String, TreeMap<Float, Float>> DIVIDEND = new TreeMap<String, TreeMap<Float, Float>>();
	// public static final TreeMap<Float,Float> DIVIDEND_PERCENT = new
	// TreeMap<Float,Float>();
	public static final InstanceCounter DATE_COUNTER = new InstanceCounter();
	public static final InstanceCounter OPEN_COUNTER = new InstanceCounter();
	public static final InstanceCounter HIGH_COUNTER = new InstanceCounter();
	public static final InstanceCounter LOW_COUNTER = new InstanceCounter();
	public static final InstanceCounter CLOSE_COUNTER = new InstanceCounter();
	public static final InstanceCounter VOLUME_COUNTER = new InstanceCounter();
	public static final InstanceCounter ADJCLOSE_COUNTER = new InstanceCounter();
	public static final InstanceCounter DIVIDEND_COUNTER = new InstanceCounter();

	public static final ArrayList<Float> DATE_VALUES = new ArrayList<Float>();

	public static final ArrayList<Float> OPEN_VALUES = new ArrayList<Float>();
	public static final ArrayList<Float> HIGH_VALUES = new ArrayList<Float>();
	public static final ArrayList<Float> LOW_VALUES = new ArrayList<Float>();
	public static final ArrayList<Float> CLOSE_VALUES = new ArrayList<Float>();
	public static final ArrayList<Float> VOLUME_VALUES = new ArrayList<Float>();
	public static final ArrayList<Float> ADJCLOSE_VALUES = new ArrayList<Float>();
	public static final ArrayList<Float> DIVIDEND_VALUES = new ArrayList<Float>();
	public static final ArrayList<ArrayList<Float>> DB_VALUES = new ArrayList<ArrayList<Float>>();

	// public static final StatInfo DATE_STATS = new StatInfo();
	// public static final StatInfo OPEN_STATS = new StatInfo();
	// public static final StatInfo HIGH_STATS = new StatInfo();
	// public static final StatInfo LOW_STATS = new StatInfo();
	// public static final StatInfo CLOSE_STATS = new StatInfo();
	// public static final StatInfo VOLUME_STATS = new StatInfo();
	// public static final StatInfo ADJCLOSE_STATS = new StatInfo();
	// public static final StatInfo DIVIDEND_STATS = new StatInfo();

	public static final ArrayList<TreeMap<String, TreeMap<Float, Float>>> TECHNICAL_DB_DATA_POINTS = new ArrayList<TreeMap<String, TreeMap<Float, Float>>>();

	long time = System.currentTimeMillis();
	static {
		long time = System.currentTimeMillis();
		TECHNICAL_DB_DATA_POINTS.add(OPEN);
		TECHNICAL_DB_DATA_POINTS.add(HIGH);
		TECHNICAL_DB_DATA_POINTS.add(LOW);
		TECHNICAL_DB_DATA_POINTS.add(CLOSE);
		TECHNICAL_DB_DATA_POINTS.add(VOLUME);
		TECHNICAL_DB_DATA_POINTS.add(ADJCLOSE);
		TECHNICAL_DB_DATA_POINTS.add(DIVIDEND);
		DB_VALUES.add(OPEN_VALUES);
		DB_VALUES.add(HIGH_VALUES);
		DB_VALUES.add(LOW_VALUES);
		DB_VALUES.add(CLOSE_VALUES);
		DB_VALUES.add(VOLUME_VALUES);
		DB_VALUES.add(ADJCLOSE_VALUES);
		DB_VALUES.add(DIVIDEND_VALUES);
		for (TreeMap<String, TreeMap<Float, Float>> dataset : TECHNICAL_DB_DATA_POINTS) {
			for (String ticker : TickerSet.TICKERS) {
				dataset.put(ticker, new TreeMap<Float, Float>());
			}
		}
		System.out.println("time: " + (System.currentTimeMillis() - time));
	}

	public TechnicalDatabase(String root) {
		int count = 0;
		while (root == null || root.length() < 1) {
			root = dbRootPathChooser();
		}
		File[] files = new File(root).listFiles();
		for (File file : files) {
			System.out.println(count++ + "     "+file.getName());
			if (file.isDirectory()) {
				loadDividends(file);
			} else {
				loadTechnicalData(file);
			}
		}
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

		if (numbers.size() == 7) {
			float date = numbers.get(0);
			float open = numbers.get(1);
			OPEN_VALUES.add(open);
			OPEN.get(ticker).put(date, open);
			float high = numbers.get(2);
			HIGH_VALUES.add(high);
			HIGH.get(ticker).put(date, high);
			float low = numbers.get(3);
			LOW_VALUES.add(low);
			LOW.get(ticker).put(date, low);
			float close = numbers.get(4);
			CLOSE_VALUES.add(close);
			CLOSE.get(ticker).put(date, close);
			float volume = numbers.get(5);
			VOLUME_VALUES.add(volume);
			VOLUME.get(ticker).put(date, volume);
			float adjclose = numbers.get(6);
			ADJCLOSE_VALUES.add(adjclose);
			ADJCLOSE.get(ticker).put(date, adjclose);
		}
	}

	private void tryToAddData(String numString, ArrayList<Float> numbers) {
		// if its a date parse it and add it
		try {
			float daynumber = TimeRecord.dayNumber(REPORT_DATE_FORMAT.parse(
					numString).getTime());
			numbers.add(daynumber);
			DATE_COUNTER.add(daynumber);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		// if its a number addit
		try {
			numbers.add(Float.valueOf(numString));
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private void loadDividends(File file) {
		File[] files = file.listFiles();
		// TODO: LOAD DIVIDNED DATA FOR EACH FILE
	}

	public static void main(String[] arg) {
		SystemMemoryUsage smu = new SystemMemoryUsage();
		TechnicalDatabase quotes = new TechnicalDatabase(null);
		for (ArrayList<Float> valueset : DB_VALUES) {
			new StatInfo(valueset);
		}
		// DATE_COUNTER.printlnCount();
		DATE_COUNTER.printlnSize();

		System.out.println("time: "
				+ (System.currentTimeMillis() - quotes.time));
		for (TreeMap<String, TreeMap<Float, Float>> dataset : TECHNICAL_DB_DATA_POINTS) {

		}
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
