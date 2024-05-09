package com.kgm.soa.bop.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.WebServiceCallUtil;
import com.kgm.dto.EndItemData;
import com.kgm.soa.bop.util.BasicSoaUtil;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;

public class SDVBOPUtilities {

	
	private static boolean isSsangyongWeb = true;
	
    /**
     * bl_occ_mvl_condition 속성에서 value 값 가져오기
     * 
     * @method getVariant
     * @date 2013. 11. 6.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, Object> getVariant(String condition) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        ArrayList<String> values = new ArrayList<String>();
        String printValues = null;
        String printDescriptions = null;

        StringBuilder sb = new StringBuilder();
        String tmpStr = null;
        StringTokenizer stringTokenizer1 = null;
        StringTokenizer stringTokenizer2 = null;
        stringTokenizer1 = new StringTokenizer(condition, "or");

        while (stringTokenizer1.hasMoreElements()) {
            tmpStr = (String) stringTokenizer1.nextElement();
            stringTokenizer2 = new StringTokenizer(tmpStr, "and");

            while (stringTokenizer2.hasMoreElements()) {
                tmpStr = (String) stringTokenizer2.nextElement();
                tmpStr = tmpStr.substring(tmpStr.indexOf("=") + 1, tmpStr.length());

                sb.append(tmpStr.replaceAll("\"", ""));
                if (stringTokenizer2.hasMoreTokens()) {
                    sb.append("and");
                }
            }

            if (stringTokenizer1.hasMoreTokens()) {
                sb.append("or");
            }
        }

        String temp = sb.toString().replaceAll(" ", "");
        printValues = temp;
        printValues = printValues.replaceAll("or", "@\n");
        printValues = printValues.replaceAll("and", " AND ");

        stringTokenizer1 = new StringTokenizer(temp, "or");
        while (stringTokenizer1.hasMoreElements()) {
            temp = (String) stringTokenizer1.nextElement();
            stringTokenizer2 = new StringTokenizer(temp, "and");
            while (stringTokenizer2.hasMoreElements()) {
                temp = (String) stringTokenizer2.nextElement();
                values.add(temp);
            }
        }

        HashMap<String, String> descriptions = getDescriptionFromVariant(values);
        printDescriptions = printValues;
        if(descriptions!=null && descriptions.size()>0){
        	for (String value : values) {
        		if (!descriptions.containsKey(value))
        			continue;
        		printDescriptions = printDescriptions.replace(value, descriptions.get(value));
        	}
        }

        data.put("values", values);
        data.put("descriptions", descriptions);
        data.put("printValues", printValues);
        data.put("printDescriptions", printDescriptions);

        return data;
    }
    
    /**
     * values 값에 해당하는 Description 가져오기
     * 
     * @method getDescriptionFromVariant
     * @date 2013. 11. 5.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getDescriptionFromVariant(ArrayList<String> values) {
    	
        HashMap<String, String> descriptions = new HashMap<String, String>();

        // 1차 WAS를 호출
        boolean isSsyongWeb = true;
        WebServiceCallUtil webServiceCallUtil = new WebServiceCallUtil(isSsyongWeb);

        for (int i = 0; values!=null && i < values.size(); i++) {

            ArrayList<HashMap<String, String>> list = null;
            
        	String value = values.get(i);
        	if(value==null || (value!=null && value.trim().length()<1)){
        		continue;
        	}
        	
        	if(value!=null && value.trim().length()>0){
        		
                DataSet ds = new DataSet();
        		ds.put("code_name", value);
        		try {
        			list = (ArrayList<HashMap<String, String>>) webServiceCallUtil.execute("com.kgm.service.VariantService", "getVariantValueDesc", ds);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
            
            if (list != null) {
                for (HashMap<String, String> map : list) {
                    descriptions.put(map.get("CODE_NAME"), map.get("CODE_DESC"));
                }
            }
        }

        return descriptions;
    }
    
    /**
     * Product 와 Process의 Data를 Query로 비교해 자동변경된 Part의 List를 EndItemData 형식의 List에 담아 Return 한다.
     * @param targetUid : Process Top BOMLine Item Puid를 입력 한다.
     * @param productId : Product Item Id를 입력 한다.
     * @return
     * @throws Exception
     */
	public static ArrayList<EndItemData> findReplacedEndItems(String targetUid, String productId) throws Exception {
    	ArrayList<EndItemData> resultList = null;
        
    	DataSet ds = new DataSet();
        ds.put("targetUid", targetUid);
        ds.put("productId", productId);

        System.out.println("targetUid : " + targetUid);
        System.out.println("productId : "  + productId);
        
        WebServiceCallUtil webServiceCallUtil = new WebServiceCallUtil(isSsangyongWeb);

        String serviceClass = "com.kgm.service.SYMCBOPService";
        resultList = (ArrayList<EndItemData>) webServiceCallUtil.execute(serviceClass, "findReplacedEndItems", ds);
        
		return resultList;
    }
	
