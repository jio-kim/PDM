package com.kgm.soa.biz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kgm.soa.service.TcServiceManager;
import com.kgm.soa.util.TcConstants;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.services.strong.core._2008_06.DataManagement.BVROutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevisionOutput;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class TcWorkflowUtil {
    private Session tcSession = null;
    private TcServiceManager tcServiceManager;
    private TcItemUtil tcItemUtil = null;
   
    public static final String  RELEASE_STR_MSG = "The revision maturity is not a 'Released'.";
    
    public TcWorkflowUtil(Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);
        tcItemUtil = new TcItemUtil(this.tcSession);
    }
    
    /**
     * 
     * Desc : create a new process, need to input process title, process name (registered process)
     * @Method Name : createNewProcess
     * @param ModelObject[] revModels 
     * @param String processTitle 
     * @param String wfprocessName
     * @return LData
     * @throws NotLoadedException
     * @throws Exception
     * @Comment
     */
    protected String createNewProcess (ModelObject[] revModels, String processTitle, String wfprocessName) throws NotLoadedException, Exception {
        
        ContextData contextData = new ContextData();
        String observerKey = "";
        String name = processTitle; //"My SOA Do Task";
        String subject = "";
        String description = "";
        
        //==============================================
        int[] attType = new int[revModels.length];
        for(int i=0; i < revModels.length; i++) {
            
            attType[i] = TcEPM_attachement.target.value();
        }
        //=============================================
        String[] revUid = new String[revModels.length];
        for(int i=0; i<revModels.length; i++) {
            revUid[i] = revModels[i].getUid();
        }
        contextData.processTemplate = wfprocessName; //"SOA Do Task";
        contextData.subscribeToEvents = false;
        contextData.subscriptionEventCount = 0;
        contextData.attachmentCount = revModels.length;
        //==================================
        contextData.attachments = revUid;
        contextData.attachmentTypes = attType;
        //==================================

        InstanceInfo instanceInfo =  tcServiceManager.getWorkflowService().createInstance(true, observerKey, name, subject, description, contextData);
                //wfService.createInstance(false, observerKey,name, subject, description, contextData);
        
        if(tcServiceManager.getDataService().ServiceDataError(instanceInfo.serviceData))
        {
            throw new Exception(TcUtil.makeMessageOfFail(instanceInfo.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        } else {
            return instanceInfo.instanceKey;
        }
/*      
 *     << possible to make codes below script for another pattern. >>
        contextData.subscribeToEvents = false;
        contextData.subscriptionEventCount = 0;
        contextData.attachmentCount = 3;
        contextData.attachments = revUid;
        contextData.attachmentTypes = new int[]{TcEPM_attachement.target.value(),TcEPM_attachement.target.value(),TcEPM_attachement.target.value() }; 
        contextData.processTemplate = wfprocessName;
        
        InstanceInfo instanceInfo = tcServiceManager.getWorkflowService().createInstance(true, observerKey,name, subject, description, contextData);
                //wfService.createInstance(true,observerKey,name, subject, description, contextData);
        if(!tcServiceManager.getDataService().ServiceDataError(instanceInfo.serviceData))
        {
            System.out.println("New WorkFlow Instance:");
            System.out.println(" instanceKey: " +instanceInfo.instanceKey);
            
            return true;
        }
        return false;
*/        
    }
   
    /**
     * Create PSR
     * 
     * @method createPSRProcess 
     * @date 2013. 5. 8.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public String createPSRProcess (String revUid) throws NotLoadedException, Exception {         
        ItemRevision itemRev = tcItemUtil.getRevisionInfo(revUid);
        if(itemRev == null) {
            throw new Exception("TcWorkflowUtil.createPSRProcess : This revision does not exist.");
        }
        /*
        String maturity = StringUtil.nullToString(itemRev.getPropertyObject("s7_MATURITY").getStringValue());        
        if("".equals(maturity)) {
            throw new Exception("TcWorkflowUtil.createPSRProcess : Maturity in this revision is null.");
        } else if(!"Released".equals(maturity)) {
            throw new Exception("TcWorkflowUtil.createPSRProcess : " + RELEASE_STR_MSG);
        }
        */
        ModelObject[] attachModels = retrieveAttachObjects(new ItemRevision[] { itemRev });
        String instancePuid = createNewProcess(attachModels, "Self Release", "PSR");        
        // Revision의 s7_MATURITY를 'Released'로 변경
        Map<String, Object> updateMaturityMap = new HashMap<String, Object>();
        updateMaturityMap.put("s7_MATURITY", "Released");
        tcItemUtil.setAttributes(itemRev, updateMaturityMap);
        return instancePuid;
    }    
    
   /**
    * Create WorkFlow 
    * 
    * @method createNewProcess 
    * @date 2013. 5. 8.
    * @param
    * @return boolean
    * @exception
    * @throws
    * @see
    */
    public String createNewProcess (String revUid, String processTitle, String workflow) throws NotLoadedException, Exception {      
        ItemRevision itemRev = (ItemRevision)tcServiceManager.getDataService().loadModelObject(revUid); 
        ModelObject[] attachModels = retrieveAttachObjects(new ItemRevision[] { itemRev });
        return createNewProcess(attachModels, processTitle, workflow);
    } 
    
    public ModelObject[] retrieveAttachObjects (ItemRevision[] itemRevs) throws Exception {        
        ArrayList<Object> attachlist = new ArrayList<Object>();
        for(ItemRevision itemRevision : itemRevs) {           
            ServiceData serviceData = tcItemUtil.getProperties(new ModelObject[] {itemRevision}, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.PROP_ITEM_MASTER_TAG});
            if(tcServiceManager.getDataService().ServiceDataError(serviceData)){
                throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
            }
            RevisionOutput[] revisionOutput = tcItemUtil.getRevisionOutputFromItem(itemRevision.get_item_id(), itemRevision.get_item_revision_id(), itemRevision.getUid());
            DatasetOutput[] datasetOutput = tcItemUtil.getDatasetOutputFromRevisionOutput(revisionOutput[0]);
            attachlist.add(itemRevision);
            for(int i=0; i < datasetOutput.length; i++) {
                attachlist.add(datasetOutput[i].dataset);
            }
            BVROutput[] bvrOutputs = tcItemUtil.getBVROutputFromRevisionOutput(revisionOutput[0]); 
            for(int i=0; i < bvrOutputs.length; i++) {
                attachlist.add(bvrOutputs[i].bvr);
            }
        }
        int size = attachlist.size();
        ModelObject[] attachModels = new ModelObject[size];
        for(int j=0; j < size; j++) {
            attachModels[j] = (ModelObject)attachlist.get(j);
        }
        return attachModels;
    }    
}
