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
	public static final TreeMap<String, TreeMap<Float, float[]>> PER_TICKER_PER_DAY_TECHNICAL_DATA = new TreeMap<String, TreeMap<Float, float[]>>();

	public static final InstanceCounter DATE_COUNTER = new InstanceCounter();

	public static final ArrayList<Float> DATE_VALUES = new ArrayList<Float>();

	long time = System.currentTimeMillis();
	static {
		long time = System.currentTimeMillis();

		for (String ticker : TickerSet.TICKERS) {
			PER_TICKER_PER_DAY_TECHNICAL_DATA.put(ticker,
					new TreeMap<Float, float[]>());
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
			System.out.println(count++ + "     " + file.getName());
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

		if (false && numbers.size() == 7) {
			float date = numbers.get(0);
			if (date < 15000)
				return;
			float open = numbers.get(1);
			float high = numbers.get(2);
			float low = numbers.get(3);
			float close = numbers.get(4);
			float volume = numbers.get(5);
			float adjclose = numbers.get(6);

			float[] dataForDay = { date, open, high, low, close, volume,
					adjclose };
			PER_TICKER_PER_DAY_TECHNICAL_DATA.get(ticker).put(date, dataForDay);
		}
	}

	private void tryToAddData(String numString, ArrayList<Float> numbers) {
		// if its a date parse it and add it
		try {
			float daynumber = TimeRecord.dayNumber(REPORT_DATE_FORMAT.parse(
					numString).getTime());
			DATE_VALUES.add(daynumber);
		//	numbers.add(daynumber);
		//	DATE_COUNTER.add(daynumber);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		if (true)
			return;
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

		DATE_COUNTER.printlnCounts();
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
