package com.ssangyong.soa.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.biz.TcSessionUtil;
import com.ssangyong.soa.common.constants.PropertyConstant;
import com.ssangyong.soa.common.constants.TcConstants;
import com.ssangyong.soa.common.constants.TcMessage;
import com.ssangyong.soa.tcservice.TcServiceManager;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2007_06.DataManagement.RelationAndTypesFilter;
import com.teamcenter.services.strong.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
import com.teamcenter.services.strong.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;


public class TcUtil {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    /**
     *
     * Desc : change from TimeZone to GMT
     * @Method Name : getDate
     * @param Calendar cal
     * @return String
     * @Comment
     */
    public static String getDate(Calendar cal) {
        Calendar cal_timezone = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
        DATE_FORMATTER.setCalendar(cal_timezone);
        return DATE_FORMATTER.format(cal.getTime());
    }

    /**
     *
     * Desc : return current system's time (TimeZone)
     * @Method Name : getCurrentTime
     * @return String
     * @Comment
     */
    public static String getCurrentTime() {
        long time = System.currentTimeMillis();
        String str = DATE_FORMATTER.format(new Date(time));

        return str;
    }

    /**
     *
     * Desc : return map is transformed into VecStruct.
     * @Method Name : transStringToVecStruct
     * @param Map<String, Object> mapProp
     * @return HashMap<String, Object>
     * @Comment
     */
    public static HashMap<String, Object> transStringToVecStruct (Map<String, Object> mapProp) {

        HashMap<String, Object> hashmap = new HashMap<String, Object>();
        Object oKey[] = mapProp.keySet().toArray();
        Object oValue[] = mapProp.values().toArray();
        VecStruct vsValue = null;

        for(int i = 0; i < mapProp.size(); i++) {
            vsValue = new VecStruct();
            vsValue.stringVec = new String[] {oValue[i].toString()};
            hashmap.put(oKey[i].toString(), vsValue);
        }
        return hashmap;
    }

    /**
     *
     * Desc : filter specific object type  with the filterType in the ModelObject Array.
     * @Method Name : filterTypeModelObject
     * @param modelComp
     * @param filterType
     * @return ModelObject[]
     * @throws Exception
     * @Comment
     */
    public ModelObject[] filterTypeModelObject(ModelObject[] modelComp, String filterType) throws Exception {
        ModelObject[] filterModel = new ModelObject[modelComp.length];

        tcServiceManager.getDataService().getProperties(modelComp, new String[]{PropertyConstant.ATTR_NAME_ITEMTYPE});
        int idx=0;
        for(ModelObject filterComp : modelComp) {

           if(filterType.equals(filterComp.getTypeObject().getName())) {
               filterModel[idx] = filterComp;
               idx++;
           }
        }
        return filterModel;
    }


    /**
     *
     * Desc : filter specific object type  with the filterType Array in the ModelObject Array.
     * @Method Name : filterTypeModelObject
     * @param modelComp
     * @param filterTypes
     * @return WorkspaceObject[]
     * @throws Exception
     * @Comment
     */
    public WorkspaceObject[] filterTypeModelObject(ModelObject[] modelComp, String[] filterTypes) throws Exception {
        WorkspaceObject[] filterModel = new WorkspaceObject[modelComp.length];

        tcServiceManager.getDataService().getProperties(modelComp, new String[]{PropertyConstant.ATTR_NAME_ITEMTYPE});
        int filter_idx=0;
        for(ModelObject filterComp : modelComp) {

           for(String filterType : filterTypes) {
               if(filterType.equals(filterComp.getTypeObject().getName())) {
                   filterModel[filter_idx] = (WorkspaceObject)filterComp;
                   filter_idx++;
               }
           }
        }
        return filterModel;
    }

    /**
     *
     * Desc : return truncated extension of the origin file name
     * @Method Name : truncateFileExt
     * @param String origin_file_name
     * @return String
     * @Comment
     */
    public static String truncateFileExt(String origin_file_name) {
        String file_ext = "";
        file_ext = origin_file_name.substring(origin_file_name.lastIndexOf(".")+1, origin_file_name.length());
        return file_ext;
    }

