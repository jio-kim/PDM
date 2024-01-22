package com.symc.plm.me.sdv.operation.common;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;
import com.teamcenter.rac.util.Registry;

/**
 * [SR150122-027][20150210] shcho, Clear automatically replaced end item (공법 할당 E/Item의 설계 DPV에 의한 자동 변경 오류 해결) (10버전에서 개발 된 소스 9으로 이식함)
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * 
 */
public class ClearReplacedEndItemOperation extends AbstractTCSDVOperation {

	private Registry registry;
	private TCComponentBOPLine target;
	
	public ClearReplacedEndItemOperation() {
		registry = Registry.getRegistry("com.symc.plm.me.sdv.operation.common.common");
	}
	
	@Override
	public void startOperation(String commandId) {
		
	}

	@Override
	public void endOperation() {
		
	}

	@Override
	public void executeOperation() throws Exception {
		BOMLineNode.setPropertyLoading(false);
		
		InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
		target = (TCComponentBOPLine) selectedTargets[0];
		
		BOMTreeTable[] treeTables = getTables();
		for(BOMTreeTable table : treeTables) {
			if(table != null) {
				clearBackgroundColor(table);
				table.refreshLineBgColors();
			}
		}

		BOMLineNode.setPropertyLoading(true);
	}
	
	/**
	 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
	 * 
	 * @return
	 * @throws Exception
	 */
	private BOMTreeTable[] getTables() throws Exception {
		BOMTreeTable[] tables = new BOMTreeTable[2];

		MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
		tables[0] = application.getViewableTreeTable();

		TCComponentBOPLine topBopLine = (TCComponentBOPLine) target.window().getTopBOMLine();
		// [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
		TCComponent mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topBopLine.getItemRevision());
				
		if(mProductRevision == null) {
			throw new Exception(registry.getString("NotExistMProduct.MSG"));
		}
		
		AbstractViewableTreeTable[] treeTables = application.getViewableTreeTables();
		for(AbstractViewableTreeTable treeTable : treeTables) {
			if(treeTable instanceof CMEBOMTreeTable) {
				if(mProductRevision.equals(treeTable.getRootBOMLineNode().getBOMLine().getItemRevision())) {
					tables[1] = (BOMTreeTable) treeTable;
					break;
				}
			}
		}
	
		return tables;
	}
	
	private void clearBackgroundColor(AbstractViewableTreeTable table) {
		TreeTableNode[] allNodes = table.getAllNodes(BOMLineNode.class);
		if(allNodes != null) {
			for(TreeTableNode node : allNodes) {
				((BOMLineNode) node).setBackgroundColor(null);
			}
		}
	}

}
