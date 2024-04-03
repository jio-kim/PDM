package com.ssangyong.soa.service.sdv;

import java.io.File;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.biz.TcDatasetUtil;
import com.ssangyong.soa.biz.TcFileUtil;
import com.ssangyong.soa.biz.TcItemUtil;
import com.ssangyong.soa.biz.TcSessionUtil;
import com.ssangyong.soa.util.TcConstants;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMPerformSignoffTask;
import com.teamcenter.soa.client.model.strong.EPMReviewTask;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class SDVTCDataManager {
    private DataManagementService dataManagementService;
    private TcItemUtil itemUtil;
    private TcDatasetUtil datasetUtil;
    private TcFileUtil fileUtil;
    private TcSessionUtil sessionUtil;

    public SDVTCDataManager(Session session) {
        dataManagementService = DataManagementService.getService(Session.getConnection());

        this.itemUtil = new TcItemUtil(session);
        this.datasetUtil = new TcDatasetUtil(session);
        this.fileUtil = new TcFileUtil(session);
        this.sessionUtil = new TcSessionUtil(session);
    }

    public ItemRevision getLatestItemRevision(String itemId) throws Exception {
        Item item = itemUtil.getItem(itemId);
        ModelObject[] revisions = item.get_revision_list();
        if(revisions != null && revisions.length > 0) {
            return (ItemRevision) revisions[revisions.length - 1];
        }

        return null;
    }

    public ModelObject[] getProcess(ItemRevision revision) throws NotLoadedException, ServiceException {
        // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체 
//        revision = (ItemRevision) loadObjectWithProperties(revision, new String[] {TcConstants.PROP_PROCESS_STAGE_LIST});
        revision = (ItemRevision) loadObjectWithProperties(revision, new String[] {TcConstants.PROP_STARTED_WORKFLOW_TASKS});

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

//    public String[] getPreferenceStringArrayValue(String scope, String preferenceName) throws Exception {
//        ReturnedPreferences2[] preferences = sessionUtil.getPreferences2(scope, new String[] {preferenceName});
//        if(preferences != null && preferences.length > 0) {
//            return preferences[0].values;
//        }
//
//        return null;
//    }

    public String[] getPreferenceStringArrayValue(String scope, String preferenceName) throws Exception
    {
    	CompletePreference[] prefs = sessionUtil.getPreference(scope, new String[]{preferenceName});
    	if (prefs != null && prefs.length > 0)
    	{
    		return prefs[0].values.values;
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
            files[0].renameTo(file);

            return file;
        }

        return null;
    }

    public void removeNamedReferenceFromDataset(Dataset dataset, ImanFile file) throws Exception {
        datasetUtil.removeNamedReferenceFromDataset(dataset.getUid(), file.getUid());
    }

    public void uploadNamedReferenceFileToDataSet(Dataset dataset, String filePath, String fileName, boolean isText) throws Exception {
        fileUtil.uploadNamedReferenceFileToDataSet(dataset.getUid(), filePath, fileName, isText);
    }

    public ModelObject loadObjectWithProperties(ModelObject modelObj, String[] propertyNames) throws ServiceException {
        ServiceData serviceData = dataManagementService.getProperties(new ModelObject[] {modelObj}, propertyNames);
        if(serviceData.sizeOfPartialErrors() > 0)
            throw new ServiceException("DataManagementService.getProperties returns a partial error - " + serviceData.getPartialError(0).getMessages()[0]);

        return serviceData.getPlainObject(0);
    }

}
