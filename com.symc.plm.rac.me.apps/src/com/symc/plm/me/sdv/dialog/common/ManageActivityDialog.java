/**
 * 
 */
package com.symc.plm.me.sdv.dialog.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.dialog.body.CreateBodyOPDialog;
import com.symc.plm.me.sdv.view.common.ManageActivityView;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR141219-020][20150108] shcho, Open with Time 창에서의 Activity 작업순서 불일치 및 순서 편집 불가 대응 신규 화면 추가
 */
public class ManageActivityDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(CreateBodyOPDialog.class);
    protected ManageActivityView manageActivityView;
    private ArrayList<HashMap<String, Object>> newTableList;

    /**
     * @param shell
     * @param dialogStub
     */
    public ManageActivityDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public ManageActivityDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean validationCheck() {
        try {
        	// [SR190104-050]Activity 저장 시간 단축을 위한 소스 수정 - 윤순식
        	
            IDataSet dataSet = getDataSetAll();

            // Activity가 Release 되어있음 수정하지 않는다.
            String ReleaseFlag = (String) dataSet.getValue("activityView", "ReleaseFlag");
            if (ReleaseFlag.equals("true")) {
                MessageBox.post(getShell(), "The activity has already been released.", "WARNING", MessageBox.WARNING);
                return false;
            }

            boolean changeFlag = false;
            Object oldDataMap = dataSet.getValue("activityView", "oldTableList");
            Object newDataMap = dataSet.getValue("activityView", "newTableList");
            ArrayList<HashMap<String, Object>> oldTableList = ((ArrayList<HashMap<String, Object>>) oldDataMap);
            newTableList = ((ArrayList<HashMap<String, Object>>) newDataMap);

            // String[] propertyNames = registry.getStringArray("table.column.search.id.body");

            // 변경사항 Check
            if (oldTableList.size() != newTableList.size()) {
                // Table Item 수가 다른 경우 변경 된 것임
                changeFlag = true;
            }
            
            for (int i = 0; i < newTableList.size(); i++) {
                HashMap<String, Object> newTableMap = newTableList.get(i);

                // 변경사항 Check2
                if (oldTableList.size() == newTableList.size()) {
                    HashMap<String, Object> oldTableMap = oldTableList.get(i);
                    for (String key : newTableMap.keySet()) {
                        // seq는 변경사항 체크에서 제외 한다.
                        if (key.equals("seq")) {
                            continue;
                        }

                        if (!newTableMap.get(key).equals(oldTableMap.get(key))) {
                            changeFlag = true;
                            break;
                        }
                    }
                }

                // Data 검증
                for (String key : newTableMap.keySet()) {
                    
                    // 필수 입력 속성 Check
                    if (key.equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME) || key.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                        String value = newTableMap.get(key).toString();
                        if (value == null || value.length() <= 0 || value.equals("0")) {
                            MessageBox.post(getShell(), key + " 속성 값은 필수 입니다.", "INFORMATION", MessageBox.INFORMATION);
                            return false;
                        }
                    }
                    
                    // m7_WORKERS 자리수 10자리 체크
                    if (key.equals(SDVPropertyConstant.ACTIVITY_WORKER)) {
                        if (newTableMap.get(key).toString().length() > 10) {
                            MessageBox.post(getShell(), "Workers(Body) 값은 10자리를 넘을 수 없습니다.", "INFORMATION", MessageBox.INFORMATION);
                            return false;
                        }
                    }

                    // //국문 Name이 변경된 경우 영문 Name을 Reset한다.(영문 작표 용)
                    // boolean objNameChange = true; //이름이 변경되면 true, 이름 변경이 없으면 false
                    // if (key.equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                    // String newObjectName = newTableMap.get(key).toString();
                    // for(HashMap<String, Object> oldTableMap2 : oldTableList) {
                    // for(String key2 : oldTableMap2.keySet()) {
                    // if(key2.equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                    // if(newObjectName.equals(oldTableMap2.get(key2).toString())) {
                    // objNameChange = false;
                    // break;
                    // };
                    // }
                    // }
                    // if(!objNameChange) {
                    // break;
                    // }
                    // }
                    // if(objNameChange) {
                    // newTableMap.put(SDVPropertyConstant.ACTIVITY_ENG_NAME, "");
                    // }
                    // }
                }
            }
            
            if (!changeFlag) {
                MessageBox.post(getShell(), "변경사항이 없습니다.", "INFORMATION", MessageBox.INFORMATION);
                return false;
            }
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    protected void applyPressed() {
        super.applyPressed();

        AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
        manageActivityView = (ManageActivityView) dialog.getView("activityView");
        manageActivityView.setOldTableList(newTableList);
    }

}