	/**
	 * 기존의 FindReplacedEndItems 기능을 썼지만 한 차종의 모든 Replaed Item을 조회 하는데는 그 조회 시간이 너무 오래 걸려 기존 쿼리를 수정 하여 
	 * 그 시간을 단축 이 메서드는 한 차종의 모든 Replaced 아이템을 조회 할때만 쓰임
	 * @param targetUid
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<EndItemData> findReplacedRootEndItems(String targetUid, String productId) throws Exception {
		ArrayList<EndItemData> resultList = null;
		
		DataSet ds = new DataSet();
		ds.put("targetUid", targetUid);
		ds.put("productId", productId);
		
		System.out.println("targetUid : " + targetUid);
		System.out.println("productId : "  + productId);
		
		WebServiceCallUtil webServiceCallUtil = new WebServiceCallUtil(isSsangyongWeb);
		
		String serviceClass = "com.kgm.service.SYMCBOPService";
		resultList = (ArrayList<EndItemData>) webServiceCallUtil.execute(serviceClass, "findReplacedRootEndItems", ds);
		
		return resultList;
	}
	
	/**
	 * Teamcenter에 저장된 EndItemList 출력에 사용되는 Excel Template을 주어지 경로에 Download 한다.
	 * 
	 * @param connection
	 * @param destinationFolder
	 * @return
	 */
	public static File getReportExcelTemplateFile(Connection connection, String destinationFolder, String templateItemId, String targetFilePath){
		
		File newFile = null;
		
		BasicSoaUtil basicSoaUtil = new BasicSoaUtil(connection);
		Item templateItem = (Item)basicSoaUtil.getItem(templateItemId);
		
		if(templateItem==null){
			return newFile;
		}
		
		ItemRevision latestItemRevision = basicSoaUtil.getLatestReleasedItemRevision(templateItem);
		ArrayList<Dataset> dataSetArrayList = null;
		
		String targetDatasetType = "MSExcelX";
		String targetRelationTypeName = null; // TC_Attaches, PROCESS_SHEET_KO_REL, PROCESS_SHEET_EN_REL
		try {
			dataSetArrayList = basicSoaUtil.getAllChildDatasetList(latestItemRevision, targetDatasetType, targetRelationTypeName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Dataset dataset = null;
		for (int i = 0; dataSetArrayList!=null && i < dataSetArrayList.size(); i++) {
			Dataset tempDataset = dataSetArrayList.get(i);
			if(tempDataset!=null){
				dataset = tempDataset;
				break;
			}
		}
		
		if(dataset==null){
			System.out.println("Dataset is null");
			return newFile;
		}
		
		ArrayList<ImanFile> targetFileArray = new ArrayList<ImanFile>();
		ModelObject[] refListImanFiles = null;
		try {
			dataset = (Dataset)basicSoaUtil.readProperties(dataset, new String[] { "ref_list", "ref_names" });
			refListImanFiles = dataset.get_ref_list();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		File[] returnFiles = null;
		if(refListImanFiles!=null){
			returnFiles = basicSoaUtil.getFile(refListImanFiles, destinationFolder, targetFilePath);
		}
		
		if(returnFiles!=null && returnFiles.length>=1){
			newFile = returnFiles[0];
		}
		
		return newFile;
	}

}
