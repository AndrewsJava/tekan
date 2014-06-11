package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.utils.guitools.FilterPanel;
import harlequinmettle.utils.guitools.JButtonWithEnterKeyAction;
import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.JSearchPanel;
import harlequinmettle.utils.guitools.PreferredJScrollPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TechnicalDatabaseViewer extends JTabbedPane {


	public static void main(String[] arg) {
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();
	}

	public TechnicalDatabaseViewer() {
		init();

	}

	private void init() {

		TechnicalDatabase db = new TechnicalDatabase();
		CurrentFundamentalsDatabase fdb = new CurrentFundamentalsDatabase();

		final FilterPanel filter_one = new FilterPanel(fdb.labels);
		final FilterPanel filter_two = new FilterPanel(fdb.labels);
		final FilterPanel filter_three = new FilterPanel(fdb.labels);
		final FilterPanel[] filters = { filter_one, filter_two, filter_three };
		JButtonWithEnterKeyAction submit = new JButtonWithEnterKeyAction(
				"apply filters for results");
		 
		submit.addActionListener(doFilterListener(fdb,filters));
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
		for (FilterPanel fp : filters) {
			controls.addComp(fp);
		}
		controls.addComp(submit);
	}

	private ActionListener doFilterListener(final CurrentFundamentalsDatabase fdb,
			final FilterPanel[] filters) { 
	//	public TreeMap<String, String> getFilterResults(FilterPanel[] searchFilters) {
			return new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent arg0) {
					TreeMap<String, String> filterResults = fdb.getFilterResults(filters);
					for(Entry<String,String> ent: filterResults.entrySet()){
						System.out.println(ent);
					}
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
				showChartInNewWindow(text_from_search_panel_text_box.trim()
						.toUpperCase());
			}

		};
	}

	private void showChartInNewWindow(String ticker) {
		final JFrame container = new JFrame(ticker + "   "
				+ TechnicalDatabase.NUM_DAYS + "  days");

		container.setSize(900, 550);
		container.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		container.setVisible(true);
		JScrollPanelledPane chart = new JScrollPanelledPane();
		TickerTechView tv = new TickerTechView(ticker);
		PreferredJScrollPane tickerTechScroll = new PreferredJScrollPane(tv);
		tv.setScrollBar(tickerTechScroll.getViewport());

		chart.addComp(tickerTechScroll);

		container.add(chart);
		container.setExtendedState(container.getExtendedState()
				| JFrame.MAXIMIZED_BOTH);
		final ComponentListener refForRemoval = doWindowRescaleListener(tv);
		container.addComponentListener(refForRemoval);
		container.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				container.removeComponentListener(refForRemoval);
			}
		});
	}

	private ComponentListener doWindowRescaleListener(final TickerTechView tv) {
		return new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent arg0) {

				tv.rescaleCanvas(arg0.getComponent().getBounds().getSize());

			}
		};
	}
}
