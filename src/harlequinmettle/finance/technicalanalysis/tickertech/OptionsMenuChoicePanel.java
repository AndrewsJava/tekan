package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.utils.guitools.HorizontalJPanel;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class OptionsMenuChoicePanel extends HorizontalJPanel {
	public static final Integer[] DAYS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 30,
			60, 90, 60 * 2, 60 * 3, 60 * 4, 60 * 5, 60 * 6 };
	JComboBox<Integer> interval;
	JComboBox<String> measure;
	JCheckBox showHide = new JCheckBox();
	int numberToUseInAvg = DAYS[8];
	String measuring;
	TickerTechModel model;
	Color color;
	GeneralPath path;
	static int idGen = 0;
	int id = 0;
	int indexMeasureId = TechnicalDatabaseSQLite.VOLUME;

	GeneralPath avgsPath = new GeneralPath();

	public OptionsMenuChoicePanel(TickerTechModel model) {
		init();
		this.model = model;
	}

	public void init() { 
		color = generateColor();
		showHide.setBackground(color);
		interval = new JComboBox<Integer>(DAYS);
		// measure = new JComboBox<String>(model.preferenceOptionsWithOptions);
		measure = new JComboBox<String>(TechnicalDatabaseSQLite.elements);
		// showHide.addItemListener(makeIntervalChoiceItemListener());
		interval.addItemListener(makeIntervalChoiceItemListener());
		measure.addItemListener(makeIntervalChoiceItemListener());
		add(showHide);
		add(interval);
		add(measure);
		id = idGen++;
	}
private Color generateColor(){

	int base = 125;
	int base2 = 55;
	int red = base;
	int green = base;
	int  blue = base;
	if(Math.random()>0.5)
	  red = base2+ (int) (Math.random() * (255 - base-base2));
	if(Math.random()>0.5)
		  green = base2+ (int) (Math.random() * (255 - base-base2));
		if(Math.random()>0.5)
			  blue = base2+ (int) (Math.random() * (255 - base-base2));
	return new Color(red, green, blue);
}
	public boolean isDisplayPreferred() {
		return showHide.isSelected();
	}

	// // ///////////////////
	// TickerTechView.tickertechviewaccess.repaint();

	private ItemListener makeIntervalChoiceItemListener() {
		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {

					numberToUseInAvg = ((Integer) interval.getSelectedItem());
					// index coordinated with techncial data
					// [date,high,low.....]
					indexMeasureId = measure.getSelectedIndex();
					if (indexMeasureId == TechnicalDatabaseSQLite.VOLUME)
						path = model.generateAvgPath(indexMeasureId,
								model.minMaxVolume, numberToUseInAvg);
					else
						path = model.generateAvgPath(indexMeasureId,
								model.minMaxPrice, numberToUseInAvg);
					System.out.println("\n");
		 
//					for (PathIterator pi = path.getPathIterator(null); !pi
//							.isDone(); pi.next()) {
//						double[] coords = new double[6];
//						pi.currentSegment(coords);
//						System.out.println(Arrays.toString(coords));
//					}
					System.out.println("days to average    : 	"
							+ numberToUseInAvg);
					System.out.println("index to measure : 	"
							+ TechnicalDatabaseSQLite.elements[indexMeasureId]);
					System.out.println("\n");
					// TODO: SAVE TO PREFERENCES - SET CHOICE TO OPTION
					TickerTechView.tickertechviewaccess.repaint();
				}
			}

		};
	}
}
