package com.symc.plm.me.utils;

import java.util.Vector;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;

/**
 * 용접 공법 구성과정에 Visulaization을 이용할때 Accumulated Part에 포함된 Part중에 용접점이 포함된 Part를
 * 표시되지 않도록 하는 기능이 필요해 구현하는 Class.
 * [NON-SR][20160218] taeku.jeong  
 * @author Taeku
 *
 */
public class WPartBlankUtility {
	
	CMEBOMTreeTable targetTreeTable = null;
	InterfaceAIFComponent[]  userSelectTargetComponents = null;
	Vector<TreeTableNode> expandForcedV = null;

	public WPartBlankUtility(){
		
		TCComponentBOMWindow selectedWindow = null;
    	MFGLegacyApplication mppApplication = null;
    	if(AIFUtility.getCurrentApplication()!=null && AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication){
    		mppApplication = (MFGLegacyApplication)AIFUtility.getCurrentApplication();
    		
    		this.userSelectTargetComponents = mppApplication.getTargetComponents();
    		if(userSelectTargetComponents!=null){
    			for (int i = 0; i < userSelectTargetComponents.length; i++) {
    				if(userSelectTargetComponents[i] instanceof TCComponentBOMLine){
    					try {
							selectedWindow = ((TCComponentBOMLine)userSelectTargetComponents[i]).window();
							if(selectedWindow!=null){
								break;
							}
						} catch (TCException e) {
							e.printStackTrace();
						}
    				}
				}
    		}
		}
    	
    	if(selectedWindow!=null){
    		AbstractViewableTreeTable[] viewableTreeTables = mppApplication.getViewableTreeTables();
    		for (int i = 0;viewableTreeTables!=null &&  i < viewableTreeTables.length; i++) {
    			
    			AbstractViewableTreeTable currentTreeTable = viewableTreeTables[i];
    			TCComponentBOMLine currentBOMLine = currentTreeTable.getBOMRoot();
    			if(currentBOMLine!=null){
    				TCComponentBOMWindow tempWindow = null;
					try {
						tempWindow = currentBOMLine.window();
					} catch (TCException e) {
						e.printStackTrace();
					}
    				if(tempWindow!=null && tempWindow.equals(selectedWindow)==true){
    					this.targetTreeTable = (CMEBOMTreeTable)currentTreeTable;
    					break;
    				}
    			}
    		}
    	}
    	
	}
	
	/**
	 * Graphics Panel에 Load된 대상중에 Itme Id가 W로 시작되는 Part를 찾아서 Graphics(Visulaizatoin)에
	 * 표시 되지 않도록 하는 처리를 수행한다.
	 * 이것은 용접공법 구성을 위해 Visulaizatoin에서 확인 하는 과정에 용접점이 보이지 않도록 조치후
	 * 필요한 용접점을 할당하는 작업을 수행하는데 필요한 기능임
	 * Visulaization에서는 Type별로 Filtering하는 기능이 있기는 하지만 Item Id를 기준으로 Filtering 하는
	 * 기능은 없는 것으로 보여서 작성함.
	 * [NON-SR][20160218] taeku.jeong 
	 */
	public void doBlankStartWithItemIdIsW(){
		
		if(this.targetTreeTable==null){
			return;
		}
		
		Vector<TCComponentBOMLine> wPartBOMLineV = new Vector<TCComponentBOMLine>();
		// Treetable의 펼쳐지거나 펼쳐져 보이지는 않지만 Viaulaizatoitn에 Load된 모든 Node를 List한다.
		TreeTableNode[] allNodes = this.targetTreeTable.getAllNodes(BOMLineNode.class);
		for (int i = 0;allNodes!=null && i < allNodes.length; i++) {
			if(allNodes[i]!=null && allNodes[i] instanceof BOMLineNode){
				boolean isChecked = allNodes[i].getChecked();
				TCComponentBOMLine tempBOMLine = ((BOMLineNode) allNodes[i]).getBOMLine();
				
				try {
					String itemId = tempBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					String itemType = tempBOMLine.getItem().getType();
					
					if(itemType!=null && itemType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_VEH_PART)==true){
						if(itemId.trim().toUpperCase().startsWith("W")==true){
							expend(allNodes[i]);
							wPartBOMLineV.add(tempBOMLine);
						}
					}
					
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}

		if(wPartBOMLineV!=null && wPartBOMLineV.size()>0){
			
			expandForcedV = new Vector<TreeTableNode>();
			
			TCComponentBOMLine[] targetBOMLines = new TCComponentBOMLine[wPartBOMLineV.size()];
			for (int i = 0; i < targetBOMLines.length; i++) {
				targetBOMLines[i] = wPartBOMLineV.get(i);
			}
			
			// Visualization에서 보이지 않도록 해야 할 대상들이 보이지 않도록 처리하다.
			targetTreeTable.blank(targetBOMLines);
			
			// 펼쳐진 Tree를 다시 접는다.
			collapseNodes();
		}
	}
	
	/**
	 * Tree가 펼쳐지 않은 상태에서는 Visulaization에 보이지 않도록 UnChecked 되지 않으므로 해당 Node는 펼쳐지도록 한다.
	 * @param node
	 */
	private void expend(TreeTableNode node){

		TreeTableNode parentNode = (TreeTableNode) node.getParent();
		boolean isParentNodeExpanded = parentNode.isNodeExpanded();
		if(isParentNodeExpanded==false){
			expend(parentNode);
		}else{
			node.expandNode();
			if(expandForcedV!=null && expandForcedV.contains(node)==false){
				expandForcedV.add(node);
			}
		}
	}
	
	private void collapseNodes(){
		
		// 전개방식을 벼경해서 지행 해봐도 접는것을 동작을 하지 않는다.
		// 생각에 아마도 Graphics에서 Blank Operation이 진행완료 되지 않아서 
		// 접는 Operation이 진행 되지 않는것으로 생각됨.
		
//			for (int i = expandForcedV.size();expandForcedV!=null && i > 0 ; i--) {
//				int indexNo = i-1;
//				TreeTableNode node = expandForcedV.get(indexNo);  
//				if(node!=null) {
//					node.collapseNode();
//				}
//			}
		
//		for (int i = 0;expandForcedV!=null && i < expandForcedV.size() ; i++) {
//			TreeTableNode node = expandForcedV.get(i);  
//			if(node!=null) {
//				node.collapseNode();
//			}
//		}
			expandForcedV.clear();
			expandForcedV = null;
	}
}
