package harlequinmettle.finance.technicalanalysis.util;

import harlequinmettle.finance.technicalanalysis.model.db.CurrentFundamentalsSQLiteDatabase;
import harlequinmettle.utils.guitools.FilterPanel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;

import javax.swing.JComboBox;

public class DefaultRangeSettingItemListener implements ItemListener {
	FilterPanel filterPanel;
	public DefaultRangeSettingItemListener(FilterPanel fp){
		this.filterPanel = fp;
	}
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			String indicator  =  ((JComboBox) event.getSource()).getSelectedItem().toString();
			Point2D.Float minMax = CurrentFundamentalsSQLiteDatabase.getMinMaxForIndicator(indicator	);
			filterPanel.low.setText(""+minMax.x);
			filterPanel.high.setText(""+minMax.y);
		}

	} 

}
