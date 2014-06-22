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

public class TickerTechModel extends TickerTechModelUtil {

	public TickerTechModel(String ticker) {
		this.ticker = ticker;
		init();
	}

	private void init() {
		restorePreferences();
		String profilePath = establishPathToProfilesText();
		profile = getProfile(profilePath, ticker);
		profile = profile.replaceAll("\\.", "\\.\n\n");
		setFundamentalData(ticker);
		doSetUpWithTechnicalDatabaseSQLite(ticker);
	}
	private void setFundamentalData(String ticker) {

		TreeMap<String, Float> tickersFundamentals = CurrentFundamentalsSQLiteDatabase.CURRENT_TICKER_TO_LABEL_DATA_MAPING
				.get(ticker);
		for (int i = 0; i < CurrentFundamentalsSQLiteDatabase.forDisplaying.length; i++) {
			String readabledata = "NAN";
			try {

				float data = tickersFundamentals
						.get(CurrentFundamentalsSQLiteDatabase.forDisplaying[i]);
				if (data != data || Float.isInfinite(data))
					continue;
				readabledata = NumberFormater.floatToBMKTrunkated(data);

			} catch (Exception e) {
				e.printStackTrace();
			}
			currentFundamentals.put(
					CurrentFundamentalsSQLiteDatabase.forDisplaying[i],
					readabledata);
		}

	}
	private String getProfile(String profilePath, String ticker) {
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

	private String establishPathToProfilesText() {
		ChooseFilePrompterPathSaved profileDatabasePathSaver = new ChooseFilePrompterPathSaved(
				"application_settings", "technical_database_settings");
		String profilePath = profileDatabasePathSaver
				.getSetting("path to profiles text file");
		return profilePath;
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
//		TreeMap<Float, Float> allHighLow = new TreeMap<Float, Float> ();
//		allHighLow.putAll(high);
//		allHighLow.putAll(low);
		minMaxPrice = calcMinMax(low);
		highLow = generateDisplayableLines(low, high, minMaxPrice);

		openClose = generateDisplayableLines(open, close, minMaxPrice);
		minMaxVolume = calcMinMax(volume);
		days = calculateDaysFromMap(volume);
		volumeBars = generateDisplayableLines(volume, minMaxVolume);

		if (DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.containsKey(ticker))
			dividendEllipses = generateDivDisplay(close);
	}


	void setScrollBar(JViewport jViewport) {
		viewport = jViewport;
	}
}
