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
	 * ������.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 8. 10.
	 * @param dialog
	 */
	public NewDatasetOperation(NewDatasetDialog dialog) {
//		super(dialog, "�����ͼ� ���� ��...");
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
//					MessageBox.post(dialog, "������ �Ǵ� ������ ������, ������ ���� �� ��� �Ͻʽÿ�.", "�˸�", MessageBox.INFORMATION);
					MessageBox.post(dialog, registry.getString("newDataset.MESSAGE.NoSelected"), registry.getString("newDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return;
				}
			}
		}else{
			flag = true;
//			MessageBox.post(dialog, "����� �ϳ��� ���� �� �� �ٽ� �����Ͽ� �ֽʽÿ�.", "�˸�", MessageBox.INFORMATION);
			MessageBox.post(dialog, registry.getString("newDataset.MESSAGE.MultiSelected"), registry.getString("newDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
		}
	}

	/**
	 * ���� ���� ���� �� DataSet ���� �� ó������.
	 * @Copyright : S-PALM
	 * @author : �ǻ��
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
			/** ���� ÷�� */
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
