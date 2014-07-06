package harlequinmettle.finance.technicalanalysis.util;

import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.finance.updatedtickerset.CurrentSymbolsDatabase;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
//TODO: FINISH SWING PROGRESS BAR DEMO IMPLEMENTATION
public class CollectTechnicalWithTaskAndProgress implements PropertyChangeListener {
	public static final String TECHNICAL_DATA = "TECHNICAL_DATA";
	public static final String DIVIDENDS_SUBFOLDER = "/DIVIDENDS";
	String urlbase = "http://ichart.finance.yahoo.com/table.csv?s=";
	static String dividendSuffix = "&g=v";

	public static void main(String[] args) {
		 CurrentSymbolsDatabase TICKERSDB = new CurrentSymbolsDatabase(new SerializationTool());
		  ArrayList<String> TICKERS = new ArrayList<String>(TICKERSDB.tickers.values());
		  CollectTechnicalWithTaskAndProgress ctd = new CollectTechnicalWithTaskAndProgress();
		// daily price data
		ctd.collectTechnicalData( TICKERS, TECHNICAL_DATA, "");
		// daily price data
		ctd.collectTechnicalData(TICKERS, TECHNICAL_DATA + DIVIDENDS_SUBFOLDER, dividendSuffix);
		ctd.validateDataDownload(TICKERS, TECHNICAL_DATA);
		ctd.validateDataDownload(TICKERS, TECHNICAL_DATA + DIVIDENDS_SUBFOLDER);
	}

	public void validateDataDownload(ArrayList<String> tickers, String filePath) {
		boolean success = false;
		for (String ticker : tickers) {
			File collectedData = new File(filePath, ticker + ".csv");
			if (collectedData.exists()) {

				try {
					String data = FileUtils.readFileToString(new File(filePath, ticker + ".csv"));

					if ((data.startsWith("Date,Open,High,Low,Close,Volume,Adj Close") && (data.split(",").length > 13))//
							//
							|| (data.startsWith("Date,Dividends") && (data.split(",").length > 3)))
						success = true;
					else {
						success = false;
						new File(filePath, ticker + ".csv").delete();
					}
				} catch (IOException e) {
				}
			} else {
				success = false;
			}
			if (!success)
				System.out.println("data fail" + ticker);
		}

	}

	public void collectTechnicalData(final ArrayList<String> tickers, boolean showProgress) {
		Task task = new Task();
		ProgressMonitor progressMonitor = new ProgressMonitor(new JFrame(), "Running a Long Task", "", 0,100);
		progressMonitor.setProgress(0);

		task.addPropertyChangeListener(this);
		task.execute();

		collectTechnicalData(tickers, TECHNICAL_DATA, "");
		collectTechnicalData(tickers, TECHNICAL_DATA + DIVIDENDS_SUBFOLDER, dividendSuffix);

	}

	private void collectTechnicalData(ArrayList<String> tickers, String filePath, String suffix) {
		int i = 0;
		ensureDirectory(filePath);
		List<String> skip = new ArrayList<String>();

		try {
			String nodatafor = FileUtils.readFileToString(new File(CollectTechnicalData.TECHNICAL_DATA + File.separator + "NODATA/nodata"
					+ ".txt"));
			skip.addAll(Arrays.asList(nodatafor.split("\n")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (String ticker : (tickers)) {
			i++;
			// if (i > 20)
			// break;
			try {
				String url = urlbase + ticker + suffix;
				if (skip.contains(url))
					continue;

				HttpDownloadUtility.downloadFile(url, filePath, ticker + ".csv");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void ensureDirectory(String directoryPath) {
		File dataDirect = new File(directoryPath);
		if (!dataDirect.exists()) {
			dataDirect.mkdirs();
		}
	}

	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			Random random = new Random();
			int progress = 0;
			setProgress(0);
			try {
				Thread.sleep(1000);
				while (progress < 100 && !isCancelled()) {
					// Sleep for up to one second.
					Thread.sleep(random.nextInt(1000));
					// Make random progress.
					progress += random.nextInt(10);
					setProgress(Math.min(progress, 100));
				}
			} catch (InterruptedException ignore) {
			}
			return null;
		}
 

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
