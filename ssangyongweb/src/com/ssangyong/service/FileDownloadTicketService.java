package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.StringUtil;
import com.ssangyong.soa.clientx.AppXCredentialManager;
import com.ssangyong.soa.clientx.AppXRequestListener;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.FileManagementService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.strong.core._2006_03.Session.LoginResponse;
import com.teamcenter.services.strong.core._2010_04.Session.MultiPreferenceResponse2;
import com.teamcenter.services.strong.core._2010_04.Session.ReturnedPreferences2;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.exceptions.CanceledOperationException;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * Class Name : FildDownloadTicketService
 * Class Description :
 * 
 * @date 2013. 9. 10.
 * 
 */
public class FileDownloadTicketService {

    SessionService sessionService;
    DataManagementService dataManagementService;
    FileManagementService fileManagementService;

    /*
     * 기존의 login 서비스와 함께 사용시 Session 중복으로 인하여 별도의 Connection설정 및 처리.
     * (때문에 타 서비스와의 혼동을 줄이기 위하여 File Download Ticket 서비스 관련된 모든 기능은 이 Class 안에서 구현 함.)
     */

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getTicket(DataSet ds) throws Exception {
        ArrayList<String> imanFilePuids = (ArrayList<String>) ds.get("puids");
        ArrayList<Object> ticketList = new ArrayList<Object>();
        if (imanFilePuids == null) {
            return ticketList;
        }

        Object[] param = createConnection();
        try {
            sessionService = SessionService.getService((Connection) param[2]);
            dataManagementService = DataManagementService.getService((Connection) param[2]);
            fileManagementService = FileManagementService.getService((Connection) param[2]);
            login(param);
            for (String imanFilePuid : imanFilePuids) {
                HashMap<String, Object> fileTicketInfo = new HashMap<String, Object>();
                fileTicketInfo.put("PUID", imanFilePuid);
                try {
                    ModelObject modelObject = loadObject((Connection) param[2], imanFilePuid);
                    fileTicketInfo.put("TICKET_INFO", getSheetTickeInfos(param, modelObject));
                    fileTicketInfo.put("SUCCESS", "T");
                    fileTicketInfo.put("ERROR_MESSAGE", "");
                } catch (Exception e) {
                    fileTicketInfo.put("SUCCESS", "F");
                    fileTicketInfo.put("ERROR_MESSAGE", StringUtil.getStackTraceString(e));
                } finally {
                    ticketList.add(fileTicketInfo);
                }
            }
            return ticketList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // LogOut
            if (param[2] != null) {
                logout(param);
            }
        }
    }

