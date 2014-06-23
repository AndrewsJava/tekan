package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TickerTechModelUtil extends TickerTechModelVars {

	void setDailyTradeData(float xpt, float ypt) {
		x = xpt;
		y = ypt;
		dailyRecord.clear();
		int scrollValue = 0;
		// if (viewport != null)
		scrollValue = viewport.getX();
		int index = 1 + (int) ((x - margins - scrollValue) / (BAR_W + INTERBARMARGINS));
		if (index < days.length)
			day = days[index];
		if (index > 0) {
			boolean first = true;
			float[] dayData = technicalData.get(day - 1);
			if (dayData != null)
				for (float f : dayData) {
					if (first) {
						first = false;
						dailyRecord.add(DATE_FORMAT.format(new Date(
								(long) f * 24 * 3600 * 1000)));
					} else if (f < 1e5) {
						dailyRecord.add("" + f);
					} else {
						if (f > 10000000) {
							f /= 1000000;
							dailyRecord.add(""
									+ new BigDecimal((long) f).toPlainString()
									+ " M");
						} else if (f > 10000) {
							f /= 1000;
							dailyRecord.add(""
									+ new BigDecimal((long) f).toPlainString()
									+ " K");
						} else {

							dailyRecord.add(""
									+ new BigDecimal((long) f).toPlainString());

						}
					}
				}
		}
	}

	protected Ellipse2D.Float calculateEllipseDisplay(int i, float exDivDate,
			float dayClose) {

		float divAmt = DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.get(ticker)
				.get(exDivDate);

		float ellipseWidth = DIVIDEND_100_PERCENT_CLOSE_WIDTH
				* (divAmt / dayClose);

		float left = margins + BAR_W / 2 + i * (BAR_W + INTERBARMARGINS)
				- ellipseWidth / 2;

		float top = eH / 2 - ellipseWidth / 2;

		return new Ellipse2D.Float(left, top, ellipseWidth, ellipseWidth);
	}

	private Line2D.Float calculateLineDisplay(int i, float thislow,
			float thishigh, Point2D.Float minmax) {
		float xLow = margins + BAR_W / 2 + i * (BAR_W + INTERBARMARGINS);
		float xHigh = xLow;

		float yLow = calculateVerticalScreenPoint(thislow, minmax);
		float yHigh = calculateVerticalScreenPoint(thishigh, minmax);

		return new Line2D.Float(xLow, yLow, xHigh, yHigh);
	}

	private float calculateVerticalScreenPoint(float value, Point2D.Float minmax) {
		float numerator = eH - margins;
		float denominator = minmax.y - minmax.x;
		// denominator should not be zero
		if (denominator == 0)
			denominator = 10;
		float factor = numerator / denominator;
		float pixels = (value - minMaxPrice.x) * factor;
		return margins + eH - pixels;
	}

	protected ArrayList<Line2D.Float> generateDisplayableLines(
			TreeMap<Float, Float> volume, Point2D.Float minmax) {
		ArrayList<Line2D.Float> hl = new ArrayList<Line2D.Float>();

		float firstDay = volume.firstKey();
		float f = firstDay;
		for (Entry<Float, Float> ent : volume.entrySet()) {
			float day = ent.getKey();
			float thislow = 0;
			float thishigh = ent.getValue();
			f = day - firstDay;
			Line2D.Float hlline = calculateLineDisplay((int) f, thislow,
					thishigh, minmax);
			hl.add(hlline);

		}
		return hl;
	}

	protected ArrayList<Line2D.Float> generateDisplayableLines(
			TreeMap<Float, Float> start, TreeMap<Float, Float> end,
			Point2D.Float minmax) {
		ArrayList<Line2D.Float> hl = new ArrayList<Line2D.Float>();

		float firstDay = end.firstKey();
		float f = firstDay;
		for (Entry<Float, Float> ent : end.entrySet()) {
			float day = ent.getKey();
			float thislow = start.get(day);
			float thishigh = ent.getValue();
			f = day - firstDay;
			Line2D.Float hlline = calculateLineDisplay((int) f, thislow,
					thishigh, minmax);
			hl.add(hlline);

		}
		return hl;
	}

	protected TreeMap<Float, Float> genMap(TreeMap<Float, float[]> techData,
			int id) {
		TreeMap<Float, Float> mapping = new TreeMap<Float, Float>();
		for (float[] daydata : techData.values())
			mapping.put(daydata[0], daydata[id]);

		return mapping;
	}
	
	
//	protected GeneralPath generateAvgPath(TreeMap<Float, Float> points,
//			Point2D.Float minMax) {
//		GeneralPath hl = new GeneralPath();
//		boolean first = true;
//		float firstDay = points.firstKey();
//
//		ArrayList<Float> keys = new ArrayList<Float>(points.keySet());
//
//		for (int J = avgLineNumber; J < (keys.size() - avgLineNumber); J++) {
//			float sum = 0;
//			float n = 0.01f;
//			for (int L = J - avgLineNumber; L <= J + 1 + 2 * avgLineNumber
//					&& L < keys.size(); L++) {
//				float day = keys.get(L);
//				float value = points.get(day);
//				sum += value;
//				n++;
//
//			}
//			float dayOfAvg = keys.get(J);
//			float f = dayOfAvg - firstDay;
//			float xMove = margins + BAR_W / 2 + f * (BAR_W + INTERBARMARGINS);
//			float average = sum / n;
//			float yMove = calculateVerticalScreenPoint(average, minMax);
//			if (first) {
//				first = false; 
//				hl.moveTo(xMove, yMove);
//			} else {
//				hl.lineTo(xMove, yMove);
//			}
//		}
//		return hl;
//	}
//	
	
	protected GeneralPath generateAvgPath(int type, Point2D.Float minMax,
			int avgLineNumber) {
		boolean useSqrt = false;
		TreeMap<Float, Float> points = genMap(technicalData, type);
		
		GeneralPath hl = new GeneralPath();
		boolean first = true;
		float firstDay = points.firstKey();

		ArrayList<Float> keys = new ArrayList<Float>(points.keySet());

		for (int J = avgLineNumber; J < (keys.size() - avgLineNumber); J++) {
			float sum = 0;
			float n = 0.01f;
			for (int L = J - avgLineNumber; L <= J + 1 + 2 * avgLineNumber
					&& L < keys.size(); L++) {
				float day = keys.get(L);
				float value = points.get(day);
				if (useSqrt)
					sum += Math.sqrt(value);
				sum+=value;
				n++;

			}
			float dayOfAvg = keys.get(J);
			float f = dayOfAvg - firstDay;
			float xMove = margins + BAR_W / 2 + f * (BAR_W + INTERBARMARGINS);
			float average = sum / n;
//			Point2D.Float newMinMax = new Point2D.Float(minMax.x, minMax.y);
//			if (useSqrt)
//				newMinMax = new Point2D.Float((float) Math.sqrt(minMax.x),
//						(float) Math.sqrt(minMax.y));
			float yMove = calculateVerticalScreenPoint(average,  minMax);
			if (first) {
				first = false;
				hl.moveTo(xMove, yMove);
			} else {
				hl.lineTo(xMove, yMove);
			}
		}
		return hl;
	}
}
 
