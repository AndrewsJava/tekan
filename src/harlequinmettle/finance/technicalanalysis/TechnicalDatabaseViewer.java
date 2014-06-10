package harlequinmettle.finance.technicalanalysis;

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
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TechnicalDatabaseViewer extends JTabbedPane {

	TechnicalDatabase db = new TechnicalDatabase();
	CurrentFundamentalsDatabase fdb = new CurrentFundamentalsDatabase();
	
	public static void main(String[] arg) {
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();

	}

	public TechnicalDatabaseViewer() {
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
		for(int i = 0; i<20; i++)
			System.out.println(Arrays.toString(fdb.data[(int)(4000*Math.random())]));
	 
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
