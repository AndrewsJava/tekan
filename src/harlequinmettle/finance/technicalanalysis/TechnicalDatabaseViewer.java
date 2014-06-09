package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.PreferredJScrollPane;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TechnicalDatabaseViewer extends JTabbedPane{

	TechnicalDatabase db = new TechnicalDatabase();
	
	public static void main(String[] arg){
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();
	
	}
	
	public TechnicalDatabaseViewer(){
		JFrame container = new JFrame();
		container.setSize(800,500);
		container.add(this);
		container.setVisible(true);
		JScrollPanelledPane controls = new JScrollPanelledPane();
		this.add("controls",controls);
		JScrollPanelledPane chart = new JScrollPanelledPane();
		TickerTechView tv = new TickerTechView();
		PreferredJScrollPane tickerTechScroll = new PreferredJScrollPane(tv);
		 chart.addComp(tickerTechScroll);
		this.add("chart",chart);
	}
}
