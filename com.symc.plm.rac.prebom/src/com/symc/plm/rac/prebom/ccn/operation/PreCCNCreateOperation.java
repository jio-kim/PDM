package com.symc.plm.rac.prebom.ccn.operation;

import java.util.HashMap;
import java.util.Map;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.ccn.dialog.PreCCNCreateDialog;
import com.symc.plm.rac.prebom.common.CommonConstant;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
/**
 * [UPGRADE][20240308] CCN 생성 오류 수정
 *
 */
public class PreCCNCreateOperation extends AbstractAIFOperation{

    private HashMap<String, Object> ccnInfoMap;
    private TCSession session;
    private TCComponentItem ccnItem = null;
    private TCComponentFolder targetFolder = null;
    
    public PreCCNCreateOperation(HashMap<String, Object> dataMap, boolean answer) {
        this.ccnInfoMap = dataMap;
    }

    public PreCCNCreateOperation(PreCCNCreateDialog preCCNCreateDialog, String s7_PRECCNTYPE, HashMap<String, Object> paramMap, HashMap<String, Object> attrMap, Object object) {
        this.ccnInfoMap = attrMap;
    }

    @Override
    public void executeOperation() throws Exception {
        session = (TCSession) getSession();
        createCCNItem();
        
    }

    private void createCCNItem() throws Exception{
        Markpoint mp = new Markpoint(session);
        InterfaceAIFComponent[] comps = SDVPreBOMUtilities.getTargets();
        if (null != comps) {
            if (comps.length == 1 && comps[0] instanceof TCComponentFolder) {
                targetFolder = (TCComponentFolder) comps[0]; 
            }
        }
        try {
            String ccnID = SDVPreBOMUtilities.getCCNId(ccnInfoMap.get(PropertyConstant.ATTR_NAME_PROJCODE).toString(), ccnInfoMap.get(PropertyConstant.ATTR_NAME_SYSTEMCODE).toString());
            String desc = ccnInfoMap.get(PropertyConstant.ATTR_NAME_ITEMDESC).toString();
            
            //[UPGRADE][20240308] CCN 생성 오류 수정
            //ccnItem = CustomUtil.createItem(TypeConstant.S7_PRECCNTYPE, ccnID, CommonConstant.CCNINITREVISIONNO, ccnID, desc);
            
            desc = desc == null || desc.isEmpty() ?ccnID:desc;
			//Item Property 속성 입력
			Map<String, String> itemPropMap = new HashMap<>();
			Map<String, String> itemRevsionPropMap = new HashMap<>();
			itemPropMap.put(IPropertyName.ITEM_ID, ccnID);
			itemPropMap.put(IPropertyName.OBJECT_NAME, ccnID);
			itemPropMap.put(IPropertyName.OBJECT_DESC, desc);
			//Item Revision 속성 입력
			itemRevsionPropMap.put(IPropertyName.ITEM_REVISION_ID, CommonConstant.CCNINITREVISIONNO);
            
			//CCN Item 생성
			ccnItem = (TCComponentItem) CustomUtil.createItemObject(session, TypeConstant.S7_PRECCNTYPE, itemPropMap, itemRevsionPropMap);
            
            TCComponentItemRevision ccnRevision = ccnItem.getLatestItemRevision();
            
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_PROJCODE, (String) ccnInfoMap.get(PropertyConstant.ATTR_NAME_PROJCODE));
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_SYSTEMCODE, (String) ccnInfoMap.get(PropertyConstant.ATTR_NAME_SYSTEMCODE));
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_PROJECTTYPE, (String) ccnInfoMap.get(PropertyConstant.ATTR_NAME_PROJECTTYPE));
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_OSPECNO, (String) ccnInfoMap.get(PropertyConstant.ATTR_NAME_OSPECNO));
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_GATENO, (String) ccnInfoMap.get(PropertyConstant.ATTR_NAME_GATENO));
            
            String[] deployTargetArray = (String[]) ccnInfoMap.get(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET);
            if (null != deployTargetArray) {
                TCProperty referenceDeptCodeTCProperty = ccnRevision.getTCProperty(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET);
                referenceDeptCodeTCProperty.setStringValueArray(deployTargetArray);
            }
            
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_REGULATION, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_REGULATION));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_COSTDOWN, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_COSTDOWN));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_ORDERINGSPEC, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_ORDERINGSPEC));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_STYLINGUPDATE, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_STYLINGUPDATE));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_WEIGHTCHANGE));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_THEOTHERS, (boolean) ccnInfoMap.get(PropertyConstant.ATTR_NAME_THEOTHERS));
            
            if(targetFolder == null){
                session.getUser().getNewStuffFolder().add("contents", ccnItem);
            }else{
                targetFolder.add("contents", ccnItem);
                targetFolder.refresh();
            }
            NavigatorOpenService openService = new NavigatorOpenService();
            openService.open(ccnRevision);
        } catch (Exception ex) {
             mp.rollBack();
             throw ex;
        }
        mp.forget();
    }

}
