package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.finance.technicalanalysis.model.CurrentFundamentalsDatabase;
import harlequinmettle.finance.technicalanalysis.model.TechnicalDatabase;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.finance.TickerSetWithETFs;
import harlequinmettle.utils.guitools.CommonColors;
import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.PreferredJScrollPane;
import harlequinmettle.utils.guitools.SmoothStroke;
import harlequinmettle.utils.guitools.SquareStroke;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class TickerTechView extends JPanel {
	public static final int INTERBARMARGINS = 2;
	public static final int BAR_W = 10;
	public static final int margins = 20;
	public static int eW = TechnicalDatabase.NUM_DAYS
			* (BAR_W + INTERBARMARGINS);
	public static int W = 2 * margins + eW;
	public static int eH = 1000;
	public static int H = 1000;
	public static final int FONT_SIZE = 18;
	public static final Font BIG_FONT = new Font("Bitstream", Font.PLAIN,
			FONT_SIZE);
	public static final SmoothStroke SMOOTH_STROKE = new SmoothStroke(3);
	public static final SquareStroke SQUARE_STROKE = new SquareStroke(8);
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"EEE YYYY-MMM-dd");

	private ArrayList<Line2D.Float> volumeBars;
	private ArrayList<Line2D.Float> openClose;
	private ArrayList<Line2D.Float> highLow;
	private Point2D.Float minMaxPrice;
	private Point2D.Float minMaxVolume;
	private float x, y, xf, yf, day;
	float[] days;
	float[][] technicalData;
	private float scalex = 1, scaley = 1;
	boolean toggleVisible = false;
	// TODO: VOLUME AVG LINE(S)
	// TODO: AVERAGE LINES
	// TODO: PERCENT
	// TODO: R-VALUE
	// TODO: LEAST SQUARES FIT
	// TODO: SEARCH RESULTS LIST
	// TODO: DIVIDENDS
	private ArrayList<String> dailyRecord = new ArrayList<String>();

	private TreeMap<String, String> currentFundamentals = new TreeMap<String, String>();

	private JViewport viewport;

	String profilePathKey = "path to profiles text file";

	public TickerTechView() {
		init("BME");
	}

	public TickerTechView(String ticker) {
		init(ticker);
	}

	public void updateSizePreferrence() {
		setPreferredSize(new Dimension((int) (scalex * W),
				(int) (scaley * H + 40)));
	}

	private void init(String ticker) {
		ChooseFilePrompterPathSaved profileDatabasePathSaver = new ChooseFilePrompterPathSaved(
				profilePathKey);
		String profilePath = profileDatabasePathSaver
				.getSetting(profilePathKey);
		String profile = getProfile(profilePath, ticker);
		updateSizePreferrence();
		setFundamentalData(ticker);
		this.addMouseListener(dateDisplayer);
		TreeMap<Float, Float> high = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.HIGH);
		TreeMap<Float, Float> low = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.LOW);

		minMaxPrice = calcMinMax(low, high);
		highLow = generateDisplayableLines(low, high, minMaxPrice);

		TreeMap<Float, Float> open = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.OPEN);
		TreeMap<Float, Float> close = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.CLOSE);
		openClose = generateDisplayableLines(open, close, minMaxPrice);

		TreeMap<Float, Float> volume = TechnicalDatabase.makeGetDataToDateMap(
				ticker, TechnicalDatabase.VOLUME);
		minMaxVolume = calcMinMax(volume);
		days = calculateDaysFromMap(volume);
		volumeBars = generateDisplayableLines(volume, minMaxVolume);
		technicalData = TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA
				.get(ticker);
		showChartInNewWindow(ticker);
	}

	private String getProfile(String profilePath, String ticker) {
		try {
			String[] files = { "NASDAQ_PROFILES_I.txt", "NYSE_PROFILES_I.txt" };
			// look for NASDAQ_PROFILES_I.txt and NYSE_PROFILES_I.txt
			// read each line by line to until starts with ticker^ return line
			for (String fileName : files) {
				File indexFile = new File(profilePath + File.separator
						+ fileName);
				if (!indexFile.exists())
					continue;

				try (BufferedReader br = new BufferedReader(
						new FileReader(indexFile))) {
					for (String line; (line = br.readLine()) != null;) {
						// process the line.
					}
					// line is not visible here.
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "no profile found";
	}

	private void setFundamentalData(String ticker) {
		try {
			int tickerIndex = TickerSetWithETFs.TICKERS.indexOf(ticker);
			float[] tickersFundamentals = CurrentFundamentalsDatabase.data[tickerIndex];
			for (int i = 0; i < CurrentFundamentalsDatabase.labels.length; i++) {
				BigDecimal readableNumber = new BigDecimal(
						tickersFundamentals[i]).round(new MathContext(2));

				currentFundamentals.put(CurrentFundamentalsDatabase.labels[i],
						"" + readableNumber.toPlainString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<java.awt.geom.Line2D.Float> generateDisplayableLines(
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

	private float[] calculateDaysFromMap(TreeMap<Float, Float> volume) {

		float dayone = volume.firstKey();
		float lastday = volume.lastKey();
		int numberdays = (int) (lastday - dayone);
		float[] daystobe = new float[numberdays];
		for (int i = 0; i < numberdays; i++) {
			daystobe[i] = dayone + i;
		}
		return daystobe;
	}

	private java.awt.geom.Point2D.Float calcMinMax(
			TreeMap<Float, Float>... values) {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for (TreeMap<Float, Float> individual : values) {
			for (float f : individual.values()) {
				if (f == f && !Float.isInfinite(f)) {
					if (f > max)
						max = f;
					if (f < min)
						min = f;
				}
			}
		}
		return new Point2D.Float(min, max);

	}

	private ArrayList<Line2D.Float> generateDisplayableLines(
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
			return eH / 2;
		float factor = numerator / denominator;
		float pixels = (value - minMaxPrice.x) * factor;
		return margins + eH - pixels;
	}

	// @Override
	// public void update(Graphics g) {
	// super.update(g);
	// }
	@Override
	public void paint(Graphics g1) {
		updateSizePreferrence();
		Graphics2D g = (Graphics2D) g1;

		g.scale(scalex, scaley);
		drawBackground(g);
		drawVolumeLines(g);
		drawHighLowLines(g);
		drawOpenCloseLines(g);
		drawDatesData(g);
		if (toggleVisible)
			drawFundamentalsData(g);
	}

	private void drawFundamentalsData(Graphics2D g) {

		g.setFont(BIG_FONT);
		g.setColor(CommonColors.REGION_HIGHLIGHT);

		float totalHeight = (margins + FONT_SIZE)
				* (4 + CurrentFundamentalsDatabase.forDisplaying.length);
		g.fill(new Rectangle2D.Float(xf, yf, 500, totalHeight));
		g.setColor(Color.black);

		int i = 0;

		for (String key : CurrentFundamentalsDatabase.forDisplaying) {

			g.drawString(key, xf + 5, yf + FONT_SIZE + 5 + FONT_SIZE * (2 + i));
			g.drawString(currentFundamentals.get(key).toString(), xf + 355, yf
					+ FONT_SIZE + 5 + FONT_SIZE * (2 + i));
			i++;

		}
	}

	private void drawDatesData(Graphics2D g) {
		g.setColor(Color.black);
		g.setFont(BIG_FONT);
		String date = DATE_FORMAT
				.format(new Date((long) day * 24 * 3600 * 1000));
		float totalHeight = (margins + FONT_SIZE) * (2 + dailyRecord.size());
		if (x > W - 100) {
			float left = x - 200;
			float top = y - 150;
			g.setColor(CommonColors.REGION_HIGHLIGHT);
			g.fill(new Rectangle2D.Float(left, top, 200, totalHeight));
			g.setColor(Color.black);
			g.drawString(date, left + 5, top + FONT_SIZE + 5);
			for (int i = 1; i < dailyRecord.size(); i++) {
				g.drawString(TechnicalDatabase.elements[i], left + 5, top
						+ FONT_SIZE + 5 + FONT_SIZE * (1 + i));
				g.drawString(dailyRecord.get(i), 80 + left + 5, top + FONT_SIZE
						+ 5 + FONT_SIZE * (1 + i));
			}
		} else {
			float left = x;
			float top = y - 150;
			g.setColor(CommonColors.REGION_HIGHLIGHT);
			g.fill(new Rectangle2D.Float(left, top, 200, totalHeight));
			g.setColor(Color.black);
			g.drawString(date, left + 5, top + FONT_SIZE + 5);
			for (int i = 1; i < dailyRecord.size(); i++) {

				g.drawString(TechnicalDatabase.elements[i], left + 5, top
						+ FONT_SIZE + 5 + FONT_SIZE * (1 + i));
				g.drawString(dailyRecord.get(i), 80 + left + 5, top + FONT_SIZE
						+ 5 + FONT_SIZE * (1 + i));
			}
		}
	}

	private void drawBackground(Graphics2D g) {
		Color bg = new Color(100, 120, 170);
		g.setColor(bg);
		g.fillRect(0, 0, W, H + 100);
		// g.setColor(Color.DARK_GRAY);
		// g.drawRect(margins,margins,eW,eH);
		g.setColor(Color.LIGHT_GRAY);
		for (int i = margins; i < W - margins; i += 25) {
			g.drawLine(i, margins, i, margins + H);
		}
		for (int i = margins; i < H - margins; i += 25) {
			g.drawLine(margins, i, margins + W, i);
		}
	}

	private void drawVolumeLines(Graphics2D g) {
		g.setStroke(SQUARE_STROKE);

		for (Line2D.Float vol : volumeBars) {

			g.setColor(CommonColors.DARK_ORANGE);
			g.draw(vol);

		}
	}

	private void drawOpenCloseLines(Graphics2D g) {
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

	private void drawHighLowLines(Graphics2D g) {
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

	public void rescaleCanvas(Dimension size) {
		// TODO Auto-generated method stub

	}

	public void setScrollBar(JViewport jViewport) {
		viewport = jViewport;
	}

	private void showChartInNewWindow(String ticker) {
		final JFrame container = new JFrame(ticker + "   "
				+ TechnicalDatabase.NUM_DAYS + "  days");

		container.setSize(900, 550);
		container.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		container.setVisible(true);
		JScrollPanelledPane chart = new JScrollPanelledPane();
		// TickerTechView tv = new TickerTechView(ticker);
		PreferredJScrollPane tickerTechScroll = new PreferredJScrollPane(this);
		this.setScrollBar(tickerTechScroll.getViewport());

		chart.addComp(tickerTechScroll);

		container.add(chart);
		container.setExtendedState(container.getExtendedState()
				| JFrame.MAXIMIZED_BOTH);
		final ComponentListener refForRemoval = doWindowRescaleListener(this);
		container.addComponentListener(refForRemoval);
		container.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				container.removeComponentListener(refForRemoval);
			}
		});
	}

	private ComponentListener doWindowRescaleListener(final TickerTechView tv) {
		return new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent arg0) {

				tv.rescaleCanvas(arg0.getComponent().getBounds().getSize());

			}
		};
	}

	final MouseAdapter dateDisplayer = new MouseAdapter() {

		public void mouseClicked(MouseEvent e) {
			float newx = e.getX();
			float newy = e.getY();
			if (SwingUtilities.isLeftMouseButton(e)) {
				setDailyTradeData(newx, newy);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				toggleVisible = !toggleVisible;
				xf = newx;
				yf = newy;
			} else if (SwingUtilities.isMiddleMouseButton(e)) {

			}
			repaint();
		}

		private void setDailyTradeData(float xpt, float ypt) {
			x = xpt;
			y = ypt;
			dailyRecord.clear();
			int scrollValue = 0;
			if (viewport != null)
				scrollValue = viewport.getX();
			int index = 1 + (int) ((x - margins - scrollValue) / (BAR_W + INTERBARMARGINS));
			if (index < days.length)
				day = days[index];
			if (index > 0) {
				boolean first = true;
				float[] dayData = technicalData[index];
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
										+ new BigDecimal((long) f)
												.toPlainString() + " M");
							} else if (f > 10000) {
								f /= 1000;
								dailyRecord.add(""
										+ new BigDecimal((long) f)
												.toPlainString() + " K");
							} else {

								dailyRecord.add(""
										+ new BigDecimal((long) f)
												.toPlainString());

							}
						}
					}
			}
		}
	};
}
