package com.symc.plm.me.sdv.service.migration.work.peif;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCException;

public class IFExecutActivityMasterData extends IFExecutDefault {

	private ActivityMasterData activityMasterData;

	private boolean isNeedToBOMLineAdd = false;
	private boolean isNeedToBOMLineReplace = false;

	private TCComponentMEActivity newRootActivityComponent = null;

	public IFExecutActivityMasterData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean createOrUpdate(TCData activityMasterData) {
		
		this.activityMasterData = (ActivityMasterData) activityMasterData;
		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		super.createOrUpdate(activityMasterData);

		int changeType = this.activityMasterData.getDecidedChagneType();
		
		if (changeType == TCData.DECIDED_NO_CHANGE) {
			// 추가적인 처리 없이 Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			// 삭제 처리를 수행 한다.
			boolean haveRemoveException = false;
			try {
				removeTargetObject();
			} catch (TCException e) {
				e.printStackTrace();
				haveRemoveException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveRemoveException = true;
			}
			return !(haveRemoveException);
		} else if (changeType == TCData.DECIDED_ADD) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
			
			if(operationBOPLine!=null){
				try {
					createTargetObject();
				} catch (TCException e) {
					String message = "Activity Master Creation Error ["+operationItemId+"] : "+ e.getMessage();
					this.peIFExecution.writeLogTextLine(message);
					this.activityMasterData.setStatus(TCData.STATUS_ERROR, message);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}else{
				String message = "Activity Master Creation Error ["+operationItemId+"] -> BOPLine is null";
				this.peIFExecution.writeLogTextLine(message);
				this.activityMasterData.setStatus(TCData.STATUS_ERROR, message);
				return false;
			}
			
		}

		return true;
	}

	public void createTargetObject() throws Exception, TCException {
		
		this.peIFExecution.writeLogTextLine(""+operationItemId+" : CreateTarget");

		ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
		TCComponent root = operationBOPLine.getReferenceProperty("bl_me_activity_lines");
		if (root != null) {
			if (root instanceof TCComponentCfgActivityLine) {
				
				this.peIFExecution.writeLogTextLine(""+operationItemId+" : CreateTarget.Root = "+root.getUid());
				
//				TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) root);
//				for (TCComponent childLine : childLines) {
//					if (childLine instanceof TCComponentCfgActivityLine) {
//						childActivityList.add((TCComponentCfgActivityLine) childLine);
//					}
//				}
				
			}
		}

//		// 만약에 기존의 Activity가 존재하는 경우 기존의 Activity를 제거 한다.
//		for (int i = 0; 
//				childActivityList != null && i < childActivityList.size(); 
//				i++) {
//			
//			TCComponentCfgActivityLine meActivityLine = childActivityList.get(i);
//			TCComponentMECfgLine parentLine = meActivityLine.parent();
//			ActivityUtils.removeActivity(meActivityLine);
//			parentLine.save();
//		}

		// activity refresh
		refreshBOMLine(root);

		// Activity 생성
		ActivitySubData[] activitySubDataList = new ActivitySubData[activityMasterData.getItems().length];
		for (int i = 0; 
				activityMasterData.getItems() != null && i < activityMasterData.getItems().length;
				i++) {
			
			if (activityMasterData.getItems()[i] != null
					&& activityMasterData.getItems()[i] instanceof ActivitySubData) {
				
				activitySubDataList[i] = (ActivitySubData) activityMasterData.getItems()[i];
			}
		}

		for (int i = 0; i < activitySubDataList.length; i++) {
			
			ActivitySubData currentActivitySubData = activitySubDataList[i];
			
			Element activityLineNodeElement =(Element)currentActivitySubData.getBomLineNode();
			Element activityMasterAttributeElement = (Element) currentActivitySubData.getMasterDataNode();
			
			String subActivityKorName = null;
			if (activityMasterAttributeElement.getElementsByTagName("J") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("J")
						.getLength() > 0) {
					subActivityKorName = activityMasterAttributeElement
							.getElementsByTagName("J").item(0).getTextContent();
				}
			}
			subActivityKorName = getNullChangedStr(subActivityKorName);
			
			//----------------------------------------		
			// Sub Activity Line 생성
			//----------------------------------------		
			TCComponentCfgActivityLine activityLine = null;
			TCComponentMEActivity activity = null;
			
			TCComponent[] afterTCComponents = ActivityUtils.createActivitiesBelow(new TCComponent[] { root }, subActivityKorName);
			if (afterTCComponents != null && afterTCComponents.length > 0){
				activityLine = (TCComponentCfgActivityLine) afterTCComponents[0];
			}
			
			// Sub Activity Object 생성
			if(activityLine!=null){
				activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
			}
			
			//----------------------------------------			
			// Property 설정
			//----------------------------------------

			// Activity Time
			double timeSystemUnitTime = 0.0;
			if (activityMasterAttributeElement.getElementsByTagName("L") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("L")
						.getLength() > 0) {
					String tempStr = activityMasterAttributeElement
							.getElementsByTagName("L").item(0).getTextContent();
					timeSystemUnitTime = Double.parseDouble(tempStr);
				}
			}

