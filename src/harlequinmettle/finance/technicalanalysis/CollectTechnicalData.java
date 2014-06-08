package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.finance.tickerset.TickerSet;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class CollectTechnicalData {
	public static final String TECHNICAL_DATA = "TECHNICAL_DATA";
	public static final String DIVIDENDS_SUBFOLDER = "/DIVIDENDS";
	String urlbase = "http://ichart.finance.yahoo.com/table.csv?s=";
	static String dividendSuffix = "&g=v";

	public static void main(String[] args) {
		CollectTechnicalData ctd = new CollectTechnicalData();
		//daily price data
		ctd.collectTechnicalData(TECHNICAL_DATA,""); 
		//daily price data
		ctd.collectTechnicalData(TECHNICAL_DATA+DIVIDENDS_SUBFOLDER,dividendSuffix); 

	}
 
	private void collectTechnicalData(String filePath, String suffix) {
		int i = 0;
		ensureDirectory(filePath);
		for (String ticker : TickerSet.TICKERS) {
			i++;
			// if (i > 20)
			// break;
			try {
				HttpDownloadUtility.downloadFile(urlbase + ticker+suffix,
						filePath, ticker + ".csv");
				//File collectedData = new File(filePath, ticker + ".csv");
				// if (collectedData.exists()) {
				// String data = FileUtils.readFileToString(new File(
				// filePath, ticker + ".csv"));
				// System.out.println(data.substring(0, 250));
				//	}
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
