package harlequinmettle.finance.technicalanalysis.applications;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.DividendDatabase;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.finance.technicalanalysis.tickertech.TickerTechView;
import harlequinmettle.finance.technicalanalysis.util.DefaultRangeSettingItemListener;
import harlequinmettle.finance.technicalanalysis.util.DividendForecaster;
import harlequinmettle.finance.technicalanalysis.view.FilePathButtonsScrollingPanel;
import harlequinmettle.finance.technicalanalysis.view.InfoPanel;
import harlequinmettle.finance.technicalanalysis.view.SQLitePaneledPane;
import harlequinmettle.finance.technicalanalysis.view.TickerButtonsScrollingPanel;
import harlequinmettle.utils.filetools.ChooseFilePrompterPathSaved;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.finance.ETFs;
import harlequinmettle.utils.finance.updatedtickerset.CurrentSymbolsDatabase;
import harlequinmettle.utils.guitools.FilterPanel;
import harlequinmettle.utils.guitools.HorizontalJPanel;
import harlequinmettle.utils.guitools.JButtonWithEnterKeyAction;
import harlequinmettle.utils.guitools.JLabelFactory;
import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.JSearchPanel;
import harlequinmettle.utils.systemtools.SystemMemoryUseDisplay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TechnicalDatabaseViewer extends JTabbedPane {

	public static final CurrentSymbolsDatabase TICKERSDB = new CurrentSymbolsDatabase(new SerializationTool());
	public static final ArrayList<String> TICKERS = new ArrayList<String>(TICKERSDB.tickers.values());
	
	public static DividendDatabase DDB = new DividendDatabase();
	public static TechnicalDatabaseSQLite TDB = new TechnicalDatabaseSQLite();
	public static CurrentFundamentalsSQLiteDatabase FDB = new CurrentFundamentalsSQLiteDatabase();

	public static final ArrayList<JCheckBox> PREFS = new ArrayList<JCheckBox>();
	public static TreeMap<String, Boolean> displayPreferences = new TreeMap<String, Boolean>();
	static {
		displayPreferences = SerializationTool.deserializeObject(displayPreferences.getClass(),
				"display_fundamental_treemap_serialization_key");
		if (displayPreferences == null)
			displayPreferences = new TreeMap<String, Boolean>();
	}

	// public static final TechnicalDatabaseSQLite TDB = new
	// TechnicalDatabaseSQLite(2000);

	public static void main(String[] arg) {
		long time = System.currentTimeMillis();
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();
		System.out.println("TOTAL TIME TO LOAD ALL: " + (System.currentTimeMillis() - time) / 1000 + " sec");
		System.out.println("MEMORY USED : " + (Runtime.getRuntime().totalMemory() / 1000000) + "   MB");
	}

	public TechnicalDatabaseViewer() {
		init();
	}

	private void init() {

		SystemMemoryUseDisplay smu = new SystemMemoryUseDisplay();
		// TechnicalDatabase db = new TechnicalDatabase(2, 0);

		// new ArrayList<String>(
		// TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA
		// .keySet())

		final FilterPanel filter_one = new FilterPanel(FDB.subsetLabels);
		final FilterPanel filter_two = new FilterPanel(FDB.subsetLabels);
		final FilterPanel filter_three = new FilterPanel(FDB.subsetLabels);
		final FilterPanel filter_four = new FilterPanel(FDB.subsetLabels);
		final FilterPanel filter_five = new FilterPanel(FDB.subsetLabels);
		final FilterPanel[] fundamentalIndicatorfilters = { filter_one, filter_two, filter_three, filter_four, filter_five };
		JButtonWithEnterKeyAction submit = new JButtonWithEnterKeyAction("apply filters for results");

		submit.addActionListener(doFilterListener(FDB, fundamentalIndicatorfilters));

		JFrame container = new JFrame("Control Panel - Technical Analysis");


		JScrollPanelledPane controls = new JScrollPanelledPane();
		this.add("controls", controls);

		JScrollPanelledPane rebuildSQLite = new SQLitePaneledPane();
		this.add("rebuild SQLite", rebuildSQLite);
		
		JScrollPanelledPane technicalAnalysis = new TechnicalAnalysisPaneledPane();
		this.add("technical analysis", technicalAnalysis);

		// JScrollPanelledPane settingsManager = new SettingsManagementPane();
		// this.add("manage settings", settingsManager);

		JSearchPanel searchPanel = new JSearchPanel(45);
		searchPanel.addSearchAction(doSearchActionListener(searchPanel));

		controls.addComp(searchPanel);

		controls.addComp(makeUpcomingDividendsPanel());

		controls.addComp(makeFreeTradeETFPanel());

		controls.addComp(makeRecentReportsPanel());
		InfoPanel info = new InfoPanel(FDB.subsetLabels);
		info.choices.addItemListener(new DefaultRangeSettingItemListener(info));
		info.setFontSize(40);
		controls.addComp(info);

		for (FilterPanel fp : fundamentalIndicatorfilters) {
			fp.setFontSize(40);
			controls.addComp(fp);
		}
		controls.addComp(submit);

		controls.addComp(JLabelFactory.doLightBluishJLabel("group result indicator display preferences"));
		for (String label : FDB.subsetLabels) {
			JCheckBox box = new JCheckBox(label);
			if (displayPreferences.containsKey(label))
				box.setSelected(displayPreferences.get(label));
			box.addItemListener(makePreferencesItemListener());
			PREFS.add(box);
			controls.addComp(box);
		}
		     
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container.setSize(900, 500);
		container.add(this);
		container.setVisible(true);
		
	}

	private ItemListener makePreferencesItemListener() {
		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {

				if (arg0.getStateChange() == ItemEvent.SELECTED || arg0.getStateChange() == ItemEvent.DESELECTED) {
					JCheckBox source = ((JCheckBox) arg0.getSource());
					String sourceText = source.getText();
					displayPreferences.put(sourceText, source.isSelected());
 
					SerializationTool.serializeObject(displayPreferences, "display_fundamental_treemap_serialization_key");
				}
			}

		};
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
				ChooseFilePrompterPathSaved downloads = new ChooseFilePrompterPathSaved("application_settings", pathToObj);
				String root = downloads.getSetting(key);

				new FilePathButtonsScrollingPanel((root));

			}

		};
	}

	private JComponent makeUpcomingDividendsPanel() {
		HorizontalJPanel tickerPanel = new HorizontalJPanel();

		JButton tickerTechOpener = new JButton("with upcoming dividnes");
		tickerPanel.add(tickerTechOpener);
		tickerTechOpener.addActionListener(makeUpcomingDividendsActionListener());
		return tickerPanel;
	}

	private ActionListener makeUpcomingDividendsActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TickerButtonsScrollingPanel(new DividendForecaster().scannForUpcommingDividends(), "ex div date soon");

			}

		};
	}

	private JComponent makeFreeTradeETFPanel() {
		HorizontalJPanel tickerPanel = new HorizontalJPanel();

		JButton tickerTechOpener = new JButton("Free Trade ETFs");
		tickerPanel.add(tickerTechOpener);
		tickerTechOpener.addActionListener(makeFreeTradesOpenerActionListener());
		return tickerPanel;
	}

	private ActionListener makeFreeTradesOpenerActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TickerButtonsScrollingPanel(Arrays.asList(ETFs.fidelityFreeTradeETFS), "fidelity etfs");

			}

		};
	}

	private ActionListener doFilterListener(final CurrentFundamentalsSQLiteDatabase fdb, final FilterPanel[] filters) {

		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreeMap<String, String> filterResults = fdb.getFilterResults(filters);
				String title = "";
				for (FilterPanel fp : filters) {
					if (fp.shouldFilterBeApplied())
						title += " " + fp.getFilterName();
				}

				new TickerButtonsScrollingPanel(filterResults, "Filter(s): " + title);
			}

		};
	}

	private ActionListener doSearchActionListener(final JSearchPanel searchPanel) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// getParent()
				String text_from_search_panel_text_box = searchPanel.getSearchText();

				new TickerTechView(text_from_search_panel_text_box.trim().toUpperCase());
			}

		};
	}

}
