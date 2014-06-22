package harlequinmettle.finance.technicalanalysis.util;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseInterface;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.finance.technicalanalysis.view.TechnicalDatabaseViewer;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.CommonColors;
import harlequinmettle.utils.guitools.SmoothStroke;
import harlequinmettle.utils.guitools.SquareStroke;
import harlequinmettle.utils.numbertools.format.NumberFormater;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JViewport;

public class TickerTechModel {

	public static final int INTERBARMARGINS = 2;
	public static final int DIVIDEND_100_PERCENT_CLOSE_WIDTH = 5000;
	public static final int BAR_W = 10;
	public static final int margins = 20;

	public static float eW = 1111;
	public static float frameW = 500;
	public static float W = 2 * margins + eW;
	public static int eH = 1000;
	public static int H = 1000;
	public static final int FONT_SIZE = 18;
	public static final int REAL_BIG_FONT_SIZE = 24;
	public static final Font BIG_FONT = new Font("Bitstream", Font.PLAIN,
			FONT_SIZE);
	public static final Font REAL_BIG_FONT = new Font("Bitstream", Font.PLAIN,
			REAL_BIG_FONT_SIZE);
	public static final SmoothStroke SMOOTH_STROKE = new SmoothStroke(3);
	public static final SquareStroke SQUARE_STROKE = new SquareStroke(8);
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"EEE YYYY-MMM-dd");

	public TreeMap<Float, Ellipse2D.Float> dividendEllipses = new TreeMap<Float, Ellipse2D.Float>();
	public ArrayList<Line2D.Float> volumeBars;
	public ArrayList<Line2D.Float> openClose;
	public ArrayList<Line2D.Float> highLow;
	public Point2D.Float minMaxPrice;
	public Point2D.Float minMaxVolume;
	public float x, y, xf, yf, day;
	public float[] days;
	public float[][] technicalData;
	public float scalex = 1, scaley = 1;
	public String profile = "no profile on record";
	// TODO: VOLUME AVG LINE(S)
	// TODO: AVERAGE LINES
	// TODO: PERCENT
	// TODO: R-VALUE
	// TODO: LEAST SQUARES FIT
	// TODO: SEARCH RESULTS LIST
	// TODO: DIVIDENDS
	public ArrayList<String> dailyRecord = new ArrayList<String>();

	public TreeMap<String, String> currentFundamentals = new TreeMap<String, String>();

	public JViewport viewport;

	public String pathToObj = "technical_database_settings";
	public String profilePathKey = "path to profiles text file";
	public String ticker;
	public TreeMap<String, Boolean> myPreferences = new TreeMap<String, Boolean>();
	public static final String VOL_BARS = "show volume bars";
	public static final String CANDLESTICKS = "show candlesticks";
	public static final String GRID_LINES = "show grid";
	public static final String DIV_BALLS = "show dividends";
	// public static final String VOL_BAR ="show open line"
	// public static final String VOL_BAR = "show close line"
	// public static final String VOL_BAR = "show high line"
	// public static final String VOL_BAR ="show low line"
	// public static final String VOL_BAR = "show volume line"
	public static final String[] preferenceOptions = { //
	VOL_BARS, CANDLESTICKS, GRID_LINES, DIV_BALLS };
	public String preferencesSerializedName = "preferences_serialized_name";

	public TickerTechModel(String ticker) {
		this.ticker = ticker;
		init();
	}

	public void init() {
restorePreferences();
		String profilePath = establishPathToProfilesText();
		profile = getProfile(profilePath, ticker);
		profile = profile.replaceAll("\\.", "\\.\n\n");
		setFundamentalData(ticker);
		doSetUpWithTechnicalDatabaseSQLite(ticker);
	}

	public void doSetUpWithTechnicalDatabaseSQLite(String ticker2) {
		technicalData = TechnicalDatabaseViewer.TDB.SQLITE_PER_TICKER_PER_DAY_TECHNICAL_DATA
				.get(ticker2);
		// queryTechnicalDatabase(ticker2);
		eW = technicalData.length * (7f / 5f) * (BAR_W + INTERBARMARGINS);
		W = 2 * margins + eW;


		TreeMap<Float, Float> high = genMap(technicalData,
				TechnicalDatabaseSQLite.HIGH);
		TreeMap<Float, Float> low = genMap(technicalData,
				TechnicalDatabaseSQLite.LOW);
		TreeMap<Float, Float> open = genMap(technicalData,
				TechnicalDatabaseSQLite.OPEN);
		TreeMap<Float, Float> close = genMap(technicalData,
				TechnicalDatabaseSQLite.CLOSE);
		TreeMap<Float, Float> volume = genMap(technicalData,
				TechnicalDatabaseSQLite.VOLUME);

		minMaxPrice = calcMinMax(low, high);
		highLow = generateDisplayableLines(low, high, minMaxPrice);

		openClose = generateDisplayableLines(open, close, minMaxPrice);
		minMaxVolume = calcMinMax(volume);
		days = calculateDaysFromMap(volume);
		volumeBars = generateDisplayableLines(volume, minMaxVolume);

		if (DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.containsKey(ticker))
			dividendEllipses = generateDivDisplay(close);
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
		float lastmax = 0;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for (TreeMap<Float, Float> individual : values) {
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
		}
		return new Point2D.Float(min, lastmax);

	}

	private TreeMap<Float, Float> genMap(float[][] techData, int id) {
		TreeMap<Float, Float> mapping = new TreeMap<Float, Float>();
		for (float[] daydata : techData)
			mapping.put(daydata[0], daydata[id]);

		return mapping;
	}
	public void restorePreferences() {

		myPreferences = SerializationTool.deserialize(myPreferences.getClass(),
				preferencesSerializedName);
		if (myPreferences == null) {
			myPreferences = new TreeMap<String, Boolean>();
			for (String pref : preferenceOptions) {
				myPreferences.put(pref, true);
			}
		}
	}
	public void setDailyTradeData(float xpt, float ypt) {
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
			float[] dayData = technicalData[index - 1];
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
	public String getProfile(String profilePath, String ticker) {
		try {
			String[] files = { "NASDAQ_PROFILES_I.txt", "NYSE_PROFILES_I.txt" };
			// look for NASDAQ_PROFILES_I.txt and NYSE_PROFILES_I.txt
			// read each line by line to until starts with ticker^ return line
			for (String fileName : files) {
				File indexFile = new File(profilePath + File.separator
						+ fileName);
				System.out.println("file: " + indexFile.getAbsolutePath()
						+ "    exists:  " + indexFile.exists());
				int tries = 0;
				while (!indexFile.exists()) {
					profilePath = establishPathToProfilesText();
					indexFile = new File(profilePath + File.separator
							+ fileName);
					if (tries++ > 4)
						break;
				}
				try (BufferedReader br = new BufferedReader(new FileReader(
						indexFile))) {
					for (String line; (line = br.readLine()) != null;) {
						if (line.startsWith(ticker + "^"))
							return line.replaceAll("_", " ");
					}
					// line is not visible here.
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "no profile found";
	}

	public void setFundamentalData(String ticker) {

		TreeMap<String, Float> tickersFundamentals = CurrentFundamentalsSQLiteDatabase.CURRENT_TICKER_TO_LABEL_DATA_MAPING
				.get(ticker);
		for (int i = 0; i < CurrentFundamentalsDatabase.forDisplaying.length; i++) {
			String readabledata = "NAN";
			try {

				float data = tickersFundamentals
						.get(CurrentFundamentalsDatabase.forDisplaying[i]);
				if (data != data || Float.isInfinite(data))
					continue;
				readabledata = NumberFormater.floatToBMKTrunkated(data);

			} catch (Exception e) {
				e.printStackTrace();
			}
			currentFundamentals.put(
					CurrentFundamentalsDatabase.forDisplaying[i], readabledata);
		}

	}

	public String establishPathToProfilesText() {
		ChooseFilePrompterPathSaved profileDatabasePathSaver = new ChooseFilePrompterPathSaved(
				pathToObj);
		String profilePath = profileDatabasePathSaver
				.getSetting(profilePathKey);
		return profilePath;
	}

	public TreeMap<Float, Ellipse2D.Float> generateDivDisplay(
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

	public Ellipse2D.Float calculateEllipseDisplay(int i, float exDivDate,
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

	public Line2D.Float calculateLineDisplay(int i, float thislow,
			float thishigh, Point2D.Float minmax) {
		float xLow = margins + BAR_W / 2 + i * (BAR_W + INTERBARMARGINS);
		float xHigh = xLow;

		float yLow = calculateVerticalScreenPoint(thislow, minmax);
		float yHigh = calculateVerticalScreenPoint(thishigh, minmax);

		return new Line2D.Float(xLow, yLow, xHigh, yHigh);
	}

	public float calculateVerticalScreenPoint(float value, Point2D.Float minmax) {
		float numerator = eH - margins;
		float denominator = minmax.y - minmax.x;
		// denominator should not be zero
		if (denominator == 0)
			return eH / 2;
		float factor = numerator / denominator;
		float pixels = (value - minMaxPrice.x) * factor;
		return margins + eH - pixels;
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

	public ArrayList<Line2D.Float> generateDisplayableLines(
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

	public void drawDividendOvals(Graphics2D g) {

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

	public void drawProfileDescription(Graphics2D g) {
		g.setFont(REAL_BIG_FONT);
		g.setColor(CommonColors.FAINT_RED);
		FontMetrics fontMetrics = g.getFontMetrics();
		int width = fontMetrics.stringWidth(profile);
		float lineCount = width / (W / 2);
		int substringSize = (int) (profile.length() / lineCount);
		for (int i = 0; i < lineCount; i++) {
			int substringStart = i * substringSize;
			int substringEnd = (i + 1) * substringSize > profile.length() ? profile
					.length() : (i + 1) * substringSize;
			int x = 50;
			int y = 20 + (i + 2) * REAL_BIG_FONT_SIZE;
			g.drawString(profile.substring(substringStart, substringEnd), x, y);
		}
	}

	public void drawFundamentalsData(Graphics2D g) {

		g.setFont(BIG_FONT);
		g.setColor(CommonColors.REGION_HIGHLIGHT);

		float totalHeight = (margins + FONT_SIZE)
				* (4 + CurrentFundamentalsDatabase.forDisplaying.length);
		g.fill(new Rectangle2D.Float(xf, margins, 500, totalHeight));
		g.setColor(Color.black);

		int i = 0;

		for (String key : CurrentFundamentalsDatabase.forDisplaying) {

			g.drawString(key, xf + 5, margins + FONT_SIZE * (2 + i));
			if (currentFundamentals.containsKey(key))
				g.drawString(currentFundamentals.get(key).toString(), xf + 355,
						margins + FONT_SIZE * (2 + i));
			i++;

		}
	}

	public void drawDaysData(Graphics2D g) {

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

	public void drawBackground(Graphics2D g) {
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

	public void drawVolumeLines(Graphics2D g) {
		g.setStroke(SQUARE_STROKE);
		g.setColor(CommonColors.COLOR_HISTOGRAM_BAR_VOL);

		for (Line2D.Float vol : volumeBars) {

			g.draw(vol);

		}
	}

	public void drawOpenCloseLines(Graphics2D g) {
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

	public void drawHighLowLines(Graphics2D g) {
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

	public void setScrollBar(JViewport jViewport) {
		viewport = jViewport;
	}
}
