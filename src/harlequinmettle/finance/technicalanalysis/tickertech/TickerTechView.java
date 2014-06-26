package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.JLabelFactory;
import harlequinmettle.utils.guitools.JScrollPanelledPane;
import harlequinmettle.utils.guitools.PreferredJScrollPane;
import harlequinmettle.utils.guitools.VerticalJPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TickerTechView extends JPanel {
	TickerTechModel model;
	public static TickerTechView tickertechviewaccess;

	public TickerTechView() {
		model = new TickerTechModel("BME");
		init();
	}

	public TickerTechView(String ticker) {
		model = new TickerTechModel(ticker);
		init();
	}

	public void updateSizePreferrence() {
		setPreferredSize(new Dimension((int) (model.W), (int) (model.H - 40)));
	}

	private void init() {
		tickertechviewaccess = this;

		// H = getHeight() - 40;
		model.frameW = getWidth();
		model.eH = model.H - 2 * model.margins;
		updateSizePreferrence();
		this.addMouseListener(dateDisplayer); 
		updateSizePreferrence();
		showChartInNewWindow(model.ticker);

		continuousTickerTechRenderThread.start();
	}

 
	@Override
	public void paintComponent(Graphics g1) {
		// super.paintComponent(g1);
		updateSizePreferrence();
		Graphics2D g = (Graphics2D) g1;
		model.visible = this.getVisibleRect();
		g.clip(model.visible);

		model.drawBackground(g);
		model.drawMeasurementIndicators(g);
		model.drawDates(g);
		if (model.myPreferences.get(model.GRAPH_WEEKS))
			model.drawWeeklyLines(g);
		if (model.myPreferences.get(model.VOL_BARS))
			model.drawVolumeLines(g);
		if (model.myPreferences.get(model.DIV_BALLS))
			model.drawDividendOvals(g);
		if (model.myPreferences.get(model.CANDLESTICKS)) {
			model.drawHighLowLines(g);
			model.drawOpenCloseLines(g);
		}

		model.drawAvgLines(g);
		model.drawDaysData(g);
 
	}

	public void rescaleCanvas(Dimension size) {
		// TODO Auto-generated method stub

	}

	private void showChartInNewWindow(String ticker) {
		final JFrame container = new JFrame(ticker + "   " + "  days ago  ");

		//container.setSize(900, 550);
		container.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		container.setVisible(true);
		PreferredJScrollPane tickerTechScroll = new PreferredJScrollPane(this);

		model.setScrollBar(tickerTechScroll.getViewport());

		container.add(tickerTechScroll);
		container.setExtendedState(container.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		final ComponentListener refForRemoval = doWindowRescaleListener(this);
		container.addComponentListener(refForRemoval);
		container.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				container.removeComponentListener(refForRemoval);
			}
		});

		container.setJMenuBar(makeGraphOptionsJMenu());
	}

	private JMenuBar makeGraphOptionsJMenu() {

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(makeProfileMenuItem());
		menuBar.add(makeFundamentalsGridMenuItem());
		menuBar.add( new TechnicalIndicatorsMenuItem(model));
		menuBar.add(makeOptionsMenuItem());
		return menuBar;
	}
 

	private JMenu makeOptionsMenuItem() {
		JMenu menu = new JMenu("[display options]");
		menu.addItemListener(new OptionsWindowOpenItemListener(model));
		return menu;
	}

	private JMenu makeFundamentalsGridMenuItem() {

		JMenu menu = new JMenu("[fundamental indicators]");
		menu.setMnemonic(KeyEvent.VK_S);
		VerticalJPanel twoColumn = new VerticalJPanel(2);
		PreferredJScrollPane scrollable = new PreferredJScrollPane(twoColumn);
		scrollable.setPreferredSize(new Dimension(400, 900));
		boolean alternate = true;
		for (String key : CurrentFundamentalsSQLiteDatabase.forDisplaying) {
			String value = "";
			if (model.currentFundamentals.containsKey(key))
				value = model.currentFundamentals.get(key);
			if (alternate = !alternate) {
				twoColumn.add(JLabelFactory.doBluishJLabel(key, model.BIG_FONT));
				twoColumn.add(JLabelFactory.doLightBluishJLabel(value, model.BIG_FONT));
			} else {
				twoColumn.add(JLabelFactory.doLightBluishJLabel(key, model.BIG_FONT));
				twoColumn.add(JLabelFactory.doBluishJLabel(value, model.BIG_FONT));
			}
		}
		menu.setAutoscrolls(true); 
		menu.add(scrollable);
		return menu;
	}

	private JMenu makeProfileMenuItem() {

		JMenu menu = new JMenu("[company description]");

		menu.setMnemonic(KeyEvent.VK_D);
		JButton openChromium = new JButton("open in chromium");
		openChromium.addActionListener(new ChromiumOpenerActionListener(model.ticker));
		menu.add(openChromium);
		// ///
		JTextArea myText = new JTextArea();
		myText.setBackground(new Color(200, 225, 255));
		myText.setText(model.profile);
		myText.setLineWrap(true);
		myText.setFont(model.BIG_FONT);
		myText.setWrapStyleWord(true);
		// myText.setPreferredSize(new Dimension(900, model.eH / 2));
		PreferredJScrollPane scrollable = new PreferredJScrollPane(myText);
		scrollable.setPreferredSize(new Dimension(900, 9 * model.eH / 10));
		// ///

		menu.add(scrollable);
		return menu;
	}

	private ComponentListener doWindowRescaleListener(final TickerTechView tv) {
		return new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent arg0) {

				tv.rescaleCanvas(arg0.getComponent().getBounds().getSize());

			}
		};
	}

	final MouseAdapter dateDisplayer = new MouseAdapter() {

		public void mouseClicked(MouseEvent e) {
			float newx = e.getX();
			float newy = e.getY();
			if (SwingUtilities.isLeftMouseButton(e)) {
				model.setDailyTradeData(newx, newy);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				// TODO: somthing with right mouse click
			} else if (SwingUtilities.isMiddleMouseButton(e)) {

			}
			repaint();
		}
	};
 
	private Thread continuousTickerTechRenderThread = new Thread(new Runnable() {

		@Override
		public void run() {

			// for 10 minutes max
		//	for (int i = 0; i < 50 * 60 * 10; i++) {
				while (true) {
				if (!getParent().isDisplayable())
					break;
				if(model.myPreferences.get(model.GRAPH_PRICE_MEASURE) || model.myPreferences.get(model.GRAPH_VOL_MEASURE) )
				repaint(); 
		 
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
			 
				}
			}
		}

	});
}
