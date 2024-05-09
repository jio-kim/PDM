package com.kgm.soa.biz;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.kgm.soa.common.constants.PropertyConstant;
import com.kgm.soa.common.constants.TcConstants;
import com.kgm.soa.common.constants.TcMessage;
import com.kgm.soa.tcservice.TcServiceManager;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.services.loose.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.strong.core._2008_06.DataManagement.BVROutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetProperties2;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevisionOutput;
import com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.GetFileResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * Desc :
 * 
 * @author yunjae.jung
 */
public class TcFileUtil {

    private ServiceData serviceData;
    private TcServiceManager tcServiceManager;
    private TcItemUtil tcItemUtil;
    private TcDatasetUtil tcDatasetUtil;

    public TcFileUtil(Session tcSession) {
        tcItemUtil = new TcItemUtil(tcSession);
        tcDatasetUtil = new TcDatasetUtil(tcSession);
        tcServiceManager = new TcServiceManager(tcSession);
    }
    
    /**
     * 
     * Desc : get file object via FMS
     * 
     * @Method Name : getFiles
     * @param ModelObject
     *            [] fileComp
     * @return File[]
     * @throws FileNotFoundException
     * @throws UnknownHostException
     * @throws Exception
     * @Comment
     */
    public File[] getFiles(ModelObject[] fileComp) throws FileNotFoundException, UnknownHostException, Exception {
        if (fileComp == null || fileComp.length == 0) {
            return null;
        }
        File[] file = null;
        FileManagementUtility fMSFileManagement = null;
        GetFileResponse getfileResponse = null;

        try {
            fMSFileManagement = tcServiceManager.getFileService().newUtility();
            getfileResponse = fMSFileManagement.getFiles(fileComp);

            int cnt = getfileResponse.sizeOfFiles();
            if (cnt > 0) {
                file = new File[cnt];
                for (int i = 0; i < cnt; i++) {
                    file[i] = getfileResponse.getFile(i);
                }

            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fMSFileManagement != null) {
                fMSFileManagement.term();
            }
        }
        return file;
    }

    public ModelObject[] retrieveAttachObjects(ItemRevision[] itemRevs) throws Exception {
        ArrayList<Object> attachlist = new ArrayList<Object>();
        for (ItemRevision itemRevision : itemRevs) {
            tcServiceManager.getDataService().getProperties(new ModelObject[] { itemRevision }, new String[] { PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_ITEMREVID, PropertyConstant.ATTR_NAME_ITEM_MASTER_TAG });
            RevisionOutput[] revisionOutput = tcItemUtil.getRevisionOutputFromItem(itemRevision.get_item_id(), itemRevision.get_item_revision_id(), itemRevision.getUid());
            DatasetOutput[] datasetOutput = tcDatasetUtil.getDatasetOutputFromRevisionOutput(revisionOutput[0]);

            attachlist.add(itemRevision);
            for (int i = 0; i < datasetOutput.length; i++) {
                attachlist.add(datasetOutput[i].dataset);
            }
            BVROutput[] bvrOutputs = tcItemUtil.getBVROutputFromRevisionOutput(revisionOutput[0]);
            for (int i = 0; i < bvrOutputs.length; i++) {
                attachlist.add(bvrOutputs[i].bvr);
            }
        }

        int size = attachlist.size();
        ModelObject[] attachModels = new ModelObject[size];
        for (int j = 0; j < size; j++) {
            attachModels[j] = (ModelObject) attachlist.get(j);
        }
        return attachModels;
    }

