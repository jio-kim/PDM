package com.symc.plm.me.sdv.view.migration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.TreeColumnInfo;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;

public class NEWPEIFMigrationViewControll {

	private NEWPEIFMigrationViewPane peIFMigrationViewPane;
	private Tree tree;
	private Text logText;

	private LineItemData itemLineData;

	public NEWPEIFMigrationViewControll(NEWPEIFMigrationViewPane peIFMigrationViewPane) {

		this.peIFMigrationViewPane = peIFMigrationViewPane;
		this.tree = this.peIFMigrationViewPane.getTree();
		this.logText = this.peIFMigrationViewPane.getLogText();
	}

	public NEWPEIFMigrationViewPane getPeIFMigrationViewPane() {
		return peIFMigrationViewPane;
	}

	public void treeRemoveAll() {
		tree.removeAll();
	}

	public void createTreeColumn() {

		// Column 초기화
		while (tree != null && tree.getColumns() != null
				&& tree.getColumns().length > 0) {
			tree.getColumns()[0].dispose();
		}

		// Tree Column 생성
		// Column Info 설정.
		ArrayList<TreeColumnInfo> treeColumnInfoList = new ArrayList<TreeColumnInfo>();
		TreeColumnInfo objectIdInfo = new TreeColumnInfo();
		objectIdInfo.setId("object_id");
		objectIdInfo.setName("Object ID");
		objectIdInfo.setWidth(300);
		treeColumnInfoList.add(objectIdInfo);

		TreeColumnInfo classTypeInfo = new TreeColumnInfo();
		classTypeInfo.setId("class_type");
		classTypeInfo.setName("Class Type");
		classTypeInfo.setWidth(105);
		treeColumnInfoList.add(classTypeInfo);

		// TreeColumnInfo objectNameInfo = new TreeColumnInfo();
		// objectNameInfo.setId("object_name");
		// objectNameInfo.setName("Object Name");
		// objectNameInfo.setWidth(210);
		// treeColumnInfoList.add(objectNameInfo);

		for (int i = 0; i < treeColumnInfoList.size(); i++) {
			TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
			treeColumn.setData(treeColumnInfoList.get(i).getId());
			treeColumn.setWidth(treeColumnInfoList.get(i).getWidth());
			treeColumn.setText(treeColumnInfoList.get(i).getName());
		}
		// Status Column Add
		TreeColumn statusColumn = new TreeColumn(tree, SWT.NONE);
		statusColumn.setData("column_status_info");
		statusColumn.setWidth(400);
		statusColumn.setText("Status");
		tree.setHeaderVisible(true);

	}

	public LineItemData createLineItemData(String lineItemId,
			TCComponentBOMLine lineItemBOMLine) {
		itemLineData = new LineItemData(tree, 0,
				TCData.TC_TYPE_CLASS_NAME_LINE, tree.getColumns());
		itemLineData.setText(new String[] { lineItemId,
				itemLineData.getClassType() });
		itemLineData.setItemId(lineItemId);
		if (lineItemBOMLine != null) {
			itemLineData.setBopBomLine(lineItemBOMLine);
		}

		showTreeItem(itemLineData);

		return itemLineData;
	}

	public LineItemData getLineItemData() {
		return itemLineData;
	}

	public void setLineItemDataStatus(int nStatus, String statusMassage) {
		itemLineData.setStatus(nStatus, statusMassage);
		tree.update();
	}

