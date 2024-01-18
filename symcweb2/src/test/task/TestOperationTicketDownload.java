/**
 *
 */
package test.task;

import java.util.HashMap;
import java.util.Map;

import com.symc.common.soa.clientx.AppXCredentialManager;
import com.symc.common.soa.clientx.AppXExceptionHandler;
import com.symc.common.soa.clientx.AppXModelEventListener;
import com.symc.common.soa.clientx.AppXPartialErrorListener;
import com.symc.common.soa.clientx.AppXRequestListener;
import com.symc.common.soa.util.TcConstants;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.administration.PreferenceManagementService;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.FileManagementService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.strong.core._2006_03.Session.LoginResponse;
import com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.strong.core._2007_01.DataManagement.RelationFilter;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.strong.core._2010_04.Session.ReturnedPreferences2;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.MSExcelX;
import com.teamcenter.soa.exceptions.CanceledOperationException;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * Class Name : Test
 * Class Description :
 * 
 * @date 2013. 9. 10.
 * 
 */
public class TestOperationTicketDownload {

    @org.junit.Test
    public void test() throws Exception {
        Object[] param = createConnection();
        login(param);
        FileManagementService fileManagementService = FileManagementService.getService((Connection) param[2]);
        try {
            GetItemFromAttributeResponse operationItem = getItemFromID("35-1D-670-0100-00", (Connection) param[2]);
            Item opItem = operationItem.output[0].item;
            getProperties((Connection) param[2], new ModelObject[] { opItem }, new String[] { TcConstants.PROP_ITEM_ID, "revision_list" });
            ModelObject[] itemRevisions = opItem.get_revision_list();
            if (itemRevisions == null || itemRevisions.length == 0) {
                throw new Exception(opItem.get_item_id() + " : This revision does not exist.");
            }
            // TODO :latest working revision을 가지고 오므로 latest released된 revision을 가져오게 변경 할 것......  
            ItemRevision rev = (ItemRevision) itemRevisions[itemRevisions.length - 1];
            // 국문표준작업서
            System.out.println("\n\n");
            System.out.println("국문표준작업서 테스트 ---------------> " + getSheetTickeInfos(param, fileManagementService, rev, "M7_PROCESS_SHEET_KO_REL").toString());
            System.out.println("\n");
            System.out.println("영문표준작업서 테스트 ---------------> " + getSheetTickeInfos(param, fileManagementService, rev, "M7_PROCESS_SHEET_EN_REL").toString());
            System.out.println("\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        logout(param);
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
    public HashMap<String, String> getSheetTickeInfos(Object[] param, FileManagementService fileManagementService, ItemRevision rev, String sheetRef) throws Exception, NotLoadedException, ServiceException {
        HashMap<String, String> fileInfoMap = new HashMap<String, String>();
        getProperties((Connection) param[2], new ModelObject[] { rev }, new String[] { sheetRef });
        ModelObject[] modelObjects = rev.getPropertyObject(sheetRef).getModelObjectArrayValue();
        if (modelObjects != null && modelObjects.length > 0) {
            for (ModelObject modelObject : modelObjects) {
                if (modelObject instanceof MSExcelX) {
                    MSExcelX excelDataset = (MSExcelX) modelObjects[0];
                    // "ref_list"
                    getProperties((Connection) param[2], new ModelObject[] { excelDataset }, new String[] { "ref_list", "ref_names" });
                    ModelObject[] excelFiles = excelDataset.get_ref_list();
                    for (ModelObject modelFile : excelFiles) {
                        ImanFile file = (ImanFile) modelFile;
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
                }
            }
        }
        return fileInfoMap;
    }

    public ServiceData getProperties(Connection connection, ModelObject[] modelObject, String[] reltypes) throws Exception {
        ServiceData serviceData = DataManagementService.getService(connection).getProperties(modelObject, reltypes);
        if (!ServiceDataError(serviceData)) {
            return serviceData;
        }
        return null;
    }

    private Object[] createConnection() {
        AppXCredentialManager credentialManager = new AppXCredentialManager();
        credentialManager.setUserPassword("if_system", "if_system", "");
        AppXRequestListener appXRequestListener = new AppXRequestListener();
        Connection.addRequestListener(appXRequestListener);
//        Connection connection = new Connection("http://plmwasdev/NewPLM", new HttpState(), credentialManager, SoaConstants.REST, SoaConstants.HTTP, false);
        Connection connection = new Connection("http://plmwasdev/NewPLM", credentialManager, SoaConstants.REST, SoaConstants.HTTP);

        connection.setExceptionHandler(new AppXExceptionHandler());
        connection.getModelManager().addPartialErrorListener(new AppXPartialErrorListener());
        connection.getModelManager().addModelEventListener(new AppXModelEventListener());

        Object[] param = new Object[3];
        param[0] = credentialManager;
        param[1] = appXRequestListener;
        param[2] = connection;
        return param;
    }

    public boolean login(Object[] param) {
        // Get the service stub
        SessionService sessionService = SessionService.getService((Connection) param[2]);

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

    public void logout(Object[] param) {
        // Get the service stub
        SessionService sessionService = SessionService.getService((Connection) param[2]);
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

    /**
     * Desc : retrieve ItemAttribute via itemid
     * 
     * @Method Name : getItemFromID
     * @param String
     *            itemId
     * @return GetItemFromAttributeResponse
     * @throws Exception
     * @Comment
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GetItemFromAttributeResponse getItemFromID(String itemId, Connection connection) throws Exception {
        DataManagementService dataManagementService = DataManagementService.getService(connection);
        GetItemFromAttributeResponse gifiRes = null;

        Map itemAttributes = new HashMap<String, Object>();
        itemAttributes.put(TcConstants.PROP_ITEM_ID, itemId);

        GetItemFromAttributeInfo agetitemfromattributeinfo = new GetItemFromAttributeInfo();
        agetitemfromattributeinfo.itemAttributes = itemAttributes;

        RelationFilter rf = new RelationFilter();
        GetItemFromIdPref getitemfromidpref = new GetItemFromIdPref();
        getitemfromidpref.prefs = new RelationFilter[] { rf };

        gifiRes = dataManagementService.getItemFromAttribute(new GetItemFromAttributeInfo[] { agetitemfromattributeinfo }, 1, getitemfromidpref);

        return gifiRes;
    }

    /**
     * 
     * Desc :
     * 
     * @Method Name : getFmsBootStrapUrl
     * @return
     * @throws Exception
     * @Comment
     */
    public String[] getFmsBootStrapUrl(Connection connection) throws Exception {
        ReturnedPreferences2 preferences[] = getPreferences2(connection, "site", new String[] { "Fms_BootStrap_Urls" });
        // String[] fmsUrlInfo = fmsUrlInfo[i] = preferences[i].values
        String[] fmsUrlInfo = preferences[0].values;
        return fmsUrlInfo;
    }

    /**
     * protected에서 public으로 변경
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
    public ReturnedPreferences2[] getPreferences2(Connection connection, String prefScope, String[] prefName) throws Exception {
//        SessionService sessionService = SessionService.getService(connection);
//        MultiPreferenceResponse2 multiPreferResp2 = null;
//        com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[] scopedPrefnames = new com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[1];
//        scopedPrefnames[0] = new com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames();
//        scopedPrefnames[0].scope = prefScope;
//        scopedPrefnames[0].names = prefName;
//        multiPreferResp2 = sessionService.getPreferences2(scopedPrefnames);
//        if (!ServiceDataError(multiPreferResp2.data)) {
//            return multiPreferResp2.preferences;
//        }

        PreferenceManagementService prefService = PreferenceManagementService.getService(connection);
		GetPreferencesResponse ret = prefService.getPreferences(prefName, true);
		if (ret != null && ret.data.sizeOfPartialErrors() == 0)
		{
			for (CompletePreference pref : ret.response) {
				if (pref.definition.protectionScope.toUpperCase().equals(prefScope.toUpperCase()))
				{
					ReturnedPreferences2 retPref2 = new ReturnedPreferences2();
					retPref2.category = pref.definition.category;
					retPref2.description = pref.definition.description;
					retPref2.scope = pref.definition.protectionScope;
					retPref2.values = pref.values.values;

					return new ReturnedPreferences2[]{retPref2};
				}
			}
//			retPrefValue = ret.response[0];
		}

        return null;
    }

    public boolean ServiceDataError(final ServiceData serviceData) throws Exception {
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