    /**
     * 
     * Desc : enable to upload physical file and register on dataset with reference name
     * 
     * @Method Name : uploadNamedReferenceFileToDataSet
     * @param String
     *            dataset_puid
     * @param String
     *            file_path
     * @param String
     *            orginal_filename
     * @param boolean isText
     * @return LData
     * @throws Exception
     * @Comment
     *     2012.06.01 yun.jung@siemens.com
     *     fMSFileManagement.putFiles(inputs) 이 수행 되었을 때, 생성된 ImanFile이 리턴되지 않는다.
     *     WAS에 upload 된 파일이 사용자의 파일명과 다른 경우, 정보 복원을 위해 orginal_filename을
     *     setProperties 를 통해 업데이트하여야 하므로 ImanFile을 구하는 함수를 별도 개발해야 한다.  
     */
    public ModelObject uploadNamedReferenceFileToDataSet(String dataset_puid, String file_path, String orginal_filename, boolean isText) throws Exception {
        FileManagementUtility fMSFileManagement = null;
        ModelObject fileObject = null;
        try {
            fMSFileManagement = tcServiceManager.getFileService().newUtility();
            GetDatasetWriteTicketsInputData[] inputs = new GetDatasetWriteTicketsInputData[1];
            Dataset dataset = (Dataset) tcServiceManager.getDataService().loadModelObject(dataset_puid);
            String namedRefName = TcUtil.getReferenceTypeFromDataset(dataset);
            File file = new File(file_path);
            inputs[0] = getGetDatasetWriteTicketsInputData(dataset, namedRefName, file, isText);
            serviceData = fMSFileManagement.putFiles(inputs);

            if (!tcServiceManager.getDataService().ServiceDataError(serviceData)) {

                if (serviceData.sizeOfUpdatedObjects() > 0) {
                    fileObject = serviceData.getUpdatedObject(0);

                } else if (serviceData.sizeOfCreatedObjects() > 0) {

                    fileObject = serviceData.getCreatedObject(0);
                }

            } else {
                throw new Exception("<DATASET FILE UPLOAD ERROR> : " + TcUtil.makeMessageOfFail(serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (fMSFileManagement != null) {
                fMSFileManagement.term();
            }
        }
        return fileObject;
    }

    /**
     * 
     * Desc : get ticket of registered named reference file
     * 
     * @Method Name : getGetDatasetWriteTicketsInputData
     * @param String
     *            datasetName
     * @param String
     *            datasetType
     * @param String
     *            namedRefName
     * @param File
     *            file
     * @param boolean isText
     * @return GetDatasetWriteTicketsInputData
     * @throws Exception
     * @Comment
     */
    protected GetDatasetWriteTicketsInputData getGetDatasetWriteTicketsInputData(String datasetName, String datasetType, String namedRefName, File file, boolean isText) throws Exception {
        com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[] datasetinfo = new DatasetInfo[1];
        DatasetProperties2 props = new DatasetProperties2();
        props.clientId = "datasetWriteTixTestClientId";
        props.type = datasetType;
        props.name = datasetName;
        props.description = datasetName;

        datasetinfo[0] = new DatasetInfo();
        datasetinfo[0].clientId = "datasetWriteTixTestClientId";
        datasetinfo[0].type = datasetType;
        datasetinfo[0].name = datasetName;
        datasetinfo[0].description = datasetName;

        // create a datset
        com.teamcenter.services.strong.core._2010_04.DataManagement.CreateDatasetsResponse createdatasetresponse = tcServiceManager.getDataService().createDatasets(datasetinfo);

        // get the dataset
        Dataset dataset = createdatasetresponse.datasetOutput[0].dataset;

        // create a file to associate with dataset
        DatasetFileInfo fileInfo = new DatasetFileInfo();
        DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];

        // assume this file is in current dir
        fileInfo.clientId = "file_1";
        fileInfo.fileName = file.getAbsolutePath();
        fileInfo.namedReferencedName = namedRefName;
        fileInfo.isText = isText;
        fileInfo.allowReplace = false;
        fileInfos[0] = fileInfo;

        GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
        inputData.dataset = dataset;
        inputData.createNewVersion = false;
        inputData.datasetFileInfos = fileInfos;

        return inputData;

    }

    /**
     * 
     * Desc : get ticket of registered named reference file
     * 
     * @Method Name : getGetDatasetWriteTicketsInputData
     * @param Dataset
     *            dataset
     * @param String
     *            NamedRefName
     * @param File
     *            file
     * @param boolean isText
     * @return GetDatasetWriteTicketsInputData
     * @throws Exception
     * @Comment
     */
    private GetDatasetWriteTicketsInputData getGetDatasetWriteTicketsInputData(Dataset dataset, String namedRefName, File file, boolean isText) throws Exception {
        // create a file to associate with dataset
        DatasetFileInfo fileInfo = new DatasetFileInfo();
        DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];

        // assume this file is in current dir
        fileInfo.clientId = "file_1";
        fileInfo.fileName = file.getAbsolutePath();
        fileInfo.namedReferencedName = namedRefName;
        fileInfo.isText = isText;
        fileInfo.allowReplace = false;
        fileInfos[0] = fileInfo;

        GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
        inputData.dataset = dataset;
        inputData.createNewVersion = false;
        inputData.datasetFileInfos = fileInfos;

        return inputData;

    }

}