	/**
	 * Line에 붙어있는 Operation BOMLine에 대응하는 OperationItemData Type 객체를 생성후 Return
	 * 한다.
	 * 
	 * @param operationBOMLine
	 * @param operationBOMLineNode
	 * @return
	 */
	public OperationItemData addOperationItemData(
			TCComponentBOMLine operationBOMLine, Node operationBOMLineNode,
			Node operationMasterNode) {

		OperationItemData tempOperationItemData = null;

		String operationItemId = null;
		// String tcfindNo = null;
		if (operationBOMLine != null) {
			try {
				operationItemId = operationBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				// tcfindNo =
				// operationBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if (operationBOMLineNode != null && operationItemId == null) {
			// Element Attribute 값을 읽는다.
			operationItemId = ((Element) operationBOMLineNode)
					.getAttribute("OperationItemId");
			// String elementText =
			// ((Element)currentOperationNode).getTextContent();
			// tcfindNo = getMatchedFirstChildNodeText("O",
			// (Element)nfOperationBOMLineNode);
		}

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return tempOperationItemData;
		}

		int currentItmeCount = itemLineData.getItemCount();

		tempOperationItemData = new OperationItemData(itemLineData,
				currentItmeCount, TCData.TC_TYPE_CLASS_NAME_OPERATION,
				tree.getColumns());
		tempOperationItemData.setText(new String[] { operationItemId,
				tempOperationItemData.getClassType() });
		tempOperationItemData.setItemId(operationItemId);
		if (operationBOMLine != null) {
			tempOperationItemData.setBopBomLine(operationBOMLine);
		}
		if (operationBOMLineNode != null) {
			tempOperationItemData.setBomLineNode(operationBOMLineNode);
		}
		if (operationMasterNode != null) {
			tempOperationItemData.setMasterDataNode(operationMasterNode);
		}

		showTreeItem(tempOperationItemData);

		return tempOperationItemData;
	}

