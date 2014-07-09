package harlequinmettle.finance.technicalanalysis.applications;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.finance.technicalanalysis.view.TickerButtonsScrollingPanel;
import harlequinmettle.utils.guitools.FilterPanel;
import harlequinmettle.utils.guitools.JLabelFactory;
import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.numbertools.math.statistics.BasicCalculations;
import harlequinmettle.utils.numbertools.math.statistics.StatInfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class TechnicalAnalysisPaneledPane extends JScrollPanelledPane {

	public TechnicalAnalysisPaneledPane() {
		init();
	}
String[] technicalFilterOptions = {"stdev daily price change"};
	private void init() {
		addComp(JLabelFactory.doBluishJLabel("Technical Filters"));
		final FilterPanel technical_filter_one = new FilterPanel(technicalFilterOptions);
		final FilterPanel[] technicalIndicatorfilters = { technical_filter_one };
		for (FilterPanel fp : technicalIndicatorfilters) {
			fp.setFontSize(40);
			addComp(fp);
		}
		JButton t = new JButton("filter technicals");
		t.addActionListener(doTechnicalFilterListener(TechnicalDatabaseViewer.TDB, technicalIndicatorfilters));
		addComp(t);
		
		final FilterPanel filter_one = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_two = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_three = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_four = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_five = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel[] fundamentalIndicatorfilters = { filter_one, filter_two, filter_three, filter_four, filter_five };
		addComp(JLabelFactory.doBluishJLabel("Fundamentals Filters"));
		for (FilterPanel fp : fundamentalIndicatorfilters) {
			fp.setFontSize(40);
			addComp(fp);
		}
		JButton b = new JButton("filter fundamentals and analyze");
		b.addActionListener(doFilterAndAnalyzeListener(TechnicalDatabaseViewer.FDB, fundamentalIndicatorfilters));
		addComp(b);

		// JButton c = new JButton("construct SQLite database");
		// c.addActionListener(generateSQLiteContructorActionListener());
		// addComp(c);

	}

	private ActionListener doTechnicalFilterListener(final TechnicalDatabaseSQLite tDB, final FilterPanel[] technicalIndicatorfilters) {

		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreeMap<String, Float> stdev =   analyzeResults( tDB.SQLITE_PER_TICKER_PER_DAY_TECHNICAL_DATA.keySet());
				  ArrayList<String> filtered = 	new ArrayList<String>();
				  for(Entry<String,Float> ent: stdev.entrySet()){
					  //TODO coordinate properly mulitple technical filters
					  //for now just the one metric daily price change
					  float lowStDev = technicalIndicatorfilters[0].getLow();
					float highStDev =   technicalIndicatorfilters[0].getHigh();
					float stdv = ent.getValue();
					if(stdv<=highStDev && stdv >= lowStDev){
						filtered.add(ent.getKey());
					}
				  }
				  new TickerButtonsScrollingPanel(filtered,technicalIndicatorfilters[0].getFilterName());
			} 
		};
	}

	private ActionListener doFilterAndAnalyzeListener(final CurrentFundamentalsSQLiteDatabase fdb, final FilterPanel[] filters) {

		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreeMap<String, String> filterResults = fdb.getFilterResults(filters);
				String title = "";
				for (FilterPanel fp : filters) {
					if (fp.shouldFilterBeApplied())
						title += " " + fp.getFilterName();
				}
				System.out.println(filterResults.size() + "    (:U)-H RESULTS");
				StatInfo results = new StatInfo(new ArrayList<Float>(analyzeResults(filterResults.keySet()).values()));
			}

		};
	}

	protected TreeMap<String,Float> analyzeResults(Collection<String> values) {
		int nullcount = 0;
		TreeMap<String,Float> standardDeviations = new TreeMap<String,Float>();
		for (String ticker : values) {
			float[][] techData = TechnicalDatabaseViewer.TDB.SQLITE_PER_TICKER_PER_DAY_TECHNICAL_DATA.get(ticker);
			if (techData == null) {
				System.out.println(nullcount++ + "     "+ ticker);
				continue;
			}
			ArrayList<Double> totalChanges = new ArrayList<Double>();
			for (float[] daydata : techData) {

				double dailyRange = BasicCalculations.calculatePercentChange(daydata[TechnicalDatabaseSQLite.OPEN],
						daydata[TechnicalDatabaseSQLite.CLOSE]);
				if (dailyRange == dailyRange && !Double.isInfinite(dailyRange))
					totalChanges.add(dailyRange);
			}
			double[] target = new double[totalChanges.size()];
			for (int i = 0; i < target.length; i++) {
				target[i] = totalChanges.get(i);
			}
			float standardDev = (float) new StandardDeviation().evaluate(target);
			standardDeviations.put(ticker,standardDev);
		}
		return  (standardDeviations );
	}

}
