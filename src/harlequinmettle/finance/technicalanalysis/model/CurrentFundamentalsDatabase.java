package harlequinmettle.finance.technicalanalysis.model;

import harlequinmettle.finance.database.DataUtil;
import harlequinmettle.finance.tickerset.TickerSet;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.finance.TickerSetWithETFsOptimized;
import harlequinmettle.utils.guitools.FilterPanel;

import java.io.File;
import java.util.Arrays;
import java.util.TreeMap;

public class CurrentFundamentalsDatabase {
	int nancount = 0;
	String pathToObj = ".path_to_small_database";
	String pathtodata = "path to small database";

	public CurrentFundamentalsDatabase() { 
		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
				pathToObj );
		root = settingssaver.getSetting(pathtodata);
		// root = ChooseFilePrompter.directoryPathChooser();
		loadCurrentDataFromFiles();
		System.out.println("total:  "
				+ (TickerSet.TICKERS.size() * labels.length) + "   nan:  "
				+ nancount);
	}

	public TreeMap<String, String> getFilterResults(FilterPanel[] searchFilters) {
		TreeMap<String, String> results = new TreeMap<String, String>();
		int i = 0;
		for (String ticker : TickerSet.TICKERS) {
			boolean qualifies = true;
			String reasonForQualification = "";
			for (FilterPanel filter : searchFilters) {
				if (!filter.shouldFilterBeApplied())
					continue;
				int id = filter.getId();
				float low = filter.getLow();
				float high = filter.getHigh();
				float dataPoint = data[i][id];
				if (dataPoint != dataPoint || dataPoint > high
						|| dataPoint < low)
					qualifies = false;
				else
					reasonForQualification += "  " + labels[id] + "  :  ["
							+ dataPoint + "] ";
			}

			if (qualifies)
				results.put(ticker, reasonForQualification);
			i++;
		}
		return results;
	}

	public void loadCurrentDataFromFiles() {

		// for each time stage file construct array 85 fundamental data
		// points for each symbol

		for (float[] f : data) {
			Arrays.fill(f, Float.NaN);
		}

		String[] smallDBFiles = new File(root + File.separator + "q").list();
		String[] smallDBFiles2 = new File(root + File.separator + "y").list();
		Arrays.sort(smallDBFiles);
		Arrays.sort(smallDBFiles2);
		// get some data from 1year ago
		boolean isOneYearAgo = true;	
		for (int x = smallDBFiles.length - 54; x < smallDBFiles.length; x++) {
			// skip ahead 44 weeks worth
			if (x == smallDBFiles.length - 48){
				x += 44;
			  isOneYearAgo = false;	
			}
			TreeMap<String, String> textData = new TreeMap<String, String>();

			DataUtil.loadStringData(root + File.separator + "q"
					+ File.separator + smallDBFiles[x], textData);
			DataUtil.loadStringData(root + File.separator + "y"
					+ File.separator + smallDBFiles2[x], textData);

			int nullcount = 0;
			// ASSUMES A 1 TO 1 EXISTENCE OF NAS AND NY FILES - TRUE SO FAR
			for (int j = 0; j < TickerSet.TICKERS.size(); j++) {
				String ticker = TickerSet.TICKERS.get(j);
				String textdata = textData.get(ticker);

				final int[] sizes = { 2, 36, 44, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
						1, 1, 1 };

				if (textdata != null) {

					float[] rawData = DataUtil.validSmallDataSet(textdata,
							sizes);
					if (rawData.length != 175)
						continue;
					fillFundamentalData(data[j], rawData,isOneYearAgo);
					nancount += badDataCount(data[j]);
				} else {
					System.out.println(++nullcount+ "   NULL TEXT");
				}

			}
		}
	}

	private int badDataCount(float[] fs) {
		int badcount = 0;
		for (float f : fs) {
			if (f != f)
				badcount++;
		}
		return badcount;
	}

	private void fillFundamentalData(float[] values, float[] rawData,
			boolean isOlderThan1Year) {
		for (int k = 0; k < 82; k++) {
			// k==1 cnn analyst 1 year forecast
			if (k == 1 && isOlderThan1Year)
				values[k] = betterOf(values[k], rawData[k]);
			else if (k != 1)
				values[k] = betterOf(values[k], rawData[k]);
		}
		values[82] = betterOf(values[82], rawData[172]);
		values[83] = betterOf(values[83], rawData[173]);
		values[84] = betterOf(values[84], rawData[174]);
	}

	private float betterOf(float originally, float newdata) {
		// if first is no good AND second either NaN
		if ((originally == -1e-7 || originally != originally)
				&& (newdata == -1e-7 || newdata != newdata))
			return Float.NaN;
		// if second is not either -1e-7 or NaN use it
		if (!(newdata == -1e-7 || newdata != newdata))
			return newdata;
		else
			return originally;

	}

	public static String[] labels = { "CNN analysts",// cnn - 0
			"1-yr forcast",// cnn - 1
			// /////////////////////////////
			"Avg. Estimate Current Qt",// these 5 use
			"Avg. Estimate Next Qt",// these 5 use
			"Avg. Estimate Current Yr",// these 5 use
			"Avg. Estimate Next Yr",// these 5 use
			"No. of Analysts Current Qt",//
			"No. of Analysts Next Qt",//
			"No. of Analysts Current Yr",//
			"No. of Analysts Next Yr",//
			"Low Estimate Current Qt",//
			"Low Estimate Next Qt",//
			"Low Estimate Current Yr",//
			"Low Estimate Next Yr",//
			"High Estimate Current Qt", // /
			"High Estimate Next Qt", // /
			"High Estimate Current Yr", // /
			"High Estimate Next Yr", // /
			"Year Ago EPS Next Yr", // after
			"Year Ago EPS Current Qt", // after
			"Year Ago EPS Current Yr", // after
			"Year Ago EPS Next Yr", // after
			// this
			// key
			// substring
			// to
			// Revenue
			// Est

			"EPS Est 12 Mo Ago",// these 4 use
			"EPS Est 9 Mo Ago",// these 4 use
			"EPS Est 6 Mo Ago",// these 4 use
			"EPS Est 3 Mo Ago",// these 4 use
			"EPS Actual 12 Mo Ago", //
			"EPS Actual 9 Mo Ago", //
			"EPS Actual 6 Mo Ago", //
			"EPS Actual 3 Mo Ago", //
			"Difference 12 Mo Ago", //
			"Difference 9 Mo Ago", //
			"Difference 6 Mo Ago", //
			"Difference 3 Mo Ago", //
			"Surprise % 12 Mo Ago",//
			"Surprise % 9 Mo Ago",//
			"Surprise % 6 Mo Ago", // /
			"Surprise % 3 Mo Ago",// ///////-----37

			// ////////////////////////////
			"Market Cap", // 0
			"Enterprise Value", //
			"Trailing P/E",// 2 ///--------------40
			"Forward P/E",// 3 //////-----------41
			"PEG Ratio", // 4
			"Price/Sales", //
			"Price/Book",//
			"Enterprise Value/Revenue",// 7
			"Enterprise Value/EBITDA ",//
			"Profit Margin",//
			"Operating Margin",//
			"Return on Assets", //
			"Return on Equity",//
			"Revenue", // 13
			"Revenue Per Share",//
			"Qtrly Revenue Growth",//
			"Gross Profit",// 16
			"EBITDA",//
			"Net Income Avl to Common", //
			"Diluted EPS",// 19
			"Qtrly Earnings Growth",//
			"Total Cash",//
			"Total Cash Per Share",// 22
			"Total Debt",//
			"Total Debt/Equity",//
			"Current Ratio", // 25
			"Book Value Per Share",//
			"Operating Cash Flow",// 27
			"Levered Free Cash Flow", //
			"Beta",//
			"52-Week Change",// 30
			"50-Day Moving Average",// ///////-----------69
			"200-Day Moving Average",// 32 /-----------70
			"Avg Vol (3 month)",// ---------------------71
			"Avg Vol (10 day)",// ---------------------72
			"Shares Outstanding",// 35
			"Float",//
			"% Held by Insiders",//
			"% Held by Institutions",// 38
			"Shares Short (as of",//
			"Short Ratio (as of",// 40
			"Short % of Float (as of",//
			"Shares Short (prior month)",//
			"Payout Ratio", // 43
			// ///////////////////////////////////
			"Dividends",//
			"Split",//
			"Options",//
	// ////////////////////////////
	};

	public static String[] forDisplaying = { //
			"CNN analysts",// cnn - 0
			"1-yr forcast",// cnn - 1


			// ////////////////////////////
			"Market Cap", // 0 
			"Trailing P/E",//  
			"Forward P/E",//  
			"PEG Ratio", //  
			"Price/Sales", //
			"Price/Book",// 
			"Profit Margin",//
			"Operating Margin",//
			"Return on Assets", //
			"Return on Equity",//
			"Revenue", //  
			"Revenue Per Share",//
			"Qtrly Revenue Growth",//
			"Gross Profit",//  
			"Net Income Avl to Common", //
			"Diluted EPS",//  
			"Qtrly Earnings Growth",//
			"Total Cash",//
			"Total Cash Per Share",//  
			"Total Debt",//
			"Total Debt/Equity",//
			"Current Ratio", //  
			"Book Value Per Share",//
			"Operating Cash Flow",//  
			"Levered Free Cash Flow", //
			"Beta",//
			"52-Week Change",//  
			"50-Day Moving Average",// ///////------ 
			"200-Day Moving Average",// 
			"Avg Vol (3 month)",// --------------------- 
			"Avg Vol (10 day)",// --------------------- 
			"Shares Outstanding",//  
			"Float",//
			"% Held by Insiders",//
			"% Held by Institutions",//  
			"Payout Ratio", //  
			// ///////////////////////////////////
			"Dividends",//
			"Split",//
			"Options",//			// /////////////////////////////
			"Avg. Estimate Current Qt",// these 5 use
			"Avg. Estimate Next Qt",// these 5 use
			"Avg. Estimate Current Yr",// these 5 use
			"Avg. Estimate Next Yr",// these 5 use
		  
			"EPS Est 3 Mo Ago",// these 4 use 
			"EPS Actual 3 Mo Ago", // 
			"Difference 3 Mo Ago", // 
			"Surprise % 9 Mo Ago",//
			"Surprise % 6 Mo Ago", // /
			"Surprise % 3 Mo Ago",// ///////---- 
	// ////////////////////////////
	};

	public String root = "";

	public static float[][] data = new float[TickerSetWithETFsOptimized.TICKERS.size()][labels.length];

}
