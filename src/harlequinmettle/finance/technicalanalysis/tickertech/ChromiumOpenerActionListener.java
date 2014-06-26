package harlequinmettle.finance.technicalanalysis.tickertech;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChromiumOpenerActionListener implements ActionListener {
private String ticker;
public ChromiumOpenerActionListener(String ticker){
	this.ticker = ticker;
}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			String page = "http://finance.yahoo.com/echarts?s="+ticker;
			ProcessBuilder pb = new ProcessBuilder("chromium", page);
			Process process = pb.start();
	 
			// process.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
