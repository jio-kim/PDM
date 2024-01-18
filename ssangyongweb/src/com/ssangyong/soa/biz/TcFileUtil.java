package com.ssangyong.soa.biz;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.ssangyong.soa.service.TcServiceManager;
import com.ssangyong.soa.util.TcConstants;
import com.ssangyong.soa.util.TcUtil;
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
 * @author yunjae.jung
 */
public class TcFileUtil {

    private ServiceData serviceData;
    private TcServiceManager tcServiceManager;
    private TcItemUtil   tcItemUtil;
    private TcDatasetUtil   tcDatasetUtil;
    
    public TcFileUtil(Session tcSession) {
        tcServiceManager = new TcServiceManager(tcSession);   
    }

    /**
     * 
     * Desc : cascade type from ImanFile to File
     * @Method Name : getFilesFromImanFile
     * @param String[] file_puid
     * @return LMultiData
     * @throws Exception
     * @Comment
     */
//    public LMultiData getFilesFromImanFile (String[] file_puid) throws Exception {
//        LMultiData lmultidata = new LMultiData();
//        LData ldata = null;
//        ModelObject[] modelComps = tcServiceManager.getDataService().loadModelObjects(file_puid);
//        int cnt = modelComps.length;
//        ImanFile[] imanfiles = new ImanFile[cnt];
//        
//        for(int i=0; i <cnt; i++) {
//            imanfiles[i] = (ImanFile)modelComps[i];
//        }
//        
//        lmultidata = getFilesFromImanFile(imanfiles, true);
//        
//        if(lmultidata.isEmpty() ) {
//            ldata = new LData();
//            ldata = TcUtil.returnMessageOfFail(TcMessage.MSG_NO_RESULT);
//            ldata.setString(TcConstants.PROP_PUID, imanfiles[0].getUid());
//            lmultidata.addLData(ldata);
//        }
//        
//        return lmultidata;
//    }
    
