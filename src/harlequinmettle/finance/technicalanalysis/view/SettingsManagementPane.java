package harlequinmettle.finance.technicalanalysis.view;

import harlequinmettle.utils.filetools.ChooseFilePrompter;
import harlequinmettle.utils.filetools.SavedSettings;
import harlequinmettle.utils.filetools.SerializationTool;
import harlequinmettle.utils.guitools.JScrollPanelledPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;

public class SettingsManagementPane extends JScrollPanelledPane {
public SettingsManagementPane(){
	init();
}
public void init(){
	File[] settingsObjects = new File("application_settings").listFiles();
	for(File f: settingsObjects){
	JButton b = new JButton(f.getName());
	b.addActionListener(generateBrowseListener(f));
	addComp(b);
	}
}
private ActionListener generateBrowseListener(final File f) {
	return new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {

			SavedSettings savedsettings = SerializationTool.deserializeObject(SavedSettings.class,
					f.getPath()); 
			if(savedsettings.settings.size()==1){
				String key = savedsettings.settings.firstKey();
			String savedPath = ChooseFilePrompter.filePathChooser();
			savedsettings.settings.put(key, savedPath);
			SerializationTool.serializeObject(savedsettings, savedPath);
			}else{
				//choose which setting to set
			}
			
		}
		
	};
}
}
