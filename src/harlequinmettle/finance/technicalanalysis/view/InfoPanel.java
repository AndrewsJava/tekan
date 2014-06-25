package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.utils.guitools.FilterPanel;
import harlequinmettle.utils.guitools.JLabelFactory;

import java.util.ArrayList;

import javax.swing.JLabel;

public class InfoPanel extends FilterPanel{

	public InfoPanel(ArrayList<String> chooseFrom) {
		super(chooseFrom);
		JLabel infoLabel = JLabelFactory.doBluishJLabel("get info for indicator: ");
		high.setEditable(false);
		low.setEditable(false);
	this.remove(include);
	this.add(infoLabel,0);
	}

}
