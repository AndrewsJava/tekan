package harlequinmettle.finance.technicalanalysis.applications;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.utils.guitools.FilterPanel;
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

	private void init() {
		final FilterPanel filter_one = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_two = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_three = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_four = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel filter_five = new FilterPanel(TechnicalDatabaseViewer.FDB.subsetLabels);
		final FilterPanel[] fundamentalIndicatorfilters = { filter_one, filter_two, filter_three, filter_four, filter_five };
		for (FilterPanel fp : fundamentalIndicatorfilters) {
			fp.setFontSize(40);
			addComp(fp);
		}
		JButton b = new JButton("filter and analyze");
		b.addActionListener(doFilterAndAnalyzeListener(TechnicalDatabaseViewer.FDB, fundamentalIndicatorfilters));
		addComp(b);

		// JButton c = new JButton("construct SQLite database");
		// c.addActionListener(generateSQLiteContructorActionListener());
		// addComp(c);

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
				StatInfo results = analyzeResults(filterResults.keySet());
			}

		};
	}

	protected StatInfo analyzeResults(Collection<String> values) {
		int nullcount = 0;
		ArrayList<Float> standardDeviations = new ArrayList<Float>();
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
			standardDeviations.add(standardDev);
		}
		return new StatInfo(standardDeviations);
	}

}
