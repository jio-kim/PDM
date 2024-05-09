package com.kgm.commands.migration.bom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.kgm.commands.migration.MigrationDialog;
import com.kgm.common.OperationAbortedListener;
import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

@SuppressWarnings({"rawtypes", "unused", "unchecked"})
public class BomMigrationOperation extends AbstractAIFOperation implements OperationAbortedListener {
	private TCSession session;
	private TCComponentBOMWindow bomWindow;
	private JProgressBar progressBar;
	private DefaultTableModel model;
    private HashMap structureMap = new HashMap();
	private HashMap bomlineMap = new HashMap();
	private int line = 0;
	private boolean goOnError, aborted;
	private MigrationDialog dialog;
	private static final int PRODUCT = 0;
	private static final int RAW_MATERIALS = 1;
	private static final int SUB_MATERIALS = 2;
	private static final int MOLD_FORMER = 3;
	private static final int MOLD_TAPPING = 4;
	private static final int MOLD_TAP = 5;
	private static final int MOLD_ETC = 6;
	private static final int EQUIPMENT_FORMER = 7;
	private static final int EQUIPMENT_TAPPING = 8;
	private static final int EQUIPMENT_ETC = 9;
	private static final int PROCESSING = 10;

	private TCComponent equipment_configuration_folder;
	private TCComponentItemType itemType;
	
	private boolean newEquipment = true;
	
	private String Top_Item;

	public BomMigrationOperation(MigrationDialog dialog, TCSession session, JTable table, boolean goOnError, JProgressBar progressBar) {
		this.session     = session;
		this.model       = (DefaultTableModel)table.getModel();
		this.progressBar = progressBar;
		this.goOnError = goOnError;
		this.dialog = dialog;
	}

