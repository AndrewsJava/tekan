package harlequinmettle.finance.technicalanalysis.model;

import harlequinmettle.utils.numbertools.math.statistics.StatInfo;
import harlequinmettle.utils.systemtools.SystemMemoryUseDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class TestTechnicalDatabaseData {

	public static void main(String[] args) {
		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		TechnicalDatabase techDatabase = new TechnicalDatabase(5);

		testData(techDatabase);
	}

	private static void testData(TechnicalDatabase techDatabase) {

		// <ticker, [day][technical data]>
		TreeMap<String, float[][]> DB = techDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA;
		ArrayList<Float> emptyArrayStat = new ArrayList<Float>();
		ArrayList<Float> statArray = new ArrayList<Float>();
		short emptyArrayCounter = 0;
		int validArraysCounterTotal = 0;
		int emptyArraysCounterTotal = 0;
		for (int i = 0; i < 7; i++) {
			statArray.clear();
			for (Entry<String, float[][]> ent : DB.entrySet()) {
				String ticker = ent.getKey();
				float[][] individualTrackRecord = ent.getValue();
				emptyArraysCounterTotal+=emptyArrayCounter;
				emptyArrayCounter = 0;
				for (float[] dayData : individualTrackRecord) {

					if (dayData != null) {
						// System.out.println(Arrays.toString(dayData));
						validArraysCounterTotal++;
						statArray.add(dayData[i]);
						if (Math.random()<0.001){ 

							System.out.println(i+" : "+dayData[i]);
						}
					} else {
						// System.out.println(emptyArrayCounter+"  null");
						emptyArrayCounter++;
					}
				}
				if (i == 0){
					emptyArrayStat.add((float) emptyArrayCounter);

				}
			}
		if (i == 1) {
				StatInfo nullStats = new StatInfo(emptyArrayStat);
				System.out.println(emptyArraysCounterTotal/1000000+" M  empty , valid:  "+validArraysCounterTotal/1000000 +"  M");
				System.out.println(emptyArrayStat);
				System.out.println(nullStats);
				new Scanner(System.in).nextLine();
			}// System.out.println("empty arrays: " + emptyArrayCounter);
			StatInfo dataStats = new StatInfo(statArray);
			System.out.println(dataStats);
			new Scanner(System.in).nextLine();
		}
	}

}