    /**
     * 
     * Desc : download physical file or get data of files via "reqFile" 
     * @Method Name : getFilesFromImanFile
     * @param ImanFile[] imanfiles
     * @param boolean reqFile
     * @return LMultiData
     * @throws Exception
     * @Comment
     */
//    private LMultiData getFilesFromImanFile (ImanFile[] imanfiles, boolean reqFile) throws Exception {
//        LMultiData lmultidata = new LMultiData();
//        TcCommonUtil tcCommonUtil = new TcCommonUtil(tcSession);
//        LData ldata = null;
//
//        tcServiceManager.getDataService().getProperties(imanfiles, 
//                 new String[]{ 
////                            TcConstants.PROP_OBJECT_NAME,  TcConstants.PROP_DATASET_VERSION, TcConstants.PROP_CHECKED_OUT,TcConstants.PROP_OWNING_GROUP,
//                              TcConstants.PROP_CREATION_DATE, TcConstants.PROP_OBJECT_TYPE,  TcConstants.PROP_OWNING_USER, TcConstants.PROP_TCFILE_BYTESIZE, 
//                              TcConstants.PROP_LAST_MOD_DATE, TcConstants.PROP_LAST_MOD_USER, TcConstants.PROP_TCFILE_NAME } 
//                              );
//             for(int i=0; i < imanfiles.length; i++){
//                 
//                 if(imanfiles[i] !=null) {
//                     
//                     if(reqFile) {
//                         
//                         File cacheFile = getFile(imanfiles[i]);
//                         if(cacheFile.isFile()) {
//                                ldata = new LData();
//                                 ldata.setString(TcConstants.TC_RETURN_MESSAGE, TcConstants.TC_RETURN_OK);
//                                 ldata.set(TcConstants.TYPE_IMAN_FILE, imanfiles[i]);
//                                 ldata.setString(TcConstants.ENV_FILE_PATH, cacheFile.getAbsolutePath());
//                                 ldata.setString(TcConstants.ENV_FILE_NAME, cacheFile.getName());
//                                 ldata.setString(TcConstants.PROP_PUID, imanfiles[i].getUid());
//                                 ldata.setString(TcConstants.PROP_TCFILE_NAME, imanfiles[i].get_original_file_name());
//                                 ldata.setString(TcConstants.PROP_OBJECT_TYPE, imanfiles[i].getProperty(TcConstants.PROP_OBJECT_TYPE).getStringValue());
//                                 ldata.setString(TcConstants.PROP_LAST_MOD_USER,   tcCommonUtil.getUserName(imanfiles[i].getProperty(TcConstants.PROP_LAST_MOD_USER).getModelObjectValue()));                                
//                                 ldata.setString(TcConstants.PROP_LAST_MOD_DATE,   TcUtil.getDate(imanfiles[i].getProperty(TcConstants.PROP_LAST_MOD_DATE).getDateValue()));
//                                 ldata.setString(TcConstants.PROP_CREATION_DATE,   TcUtil.getDate(imanfiles[i].getProperty(TcConstants.PROP_CREATION_DATE).getDateValue()));
//                                 ldata.setString(TcConstants.PROP_TCFILE_BYTESIZE,     imanfiles[i].get_byte_size());
//
//                                 lmultidata.addLData(ldata);
//                         }
//                     }else{
//                         ldata = new LData();
//                         ldata.setString(TcConstants.TC_RETURN_MESSAGE, TcConstants.TC_RETURN_OK);
//                         ldata.set(TcConstants.TYPE_IMAN_FILE, imanfiles[i]);
//                         ldata.setString(TcConstants.ENV_FILE_PATH, "");
//                         ldata.setString(TcConstants.ENV_FILE_NAME, "");
//                         ldata.setString(TcConstants.PROP_PUID, imanfiles[i].getUid());
//                         ldata.setString(TcConstants.PROP_TCFILE_NAME, imanfiles[i].get_original_file_name());
//                         ldata.setString(TcConstants.PROP_OBJECT_TYPE, imanfiles[i].getProperty(TcConstants.PROP_OBJECT_TYPE).getStringValue());
//                         ldata.setString(TcConstants.PROP_LAST_MOD_USER,   tcCommonUtil.getUserName(imanfiles[i].getProperty(TcConstants.PROP_LAST_MOD_USER).getModelObjectValue()));                                
//                         ldata.setString(TcConstants.PROP_LAST_MOD_DATE,   TcUtil.getDate(imanfiles[i].getProperty(TcConstants.PROP_LAST_MOD_DATE).getDateValue()));
//                         ldata.setString(TcConstants.PROP_CREATION_DATE,   TcUtil.getDate(imanfiles[i].getProperty(TcConstants.PROP_CREATION_DATE).getDateValue()));
//                         ldata.setString(TcConstants.PROP_TCFILE_BYTESIZE,     imanfiles[i].get_byte_size());
//
//                         lmultidata.addLData(ldata);
//                     }
//                 }
//              }
//         
//             
//        return lmultidata;
//    }
    
    /**
     * 
     * Desc : download physical file of dataset or get data of dataset via "reqFile" 
     * @Method Name : getFilesFromDataset
     * @param Dataset[] dataset
     * @param String targetFile_puid
     * @param boolean reqFile
     * @return LMultiData
     * @throws Exception
     * @Comment
     */
//    public LMultiData getFilesFromDataset( Dataset[] dataset, String targetFile_puid, boolean reqFile) throws Exception {
//
//        LMultiData lmultidata = new LMultiData();
//        LData ldata = null;
//        tcUtil = new TcUtil(tcSession);
////          if("".equals(directory) || null ==directory) {
////              directory = TcConstants.ENV_TC_CACHE_DIR;
////          }
//            tcServiceManager.getDataService().getProperties(dataset, new String[] {TcConstants.PROP_REF_LIST});
//            ModelObject refs[] = dataset[0].get_ref_list();
//
//            if (refs.length > 0) {
//                tcServiceManager.getDataService().getProperties(refs, new String[] {"file_name", "original_file_name"});
//                
//                ModelObject[] fileComp = tcUtil.filterTypeModelObject(refs, TcConstants.TYPE_IMAN_FILE);
//                int model_size = fileComp.length;
//                ImanFile[] imanfiles = new ImanFile[model_size];
//                int comp_idx=0;
//                for (int i = 0; i < model_size; ++i) {
//                    if(fileComp[i] !=null && !"".equals(targetFile_puid) && null != targetFile_puid) {
//                        if(fileComp[i].getUid().equals(targetFile_puid)) {
//                            imanfiles[comp_idx] = (ImanFile)fileComp[i];
//                            comp_idx++;
//                        }
//                       
//                    }else{
//                        if(fileComp[i] !=null) {
//                            imanfiles[comp_idx] = (ImanFile)fileComp[i];
//                            comp_idx++;
//                        }
//                    }
//                }
//
//                if (imanfiles.length > 0) {
//                    lmultidata = getFilesFromImanFile(imanfiles, reqFile);
//                }else{
//                        ldata = new LData();
//                        ldata.setString(TcConstants.TC_RETURN_MESSAGE, TcConstants.TC_RETURN_FAIL);
//                        ldata.setString(TcConstants.TC_RETURN_FAIL_REASON, TcMessage.MSG_NO_RESULT);
//                        ldata.setString(TcConstants.PROP_PUID, dataset[0].getUid());
//                        lmultidata.addLData(ldata);
//                }
//            }
//           if (lmultidata.size() > 0) {
//               LSortUtils.sort(lmultidata, TcConstants.PROP_CREATION_DATE, LSortUtils.ORDER_ASC);
//           }
//            return lmultidata;
//    } 

