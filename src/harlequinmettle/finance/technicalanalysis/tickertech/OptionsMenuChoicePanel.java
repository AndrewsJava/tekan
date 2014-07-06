package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.finance.technicalanalysis.model.db.TechnicalDatabaseSQLite;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.HorizontalJPanel;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.GeneralPath;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class OptionsMenuChoicePanel extends HorizontalJPanel {

	private static final Integer[] DAYS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 25, 30, 35, 40, 45, 50, 55,
			60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190,
			200 };

	private JComboBox<Integer> interval;
	private JComboBox<String> measure;
	private JCheckBox sqrt = new JCheckBox("square root");
	private JCheckBox trailing = new JCheckBox("trailing");

	private TickerTechModel model;
	private OptionsMenuModel state = new OptionsMenuModel();

	JCheckBox showHide = new JCheckBox("show");
	GeneralPath path;

	public OptionsMenuChoicePanel(TickerTechModel model) {

		this.model = model;
		this.state = new OptionsMenuModel();
		init();
	}

	public OptionsMenuChoicePanel(TickerTechModel model, OptionsMenuModel state) {

		this.model = model;
		this.state = state;
		init();
	}

	public void init() {
		interval = new JComboBox<Integer>(DAYS);
		measure = new JComboBox<String>(TechnicalDatabaseSQLite.elements);

	//	System.out.println("\n\noptions init");
	//	state.print();

		showHide.setSelected(state.show);
		interval.setSelectedItem(state.numberToUseInAvg);
		measure.setSelectedIndex(state.indexMeasureId);
		sqrt.setSelected(state.useCompression);
		trailing.setSelected(state.useTrailing);

		interval.addItemListener(makeIntervalChoiceItemListener());
		measure.addItemListener(makeIntervalChoiceItemListener());
		showHide.addItemListener(makeIntervalChoiceItemListener());
		sqrt.addItemListener(makeIntervalChoiceItemListener());
		trailing.addItemListener(makeIntervalChoiceItemListener());

		add(showHide);
		add(interval);
		add(measure);
		add(sqrt);
		add(trailing);

		setLineStateFromInputs();
		redrawColors();
	}

	public Color getColor() {
		return state.color;
	}

	public void resetColors() {
		state.color = state.generateColor();
		redrawColors();
	}

	private void redrawColors() {
		showHide.setBackground(state.color);
		sqrt.setBackground(state.color);
		invalidate();
	}

	public boolean isDisplayPreferred() {
		return showHide.isSelected();
	}

	// // ///////////////////
	// TickerTechView.tickertechviewaccess.repaint();

	private ItemListener makeIntervalChoiceItemListener() {
		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED
						|| event.getStateChange() == ItemEvent.DESELECTED) {

					setLineStateFromInputs();

					if (showHide.isSelected()) {
						model.optionStates.remove(state);
						model.optionStates.add(state);
						//System.out.println("\n\nshow is selected");
						//state.print();
						SerializationTool.serializeObject(model.optionStates,
								model.morePreferencesSerializedName);
					} else {
						model.optionStates.remove(state);
						SerializationTool.serializeObject(model.optionStates,
								model.morePreferencesSerializedName);
					}

					if (TickerTechView.tickertechviewaccess != null)
						TickerTechView.tickertechviewaccess.repaint();
				}
			}

		};
	}

	void setLineStateFromInputs() {

		state.numberToUseInAvg = ((Integer) interval.getSelectedItem());
		// index coordinated with techncial data
		// [date,high,low.....]
		state.indexMeasureId = measure.getSelectedIndex();
		state.useTrailing = trailing.isSelected();
		state.useCompression = sqrt.isSelected();
		state.show = showHide.isSelected();

		if (state.indexMeasureId == TechnicalDatabaseSQLite.VOLUME)
			path = model.generateAvgPath(state.indexMeasureId,
					model.minMaxVolume, state.numberToUseInAvg,
					state.useCompression, state.useTrailing);
		else
			path = model.generateAvgPath(state.indexMeasureId,
					model.minMaxPrice, state.numberToUseInAvg,
					state.useCompression, state.useTrailing);
	}
}
