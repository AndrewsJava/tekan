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
			// // the background thread watches the output from the process
			// new Thread(new Runnable() {
			// public void run() {
			// try {
			// BufferedReader reader =
			// new BufferedReader(new InputStreamReader(is));
			// String line;
			// while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			// }
			// } catch (IOException e) {
			// e.printStackTrace();
			// } finally {
			// is.close();
			// }
			// }
			// }).start();
			// // the outer thread waits for the process to finish
			// process.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