    /**
     *
     * Desc : return status of workflow's process.
     * @Method Name : getProcessStatus
     * @param WorkspaceObject processWork
     * @return String
     * @throws Exception
     * @Comment
     */
    public String getProcessStatus(WorkspaceObject processWork) throws Exception {
        String rel_status ="";
//        tcServiceManager.getDataService().getProperties(processWork.get, new String[]{TcConstants.PROP_RELEASE_STATUS_LIST});
//        ReleaseStatus[] releaStatus = new ReleaseStatus[processWork.get_release_status_list().length];
//        releaStatus = processWork.get_release_status_list();
//        tcServiceManager.getDataService().getProperties(releaStatus, new String[]{TcConstants.PROP_NAME});
//        if(releaStatus !=null && releaStatus.length > 0) {
//            rel_status = releaStatus[processWork.get_release_status_list().length-1].get_name();
//        }else{
//            rel_status = TcConstants.TC_PROCESS_WORKING;
//        }

        return rel_status;
    }

    /**
     *
     * Desc : delete file after checking exist file.
     * @Method Name : deletelocalFile
     * @param String file_path
     * @return boolean
     * @Comment
     */
    public static boolean deletelocalFile(String file_path) {
        boolean rslt = false;
        File local_File = new File(file_path);
        if(local_File.isFile()) {
            rslt = local_File.delete();
        }

        return rslt;
    }

    /**
     *
     * Desc : return type name of modelobject
     * @Method Name : getTypeNameFromObject
     * @param String object_puid
     * @return String
     * @throws Exception
     * @Comment
     */
    public  String getTypeNameFromObject(String object_puid) throws Exception {
        ModelObject modelComps = tcServiceManager.getDataService().loadModelObject(object_puid);

        return modelComps.getTypeObject().getName();
    }