	/**
	 * End Item Tree Node를 추가한다.
	 * 
	 * @param operationItemId
	 * @param endItemId
	 * @param absOccPuid
	 * @param findNo
	 */
	public EndItemData addEndItemItemData(OperationItemData operationItemData,
			TCComponentBOMLine bopEndItemBOMLine,
			TCComponentBOMLine productEndItemBOMLine, Node endItemBOMLineNode) {

		EndItemData endItemData = null;

		String tcEndItemId = null;
		String tcfindNo = null;
		String ebomcombinedAbsOccPuid = null;
		String ebomAbsOccPuid = null;

		if (bopEndItemBOMLine != null) {
			try {
				tcEndItemId = bopEndItemBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcfindNo = bopEndItemBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if (productEndItemBOMLine != null) {
			String productBOMLineParentOccThreadUid = null;
			ebomAbsOccPuid = NewPEIFExecution
					.getOccThreadUid(productEndItemBOMLine);
			try {
				if (productEndItemBOMLine.parent() != null) {
					productBOMLineParentOccThreadUid = NewPEIFExecution
							.getOccThreadUid(productEndItemBOMLine.parent());
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (productBOMLineParentOccThreadUid != null
					&& ebomAbsOccPuid != null) {
				ebomcombinedAbsOccPuid = productBOMLineParentOccThreadUid
						.trim() + "+" + ebomAbsOccPuid.trim();
			}
		}

		String nfEndItemId = null;
		String nfFindNo = null;
		String nfEbomcombinedAbsOccPuid = null;
		String nfEbomAbsOccPuid = null;

		if (endItemBOMLineNode != null) {

			if (((Element) endItemBOMLineNode).getElementsByTagName("K") != null) {
				nfEndItemId = ((Element) endItemBOMLineNode)
						.getElementsByTagName("K").item(0).getTextContent();
			}
			if (((Element) endItemBOMLineNode).getElementsByTagName("O") != null) {
				nfFindNo = ((Element) endItemBOMLineNode)
						.getElementsByTagName("O").item(0).getTextContent();
			}

			if (((Element) endItemBOMLineNode).getElementsByTagName("L") != null) {
				nfEbomcombinedAbsOccPuid = ((Element) endItemBOMLineNode)
						.getElementsByTagName("L").item(0).getTextContent();
			}
			if (((Element) endItemBOMLineNode).getElementsByTagName("M") != null) {
				nfEbomAbsOccPuid = ((Element) endItemBOMLineNode)
						.getElementsByTagName("M").item(0).getTextContent();
			}
		}

		String endItemId = null;
		if (tcEndItemId != null && tcEndItemId.trim().length() > 0) {
			endItemId = tcEndItemId.trim();
		} else if (nfEndItemId != null && nfEndItemId.trim().length() > 0) {
			endItemId = nfEndItemId.trim();
		}

		String findNo = null;
		if (tcfindNo != null && tcfindNo.trim().length() > 0) {
			findNo = tcfindNo.trim();
		} else if (nfFindNo != null && nfFindNo.trim().length() > 0) {
			findNo = nfFindNo.trim();
		}

		if (ebomcombinedAbsOccPuid == null
				|| (ebomcombinedAbsOccPuid != null && ebomcombinedAbsOccPuid
						.trim().length() < 0)) {
			if (nfEbomcombinedAbsOccPuid != null
					&& nfEbomcombinedAbsOccPuid.trim().length() > 0) {
				ebomcombinedAbsOccPuid = nfEbomcombinedAbsOccPuid.trim();
			}
		}
		if (ebomAbsOccPuid == null
				|| (ebomAbsOccPuid != null && ebomAbsOccPuid.trim().length() < 0)) {
			if (nfEbomAbsOccPuid != null
					&& nfEbomAbsOccPuid.trim().length() > 0) {
				ebomAbsOccPuid = nfEbomAbsOccPuid.trim();
			}
		}

		int currentItmeCount = operationItemData.getItemCount();

		endItemData = new EndItemData(operationItemData, currentItmeCount,
				TCData.TC_TYPE_CLASS_NAME_END_ITEM, tree.getColumns());
		endItemData.setText(new String[] { endItemId,
				endItemData.getClassType() });
		endItemData.setItemId(endItemId);
		if (bopEndItemBOMLine != null) {
			endItemData.setBopBomLine(bopEndItemBOMLine);
		}
		if (productEndItemBOMLine != null) {
			endItemData.setProductBomLine(productEndItemBOMLine);
		}
		if (endItemBOMLineNode != null) {

		}
		endItemData.setBomLineNode(endItemBOMLineNode);
		endItemData.setFindNo(findNo);
		endItemData.setAbsOccPuids(ebomcombinedAbsOccPuid);
		endItemData.setOccPuid(ebomAbsOccPuid);

		// endItemData.setOccPuid(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_OCCPUID_COLUMN_INDEX));
		// endItemData.setFunctionItemId(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_FUNCTION_PART_NO_COLUMN_INDEX));

		showTreeItem(endItemData);

		return endItemData;
	}

	/**
	 * Operation에 붙어있는 Tool BOMLine에 대응하는 ToolData Type 객체를 생성후 Return 한다.
	 * 
	 * @param operationItemData
	 * @param toolBOMLine
	 * @param toolBOMLineNode
	 * @return
	 */
	public ToolData addToolItemData(OperationItemData operationItemData,
			TCComponentBOMLine toolBOMLine, Node toolBOMLineNode,
			Node toolMasterNode) {

		ToolData toolData = null;

		String toolItemId = null;
		String tcFindNo = null;
		if (toolBOMLine != null) {
			try {
				toolItemId = toolBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcFindNo = toolBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if (toolBOMLineNode != null && (toolItemId == null || tcFindNo == null)) {
			if (((Element) toolBOMLineNode).getElementsByTagName("K") != null) {
				toolItemId = ((Element) toolBOMLineNode)
						.getElementsByTagName("K").item(0).getTextContent();
			}
			if (((Element) toolBOMLineNode).getElementsByTagName("M") != null) {
				tcFindNo = ((Element) toolBOMLineNode)
						.getElementsByTagName("M").item(0).getTextContent();
			}
		}

		int currentItmeCount = operationItemData.getItemCount();

		toolData = new ToolData(operationItemData, currentItmeCount,
				TCData.TC_TYPE_CLASS_NAME_TOOL, tree.getColumns());
		toolData.setText(new String[] { toolItemId, toolData.getClassType() });
		toolData.setItemId(toolItemId);
		if (toolBOMLine != null) {
			toolData.setBopBomLine(toolBOMLine);
		}
		toolData.setBomLineNode(toolBOMLineNode);
		toolData.setFindNo(tcFindNo);
		toolData.setMasterDataNode(toolMasterNode);

		// toolData.setData(PEExcelConstants.BOM, toolRowData);

		showTreeItem(toolData);

		return toolData;
	}

	/**
	 * Operation에 붙어있는 Equipment & Facility BOMLine에 대응하는 EquipmentData Type 객체를
	 * 생성후 Return 한다.
	 * 
	 * @param operationItemData
	 * @param equipmentBOMLine
	 * @param equipmentBOMLineNode
	 * @return
	 */
	public EquipmentData addEquipmentItemData(
			OperationItemData operationItemData,
			TCComponentBOMLine equipmentBOMLine, Node equipmentBOMLineNode,
			Node equipmentMasterNode) {

		EquipmentData equipmentData = null;

		String equipmentItemId = null;
		String tcFindNo = null;
		if (equipmentBOMLine != null) {
			try {
				equipmentItemId = equipmentBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcFindNo = equipmentBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if (equipmentBOMLineNode != null
				&& (equipmentItemId == null || tcFindNo == null)) {
			if (((Element) equipmentBOMLineNode).getElementsByTagName("K") != null) {
				equipmentItemId = ((Element) equipmentBOMLineNode)
						.getElementsByTagName("K").item(0).getTextContent();
			}
			if (((Element) equipmentBOMLineNode).getElementsByTagName("M") != null) {
				tcFindNo = ((Element) equipmentBOMLineNode)
						.getElementsByTagName("M").item(0).getTextContent();
			}
		}
		
		int currentItmeCount = operationItemData.getItemCount();

		equipmentData = new EquipmentData(operationItemData, currentItmeCount,
				TCData.TC_TYPE_CLASS_NAME_EQUIPMENT, tree.getColumns());
		equipmentData.setText(new String[] { equipmentItemId,
				equipmentData.getClassType() });
		equipmentData.setItemId(equipmentItemId);
		if (equipmentBOMLine != null) {
			equipmentData.setBopBomLine(equipmentBOMLine);
		}
		equipmentData.setBomLineNode(equipmentBOMLineNode);
		equipmentData.setFindNo(tcFindNo);

		if(equipmentMasterNode!=null){
			equipmentData.setMasterDataNode(equipmentMasterNode);
		}
		
		// equipmentData.setData(PEExcelConstants.BOM, equipmentRowData);

		showTreeItem(equipmentData);

		return equipmentData;
	}

	/**
	 * Operation에 붙어 있는 부자재 BOMLine에 대응하는 SubsidiaryData Type의 객체를 생성후 Return
	 * 한다.
	 * 
	 * @param operationItemData
	 * @param subsidiaryBOMLine
	 * @param subsidiaryBOMLineNode
	 * @return
	 */
	public SubsidiaryData addSubsidiaryItemData(
			OperationItemData operationItemData,
			TCComponentBOMLine subsidiaryBOMLine, Node subsidiaryBOMLineNode) {

		SubsidiaryData subsidiaryData = null;

		int currentItmeCount = operationItemData.getItemCount();

		String subsidiaryItemId = null;
		String tcFindNo = null;
		if (subsidiaryBOMLine != null) {
			try {
				subsidiaryItemId = subsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcFindNo = subsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if (subsidiaryBOMLineNode != null
				&& (subsidiaryItemId == null || tcFindNo == null)) {
			if (((Element) subsidiaryBOMLineNode).getElementsByTagName("K") != null) {
				subsidiaryItemId = ((Element) subsidiaryBOMLineNode)
						.getElementsByTagName("K").item(0).getTextContent();
			}
			if (((Element) subsidiaryBOMLineNode).getElementsByTagName("O") != null) {
				tcFindNo = ((Element) subsidiaryBOMLineNode)
						.getElementsByTagName("O").item(0).getTextContent();
			}
		}

		subsidiaryData = new SubsidiaryData(operationItemData,
				currentItmeCount, TCData.TC_TYPE_CLASS_NAME_SUBSIDIARY,
				tree.getColumns());
		subsidiaryData.setText(new String[] { subsidiaryItemId,
				subsidiaryData.getClassType() });
		subsidiaryData.setItemId(subsidiaryItemId);
		if (subsidiaryBOMLine != null) {
			subsidiaryData.setBopBomLine(subsidiaryBOMLine);
		}
		subsidiaryData.setBomLineNode(subsidiaryBOMLineNode);
		subsidiaryData.setFindNo(tcFindNo);

		showTreeItem(subsidiaryData);

		return subsidiaryData;
	}

	/**
	 * Activity가 Operation에 추가 될때 Operation에 직접 붙는것이 아니라 Activity Root에 해당하는
	 * ActivityMasterData Type의 객체를 Return 한다.
	 * 
	 * @param operationItemData
	 * @return
	 */
	public ActivityMasterData addActivityMasterData(
			OperationItemData operationItemData) {

		ActivityMasterData activityMasterData = null;

		if (operationItemData == null) {
			return activityMasterData;
		}

		String operationItemId = operationItemData.getItemId();

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return activityMasterData;
		}

		int currentItmeCount = operationItemData.getItemCount();

		activityMasterData = new ActivityMasterData(operationItemData,
				currentItmeCount, TCData.TC_TYPE_CLASS_NAME_ACTIVITY,
				tree.getColumns());
		activityMasterData.setText(new String[] { operationItemId,
				activityMasterData.getClassType() });

		showTreeItem(activityMasterData);

		return activityMasterData;
	}

	/**
	 * Activity Line 을 추가하고 ActivityLine에 해당 하는 ActivitySubData를 Return 한다.
	 * 
	 * @param operationItemId
	 * @param activityMasterData
	 * @param tcActivityComponent
	 * @param activityLineNode
	 * @param activityLineMasterNode
	 * @param tcActivitySeq
	 * @return
	 */
	public ActivitySubData addActivitySubData(String operationItemId,
			ActivityMasterData activityMasterData,
			TCComponentMEActivity tcActivityComponent, Node activityLineNode,
			Node activityLineMasterNode, String tcActivitySeq) {

		int activityCount = 0;

		if (activityMasterData != null) {
			activityCount = activityMasterData.getItemCount();
		}

		int activitySeqNo = -1;
		if (tcActivitySeq != null && tcActivitySeq.trim().length() > 0) {
			try {
				activitySeqNo = new Integer(tcActivitySeq).intValue();
			} catch (Exception e) {

			}
		}

		String activityName = null;
		if (tcActivityComponent != null) {
			try {
				activityName = tcActivityComponent
						.getProperty(SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if (activityLineMasterNode != null && activityName == null) {
			if (((Element) activityLineMasterNode).getElementsByTagName("J") != null) {
				activityName = ((Element) activityLineMasterNode)
						.getElementsByTagName("J").item(0).getTextContent();
			}
		}

		ActivitySubData activitySubData = new ActivitySubData(
				activityMasterData, activityCount,
				TCData.TC_TYPE_CLASS_NAME_ACTIVITY_SUB, tree.getColumns());
		if (activityName != null && activityName.trim().length() > 0) {
			activitySubData.setText(new String[] { activityName,
					activitySubData.getClassType() });
		} else {
			activitySubData.setText(new String[] { tcActivitySeq,
					activitySubData.getClassType() });
		}

		if (tcActivityComponent != null) {
			activitySubData.setActivityComponent(tcActivityComponent);
		}
		if (activityLineNode != null) {
			activitySubData.setBomLineNode(activityLineNode);
		}
		if (activityLineMasterNode != null) {
			activitySubData.setMasterDataNode(activityLineMasterNode);
		}

		if (activitySeqNo >= 0) {
			activitySubData.setActivitySeq(activitySeqNo);
		}

		showTreeItem(activitySubData);

		return activitySubData;
	}
	
	public void updateLogTextToFile(String filePath){
		
	    File zLogFile = new File(filePath);			

	    BufferedWriter out = null;
	    FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(zLogFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(fWriter!=null){
			out = new BufferedWriter(fWriter);
		}
	    
		if(out!=null){
			String text = logText.getText();
			try {
				out.write(text);
			} catch (IOException e) {
				e.printStackTrace();
			}	      
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		if(fWriter!=null){
			try {
				fWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void clearLogText() {
		logText.setText("");
		logText.update();
	}

	public void writeLogTextLine(String messageText) {
		final String messag = messageText + "\n";
		logText.append(messag);
		//logText.update();
		logText.redraw();
	}

	public void showTreeItem(TreeItem treeItem) {
		tree.showItem(treeItem);
		tree.update();
	}

	
	public void redrawUI() {
		peIFMigrationViewPane.update();
		peIFMigrationViewPane.redraw();
//		logText.update();
//		logText.redraw();
//		tree.update();
//		tree.redraw();
	}
}
