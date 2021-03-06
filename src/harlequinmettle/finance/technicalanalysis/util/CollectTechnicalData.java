package harlequinmettle.finance.technicalanalysis.util;

import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.finance.updatedtickerset.CurrentSymbolsDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

public class CollectTechnicalData {

	public static final String TECHNICAL_DATA = "TECHNICAL_DATA";
	public static final String DIVIDENDS_SUBFOLDER = "/DIVIDENDS";
	String urlbase = "http://ichart.finance.yahoo.com/table.csv?s=";
	static String dividendSuffix = "&g=v";
	String currentTicker = "";
	int progress = 0;
	boolean running = false;

	public static void main(String[] args) {
		 CurrentSymbolsDatabase TICKERSDB = new CurrentSymbolsDatabase(new SerializationTool());
		  ArrayList<String> TICKERS = new ArrayList<String>(TICKERSDB.tickers.values());
		CollectTechnicalData ctd = new CollectTechnicalData();
		// daily price data
		ctd.collectTechnicalData( TICKERS, TECHNICAL_DATA, "");
		// daily price data
		ctd.collectTechnicalData(TICKERS, TECHNICAL_DATA + DIVIDENDS_SUBFOLDER, dividendSuffix);
		ctd.validateDataDownload(TICKERS, TECHNICAL_DATA);
		ctd.validateDataDownload(TICKERS, TECHNICAL_DATA + DIVIDENDS_SUBFOLDER);
	}

	public void validateDataDownload(ArrayList<String> tickers, String filePath) {
		boolean success = false;
		for (String ticker : tickers) {
			File collectedData = new File(filePath, ticker + ".csv");
			if (collectedData.exists()) {

				try {
					String data = FileUtils.readFileToString(new File(filePath, ticker + ".csv"));

					if ((data.startsWith("Date,Open,High,Low,Close,Volume,Adj Close") && (data.split(",").length > 13))//
							//
							|| (data.startsWith("Date,Dividends") && (data.split(",").length > 3)))
						success = true;
					else {
						success = false;
						new File(filePath, ticker + ".csv").delete();
					}
				} catch (IOException e) {
				}
			} else {
				success = false;
			}
			if (!success)
				System.out.println("data fail" + ticker);
		}

	}

	public void collectTechnicalData(final ArrayList<String> tickers) {
		if (running)
			return;
		running = true;
		new Thread() {
			@Override
			public void run() {
				collectTechnicalData(tickers, TECHNICAL_DATA, "");
				collectTechnicalData(tickers, TECHNICAL_DATA + DIVIDENDS_SUBFOLDER, dividendSuffix);
				running = false;
				progressTextArea.setText("done");
			}
		}.start();
	}

	private void collectTechnicalData(ArrayList<String> tickers, String filePath, String suffix) {
		int i = 0;
		ensureDirectory(filePath);
		List<String> skip = new ArrayList<String>();

		try {
			String nodatafor = FileUtils.readFileToString(new File(CollectTechnicalData.TECHNICAL_DATA + File.separator + "NODATA/nodata"
					+ ".txt"));
			skip.addAll(Arrays.asList(nodatafor.split("\n")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (String ticker : (tickers)) {

			Thread.yield();
			i++;
			if (i % 20 == 0)
				progressTextArea.setText("" + progress + "     " + ticker);
			try {
				String url = urlbase + ticker + suffix;
				if (skip.contains(url))
					continue;

				HttpDownloadUtility.downloadFile(url, filePath, ticker + ".csv");
				currentTicker = ticker;
				progress++;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void ensureDirectory(String directoryPath) {
		File dataDirect = new File(directoryPath);
		if (!dataDirect.exists()) {
			dataDirect.mkdirs();
		}
	}

	JTextField progressTextArea;

	public void setProgressTextArea(JTextField progress) {
		this.progressTextArea = progress;

	}

}
