package harlequinmettle.finance.technicalanalysis.datatest;

import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabase;
import harlequinmettle.utils.debugtools.InstanceCounter;
import harlequinmettle.utils.finance.TickerSetWithETFs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

public class FindSparseDataTickers {

	public static void main(String[] args) {
		compareTickerListToFiles();
		// SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		// TechnicalDatabase techDatabase = new TechnicalDatabase();
		//
		// TreeMap<Double, String> sparseData = finsSparseTickers(techDatabase);
	}

	private static void compareTickerListToFiles() {
		ArrayList<String> fullTickerList = new ArrayList<String>();
		fullTickerList.addAll(TickerSetWithETFs.TICKERS);
		ArrayList<String> limitedData = collectAllTickersWithData(4800);
		// fullTickerList.removeAll(collectAllTickersWithData(0));
		// 300 in TickerSet.Tickers with no file associated
		// System.out.println(fullTickerList.size() + "   ->" + fullTickerList);
		// int i = 0;
		// for (String s : fullTickerList) {
		// System.out.println("\"" + s + "\",//" + i++);
		//
		// }
	}

	private static ArrayList<String> collectAllTickersWithData(
			int overDaysOfData) {
		ArrayList<String> fileTickerList = new ArrayList<String>();
		InstanceCounter ic = new InstanceCounter();
		String history = "";
		File datalocation = new File(
				"/home/andrew/Desktop/GAEm/TechnicalAnalysis/TECHNICAL_DATA");
		File[] files = datalocation.listFiles();
		int i = 0;
		for (File s : files) {

			if (!s.isFile())
				continue;
			String ticker = s.getName().replace(".csv", "");
			String record = tryToReadFileToString(s);
			// int recent = record.split(("2014-\\d{2}-\\d{2}")).length;
			int total = record.split(System.lineSeparator()).length;
			if (total > 1)
				history += ticker + " " + total + "\n";
			// ic.add(total);
			// if (total >100)
			// System.out.println(i++ + "    " + s.getName() + "      "
			// + total);
			// if (total > overDaysOfData)
			// fileTickerList.add(s.getName().replace(".csv", ""));
			// if (total > overDaysOfData)
			// System.out.println(i++ + "    " + s.getName() + "      "
			// + total);
		}
		File writeTo = new File(datalocation.getAbsolutePath()
				+ File.separatorChar
				+ "tickerhistorysize/tickerhistorysize.txt");
	 
		try {
			FileUtils.writeStringToFile(writeTo, history);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ic.printlnCounts(1);
		return fileTickerList;
	}

	private static String tryToReadFileToString(File s) {
		try {
			return FileUtils.readFileToString(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "unable to read file";
	}

	private static TreeMap<Double, String> finsSparseTickers(
			TechnicalDatabase techDatabase) {
		// <actualinvalidcount+ smalluniqeamount, ticker>
		TreeMap<Double, String> lowPriorityTickers = new TreeMap<Double, String>();
		// <ticker, [day][technical data]>
		TreeMap<String, float[][]> DB = techDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA;

		int uc = 100;
		for (Entry<String, float[][]> ent : DB.entrySet()) {
			String ticker = ent.getKey();
			int i = 0;
			double uniqueAmount = 1e-7;
			float[][] individualTrackRecord = ent.getValue();
			int invalidCount = 0;
			int validCount = 0;
			for (float[] dayData : individualTrackRecord) {

				if (dayData != null) {
					validCount++;
				} else {
					invalidCount++;
				}

				i++;
			}
			lowPriorityTickers.put(invalidCount + uniqueAmount * uc, ticker);

			uc++;
			System.out.println(ticker + "     -----  " + invalidCount
					+ "  empty , valid:  " + validCount + "   ");
		}

		return lowPriorityTickers;

	}

}
