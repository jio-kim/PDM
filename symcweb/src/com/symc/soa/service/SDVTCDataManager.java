package com.symc.soa.service;

import java.io.File;
import java.util.Map;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcDatasetUtil;
import com.symc.common.soa.biz.TcFileUtil;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcSessionUtil;
import com.symc.common.soa.util.TcConstants;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.loose.core.ICTService;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.Arg;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedInfo;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedOutput;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMPerformSignoffTask;
import com.teamcenter.soa.client.model.strong.EPMReviewTask;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.soaictstubs.TcUtility;

/**
 * [SR150522-029] [20150610] ymjang 결재 완료된 용접조건표의 작성일, 결재자 정보 표기 안됨.
 */
public class SDVTCDataManager {
    private DataManagementService dataManagementService;
    private TcItemUtil itemUtil;
    private TcDatasetUtil datasetUtil;
    private TcFileUtil fileUtil;
    private TcSessionUtil sessionUtil;

    public SDVTCDataManager(Session session) {
        dataManagementService = DataManagementService.getService(session.getConnection());

        this.itemUtil = new TcItemUtil(session);
        this.datasetUtil = new TcDatasetUtil(session);
        this.fileUtil = new TcFileUtil(session);
        this.sessionUtil = new TcSessionUtil(session);
    }

    public void setByPass() throws Exception {
        sessionUtil.setByPass();
    }

    public ItemRevision getLatestItemRevision(String itemId) throws Exception {
        Item item = itemUtil.getItem(itemId);
        ModelObject[] revisions = item.get_revision_list();
        if(revisions != null && revisions.length > 0) {
            return (ItemRevision) revisions[revisions.length - 1];
        }

        return null;
    }

    /**
     * 결재된 최신 리비전
     *
     * @method getReleasedLatestItemRevision
     * @date 2014. 2. 18.
     * @param
     * @return ItemRevision
     * @exception
     * @throws
     * @see
     */
    public ItemRevision getReleasedLatestItemRevision(String itemId) throws Exception {
        Item item = itemUtil.getItem(itemId);
        ModelObject[] revisions = item.get_revision_list();
        if(revisions != null && revisions.length > 0) {
            for(int i = revisions.length - 1; i >= 0; i--) {
                loadObjectWithProperties(revisions[i], new String[] {TcConstants.PROP_DATE_RELEASED});
                if(((ItemRevision)revisions[i]).get_date_released() != null) {
                    return (ItemRevision) revisions[i];
                }
            }
        }

        return null;
    }

    public void setProperty(ModelObject modelObject, Map<String, Object> propertyMap) throws Exception {
        itemUtil.setAttributes(modelObject, propertyMap);
    }

    public ModelObject[] getProcess(ItemRevision revision) throws NotLoadedException, ServiceException {
        revision = (ItemRevision) loadObjectWithProperties(revision, new String[] {TcConstants.PROP_PROCESS_STAGE_LIST});

        return revision.get_process_stage_list();
    }