	@Override
	public void executeOperation() {
		try {
			TCComponent[] equip_config_folder = CustomUtil.queryComponent(CustomUtil.getTextServerString(session, "k_find_general_name"), new String[]{"Type", "OwningUser", "Name"}, new String[]{"Folder", "infodba", "설비 구성"});
			if(equip_config_folder != null){
				if(equip_config_folder.length > 1){
					for(int i=0; i<equip_config_folder.length; i++){
						String str = equip_config_folder[i].getProperty("gov_classification");
						if(!(str == null || str.equals(""))){
							equipment_configuration_folder = equip_config_folder[i];
							break;
						}
					}
				}
				else if(equip_config_folder.length == 1){
					equipment_configuration_folder = equip_config_folder[0];
				}
			}
			
			itemType = (TCComponentItemType)session.getTypeComponent(SYMCClass.Equipment_Dummy_TYPE);
			
			int count = topBomLineCount();
			if(count == 0){
				MessageBox.post(dialog, "BOM 구성이 완료 되었습니다. 더이상 구성할 제품 코드가 없습니다.", "알림", MessageBox.INFORMATION);
				return;
			}
			int showOK = JOptionPane.showConfirmDialog(dialog, count+" 개 남음 \n진행(생성) 하시려면 예(Y) 버튼을 누르세요. 가장 많은 개수의 제품 아이디는 => " + Top_Item + " 입니다.", "Create...", JOptionPane.YES_NO_OPTION);
			if(showOK == JOptionPane.OK_OPTION){
				createBomStructure();
				MessageBox.post(dialog, "BOM 구성이 완료 되었습니다. 결과를 확인 하세요." + (count-1) + " 남았습니다.", "알림", MessageBox.INFORMATION);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private int topBomLineCount(){
		int count = 0;
		int count2 = 0;
		for(int i = 0 ; i < model.getRowCount() ; i++){
			count2 = 0;
			String is_top_line = (String)model.getValueAt(i, 1);
			String parent_line_id = (String)model.getValueAt(i, 2);
			if(is_top_line != null && !is_top_line.equals("") && is_top_line.trim().equalsIgnoreCase("Y")){
				
				TCComponent[] comps = findTopItem(parent_line_id);
				try {
					for(int k=0; k<comps.length; k++){
						if(comps[k].getRelatedComponent("bom_view_tags") == null){
							count2++;
						}
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
				if(count < count2){
					count = count2;
					Top_Item = parent_line_id;
				}
			}
		}
		return count;
	}

	private void createBomStructure(){

		progressBar.setMaximum(model.getRowCount());

		for(int i = 0 ; i < model.getRowCount() ; i++){

			if(aborted){
				return;
			}

			line = i;

			try{
				String is_top_line = (String)model.getValueAt(i, 1);
				String parent_line_id = (String)model.getValueAt(i, 2);
				String child_line_id = (String)model.getValueAt(i, 4);

				progressBar.setString("Create BOMLine - " + parent_line_id);
				progressBar.setValue(i + 1);

				if(is_top_line != null && !is_top_line.equals("") && is_top_line.trim().equalsIgnoreCase("Y")){   // Top-Bomline

					structureMap.clear();
					bomlineMap.clear();

					if(bomWindow != null){
						bomWindow.save();
						bomWindow.close();
						bomWindow = null;
					}

					TCComponent[] comps = findTopItem(parent_line_id);
					TCComponent[] comps1 = findChildItem(parent_line_id, child_line_id, line);
					TCComponentItem parent = null;
					
					for(int k=0; k<comps.length; k++){
						if(comps[k].getRelatedComponent("bom_view_tags") != null){
							newEquipment = false;
							break;
						}else{
							newEquipment = true;
						}
					}
					
					for(int k=0; k<comps.length; k++){
						if(comps[k].getRelatedComponent("bom_view_tags") == null){
							parent = (TCComponentItem)comps[k];
							break;
						}
					}
					if(parent != null){
						createTopBOMLine(parent_line_id, parent, child_line_id, (TCComponentItem)comps1[0]);
						model.setValueAt(new String("OK"), line, model.getColumnCount() - 2);
					}
				} 
				else {   //Child-Bomline
					TCComponent[] comps = findChildItem(parent_line_id, child_line_id, line);
					TCComponentItem child = (TCComponentItem)comps[0];
					addChildBOMLine(parent_line_id, child_line_id, child);
					model.setValueAt(new String("OK"), line, model.getColumnCount() - 2);
				}
			}catch (Exception e) {
				e.printStackTrace();
				model.setValueAt(new String("Fail"), line, model.getColumnCount() - 2);
				model.setValueAt(e.getMessage(), line, model.getColumnCount() - 1);
				if (!goOnError) {
					return;
				} else {
					continue;
				}
			}
		}

		if(bomWindow != null){
			try {
				bomWindow.save();
				bomWindow.close();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}

	private TCComponent[] findTopItem(String id){
		try {
			TCComponent[] queryResult = CustomUtil.queryComponent(SYMCClass.QryProductSearch, new String[]{"제품 코드"}, new String[]{id});
			return queryResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private TCComponent[] findChildItem(String parent_id, String child_id, int row){
		try {
			String item_type = (String)model.getValueAt(row, 5);
			int type = Integer.parseInt(item_type.trim());
			TCComponent[] queryResult = null;
			switch (type) {

			case PROCESSING:
				queryResult = new TCComponent[1]; 
				queryResult[0] = CustomUtil.createItem(SYMCClass.Processing_TYPE, CustomUtil.getNextItemId(SYMCClass.Processing_TYPE), SYMCClass.ITEM_REV_ID, child_id+"단계", "");
				queryResult[0].setProperty("gov_classification", child_id);
				break;

			case RAW_MATERIALS:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryRawMaterialsSearch, new String[]{"원자재 코드"}, new String[]{child_id});
				break;

			case SUB_MATERIALS:
				queryResult = CustomUtil.queryComponent(SYMCClass.QrySubMaterialsSearch, new String[]{"부자재 코드"}, new String[]{child_id});
				break;

			case EQUIPMENT_FORMER:
				queryResult = new TCComponent[1];
				TCComponent[] comps = CustomUtil.queryComponent(SYMCClass.QryEquipmentFSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				if(comps != null && comps.length != 0){
					//TCComponentItem item = itemType.find(child_id + "_" + parent_id);
				    TCComponentItem item = null;
					TCComponentItem[] items = itemType.findItems(child_id + "_" + parent_id);
					if(items != null && items.length > 0) {
                        item = items[0];
                    }
					
					if(item == null){
						queryResult[0] = CustomUtil.createItem(SYMCClass.Equipment_Dummy_TYPE, child_id + "_" + parent_id, SYMCClass.ITEM_REV_ID, comps[0].toDisplayString(), "");
						comps[0].add("IMAN_reference",  queryResult[0]);
					}
					else{
						queryResult[0] = item;
					}

//					TCComponentContextList list = equipment_configuration_folder.getChildrenList("contents");
//					if(!list.toComponentVector().contains(comps[0])){
//						equipment_configuration_folder.add("contents", comps[0]);
//						comps[0].add("IMAN_reference", queryResult[0]);
//					}
					
					List<InterfaceAIFComponent> list = new ArrayList<InterfaceAIFComponent>();
					AIFComponentContext[] contexts = equipment_configuration_folder.getChildren("contents");
					for(AIFComponentContext context : contexts) {
					    list.add(context.getComponent());
					}
					
				    if(!list.contains(comps[0])){
				        equipment_configuration_folder.add("contents", comps[0]);
				        comps[0].add("IMAN_reference", queryResult[0]);
				    }
				}
				break;

			case EQUIPMENT_TAPPING:
				queryResult = new TCComponent[1];
				TCComponent[] comps1 = CustomUtil.queryComponent(SYMCClass.QryEquipmentTSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				if(comps1 != null && comps1.length != 0){
					//TCComponentItem item = itemType.find(child_id + "_" + parent_id);
				    TCComponentItem item = null;
                    TCComponentItem[] items = itemType.findItems(child_id + "_" + parent_id);
                    if(items != null && items.length > 0) {
                        item = items[0];
                    }
                    
					if(item == null){
						queryResult[0] = CustomUtil.createItem(SYMCClass.Equipment_Dummy_TYPE, child_id + "_" + parent_id, SYMCClass.ITEM_REV_ID, comps1[0].toDisplayString(), "");
						comps1[0].add("IMAN_reference",  queryResult[0]);
					}
					else{
						queryResult[0] = item;
					}
					
					//TCComponentContextList list = equipment_configuration_folder.getChildrenList("contents");
					List<InterfaceAIFComponent> list = new ArrayList<InterfaceAIFComponent>();
                    AIFComponentContext[] contexts = equipment_configuration_folder.getChildren("contents");
                    for(AIFComponentContext context : contexts) {
                        list.add(context.getComponent());
                    }
                    
					if(!list.contains(comps1[0])){
						equipment_configuration_folder.add("contents", comps1[0]);
						comps1[0].add("IMAN_reference", queryResult[0]);
					}
				}
				break;

			case EQUIPMENT_ETC:
				queryResult = new TCComponent[1];
				TCComponent[] comps2 = CustomUtil.queryComponent(SYMCClass.QryEquipmentESearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				if(comps2 != null && comps2.length != 0){
					//TCComponentItem item = itemType.find(child_id + "_" + parent_id);
				    TCComponentItem item = null;
                    TCComponentItem[] items = itemType.findItems(child_id + "_" + parent_id);
                    if(items != null && items.length > 0) {
                        item = items[0];
                    }
                    
					if(item == null){
						queryResult[0] = CustomUtil.createItem(SYMCClass.Equipment_Dummy_TYPE, child_id + "_" + parent_id, SYMCClass.ITEM_REV_ID, comps2[0].toDisplayString(), "");
						comps2[0].add("IMAN_reference",  queryResult[0]);
					}
					else{
						queryResult[0] = item;
					}

					//TCComponentContextList list = equipment_configuration_folder.getChildrenList("contents");
					List<InterfaceAIFComponent> list = new ArrayList<InterfaceAIFComponent>();
                    AIFComponentContext[] contexts = equipment_configuration_folder.getChildren("contents");
                    for(AIFComponentContext context : contexts) {
                        list.add(context.getComponent());
                    }
                    
					if(!list.contains(comps2[0])){
						equipment_configuration_folder.add("contents", comps2[0]);
						comps2[0].add("IMAN_reference", queryResult[0]);
					}
				}
				break;

			case MOLD_FORMER:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryMoldFormerSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				break;

			case MOLD_TAPPING:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryMoldTappingSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				break;

			case MOLD_TAP:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryMoldTapSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				break;

			case MOLD_ETC:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryMoldEtcSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				break;
			}
			return queryResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createTopBOMLine(String parent_id, TCComponentItem top, String child_id, TCComponentItem child) throws Exception {
		TCComponentBOMLine bomTopLine = null;
		TCComponentBOMLine childLine = null;
		try{
			TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType)session.getTypeComponent("RevisionRule");
			TCComponentRevisionRule revisionRule = revisionRuleType.getDefaultRule();
			TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType)session.getTypeComponent("BOMWindow");
			bomWindow = bomWindowType.create(revisionRule);

			String type = model.getValueAt(line, 5).toString();
			TCComponentItem childItem = null;

			if(Integer.parseInt(type) == EQUIPMENT_FORMER){
				TCComponent[] comps = CustomUtil.queryComponent(SYMCClass.QryEquipmentFSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				if(comps != null && comps.length != 0){
					//TCComponentItem item = itemType.find(child_id + "_" + parent_id);
				    TCComponentItem item = null;
                    TCComponentItem[] items = itemType.findItems(child_id + "_" + parent_id);
                    if(items != null && items.length > 0) {
                        item = items[0];
                    }
                    
					if(item == null){
						childItem = CustomUtil.createItem(SYMCClass.Equipment_Dummy_TYPE, child_id + "_" + parent_id, SYMCClass.ITEM_REV_ID, comps[0].toDisplayString(), "");
						comps[0].add("IMAN_reference", childItem);
					}
					else{
						childItem = item;
					}
					
					//TCComponentContextList list = equipment_configuration_folder.getChildrenList("contents");
					List<InterfaceAIFComponent> list = new ArrayList<InterfaceAIFComponent>();
                    AIFComponentContext[] contexts = equipment_configuration_folder.getChildren("contents");
                    for(AIFComponentContext context : contexts) {
                        list.add(context.getComponent());
                    }
                    
					if(!list.contains(comps[0])){
						equipment_configuration_folder.add("contents", comps[0]);
						comps[0].add("IMAN_reference", childItem);
					}
				}
			}
			else if(Integer.parseInt(type) == EQUIPMENT_TAPPING){
				TCComponent[] comps = CustomUtil.queryComponent(SYMCClass.QryEquipmentTSearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				if(comps != null && comps.length != 0){
					//TCComponentItem item = itemType.find(child_id + "_" + parent_id);
				    TCComponentItem item = null;
                    TCComponentItem[] items = itemType.findItems(child_id + "_" + parent_id);
                    if(items != null && items.length > 0) {
                        item = items[0];
                    }
                    
					if(item == null){
						childItem = CustomUtil.createItem(SYMCClass.Equipment_Dummy_TYPE, child_id + "_" + parent_id, SYMCClass.ITEM_REV_ID, comps[0].toDisplayString(), "");
						comps[0].add("IMAN_reference", childItem);
					}
					else{
						childItem = item;
					}
					
					//TCComponentContextList list = equipment_configuration_folder.getChildrenList("contents");
					List<InterfaceAIFComponent> list = new ArrayList<InterfaceAIFComponent>();
                    AIFComponentContext[] contexts = equipment_configuration_folder.getChildren("contents");
                    for(AIFComponentContext context : contexts) {
                        list.add(context.getComponent());
                    }
                    
					if(!list.contains(comps[0])){
						equipment_configuration_folder.add("contents", comps[0]);
						comps[0].add("IMAN_reference", childItem);
					}
				}
			}
			else if(Integer.parseInt(type) == EQUIPMENT_ETC){
				TCComponent[] comps = CustomUtil.queryComponent(SYMCClass.QryEquipmentESearch, new String[]{"ERP_CODE"}, new String[]{child_id});
				if(comps != null && comps.length != 0){
					//TCComponentItem item = itemType.find(child_id + "_" + parent_id);
				    TCComponentItem item = null;
                    TCComponentItem[] items = itemType.findItems(child_id + "_" + parent_id);
                    if(items != null && items.length > 0) {
                        item = items[0];
                    }
                    
					if(item == null){
						childItem = CustomUtil.createItem(SYMCClass.Equipment_Dummy_TYPE, child_id + "_" + parent_id, SYMCClass.ITEM_REV_ID, comps[0].toDisplayString(), "");
						comps[0].add("IMAN_reference", childItem);
					}
					else{
						childItem = item;
					}
					
					//TCComponentContextList list = equipment_configuration_folder.getChildrenList("contents");
					List<InterfaceAIFComponent> list = new ArrayList<InterfaceAIFComponent>();
                    AIFComponentContext[] contexts = equipment_configuration_folder.getChildren("contents");
                    for(AIFComponentContext context : contexts) {
                        list.add(context.getComponent());
                    }
                    
					if(!list.contains(comps[0])){
						equipment_configuration_folder.add("contents", comps[0]);
						comps[0].add("IMAN_reference", childItem);
					}
				}
			}

			bomTopLine = bomWindow.setWindowTopLine(null, top.getLatestItemRevision(), null, null);
			if(childItem == null){
				childLine = bomTopLine.add(null, child.getLatestItemRevision(), null, false);
			}
			else{
				childLine = bomTopLine.add(null, childItem.getLatestItemRevision(), null, false);
			}

			structureMap.put(parent_id , "0");
			bomlineMap.put(parent_id, bomTopLine);

			structureMap.put(child_id + "_" + parent_id , "1");
			bomlineMap.put(child_id + "_" + parent_id, childLine);

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void setBOMLineProperty(TCComponentBOMLine line, String propNames) throws Exception {

		line.getTCProperty(propNames).setStringValue((String)model.getValueAt(this.line, 6));
	}

	private void addChildBOMLine(String parent_id, String child_id, TCComponentItem childItem) throws Exception {
		if(bomWindow == null){
			return;
		}
		if(!structureMap.containsKey(parent_id)){
			return;
		}
		if(!bomlineMap.containsKey(parent_id)){
			return;
		}

		TCComponentBOMLine childBomLine = null;

		try{
			int parentLvl = Integer.parseInt(structureMap.get(parent_id).toString());
			int childLvl = parentLvl + 1;

			TCComponentBOMLine parent = (TCComponentBOMLine)bomlineMap.get(parent_id);
			if(parent.getItem().getType().equals(SYMCClass.Equipment_Dummy_TYPE)){
				if(!newEquipment){
					return;
				}
			}
			AIFComponentContext[] context = parent.getChildren();
			ArrayList list = new ArrayList();
			for(int i=0; i<context.length; i++){
				list.add(context[i]);
			}
			if(!list.contains(childItem.getLatestItemRevision())){
				childBomLine = parent.add(null, childItem.getLatestItemRevision(), null, false);
				structureMap.put(child_id, childLvl);
				bomlineMap.put(child_id, childBomLine);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		//		setBOMLineProperty(bomLine, "bl_quantity");
	}

	@Override
	public void operationAborted() {
		aborted = true;
	}

	public boolean checkValidate() {
		return true;
	}
}