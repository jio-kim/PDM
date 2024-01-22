package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.body.CreateWeldOPView;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;

/**
 * 
 * [SR140702-044][20140702] jwLee 용접공법 ID체계 변경 (1. LOV추가, 2. Serial No.체계 변경, 3. 용접공법 중복 검사 소스 이동)
 * [SR150524-002][20150730] shcho, 용접공법 생성작업 시간 과다 소요 개선 
 */
public class CreateWeldOPInitOperation extends AbstractSDVInitOperation {

    protected boolean applyFlag;
    // [SR151211-006][20151224] taeku.jeong 용접공법을 연속으로 생성하는 Apply Button을 이용시 TargetOperation이 변경 되는 오류 해결
    private TCComponentBOPLine initedOperationBOPLine;

	public CreateWeldOPInitOperation() {
        super();
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            // 선택한 공정 BOPLine 을 가져온다.
        	TCComponentBOPLine operationBOPLine = initedOperationBOPLine;
        	// [SR151211-006][20151224] taeku.jeong 용접공법을 연속으로 생성하는 Apply Button을 이용시 TargetOperation이 변경 되는 오류 해결
        	// UI가 초기 인식 될때 선택된 Operation BOPLine을 활용하도록 설정.
        	if(initedOperationBOPLine==null){
        		MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        		operationBOPLine = (TCComponentBOPLine) mfgApp.getSelectedBOMLines()[0];
        	}
            TCComponentBOPLine stationBOPLine = (TCComponentBOPLine) operationBOPLine.parent();
            RawDataMap targetDataMap = new RawDataMap();

            // MECO ItemRevision 수집
            TCComponentItemRevision mecoItemRevision = null;
            String operationID = null;
            TCProperty[] tcProperty = operationBOPLine.getItemRevision().getTCProperties(new String[] { SDVPropertyConstant.OPERATION_REV_MECO_NO, SDVPropertyConstant.ITEM_ITEM_ID });
            if (tcProperty != null && tcProperty.length > 0) {
                mecoItemRevision = (TCComponentItemRevision) (tcProperty[0].getReferenceValue());
                operationID = tcProperty[1].getStringValue();
            }

            // 공법 하위 WorkArea 및 Gun 리스트 수집
            List<TCComponentBOPLine> workAreaList = new ArrayList<TCComponentBOPLine>();
            List<TCComponentBOPLine> gunBOPLineList = new ArrayList<TCComponentBOPLine>();
            getBelowBOPLines(operationBOPLine, workAreaList, gunBOPLineList);

            // Gun 을 사용하는 용접공법을 알아보고 용접공법에 SerealNO 를 가져온다
            String weldOPSerealNo = "01";
            weldOPSerealNo = getWeldOpSerealNo(stationBOPLine, operationID);

            targetDataMap.put("stationBOPLine", stationBOPLine, IData.OBJECT_FIELD); // TODO : View 및 Action에서 처리시 SDVTypeConstant.BOP_PROCESS_STATION_ITEM인 것들을 StationBOPLine 로 변경 필요
            targetDataMap.put("operationBOPLine", operationBOPLine, IData.OBJECT_FIELD); // TODO : View 및 Action에서 처리시 opList(LIST_FIELD)인 것들을 OperationBOPLine (OBJECT_FIELD)로 변경 필요
            targetDataMap.put("mecoItemRevision", mecoItemRevision, IData.OBJECT_FIELD); // TODO : View 및 Action에서 처리시 mecoList(LIST_FIELD)인 것들을 MECOItemRevision (OBJECT_FIELD)로 변경 필요, 그리고 MECORevision이 null인경우에 대한 null처리 추가
            targetDataMap.put(SDVPropertyConstant.STATION_IS_ALTBOP, stationBOPLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP), IData.STRING_FIELD);
            targetDataMap.put(SDVPropertyConstant.STATION_ALT_PREFIX, stationBOPLine.getProperty(SDVPropertyConstant.STATION_ALT_PREFIX), IData.STRING_FIELD);
            targetDataMap.put("gunBOPLineList", gunBOPLineList, IData.LIST_FIELD); // TODO : View 및 Action에서 처리시 operationID (LIST_FIELD) 인 것들을 gunBOPLineList로 변경 필요
            targetDataMap.put("weldOPSerealNo", weldOPSerealNo, IData.STRING_FIELD); // TODO : View 및 Action에서 처리시 operationID + "-gun" (OBJECT_FIELD)인 것들을 weldOPSerealNo (STRING_FIELD)로 변경 필요
            targetDataMap.put("workAreaList", workAreaList, IData.LIST_FIELD);// TODO : View 및 Action에서 처리시 operationID + "-workArea" 인 것들을 workAreaList로 변경 필요

