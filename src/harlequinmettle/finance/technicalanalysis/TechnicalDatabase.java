package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.finance.tickerset.TickerSet;
import harlequinmettle.utils.TimeRecord;
import harlequinmettle.utils.debugtools.InstanceCounter;
import harlequinmettle.utils.filetools.SerializationTool;
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
	public static final int NUMBER_OF_DAYS = 365 * 20;
	public static final int TODAY = (int) TimeRecord.dayNumber(System
			.currentTimeMillis());
	public static String settingsFileName = ".technical_database_settings";
	public static Settings settings;
	// String ticker maps to time series map daynumber->value
	// public static final TreeMap<String, TreeMap<Float, float[]>>
	// PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, TreeMap<Float,
	// float[]>>();
	// <ticker, [day][technical data]>
	public static final TreeMap<String, float[][]> PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, float[][]>();

	public static final InstanceCounter DATE_COUNTER = new InstanceCounter();

	public static final ArrayList<Float> DATE_VALUES = new ArrayList<Float>();

	long time = System.currentTimeMillis();
	static {
		long time = System.currentTimeMillis();

		for (String ticker : TickerSet.TICKERS) {
			PER_TICKER_PER_DAY_TECHNICAL_DATA.put(ticker,
					new float[NUMBER_OF_DAYS][]);
		}

		restoreSettings();
		System.out.println("time: " + (System.currentTimeMillis() - time));
	}

	public TechnicalDatabase(String root) {
		int count = 0;
		 settings.rootPath = root;
			while (settings.rootPath == null || settings.rootPath.length() < 1) {
				settings.rootPath = dbRootPathChooser();
				SerializationTool.serialize(settings, settingsFileName);
			} 
	 
		File[] files = new File(settings.rootPath).listFiles();
		for (File file : files) {
			System.out.println(count++ + "     " + file.getName());
			if (file.isDirectory()) {
				loadDividends(file);
			} else {
				loadTechnicalData(file);
			}
		}
	}

	private static void restoreSettings() {
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

		if (numbers.size() == 7) {
			float date = numbers.get(0);
			if (date < TODAY - NUMBER_OF_DAYS)
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
					- (TODAY - NUMBER_OF_DAYS)] = dataForDay;
		}
	}

	private void tryToAddData(String numString, ArrayList<Float> numbers) {
		// if its a date parse it and add it
		try {
			float daynumber = TimeRecord.dayNumber(REPORT_DATE_FORMAT.parse(
					numString).getTime());
			// DATE_VALUES.add(daynumber);
			// numbers.add(daynumber);
			DATE_COUNTER.add(daynumber);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		// if (true)
		// return;
		// if its a number valueof and add it
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

		DATE_COUNTER.printlnCounts(0.1f);
		DATE_COUNTER.printlnSize();
		new StatInfo(DATE_VALUES);
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
