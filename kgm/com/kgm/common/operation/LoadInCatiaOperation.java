package com.kgm.common.operation;

import com.ebsolutions.catiman.actions.LoadAssemblyAction;
import com.kgm.commands.bomviewer.BOMViewerConstants;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

/**
 * CATIA Load
 * @author jclee
 *
 */
public class LoadInCatiaOperation extends LoadAssemblyAction {
	TCComponentDataset dataset;
	TCComponentItemRevision revision;
	
	/**
	 * Constructor
	 * Revision�� ���õ� ��� CATProduct, CATPart, CATDrawing ������ �켱������ ���� CATIA Open
	 * @param revision
	 */
	public LoadInCatiaOperation(TCComponentItemRevision revision) {
		this.revision = revision;
		boolean isSelectedDataset = false;
		
		try {
			// Open �켱����
			AIFComponentContext[] children = revision.getChildren();
			// 1. CATProduct
			for (int inx = 0; inx < children.length; inx++) {
				InterfaceAIFComponent child = children[inx].getComponent();
				if (child instanceof TCComponentDataset) {
					TCComponentDataset dChild = (TCComponentDataset) child;
					
					if (dChild.getType().equals(BOMViewerConstants.TYPE_DATASET_CATPRODUCT)) {
						this.dataset = dChild;
						isSelectedDataset = true;
						break;
					}
				}
			}
			
			// 2. CATPart
			if (!isSelectedDataset) {
				for (int inx = 0; inx < children.length; inx++) {
					InterfaceAIFComponent child = children[inx].getComponent();
					if (child instanceof TCComponentDataset) {
						TCComponentDataset dChild = (TCComponentDataset) child;
						
						if (dChild.getType().equals(BOMViewerConstants.TYPE_DATASET_CATPART)) {
							this.dataset = dChild;
							isSelectedDataset = true;
							break;
						}
					}
				}
			}
			
			// 3. CATDrawing
			if (!isSelectedDataset) {
				for (int inx = 0; inx < children.length; inx++) {
					InterfaceAIFComponent child = children[inx].getComponent();
					if (child instanceof TCComponentDataset) {
						TCComponentDataset dChild = (TCComponentDataset) child;
						
						if (dChild.getType().equals(BOMViewerConstants.TYPE_DATASET_CATDRAWING)) {
							this.dataset = dChild;
							isSelectedDataset = true;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Constructor
	 * Revision, Dataset�� �����Ͽ� Open
	 * @param dataset
	 * @param revision
	 */
	public LoadInCatiaOperation(TCComponentDataset dataset, TCComponentItemRevision revision) {
		this.dataset = dataset;
		this.revision = revision;
	}
	
	/**
	 * Load �� ������ Component ����.
	 */
//	@Override ����(��μ�)
//	protected void initSelectedComponentInfo() {
//		set_seletcted_component(dataset);
//		set_selected_component_revision(revision);
//	}
}