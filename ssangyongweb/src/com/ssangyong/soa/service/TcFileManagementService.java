package com.ssangyong.soa.service;

import java.net.InetAddress;
import java.util.HashMap;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.biz.TcSessionUtil;
import com.ssangyong.soa.util.TcConstants;
import com.teamcenter.fms.clientcache.proxy.IFileCacheProxyCB;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.FileManagementService;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.GetFileResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ImanFile;

/**
 * 
 * Desc :
 * @author yunjae.jung
 */
@SuppressWarnings("static-access")
public class TcFileManagementService implements  //FileManagement, 
com.teamcenter.services.strong.core._2006_03.FileManagement,
com.teamcenter.services.strong.core._2007_01.FileManagement,
com.teamcenter.services.internal.strong.core._2008_06.FileManagement,
com.teamcenter.services.internal.strong.core._2010_09.FileManagement
{
    private Session tcSession = null;
    private FileManagementUtility fileManagementUtility;
    public TcFileManagementService(Session tcSession) throws ServiceException, Exception {
        this.tcSession = tcSession;
        if(this.fileManagementUtility == null) {
			HashMap<String, Object> fscMap = new HashMap<String,Object>();
			fscMap = loadFscInfo();
            this.fileManagementUtility = new FileManagementUtility(this.tcSession.getConnection(),(String)fscMap.get("hostname"), (String[])fscMap.get("fscUrl"), (String[])fscMap.get("bootStrapUrl"), (String)fscMap.get("target_dir"));
        }

    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.FileManagement#commitDatasetFiles(com.teamcenter.services.strong.core._2006_03.FileManagement.CommitDatasetFileInfo[])
     */
    @Override
    public ServiceData commitDatasetFiles(CommitDatasetFileInfo[] arg0){
        return getService().commitDatasetFiles(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2010_09.FileManagement#commitReplacedFiles(com.teamcenter.services.internal.strong.core._2010_09.FileManagement.CommitReplacedFileInfo[], boolean[])
     */
    @Override
    public ServiceData commitReplacedFiles(CommitReplacedFileInfo[] arg0,
            boolean[] arg1){
        return getInernalService().commitReplacedFiles(arg0, arg1);
    }


    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.FileManagement#getDatasetWriteTickets(com.teamcenter.services.strong.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData[])
     */
    @Override
    public GetDatasetWriteTicketsResponse getDatasetWriteTickets(
            GetDatasetWriteTicketsInputData[] arg0){
        return getService().getDatasetWriteTickets(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.FileManagement#getFileReadTickets(com.teamcenter.soa.client.model.strong.ImanFile[])
     */
    public com.teamcenter.services.strong.core._2006_03.FileManagement.FileTicketsResponse getFileReadTickets1(ImanFile[] files){
        return getService().getFileReadTickets(files); 
    }

    

    public GetFileResponse getFileToLocation(ModelObject modelobject, String s, IFileCacheProxyCB ifilecacheproxycb, Object obj) {
        return newUtility().getFileToLocation(modelobject, s, ifilecacheproxycb, obj);
    }
    
    public FileManagementService getService() {    
        return FileManagementService.getService(tcSession.getConnection());        
    }
    
    public com.teamcenter.services.internal.strong.core.FileManagementService getInernalService() {
        
        return com.teamcenter.services.internal.strong.core.FileManagementService.getService(tcSession.getConnection());
    }
    
    @Override
    public GetTransientFileTicketsResponse getTransientFileTicketsForUpload(
            TransientFileInfo[] atransientfileinfo){
        return getService().getTransientFileTicketsForUpload(atransientfileinfo);
    }

    @Deprecated
    public FileManagementUtility initFSC() throws ServiceException, Exception{ //hostAddress, fscUrl, bootStrapUrl, TcConstants.ENV_TC_CACHE_DIR
         HashMap<String, Object> fscMap = new HashMap<String,Object>();
         fscMap = loadFscInfo();
         newUtility().initFSC((String)fscMap.get("hostname"), (String[])fscMap.get("fscUrl"), (String[])fscMap.get("bootStrapUrl"), (String)fscMap.get("target_dir"));
         return newUtility();
    }
    
    public HashMap<String, Object> loadFscInfo() throws ServiceException, Exception {
        InetAddress hostName = InetAddress.getLocalHost();
        String hostAddress = hostName.getHostAddress();
//      String fscUrl[] = {"http://LGEVGPDM07S:4455/"};
//      String bootStrapUrl[] = {"http://LGEVGPDM07S:4455/"};
//      TcPreferenceUtil tcPrefUtil = new TcPreferenceUtil(tcSession);
//      String[] url = tcPrefUtil.getModifiedSitePreferences(TcConstants.TC_PREF_CATG_MT_FILE_CACHE, TcConstants.TC_PREF_NAME_FMS_BOOTSTRP_URL);
 
        
        TcSessionUtil tcSessionUtil = new TcSessionUtil(tcSession);
        String[] fmsUrl = tcSessionUtil.getFmsBootStrapUrl();
//      String[] fscUrl = fmsUrl;
        String[] bootStrapUrl =fmsUrl;
        HashMap<String, Object> fscMap = new HashMap<String, Object>();
        fscMap.put("hostname", hostAddress);
//      fscMap.put("fscUrl", fscUrl);
        fscMap.put("bootStrapUrl", bootStrapUrl);
        fscMap.put("target_dir", tcSession.contextRoot+TcConstants.ENV_TC_CACHE_DIR);
        return fscMap;
        
        /*
        hostAddress = "10.80.28.162";
        String[] bootStrapUrl = {"http://10.80.28.162:4544/"};
        HashMap fscMap = new HashMap<String, Object>();
        fscMap.put("hostname", hostAddress);
//      fscMap.put("fscUrl", fscUrl);
        fscMap.put("bootStrapUrl", bootStrapUrl);
        fscMap.put("target_dir", "D:"+File.separator+TcConstants.ENV_TC_CACHE_DIR);
        return fscMap;
        */
    }
    public FileManagementUtility newUtility() {
        
        return fileManagementUtility;
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_06.FileManagement#commitRegularFiles(com.teamcenter.services.internal.strong.core._2008_06.FileManagement.CommitUploadedRegularFilesInput[])
     */
    @Override
    public CommitUploadedRegularFilesResponse commitRegularFiles(
            CommitUploadedRegularFilesInput[] acommituploadedregularfilesinput){
        return getInernalService().commitRegularFiles(acommituploadedregularfilesinput);
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_06.FileManagement#getFileTransferTickets(com.teamcenter.soa.client.model.strong.ImanFile[])
     */
    @Override
    public GetFileTransferTicketsResponse getFileTransferTickets(
            ImanFile[] aimanfile){
        return getInernalService().getFileTransferTickets(aimanfile);
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_06.FileManagement#getRegularFileTicketsForUpload(com.teamcenter.services.internal.strong.core._2008_06.FileManagement.GetRegularFileWriteTicketsInput[])
     */
    @Override
    public GetRegularFileWriteTicketsResponse getRegularFileTicketsForUpload(
            GetRegularFileWriteTicketsInput[] agetregularfilewriteticketsinput){
        return getInernalService().getRegularFileTicketsForUpload(agetregularfilewriteticketsinput);
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_06.FileManagement#getWriteTickets(com.teamcenter.services.internal.strong.core._2008_06.FileManagement.WriteTicketsInput[])
     */
    @Override
    public com.teamcenter.services.internal.strong.core._2008_06.FileManagement.FileTicketsResponse getWriteTickets(
            WriteTicketsInput[] awriteticketsinput){
        return getInernalService().getWriteTickets(awriteticketsinput);
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_06.FileManagement#updateImanFileCommits(java.lang.String[])
     */
    @Override
    public com.teamcenter.services.internal.strong.core._2008_06.FileManagement.UpdateImanFileCommitsResponse updateImanFileCommits(String[] as){
        return getInernalService().updateImanFileCommits(as);
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.FileManagement#getFileReadTickets(com.teamcenter.soa.client.model.strong.ImanFile[])
     */
    @Override
    public com.teamcenter.services.strong.core._2006_03.FileManagement.FileTicketsResponse getFileReadTickets(ImanFile[] aimanfile){
        return getService().getFileReadTickets(aimanfile);
    }
}
