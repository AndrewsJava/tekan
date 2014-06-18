package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.utils.guitools.HorizontalJPanel;
import harlequinmettle.utils.guitools.JScrollPanelledPane;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;

public class FilePathButtonsScrollingPanel extends TickerButtonsScrollingPanel {

	public FilePathButtonsScrollingPanel( String root) {
		JFrame display = init("files");
		display.add(makeScrollingFileButtonList(root));
	}

	private Component makeScrollingFileButtonList(String root) {

		JScrollPanelledPane scrollForButtons = new JScrollPanelledPane();
		 File[] files =new File(root).listFiles();
		 Arrays.sort(files);
		for (File f: files) {
			String fname = f.getName();
			if(fname.contains("EARNINGS") || fname.contains("DIVIDENDS") )
				scrollForButtons.addComp(makeFileReadButton(f));
		}

		return scrollForButtons;
	}

	private JComponent makeFileReadButton(File f) {
		HorizontalJPanel filePanel = new HorizontalJPanel();

		JButton fileOpener = new JButton(f.getName());
		filePanel.add(fileOpener);
		fileOpener
				.addActionListener(makeFileReaderActionListener(f));
		return filePanel;
	}

	private ActionListener makeFileReaderActionListener(final File f) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
 

				TreeMap<String, String> results = new TreeMap<String, String>();
					try {
						String html = FileUtils.readFileToString(f);
						String ticker = html.substring(html.indexOf("[")+1,html.indexOf("]"));
						String[] tickers = ticker.split(",");
					    for(String t: tickers){
					    	results.put(t.trim(), t.trim());
					    }
					} catch (IOException e) { 
						e.printStackTrace();
					}
					

					new TickerButtonsScrollingPanel(  results);
				 

			}

		};
	}

}