    /**
     * 
     * Desc : get file object via FMS
     * @Method Name : getFiles 
     * @param ModelObject[] fileComp
     * @return File[]
     * @throws FileNotFoundException
     * @throws UnknownHostException
     * @throws Exception
     * @Comment
     */
    public File[] getFiles(ModelObject[] fileComp) throws FileNotFoundException, UnknownHostException, Exception {

        File[] file = null;
        FileManagementUtility fMSFileManagement = null;
        GetFileResponse getfileResponse = null;
        
        try{
            fMSFileManagement = tcServiceManager.getFileService().newUtility();
            getfileResponse = fMSFileManagement.getFiles(fileComp);
            
            int cnt = getfileResponse.sizeOfFiles();
            if(cnt > 0) {
                file = new File[cnt];
                for(int i=0; i<cnt; i++) {
                    file[i] = getfileResponse.getFile(i);
                }
    
            }
        }catch(Exception e){
            throw e;
        }finally{
            if(fMSFileManagement!=null) {
                fMSFileManagement.term();
            }
        }
        return file;
    }
    
    
    @SuppressWarnings("unused")
	private File getFile(ModelObject fileComp) throws FileNotFoundException, UnknownHostException, Exception {
        
        return getFiles(new ModelObject[]{fileComp})[0];
        
    }    
    
    public ModelObject[] retrieveAttachObjects (ItemRevision[] itemRevs) throws Exception {        
        ArrayList<ModelObject> attachlist = new ArrayList<ModelObject>();
        for(ItemRevision itemRevision : itemRevs) {
            tcServiceManager.getDataService().getProperties(new ModelObject[] { itemRevision }, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.PROP_ITEM_MASTER_TAG});
            RevisionOutput[] revisionOutput = tcItemUtil.getRevisionOutputFromItem(itemRevision.get_item_id(), itemRevision.get_item_revision_id(), itemRevision.getUid());
            DatasetOutput[] datasetOutput = tcDatasetUtil.getDatasetOutputFromRevisionOutput(revisionOutput[0]);
            
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
    
    /**
     * 
     * Desc : enable to upload physical file and register on dataset with reference name
     * @Method Name : uploadNamedReferenceFileToDataSet
     * @param String dataset_puid
     * @param String file_path
     * @param String orginal_filename
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
        try{
            fMSFileManagement = tcServiceManager.getFileService().newUtility();
            GetDatasetWriteTicketsInputData[] inputs  = new GetDatasetWriteTicketsInputData[1];                
            Dataset dataset = (Dataset)tcServiceManager.getDataService().loadModelObject(dataset_puid);                
            String namedRefName = TcUtil.getReferenceTypeFromDataset(dataset);                
            File file = new File(file_path);
            inputs[0] = getGetDatasetWriteTicketsInputData(dataset, namedRefName, file, isText);
            serviceData = fMSFileManagement.putFiles(inputs);                
            if (tcServiceManager.getDataService().ServiceDataError(serviceData)) {
                throw new Exception("<DATASET FILE UPLOAD ERROR> : " + TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
            } else {
                for (int i = 0; serviceData  != null && i < serviceData.sizeOfCreatedObjects(); i++) {
                    fileObject = serviceData.getCreatedObject(0);
                }                
            }                
        }catch(Exception ex) {
            throw ex;
        }finally{            
            if(fMSFileManagement !=null){
                fMSFileManagement.term();
            }
        }
        return fileObject;
    }
    


    /**
     * 
     * Desc : get ticket of registered named reference file
     * @Method Name : getGetDatasetWriteTicketsInputData
     * @param String datasetName
     * @param String datasetType
     * @param String namedRefName
     * @param File file
     * @param boolean isText
     * @return GetDatasetWriteTicketsInputData
     * @throws Exception
     * @Comment
     */
    protected GetDatasetWriteTicketsInputData  getGetDatasetWriteTicketsInputData(String datasetName, String datasetType, String namedRefName, File file, boolean isText)
    throws Exception
    {
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
        
        //create a datset
        com.teamcenter.services.strong.core._2010_04.DataManagement.CreateDatasetsResponse createdatasetresponse = tcServiceManager.getDataService().createDatasets(datasetinfo);

        //get the dataset
        Dataset dataset = createdatasetresponse.datasetOutput[0].dataset;
        
        //create a file to associate with dataset
        DatasetFileInfo fileInfo = new DatasetFileInfo();
        DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];

        // assume this file is in current dir
        fileInfo.clientId            = "file_1";
        fileInfo.fileName            = file.getAbsolutePath();
        fileInfo.namedReferencedName = namedRefName;
        fileInfo.isText              = isText;
        fileInfo.allowReplace        = false;
        fileInfos[0] = fileInfo;

        GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
        inputData.dataset = dataset;
        inputData.createNewVersion = false;
        inputData.datasetFileInfos = fileInfos;
        
        return inputData;

    }
    