    /**
     *
     * Desc : return type name of namedreference depending on the type of dataset
     * @Method Name : getReferenceTypeFromDataset
     * @param ModelObject modelComp
     * @return String
     * @Comment
     */
    public static String getReferenceTypeFromDataset(ModelObject modelComp) {

        String referenceName = null;
        String typeName = modelComp.getTypeObject().getName();

        if(TcConstants.TYPE_DATASET_PDF.equals(typeName)) {
            return referenceName = TcConstants.TYPE_NR_PDF;
        }else if(TcConstants.TYPE_DATASET_CATDRAWING.equals(typeName)) {
            return referenceName = TcConstants.TYPE_NR_CATDRAWING;
        }else if(TcConstants.TYPE_DATASET_CATPART.equals(typeName)) {
            return referenceName = TcConstants.TYPE_NR_CATPART;
        }else if(TcConstants.TYPE_DATASET_CATPRODUCT.equals(typeName)) {
            return referenceName = TcConstants.TYPE_NR_CATPRODUCT;
        }else if(TcConstants.TYPE_DATASET_CATIA.equals(typeName)) {
            return referenceName = TcConstants.TYPE_NR_CATIA;
        }else if(TcConstants.TYPE_DATASET_EXCEL_2007.equals(typeName)) {
            return referenceName = TcConstants.TYPE_NR_EXCEL_2007;
        }

        return referenceName;

    }
    private Session tcSession = null;
    private TcServiceManager tcServiceManager;
    public TcUtil(Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);
    }

    /**
     *
     * Desc : expand related models is based on primary object
     * @Method Name : getRelatedObjectForPrimary
     * @param ModelObject[] modelComp
     * @param String[] otherSideObjectTypes
     * @param String relationTypeName
     * @return ExpandGRMRelationsResponse2
     * @throws Exception
     * @Comment
     */
    public ExpandGRMRelationsResponse2 getRelatedObjectForPrimary(ModelObject[] modelComp, String[] otherSideObjectTypes, String relationTypeName) throws Exception {

        RelationAndTypesFilter typeFilter = new RelationAndTypesFilter();
        typeFilter.otherSideObjectTypes = otherSideObjectTypes;
        typeFilter.relationTypeName = relationTypeName;

        ExpandGRMRelationsPref2 relationPref = new ExpandGRMRelationsPref2();
        relationPref.expItemRev = false;
        relationPref.info = new RelationAndTypesFilter[]{typeFilter};

        ExpandGRMRelationsResponse2 relationResp = tcServiceManager.getDataService().expandGRMRelationsForPrimary(modelComp, relationPref);

        return relationResp;
    }

    /**
     *
     * Desc : expand related models is based on secondary object
     * @Method Name : getRelatedObjectForSecondary
     * @param ModelObject[] modelComp
     * @param String[] otherSideObjectTypes
     * @param String relationTypeName
     * @return ExpandGRMRelationsResponse2
     * @throws Exception
     * @Comment
     */
    public ExpandGRMRelationsResponse2 getRelatedObjectForSecondary(ModelObject[] modelComp, String[] otherSideObjectTypes, String relationTypeName) throws Exception {

        RelationAndTypesFilter typeFilter = new RelationAndTypesFilter();
        typeFilter.otherSideObjectTypes = otherSideObjectTypes;
        typeFilter.relationTypeName = relationTypeName;

        ExpandGRMRelationsPref2 relationPref = new ExpandGRMRelationsPref2();
        relationPref.expItemRev = false;
        relationPref.info = new RelationAndTypesFilter[]{typeFilter};

        ExpandGRMRelationsResponse2 relationResp = tcServiceManager.getDataService().expandGRMRelationsForSecondary(modelComp, relationPref);

        return relationResp;
    }

    /**
     *
     * Desc : retrieve realted object on specific object as the single relation_type
     * @Method Name : getRelatedObjectFromModelObject
     * @param ModelObject modelComps
     * @param String relation_type
     * @return ModelObject[]
     * @throws Exception
     * @Comment
     */
    public ModelObject[] getRelatedObjectFromModelObject (ModelObject modelComps, String relation_type) throws Exception {

        tcServiceManager.getDataService().getProperties(new ModelObject[]{modelComps}, new String[] {relation_type});
        ModelObject[] relatedComps = modelComps.getPropertyObject(relation_type).getModelObjectArrayValue();

        return relatedComps;
    }


    /**
     *
     * Desc : get list being used type of "named reference".
     * @Method Name : getNamedReferenceTemplateList
     * @return String[]
     * @throws Exception
     * @Comment
     *  If it will create NamedReference Type, add "TYPE" on below the method.
     */
    public static String[] getNamedReferenceTemplateList () throws Exception {
        String ref_names[] = {TcConstants.TYPE_NR_UGMASTER, TcConstants.TYPE_NR_UGPART, TcConstants.TYPE_NR_PDF, TcConstants.TYPE_NR_IMAGE};
        return ref_names;
    }


    /**
     *
     * Desc : make error message for notifying to client when is returned exception from ServiceData.
     * @Method Name : makeMessageOfFail
     * @param ServiceData serviceData
     * @return HashMap
     * @Comment
     */
    public static HashMap<String, Object> makeMessageOfFail(ServiceData serviceData) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i< serviceData.sizeOfPartialErrors(); i++) {
            ErrorStack errorStack = serviceData.getPartialError(i);
            ErrorValue[] errorValues =errorStack.getErrorValues();

            for(ErrorValue errorValue : errorValues){
                sb.append("[TC_ERR_CODE]: "+errorValue.getCode()+"\n");
                sb.append("[TC_ERR_LEV]: "+errorValue.getLevel()+"\n");
                sb.append("[TC_ERR_MSG]: "+errorValue.getMessage()+"\n");
            }
        }

        HashMap<String, Object> ldata = new HashMap<String, Object>();
        ldata.put(TcMessage.TC_RETURN_MESSAGE, TcMessage.TC_RETURN_FAIL);
        ldata.put(TcMessage.TC_RETURN_FAIL_REASON, sb.toString());

        return ldata;
    }

    /**
     *
     * Desc : make error message for notifying to client when is returned exception from ServiceData.
     *        Specially is enable to process ServiceData of array type.
     * @Method Name : makeMessageOfFail
     * @param ArrayList servicelist
     * @return HashMap
     * @Comment
     */
    public static HashMap<String, Object> makeMessageOfFail(ArrayList<ServiceData> servicelist) {
        StringBuffer sb = new StringBuffer();
        for(int k=0; k < servicelist.size(); k++) {
            ServiceData serviceData = (ServiceData)servicelist.get(k);

            for(int i=0; i< serviceData.sizeOfPartialErrors(); i++) {
                ErrorStack errorStack = serviceData.getPartialError(i);
                ErrorValue[] errorValues =errorStack.getErrorValues();

                for(ErrorValue errorValue : errorValues){
                    sb.append("[TC_ERR_CODE]: "+errorValue.getCode()+"\n");
                    sb.append("[TC_ERR_LEV]: "+errorValue.getLevel()+"\n");
                    sb.append("[TC_ERR_MSG]: "+errorValue.getMessage()+"\n");
                }
                sb.append("-------------------------------------------------"+"\n");
            }

        }


        HashMap<String, Object> ldata = new HashMap<String, Object>();
        ldata.put(TcMessage.TC_RETURN_MESSAGE, TcMessage.TC_RETURN_FAIL);
        ldata.put(TcMessage.TC_RETURN_FAIL_REASON, sb.toString());

        return ldata;
    }

    /**
     *
     * Desc : make a error message as parameter.
     * @Method Name : returnMessageOfFail
     * @param String message
     * @return HashMap
     * @Comment
     */
    public static HashMap<String, Object> returnMessageOfFail(String message) {

        HashMap<String, Object> ldata = new HashMap<String, Object>();
        ldata.put(TcMessage.TC_RETURN_MESSAGE, TcMessage.TC_RETURN_FAIL);
        ldata.put(TcMessage.TC_RETURN_FAIL_REASON, message);

        return ldata;
    }

    /**
     *
     * Desc : return success message.
     * @Method Name : makeMessageOfSucess
     * @return HashMap
     * @Comment
     */
    public static HashMap<String, Object> makeMessageOfSucess() {
        HashMap<String, Object> ldata = new HashMap<String, Object>();
        ldata.put(TcMessage.TC_RETURN_MESSAGE, TcMessage.TC_RETURN_OK);

        return ldata;
    }


    /**
     *
     * Desc : return result that compare specific object' owner to logging user
     * @Method Name : compareUserOfModel
     * @param String uid
     * @return boolean
     * @throws Exception
     * @Comment
     */
    public boolean compareUserOfModel(String uid) throws Exception {
        ModelObject modelComp = tcServiceManager.getDataService().loadModelObject(uid);
        tcServiceManager.getDataService().getProperties(new ModelObject[] {modelComp}, new String[] { PropertyConstant.ATTR_NAME_OWNINGUSER });
        TcSessionUtil tcSessionUtil = new TcSessionUtil(tcSession);
        ModelObject[] users = new ModelObject[2];
        users[0] = (User)modelComp.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGUSER).getModelObjectValue();
        users[1] = tcSessionUtil.getUser();
        tcServiceManager.getDataService().getProperties(users, new String[] { PropertyConstant.ATTR_NAME_USERID });
        if( ((User)users[1]).get_user_id().equals( ((User)users[0]).get_user_id())) {
            return true;
        }
        return false;
    }

    /**
     *
     * Desc : return generated DataSet name as type of DataSet.
     * @Method Name : getDatasetNaming
     * @param String item_id
     * @param String item_rev_id
     * @param String dataset_type
     * @return String
     * @Comment
     */
    public static String getDatasetNaming(String item_id, String item_rev_id, String dataset_type){

        String tmpName = item_id;
        if(TcConstants.TYPE_DATASET_UGMASTER.equals(dataset_type)){
            tmpName = tmpName+"-"+item_rev_id+TcConstants.SUFFIX_UGMASTER_ID;
        }else if(TcConstants.TYPE_DATASET_UGPART.equals(dataset_type)){
            tmpName = tmpName+TcConstants.SUFFIX_UGPART_ID;
        }else if(TcConstants.TYPE_DATASET_PDF.equals(dataset_type)){
            tmpName = tmpName+"-"+item_rev_id+TcConstants.SUFFIX_PDF_ID;
        }

        return tmpName;
    }


}