    /**
     * 
     * @method getSheetTickeInfos
     * @date 2014. 1. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, String> getSheetTickeInfos(Object[] param, ModelObject modelObject) throws Exception, NotLoadedException, ServiceException {
        HashMap<String, String> fileInfoMap = new HashMap<String, String>();
        if (modelObject instanceof ImanFile) {
            ImanFile file = (ImanFile) modelObject;
            getProperties((Connection) param[2], new ModelObject[] { file }, new String[] { "file_name", "original_file_name", "file_size", "last_mod_user", "last_mod_date" });
            ImanFile[] files = new ImanFile[1];
            files[0] = file;
            FileTicketsResponse ftresponse = fileManagementService.getFileReadTickets(files);
            if (ftresponse.serviceData.sizeOfPartialErrors() > 0)
                throw new ServiceException("FileManagementService.getFileReadTickets returns a partial error - " + ftresponse.serviceData.getPartialError(0).getMessages()[0]);
            String ticketValue = (String) ftresponse.tickets.get(files[0]);
            String ticketoriginFileName = files[0].get_original_file_name();
            fileInfoMap.put("fileName", files[0].get_file_name());
            fileInfoMap.put("originFileName", ticketoriginFileName);
            fileInfoMap.put("fileSize", files[0].get_file_size());
            fileInfoMap.put("downloadUrl", (getFmsBootStrapUrl((Connection) param[2])[0]) + "?ticket=" + ticketValue);
        }

        return fileInfoMap;
    }

    private ServiceData getProperties(Connection connection, ModelObject[] modelObject, String[] reltypes) throws Exception {
        ServiceData serviceData = dataManagementService.getProperties(modelObject, reltypes);
        if (!ServiceDataError(serviceData)) {
            return serviceData;
        }
        return null;
    }

    private Object[] createConnection() throws Exception {
        HashMap<String, String> env = getEnv();
        String serverHost = env.get("TC_WEB_URL");
        String userID = env.get("TC_DEMON_ID");
        String password = new String(Base64.decodeBase64(env.get("TC_DEMON_PASSWD").getBytes()));

        AppXCredentialManager credentialManager = new AppXCredentialManager();
        credentialManager.setUserPassword(userID, password, "");
        AppXRequestListener appXRequestListener = new AppXRequestListener();
        Connection.addRequestListener(appXRequestListener);
//        Connection connection = new Connection(serverHost, new HttpState(), credentialManager, SoaConstants.REST, SoaConstants.HTTP, false);

        String protocol=null;
        String envNameTccs = null;
        if ( serverHost.startsWith("http") )
        {
            protocol   = SoaConstants.HTTP;
        }
        else if ( serverHost.startsWith("tccs") )
        {
            protocol   = SoaConstants.TCCS;
            serverHost = serverHost.trim();
            int envNameStart = serverHost.indexOf('/') + 2;
            envNameTccs = serverHost.substring( envNameStart, serverHost.length() );
            serverHost = "";
        }
        else
        {
            protocol   = SoaConstants.IIOP;
        }
        Connection connection = new Connection(serverHost, credentialManager, SoaConstants.REST, protocol);

        if( protocol == SoaConstants.TCCS )
        {
           connection.setOption(  Connection.TCCS_ENV_NAME, envNameTccs );
        }

        Object[] param = new Object[3];
        param[0] = credentialManager;
        param[1] = appXRequestListener;
        param[2] = connection;
        return param;
    }

    private HashMap<String, String> getEnv() throws Exception {
        EnvService envService = new EnvService();
        HashMap<String, String> env = envService.getTCWebEnv();
        return env;
    }

    private boolean login(Object[] param) {
        // Get the service stub
        try {
            // Prompt for credentials until they are right, or until user
            // cancels
            String[] credentials = ((AppXCredentialManager) param[0]).promptForCredentials();
            try {
                System.out.println("##################################");
                System.out.println("###   팀센터 로그인을 합니다. ");
                System.out.println("##################################");
                // *****************************
                // Execute the service operation
                // *****************************
                LoginResponse out = sessionService.login(credentials[0], credentials[1], credentials[2], credentials[3], "", credentials[4]);
                System.out.println("###   팀센터 로그에 성공하였습니다. ");
                System.out.println("###   로그인 : " + out.user);
                System.out.println("##################################");
                return true;
            } catch (InvalidCredentialsException e) {
                credentials = ((AppXCredentialManager) param[0]).getCredentials(e);
                e.printStackTrace();
            }
        }
        // User canceled the operation, don't need to tell him again
        catch (CanceledOperationException e) {
            e.printStackTrace();
        }
        // Exit the application
        // System.exit(0);
        return false;
    }

    private void logout(Object[] param) {
        // Get the service stub
        try {
            // *****************************
            // Execute the service operation
            // *****************************
            sessionService.logout();
            // Remove a Request Listener
            Connection.removeRequestListener((AppXRequestListener) param[1]);
            System.out.println("##################################");
            System.out.println("####        로그아웃          ####  ");
            System.out.println("##################################");
        } catch (ServiceException e) {
            e.printStackTrace();
            System.out.println("팀센터 Logout에 실패 하였습니다.");
        }
    }

    // /**
    // * Desc : retrieve ItemAttribute via itemid
    // *
    // * @Method Name : getItemFromID
    // * @param String
    // * itemId
    // * @return GetItemFromAttributeResponse
    // * @throws Exception
    // * @Comment
    // */
    // @SuppressWarnings({ "unchecked", "rawtypes" })
    // private GetItemFromAttributeResponse getItemFromID(String itemId, Connection connection) throws Exception {
    // GetItemFromAttributeResponse gifiRes = null;
    //
    // Map itemAttributes = new HashMap<String, Object>();
    // itemAttributes.put(TcConstants.PROP_ITEM_ID, itemId);
    //
    // GetItemFromAttributeInfo agetitemfromattributeinfo = new GetItemFromAttributeInfo();
    // agetitemfromattributeinfo.itemAttributes = itemAttributes;
    //
    // RelationFilter rf = new RelationFilter();
    // GetItemFromIdPref getitemfromidpref = new GetItemFromIdPref();
    // getitemfromidpref.prefs = new RelationFilter[] { rf };
    //
    // gifiRes = dataManagementService.getItemFromAttribute(new GetItemFromAttributeInfo[] { agetitemfromattributeinfo }, 1, getitemfromidpref);
    //
    // return gifiRes;
    // }

    /**
     * 
     * Desc :
     * 
     * @Method Name : getFmsBootStrapUrl
     * @return
     * @throws Exception
     * @Comment
     */
    private String[] getFmsBootStrapUrl(Connection connection) throws Exception {
        ReturnedPreferences2 preferences[] = getPreferences2(connection, "site", new String[] { "Fms_BootStrap_Urls" });
        // String[] fmsUrlInfo = fmsUrlInfo[i] = preferences[i].values
        String[] fmsUrlInfo = preferences[0].values;
        return fmsUrlInfo;
    }

    /**
     * protected에서 private으로 변경
     * 2014.01.02 hybyeon
     * 
     * @method getPreferences2
     * @date 2014. 1. 2.
     * @param
     * @return ReturnedPreferences2[]
     * @exception
     * @throws
     * @see
     */
    @Deprecated
    private ReturnedPreferences2[] getPreferences2(Connection connection, String prefScope, String[] prefName) throws Exception {
        SessionService sessionService = SessionService.getService(connection);
        MultiPreferenceResponse2 multiPreferResp2 = null;
        com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[] scopedPrefnames = new com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[1];
        scopedPrefnames[0] = new com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames();
        scopedPrefnames[0].scope = prefScope;
        scopedPrefnames[0].names = prefName;
        multiPreferResp2 = sessionService.getPreferences2(scopedPrefnames);
        if (!ServiceDataError(multiPreferResp2.data)) {
            return multiPreferResp2.preferences;
        }
        return null;
    }

    private ModelObject loadObject(Connection connection, String puid) throws Exception {
        ServiceData data = dataManagementService.loadObjects(new String[] { puid });
        if (!ServiceDataError(data)) {
            return (ModelObject) data.getPlainObject(0);
        } else {
            throw new Exception("'" + puid + "' is not TC Object");
        }
    }

    private boolean ServiceDataError(final ServiceData serviceData) throws Exception {
        if (serviceData.sizeOfPartialErrors() > 0) {
            for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
                for (String msg : serviceData.getPartialError(i).getMessages())
                    throw new Exception(msg);
            }

            return true;
        }

        return false;
    }

}
