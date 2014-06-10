package harlequinmettle.finance.technicalanalysis;

import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.PreferredJScrollPane;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TechnicalDatabaseViewer extends JTabbedPane{

	TechnicalDatabase db = new TechnicalDatabase();
	
	public static void main(String[] arg){
		TechnicalDatabaseViewer tdbviewer = new TechnicalDatabaseViewer();
	
	}
	
	public TechnicalDatabaseViewer(){
		JFrame container = new JFrame();
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container.setSize(800,500);
		container.add(this);
		container.setVisible(true);
		JScrollPanelledPane controls = new JScrollPanelledPane();
		this.add("controls",controls);
	showChartInNewWindow();
	}

	private void showChartInNewWindow() {
		final JFrame container = new JFrame();		
		 
		container.setSize(900, 550);
		container.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		container.setVisible(true);
		JScrollPanelledPane chart = new JScrollPanelledPane();
		TickerTechView tv = new TickerTechView();
		PreferredJScrollPane tickerTechScroll = new PreferredJScrollPane(tv);
		 chart.addComp(tickerTechScroll);
		 
		container.add(chart );
		container.setExtendedState(container.getExtendedState()
				| JFrame.MAXIMIZED_BOTH);
		final ComponentListener refForRemoval =  doWindowRescaleListener(tv);
		container.addComponentListener(refForRemoval);
		container.addWindowListener(new WindowAdapter(){
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
