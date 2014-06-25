package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.utils.guitools.CommonColors;
import harlequinmettle.utils.numbertools.format.NumberFormater;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

public class TickerTechModelRenderUtil extends TickerTechModelUtil {

	protected void drawDividendOvals(Graphics2D g) {

		for (Entry<Float, Ellipse2D.Float> div : dividendEllipses.entrySet()) {
			g.setColor(Color.magenta);
			g.fill(div.getValue());
			g.setColor(Color.cyan);
			float dividend = DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.get(ticker).get(div.getKey());

			String exDivDate = DATE_FORMAT.format(new Date((long) (float) div.getKey() * 24 * 3600 * 1000));
			g.drawString("" + dividend, div.getValue().x, eH / 2);
			g.drawString("" + exDivDate, div.getValue().x, eH / 2 + 25);
		}
	}

	protected void drawDates(Graphics2D g) {
		g.setFont(BIG_FONT);
		g.setColor(Color.black);
		float month = 4 * 7 * (BAR_W + INTERBARMARGINS);
		for (int i = margins; i < W - margins; i += month) {

			float dayNumber = tryToGetDateForPixel(i);
			String date = DATE_FORMAT.format(new Date((long) dayNumber * 24 * 3600 * 1000));
			g.drawString(date, i, margins);
		}
	}

	private float tryToGetDateForPixel(int i) {

		int index = 1 + (int) ((i - margins) / (BAR_W + INTERBARMARGINS));
		if (index < 0 || index > days.length)
			return 0;
		float dayNumber = days[index];
		return dayNumber;
	}

	protected void drawDaysData(Graphics2D g) {

		if (day == 0 && x == 0)
			return;
		g.setColor(Color.black);
		g.setFont(BIG_FONT);
		String date = DATE_FORMAT.format(new Date((long) day * 24 * 3600 * 1000));
		float totalHeight = 260;
		float left = x;
		float top = y;
		g.setColor(CommonColors.REGION_HIGHLIGHT);
		g.fill(new Rectangle2D.Float(left, y-30, 220, totalHeight));
		g.setColor(Color.black);
		g.drawString(date, left + 5, top + FONT_SIZE + 5);
		for (int i = 1; i < dailyRecord.size(); i++) {

			g.drawString(dailyRecordLabels.get(i), left + 5, top + FONT_SIZE + 5 + FONT_SIZE * (1 + i));
			g.drawString(dailyRecord.get(i), 110 + left + 5, top + FONT_SIZE + 5 + FONT_SIZE * (1 + i));
		}

//		g.drawString("" + minMaxPrice.y, left + 5, 50);
//		g.drawString("" + minMaxPrice.x, left + 5, eH - 50);
//
//		g.setColor(CommonColors.FAINT_ORANGE);
//		g.drawString("" + minMaxVolume.y, left + 5, 80);
//		g.drawString("" + minMaxVolume.x, left + 5, eH - 80);
		// }
	}

	protected void drawBackground(Graphics2D g) {
		Color bg = new Color(100, 120, 170);
		g.setColor(bg);
		g.fillRect(visible.x,visible.y,visible.width,visible.height);
		//g.fillRect(0, 0, (int) W, H + 100);
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

	protected void drawMeasurementIndicators(Graphics2D g) {
		if (!myPreferences.get(GRAPH_PRICE_MEASURE) &&  !myPreferences.get(GRAPH_VOL_MEASURE))
			return;
		g.setColor(CommonColors.REGION_HIGHLIGHT);
		g.setStroke(THIN_STROKE);
		int numberOfIntervals = 5;
		float interval = eH / numberOfIntervals;
		ArrayList<Integer> yValues = new ArrayList<Integer>();
		for (int i = margins; i < H - margins; i += interval) {
			g.drawLine(margins, i, (int) W - margins, i);
			yValues.add(i);
		}
		if (myPreferences.get(GRAPH_PRICE_MEASURE))
			drawPrices(g, numberOfIntervals, yValues);
		if (myPreferences.get(GRAPH_VOL_MEASURE))
			drawVolumes(g, numberOfIntervals, yValues);
	}

	private void drawVolumes(Graphics2D g, int numberOfIntervals, ArrayList<Integer> yVal) {
	 
		g.setColor(Color.yellow);
		g.setFont(BIG_FONT);       
		float left = visible.x+visible.width-margins-100;
		float volInterval = (minMaxVolume.y-minMaxVolume.x)/numberOfIntervals;
		float volume = minMaxVolume.y+volInterval;
		for (int y: yVal) { 
			volume-=volInterval;
			  String displayVol =NumberFormater.floatToBMKTrunkated(volume);
			g.drawString(displayVol ,  left  ,y+23);
		}
	}

	private void drawPrices(Graphics2D g, int numberOfIntervals, ArrayList<Integer> yVal) {
		g.setColor(Color.green);
		g.setFont(BIG_FONT);       
		int left =  visible.x+margins;
		float priceInterval = (minMaxPrice.y-minMaxPrice.x)/numberOfIntervals;
		float price = minMaxPrice.y+priceInterval;
		for (int y: yVal) { 
			  price-=priceInterval;
			  String displayPrice =NumberFormater.floatToBMKTrunkated(price, 2);
			g.drawString(displayPrice ,  left  ,y+23);
		}
	}

	public void drawWeeklyLines(Graphics2D g) {
		g.setColor(CommonColors.REGION_HIGHLIGHT);
		g.setStroke(SQUARE_STROKE);
		float week = 7 * (BAR_W + INTERBARMARGINS);
		for (int i = margins; i < W - margins; i += week) {
			g.drawLine(i, margins, i, margins + H);
		}
	}

	protected void drawAvgLines(Graphics2D g) {
		for (OptionsMenuChoicePanel avgLine : lineAverageChoices) {
			if (!avgLine.isDisplayPreferred() || avgLine.path == null)
				continue;
			g.setStroke(SMOOTH_STROKE);
			g.setColor(avgLine.getColor());
			g.draw(avgLine.path);
		}
	}

	protected void drawVolumeLines(Graphics2D g) {
		g.setStroke(SQUARE_STROKE);
		g.setColor(CommonColors.COLOR_VOL);

		for (Line2D.Float vol : volumeBars) {

			g.draw(vol);

		}
	}

	protected void drawOpenCloseLines(Graphics2D g) {
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

	protected void drawHighLowLines(Graphics2D g) {
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
