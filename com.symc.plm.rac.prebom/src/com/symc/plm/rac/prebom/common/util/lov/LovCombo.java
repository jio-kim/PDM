package com.symc.plm.rac.prebom.common.util.lov;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;

import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.teamcenter.rac.kernel.TCComponentUnitOfMeasure;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.client.model.LovValue;

@SuppressWarnings("serial")
public class LovCombo extends JComboBox<ListOfValue>{
    
	@SuppressWarnings("unused")
    private TCSession session = null;
	@SuppressWarnings("unused")
    private String lovName = null;
	private String userTypedKeys = null;
	
	public LovCombo(TCSession session, String lovName) throws TCException{
		this.session = session;
		this.lovName = lovName;
		
		List<LovValue> list = BomUtil.getLovValues(session, lovName);
		if( list == null || list.isEmpty()){
			return;
		}
		
		addItem(new ListOfValue("","","",""));
		for( LovValue lovValue : list){
			
			ListOfValue lov = null;
			if( lovName.equals("Unit of Measures")){
				TCComponentUnitOfMeasure uom = (TCComponentUnitOfMeasure)lovValue.getValue();
				lov = new ListOfValue(uom.toString(), uom.toString(), uom.toString(), uom.toString());
			}else{
				lov = new ListOfValue(lovValue.getStringValue(), lovValue.getDescription(), lovValue.getDisplayValue(), lovValue.getDisplayDescription());
			}
			addItem(lov);
		}
		
		JComboBox.KeySelectionManager manager = new JComboBox.KeySelectionManager() {
			public int selectionForKey(char aKey, ComboBoxModel aModel) {

				String currentKeys = userTypedKeys;
				if (currentKeys == null) {
					currentKeys = "" + aKey;
				} else {
					currentKeys += aKey;
				}
				currentKeys = currentKeys.toUpperCase();

				for (int i = 0; i < aModel.getSize(); i++) {
					if (aModel.getElementAt(i).toString().toUpperCase()
							.startsWith(currentKeys)) {
						userTypedKeys = currentKeys;
						return i;
					}
				}
				return -1;
			}
		};
		setKeySelectionManager(manager);
		
		addFocusListener(new FocusAdapter(){

			@Override
			public void focusLost(FocusEvent focusevent) {
				userTypedKeys = null;
				super.focusLost(focusevent);
			}
			
		});
		
		getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"deleteTypedKey");
		getActionMap().put("deleteTypedKey", new DeleteAction());
	}
	
	public String getSelectedString(){
		ListOfValue lov = (ListOfValue)getSelectedItem();
		return lov.getStringValue();
	}
	
	public String getSelectedDisplayString(){
		ListOfValue lov = (ListOfValue)getSelectedItem();
		return lov.getDisplayValue();
	}
	
	public String getSelectedDescription(){
		ListOfValue lov = (ListOfValue)getSelectedItem();
		return lov.getDescription();
	}
	
	class DeleteAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if( userTypedKeys != null && !userTypedKeys.equals("")){
				userTypedKeys = userTypedKeys.substring(0, userTypedKeys.length() - 1);
			}
		}
	}
}