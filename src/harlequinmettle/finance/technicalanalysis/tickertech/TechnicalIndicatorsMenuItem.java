package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.utils.numbertools.math.statistics.BasicCalculations;
import harlequinmettle.utils.numbertools.math.statistics.StatInfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JMenu;

public class TechnicalIndicatorsMenuItem extends JMenu {
	TickerTechModel model;

	public TechnicalIndicatorsMenuItem(TickerTechModel model) {
		super("[technical indicators]");
		this.model = model;
		init();
	}

	private void init() {
		// Date Open High Low Close Volume AdjClose*
		setMnemonic(KeyEvent.VK_T);
		JButton dailyPriceFlux = new JButton("show open to close fluctuation statistics");
		dailyPriceFlux.addActionListener(makeDailyPriceScanListener(TechnicalDatabaseSQLite.OPEN, TechnicalDatabaseSQLite.CLOSE));
		add(dailyPriceFlux);

		JButton dailyRangeLowToHigh = new JButton("show low to high fluctuation statistics");
		dailyRangeLowToHigh.addActionListener(makeDailyPriceScanListener(TechnicalDatabaseSQLite.LOW, TechnicalDatabaseSQLite.HIGH));
		add(dailyRangeLowToHigh);
		
		JButton priceToPrice = new JButton("show prices to all price fluctuation statistics");
		priceToPrice.addActionListener(makePriceToPriceScanListener( TechnicalDatabaseSQLite.CLOSE));
		add(priceToPrice);
		
	}

	private ActionListener makePriceToPriceScanListener(  final int id) {
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				scanPriceToPrice(id);
			}

			private void scanPriceToPrice(int id) {
				ArrayList<Float> totalChanges = new ArrayList<Float>();
				for (Entry<Float, float[]> ent : model.technicalData.entrySet()) {
					float[] dayData = ent.getValue();
					for (Entry<Float, float[]> ent2 : model.technicalData.entrySet()) {
						float[] otherDayData = ent2.getValue();
					// Date Open High Low Close Volume AdjClose*
					float dailyRange = BasicCalculations.calculatePercentChange(dayData[id], otherDayData[id]);
					totalChanges.add(dailyRange);
					
					}
				}
				StatInfo changStats = new StatInfo(totalChanges);
				System.out.println(changStats);
		 
			}
		};
	}

	private ActionListener makeDailyPriceScanListener(final int start, final int end) {
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				scanDailyPriceData(start, end);
			}
			protected void scanDailyPriceData(final int start, final int end) {
				ArrayList<Float> totalChanges = new ArrayList<Float>();
				for (Entry<Float, float[]> ent : model.technicalData.entrySet()) {
					float[] dayData = ent.getValue();
					// Date Open High Low Close Volume AdjClose*
					float dailyRange = BasicCalculations.calculatePercentChange(dayData[start], dayData[end]);
					totalChanges.add(dailyRange);
				}
				StatInfo changStats = new StatInfo(totalChanges);
				System.out.println(changStats);
			}
		};
	}


}
