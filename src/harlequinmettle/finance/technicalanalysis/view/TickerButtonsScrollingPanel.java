package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabase;
import harlequinmettle.utils.guitools.HorizontalJPanel;
import harlequinmettle.utils.guitools.JScrollPanelledPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class TickerButtonsScrollingPanel {

	public TickerButtonsScrollingPanel( ) { 
	}
	public TickerButtonsScrollingPanel(TreeMap<String, String> results,String title) {
		JFrame display = init(title);
		display.add(makeScrollingTickerButtonList(results));
	}

	protected JFrame init(String title) { 
		JFrame display = new JFrame(title);
		display.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		display.setSize(400, 800);
		display.setVisible(true);
		return display;
	}

	public TickerButtonsScrollingPanel(List<String> asList, String title) {
		JFrame display = init(title);
		display.add(makeScrollingTickerButtonList(asList));
	}

	private Component makeScrollingTickerButtonList(List<String> asList) {

		JScrollPanelledPane scrollForButtons = new JScrollPanelledPane();
		for (String ticker : asList) {
			if(TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA.containsKey(ticker.toUpperCase().replaceAll(".*\\W+.*", "")))
			scrollForButtons.addComp(makeTickerButton(ticker));
		} 
		return scrollForButtons;
	}

	private Component makeScrollingTickerButtonList(
			TreeMap<String, String> results) {

		JScrollPanelledPane scrollForButtons = new JScrollPanelledPane();
		for (Entry<String, String> ent : results.entrySet()) {
			scrollForButtons.addComp(makeTickerButton(ent.getKey(),
					ent.getValue()));
		}

		return scrollForButtons;
	}

	private JComponent makeTickerButton(String... buttonDetails) {
		HorizontalJPanel tickerPanel = new HorizontalJPanel();

		JButton tickerTechOpener = new JButton(Arrays.toString(buttonDetails));
		tickerPanel.add(tickerTechOpener);
		tickerTechOpener
				.addActionListener(makeTickerTechOpenerActionListener(buttonDetails[0]));
		return tickerPanel;
	}

	private ActionListener makeTickerTechOpenerActionListener(
			final String ticker) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JButton source = (JButton) arg0.getSource();
				source.setBackground(Color.blue);
			//	new TickerTechView(ticker.toUpperCase().replaceAll(".*\\W+.*", ""));
				new TickerTechView(ticker.toUpperCase().replaceAll("[^A-Z]", ""));
			}

		};
	}
}
