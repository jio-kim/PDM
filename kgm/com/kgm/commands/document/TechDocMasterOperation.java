package com.kgm.commands.document;

import java.util.HashMap;

import com.kgm.commands.ec.search.FileAttachmentComposite;
import com.kgm.commands.partmaster.Constants;
import com.kgm.common.SYMCClass;
import com.kgm.common.operation.SYMCAbstractCreateOperation;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ExtendedAttributes;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ServiceData;

/**
 * Part 생성 Operation
 * VehPart/StdPart/Material/Product/Variant/Function/FunctionMaster/Project
 * 
 */
@SuppressWarnings("unchecked")
public class TechDocMasterOperation extends SYMCAbstractCreateOperation
{

	/** Manage Dialog에서 넘어온 Param Map */
	HashMap<String, Object> pMap;
	/** 속성 AttrMap */
	HashMap<String, Object> attrMap;
	/** Part No. */
	String strPartNo;
	/** Part Name */
	String strPartName;

	/** Part Item Type */
	String strItemType;

	/** File Composite */
	FileAttachmentComposite fileComposite;
	
    public static final String TC_RETURN_MESSAGE = "ReturnMsg";
    public static final String TC_RETURN_OK = "OK";
    public static final String TC_RETURN_FAIL    = "FAIL";
    public static final String TC_RETURN_FAIL_REASON = "FAIL_REASON";
    public static final String TC_RETURN_STOP_ROW = "STOP_ROW";

	/**
	 * Part 생성 Operation
	 * 
	 * @param dialog : Dialog Instance
	 * @param strItemType : Part Item Type
	 * @param pMap : Manage Dialog에서 넘어온 Param Map
	 * @param attrMap : 속성 AttrMap
	 * @param fileComposite : File Composite
	 */
	public TechDocMasterOperation(AbstractSWTDialog dialog, String strItemType, HashMap<String, Object> pMap, HashMap<String, Object> attrMap, FileAttachmentComposite fileComposite)
	{
		super(dialog);
		this.pMap = pMap;
		this.attrMap = attrMap;
		this.strItemType = strItemType;
		this.fileComposite = fileComposite;
	}

	/**
	 * Item 생성
	 */
    @Override
	public void createItem() throws Exception
	{
		
		strPartNo = ((String) attrMap.get(Constants.ATTR_NAME_ITEMID)).toUpperCase();
		String strPartRev = "A";
		strPartName = (String) attrMap.get(Constants.ATTR_NAME_ITEMNAME);
		
		
		
        ItemProperties itemProperty = new ItemProperties();
        itemProperty.clientId = "1";
        itemProperty.itemId = strPartNo;
        itemProperty.revId = strPartRev;
        itemProperty.name = strPartName;
        itemProperty.type = SYMCClass.S7_TECHDOCTYPE;
        itemProperty.description = "";
        itemProperty.uom = "EA";

        
        ExtendedAttributes extendedattributes1 = new ExtendedAttributes();
        DataManagement.ExtendedAttributes[] atts = new DataManagement.ExtendedAttributes[1];
        atts[0] = extendedattributes1;
        itemProperty.extendedAttributes = atts;
        atts[0].objectType = SYMCClass.S7_TECHDOCTYPE;
//      atts[0].attributes.put("s7_REVISION", attrMap.getValue("s7_REVISION"));
        atts[0].attributes.put("s7_REVISION", attrMap.get("s7_REVISION").toString());

        // *****************************
        // Execute the service operation
        // *****************************
        DataManagementService dataService = DataManagementService.getService(CustomUtil.getTCSession());
        
        CreateItemsResponse response = dataService.createItems(new ItemProperties[] { itemProperty }, null, "");
        
       
        
        if (ServiceDataError(response.serviceData)) {
           
            throw new Exception(makeMessageOfFail(response.serviceData).get(TC_RETURN_FAIL_REASON).toString());
        }
		
        newComp = CustomUtil.findItem(this.strItemType, strPartNo);
		
		// File Composite가 존재하는 경우 Dataset 연결
		if (fileComposite != null && fileComposite.isFileModified())
		{
			fileComposite.createDatasetAndMakerelation(newComp);
		}
		
	}
	
