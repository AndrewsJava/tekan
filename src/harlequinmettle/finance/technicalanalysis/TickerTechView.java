package harlequinmettle.finance.technicalanalysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JPanel;

public class TickerTechView extends JPanel {
	public static final int INTERBARMARGINS = 2;
	public static final int BAR_W = 10;
	public static final int margins = 20;
	public static int eW = TechnicalDatabase.NUM_DAYS
			* (BAR_W + INTERBARMARGINS);
	public static int W = 2 * margins + eW;
	public static int eH = 1000;
	public static int H = 1000;
	public static final int FONT_SIZE = 15;
	public static final Font BIG_FONT = new Font("Sans-Serif", Font.BOLD,
			FONT_SIZE);

	private ArrayList<Rectangle2D.Float> volumeBars;
	private ArrayList<Rectangle2D.Float> openClose;
	private ArrayList<Boolean> openToCloseGains;
	private ArrayList<Line2D.Float> highLow;
	private Point2D.Float minMaxPrice;
	private Point2D.Float minMaxVolume;

	public TickerTechView() {
		init("BME");
	}

	public TickerTechView(String ticker) {
		init(ticker);
	}

	private void init(String ticker) {
		 setPreferredSize(new Dimension(W, H+40));
		TreeMap<Float, Float> high = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.HIGH);
		TreeMap<Float, Float> low = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.LOW);
		minMaxPrice = calcMinMax(low, high);
		highLow = generateHighLowLines(high, low);
	}

	private java.awt.geom.Point2D.Float calcMinMax(TreeMap<Float, Float> low,
			TreeMap<Float, Float> high) {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for (float f : high.values()) {
			if (f == f && !Float.isInfinite(f))
				if (f > max)
					max = f;
		}

		for (float f : low.values()) {
			if (f == f && !Float.isInfinite(f))
				if (f < min)
					min = f;
		}

		return new Point2D.Float(min, max);

	}

	private ArrayList<Line2D.Float> generateHighLowLines(
			TreeMap<Float, Float> high, TreeMap<Float, Float> low) {
		ArrayList<Line2D.Float> hl = new ArrayList<Line2D.Float>();
	 
		float firstDay = high.firstKey();
		float f =  firstDay;
		for (Entry<Float, Float> ent : high.entrySet()) {
			float day = ent.getKey();
			float thislow = low.get(day);
			float thishigh = ent.getValue();
			f = day - firstDay;
			Line2D.Float hlline = calculateHiLowLine((int)f, thislow, thishigh);
			hl.add(hlline);
		 
		}
		return hl;
	}

	private Line2D.Float calculateHiLowLine(int i, float thislow, float thishigh) {
		float xLow = margins + BAR_W / 2 + i * (BAR_W + INTERBARMARGINS);
		float xHigh = xLow;

		float yLow = calculateVerticalScreenPoint(thislow);
		float yHigh = calculateVerticalScreenPoint(thishigh);

		return new Line2D.Float(xLow, yLow, xHigh, yHigh);
	}

	private float calculateVerticalScreenPoint(float thislow) {
		float numerator = eH - margins;
		float denominator = minMaxPrice.y - minMaxPrice.x;
		// denominator should not be zero
		if (denominator == 0)
			return eH / 2;
		float factor = numerator / denominator;
		float pixels = (thislow - minMaxPrice.x) * factor;
		return margins + eH - pixels;
	}

	@Override
	public void paint(Graphics  g1) {
		Graphics2D g = (Graphics2D)g1;
		Color bg = new Color(150,180,200);
g.setColor(bg);
g.fillRect(0, 0, W, H);
g.setColor(Color.black);
for(Line2D.Float hilw : highLow ){
	g.draw(hilw);
}
	}

	public void rescaleCanvas(Dimension size) {
		// TODO Auto-generated method stub
		
	}
}
