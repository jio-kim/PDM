package com.symc.plm.me.sdv.operation.ps;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.BOPStructureDataUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

public class InstructionSheetSearchUtil {

	private boolean isEnglish = false;
	private  TCComponentBOMLine topBOMLine; 
	private  HashMap<String, String> structureSearchConditionMap;
	
	private static String FINDAISDATASERVICE= "com.kgm.service.SYMCBOPFindAISDataService";
	
	private String shopItemId = null;
	private String processType = null;
	private String shopCode = null;
	private String vehicleCode = null;
	private String productCode = null;
	private String findKey = null;
	private String plantLineChar = null;
	private String plantLineCode = null;

	private Document resultXMLDoc = null;
    private XPath xpath = null;
	
	public InstructionSheetSearchUtil(){
	 	// xpath 생성
	    this.xpath = XPathFactory.newInstance().newXPath();
	}
	
	private void initBasicData(){
		
		// resultXMLDoc을 초기화 한다.
		resultXMLDoc = null;
		
		// 주요 검색 조건 값을 초기화 한다.
		this.shopItemId = null;
		this.processType= null;
		this.shopCode = null;
		this.vehicleCode = null;
		this.productCode = null;
		this.findKey = null;
		
		if(this.topBOMLine!=null){
			
			try {
				TCComponentBOMLine tempTopBOMLine = this.topBOMLine.window().getTopBOMLine();
				
				System.out.println("tempTopBOMLine = "+tempTopBOMLine);
				
				
				this.shopItemId = tempTopBOMLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				this.processType= tempTopBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);
				this.shopCode = tempTopBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
				this.vehicleCode = tempTopBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);
				this.productCode = tempTopBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
				
				System.out.println("this.shopItemId = "+this.shopItemId);
				System.out.println("this.processType = "+this.processType);
				System.out.println("this.shopCode = "+this.shopCode);
				System.out.println("this.vehicleCode = "+this.vehicleCode);
				System.out.println("this.productCode = "+this.productCode);
				
				
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
    	BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
    	aBOPStructureDataUtility.initBOMLineData(this.topBOMLine.getSession(), this.shopItemId);
    	
    	// Structure Data를 저장하는 Function 수행
    	try {
    		// 2020-09-02 seho 리비전 룰 적용이 필요하여 수정함.
    		// true면 latest release 리비전 룰 적용.. 
    		// null 이면 latest 리비전 룰 적용..
    		String isLatestReleased = (String)structureSearchConditionMap.get("release_operation");//true 이거나 null이거나...
			this.findKey = aBOPStructureDataUtility.makeNewBOPInformationData(shopItemId, isLatestReleased);
			if(this.findKey!=null){
				this.findKey = this.findKey.trim();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	if(this.processType!=null && (this.processType.trim().equalsIgnoreCase("A")==true || this.processType.trim().equalsIgnoreCase("ASSY")==true)){
    		this.plantLineChar = "D";
    		this.plantLineCode = this.shopCode.charAt(1)+"D";
    		this.plantLineCode = "1D";
    	}else if(this.processType!=null && (this.processType.trim().equalsIgnoreCase("B")==true || this.processType.trim().equalsIgnoreCase("BODY")==true)){
    		this.plantLineChar = "B";
    		this.plantLineCode = this.shopCode.trim();
    	}else if(this.processType!=null && (this.processType.trim().equalsIgnoreCase("P")==true || this.processType.trim().equalsIgnoreCase("PAINT")==true)){
    		this.plantLineChar = "P";
    		this.plantLineCode = this.shopCode.trim();
    	}
	}
	
	public ArrayList<HashMap> getSearchResultData(TCComponentBOMLine topBOMLine, HashMap<String, String> structureSearchConditionMap, boolean isEnglish){
		
		this.isEnglish = isEnglish;
		this.topBOMLine = topBOMLine;
		this.structureSearchConditionMap = structureSearchConditionMap;
		
		// 조립작업표준서 검색 Data 생성을위해 Structure Data 저장 
		initBasicData();
		
	    DataSet findCondition = new DataSet();
	    if(this.findKey!=null){
	    	findCondition.setString("findKeyCode", this.findKey);
	    }
	    
	    System.out.println("Find Cond Init ------");
	    
	    if(this.structureSearchConditionMap==null){
	    	System.out.println("this.structureSearchConditionMap = null");
	    }else{
	    	System.out.println("this.structureSearchConditionMap is not null");
	    }
	    
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("shop_code")!=null){
	    	findCondition.setString("shop_code", this.structureSearchConditionMap.get("shop_code"));
	    	System.out.println("FIND COND : shop_code = "+this.structureSearchConditionMap.get("shop_code"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("line_code")!=null){
	    	findCondition.setString("line_code", this.structureSearchConditionMap.get("line_code"));
	    	System.out.println("FIND COND : line_code = "+this.structureSearchConditionMap.get("line_code"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("station_code")!=null){
	    	findCondition.setString("station_code", this.structureSearchConditionMap.get("station_code"));
	    	System.out.println("FIND COND : station_code = "+this.structureSearchConditionMap.get("station_code"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("item_id")!=null){
	    	findCondition.setString("item_id", this.structureSearchConditionMap.get("item_id"));
	    	System.out.println("FIND COND : item_id = "+this.structureSearchConditionMap.get("item_id"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("m7_KOR_NAME")!=null){
	    	findCondition.setString("m7_KOR_NAME", this.structureSearchConditionMap.get("m7_KOR_NAME"));
	    	System.out.println("FIND COND : m7_KOR_NAME = "+this.structureSearchConditionMap.get("m7_KOR_NAME"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("m7_MECO_NO")!=null){
	    	findCondition.setString("m7_MECO_NO", this.structureSearchConditionMap.get("m7_MECO_NO"));
	    	System.out.println("FIND COND : m7_MECO_NO = "+this.structureSearchConditionMap.get("m7_MECO_NO"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("owning_user")!=null){
	    	findCondition.setString("owning_user", this.structureSearchConditionMap.get("owning_user"));
	    	System.out.println("FIND COND : owning_user = "+this.structureSearchConditionMap.get("owning_user"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("publish_user")!=null){
	    	findCondition.setString("publish_user", this.structureSearchConditionMap.get("publish_user"));
	    	System.out.println("FIND COND : publish_user = "+this.structureSearchConditionMap.get("publish_user"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("release_operation")!=null){
	    	findCondition.setString("release_operation", this.structureSearchConditionMap.get("release_operation"));
	    	System.out.println("FIND COND : release_operation = "+this.structureSearchConditionMap.get("release_operation"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("norelease_operation")!=null){
	    	findCondition.setString("norelease_operation", this.structureSearchConditionMap.get("norelease_operation"));
	    	System.out.println("FIND COND : norelease_operation = "+this.structureSearchConditionMap.get("norelease_operation"));
	    }	    
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("empty_operation")!=null){
	    	findCondition.setString("empty_operation", this.structureSearchConditionMap.get("empty_operation"));
	    	System.out.println("FIND COND : empty_operation = "+this.structureSearchConditionMap.get("empty_operation"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("different_operation")!=null){
	    	findCondition.setString("different_operation", this.structureSearchConditionMap.get("different_operation"));
	    	System.out.println("FIND COND : different_operation = "+this.structureSearchConditionMap.get("different_operation"));
	    }
	    if(this.structureSearchConditionMap!=null && this.structureSearchConditionMap.get("bl_occ_mvl_condition")!=null){
	    	findCondition.setString("bl_occ_mvl_condition", this.structureSearchConditionMap.get("bl_occ_mvl_condition"));
	    	System.out.println("FIND COND : bl_occ_mvl_condition = "+this.structureSearchConditionMap.get("bl_occ_mvl_condition"));
	    }
	    
	    System.out.println("Find Cond Init ------ E");
	    
	    int makeDataSqlNo = 0;
	    String publishSection = null;
    	String findSqlFunctionName = null;
	    if(this.isEnglish){
	    	makeDataSqlNo = 2;
	    	findSqlFunctionName = "findENGInstructionSheets";
	    	publishSection = "EPS";
	    }else{
	    	makeDataSqlNo = 1;
	    	findSqlFunctionName = "findKORInstructionSheets";
	    	publishSection = "KPS";
	    }
	    
    	if(this.plantLineChar!=null && this.plantLineChar.trim().equalsIgnoreCase("D")==true){
    		makeDataSqlNo = makeDataSqlNo * 1;
    	}else if(this.plantLineChar!=null && this.plantLineChar.trim().equalsIgnoreCase("B")==true){
    		makeDataSqlNo = makeDataSqlNo * 10;
    	}else if(this.plantLineChar!=null && this.plantLineChar.trim().equalsIgnoreCase("P")==true){
    		makeDataSqlNo = makeDataSqlNo * 100;
    	}

    	String makeSqlFunctionName = "";
    	
    	switch (makeDataSqlNo) {
		case 1:
			makeSqlFunctionName = "insertKORAssySheetsData";
			break;

		case 10:
			makeSqlFunctionName = "insertKORBodySheetsData";
			break;

		case 100:
			makeSqlFunctionName = "insertKORPaintSheetsData";
			break;
			
		case 2:
			makeSqlFunctionName = "insertENGAssySheetsData";
			break;
			
		case 20:
			makeSqlFunctionName = "insertENGBodySheetsData";
			break;

		case 200:
			makeSqlFunctionName = "insertENGPaintSheetsData";
			break;

		default:
			makeSqlFunctionName = null;
			break;
		}
    	
	    DataSet ds = new DataSet();
	    ds.setString("findKeyCode", this.findKey);
	    ds.setString("plantLineChar", this.plantLineChar);
	    ds.setString("plantLineCode", this.plantLineCode);
	    ds.setString("vehicleCode", this.vehicleCode);
	    ds.setString("publishSection", publishSection);
	    if(structureSearchConditionMap.get("release_operation")!=null)
	    {
	    	ds.setString("release_operation", structureSearchConditionMap.get("release_operation"));
	    }
	    
	    System.out.println("--------------------------------------------------------------------------");
	    
    	System.out.println("makeSqlFunctionName = "+makeSqlFunctionName);
    	System.out.println("findKeyCode = "+ds.getString("findKeyCode"));
    	System.out.println("plantLineChar = "+ds.getString("plantLineChar"));
    	System.out.println("plantLineCode = "+ds.getString("plantLineCode"));
    	System.out.println("vehicleCode = "+ds.getString("vehicleCode"));
    	System.out.println("publishSection = "+ds.getString("publishSection"));
    	
    	System.out.println("--------------------------------------------------------------------------");
    	
    	System.out.println("shop_code = " +ds.getString("shop_code"));
    	System.out.println("line_code = " +ds.getString("line_code"));
    	System.out.println("station_code = " +ds.getString("station_code"));
    	System.out.println("item_id = " +ds.getString("item_id"));
    	System.out.println("m7_KOR_NAME = " +ds.getString("m7_KOR_NAME"));
    	System.out.println("m7_MECO_NO = " +ds.getString("m7_MECO_NO"));
    	System.out.println("owning_user = " +ds.getString("owning_user"));
    	System.out.println("publish_user = " +ds.getString("publish_user"));
    	System.out.println("release_operation = " +ds.getString("release_operation"));
    	System.out.println("norelease_operation = " +ds.getString("norelease_operation"));
    	System.out.println("empty_operation = " +ds.getString("empty_operation"));
    	System.out.println("different_operation = " +ds.getString("different_operation"));
    	
    	// 수정 
    	System.out.println("teamCode = " +ds.getString("teamCode"));
    	
    	
    	System.out.println("--------------------------------------------------------------------------");
		ArrayList<HashMap> resultDataList = null;
		ArrayList<HashMap<String, Object>> resultDataList2 = null;
		
    	if(makeSqlFunctionName==null){
    		return resultDataList;
    	}
    	
    	System.out.println("makeSqlFunctionName = "+makeSqlFunctionName);
    	System.out.println("findKeyCode = "+ds.getString("findKeyCode"));
    	
	    SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
	    Integer returnCountInt = -1;
	    try {
	    	// 조립작업표준서 검색 Data를 만드는 Query 수행
	    	returnCountInt = (Integer)remoteUtil.execute(FINDAISDATASERVICE, makeSqlFunctionName, ds);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
	    
	    System.out.println("returnCountInt = "+returnCountInt.intValue());
	    
	    if(returnCountInt>0){
	    	try {
	    		// 조립작업표준서 검색 Query 수행
	    		resultDataList = (ArrayList<HashMap>)remoteUtil.execute(FINDAISDATASERVICE, findSqlFunctionName, findCondition);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}finally{
	    		
	    	}
	    	
	    	try {
//	    		// 조립작업표준서 검색결과 ComboBox 생성 Data를 위한 XML 문서 생성
//	    		Object k = (Object)remoteUtil.execute(FINDAISDATASERVICE, "findPublishItemRevListDataXML", ds);
//	    		if(k==null){
//	    			System.out.println("k is null");
//	    		}else{
//	    			System.out.println("k is  : "+ k.getClass().getName());
//	    		}
	    		// 조립작업표준서 검색결과 ComboBox 생성 Data를 위한 XML 문서 생성
	    		resultDataList2 = (ArrayList<HashMap<String, Object>>)remoteUtil.execute(FINDAISDATASERVICE, "findPublishItemRevListDataList", ds);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}finally{
	    		
	    	}
	    	
	    	// XML 문서 생성
		    String xmlBody = "";
		    if(resultDataList2!=null){
		    	
		    	for (int i = 0; resultDataList2!=null && i < resultDataList2.size(); i++) {
		    		HashMap<String, Object> rowHash = (HashMap<String, Object>)resultDataList2.get(i);
		    		
		    		String tempOperationId = null; 
		    		String tempOperationRevId = null;
		    		String tempPubItemId = null;
		    		String tempPubRevId = null;
		    		String tempPubUserName = null; 
		    		String tempPubStatus = null;
		    		String tempPubReleasedDate = null; 
		    		String tempPubRevPuid = null;

		    		if(rowHash.get("OPERATION_ID")!=null ) {
		    			tempOperationId = rowHash.get("OPERATION_ID").toString(); 
		    		}
		    		if(rowHash.get("OPERATION_REV_ID")!=null) {
		    			tempOperationRevId = rowHash.get("OPERATION_REV_ID").toString();
		    		}
		    		if(rowHash.get("PUB_ITEM_ID")!=null) {
		    			tempPubItemId = rowHash.get("PUB_ITEM_ID").toString();
		    		}
		    		if(rowHash.get("PUB_REV_ID")!=null) {
		    			tempPubRevId = rowHash.get("PUB_REV_ID").toString();
		    		}
		    		if(rowHash.get("PUB_USER_NAME")!=null) {
		    			tempPubUserName = rowHash.get("PUB_USER_NAME").toString();
		    		}
		    		if(rowHash.get("pub_status")!=null) {
		    			tempPubStatus = rowHash.get("PUB_STATUS").toString();
		    		}
		    		if(rowHash.get("PUB_RELEASED_DATE")!=null) {
		    			tempPubReleasedDate = rowHash.get("PUB_RELEASED_DATE").toString(); 
		    		}
		    		if(rowHash.get("PUB_REV_PUID")!=null) {
		    			tempPubRevPuid = rowHash.get("PUB_REV_PUID").toString();
		    		}
		    		
		    		String text = "" +
			    		"<PubInfo  Operatoin=\""+tempOperationId+"\" Rev=\""+tempOperationRevId+"\" UserName=\""+tempPubUserName+"\" ReleasedDate=\""+tempPubReleasedDate+"\" StatusName=\""+tempPubStatus+"\">\n"+
			    		"<ItemRev  ItemId=\""+tempPubItemId+"\" ItemRevId=\""+tempPubRevId+"\" ItemRevPuid=\""+tempPubRevPuid+"\">\n"+
			    		"</ItemRev>\n"+
			    		"</PubInfo>\n";

		    		if(xmlBody.trim().length()<1){
		    			xmlBody = text;
		    		}else{
		    			xmlBody = xmlBody + text;
		    		}
				}
		    	
			    xmlBody = "<?xml version=\"1.0\" encoding=\"EUC-KR\" ?>\n<DOC>\n"+xmlBody+"</DOC>";
			    //System.out.println(xmlBody);

			    InputSource   is = new InputSource(new StringReader(xmlBody)); 
			    
			    try {
					this.resultXMLDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			    
		    }
		    
	    }
	    
		return resultDataList;
	}
	
	public List<String> getPublishedRevisionListData(String operationId){
		
		List<String> itemRevisionIdList = null;
		
		NodeList  findedNodeList = findNodeListUsingOperationId(operationId);
		
		if(findedNodeList==null || (findedNodeList!=null && findedNodeList.getLength()<1)){
			return itemRevisionIdList;
		}
		
		 itemRevisionIdList = new ArrayList<String>();

		 for( int idx=0; idx<findedNodeList.getLength(); idx++ ){
			 
			 Element element = (Element) findedNodeList.item(idx);
			 String hh = element.getAttribute("UserName");
			 
		      try {
//				String a = xpath.evaluate("@Operatoin", element);
//				String b = xpath.evaluate("@Rev", element);
//				String c = xpath.evaluate("@UserName", element);
//				String d = xpath.evaluate("@ReleasedDate", element);
//				String e = xpath.evaluate("@StatusName", element);
//				String f = xpath.evaluate("ItemRev/@ItemId", element);
				String pubRevid= xpath.evaluate("ItemRev/@ItemRevId", element);
				
				itemRevisionIdList.add(pubRevid);
				
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    	
		 }
		
		return itemRevisionIdList;
	}
	
	private NodeList findNodeListUsingOperationId(String operationId){

		NodeList  findedNodeList = null;

		if(resultXMLDoc==null || (operationId==null || (operationId!=null && operationId.trim().length()<1)) ){
	    	return findedNodeList;
	    }
        
		String expression = "/DOC/PubInfo[@Operatoin='"+operationId+"']";  // Operation값이 35-B1-100N-00
		
		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(resultXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		 
		 return findedNodeList;
	}
}
