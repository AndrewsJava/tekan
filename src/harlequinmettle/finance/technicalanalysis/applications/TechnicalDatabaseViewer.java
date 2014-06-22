package harlequinmettle.finance.technicalanalysis.applications;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.finance.technicalanalysis.tickertech.TickerTechView;
import harlequinmettle.finance.technicalanalysis.util.DividendForecaster;
import harlequinmettle.finance.technicalanalysis.view.FilePathButtonsScrollingPanel;
import harlequinmettle.finance.technicalanalysis.view.TickerButtonsScrollingPanel;
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
import java.util.Arrays;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TechnicalDatabaseViewer extends JTabbedPane {

	public static final DividendDatabase DDB = new DividendDatabase();
	public static final TechnicalDatabaseSQLite TDB = new TechnicalDatabaseSQLite();
	//public static final TechnicalDatabaseSQLite TDB = new TechnicalDatabaseSQLite(2000);

	public static void main(String[] arg) {
		long time = System.currentTimeMillis();
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();
		System.out.println("TOTAL TIME TO LOAD ALL: "
				+ (System.currentTimeMillis() - time) / 1000 + " sec");
		System.out.println("MEMORY USED : "
				+ (Runtime.getRuntime().totalMemory() / 1000000) + "   MB");
	}

	public TechnicalDatabaseViewer() {
		init();
	}

	private void init() {

		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		// TechnicalDatabase db = new TechnicalDatabase(2, 0); 
		CurrentFundamentalsSQLiteDatabase fdb = new CurrentFundamentalsSQLiteDatabase();

		// new ArrayList<String>(
		// TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA
		// .keySet())

		final FilterPanel filter_one = new FilterPanel(fdb.subsetLabels);
		final FilterPanel filter_two = new FilterPanel(fdb.subsetLabels);
		final FilterPanel filter_three = new FilterPanel(fdb.subsetLabels);
		final FilterPanel[] filters = { filter_one, filter_two, filter_three };
		JButtonWithEnterKeyAction submit = new JButtonWithEnterKeyAction(
				"apply filters for results");

		submit.addActionListener(doFilterListener(fdb, filters));
		JFrame container = new JFrame("Control Panel - Technical Analysis");
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container.setSize(900, 500);
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
				ChooseFilePrompterPathSaved downloads = new ChooseFilePrompterPathSaved("application_settings",
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
						new DividendForecaster().scannForUpcommingDividends(),"ex div date soon");

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
						Arrays.asList(ETFs.fidelityFreeTradeETFS), "fidelity etfs");

			}

		};
	}

//	private ActionListener doFilterListener(
//			final CurrentFundamentalsDatabase fdb, final FilterPanel[] filters) {
//		// public TreeMap<String, String> getFilterResults(FilterPanel[]
//		// searchFilters) {
//		return new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				TreeMap<String, String> filterResults = fdb
//						.getFilterResults(filters);
//				for (Entry<String, String> ent : filterResults.entrySet()) {
//					System.out.println(ent);
//				}
//				new TickerButtonsScrollingPanel(filterResults);
//			}
//
//		};
//	}

	private ActionListener doFilterListener(
			final CurrentFundamentalsSQLiteDatabase fdb, final FilterPanel[] filters) {
		// public TreeMap<String, String> getFilterResults(FilterPanel[]
		// searchFilters) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreeMap<String, String> filterResults = fdb
						.getFilterResults(filters);
				String title ="";
				for(FilterPanel fp : filters){
					if(fp.shouldFilterBeApplied())
					title+=" "+fp.getFilterName();
				}
//				for (Entry<String, String> ent : filterResults.entrySet()) { 
//					System.out.println(ent);
//				}
				new TickerButtonsScrollingPanel(filterResults,"Filter(s): "+title);
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
