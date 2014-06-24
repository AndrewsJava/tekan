package harlequinmettle.finance.technicalanalysis.tickertech;

import java.awt.Color;
import java.io.Serializable;

 public class OptionsMenuModel implements Serializable {
	private static final long serialVersionUID = -1852215164270900131L;

	public int indexMeasureId;
	public int numberToUseInAvg;
	public boolean useTrailing;
	public boolean useCompression;
	public boolean show;
	public Color color;

	OptionsMenuModel() {
		color = generateColor();
	}

	public Color generateColor() {

		int base = 35;
		int base2 = 155;
		int red = base;
		int green = base;
		int blue = base;
		if (Math.random() > 0.5)
			red = base2 + (int) (Math.random() * (255 - base - base2));
		if (Math.random() > 0.5)
			green = base2 + (int) (Math.random() * (255 - base - base2));
		if (Math.random() > 0.5)
			blue = base2 + (int) (Math.random() * (255 - base - base2));
		return new Color(red, green, blue);
	}

	public void print() {

//		public int indexMeasureId;
//		public int numberToUseInAvg;
//		public boolean useTrailing;
//		public boolean useCompression;
//		public boolean show;
//		public Color color;
		System.out.println();
		System.out.println(" show:                               "+show);
		System.out.println(" numberToUseInAvg:   "+numberToUseInAvg);
		System.out.println(" indexMeasureId:           "+indexMeasureId);
		System.out.println(" useCompression:            "+useCompression);
		System.out.println(" useTrailing:                        "+useTrailing);
		
	}
}