    /**
     * 
     * Desc :  get ticket of registered named reference file
     * @Method Name : getGetDatasetWriteTicketsInputData
     * @param Dataset dataset
     * @param String NamedRefName
     * @param File file
     * @param boolean isText
     * @return GetDatasetWriteTicketsInputData
     * @throws Exception
     * @Comment
     */
    private GetDatasetWriteTicketsInputData  getGetDatasetWriteTicketsInputData(Dataset dataset,  String namedRefName, File file, boolean isText)
    throws Exception
    {
        //create a file to associate with dataset
        DatasetFileInfo fileInfo = new DatasetFileInfo();
        DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];

        // assume this file is in current dir
        fileInfo.clientId            = "file_1";
        fileInfo.fileName            = file.getAbsolutePath();
        fileInfo.namedReferencedName = namedRefName;
        fileInfo.isText              = isText;
        fileInfo.allowReplace        = false;
        fileInfos[0] = fileInfo;

        GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
        inputData.dataset = dataset;
        inputData.createNewVersion = false;
        inputData.datasetFileInfos = fileInfos;
        
        return inputData;

    }
    
//    public String[] getFscUril(String dataset_uid) throws Exception {
//        String fscServer="http://10.185.150.20:4455/tc/fms/185283809/mygroup/FSC_LGEVGPDM07S_infodba";
//        LMultiData lmultidata = getFilesFromDataset(new Dataset[]{(Dataset)tcServiceManager.getDataService().loadModelObject(dataset_uid)}, "", false);
//        String[] fscUrl = new String[lmultidata.getDataCount()];
//        for(int i=0; i< lmultidata.getDataCount(); i++) {
//            String ticket="";
//            ImanFile imanFile = (ImanFile)lmultidata.getLData(i).get(TcConstants.TYPE_IMAN_FILE);
//            FileTicketsResponse ticketResp = tcServiceManager.getFileService().getFileReadTickets(new ImanFile[]{imanFile});
//            if(!tcServiceManager.getDataService().ServiceDataError(ticketResp.serviceData) && !ticketResp.tickets.isEmpty()){
//                
//                ticket= (String)ticketResp.tickets.get(imanFile);
//                tcServiceManager.getDataService().getProperties(imanFile, TcConstants.PROP_ORIGINAL_FILE_NAME);
//                fscUrl[i] = fscServer+"/"+URLEncoder.encode(imanFile.get_original_file_name(), "UTF8")+"?ticket="+ticket+"&textencoding=Cp1252&textlineterm=";
//            }else{
//                TcUtil.makeMessageOfFail(ticketResp.serviceData);
//            }
//            
//        }
//        return fscUrl;
//    }
}
