package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.finance.tickerset.TickerSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

public class CollectTechnicalData {
	public static final String TECHNICAL_DATA = "TECHNICAL_DATA";
	public static final String DIVIDENDS_SUBFOLDER = "/DIVIDENDS";
	String urlbase = "http://ichart.finance.yahoo.com/table.csv?s=";
	static String dividendSuffix = "&g=v";

	public static void main(String[] args) {
		CollectTechnicalData ctd = new CollectTechnicalData();
		// daily price data
		ctd.collectTechnicalData(TickerSet.TICKERS,TECHNICAL_DATA, "");
		// daily price data
		ctd.collectTechnicalData(TickerSet.TICKERS,TECHNICAL_DATA + DIVIDENDS_SUBFOLDER,
				dividendSuffix);
		ctd.validateDataDownload(TickerSet.TICKERS,TECHNICAL_DATA);
		ctd.validateDataDownload(TickerSet.TICKERS,TECHNICAL_DATA + DIVIDENDS_SUBFOLDER);
	}

	private void validateDataDownload(ArrayList<String> tickers, String filePath) {
		boolean success = false;
		for (String ticker : tickers) {
			File collectedData = new File(filePath, ticker + ".csv");
			if (collectedData.exists()) {

				try {
					String data = FileUtils.readFileToString(new File(filePath,
							ticker + ".csv"));

					if (data.startsWith("Date,Open,High,Low,Close,Volume,Adj Close")
							&& (data.split(",").length > 13))
						success = true;
					else
						success = false;
				} catch (IOException e) {
				}
			} else {
				success = false;
			}
			if(!success)System.out.println("data fail");
		}
		
	}

	private void collectTechnicalData(ArrayList<String> tickers, String filePath, String suffix) {
		int i = 0;
		ensureDirectory(filePath);
		for (String ticker : tickers) {
			i++;
			// if (i > 20)
			// break;
			try {
				HttpDownloadUtility.downloadFile(urlbase + ticker + suffix,
						filePath, ticker + ".csv");

			} catch (IOException e) {
				// TODO Auto-generated catch block
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

}