    public ModelObject[] getTaskSignoffs(EPMTask process, String taskName) throws NotLoadedException, ServiceException {
        process = (EPMTask) loadObjectWithProperties(process, new String[] {TcConstants.PROP_PROCESS_CHILD_TASKS});

        ModelObject[] childTasks = process.get_child_tasks();
        if(childTasks != null) {
            for(ModelObject child : childTasks) {
                if(child instanceof EPMReviewTask) {
                    child = loadObjectWithProperties(child, new String[] {TcConstants.PROP_OBJECT_NAME, TcConstants.PROP_PROCESS_CHILD_TASKS});
                    if(taskName.equals(((EPMReviewTask) child).get_object_name())) {
                        ModelObject[] childrenOfReviewTask = ((EPMReviewTask) child).get_child_tasks();
                        for(ModelObject childOfReviewTask : childrenOfReviewTask) {
                            if(childOfReviewTask instanceof EPMPerformSignoffTask) {
                                childOfReviewTask = loadObjectWithProperties(childOfReviewTask, new String[] {TcConstants.PROP_PROCESS_VALID_SIGNOFFS});
                                return ((EPMPerformSignoffTask) childOfReviewTask).get_valid_signoffs();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public EPMPerformSignoffTask getPerformSignoffTask(EPMTask process, String reviewTaskName) throws ServiceException, NotLoadedException {
        process = (EPMTask) loadObjectWithProperties(process, new String[] {TcConstants.PROP_PROCESS_CHILD_TASKS});

        ModelObject[] childTasks = process.get_child_tasks();
        if(childTasks != null) {
            for(ModelObject child : childTasks) {
                if(child instanceof EPMReviewTask) {
                    child = loadObjectWithProperties(child, new String[] {TcConstants.PROP_OBJECT_NAME, TcConstants.PROP_PROCESS_CHILD_TASKS});
                    if(reviewTaskName.equals(((EPMReviewTask) child).get_object_name())) {
                        ModelObject[] childrenOfReviewTask = ((EPMReviewTask) child).get_child_tasks();
                        for(ModelObject childOfReviewTask : childrenOfReviewTask) {
                            if(childOfReviewTask instanceof EPMPerformSignoffTask) {
                                return (EPMPerformSignoffTask) childOfReviewTask;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    // TODOS 바꿨음. 확인 필요.
    public String[] getPreferenceStringArrayValue(String scope, String preferenceName) throws Exception {
//        ReturnedPreferences2[] preferences = sessionUtil.getPreferences2(scope, new String[] {preferenceName});
        CompletePreference[] preferences = sessionUtil.getPreference(scope, new String[] {preferenceName});
        if(preferences != null && preferences.length > 0) {
        	for (CompletePreference preference : preferences)
        		if (preference.definition.protectionScope.toUpperCase().equals(scope.toUpperCase()))
        			return preference.values.values;
        }

        return null;
    }

    public Dataset[] getAllDatasets(ItemRevision revision) throws Exception {
        return datasetUtil.retrieveDatasetObjects(revision);
    }

    public ImanFile[] getNamedReferenceFile(Dataset dataset, String namedRefType) throws Exception {
        return datasetUtil.getReferencedFileFromDataset(dataset.getUid(), new String[] {namedRefType});
    }

    public File getFiles(ImanFile imanFile) throws Exception {
        imanFile = (ImanFile) loadObjectWithProperties(imanFile, new String[] {TcConstants.PROP_TCFILE_NAME});
        String originalFileName = imanFile.get_original_file_name();

        File[] files = fileUtil.getFiles(new ModelObject[] {imanFile});
        if(files != null && files.length > 0) {
            String newFileName = files[0].getAbsolutePath();
            newFileName = newFileName.substring(0, newFileName.lastIndexOf(File.separator) + 1) + originalFileName;
            File file = new File(newFileName);
            if(file.exists()) {
                file.delete();
            }
            files[0].renameTo(file);

            return file;
        }

        return null;
    }

    public void removeNamedReferenceFromDataset(Dataset dataset, ImanFile file) throws Exception {
        datasetUtil.removeNamedReferenceFromDataset(dataset.getUid(), file.getUid());
    }

    public ModelObject uploadNamedReferenceFileToDataSet(Dataset dataset, String filePath, String fileName, boolean isText) throws Exception {
        return fileUtil.uploadNamedReferenceFileToDataSet(dataset.getUid(), filePath, fileName, isText);
    }

    public ModelObject loadObjectWithProperties(ModelObject modelObj, String[] propertyNames) throws ServiceException {
        ServiceData serviceData = dataManagementService.getProperties(new ModelObject[] {modelObj}, propertyNames);
        if(serviceData.sizeOfPartialErrors() > 0)
            throw new ServiceException("DataManagementService.getProperties returns a partial error - " + serviceData.getPartialError(0).getMessages()[0]);

        return serviceData.getPlainObject(0);
    }

    public WhereReferencedInfo[] getWhereReferenced(WorkspaceObject[] objects, int numLevels) throws Exception {
        WhereReferencedInfo[] info = null;

        WhereReferencedResponse response = dataManagementService.whereReferenced(objects, numLevels);
        if(response != null) {
            WhereReferencedOutput[] output = response.output;
            if(output != null && output.length > 0) {
                info = output[0].info;
            }
        }

        return info;
    }
    
    /**
     * [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰 
     * --> Save (TC 에서는 저장된 것 처럼 보이지만, DB 상에는 저장디어 있지 않음.
     * @method save 
     * @date 2015. 1. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void save(Connection connection, ModelObject model) throws ServiceException{

        ICTService service = ICTService.getService(connection);
        com.teamcenter.soa.client.model.Type type = model.getTypeObject();
        Arg[] args = new Arg[3];
        args[0] = TcUtility.createArg(type.getName());
        args[1] = TcUtility.createArg(type.getUid());
        args[2] = TcUtility.createArg(model.getUid());
        service.invokeICTMethod("ICCT", "save", args);
    }

    /**
     * [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰 
     * @method refresh 
     * @date 2015. 1. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void refresh(Connection connection, ModelObject model, int lockFlag) throws ServiceException{

        ICTService service = ICTService.getService(connection);
        com.teamcenter.soa.client.model.Type type = model.getTypeObject();
        Arg[] args = new Arg[4];
        args[0] = TcUtility.createArg(type.getName());
        args[1] = TcUtility.createArg(type.getUid());
        args[2] = TcUtility.createArg(model.getUid());
        args[3] = TcUtility.createArg(lockFlag);
        service.invokeICTMethod("ICCT", "refresh", args);
    }
    
    /**
     * 해당 Item Revision 의 Workflow Task 의 RootTask 를 구한다.
     * [SR150522-029] [20150610] ymjang 결재 완료된 용접조건표의 작성일, 결재자 정보 표기 안됨.
     * @param revision
     * @param prefValues
     * @return
     * @throws Exception 
     */
    public EPMTask getEPMTask(String item_Id, ItemRevision revision, String[] prefValues) throws Exception {

    	boolean isFoundProcess = false;
    	EPMTask process = null;
    	
        WhereReferencedInfo[]  whereReferencedInfos = getWhereReferenced(new WorkspaceObject[] {revision}, 1);
        for( int i = 0; i < whereReferencedInfos.length; i++){
            
            if( whereReferencedInfos[i].referencer instanceof EPMTask)
            {
                isFoundProcess = true;
                process = (EPMTask) whereReferencedInfos[i].referencer;
                process = (EPMTask) loadObjectWithProperties(process, new String[] {TcConstants.PROP_OBJECT_NAME});
                String processName = process.get_object_name();
                
                for (String prefValue : prefValues) {
                    if(prefValue.equals(processName)) {
                        isFoundProcess = true;
                        break;
                    }
                }
                if (isFoundProcess)
                	break;
            }
        }

        if(!isFoundProcess) {
            throw new Exception(item_Id + " 에 결재 프로세스가 존재하지 않습니다.");
        }
        
        return process;
    }

    
}
