package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.utils.numbertools.math.statistics.StatInfo;
import harlequinmettle.utils.systemtools.SystemMemoryUsage;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class TestTechnicalDatabaseData {

	public static void main(String[] args) {
		SystemMemoryUsage smu = new SystemMemoryUsage();
		TechnicalDatabase techDatabase = new TechnicalDatabase( );
		
		testData(techDatabase);
	}

	private static void testData(TechnicalDatabase techDatabase) {

		// <ticker, [day][technical data]>
		TreeMap<String, float[][]> DB = techDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA  ;
		for(int i = 0; i<7; i++){
		ArrayList<Float> statArray = new ArrayList<Float>();
		for(Entry<String,float[][]> ent: DB.entrySet()){
			float[][] individualTrackRecord = ent.getValue();
			for(float[] dayData: individualTrackRecord){
				if(dayData!=null){
					statArray.add(dayData[i]);
				}
			}
		}
		new StatInfo(statArray);
		new Scanner(System.in).nextLine();
		}
	}

}
