package com.ssangyong.commands.revise;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.ssangyong.common.FunctionField;
import com.ssangyong.common.SYMCAWTLabel;
import com.ssangyong.common.dialog.AbstractSearchResultDialog;
import com.ssangyong.common.utils.ComponentService;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

@SuppressWarnings("serial")
public class SYMCReviseSearchDialog extends AbstractSearchResultDialog {

	private FunctionField itemIDTF;
	private FunctionField itemNameTF;
	private FunctionField owningUserTF;
	private FunctionField ecoNoField;
	private JCheckBox	  ownedItem;
	private boolean isSecond;
	private int resultSize;
	
	public SYMCReviseSearchDialog(String title, String headerMessage, String[] header, FunctionField ecoNoField) {
		super(title, headerMessage, header);
		this.ecoNoField = ecoNoField;
		isSecond = false;
		searchButton.doClick();
	}

	@Override
	protected JPanel getSearchConditionPanel(){

		super.getSearchConditionPanel();

		Registry registry = Registry.getRegistry(this);
		
		SYMCAWTLabel label7 = new SYMCAWTLabel("ECO Number");
		itemIDTF = new FunctionField(0);
		itemIDTF.setText("*");
		conditionPanel.add("1.1.right.center.preferred.preferred", label7);
		conditionPanel.add("1.2.left.center.resizable.preferred", itemIDTF);

		SYMCAWTLabel label1 = new SYMCAWTLabel("ECO Name");
		itemNameTF = new FunctionField(0);
		conditionPanel.add("2.1.right.center.preferred.preferred", label1);
		conditionPanel.add("2.2.left.center.resizable.preferred", itemNameTF);
		
		owningUserTF = new FunctionField(0);
		conditionPanel.add("3.1.right.center.preferred.preferred", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.UserName")));
		conditionPanel.add("3.2.left.center.resizable.preferred", owningUserTF);
		
		ownedItem = new JCheckBox();
		ownedItem.setText("Owned ECO");
		ownedItem.setBackground(Color.WHITE	);
		conditionPanel.add("3.3.left.center.resizable.preferred", ownedItem);
		
		ownedItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(ownedItem.isSelected()) {
					String currentUserName = session.getUserName();
					owningUserTF.setText(currentUserName);
					owningUserTF.setEnabled(false);
				} else {
					owningUserTF.setEnabled(true);
				}
			}
		});

		ComponentService.createService(session);
		ComponentService.setLabelSize(conditionPanel, 90, 21);
		ComponentService.setComboboxSize(conditionPanel, 120, 21);
		ComponentService.setTextfieldSizs(conditionPanel, 120, 21);

		return conditionPanel;
		
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	/** 최초 ECO 검색 Dialog 호출시에는 해당 로그인 유저가 생성한 ECO(In Work 상태)를 자동 검색한다.
	 */
	public TCComponent[] search() throws Exception {
        ArrayList<String> entry = new ArrayList<String>();
        ArrayList<String> value = new ArrayList<String>();
        String queryName = "__SYMC_S7_ECO_Revision";
            entry.add("item_id");
            value.add("*"+itemIDTF.getText()+"*");
        if(!itemNameTF.getText().equals("")) {
            entry.add("object_desc");
            value.add("*"+itemNameTF.getText()+"*");
        }
        if(isFirst && !isSecond) {
        	String currentUserName = this.session.getUserName();
        	entry.add("userid");
            value.add(currentUserName);
            isFirst = false;
            isSecond = true;
            owningUserTF.setText(currentUserName);
            owningUserTF.setEnabled(false);
            ownedItem.setSelected(true);
        } else {
        	if(!owningUserTF.getText().equals("")) {
                entry.add("userid");
                value.add("*"+ owningUserTF.getText()+"*");
            }
        }
        entry.add("maturity");
        value.add("In Work");
        
        
        TCComponent[] qryResult = CustomUtil.queryComponent(queryName, entry.toArray(new String[entry.size()]), value.toArray(new String[value.size()]));
		
		
//		String[] entryName = {"Type", "Name", "ItemID", "OwningUser"};
//		String[] entryValue = {"ItemRevision", "*"+itemNameTF.getText()+"*", "*"+itemIDTF.getText()+"*", "*"+ owningUserTF.getText()+"*"};
//		TCComponent[] qryResult = CustomUtil.queryComponent("Item Revision...", entryName, entryValue);
        
//        String[] entryName = {"item_id", "object_desc", "userid"};
//		String[] entryValue = {"*"+itemIDTF.getText()+"*", "*"+itemNameTF.getText()+"*", "*"+ owningUserTF.getText()+"*"};
//		TCComponent[] qryResult = CustomUtil.queryComponent("__SYMC_S7_ECO_Revision", entryName, entryValue);
		ArrayList tempList = new ArrayList();
		resultSize = qryResult.length;

		if(qryResult != null && qryResult.length != 0){
			for(int i=0; i<qryResult.length; i++){
				tempList.add(qryResult[i]);
			}
		}
		components = new TCComponent[tempList.size()];
		for(int i=0; i<tempList.size(); i++){
			components[i] = (TCComponent)tempList.get(i);
		}
//		isFirst = false;
		return components;
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		
		if(table.getSelectedComponents() != null && table.getSelectedComponents().length != 0) {
			TCComponentItemRevision selectedRevision = (TCComponentItemRevision)table.getSelectedComponents()[0];
			try {
				ecoNoField.setText(selectedRevision.getProperty("item_id").toString());
				ecoNoField.setTcComponent(selectedRevision);
				
				
			} catch (TCException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void preSearchAction() {
		
	}

	@Override
	protected JPanel getUIPanel() {
		return conditionPanel;
	}
	
	@Override
	public boolean validCheck() {
		if (resultSize == 0 || table.getSelectedComponents().length == 0) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Selected ECO is not exist.", "Error", MessageBox.INFORMATION);
			return false;
		} else {
			return true;
		}
	}
	


}
