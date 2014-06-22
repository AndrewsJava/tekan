package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.utils.guitools.SmoothStroke;
import harlequinmettle.utils.guitools.SquareStroke;

import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JViewport;

public class TickerTechModelVars {

	protected final int INTERBARMARGINS = 2;
	protected final int DIVIDEND_100_PERCENT_CLOSE_WIDTH = 5000;
	protected final int BAR_W = 10;
	protected final int FONT_SIZE = 18;
	protected final int REAL_BIG_FONT_SIZE = 24;
	protected final SmoothStroke SMOOTH_STROKE = new SmoothStroke(3);
	protected final SquareStroke SQUARE_STROKE = new SquareStroke(8);
	protected final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"EEE YYYY-MMM-dd");
	protected final Font REAL_BIG_FONT = new Font("Bitstream", Font.PLAIN,
			REAL_BIG_FONT_SIZE);

	// protected final String VOL_BAR ="show open line"
	// protected final String VOL_BAR = "show close line"
	// protected final String VOL_BAR = "show high line"
	// protected final String VOL_BAR ="show low line"
	// protected final String VOL_BAR = "show volume line"
	final String VOL_BARS = "show volume bars";
	final String CANDLESTICKS = "show candlesticks";
	final String GRID_LINES = "show grid";
	final String DIV_BALLS = "show dividends";
	final String[] preferenceOptions = { //
	VOL_BARS, CANDLESTICKS, GRID_LINES, DIV_BALLS };
	final int margins = 20;
	final Font BIG_FONT = new Font("Bitstream", Font.PLAIN, FONT_SIZE);

	protected ArrayList<String> dailyRecord = new ArrayList<String>();
	protected JViewport viewport;
	protected TreeMap<Float, Ellipse2D.Float> dividendEllipses = new TreeMap<Float, Ellipse2D.Float>();
	protected ArrayList<Line2D.Float> volumeBars;
	protected ArrayList<Line2D.Float> openClose;
	protected ArrayList<Line2D.Float> highLow;
	protected Point2D.Float minMaxPrice;
	protected Point2D.Float minMaxVolume;
	protected float day;
	protected float[] days;
	//protected float[][] technicalData;
	TreeMap<Float, float[]> technicalData = new TreeMap<Float, float[]>();
	protected float scalex = 1, scaley = 1;

	float eW = 1111;
	float frameW = 500;
	float W = 2 * margins + eW;
	int eH = 1000;
	int H = 1000;
	float x, y;
	float xf;
	float yf;
	TreeMap<String, String> currentFundamentals = new TreeMap<String, String>();
	TreeMap<String, Boolean> myPreferences = new TreeMap<String, Boolean>();
	String profile = "no profile on record";
	String preferencesSerializedName = "application_settings" + File.separator
			+ "preferences_serialized_name";
	String ticker;
	// TODO: VOLUME AVG LINE(S)
	// TODO: AVERAGE LINES
	// TODO: PERCENT
	// TODO: R-VALUE
	// TODO: LEAST SQUARES FIT
}
