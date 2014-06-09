package harlequinmettle.finance.technicalanalysis;

import java.awt.Font;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JPanel;

public class TickerTechView  extends JPanel {
	public static final int INTERBARMARGINS =2; 
	public static final int BAR_W =10; 
	public static final int margins =20; 
	public static int eW =  TechnicalDatabase.NUM_DAYS*(BAR_W +INTERBARMARGINS);  
	public static int W = 2*margins+eW; 
	public static int eH ;
	public static int H ;
	public static final int FONT_SIZE = 15;
	public static final Font BIG_FONT = new Font("Sans-Serif", Font.BOLD,
			FONT_SIZE);
	 
	private ArrayList<Rectangle2D.Float> volumeBars;
	private ArrayList<Rectangle2D.Float> openClose;
	private ArrayList<Boolean> openToCloseGains;
	private ArrayList<Line2D.Float> highLow;
	private Point2D.Float minMaxPrice;
	private Point2D.Float minMaxVolume;
	
	
	public TickerTechView(){
		init("BME");
	}
	
	public TickerTechView(String ticker){
		init(ticker);
	}

	private void init(String ticker) {
		TreeMap<Float,Float> high = TechnicalDatabase.makeGetDataToDateMap(ticker, TechnicalDatabase.HIGH);
		TreeMap<Float,Float> low  = TechnicalDatabase.makeGetDataToDateMap(ticker, TechnicalDatabase.LOW);
		minMaxPrice = calcMinMax(low,high);
		highLow = generateHighLowLines(high,low);
	}

	private java.awt.geom.Point2D.Float calcMinMax(TreeMap<Float, Float> low,
			TreeMap<Float, Float> high) {
		 float min = Float.MAX_VALUE;
		 float max = Float.MIN_VALUE;

		 for(float f: high.values()){
			 if(f>max)max = f;
		 }

		 for(float f: low.values()){
			 if(f<min)min= f;
		 }
		 
		 return new Point2D.Float(min,max);
		 
		 
	}

	private ArrayList<java.awt.geom.Line2D.Float> generateHighLowLines(
			TreeMap<Float, Float> high, TreeMap<Float, Float> low) {
		ArrayList<java.awt.geom.Line2D.Float> hl = new ArrayList<java.awt.geom.Line2D.Float>();
		
		return hl;
	}
}