			// Category
			String category = null;
			if (activityMasterAttributeElement.getElementsByTagName("M") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("M")
						.getLength() > 0) {
					category = activityMasterAttributeElement
							.getElementsByTagName("M").item(0).getTextContent();
				}
			}
			category = getNullChangedStr(category);

            // Category
            String categoryValue = ImportCoreService.getPEActivityCategolyLOV(category);


			// Work Code (SYSTEM Code)
			// 작업약어
			String workCode = null;
			if (activityMasterAttributeElement.getElementsByTagName("G") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("G").getLength() > 0) {
					workCode = activityMasterAttributeElement.getElementsByTagName("G").item(0).getTextContent();
				}
			}
			workCode = getNullChangedStr(workCode);
			
			// 변수
			String workCodeSub = null;
			if (activityMasterAttributeElement.getElementsByTagName("H") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("H").getLength() > 0) {
					workCodeSub = activityMasterAttributeElement.getElementsByTagName("H").item(0).getTextContent();
				}
			}
			workCodeSub = getNullChangedStr(workCodeSub);
			
			if (workCodeSub != null && workCodeSub.trim().length() > 0) {
				workCode = workCode + "-"+ workCodeSub.trim().replace(",", "-");
			}

			// 난이도.
			double timeSystemFrequency = 0.0;
			if (activityMasterAttributeElement.getElementsByTagName("I") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("I")
						.getLength() > 0) {
					String tempStr = activityMasterAttributeElement
							.getElementsByTagName("I").item(0).getTextContent();
					timeSystemFrequency = Double.parseDouble(tempStr);
				}
			}

			// KPC
			String kpcValue = null;
			if (activityMasterAttributeElement.getElementsByTagName("O") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("O")
						.getLength() > 0) {
					kpcValue = activityMasterAttributeElement
							.getElementsByTagName("O").item(0).getTextContent();
				}
			}
			kpcValue = getNullChangedStr(kpcValue);

			// KPC 관리기준
			String kpcManageStandard = null;
			if (activityMasterAttributeElement.getElementsByTagName("P") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("P")
						.getLength() > 0) {
					kpcManageStandard = activityMasterAttributeElement
							.getElementsByTagName("P").item(0).getTextContent();
				}
			}
			kpcManageStandard = getNullChangedStr(kpcManageStandard);

			String toolId = null;
			if (activityMasterAttributeElement.getElementsByTagName("N") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("N")
						.getLength() > 0) {
					toolId = activityMasterAttributeElement
							.getElementsByTagName("N").item(0).getTextContent();
				}
			}
			toolId = getNullChangedStr(toolId);
			
			String activityObjectName = activity.getProperty(SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
			if(activityObjectName!=null && activityObjectName.trim().startsWith("입력에러")==true){
				String message = "Activity Name Error! ["+operationItemId+" -> "+subActivityKorName+"]";
				this.peIFExecution.writeLogTextLine(message);
				currentActivitySubData.setStatus(TCData.STATUS_ERROR, message);
			}
			
			// Property 설정
			activity.getTCProperty(
					SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)
					.setDoubleValue(timeSystemUnitTime);
			
			if (categoryValue != null) {
				activity.getTCProperty(
						SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)
						.setStringValue(categoryValue);
			}
			
			if (workCode != null) {
				
				this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].workCode Set : "+workCode);
				
				activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE)
						.setStringValue(workCode);
			}

			activity.getTCProperty(
					SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY)
					.setDoubleValue(timeSystemFrequency);
			
			if (kpcValue != null) {
				activity.getTCProperty(
						SDVPropertyConstant.ACTIVITY_CONTROL_POINT)
						.setStringValue(kpcValue);
			}
			
			if (kpcManageStandard != null) {
				activity.getTCProperty(
						SDVPropertyConstant.ACTIVITY_CONTROL_BASIS)
						.setStringValue(kpcManageStandard);
			}

			// Activity 공구자원 할당
			if (StringUtils.isEmpty(toolId)==false) {
				String[] toolIds = toolId.split(",");
				HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = findAssignToolBOMLine(operationBOPLine, toolIds);
				
				for (String itemId : findedAssignToolBOMLine.keySet()) {
					TCComponentBOMLine bomLine = findedAssignToolBOMLine
							.get(itemId);
					if (bomLine != null) {
						activity.addReferenceTools(operationBOPLine,
								new TCComponentBOMLine[] { bomLine });
					}
				}
			}
			
			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"] XXXX ---->");

			activity.save();
		}
		
		root.save();

		try {
			operationBOPLine.setReferenceProperty("bl_me_activity_lines", root);	
		} catch (Exception e) {
			this.peIFExecution.writeLogTextLine("Error Set Activity Lines : "+e.getMessage());
		}
		operationBOPLine.save();
		operationBOPLine.window().save();
	}

	public void reviseTargetObject() throws Exception, TCException {
		// Activity는 Revise개념 없음.
	}

	public void removeTargetObject() throws Exception, TCException {
		
		this.peIFExecution.writeLogTextLine(""+operationItemId+" : RemoveTarget");

		ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
		TCComponent root = operationBOPLine
				.getReferenceProperty("bl_me_activity_lines");
		if (root != null) {
			
			this.peIFExecution.writeLogTextLine(""+operationItemId+" : RemoveTarget.Root = "+root.getUid());
			
			if (root instanceof TCComponentCfgActivityLine) {
				TCComponent[] childLines = ActivityUtils
						.getSortedActivityChildren((TCComponentCfgActivityLine) root);
				for (TCComponent childLine : childLines) {
					if (childLine instanceof TCComponentCfgActivityLine) {
						childActivityList
								.add((TCComponentCfgActivityLine) childLine);
					}
				}
				
				// 기존의 Activity를 제거 한다.
				for (int i = 0; childActivityList != null
						&& i < childActivityList.size(); i++) {
					TCComponentCfgActivityLine meActivityLine = childActivityList
							.get(i);
					TCComponentMECfgLine parentLine = meActivityLine.parent();
					ActivityUtils.removeActivity(meActivityLine);
					parentLine.save();
				}

				// activity refresh
				refreshBOMLine(root);
				
				//activity.save();
				root.save();
			}
		}

	}

	public void updateTargetObject() throws Exception, TCException {

		this.peIFExecution.writeLogTextLine(""+operationItemId+" : CreateTarget");

		ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
		TCComponent root = operationBOPLine.getReferenceProperty("bl_me_activity_lines");
		if (root != null) {
			if (root instanceof TCComponentCfgActivityLine) {
				
				this.peIFExecution.writeLogTextLine(""+operationItemId+" : CreateTarget.Root = "+root.getUid());
				
				TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) root);
				for (TCComponent childLine : childLines) {
					if (childLine instanceof TCComponentCfgActivityLine) {
						childActivityList.add((TCComponentCfgActivityLine) childLine);
					}
				}
			}
		}

		// 만약에 기존의 Activity가 존재하는 경우 기존의 Activity를 제거 한다.
		for (int i = 0; 
				childActivityList != null && i < childActivityList.size(); 
				i++) {
			
			TCComponentCfgActivityLine meActivityLine = childActivityList.get(i);
			TCComponentMECfgLine parentLine = meActivityLine.parent();
			ActivityUtils.removeActivity(meActivityLine);
			parentLine.save();
		}

		// activity refresh
		refreshBOMLine(root);

		// Activity 생성
		ActivitySubData[] activitySubDataList = new ActivitySubData[activityMasterData.getItems().length];
		for (int i = 0; 
				activityMasterData.getItems() != null && i < activityMasterData.getItems().length;
				i++) {
			
			if (activityMasterData.getItems()[i] != null
					&& activityMasterData.getItems()[i] instanceof ActivitySubData) {
				
				activitySubDataList[i] = (ActivitySubData) activityMasterData.getItems()[i];
			}
		}

		for (int i = 0; i < activitySubDataList.length; i++) {
			
			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"] ---->");

			ActivitySubData currentActivitySubData = activitySubDataList[i];
			
			Element activityLineNodeElement =(Element)currentActivitySubData.getBomLineNode();
			Element activityMasterAttributeElement = (Element) currentActivitySubData.getMasterDataNode();
			
			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].activityLineNodeElement : "+activityLineNodeElement);
			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].activityMasterAttributeElement : "+activityMasterAttributeElement);

			String subActivityKorName = null;
			if (activityMasterAttributeElement.getElementsByTagName("J") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("J")
						.getLength() > 0) {
					subActivityKorName = activityMasterAttributeElement
							.getElementsByTagName("J").item(0).getTextContent();
				}
			}
			if(subActivityKorName==null || (subActivityKorName!=null && subActivityKorName.trim().length()<1)){
				subActivityKorName = "";
			}

			// Activity Time
			double timeSystemUnitTime = 0.0;
			if (activityMasterAttributeElement.getElementsByTagName("L") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("L")
						.getLength() > 0) {
					String tempStr = activityMasterAttributeElement
							.getElementsByTagName("L").item(0).getTextContent();
					timeSystemUnitTime = Double.parseDouble(tempStr);
				}
			}

			// Category
			String category = null;
			if (activityMasterAttributeElement.getElementsByTagName("M") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("M")
						.getLength() > 0) {
					category = activityMasterAttributeElement
							.getElementsByTagName("M").item(0).getTextContent();
				}
			}
			if(category==null || (category!=null && category.trim().length()<1)){
				category = "";
			}
            // Category
            String categoryValue = ImportCoreService.getPEActivityCategolyLOV(category);


			// Work Code (SYSTEM Code)
			// 작업약어
			String workCode = null;
			if (activityMasterAttributeElement.getElementsByTagName("G") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("G")
						.getLength() > 0) {
					workCode = activityMasterAttributeElement
							.getElementsByTagName("G").item(0).getTextContent();
				}
			}
			if(workCode==null || (workCode!=null && workCode.trim().length()<1)){
				workCode = "";
			}
			
			// 변수
			String workCodeSub = null;
			if (activityMasterAttributeElement.getElementsByTagName("H") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("H")
						.getLength() > 0) {
					workCodeSub = activityMasterAttributeElement
							.getElementsByTagName("H").item(0).getTextContent();
				}
			}
			if(workCodeSub==null || (workCodeSub!=null && workCodeSub.trim().length()<1)){
				workCodeSub = "";
			}
			
			if (workCodeSub != null && workCodeSub.trim().length() > 0) {
				workCode = workCode + "-"
						+ workCodeSub.trim().replace(",", "-");
			}

			// 난이도.
			double timeSystemFrequency = 0.0;
			if (activityMasterAttributeElement.getElementsByTagName("I") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("I")
						.getLength() > 0) {
					String tempStr = activityMasterAttributeElement
							.getElementsByTagName("I").item(0).getTextContent();
					timeSystemFrequency = Double.parseDouble(tempStr);
				}
			}

			// KPC
			String kpcValue = null;
			if (activityMasterAttributeElement.getElementsByTagName("O") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("O")
						.getLength() > 0) {
					kpcValue = activityMasterAttributeElement
							.getElementsByTagName("O").item(0).getTextContent();
				}
			}
			if(kpcValue==null || (kpcValue!=null && kpcValue.trim().length()<1)){
				kpcValue = "";
			}

			// KPC 관리기준
			String kpcManageStandard = null;
			if (activityMasterAttributeElement.getElementsByTagName("P") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("P")
						.getLength() > 0) {
					kpcManageStandard = activityMasterAttributeElement
							.getElementsByTagName("P").item(0).getTextContent();
				}
			}
			if(kpcManageStandard==null || (kpcManageStandard!=null && kpcManageStandard.trim().length()<1)){
				kpcManageStandard = "";
			}

			String toolId = null;
			if (activityMasterAttributeElement.getElementsByTagName("N") != null) {
				if (activityMasterAttributeElement.getElementsByTagName("N")
						.getLength() > 0) {
					toolId = activityMasterAttributeElement
							.getElementsByTagName("N").item(0).getTextContent();
				}
			}
			if(toolId==null || (toolId!=null && toolId.trim().length()<1)){
				toolId = "";
			}
			
			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].subActivityKorName : "+subActivityKorName);

			TCComponentCfgActivityLine activityLine = null;
			TCComponent[] afterTCComponents = ActivityUtils.createActivitiesBelow(new TCComponent[] { root },subActivityKorName);
			if (afterTCComponents != null && afterTCComponents.length > 0){
				this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].afterTCComponents : "+afterTCComponents);	
				activityLine = (TCComponentCfgActivityLine) afterTCComponents[0];
			}

			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].activityLine : "+activityLine);
			
			// Sub Activity Object 생성
			TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();

			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"].activity : "+activity);
			
			// Property 설정
			activity.getTCProperty(
					SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)
					.setDoubleValue(timeSystemUnitTime);
			
			if (categoryValue != null) {
				activity.getTCProperty(
						SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)
						.setStringValue(categoryValue);
			}
			
			if (workCode != null) {
				activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE)
						.setStringValue(workCode);
			}

			activity.getTCProperty(
					SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY)
					.setDoubleValue(timeSystemFrequency);
			
			if (kpcValue != null) {
				activity.getTCProperty(
						SDVPropertyConstant.ACTIVITY_CONTROL_POINT)
						.setStringValue(kpcValue);
			}
			
			if (kpcManageStandard != null) {
				activity.getTCProperty(
						SDVPropertyConstant.ACTIVITY_CONTROL_BASIS)
						.setStringValue(kpcManageStandard);
			}

			// Activity 공구자원 할당
			if (StringUtils.isEmpty(toolId)) {
				activity.save();
				root.save();
				return;
			}
			
			String[] toolIds = toolId.split(",");
			HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = findAssignToolBOMLine(operationBOPLine, toolIds);
			
			for (String itemId : findedAssignToolBOMLine.keySet()) {
				TCComponentBOMLine bomLine = findedAssignToolBOMLine
						.get(itemId);
				if (bomLine != null) {
					activity.addReferenceTools(operationBOPLine,
							new TCComponentBOMLine[] { bomLine });
				}
			}
			
			this.peIFExecution.writeLogTextLine("activitySubDataList["+i+"] XXXX ---->");

			activity.save();
			root.save();

			operationBOPLine.setReferenceProperty("bl_me_activity_lines", root);
			operationBOPLine.save();
		}
		
	}

	/**
	 * 할당 대상 공구(Tool) 공법(Operation)에서 검색
	 * 
	 * @method findAssignToolBOMLine
	 * @date 2013. 12. 19.
	 * @param
	 * @return HashMap<String,TCComponentBOMLine>
	 * @exception
	 * @throws
	 * @see
	 */
	private HashMap<String, TCComponentBOMLine> findAssignToolBOMLine(
			TCComponentBOMLine operationBOMLine, String[] toolIds)
			throws Exception {
		HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = new HashMap<String, TCComponentBOMLine>();
		if (toolIds == null || toolIds.length == 0) {
			return findedAssignToolBOMLine;
		}
		// 초기화
		for (int i = 0; i < toolIds.length; i++) {
			toolIds[i] = (toolIds[i] == null) ? "" : toolIds[i].trim();
		}
		for (String toolId : toolIds) {
			findedAssignToolBOMLine.put(toolId, null);
		}
		TCComponentBOMLine[] childs = SDVBOPUtilities
				.getUnpackChildrenBOMLine(operationBOMLine);
		for (TCComponentBOMLine operationUnderBOMLine : childs) {
			String itemId = operationUnderBOMLine
					.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			// PE I/F 공구ID를 가지고 공법하위 공구 검색
			if (findedAssignToolBOMLine.containsKey(itemId)) {
				findedAssignToolBOMLine.put(itemId, operationUnderBOMLine);
			}
		}
		return findedAssignToolBOMLine;
	}

}
