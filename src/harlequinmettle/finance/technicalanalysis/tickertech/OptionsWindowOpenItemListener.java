package harlequinmettle.finance.technicalanalysis.tickertech;

import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.JLabelFactory;
import harlequinmettle.utils.guitools.JScrollPanelledPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

public class OptionsWindowOpenItemListener implements ItemListener {
	TickerTechModel model;
	OptionsWindowOpenItemListener( TickerTechModel model ){
		this.model = model;
	}
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
							.doBluishJLabel("show | days | indicator | compression | trailing"));
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


			public ItemListener makePreferencesItemListener() {
				return new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent arg0) {

						if (arg0.getStateChange() == ItemEvent.SELECTED
								|| arg0.getStateChange() == ItemEvent.DESELECTED) {
							JCheckBox source = ((JCheckBox) arg0.getSource());
							String sourceText = source.getText();
							model.myPreferences.put(sourceText, source.isSelected());
							 
							TickerTechView.tickertechviewaccess.repaint();
							SerializationTool.serializeObject(model.myPreferences,
									model.preferencesSerializedName);
						}
					}

				};
			}



}
