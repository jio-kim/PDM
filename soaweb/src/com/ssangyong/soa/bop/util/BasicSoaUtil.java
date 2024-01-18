package com.ssangyong.soa.bop.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.ssangyong.CommonConstants;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.administration.PreferenceManagementService;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.strong.core._2007_01.DataManagement.RelationFilter;
import com.teamcenter.services.strong.core._2008_06.DataManagement;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsItemOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ItemInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.NamedReferenceList;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevisionOutput;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeItemOutput;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.GetFileResponse;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.exceptions.CanceledOperationException;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class BasicSoaUtil {
	
	private Connection connection;
	DataManagementService dataManagementService;
	SavedQueryService savedQueryService;
	
	public static String CACHE_DOWNLOAD_DIR=CommonConstants.LOG_PATH;

	public BasicSoaUtil(Connection connection){
		this.connection = connection;
		dataManagementService = DataManagementService.getService(connection);
		savedQueryService = SavedQueryService.getService(connection);
	}

	public Connection getConnection() {
		return connection;
	}

	public String getLoginUserId() {
		
		String userId = null;
		if(connection==null){
			return userId;
		}

		String[] credentials = null;
		try {
			credentials = ((Connection)connection).getCredentialManager().getCredentials(new InvalidCredentialsException());
		} catch (CanceledOperationException e) {
			e.printStackTrace();
		}
		if(credentials!=null && credentials.length>0){
			userId = credentials[0];
		}

		return userId;
	}
	
	public String getLoginUserPassword() {
		
		String userPassword = null;
		if(connection==null){
			return userPassword;
		}

		String[] credentials = null;
		try {
			credentials = ((Connection)connection).getCredentialManager().getCredentials(new InvalidCredentialsException());
		} catch (CanceledOperationException e) {
			e.printStackTrace();
		}
		if(credentials!=null && credentials.length>1){
			userPassword = credentials[1];
		}

		return userPassword;
	}
	
	public ItemRevision createItemRevision(String itemType, String id, String revNo, 
			String name, String description, String unit, 
			String sParentObjUid, HashMap<String, String> revMasterProp) throws Exception {
		
		ItemRevision newItemRevision = null;
		
	    // Get the service stub
		com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties itemProperty = 
				new com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties();
	    
	    itemProperty.clientId="1";
	    itemProperty.itemId=id;
	    itemProperty.revId=revNo;
	    itemProperty.name = name;
	    itemProperty.type = itemType;
	    itemProperty.description = description;
	    itemProperty.uom = unit;
	
	    ModelObject parentComp = null;
	    if (sParentObjUid != null && sParentObjUid.length() > 0) {
	        parentComp = getModelObject(sParentObjUid);
	    }
	    
	    String paramString = "";
	    
	    ItemProperties[] paramArrayOfItemProperties = new ItemProperties[]{itemProperty};
		ModelObject paramModelObject = parentComp; 
	    
		CreateItemsResponse response  = dataManagementService.createItems(paramArrayOfItemProperties, paramModelObject, paramString);
		
		int errorSize = response.serviceData.sizeOfPartialErrors();
		if(errorSize>0){
			throw new Exception(response.serviceData.getPartialError(0).getMessages()[0]);
		}
		
		int sizeofCreate = 0;
		if(response.output!=null){
			sizeofCreate = response.output.length;
		}
		for (int i = 0; sizeofCreate>0 && i < sizeofCreate; i++) {
			ModelObject tempModelObj = response.output[i].itemRev;
			if(tempModelObj!=null && tempModelObj instanceof ItemRevision){
				newItemRevision = (ItemRevision)tempModelObj;
			}
			
		}
		
		return newItemRevision;
	}

	public ItemRevision[] createItemRevisions(ItemProperties[] paramArrayOfItemProperties) throws Exception {
		
		ItemRevision[] newRevisions = null;
		
		ModelObject paramModelObject = null; 
		String paramString = "";
		
		CreateItemsResponse response  = dataManagementService.createItems(paramArrayOfItemProperties, paramModelObject, paramString);
		
		int errorSize = response.serviceData.sizeOfPartialErrors();
		if(errorSize>0){
			throw new Exception(response.serviceData.getPartialError(0).getMessages()[0]);
		}
		
		Vector<ItemRevision> newRevVec = new Vector<ItemRevision>();
		
		int sizeofCreate = 0;
		if(response.output!=null){
			sizeofCreate = response.output.length;
		}
		for (int i = 0; sizeofCreate>0 && i < sizeofCreate; i++) {
			ModelObject tempModelObj = response.output[i].itemRev;
			if(tempModelObj!=null && tempModelObj instanceof ItemRevision){
				if(newRevVec.contains( (ItemRevision)tempModelObj) == false){
					newRevVec.add( (ItemRevision)tempModelObj );
				}
			}
			
		}
	
		if(newRevVec!=null && newRevVec.size()>0){
			newRevisions = new ItemRevision[newRevVec.size()];
			for (int i = 0;newRevisions!=null && i < newRevisions.length; i++) {
				newRevisions[i] = newRevVec.get(i);
			}
		}
		
		return newRevisions;
	}

	public void deleteModelObject(ModelObject[] targetModelObjects) throws ServiceException {
		
		ServiceData serviceData = null;
		
		if(targetModelObjects!=null && targetModelObjects.length>0){
			serviceData = dataManagementService.deleteObjects(targetModelObjects);
		}
		
		if(serviceData.sizeOfPartialErrors()>0){
			throw new ServiceException(serviceData.getPartialError(0).getMessages());
		}
	}

	public DataManagementService getDataManagementService() {
		return dataManagementService;
	}

	public SavedQueryService getSavedQueryService() {
		return savedQueryService;
	}
	
	public ModelObject getModelObject(String objectPuid){
		ModelObject findedObject = null;
		
        if (objectPuid == null ||
        		(objectPuid!=null && objectPuid.trim().length()<1) || 
        		(objectPuid!=null && objectPuid.trim().equals("AAAAAAAAAAAAAA"))
        		) {
            return null;
        }
        
        ServiceData serviceData = dataManagementService.loadObjects(new String[] { objectPuid });
        if(serviceData!=null){
        	int plainObjCount = serviceData.sizeOfPlainObjects();
        	for (int i = 0; plainObjCount>0 && i < plainObjCount; i++) {
        		ModelObject tempModelObject = serviceData.getPlainObject(i);
        		if(tempModelObject!=null){
        			findedObject = tempModelObject;
        			break;
        		}
			}
        }
		
		return findedObject;
	}
	
	public Item getItem(String itemId){
		String itemType = null;
		return getItem(itemType, itemId);
	}
	
	public Item getItem(String itemType, String itemId){
		
		Item item = null;
		
        Map itemAttributes = new HashMap<String, Object>();
        if(itemType!=null && itemType.trim().length()>0){
        	itemAttributes.put("object_type", itemType);
        }
        itemAttributes.put("item_id", itemId);
		
        GetItemFromAttributeInfo agetitemfromattributeinfo = new GetItemFromAttributeInfo();
        agetitemfromattributeinfo.itemAttributes = itemAttributes;
        
        RelationFilter relationFilter = new RelationFilter();
        
        GetItemFromIdPref getItemFromIdPref = new GetItemFromIdPref();
        getItemFromIdPref.prefs = new RelationFilter[] { relationFilter };

        GetItemFromAttributeResponse getItemFromAttributeResponse = null;
        
        getItemFromAttributeResponse = dataManagementService.getItemFromAttribute(
        				new GetItemFromAttributeInfo[] { agetitemfromattributeinfo }, 
        				1, 
        				getItemFromIdPref);
        
        
        System.out.println("getItemFromAttributeResponse.serviceData = "+getItemFromAttributeResponse.serviceData);
        System.out.println("getItemFromAttributeResponse.serviceData.sizeOfPartialErrors() = "+getItemFromAttributeResponse.serviceData.sizeOfPartialErrors());
        
        if(getItemFromAttributeResponse.serviceData.sizeOfPartialErrors() < 1){
        	
        	GetItemFromAttributeItemOutput[] outPut = getItemFromAttributeResponse.output;
        	
        	for (int i = 0; outPut!=null && i < outPut.length; i++) {
				if(outPut[i] != null && outPut[i].item !=null && outPut[i].item instanceof Item){
					Item tempItem = (Item)outPut[i].item;
					
					String tempItemId = null;
					try {
						tempItem = (Item) readProperties(tempItem, new String[]{
								"item_id", "object_type", "object_name" });
						tempItemId = tempItem.get_item_id();
						
						System.out.println("tempItemId = "+tempItemId);
						
						if(tempItemId!=null && itemId!=null && itemId.trim().equalsIgnoreCase(tempItemId.trim())){
							item = tempItem;
							break;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
        	
        }else{
        	
        	for (int i = 0; i < getItemFromAttributeResponse.serviceData.sizeOfPartialErrors(); i++) {
        		ErrorStack aErrorStack = getItemFromAttributeResponse.serviceData.getPartialError(i);
        		System.out.println("Error : "+aErrorStack.toString());
			}
        }
        
		return item;
	}
	
	public ItemRevision getItemRevision(String itemId, String itemRevId){
		ItemRevision itemRevision = null;
		
		Item item = getItem(itemId);
		if(item!=null){
			return itemRevision;
		}
		
		try {
			item = (Item)readProperties(item, new String[]{"revision_list"});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			ModelObject[] revisionModels = item.get_revision_list();
			for (int i = 0;revisionModels!=null && i < revisionModels.length; i++) {
				if(revisionModels[i] != null && revisionModels[i] instanceof ItemRevision){
					ItemRevision tempItemRevision = (ItemRevision)revisionModels[i];
					
					tempItemRevision.get_item_id();
					tempItemRevision.get_item_revision_id();
					tempItemRevision.get_object_type();
					tempItemRevision.get_object_name();
					
					try {
						tempItemRevision = (ItemRevision)readProperties(tempItemRevision, new String[]{
								"item_id", "item_revision_id",
								"object_type", "object_name" });
						
						String tempItemRevId = tempItemRevision.get_item_revision_id();
						if(itemRevId!=null && tempItemRevId!=null && tempItemRevId.trim().equalsIgnoreCase(itemRevId.trim())){
							itemRevision = tempItemRevision;
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		
		return itemRevision;
	}

	public ItemRevision getLatestReleasedItemRevision(Item item){
		
		ItemRevision latestItemRevision = null;
		
		ModelObject[] livisionList = null;
		
		String currentMaxItemRevId = null;
		ItemRevision currentMaxItemRevision = null;
		
		try {
			item = (Item)readProperties(item, new String[]{"revision_list"});
	
			livisionList = item.get_revision_list();
			if(livisionList==null || (livisionList!=null && livisionList.length<1)){
				return latestItemRevision;
			}
	
			for (int i = 0; livisionList!=null && i < livisionList.length; i++) {
				ItemRevision tempRevision = (ItemRevision)livisionList[i];
				tempRevision = (ItemRevision)readProperties(tempRevision, new String[]{"release_statuses", "item_revision_id"});
				
				ModelObject[] releasedStatus = tempRevision.get_release_statuses();
				if(releasedStatus!=null && releasedStatus.length>0){
					String itemRevisionId = tempRevision.get_item_revision_id();
					if(currentMaxItemRevId==null){
						currentMaxItemRevId = itemRevisionId;
						currentMaxItemRevision = tempRevision;
					}else{
						int result = itemRevisionId.compareToIgnoreCase(currentMaxItemRevId);
						if(result>=0){
							currentMaxItemRevId = itemRevisionId;
							currentMaxItemRevision = tempRevision;							
						}
					}
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("currentMaxItemRevId : "+currentMaxItemRevId);
		
		if(currentMaxItemRevision!=null){
			latestItemRevision = currentMaxItemRevision;
		}
		
		return latestItemRevision;
	}

	public ImanQuery getImanQuery(String queryName) {

    	ImanQuery findedQuery = null;
        try {
            GetSavedQueriesResponse savedQueries = savedQueryService.getSavedQueries();
            for (int i = 0; i < savedQueries.queries.length; i++) {
                if (savedQueries.queries[i].name.equals(queryName)){
                	findedQuery = savedQueries.queries[i].query;
                	break;
                }
            }  
        } catch (ServiceException e) {
            e.printStackTrace();                  
        }
        
        return findedQuery;          
    }
	
	public File[] getFile(ModelObject[] srcImanFiles, String destinationFolder){

		ArrayList<File> fileListArray = new ArrayList<File>();
		for (int i = 0; srcImanFiles!=null && i < srcImanFiles.length; i++) {
			ImanFile srcImanFile = (ImanFile)srcImanFiles[i];
			if(srcImanFile!=null){
				File file = getFile(srcImanFile, destinationFolder);
				if(file!=null && file.exists()==true){
					fileListArray.add(file);
				}
			}
		}
		
		File[] returnFiles = null;
		if(fileListArray==null || (fileListArray!=null && fileListArray.size()<1)){
			return returnFiles;
		}

		returnFiles = new File[fileListArray.size()]; 
		for (int i = 0; i < fileListArray.size(); i++) {
			returnFiles[i] = fileListArray.get(i);
		}
    	
		return returnFiles;
	}
	
	public File[] getFile(ModelObject[] srcImanFiles, String destinationFolder, String targetFilePath){

		ArrayList<File> fileListArray = new ArrayList<File>();
		for (int i = 0; srcImanFiles!=null && i < srcImanFiles.length; i++) {
			ImanFile srcImanFile = (ImanFile)srcImanFiles[i];
			if(srcImanFile!=null){
				File file = getFile(srcImanFile, destinationFolder, targetFilePath);
				if(file!=null && file.exists()==true){
					fileListArray.add(file);
				}
			}
		}
		
		File[] returnFiles = null;
		if(fileListArray==null || (fileListArray!=null && fileListArray.size()<1)){
			return returnFiles;
		}

		returnFiles = new File[fileListArray.size()]; 
		for (int i = 0; i < fileListArray.size(); i++) {
			returnFiles[i] = fileListArray.get(i);
		}
    	
		return returnFiles;
	}
	
	public File getFile(ImanFile srcImanFile, String destinationFolder){
		
		String currentFileName = null;
		String orignFileName = null;
		String orignFileExt = null;
		String mimeType = null;
		
		String[] propNameLists = new String[] { "file_name", "original_file_name", "file_ext", "file_size", "last_mod_user", "last_mod_date", "mime_type" };
		try {
			srcImanFile = (ImanFile)readProperties(srcImanFile, propNameLists);
			
			currentFileName = srcImanFile.get_file_name();
			orignFileName = srcImanFile.get_original_file_name();
			orignFileExt = srcImanFile.get_file_ext();
			mimeType = srcImanFile.get_mime_type();

			String fileInfo = "\n"+
					"currentFileName : "+currentFileName+"\n"+
					"orignFileName : "+orignFileName+"\n"+
					"orignFileExt : "+orignFileExt+"\n"+
					"mimeType : "+mimeType;
			
			System.out.println(fileInfo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		ImanFile[] srcImanFiles = new ImanFile[]{srcImanFile}; 

		// FileManagementUtility를 이용해 Download 하는 방법적용
		FileManagementUtility fMSFileManagement = getFileManagementUtility();

		File[] files = null;
        GetFileResponse getfileResponse = null;
        try{
            getfileResponse = fMSFileManagement.getFiles(srcImanFiles);
            int cnt = getfileResponse.sizeOfFiles();
            if(cnt > 0) {
            	files = getfileResponse.getFiles();
            }
        }catch(Exception e){
            //throw e;
        	e.printStackTrace();
        }finally{
        	// FMS Connection Close
            if(fMSFileManagement!=null) {
                fMSFileManagement.term();
            }
        }
        
		File returnFile = null;
		
        for (int i = 0; files!=null && i < files.length; i++) {
        	
        	File tempFile = files[i];
        	
			String filePath = tempFile.getPath();
			String filename = tempFile.getName();
			String canonicalPath = null;
			try {
				canonicalPath = tempFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("** filePath = "+filePath);
			System.out.println("** filename = "+filename);
			System.out.println("** canonicalPath = "+canonicalPath);
			
			if(tempFile!=null && tempFile.exists()==true){
				
				String targetPath = destinationFolder + File.separator + orignFileName;
			
				System.out.println("targetPath = "+targetPath);
				
				File targetFile = new File(targetPath);
				boolean isSuccess = tempFile.renameTo(targetFile);
				
				if(isSuccess){
					returnFile = new File(targetPath);
					break;
				}
			}
			
		}
		
		return (File)returnFile;
	}
	
	public File getFile(ImanFile srcImanFile, String destinationFolder, String targetFilePath){
		
		String currentFileName = null;
		String orignFileName = null;
		String orignFileExt = null;
		String mimeType = null;
		
		String[] propNameLists = new String[] { "file_name", "original_file_name", "file_ext", "file_size", "last_mod_user", "last_mod_date", "mime_type" };
		try {
			srcImanFile = (ImanFile)readProperties(srcImanFile, propNameLists);
			
			currentFileName = srcImanFile.get_file_name();
			orignFileName = srcImanFile.get_original_file_name();
			orignFileExt = srcImanFile.get_file_ext();
			mimeType = srcImanFile.get_mime_type();

			String fileInfo = "\n"+
					"currentFileName : "+currentFileName+"\n"+
					"orignFileName : "+orignFileName+"\n"+
					"orignFileExt : "+orignFileExt+"\n"+
					"mimeType : "+mimeType;
			
			System.out.println(fileInfo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		ImanFile[] srcImanFiles = new ImanFile[]{srcImanFile}; 

		// FileManagementUtility를 이용해 Download 하는 방법적용
		FileManagementUtility fMSFileManagement = getFileManagementUtility();

		File[] files = null;
        GetFileResponse getfileResponse = null;
        try{
            getfileResponse = fMSFileManagement.getFiles(srcImanFiles);
            int cnt = getfileResponse.sizeOfFiles();
            if(cnt > 0) {
            	files = getfileResponse.getFiles();
            }
        }catch(Exception e){
            //throw e;
        	e.printStackTrace();
        }finally{
        	// FMS Connection Close
            if(fMSFileManagement!=null) {
                fMSFileManagement.term();
            }
        }
        
		File returnFile = null;
		
        for (int i = 0; files!=null && i < files.length; i++) {
        	
        	File tempFile = files[i];
        	
			String filePath = tempFile.getPath();
			String filename = tempFile.getName();
			String canonicalPath = null;
			try {
				canonicalPath = tempFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("** filePath = "+filePath);
			System.out.println("** filename = "+filename);
			System.out.println("** canonicalPath = "+canonicalPath);
			
			if(tempFile!=null && tempFile.exists()==true){
				
				String targetPath = null;
				if(targetFilePath!=null && targetFilePath.trim().length()>0){
					targetPath = targetFilePath;	
				}else{
					targetPath = destinationFolder + File.separator + orignFileName;
				}
				
				System.out.println("targetPath = "+targetPath);
				
				File targetFile = new File(targetPath);
				
				boolean isSuccess = tempFile.renameTo(targetFile);
				
				if(isSuccess){
					returnFile = new File(targetPath);
					break;
				}
			}
			
		}
		
		return (File)returnFile;
	}
	
	
	public FileManagementUtility getFileManagementUtility(){
		
		HashMap<String, Object> fscMap = null;
		try {
			fscMap = loadFscInfo();
		} catch (ServiceException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		FileManagementUtility fileManagementUtility = null;
		if(fscMap!=null){
			try {
				fileManagementUtility = new FileManagementUtility(
						connection,
						(String)fscMap.get("hostname"), 
						(String[])fscMap.get("fscUrl"), 
						(String[])fscMap.get("bootStrapUrl"), 
						(String)fscMap.get("target_dir")
						);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		return fileManagementUtility;
	}
	
    public HashMap<String, Object> loadFscInfo() throws ServiceException, Exception {
    	
    	// 현재 Program이 구동되는 Host의 IP를 hostAddress에 기록한다. 
    	InetAddress hostName = InetAddress.getLocalHost();
        String hostAddress = hostName.getHostAddress();

        // Preference에 기록된 Fms_BootStrap_Urls 값을 읽어온다.
        String prefScope = "site";
        String[] prefNames = new String[]{"Fms_BootStrap_Urls"};
        CompletePreference[] preferences = getCompletePreference (prefScope, prefNames ); 
        String[] bootStrapUrl =preferences[0].values.values;

        // Cache를 다운로드하는 Fold 위치 지정
        String cacheDownloadDir = BasicSoaUtil.CACHE_DOWNLOAD_DIR;

        HashMap<String, Object> fscMap = new HashMap<String, Object>();
        fscMap.put("hostname", hostAddress);
//      fscMap.put("fscUrl", fscUrl);
        fscMap.put("bootStrapUrl", bootStrapUrl);
        fscMap.put("target_dir", cacheDownloadDir);
        return fscMap;

    }
    
    public CompletePreference[] getCompletePreference (String prefScope, String[] prefNames ) throws Exception {
    	
    	//new String[]{"Fms_BootStrap_Urls"}
    	
    	PreferenceManagementService preferenceManagementService = PreferenceManagementService.getService(connection); 
    	ArrayList<CompletePreference> prefList = new ArrayList<CompletePreference>();
    	
    	GetPreferencesResponse prefResponse = preferenceManagementService.getPreferences(prefNames, true);
    	if (prefResponse.data.sizeOfPartialErrors()<1){
    		for (CompletePreference preference : prefResponse.response)
    			if (preference.definition.protectionScope.toUpperCase().equals(prefScope.toUpperCase()))
    				prefList.add(preference);
    	}
    	
    	if (prefList.size() > 0){
    		return prefList.toArray(new CompletePreference[0]);
    	} else {
    		return null;
    	}
  }
	
    public ArrayList<Dataset> getAllChildDatasetList(ItemRevision itemRevision, String targetDatasetTypeName, String relationTypeName) throws Exception {
        
        GetItemAndRelatedObjectsInfo relatedInfo = new GetItemAndRelatedObjectsInfo();
        String[] namedRefTemplates = new String[]{"excel"};
        NamedReferenceList[] namedRef = new NamedReferenceList[namedRefTemplates.length];
        int cnt = 0;
        for (String namedRefTemplate : namedRefTemplates) {

            namedRef[cnt] = new NamedReferenceList();
            namedRef[cnt].namedReference = namedRefTemplate;
            namedRef[cnt].ticket = true;
            cnt++;
        }
        relatedInfo.bvrTypeNames = new String[] {};
        relatedInfo.clientId = "ItemAndRelatedObjectsInfo";
        relatedInfo.datasetInfo = new DatasetInfo();
        relatedInfo.itemInfo = new ItemInfo();
        relatedInfo.revInfo = new RevInfo();
        //relatedInfo.itemInfo.uid = targetItem.getUid();

        relatedInfo.datasetInfo.clientId = "datasetInfo";
        relatedInfo.datasetInfo.filter.processing = "All";
        relatedInfo.datasetInfo.namedRefs = namedRef;

        relatedInfo.revInfo.clientId = "revInfo";
        relatedInfo.revInfo.id = "";
        relatedInfo.revInfo.useIdFirst = false;
        relatedInfo.revInfo.revisionRule = "";
        relatedInfo.revInfo.processing = "All"; // "Nrev";
        relatedInfo.revInfo.uid = itemRevision.getUid();

    	DataManagementService dataManagementService = DataManagementService.getService(connection);
        GetItemAndRelatedObjectsResponse resp = dataManagementService.getItemAndRelatedObjects(new GetItemAndRelatedObjectsInfo[] { relatedInfo });
        
        ArrayList<Dataset> datasetArrayList = new ArrayList<Dataset>();
        
        if (resp.serviceData!=null && resp.serviceData.sizeOfPartialErrors()<1) {
        	
            for (GetItemAndRelatedObjectsItemOutput itemsOut : resp.output) {
            	
                for (RevisionOutput rev : itemsOut.itemRevOutput) {
                    
                    ItemRevision tempRevision = (ItemRevision)rev.itemRevision;
                    if(itemRevision!=null && itemRevision.equals(tempRevision)==true){
                    	
                    	DataManagement.DatasetOutput[] datasetOutput = rev.datasetOutput;
                    	if(datasetOutput!=null){
                    		for (int j = 0; j < datasetOutput.length; j++) {
                    			Dataset aDataset = (Dataset)datasetOutput[j].dataset;
                    			
        						aDataset = (Dataset)readProperties(aDataset, new String[]{
        								"object_type", "object_name" });
        						String datasetType = aDataset.get_object_type();
                    			String tempRelationTypeName = datasetOutput[j].relationTypeName;
                    			
                    			System.out.println("## datasetType : "+datasetType);
                    			System.out.println("## relationTypeName : "+tempRelationTypeName);
                    			
                    			boolean isTarget = true;
                    			
                    			if(targetDatasetTypeName!=null && datasetType!=null && targetDatasetTypeName.trim().equalsIgnoreCase(datasetType.trim())==false){
                    				isTarget = false;
                    			}
                    			if(isTarget==true && relationTypeName!=null && tempRelationTypeName!=null && relationTypeName.trim().equalsIgnoreCase(tempRelationTypeName.trim())==false ){
                    				isTarget = false;
                    			}
                    			
                    			if(aDataset!=null && isTarget==true){
                    				datasetArrayList.add(aDataset);
                    			}

                    		}
                    	}
                    }
                    
                }
            }
        }
        
        return datasetArrayList;
    }

	public ModelObject readProperties(ModelObject modelObject, String[] propertyNames) throws Exception {

		ModelObject plainModelObject = modelObject;
		
		if(connection==null){
			return plainModelObject;
		}
		
		ModelObject[] paramArrayOfModelObject = new ModelObject[]{modelObject}; 
		
		ServiceData aServiceData = dataManagementService.getProperties(paramArrayOfModelObject, propertyNames);
		
		 if(aServiceData.sizeOfPartialErrors() > 0){
			 throw new Exception(aServiceData.getPartialError(0).getMessages()[0]);
		 }else{
	
			 int planCount = aServiceData.sizeOfPlainObjects();
			 for (int i = 0;planCount>0 && i < planCount; i++) {
				 ModelObject tempPlainModelObject = aServiceData.getPlainObject(i);
				 if(tempPlainModelObject !=null && tempPlainModelObject.equals(modelObject)){
					 plainModelObject = tempPlainModelObject;
					 break;
				 }
			 }
			 
		 }
	
		return plainModelObject;
	}

}
