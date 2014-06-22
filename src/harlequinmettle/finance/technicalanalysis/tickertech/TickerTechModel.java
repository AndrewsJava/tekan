package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.applications.TechnicalDatabaseViewer;
import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseInterface;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.CommonColors;
import harlequinmettle.utils.guitools.SmoothStroke;
import harlequinmettle.utils.guitools.SquareStroke;
import harlequinmettle.utils.numbertools.format.NumberFormater;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
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
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JViewport;
//extension chain: TickerTechModelVars -> TickerTechModelUtil -> TickerTechModelRenderUtil -> TickerTechModelSetupUtil
public class TickerTechModel extends TickerTechModelSetupUtil {

	public TickerTechModel(String ticker) {
		this.ticker = ticker;
		init();
	}

	private void init() {
		//inherited  TickerTechModelSetupUtil
		restorePreferences();
		profile = getProfile( ticker);
		profile = profile.replaceAll("\\.", "\\.\n\n");
		setFundamentalData(ticker);
		//local with calls to TickerTechModelUtil
		doSetUpWithTechnicalDatabaseSQLite(ticker);
	}

	private void doSetUpWithTechnicalDatabaseSQLite(String ticker2) {
		technicalData = mapTechnicalDataToDay(TechnicalDatabaseViewer.TDB.SQLITE_PER_TICKER_PER_DAY_TECHNICAL_DATA
				.get(ticker2));
		// queryTechnicalDatabase(ticker2);
		eW = technicalData.size() * (7f / 5f) * (BAR_W + INTERBARMARGINS)+1000;
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
 
		minMaxPrice = calcMinMax(low);
		highLow = generateDisplayableLines(low, high, minMaxPrice);

		openClose = generateDisplayableLines(open, close, minMaxPrice);
		
		minMaxVolume = calcMinMax(volume);
		days = calculateDaysFromMap(volume);
		volumeBars = generateDisplayableLines(volume, minMaxVolume);
		
		
		avgVolPath = generateAvgPath(volume, minMaxVolume);

		if (DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.containsKey(ticker))
			dividendEllipses = generateDivDisplay(close);
	}



	private TreeMap<Float, Ellipse2D.Float> generateDivDisplay(
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

	private TreeMap<Float, float[]> mapTechnicalDataToDay(float[][] techData) {
		TreeMap<Float, float[]> mapping = new TreeMap<Float, float[]>();
		for (float[] daydata : techData)
			mapping.put(daydata[0], daydata);

		return mapping;
	}	private Point2D.Float calcMinMax(
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
	private TreeMap<Float, Float> genMap(TreeMap<Float, float[]> techData, int id) {
		TreeMap<Float, Float> mapping = new TreeMap<Float, Float>();
		for (float[] daydata : techData.values())
			mapping.put(daydata[0], daydata[id]);

		return mapping;
	}
	void setScrollBar(JViewport jViewport) {
		viewport = jViewport;
	}

}
