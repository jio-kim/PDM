package com.kgm.commands.newdataset;

import com.kgm.common.SYMCClass;
import com.kgm.common.attachfile.AttachFilePanel;
import com.kgm.common.operation.SYMCAWTAbstractCreateOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class NewDatasetOperation extends SYMCAWTAbstractCreateOperation {

	@SuppressWarnings("unused")
    private NewDatasetDialog dialog;
	private AttachFilePanel attachFilePanel;
	private InterfaceAIFComponent[] components;
	private TCComponent comp;
	private String relation;
	private boolean flag;
	private AIFComponentContext[] parents;
	private String pseudoFolderName;
	
	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 8. 10.
	 * @param dialog
	 */
	public NewDatasetOperation(NewDatasetDialog dialog) {
//		super(dialog, "데이터셋 생성 중...");
		super(dialog, "Dataset is creating...");
		this.dialog = dialog;
		this.attachFilePanel = dialog.getAttachFilePanel();
		components = AIFUtility.getCurrentApplication().getTargetComponents();
		Registry registry = Registry.getRegistry(this);
		if(components.length == 1){
			comp = (TCComponent) components[0];
			System.out.println("comp : "+comp.getType());
			if(comp.getType().equals("PseudoFolder")){
				pseudoFolderName = comp.toString();
				createDataSetPseudoFolder();
			}else{
				if(comp instanceof TCComponentItem){
//					relation = SYMCClass.REFERENCE_REL;
					relation = SYMCClass.RELATED_DWG_REL;
				}else if(comp instanceof TCComponentItemRevision){
//					relation = SYMCClass.REFERENCE_REL;
					relation = SYMCClass.RELATED_DWG_REL;
				}else if(comp instanceof TCComponentFolder){
//					relation = SYMCClass.CONTENT_REL;
					relation = SYMCClass.RELATED_DWG_REL;
				}else{
					flag = true;
//					MessageBox.post(dialog, "아이템 또는 아이템 리비젼, 폴더를 선택 후 등록 하십시오.", "알림", MessageBox.INFORMATION);
					MessageBox.post(dialog, registry.getString("newDataset.MESSAGE.NoSelected"), registry.getString("newDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return;
				}
			}
		}else{
			flag = true;
//			MessageBox.post(dialog, "대상을 하나만 선택 한 후 다시 실행하여 주십시오.", "알림", MessageBox.INFORMATION);
			MessageBox.post(dialog, registry.getString("newDataset.MESSAGE.MultiSelected"), registry.getString("newDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
		}
	}

	/**
	 * 가상 폴더 선택 후 DataSet 생성 시 처리사항.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 8. 10.
	 */
	private void createDataSetPseudoFolder() {
		relation = "";
		try{
			parents = comp.whereReferenced();
			int parentsSize = parents.length;
			
			for(int k=0; k<parentsSize; k++){
				TCComponent comp2 = (TCComponent) parents[k].getComponent();
				if(comp2 instanceof TCComponentItemRevision){
					this.comp = comp2;
					TCPropertyDescriptor[] dist = comp.getPasteRelations();
					for(int zd=0; zd<dist.length; zd++){
						String dispName = dist[zd].getDisplayName();
						if(pseudoFolderName.equals(dispName)){
							relation = dist[zd].getName();
							break;
						}
					}
				}
				if(comp2 instanceof TCComponentItem){
					this.comp = comp2;
					TCPropertyDescriptor[] dist = comp.getPasteRelations();
					for(int zd=0; zd<dist.length; zd++){
						String dispName = dist[zd].getDisplayName();
						if(pseudoFolderName.equals(dispName)){
							relation = dist[zd].getName();
							break;
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createItem() throws Exception {
	}

	@Override
	public void endOperation() throws Exception {
		if(!flag){
			/** 파일 첨부 */
			attachFilePanel.setRelationType(relation);
			attachFilePanel.attachOperation(comp);
				
//			dialog.closeDialog();
		}
	}

	@Override
	public void setProperties() throws Exception {
	}

	@Override
	public void startOperation() throws Exception {
	}
}
