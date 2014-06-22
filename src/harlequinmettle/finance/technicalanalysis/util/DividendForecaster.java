package harlequinmettle.finance.technicalanalysis.util;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.utils.TimeRecord;

import java.util.Map.Entry;
import java.util.TreeMap;

public class DividendForecaster {

	public static void main(String[] args) {
		DividendForecaster df = new DividendForecaster();
		DividendDatabase ddb = new DividendDatabase();
		CurrentFundamentalsSQLiteDatabase fdb = new CurrentFundamentalsSQLiteDatabase();
		df.scannForUpcommingDividends();
	}

	public TreeMap<String, String> scannForUpcommingDividends() {
		TreeMap<String, String> results = new TreeMap<String, String>();
		float today = TimeRecord.dayNumber(System.currentTimeMillis());
		int count = 0;
		for (Entry<String, TreeMap<Float, Float>> ent : DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP
				.entrySet()) {
			String ticker = ent.getKey();
			// System.out.println(ticker );
			TreeMap<Float, Float> dividends = ent.getValue();
			if (dividends.size() < 2)
				continue;
			float lastDate = 0;
			float lastDiv = 0;
			boolean first = true;
			for (Entry<Float, Float> dateDiv : dividends.entrySet()) {
				float currentDate = dateDiv.getKey();
				float dividend = dateDiv.getValue();
				if (first || dividend == 0) {
					first = false;
					lastDate = currentDate;
					continue;
				} else if (isDividendExpected(currentDate, lastDate, today)) {
					if (!CurrentFundamentalsSQLiteDatabase.CURRENT_TICKER_TO_LABEL_DATA_MAPING
							.containsKey(ticker))
						continue;
					TreeMap<String, Float> tickerData = CurrentFundamentalsSQLiteDatabase.CURRENT_TICKER_TO_LABEL_DATA_MAPING
							.get(ticker);

					String divoverview = "unknown";
					if (tickerData.containsKey("50-Day Moving Average")) {
						float fiftyDayAvg = tickerData
								.get("50-Day Moving Average");
						divoverview = (((int) (1000 * dividend / fiftyDayAvg) / 10f)
								+ "%" + ticker + "     " + lastDiv + "     " + dividend);
					} else {
						divoverview += "%" + ticker + "     " + lastDiv
								+ "     " + dividend;

					}
					results.put(divoverview, ticker);
					System.out.println(ticker);
					System.out.println(divoverview);
					System.out.println("last: " + lastDate + " " + lastDiv);
					System.out.println("now: " + ent);

					float lastInterval = currentDate - lastDate;
					float expected = currentDate + lastInterval;
					System.out
							.println("                                 today: "
									+ today);
					System.out.println("                           expected: "
							+ (expected));
				}

				lastDiv = dividend;
				lastDate = currentDate;
			}
		}
		return results;
	}

	private float getPriceFromTechnicalDatabase(String ticker, float currentDate) {
		float approxPrice = 5;
		// float[][] techData =
		// TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA
		// .get(ticker);
		// int date = (int) (currentDate - TechnicalDatabase.NUM_DAYS_START);
		// approxPrice = techData[techData.length-1][6];
		return approxPrice;
	}

	private static boolean isDividendExpected(float currentDate,
			float lastDate, float today) {
		float lastInterval = currentDate - lastDate;
		float expected = currentDate + lastInterval;

		if (expected < today + 15 && expected > today + 2) {
			return true;
		}
		return false;
	}

}
