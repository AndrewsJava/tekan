package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.finance.technicalanalysis.applications.TechnicalDatabaseViewer;
import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.finance.technicalanalysis.sqlitedatabasebuilders.TechnicalDBSQLiteBlobsBuilder;
import harlequinmettle.finance.technicalanalysis.util.CollectTechnicalData;
import harlequinmettle.utils.guitools.JScrollPanelledPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

public class SQLitePaneledPane extends JScrollPanelledPane {

	JTextField progress = new JTextField("not started");
	
	public SQLitePaneledPane(){
		init();
	}
	
	private void init(){
		addComp(progress);
		JButton b = new JButton("collect technical data");
		b.addActionListener(generateCollectTechnicalActionListener());
		addComp(b);
		JButton c = new JButton("construct SQLite database");
		c.addActionListener(generateSQLiteContructorActionListener());
		addComp(c);
		
	}

	private ActionListener generateSQLiteContructorActionListener() {
		return new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				long time = System.currentTimeMillis();
				final TechnicalDBSQLiteBlobsBuilder buildDB = new TechnicalDBSQLiteBlobsBuilder();
				new Thread(){
				@Override
				public void run(){
					buildDB.buildDB();
					TechnicalDatabaseViewer.TDB = new TechnicalDatabaseSQLite();
				}}.start();
				System.out.println("--time: " + (System.currentTimeMillis() - time)
						/ 1000);
				
			}
			
		};
	}

	private ActionListener generateCollectTechnicalActionListener() {
	return new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			CollectTechnicalData collector = new CollectTechnicalData();
			collector.setProgressTextArea(progress); 
			collector.collectTechnicalData(TechnicalDatabaseViewer.TICKERS);
			
		}
		
	};
	}
}