            DataSet targetDataset = new DataSet();
            targetDataset.addDataMap("WeldOPViewInit", targetDataMap);

            setData(targetDataset);

            // apply 일때만 실행
            if (applyFlag != false) {
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
                CreateWeldOPView weldView = (CreateWeldOPView) dialog.getView("createWeldOP");
                weldView.setApplyDataSet(targetDataset);
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    private String getWeldOpSerealNo(TCComponentBOPLine stationBOMLine, String operationID) throws Exception {
        String nextSerialNo = "01";

        TCProperty tcProperty = stationBOMLine.getTCProperty("bl_rev_ps_children");
        String[] propValues = tcProperty.getStringDelimitedValues(",");
        HashSet<String> weldOPNoSet = new HashSet<String>();
        for (String displayableChildID : propValues) {
            if (displayableChildID.contains(operationID + "-WEOP")) {
            	// [NON_SR] [20151104] taeku.joeng 윤순식 차장님 요청으로 문제점 파악후 수정
            	// 이미 생성된 Weld Operatoin의 순번을 Index하기위한 Data를 만드는과정에 불필요한
            	// 문자열이 함께있어서 오류가 발생됨
            	String tempStr = displayableChildID.split("-")[5];
            	if(tempStr.indexOf("/")>=0){
            		tempStr = tempStr.substring(0, tempStr.indexOf("/"));
            	}
                weldOPNoSet.add(tempStr);
            }
        }

        if (weldOPNoSet.isEmpty()) {
            return nextSerialNo;
        }
        String[] arrWeldOpNo = weldOPNoSet.toArray(new String[weldOPNoSet.size()]);
        Arrays.sort(arrWeldOpNo);

        for (int i = 0; i < arrWeldOpNo.length; i++) {
            int serialNO = i + 1;
            String strWeldOPNo = arrWeldOpNo[i];
            int weldOPNo = Integer.parseInt(strWeldOPNo);
            
            if (serialNO == weldOPNo) {
                nextSerialNo = String.format("%02d", serialNO + 1);
                continue;
            } else {
                break;
            }
        }

        return nextSerialNo;
    }

    /**
     * 
     * 
     * @param gunList
     * @method getGunLengthList
     * @date 2014. 6. 23.
     * @param
     * @return
     * @return List<Integer>
     * @exception
     * @throws
     * @see
     */
    private void getBelowBOPLines(TCComponentBOPLine operationBOPLine, List<TCComponentBOPLine> workAreaList, List<TCComponentBOPLine> gunBOPLineList) throws Exception {
        TCProperty tcProperty = operationBOPLine.getTCProperty("Mfg0assigned_workarea");
        TCComponent[] tcComponents = tcProperty.getReferenceValueArray();
        for (TCComponent tcComponent : tcComponents) {
            TCComponentBOPLine assignedWorkArea = (TCComponentBOPLine) tcComponent;
            AIFComponentContext[] workAreaChildren = assignedWorkArea.getChildren();
            for (AIFComponentContext workAreaChildComp : workAreaChildren) {
                TCComponentBOPLine resourceBOPLine = (TCComponentBOPLine) workAreaChildComp.getComponent();
                if (resourceBOPLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM)) {
                    workAreaList.add(assignedWorkArea);
                    gunBOPLineList.add(resourceBOPLine);
                }
            }
        }
    }

    public void applyAction(boolean flag) throws Exception {
        this.applyFlag = flag;
        executeOperation();
    }
    
    /**
     * [SR151211-006][20151224] taeku.jeong 용접공법을 연속으로 생성하는 Apply Button을 이용시 TargetOperation이 변경 되는 오류 해결 
     * @return
     */
    public TCComponentBOPLine getInitedOperationBOPLine() {
		return initedOperationBOPLine;
	}

    /**
     * [SR151211-006][20151224] taeku.jeong 용접공법을 연속으로 생성하는 Apply Button을 이용시 TargetOperation이 변경 되는 오류 해결
     * @param initedOperationBOPLine
     */
	public void setInitedOperationBOPLine(TCComponentBOPLine initedOperationBOPLine) {
		this.initedOperationBOPLine = initedOperationBOPLine;
	}

}
