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
	}

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

		container.setSize(900, 550);
		container.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		container.setVisible(true);
		JScrollPanelledPane chart = new JScrollPanelledPane();
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
		return menuBar;
	}

	private JMenu makeOptionsMenuItem() {
		JMenu menu = new JMenu("[options]");
		menu.addItemListener(makeOptionsWindowOpenItemListener());
		return menu;
	}

	private ItemListener makeOptionsWindowOpenItemListener() {
		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				// if (true) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					JFrame optionsWindow = new JFrame("select graph options");
					optionsWindow
							.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					JScrollPanelledPane options = new JScrollPanelledPane();
					optionsWindow.setVisible(true);
					optionsWindow.setSize(650, 200);
					optionsWindow.add(options);
					optionsWindow.setAlwaysOnTop(true);

					for (String s : model.preferenceOptions) {
						JCheckBox cbMenuItem = new JCheckBox(s);
						cbMenuItem
								.addItemListener(makePreferencesItemListener());
						if (model.myPreferences.get(s))
							cbMenuItem.setSelected(true);
						options.addComp(cbMenuItem);
					}
					options.addComp(JLabelFactory
							.doBluishJLabel("averages - (days*2),measure,option"));
					for (OptionsMenuChoicePanel avgLine : model.lineAverageChoices)
						options.addComp(avgLine);
					JButton resetColors = new JButton("reset colors");
					options.addComp(resetColors);
					resetColors.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							for (OptionsMenuChoicePanel lineOp : model.lineAverageChoices) {
								if (!lineOp.showHide.isSelected()) {
									lineOp.resetColors();
								}
							}
						}

					});
				}
			}

		};
	}

	// private JMenu makeOptionsMenuItem() {
	// JMenu menu = new JMenu("[options]");
	// for (String s : model.preferenceOptions) {
	// JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(s);
	// cbMenuItem.addItemListener(makePreferencesItemListener());
	// if (model.myPreferences.get(s))
	// cbMenuItem.setSelected(true);
	// menu.add(cbMenuItem);
	// }
	// return menu;
	// }
	public ItemListener makePreferencesItemListener() {
		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {

				if (arg0.getStateChange() == ItemEvent.SELECTED
						|| arg0.getStateChange() == ItemEvent.DESELECTED) {
					JCheckBox source = ((JCheckBox) arg0.getSource());
					String sourceText = source.getText();
					model.myPreferences.put(sourceText, source.isSelected());
					System.out.println(model.myPreferences);
					repaint();
					SerializationTool.serialize(model.myPreferences,
							model.preferencesSerializedName);
				}
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
