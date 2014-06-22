package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseInterface;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.CommonColors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TickerTechModelUtil extends TickerTechModelVars{

	protected float[] calculateDaysFromMap(TreeMap<Float, Float> volume) {

		float dayone = volume.firstKey();
		float lastday = volume.lastKey();
		int numberdays = (int) (lastday - dayone);
		float[] daystobe = new float[numberdays];
		for (int i = 0; i < numberdays; i++) {
			daystobe[i] = dayone + i;
		}
		return daystobe;
	}

	protected Point2D.Float calcMinMax(
			TreeMap<Float, Float> individual) {
		float lastmax = 0;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
 
			for (float f : individual.values()) {
				if (f == f && !Float.isInfinite(f)) {
					if (f > max) {
						lastmax = max;
						max = f;
					}
					if (f < min)
						min = f;
				}
			}
		 
		return new Point2D.Float(min, lastmax);

	}

	protected TreeMap<Float, float[]> mapTechnicalDataToDay(float[][] techData) {
		TreeMap<Float, float[]> mapping = new TreeMap<Float, float[]>();
		for (float[] daydata : techData)
			mapping.put(daydata[0], daydata);

		return mapping;
	}
	protected TreeMap<Float, Float> genMap(TreeMap<Float, float[]> techData, int id) {
		TreeMap<Float, Float> mapping = new TreeMap<Float, Float>();
		for (float[] daydata : techData.values())
			mapping.put(daydata[0], daydata[id]);

		return mapping;
	}

	protected void restorePreferences() {

		myPreferences = SerializationTool.deserialize(myPreferences.getClass(),
				preferencesSerializedName);
		if (myPreferences == null) {
			myPreferences = new TreeMap<String, Boolean>();
			for (String pref : preferenceOptions) {
				myPreferences.put(pref, true);
			}
		}
	}

	void setDailyTradeData(float xpt, float ypt) {
		x = xpt;
		y = ypt;
		dailyRecord.clear();
		int scrollValue = 0;
	//	if (viewport != null)
			scrollValue = viewport.getX();
		int index = 1 + (int) ((x - margins - scrollValue) / (BAR_W + INTERBARMARGINS));
		if (index < days.length)
			day = days[index];
		if (index > 0) {
			boolean first = true;
			float[] dayData = technicalData.get(day-1);
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



	protected TreeMap<Float, Ellipse2D.Float> generateDivDisplay(
			TreeMap<Float, Float> close) {
		TreeMap<Float, Ellipse2D.Float> divs = new TreeMap<Float, Ellipse2D.Float>();

		float firstDay = close.firstKey();
		float f = firstDay;
		for (Entry<Float, Float> ent : close.entrySet()) {
			float day = ent.getKey();

			if (!DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.get(ticker)
					.containsKey(day))
				continue;

			float dayClose = ent.getValue();
			f = day - firstDay;
			if (!DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.get(ticker)
					.containsKey(day))
				continue;
			Ellipse2D.Float dividendEllipse = calculateEllipseDisplay((int) f,
					day, dayClose);
			divs.put(day, dividendEllipse);

		}
		return divs;
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

	protected Line2D.Float calculateLineDisplay(int i, float thislow,
			float thishigh, Point2D.Float minmax) {
		float xLow = margins + BAR_W / 2 + i * (BAR_W + INTERBARMARGINS);
		float xHigh = xLow;

		float yLow = calculateVerticalScreenPoint(thislow, minmax);
		float yHigh = calculateVerticalScreenPoint(thishigh, minmax);

		return new Line2D.Float(xLow, yLow, xHigh, yHigh);
	}

	protected float calculateVerticalScreenPoint(float value, Point2D.Float minmax) {
		float numerator = eH - margins;
		float denominator = minmax.y - minmax.x;
		// denominator should not be zero
		if (denominator == 0)
			return eH / 2;
		float factor = numerator / denominator;
		float pixels = (value - minMaxPrice.x) * factor;
		return margins + eH - pixels;
	}

	protected ArrayList<java.awt.geom.Line2D.Float> generateDisplayableLines(
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

	void drawDividendOvals(Graphics2D g) {

		for (Entry<Float, Ellipse2D.Float> div : dividendEllipses.entrySet()) {
			g.setColor(Color.magenta);
			g.fill(div.getValue());
			g.setColor(Color.cyan);

			g.drawString(
					""
							+ DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.get(
									ticker).get(div.getKey()),
					div.getValue().x, eH / 2);
		}
	}

	void drawDaysData(Graphics2D g) {

		g.setColor(Color.black);
		g.setFont(BIG_FONT);
		String date = DATE_FORMAT
				.format(new Date((long) day * 24 * 3600 * 1000));
		float totalHeight = eH;// = (margins + FONT_SIZE) * (2 +
								// dailyRecord.size());
		// if (x > W - 100) {
		// float left = x - 200;
		// float top = y - 150;
		// g.setColor(CommonColors.REGION_HIGHLIGHT);
		// g.fill(new Rectangle2D.Float(left, margins, 200, totalHeight));
		// g.setColor(Color.black);
		// g.drawString(date, left + 5, top + FONT_SIZE + 5);
		// for (int i = 1; i < dailyRecord.size(); i++) {
		// g.drawString(TechnicalDatabase.elements[i], left + 5, top
		// + FONT_SIZE + 5 + FONT_SIZE * (1 + i));
		// g.drawString(dailyRecord.get(i), 80 + left + 5, top + FONT_SIZE
		// + 5 + FONT_SIZE * (1 + i));
		// }
		// } else {
		float left = x;
		float top = y;
		g.setColor(CommonColors.REGION_HIGHLIGHT);
		g.fill(new Rectangle2D.Float(left, margins, 200, totalHeight));
		g.setColor(Color.black);
		g.drawString(date, left + 5, top + FONT_SIZE + 5);
		for (int i = 1; i < dailyRecord.size(); i++) {

			g.drawString(TechnicalDatabaseInterface.elements[i], left + 5, top
					+ FONT_SIZE + 5 + FONT_SIZE * (1 + i));
			g.drawString(dailyRecord.get(i), 80 + left + 5, top + FONT_SIZE + 5
					+ FONT_SIZE * (1 + i));
		}

		g.drawString("" + minMaxPrice.y, left + 5, 50);
		g.drawString("" + minMaxPrice.x, left + 5, eH - 50);

		g.setColor(CommonColors.FAINT_ORANGE);
		g.drawString("" + minMaxVolume.y, left + 5, 80);
		g.drawString("" + minMaxVolume.x, left + 5, eH - 80);
		// }
	}

	void drawBackground(Graphics2D g) {
		Color bg = new Color(100, 120, 170);
		g.setColor(bg);
		g.fillRect(0, 0, (int) W, H + 100);
		// g.setColor(Color.DARK_GRAY);
		// g.drawRect(margins,margins,eW,eH);
		if (myPreferences.get(GRID_LINES)) {
			g.setColor(Color.LIGHT_GRAY);
			for (int i = margins; i < W - margins; i += 25) {
				g.drawLine(i, margins, i, margins + H);
			}
			for (int i = margins; i < H - margins; i += 25) {
				g.drawLine(margins, i, margins + (int) W, i);
			}
		}
	}

	void drawVolumeLines(Graphics2D g) {
		g.setStroke(SQUARE_STROKE);
		g.setColor(CommonColors.COLOR_HISTOGRAM_BAR_VOL);

		for (Line2D.Float vol : volumeBars) {

			g.draw(vol);

		}
	}

	void drawOpenCloseLines(Graphics2D g) {
		g.setStroke(SQUARE_STROKE);

		for (Line2D.Float openclose : openClose) {
			if (openclose.y1 > openclose.y2)
				g.setColor(Color.green);
			else if (openclose.y1 < openclose.y2)
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.draw(openclose);

		}
	}

	void drawHighLowLines(Graphics2D g) {
		g.setStroke(SMOOTH_STROKE);
		g.setColor(Color.black);

		int i = 0;
		for (Line2D.Float hilw : highLow) {

			if (i > 1) {
				float yesterdayClose = openClose.get(i - 1).y2;
				float todayClose = openClose.get(i).y2;
				if (todayClose < yesterdayClose)
					g.setColor(CommonColors.FAINT_GREEN);
				else
					g.setColor(CommonColors.FAINT_RED);
			}
			g.draw(hilw);
			i++;
		}
	}

}
