package com.symc.plm.me.sdv.operation.ps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.rac.kernel.SYMCBOPEditData;
import com.kgm.common.WaitProgressBar;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;


/**
 * [SR141016-019][20141029]shcho, MECO EPL Load �Ϸ�� �˸�â ���� ERROR ���� INFORMATION���� ����.
 * [SR141029-008][20141029]shcho, Exception �߻��� progress Bar close ó�� �߰� (������ progress bar�� ��� �������� ���¿���)
 * 
 */
public class MEPLLoadOperation extends AbstractSDVActionOperation {

    private Registry registry = Registry.getRegistry(this);

    private WaitProgressBar progress;

    /**
     * symc.view.searchResultViewKO ���� ȣ�� �ȴ�.
     * @param actionId
     * @param ownerId
     * @param dataset
     */
    public MEPLLoadOperation(String actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void executeOperation() throws Exception {
        if(progress == null) {
            progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
        }
        progress.setWindowSize(500, 400);
        progress.start();
        progress.setShowButton(true);
        progress.setStatus("[" + new Date() + "]" + "MEPL Load start.");
        progress.setAlwaysOnTop(true);

        try {
            IDataSet dataset = getDataSet();
            List<HashMap<String, Object>> opList = null;

            if(dataset != null) {
                Collection<IDataMap> dataMaps = dataset.getAllDataMaps();

                if(dataMaps != null) {
                    for(IDataMap dataMap : dataMaps) {
                        if(dataMap.containsKey("targetOperationList")) {
                            opList = dataMap.getTableValue("targetOperationList");
                            break;
                        }
                    }
                }
            }

            if(opList == null) {
                progress.setStatus("[" + new Date() + "]" + "Target Operations is null.");
                return;
            }

            List<String> mecoList = new ArrayList<String>();
            for(HashMap<String, Object> operationMap : opList) {
                String mecoNo = (String) operationMap.get(SDVPropertyConstant.OPERATION_REV_MECO_NO);
                if(!mecoList.contains(mecoNo)) {
                    mecoList.add(mecoNo);
                }
            }

            progress.setStatus("[" + new Date() + "]" + "Authorization checking...");

            List<TCComponentItemRevision> mecoRevList = new ArrayList<TCComponentItemRevision>();
            String loginUserId = ((TCSession) AIFUtility.getDefaultSession()).getUser().getUserId();
            StringBuffer sb = new StringBuffer();
            for(String mecoNo : mecoList) {
                TCComponentItemRevision mecoRev = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM).getLatestItemRevision();
                mecoRevList.add(mecoRev);

                TCComponentUser user = (TCComponentUser) mecoRev.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
                if(!loginUserId.equals(user.getUserId())) {
                    sb.append(mecoNo + "\n");
                }
            }

            if(sb.length() > 0) {
                sb.insert(0, "The access is denied.\n");
                throw new Exception(sb.toString());
            }

            for(TCComponentItemRevision mecoRev : mecoRevList) {
                progress.setStatus("[" + new Date() + "]" + "Loading MEPL of " + mecoRev);

                CustomUtil cutomUtil = new CustomUtil();
                // MEPL ���� (MECO_EPL Table�� Data ����)
                ArrayList<SYMCBOPEditData> meplList = cutomUtil.buildMEPL((TCComponentChangeItemRevision) mecoRev, true);
                if(meplList == null){
                    throw new NullPointerException("Error occured on MEPL loading");
                }
            }
            progress.close();
            //[SR141016-019][20141029]shcho, MECO EPL Load �Ϸ�� �˸�â ���� ERROR ���� INFORMATION���� ����.
            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Load MEPL", MessageBox.INFORMATION);
        } catch(Exception e) {
            e.printStackTrace();
            progress.setStatus("[" + new Date() + "]" + "Error : " + e.getMessage());
            progress.close("Error", true, false); //[SR141029-008][20141029]shcho, Exception �߻��� progress Bar close ó�� �߰�
        }
    }

    @Override
    public void endOperation() {

    }

}
