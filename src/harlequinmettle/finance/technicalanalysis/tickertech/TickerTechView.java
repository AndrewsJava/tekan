package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.legacy.CurrentFundamentalsDatabase;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TickerTechView extends JPanel {
	TickerTechModel model;

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
		// H = getHeight() - 40;
		model.frameW = getWidth();
		model.eH = model.H - 2 * model.margins;
		updateSizePreferrence();
		this.addMouseListener(dateDisplayer);

		updateSizePreferrence();
		showChartInNewWindow(model.ticker);
	}

	// private void doSetUpWithTechnicalDatabase() {
	//
	// TreeMap<Float, Float> high = TechnicalDatabase.makeGetDataToDateMap(
	// ticker, TechnicalDatabase.HIGH);
	// TreeMap<Float, Float> low = TechnicalDatabase.makeGetDataToDateMap(
	// ticker, TechnicalDatabase.LOW);
	//
	// minMaxPrice = calcMinMax(low, high);
	// highLow = generateDisplayableLines(low, high, minMaxPrice);
	//
	// TreeMap<Float, Float> open = TechnicalDatabase.makeGetDataToDateMap(
	// ticker, TechnicalDatabase.OPEN);
	// TreeMap<Float, Float> close = TechnicalDatabase.makeGetDataToDateMap(
	// ticker, TechnicalDatabase.CLOSE);
	// openClose = generateDisplayableLines(open, close, minMaxPrice);
	//
	// TreeMap<Float, Float> volume = TechnicalDatabase.makeGetDataToDateMap(
	// ticker, TechnicalDatabase.VOLUME);
	// minMaxVolume = calcMinMax(volume);
	// days = calculateDaysFromMap(volume);
	// volumeBars = generateDisplayableLines(volume, minMaxVolume);
	// technicalData = TechnicalDatabase.PER_TICKER_PER_DAY_TECHNICAL_DATA
	// .get(ticker);
	//
	// if (DividendDatabase.PER_TICKER_DIVIDEND_DAY_MAP.containsKey(ticker))
	// dividendEllipses = generateDivDisplay(close);
	// }

	// @Override
	// public void update(Graphics g) {
	// super.update(g);
	// }
	@Override
	public void paintComponent(Graphics g1) {
		updateSizePreferrence();
		Graphics2D g = (Graphics2D) g1;

		// g.scale(scalex, scaley);
		model.drawBackground(g);
		if (model.myPreferences.get(model.VOL_BARS))
			model.drawVolumeLines(g);
		if (model.myPreferences.get(model.DIV_BALLS))
			model.drawDividendOvals(g);
		// drawProfileDescription(g);
		if (model.myPreferences.get(model.CANDLESTICKS)) {
			model.drawHighLowLines(g);
			model.drawOpenCloseLines(g);
		}
		model.drawDaysData(g);
		// if (toggleVisible)
		// drawFundamentalsData(g);
	}

	public void rescaleCanvas(Dimension size) {
		// TODO Auto-generated method stub

	}

	private void showChartInNewWindow(String ticker) {
		final JFrame container = new JFrame(ticker + "   "
		// + TechnicalDatabase.NUM_DAYS_START + "  days ago, to "
		// + TechnicalDatabase.NUM_DAYS_END
				+ "  days ago  ");

		container.setSize(900, 550);
		container.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		container.setVisible(true);
		JScrollPanelledPane chart = new JScrollPanelledPane();
		// TickerTechView tv = new TickerTechView(ticker);
		PreferredJScrollPane tickerTechScroll = new PreferredJScrollPane(this);
		model.setScrollBar(tickerTechScroll.getViewport());

		chart.addComp(tickerTechScroll);

		container.add(chart);
		container.setExtendedState(container.getExtendedState()
				| JFrame.MAXIMIZED_BOTH);
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
		menuBar.add(makeOptionsMenuItem());

		// a group of check box menu items
		// menu.addSeparator();

		// cbMenuItem.setMnemonic(KeyEvent.VK_C);
		// menu.add(cbMenuItem);
		return menuBar;
	}

	private JMenu makeOptionsMenuItem() {
		JMenu menu = new JMenu("[options]");
		for (String s : model.preferenceOptions) {
			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(s);
			cbMenuItem.addItemListener(makePreferencesItemListener());
			if (model.myPreferences.get(s))
				cbMenuItem.setSelected(true);
			menu.add(cbMenuItem);
		}
		return menu;
	}

	private ItemListener makePreferencesItemListener() {
		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {

				JCheckBoxMenuItem source = ((JCheckBoxMenuItem) arg0
						.getSource());
				String sourceText = source.getText();
				model.myPreferences.put(sourceText, source.isSelected());
				System.out.println(model.myPreferences);
				repaint();
				SerializationTool.serialize(model.myPreferences,
						model.preferencesSerializedName);
			}

		};
	}

	private JMenu makeFundamentalsGridMenuItem() {

		JMenu menu = new JMenu("[company statistics]");
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
				twoColumn
						.add(JLabelFactory.doBluishJLabel(key, model.BIG_FONT));
				twoColumn.add(JLabelFactory.doLightBluishJLabel(value,
						model.BIG_FONT));
			} else {
				twoColumn.add(JLabelFactory.doLightBluishJLabel(key,
						model.BIG_FONT));
				twoColumn.add(JLabelFactory.doBluishJLabel(value,
						model.BIG_FONT));
			}
		}
		menu.setAutoscrolls(true);
		// menu.scrollRectToVisible(aRect);
		menu.add(scrollable);
		return menu;
	}

	private JMenu makeProfileMenuItem() {

		JMenu menu = new JMenu("[company description]");

		menu.setMnemonic(KeyEvent.VK_D);
		menu.getAccessibleContext().setAccessibleDescription(
				"The only menu in this program that has menu items");

		// ///
		JTextArea myText = new JTextArea();
		myText.setBackground(new Color(200, 225, 255));
		myText.setText(model.profile);
		myText.setLineWrap(true);
		myText.setFont(model.BIG_FONT);
		myText.setWrapStyleWord(true);
		myText.setPreferredSize(new Dimension(900, model.eH / 2));
		PreferredJScrollPane scrollable = new PreferredJScrollPane(myText);
		scrollable.setPreferredSize(new Dimension(900, model.eH / 2));
		// ///

		// JMenuItem menuItem = new JMenuItem(profile,
		// KeyEvent.VK_T);
		// menuItem.setPreferredSize(new Dimension(eW,eH));
		// menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
		// ActionEvent.ALT_MASK));
		// menuItem.getAccessibleContext().setAccessibleDescription(
		// "This doesn't really do anything");
		// menu.add(menuItem);
		menu.add(scrollable);
		// menu.add(myText);
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

		public boolean toggleVisible = false;

		public void mouseClicked(MouseEvent e) {
			float newx = e.getX();
			float newy = e.getY();
			if (SwingUtilities.isLeftMouseButton(e)) {
				model.setDailyTradeData(newx, newy);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				toggleVisible = !toggleVisible;
				model.xf = newx;
				model.yf = newy;
			} else if (SwingUtilities.isMiddleMouseButton(e)) {

			}
			repaint();
		}

	};
}