	public static TCComponentItem createTechDoc(String strPartNo , String strPartName) throws Exception
	{
        ItemProperties itemProperty = new ItemProperties();
        itemProperty.clientId = "1";
        itemProperty.itemId = strPartNo;
        itemProperty.revId = "A";
        itemProperty.name = strPartName;
        itemProperty.type = SYMCClass.S7_TECHDOCTYPE;
        itemProperty.description = "";
        itemProperty.uom = "EA";

        
        ExtendedAttributes extendedattributes1 = new ExtendedAttributes();
        DataManagement.ExtendedAttributes[] atts = new DataManagement.ExtendedAttributes[1];
        atts[0] = extendedattributes1;
        itemProperty.extendedAttributes = atts;
        atts[0].objectType = SYMCClass.S7_TECHDOCTYPE;
        atts[0].attributes.put("s7_REVISION", "000");

        // *****************************
        // Execute the service operation
        // *****************************
        DataManagementService dataService = DataManagementService.getService(CustomUtil.getTCSession());
        
        CreateItemsResponse response = dataService.createItems(new ItemProperties[] { itemProperty }, null, "");
        
       
        
        if (ServiceDataError(response.serviceData)) {
           
            throw new Exception(makeMessageOfFail(response.serviceData).get(TC_RETURN_FAIL_REASON).toString());
        }
		
        TCComponentItem newComp = CustomUtil.findItem(SYMCClass.S7_TECHDOCTYPE, strPartNo);
		
		return newComp;
	}
	

    /**
     * 
     * Desc : make error message for notifying to client when is returned exception from ServiceData.
     * @Method Name : makeMessageOfFail
     * @param ServiceData serviceData
     * @return HashMap
     * @Comment
     */
    public static HashMap<String, String> makeMessageOfFail(ServiceData serviceData) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i< serviceData.sizeOfPartialErrors(); i++) {
            ErrorStack errorStack = serviceData.getPartialError(i);
            ErrorValue[] errorValues =errorStack.getErrorValues();
            
            for(ErrorValue errorValue : errorValues){
                sb.append("[TC_ERR_CODE]: "+errorValue.getCode()+"\n");
                sb.append("[TC_ERR_LEV]: "+errorValue.getLevel()+"\n");
                sb.append("[TC_ERR_MSG]: "+errorValue.getMessage()+"\n");
            }
        }
        
        HashMap<String, String> ldata = new HashMap<String, String>();
        ldata.put(TC_RETURN_MESSAGE, TC_RETURN_FAIL);
        ldata.put(TC_RETURN_FAIL_REASON, sb.toString());
        
        return ldata;
    }
	
	
    public static boolean ServiceDataError(final ServiceData serviceData) {
        if(serviceData.sizeOfPartialErrors() > 0)
        {
            for(int i = 0; i < serviceData.sizeOfPartialErrors(); i++)
            {
                for(String msg : serviceData.getPartialError(i).getMessages())
                    System.out.println(msg);
            }

            return true;
        }

        return false;
    }
	
	
	@Override
	public void startOperation() throws Exception
	{

	}

	@Override
	public void endOperation() throws Exception
	{
		//    NavigatorOpenService openService = new NavigatorOpenService();
		//    openService.open(newComp);
	}

	/**
	 * Item 생성 후 Revision에 속성값 저장
	 */
	@Override
	public void setProperties() throws Exception
	{


		// ItemID는 수정 대상이 아님
		attrMap.remove("item_id");




		String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
		TCProperty[] props = newComp.getTCProperties(szKey);

		for (int i = 0; i < props.length; i++)
		{

			if (props[i] == null)
			{
				System.out.println(szKey[i] + " is Null");
				continue;
			}

			Object value = attrMap.get(props[i].getPropertyName());

			CustomUtil.setObjectToPropertyValue(props[i], value);
		}

		// 속성 일괄 반영
		newComp.setTCProperties(props);
		newComp.refresh();


		// File Composite가 존재하는 경우 Dataset 연결
		if (fileComposite != null && fileComposite.isFileModified())
		{
			fileComposite.createDatasetAndMakerelation(newComp);
		}
	}
}
