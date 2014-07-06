package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.numbertools.format.NumberTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeMap;

public class TickerTechModelSetupUtil extends TickerTechModelRenderUtil{

	protected void restorePreferences() {
 
		myPreferences = SerializationTool.deserializeObject(myPreferences.getClass(),
				preferencesSerializedName);
		if (myPreferences == null) {
			myPreferences = new TreeMap<String, Boolean>();
			for (String pref : preferenceOptions) {
				myPreferences.put(pref, true);
			}
		}
		//////////////////

		optionStates = SerializationTool.deserializeObject(optionStates.getClass(),
				morePreferencesSerializedName);
		if (optionStates == null) {
			optionStates = new ArrayList<OptionsMenuModel> ();
		
		}
	}

	private String establishPathToProfilesText() {
		ChooseFilePrompterPathSaved profileDatabasePathSaver = new ChooseFilePrompterPathSaved(
				"application_settings", "technical_database_settings");
		String profilePath = profileDatabasePathSaver
				.getSetting("path to profiles text file");
		return profilePath;
	}
	protected String getProfile(  String ticker) {

		String profilePath = establishPathToProfilesText();
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
	protected void setFundamentalData(String ticker) {

		TreeMap<String, Float> tickersFundamentals = CurrentFundamentalsSQLiteDatabase.CURRENT_TICKER_TO_LABEL_DATA_MAPING
				.get(ticker);
		for (int i = 0; i < CurrentFundamentalsSQLiteDatabase.forDisplaying.length; i++) {
			String readabledata = "NAN";
			try {

				float data = tickersFundamentals
						.get(CurrentFundamentalsSQLiteDatabase.forDisplaying[i]);
				if (data != data || Float.isInfinite(data))
					continue;
				readabledata = NumberTools.floatToBMKTrunkated(data);

			} catch (Exception e) {
				e.printStackTrace();
			}
			currentFundamentals.put(
					CurrentFundamentalsSQLiteDatabase.forDisplaying[i],
					readabledata);
		}

	}

}
