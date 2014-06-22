package harlequinmettle.finance.technicalanalysis.legacy;

import harlequinmettle.finance.database.DataUtil;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.filetools.SerializationToolExplicit;
import harlequinmettle.utils.finance.TickerSetWithETFsOptimized;
import harlequinmettle.utils.guitools.FilterPanel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class CurrentFundamentalsDatabase {
//
//	int nancount = 0;
//	int count = 0;
//	public   String pathToObj = "technical_database_settings";
//	String pathtodata = "path to small database";
//
//	ArrayList<String> tickersForDatabase = TickerSetWithETFsOptimized.TICKERS;
//
//	public CurrentFundamentalsDatabase(ArrayList<String> forTickers) {
//		tickersForDatabase = forTickers;
//		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
//				pathToObj);
//		root = settingssaver.getSetting(pathtodata);
//		init();
//	}
//
//	public CurrentFundamentalsDatabase() {
//		ChooseFilePrompterPathSaved settingssaver = new ChooseFilePrompterPathSaved(
//				pathToObj);
//		root = settingssaver.getSetting(pathtodata);
//		init();
//	}
//
//	private void init() {
////TODO SQLITE DB BECAUSE SERIALIZATION DOESN'T WORK FOR NESTED MIXED VARIABLE MAPS
////		if (new File("TECHNICAL_DATA/OBJECTS/FUNDAMENTALDATAOBJ_"
////				+ tickersForDatabase.size()).exists()) {
////			data = SerializationToolExplicit.deserializeCurrentFundamentalsDatabase(
////					"TECHNICAL_DATA/OBJECTS/DIVDATAOBJ_");
////		} else {
//
//			for (String ticker : tickersForDatabase) {
//				  TreeMap<String,Float> nanInitMap = new TreeMap<String,Float>();
//				  for(String label: labels){
//					  nanInitMap.put(label, Float.NaN);
//				  }
//				data.put(ticker, nanInitMap	);
//		 	}
//
//			loadFundamentalData();
//
////			SerializationTool.serialize(data,
////					"TECHNICAL_DATA/OBJECTS/FUNDAMENTALDATAOBJ_"
////							+ tickersForDatabase.size());
//
//		//}
//	}
//
//	public TreeMap<String, String> getFilterResults(FilterPanel[] searchFilters) {
//		TreeMap<String, String> results = new TreeMap<String, String>();
//		int i = 0;
//		for (String ticker : tickersForDatabase) {
//			boolean qualifies = true;
//			String reasonForQualification = "";
//			for (FilterPanel filter : searchFilters) {
//				if (!filter.shouldFilterBeApplied())
//					continue;
//				int id = filter.getId();
//				float low = filter.getLow();
//				float high = filter.getHigh();
//				float dataPoint = data.get(ticker).get(forDisplaying[id]);
//				if (dataPoint != dataPoint || dataPoint > high
//						|| dataPoint < low)
//					qualifies = false;
//				else
//					reasonForQualification += "  " + forDisplaying[id] + "  :  ["
//							+ dataPoint + "] ";
//			}
//
//			if (qualifies)
//				results.put(ticker, reasonForQualification);
//			i++;
//		}
//		return results;
//	}
//
//	public void loadFundamentalData() {
//
//		// for each time stage file construct array 85 fundamental data
//		// points for each symbol
//
//		String[] smallDBFiles = new File(root + File.separator + "q").list();
//		String[] smallDBFiles2 = new File(root + File.separator + "y").list();
//		Arrays.sort(smallDBFiles);
//		Arrays.sort(smallDBFiles2);
//		// get some data from 1year ago
//		boolean isOneYearAgo = true;
//		for (int x = smallDBFiles.length - 54; x < smallDBFiles.length; x++) {
//			// skip ahead 44 weeks worth
//			if (x == smallDBFiles.length - 48) {
//				x += 44;
//				isOneYearAgo = false;
//			}
//			TreeMap<String, String> textData = new TreeMap<String, String>();
//
//			DataUtil.loadStringData(root + File.separator + "q"
//					+ File.separator + smallDBFiles[x], textData);
//			DataUtil.loadStringData(root + File.separator + "y"
//					+ File.separator + smallDBFiles2[x], textData);
//
//			int nullcount = 0;
//			// ASSUMES A 1 TO 1 EXISTENCE OF NAS AND NY FILES - TRUE SO FAR
//			for (int j = 0; j < tickersForDatabase.size(); j++) {
//				String ticker = tickersForDatabase.get(j);
//				String textdata = textData.get(ticker);
//
//				final int[] sizes = { 2, 36, 44, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
//						1, 1, 1 };
//
//				if (textdata != null) {
//
//					float[] rawData = DataUtil.validSmallDataSet(textdata,
//							sizes);
//					if (rawData.length != 175)
//						continue;
//					
//					fillFundamentalData(data.get(ticker), rawData, isOneYearAgo);
//					nancount += badDataCount(data.get(ticker));
//				} else {
//					System.out.println(++nullcount + "   NULL TEXT");
//				}
//
//			}
//		}
//	}
//
//	private int badDataCount(TreeMap<String, Float> fs) {
//		int badcount = 0;
//		for (float f : fs.values()) {
//			if (f != f)
//				badcount++;
//		}
//		return badcount;
//	}
//
//	private void fillFundamentalData( TreeMap<String, Float> values, float[] rawData,
//			boolean isOlderThan1Year) {
//		for (int k = 0; k < 82; k++) {
//			// k==1 cnn analyst 1 year forecast
//			if (k == 1 && isOlderThan1Year)
//				values.put(labels[k],betterOf(values.get(labels[k]), rawData[k]));
//			else if (k != 1)
//				values.put(labels[k], betterOf(values.get(labels[k]), rawData[k]));
//		}
//		values.put(labels[82], betterOf(values.get(labels[82]), rawData[172]));
//		values.put(labels[83], betterOf(values.get(labels[83]), rawData[173]));
//		values.put(labels[84], betterOf(values.get(labels[84]), rawData[174]));
//	}
//
//	private float betterOf(float originally, float newdata) {
//		// if first is no good AND second either NaN
//		if ((originally == -1e-7 || originally != originally)
//				&& (newdata == -1e-7 || newdata != newdata))
//			return Float.NaN;
//		// if second is not either -1e-7 or NaN use it
//		if (!(newdata == -1e-7 || newdata != newdata))
//			return newdata;
//		else
//			return originally;
//
//	}


	public String root = "no path is set";

	public static TreeMap<String, TreeMap<String, Float>> data = new TreeMap<String, TreeMap<String, Float>>();

}
