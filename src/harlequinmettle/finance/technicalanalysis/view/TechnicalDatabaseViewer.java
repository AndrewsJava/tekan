package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.finance.technicalanalysis.datatest.DividendForecaster;
import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabase;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.finance.ETFs;
import harlequinmettle.utils.guitools.FilterPanel;
import harlequinmettle.utils.guitools.HorizontalJPanel;
import harlequinmettle.utils.guitools.JButtonWithEnterKeyAction;
import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.JSearchPanel;
import harlequinmettle.utils.systemtools.SystemMemoryUseDisplay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.apache.commons.io.FileUtils;

public class TechnicalDatabaseViewer extends JTabbedPane {

	public static final DividendDatabase ddb = new DividendDatabase();

	public static void main(String[] arg) {
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();
	}

	public TechnicalDatabaseViewer() {
		init();
	}

	private void init() {

		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		TechnicalDatabase db = new TechnicalDatabase(2, 0);
		CurrentFundamentalsDatabase fdb = new CurrentFundamentalsDatabase();

		// new ArrayList<String>(
		// TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA
		// .keySet())

		final FilterPanel filter_one = new FilterPanel(fdb.forDisplaying);
		final FilterPanel filter_two = new FilterPanel(fdb.forDisplaying);
		final FilterPanel filter_three = new FilterPanel(fdb.forDisplaying);
		final FilterPanel[] filters = { filter_one, filter_two, filter_three };
		JButtonWithEnterKeyAction submit = new JButtonWithEnterKeyAction(
				"apply filters for results");

		submit.addActionListener(doFilterListener(fdb, filters));
		JFrame container = new JFrame();
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container.setSize(800, 500);
		container.add(this);
		container.setVisible(true);

		JScrollPanelledPane controls = new JScrollPanelledPane();
		this.add("controls", controls);

		JSearchPanel searchPanel = new JSearchPanel();
		searchPanel.addSearchAction(doSearchActionListener(searchPanel));
		
		controls.addComp(searchPanel);

		controls.addComp(makeUpcomingDividendsPanel());

		controls.addComp(makeFreeTradeETFPanel());

		controls.addComp(makeRecentReportsPanel());

		for (FilterPanel fp : filters) {
			controls.addComp(fp);
		}
		controls.addComp(submit);
	}

	private JComponent makeRecentReportsPanel() {
		HorizontalJPanel tickerPanel = new HorizontalJPanel();

		JButton tickerTechOpener = new JButton("Recent Reports (in Downloads)");
		tickerPanel.add(tickerTechOpener);
		tickerTechOpener.addActionListener(makeRecentReportsActionListener());
		return tickerPanel;
	}

	private ActionListener makeRecentReportsActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String pathToObj = "technical_database_settings";
				String key = "path to downloads folder";
				ChooseFilePrompterPathSaved downloads = new ChooseFilePrompterPathSaved(
						pathToObj);
				String root = downloads.getSetting(key);

				new FilePathButtonsScrollingPanel((root));

			}

		};
	}

	private JComponent makeUpcomingDividendsPanel() {
		HorizontalJPanel tickerPanel = new HorizontalJPanel();

		JButton tickerTechOpener = new JButton("with upcoming dividnes");
		tickerPanel.add(tickerTechOpener);
		tickerTechOpener
				.addActionListener(makeUpcomingDividendsActionListener());
		return tickerPanel;
	}

	private ActionListener makeUpcomingDividendsActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TickerButtonsScrollingPanel(
						new DividendForecaster().scannForUpcommingDividends());

			}

		};
	}

	private JComponent makeFreeTradeETFPanel() {
		HorizontalJPanel tickerPanel = new HorizontalJPanel();

		JButton tickerTechOpener = new JButton("Free Trade ETFs");
		tickerPanel.add(tickerTechOpener);
		tickerTechOpener
				.addActionListener(makeFreeTradesOpenerActionListener());
		return tickerPanel;
	}

	private ActionListener makeFreeTradesOpenerActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TickerButtonsScrollingPanel(
						Arrays.asList(ETFs.fidelityFreeTradeETFS));

			}

		};
	}

	private ActionListener doFilterListener(
			final CurrentFundamentalsDatabase fdb, final FilterPanel[] filters) {
		// public TreeMap<String, String> getFilterResults(FilterPanel[]
		// searchFilters) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreeMap<String, String> filterResults = fdb
						.getFilterResults(filters);
				for (Entry<String, String> ent : filterResults.entrySet()) {
					System.out.println(ent);
				}
				new TickerButtonsScrollingPanel(filterResults);
			}

		};
	}

	private ActionListener doSearchActionListener(final JSearchPanel searchPanel) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// getParent()
				String text_from_search_panel_text_box = searchPanel
						.getSearchText();

				new TickerTechView(text_from_search_panel_text_box.trim()
						.toUpperCase());
			}

		};
	}

}
