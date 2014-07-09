package harlequinmettle.finance.technicalanalysis.util;

import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.finance.ETFs;
import harlequinmettle.utils.finance.TickerSet;
import harlequinmettle.utils.finance.updatedtickerset.CurrentSymbolsDatabase;
import harlequinmettle.utils.nettools.NetPuller;
import harlequinmettle.utils.timetools.TimeRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class SupplementalFundamentalsCollector {
public static void main(String arg[]){
	CurrentSymbolsDatabase db = new CurrentSymbolsDatabase(new SerializationTool());
	
	ArrayList<String> tickers  = new ArrayList<String>(db.tickers.values());
	tickers.addAll(Arrays.asList(ETFs.fidelityFreeTradeETFS));
	tickers.removeAll(TickerSet.TICKERS);
	System.out.println("size: "+tickers.size()+"      "+tickers	);
	SupplementalFundamentalsCollector.collectAndExtractDataFromNetwork(tickers); 
}
	public static final String yahoobase = "http://finance.yahoo.com/q";
	public static final String fcstbase = "http://money.cnn.com/quote/forecast/forecast.html?symb=";

	public static String eodbase = "http://www.eoddata.com/stockquote/";
	public static String cnnbase = "http://money.cnn.com/quote/quote.html?symb=";
	public static String longString = "123456789012345678901234567890123456789012345678901234567890-12345678901234567890-12345678901234567890";

	public static void collectAndExtractDataFromNetwork(List<String> tickers) {
		String root = new ChooseFilePrompterPathSaved("application_settings", "path_to_suplemental_destination")
				.getSetting("destination folder for suplemental funadamental data");
		String fileName = TimeRecord.fileTitleInt("supplemental");
		 
		if (root != null)
			fileName = root + File.separatorChar + fileName;

		System.out.println("  "+root	);
		System.out.println("  "+fileName	);
		
		File started = new File(fileName);
		int i = 0;
		String forFile = "";
		if(started.exists()){
			 try {
				forFile = FileUtils.readFileToString(started);
			} catch (IOException e) { 
			}
			i = forFile.split(System.lineSeparator()).length;
		}
		for (;i<tickers.size();) {
			String ticker = tickers.get(i);
			forFile += collectAndExtractDataFromNetwork(ticker);
			if(i%20==0)
			try {
				FileUtils.writeStringToFile(new File(fileName), forFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("  "+ i++ +"      "+ticker	);
			}
		try {
			FileUtils.writeStringToFile(new File(fileName), forFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String collectAndExtractDataFromNetwork(String tk) {

		StringBuilder sb = new StringBuilder();
		 
		sb.append(cnnForecast(tk, true).trim());
		sb.append(" ");
		sb.append(analystEstimates(tk));
		sb.append(" ");
		sb.append(keyBasedData(yahoobase + "/ks?s=" + tk, keykeys));
		sb.append(" ");
		sb.append(limitToTen(pastPrices(tk)));
		sb.append(" ");
		sb.append(optionsCount(tk));

		return tk + "^" + sb.toString().replace("\n", "#_#_#_#").replace("\r", "") + "\n";
	}

	// #################################
	// ////////////////////past and current estimates on earnings revenue growth
	public static String analystEstimates(String stock) {
		int counting = 0;
		// Year Ago EPS //after this key substring to Revenue Est
		String httpdata = NetPuller.getHtml2(yahoobase + "/ae?s=" + stock);
		if (httpdata==null )
			return "# # # # ";
		String chop = "";
		if ( httpdata.contains("Earnings Est") && httpdata.contains("Currency in USD")) {
			chop = httpdata.substring(httpdata.indexOf("Earnings Est"), httpdata.indexOf("Currency in USD")).replaceAll("d><t", "d> <t")
					.replaceAll("d></t", "d>@</t");
		}
		String rval = "";
		chop = removeHtml((chop), true).replaceAll("_", " ");
		for (String k : estimatekeys) {
			counting++;
			if (chop.contains(k)) {
				if (counting == 6 && chop.contains("Earnings Hist"))
					chop = chop.substring(chop.indexOf("Earnings Hist"));
				String datapart = chop.substring(chop.indexOf(k) + k.length());
				if (datapart.contains("@"))
					datapart = datapart.substring(0, datapart.indexOf("@")).trim();
				for (int i = 0; i < 4 - datapart.split(" ").length; i++)
					datapart += "_#";
				rval += datapart + " ";
			} else
				rval += "# # # # ";
		}
		String backAt = (rval.replaceAll("_", " ").trim().replaceAll(" ", "_"));
		// System.out.println(backAt);

		return backAt;
	}

	// #################################
	// /////////////forecast data from cnn price analysts status
	public static String cnnForecast(String stock, boolean firstTry) {

		String httpdata = NetPuller.getHtml2(fcstbase + stock);
		if (httpdata == null)
			System.out.println("null forecast");
		if (httpdata==null || httpdata.contains(">There is no") || httpdata.contains("was not found")) {
			// System.out.println("NO CNN DATA FOR: " + stock);
			return "#_#";
		}
		String chop = "#_#";

		if (httpdata.contains(">Stock Price Forecast")) {

			chop = httpdata.substring(httpdata.indexOf(">Stock Price Forecast"));
			if (chop.contains("Earnings and Sales Forecasts") && chop.contains("The"))

				chop = chop.substring(chop.indexOf("The") + 3);
			String analysts = "#";
			String forecast = "#";
			try {
				if (chop.contains("analyst"))
					analysts = chop.substring(0, chop.indexOf("analyst")).trim();

				if (chop.contains("represents a"))
					forecast = chop.substring(chop.indexOf("represents a") + 12, chop.indexOf("%")).replaceAll("_", "");
				if (forecast.contains(">"))
					forecast = forecast.substring(forecast.indexOf(">") + 1).trim();
				if (forecast.length() > 23)
					forecast = "#";
			} catch (Exception e) {
			}
			chop = analysts + "_" + forecast;
		}
		if (firstTry && chop.equals("#_#")) {
			chop = cnnForecast(stock, false);
		}
		return (chop);

	}

	// ///////////////////////////////////////////////////////////////////
	static String limitToTen(String pastPrices) {
		// TODO Auto-generated method stub
		if (pastPrices.contains("Split"))
			System.out.println("**S-->" + pastPrices);
		if (pastPrices.equals("HISTORIC"))
			return "#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#@#_#_#_#_#_#_#_#_#"
					+ " #" + " #";
		double split = 1;
		double dividends = 0;
		String[] days = pastPrices.split("@");
		StringBuilder reconstruct = new StringBuilder();
		int counter = 0;
		for (int i = 0; i < days.length; i++) {
			String[] data = days[i].split("_");
			if (data.length < 9) {
				System.out.println(pastPrices);
				String[] determine = days[i].split(":");
				try {
					if (determine.length < 2)
						dividends += doDouble(days[i].split("_")[3]);
					else if (counter < 5) {
						split = doDouble(determine[0].split("_")[3])
								/ doDouble(determine[1].toLowerCase().replace("stock_split", "").replaceAll("_", ""));
						System.out.println("SPLIT: " + split + "\nFrom: " + pastPrices);
					}
				} catch (Exception e) {
					System.out.println(days[i]);
					e.printStackTrace();
				}
			} else {
				reconstruct.append(days[i] + "@");
				counter++;
				if (counter == 10)
					break;
			}
		}
		for (int j = counter - 1; j < 9; j++)
			reconstruct.append("#_#_#_#_#_#_#_#_#@");
		return reconstruct.append(" " + dividends + " " + split).deleteCharAt(reconstruct.lastIndexOf("@")).toString();
	}

	// ///////////////////////////////////////////////////////////////////
	public static String optionsCount(String stock) {
		String httpdata = NetPuller.getHtml2(yahoobase + "/op?s=" + stock);
		if (httpdata==null || httpdata.contains(">There is no") || httpdata.contains("Check your spelling") || httpdata.indexOf("View By Expiration") < 0)
			return "0";
		try {
			httpdata = httpdata.substring(httpdata.indexOf("View By Expiration"));
			httpdata = httpdata.substring(0, httpdata.indexOf("table"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "" + (httpdata.toLowerCase().split("a href").length - 1);
	}

	/**
	 * Converts String data into double data removing commas, n/a's, and
	 * replacing M's, and B's with six and nine zeros respectively.
	 * 
	 * @param datain
	 *            raw data from stock data web site.
	 */
	static double[] dataLightSubprocessor(String datain) {
		// possibly add # replacement value
		String[] dataInSplit = datain.replaceAll("@", " ").replaceAll("_", " ").split(" ");
		//
		if (dataInSplit.length != 175)
			System.out.println(dataInSplit.length);
		double[] processed = new double[dataInSplit.length];
		for (int i = 0; i < dataInSplit.length; i++) {
			double factor = 1;
			String data = dataInSplit[i].replace("$", "");// remove unrecognized
															// symbols
			data = data.replace("%", "");
			data = data.replaceAll("--", "");
			data = data.replaceAll("\\(", "-");// NEG IN ACCOUNTING IN
												// PARENTHESIS
			data = data.replaceAll("\\)", "");
			data = data.replaceAll("NM", "");
			data = data.replaceAll("Dividend", "");

			// replaced individual if stmts with for loop HOPE STILL TO WORK
			// convert month text into number
			for (int j = 0; j < months.length; j++) {
				if (data.equals(months[j])) {
					data = "" + j;
				}
			}
			if (data.equals("#"))
				processed[i] = -0.0000001;// number to replace blanks #

			if (data.contains("B")) {// for when billions are abreviated B
				factor = 1000000000;
				data = data.replaceAll("B", "");
			}
			if (data.contains("M")) {
				factor = 1000000;
				data = data.replaceAll("M", "");
			}
			if (data.contains("K")) {
				factor = 1000;
				data = data.replaceAll("K", "");
			}
			if (data != null) {
				String dat = data.replaceAll(",", "");// remove commas from
														// zeros1,000
				double dub = doDouble(dat);
				processed[i] = dub * factor;
			} else {
				// System.out.println("** unknown string default -> -1e-7 "+
				// data);
				processed[i] = -0.0000001;
			}
		}
		return processed;
	}

	/*
	 * returns the double value of a string and returns -1E-7 if the string
	 * could not be parsed as a double.
	 * 
	 * @param value the string that gets converted into a double.
	 */
	private static double doDouble(String value) {
		try {
			double val = Double.parseDouble(value);
			if (val == val)// only return value if its not NaN , NaN==NaN is
							// false
				return val;
			else
				return -0.0000001;
		} catch (Exception e) {
			// System.out.println(" TEXT TO NUMBER ERR "+ value +" to -1e-7 ");
			return -0.0000001;
		}
	}

	// ///////////replace @,^,shorten to end of table,
	public static String reformat(String input) {
		String output = input.replaceAll("@", "_").replaceAll("^", "_").replaceAll("\\*", "_");
		if (input.contains("</table>"))
			output = output.substring(0, output.indexOf("</table>"));
		output = output.replaceAll("d><t", "d> <t").replaceAll("h><t", "h> <t").replaceAll("d></t", "d>@</t").replaceAll("&nbsp;", "-")
				.replaceAll("--", "");
		return output;
	}

	// /////////////////#######################################
	public static String pastPrices(String stock) {

		String httpdata = NetPuller.getHtml2(yahoobase + "/hp?s=" + stock);
 
		if (httpdata==null || !httpdata.contains("Adj Close"))
			return "HISTORIC";
		httpdata = removeHtml(reformat(httpdata.substring(httpdata.indexOf("Adj Close"))), false);
		httpdata = httpdata.substring(0, httpdata.lastIndexOf("@"));
		if (httpdata.indexOf("@") < 2)
			httpdata = httpdata.substring(httpdata.indexOf("@") + 1);
		if (httpdata.lastIndexOf("@") > 1)
			httpdata = (httpdata.substring(0, httpdata.lastIndexOf("@")).replaceAll(",", ""));
		return httpdata;
	}

	/**
	 * This method colects into a string data from tables about stock in the
	 * form nnn_mmm_nnn_mmm@nnn_mmm_nnn_mmm@etc
	 * 
	 * @param addy
	 *            The internet address to get html from.
	 * @param keys
	 *            The text keys expected to be found in the html data
	 */
	static protected String keyBasedData(String addy, String[] keys) {
		for (int i = 0; i < 10; i++) {// try 10 times to get html or else return
			if (i > 0)
				System.out.println("\nConnection Failure. Trying again: " + i);
			String httpdata = NetPuller.getHtml2(addy);
			String yhdata = "";
			String str = httpdata;
			if (httpdata==null || str.contains("was not found"))
				return "#_#_#";
			if (str.contains("Recommendation Trends")) {
				str = (str.substring(str.indexOf("Recommendation Trends")));
				str = str.replaceAll("d><t", "d> <t");
			}
			for (String key : keys) {
				if (str.contains(">" + key)) {
					String strx = str.substring(str.indexOf(">" + key) + 1);
					if (!strx.contains("</tr>"))
						return "#_#_#";
					strx = strx.substring(0, strx.indexOf("</tr>"));
					if (key.equals("Sector"))
						strx = strx.replaceAll(" ", "|");
					strx = removeHtml(strx, true).replaceAll("@", " ");// just
																		// in
																		// case
					if (strx.length() == 0)
						strx = "#";// placeholder if data does not exist
					yhdata += strx + "_";
				} else {
					yhdata += "#_";

				}
			}
			// return spacify(yhdata.replaceAll("--", "#"));
			return (yhdata.replaceAll("--", "#").replaceAll("_", " ").trim().replaceAll(" ", "_"));
		}
		return "#_#";
	}

	// ///////////////////////////////////////////////////////////

	/**
	 * This method removes html tags such as <a href = ...> and
	 * <table>
	 * 
	 * @param withhtml
	 *            The string text with tags included
	 */
	public static String removeHtml(String withhtml, boolean colon) {
		String stripped = withhtml;//
		String save = "";
		if (colon)
			if (stripped.indexOf(":") > 0) {// jump ahead just past the colons
				stripped = withhtml.substring(withhtml.indexOf(":") + 1);
			}
		// skip all sections of html code between the <htmlcode>
		while (stripped.indexOf("<") >= 0 && stripped.indexOf(">") > 0) {
			stripped = stripped.substring(stripped.indexOf(">") + 1);
			if (stripped.indexOf("<sup") < 2 && stripped.indexOf("<sup") > -1)
				stripped = stripped.substring(stripped.indexOf("</sup>") + 6);
			if (stripped.indexOf("<") > 0)// keep any text inbetween
											// <code>keeptext<code>
				save += stripped.substring(0, stripped.indexOf("<"));
		}
		// save = save.replaceAll("-","_");//VITAL TO PRESERVE NEGATIVES
		save = save.replaceAll(" ", "_");
		save = save.replaceAll("___", "_");
		save = save.replaceAll("__", "_");
		save = save.replaceAll("_-_", "_");
		return save;

	}

	public static String[] keykeys = { "Market Cap", // 0
			"Enterprise Value", //
			"Trailing P/E",// 2
			"Forward P/E",// 3
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
			"50-Day Moving Average",//
			"200-Day Moving Average",// 32
			"Avg Vol (3 month)",//
			"Avg Vol (10 day)",//
			"Shares Outstanding",// 35
			"Float",//
			"% Held by Insiders",//
			"% Held by Institutions",// 38
			"Shares Short (as of",//
			"Short Ratio (as of",// 40
			"Short % of Float (as of",//
			"Shares Short (prior month)",//
			"Payout Ratio" // 43
	};// 44 values

	public static String[] estimatekeys = { "Avg. Estimate",// these 5 use
			"No. of Analysts",//
			"Low Estimate",//
			"High Estimate", // /
			"Year Ago EPS", // after
							// this
							// key
							// substring
							// to
							// Revenue
							// Est

			"EPS Est",// these 4 use
			"EPS Actual", //
			"Difference", //
			"Surprise %", };//

	public static String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

}
