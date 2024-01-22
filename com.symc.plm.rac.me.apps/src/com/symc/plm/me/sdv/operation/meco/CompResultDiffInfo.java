package com.symc.plm.me.sdv.operation.meco;

import java.math.BigDecimal;
import java.util.HashMap;

public class CompResultDiffInfo {
	
	public String occThreadu;
	public String oldOccPuid;
	public String oldChildItemu; 
	public String oldVariantValue; 
	public String oldOrderNo;
	public String oldSeq;
	public double oldQty;
	public String newOccPuid;
	public String newChildItemu; 
	public String newVariantValue; 
	public String newOrderNo;
	public String newSeq;
	public double newQty;
	public String changeType; 
	public String occTypeName; 
	
	public CompResultDiffInfo(){
		// 값을 초기화
		occThreadu = null;
		oldOccPuid = null;
		oldChildItemu = null; 
		oldVariantValue = null; 
		oldOrderNo = null;
		oldSeq = null;
		oldQty = 0.0d;
		newOccPuid = null;
		newChildItemu = null; 
		newVariantValue = null; 
		newOrderNo = null;
		newSeq = null;
		newQty = 0.0d;
		changeType = null; 
		occTypeName = null; 
	}

	public CompResultDiffInfo(HashMap aHash){
		initQueryResultData(aHash);
	}
	
	private void initQueryResultData(HashMap aHash){
		
		// 값을 초기화
		occThreadu = null;
		oldOccPuid = null;
		oldChildItemu = null; 
		oldVariantValue = null; 
		oldOrderNo = null;
		oldSeq = null;
		oldQty = 0.0d;
		newOccPuid = null;
		newChildItemu = null; 
		newVariantValue = null; 
		newOrderNo = null;
		newSeq = null;
		newQty = 0.0d;
		changeType = null; 
		occTypeName = null; 
		
		if(aHash.get("OCC_THREADU") != null){
			String tempStrV = aHash.get("OCC_THREADU").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				occThreadu = tempStrV.trim();
			}
		}
		
		if(aHash.get("OLD_CHILD_ITEMU") != null){
			String tempStrV = aHash.get("OLD_CHILD_ITEMU").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				oldChildItemu = tempStrV.trim();
			}
		}
		if(aHash.get("OLD_OPTION_COND") != null){
			String tempStrV = aHash.get("OLD_OPTION_COND").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				oldVariantValue = tempStrV.trim();
			}
		}
		if(aHash.get("OLD_ORDER_NO") != null){
			String tempStrV = aHash.get("OLD_ORDER_NO").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				oldOrderNo = tempStrV.trim();
			}
		}
		if(aHash.get("OLD_SEQ_NO") != null){
			String tempStrV = aHash.get("OLD_SEQ_NO").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				oldSeq = tempStrV.trim();
			}
		}
		if(aHash.get("OLD_QTY_V") != null){
			BigDecimal tempNumb = (BigDecimal)aHash.get("OLD_QTY_V");
			if(tempNumb!=null && tempNumb.doubleValue()>0){
				oldQty = tempNumb.doubleValue();
			}
		}
		if(aHash.get("OLD_OCC_PUID") != null){
			String tempStrV = aHash.get("OLD_OCC_PUID").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				oldOccPuid = tempStrV.trim();
			}
		}
		
		if(aHash.get("NEW_CHILD_ITEMU") != null){
			String tempStrV = aHash.get("NEW_CHILD_ITEMU").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				newChildItemu = tempStrV.trim();
			}
		}
		if(aHash.get("NEW_OPTION_COND") != null){
			String tempStrV = aHash.get("NEW_OPTION_COND").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				newVariantValue = tempStrV.trim();
			}
		}
		if(aHash.get("NEW_ORDER_NO") != null){
			String tempStrV = aHash.get("NEW_ORDER_NO").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				newOrderNo = tempStrV.trim();
			}
		}
		if(aHash.get("NEW_SEQ_NO") != null){
			String tempStrV = aHash.get("NEW_SEQ_NO").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				newSeq = tempStrV.trim();
			}
		}
		if(aHash.get("NEW_QTY_V") != null){
			BigDecimal tempNumb = (BigDecimal)aHash.get("NEW_QTY_V");
			if(tempNumb!=null && tempNumb.doubleValue()>0){
				newQty = tempNumb.doubleValue();
			}
		}
		if(aHash.get("NEW_OCC_PUID") != null){
			String tempStrV = aHash.get("NEW_OCC_PUID").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				newOccPuid = tempStrV.trim();
			}
		}
		
		if(aHash.get("CHANGE_TYPE") != null){
			String tempStrV = aHash.get("CHANGE_TYPE").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				changeType = tempStrV.trim();
			}
		}
		
		if(aHash.get("OCC_TYPE_NAME") != null){
			String tempStrV = aHash.get("OCC_TYPE_NAME").toString();
			if(tempStrV!=null && tempStrV.trim().length()>0){
				occTypeName = tempStrV.trim();
			}
		}
	}
	
}
