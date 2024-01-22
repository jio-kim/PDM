package com.symc.plm.me.utils;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

public class BOPLineUtility {

	public static String updateLineToOperationAbsOccId(TCComponentBOMLine bomLine) throws TCException {
		
       	String absOccId = null;
       	
		if(bomLine==null){
			return absOccId;
		}
		
		String itemType = bomLine.getItem().getType();
		//String itemId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
       	//String currentAbsOccId = bomLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
       	
       	if(itemType!=null && itemType.trim().length()>0){
       		
       		if(itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)){
       			String vehicleCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_VEHICLE_CODE);
       			String shopCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_SHOP_CODE);
       			String lineCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
       			absOccId = vehicleCode+"-"+shopCode+"-"+lineCode;
       		}else if(itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)){
       			String vehicleCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_VEHICLE_CODE);
       			String shopCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_SHOP);
       			String lineCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE);
       			String stationCode = bomLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
       			absOccId = vehicleCode+"-"+shopCode+"-"+lineCode+"-"+stationCode;
       		}else if(itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)){
//       			TCComponentBOMLine lineBOMLine = FindConnectedPartUtility.findTypedItemBOMLine(bomLine, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
//       			String vehicleCode = lineBOMLine.getProperty(SDVPropertyConstant.LINE_REV_VEHICLE_CODE);
//       			String shopCode = lineBOMLine.getProperty(SDVPropertyConstant.LINE_REV_SHOP_CODE);
//       			String lineCode = lineBOMLine.getProperty(SDVPropertyConstant.LINE_REV_CODE);
//       			String statoinCode = bomLine.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_CODE);
       			absOccId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
       		}else if(itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM)){
       			absOccId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
       		}else if(itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM)){
       			absOccId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
       		}else if(itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)){
       			absOccId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
       		}
       	}

		if(absOccId!=null && absOccId.trim().length()>0){
			bomLine.setProperty(SDVPropertyConstant.BL_ABS_OCC_ID, absOccId);
		}

		return absOccId;
	}
}
