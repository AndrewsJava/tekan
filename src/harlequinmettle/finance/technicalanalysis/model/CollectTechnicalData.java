package harlequinmettle.finance.technicalanalysis.model;

import harlequinmettle.utils.finance.TickerSetWithETFs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CollectTechnicalData {
	public static final String TECHNICAL_DATA = "TECHNICAL_DATA";
	public static final String DIVIDENDS_SUBFOLDER = "/DIVIDENDS";
	String urlbase = "http://ichart.finance.yahoo.com/table.csv?s=";
	static String dividendSuffix = "&g=v";

	public static void main(String[] args) {
		CollectTechnicalData ctd = new CollectTechnicalData();
		// daily price data
		ctd.collectTechnicalData(TickerSetWithETFs.TICKERS, TECHNICAL_DATA, "");
		// daily price data
		ctd.collectTechnicalData(TickerSetWithETFs.TICKERS, TECHNICAL_DATA
				+ DIVIDENDS_SUBFOLDER, dividendSuffix);
		ctd.validateDataDownload(TickerSetWithETFs.TICKERS, TECHNICAL_DATA);
		ctd.validateDataDownload(TickerSetWithETFs.TICKERS, TECHNICAL_DATA
				+ DIVIDENDS_SUBFOLDER);
	}

	private void validateDataDownload(ArrayList<String> tickers, String filePath) {
		boolean success = false;
		for (String ticker : tickers) {
			File collectedData = new File(filePath, ticker + ".csv");
			if (collectedData.exists()) {

				try {
					String data = FileUtils.readFileToString(new File(filePath,
							ticker + ".csv"));

					if ((data.startsWith("Date,Open,High,Low,Close,Volume,Adj Close") 
							&& (data.split(",").length > 13))//
							//
							|| (data.startsWith("Date,Dividends") && (data
									.split(",").length > 3)))
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
				System.out.println("data fail"+ ticker);
		}

	}

	private void collectTechnicalData(ArrayList<String> tickers,
			String filePath, String suffix) {
		int i = 0;
		ensureDirectory(filePath);
		 List<String> skip  = new ArrayList<String>();
		 
		try {
			String nodatafor = FileUtils.readFileToString(new File( 
						CollectTechnicalData.TECHNICAL_DATA + File.separator
								+ "NODATA/nodata" + ".txt")); 
		skip.addAll(Arrays.asList(nodatafor.split("\n"))); 
		} catch (IOException e1) { 
			e1.printStackTrace();
		}
		for (String ticker :  (tickers)) {
			i++;
			// if (i > 20)
			// break;
			try {
				String url = urlbase + ticker + suffix;
				if(skip.contains(url))continue;
				
				HttpDownloadUtility.downloadFile(url,
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
