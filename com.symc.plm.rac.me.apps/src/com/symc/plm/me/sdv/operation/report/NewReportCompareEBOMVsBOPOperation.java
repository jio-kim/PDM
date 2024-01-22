package com.symc.plm.me.sdv.operation.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.ssangyong.common.remote.DataSet;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentMEOP;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class NewReportCompareEBOMVsBOPOperation extends SimpleSDVExcelOperation {

	private Registry registry;
	private TCComponentItem[] selectedFunctions;
	private TCComponentBOPLine shopBop;
	private TCComponentBOMWindow mProductWindow;
	private BOMTreeTable treeTable;
	
	private List<HashMap<String, Object>> bopEndItemList;
	
	private String shop_code;
	private HashMap<String, Object> lineInfo;
	private HashMap<String, Object> stationInfo;
	private HashMap<String, Object> operationInfo;

	@Override
	public void executeOperation() throws Exception {
		registry = Registry.getRegistry(this);

		bopEndItemList = new ArrayList<HashMap<String,Object>>();
		
		lineInfo = new HashMap<String, Object>();
		stationInfo = new HashMap<String, Object>();
		operationInfo = new HashMap<String, Object>();

		try {
			selectedFunctions = (TCComponentItem[]) localDataMap.get("selectedFunctions").getValue();
			if(selectedFunctions == null || selectedFunctions.length == 0) {
				MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NoSelectFunction.MSG"), 
						registry.getString("Inform.NAME"), MessageBox.INFORMATION);	
				return;
			}
			
			MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
			shopBop = (TCComponentBOPLine) application.getBOMWindow().getTopBOMLine();
			treeTable = application.getViewableTreeTable();
			mProductWindow = getMproductWindow();
			if(mProductWindow == null) {
				MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NotOpenMProduct.MSG"), 
						registry.getString("Inform.NAME"), MessageBox.INFORMATION);
				return;
			}
			
			IDataSet dataSet = getData();

			if (dataSet != null) {
				String defaultFileName = "CompareEBOMVsBOPList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
				transformer.print(mode, templatePreference, defaultFileName, dataSet);
			}
		} catch (Exception e) {
			setExecuteError(e);
		}
	}
	
	private TCComponentBOMWindow getMproductWindow() throws TCException {
		TCComponent mProductRevision = null;
		TCComponent[] meTargetComps = shopBop.getItemRevision().getRelatedComponents("IMAN_METarget");
		for (TCComponent meTargetComp : meTargetComps) {
			String type = meTargetComp.getType();
			if (type.equals(SDVTypeConstant.BOP_MPRODUCT_REVISION)) {
				mProductRevision = meTargetComp;
				break;
			}
		}
		
		if(mProductRevision != null) {
			MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
			AbstractViewableTreeTable[] tables = application.getViewableTreeTables();
			if(tables != null) {
				for(AbstractViewableTreeTable table : tables) {
					if(table instanceof CMEBOMTreeTable) {
						if(mProductRevision.equals(table.getBOMRoot().getItemRevision())) {
							return table.getBOMRoot().window();
						}
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	protected IDataSet getData() throws Exception {
		IDataSet dataSet = new org.sdv.core.common.data.DataSet();
		IDataMap dataMap = new RawDataMap();

		// DB Query를 이용하여 선택한 Function 하위의 End Item 리스트 추출
		List<HashMap<String, Object>> endItemList = getEndItemList();
		dataMap.put("EndItemListMap", endItemList, IData.TABLE_FIELD);
		
		// BOP에 Assign 된 End Item 리스트 추출
		getAssignedEndItemList(shopBop);
		dataMap.put("AssignedEndItemListMap", bopEndItemList, IData.TABLE_FIELD);
		
		// Header에 출력할 정보
		// 선택한 Function Id
		String strFunctions = "";
		for(TCComponentItem function : selectedFunctions) {
			strFunctions += function.getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + ",";
		}
		dataMap.put("FunctionIds", (strFunctions.equals("") ? "" : strFunctions.substring(0, strFunctions.length() - 1)), IData.STRING_FIELD);
		
		// BOP Id
		dataMap.put("BOPId", shopBop.getProperty(SDVPropertyConstant.BL_ITEM_ID), IData.STRING_FIELD);
		
		// Revision Rule
		dataMap.put("MProductRevRule", mProductWindow.getRevisionRule().toDisplayString(), IData.STRING_FIELD);
		dataMap.put("BOPRevRule", shopBop.window().getRevisionRule().toDisplayString(), IData.STRING_FIELD);
		
		dataSet.addDataMap("EndItemList", dataMap);

		return dataSet;
	}
	
    private List<HashMap<String, Object>> getEndItemList() {
    	List<HashMap<String, Object>> endItemList = new ArrayList<HashMap<String,Object>>();

    	CustomMECODao dao = new CustomMECODao();
		DataSet ds = new DataSet();

		try {
			TCComponentRevisionRule revRule = mProductWindow.getRevisionRule();
			Date revRuleDate = revRule.getDateProperty("rule_date");
			String strDate = null;
			if (revRuleDate != null) {
				strDate = new SimpleDateFormat("yyyyMMdd").format(revRuleDate);
			} else {
				strDate = ExcelTemplateHelper.getToday("yyyyMMdd");
			}

			ds.put("release_date", strDate);
			
			for (TCComponentItem function : selectedFunctions) {
				String functionId = function.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				ds.put("function_no", functionId);
	
				List<HashMap<String, Object>> tempList = dao.getEndItemListOnFunction(ds);
				if(tempList != null) {
					endItemList.addAll(tempList);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return endItemList;
    }
    
    private void getAssignedEndItemList(TCComponentBOMLine bomLine) throws Exception {
    	if(isShop(bomLine)) {
    		shop_code = shopBop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
    	} else if(isLine(bomLine)) {
    		lineInfo.clear();
    		lineInfo.put("line_code", bomLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
    		lineInfo.put("line_rev", bomLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
    	} else if(isStation(bomLine)) {
    		stationInfo.clear();
    		stationInfo.put("station_code", bomLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + bomLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
    		stationInfo.put("station_rev", bomLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));    		
    	} else if(isOperation(bomLine)) {
    		if(isAssyOperation(bomLine)) {
    			stationInfo.clear();
    			stationInfo.put("station_code", bomLine.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
    		}

    		setOperationInfo(bomLine);
    	} else if(isEndItem(bomLine)) {
    		TCComponentBOMLine[] lines = null; 
    		if(bomLine.isPacked()) {
    			TCComponentBOMLine[] packedLines = bomLine.getPackedLines();
    			lines = new TCComponentBOMLine[1 + packedLines.length];
    			
    			bomLine.unpack();
    			bomLine.refresh();    			
    			
    			lines[0] = bomLine;
    			for(int i = 0; i < packedLines.length; i++) {
    				lines[i + 1] = packedLines[i];
    			}
    		} else {
    			lines = new TCComponentBOMLine[1];
    			lines[0] = bomLine;
    		}
    		
    		for(TCComponentBOMLine line : lines) {
    			HashMap<String, Object> endItemInfo = setEndItemInfo(line);
    			
    			TCComponentBOMLine endItemLine = SDVBOPUtilities.getAssignSrcBomLine(mProductWindow, line);
    			if(endItemLine != null) {
    				String occPuid = endItemLine.getProperty(SDVPropertyConstant.BL_OCC_FND_OBJECT_ID);
    				endItemInfo.put("MPRODUCT_OCC_PUID", occPuid);
    				
    				bopEndItemList.add(endItemInfo);
    			}    			
    		}

    		return;
    	} else {
    		return;
    	}
    	
    	if(bomLine.getChildrenCount() > 0) {
    		treeTable.getNode(bomLine).loadChildren();

    		AIFComponentContext[] contexts = bomLine.getChildren();
    		for(AIFComponentContext context : contexts) {
    			TCComponentBOMLine comp = (TCComponentBOMLine) context.getComponent();
    			getAssignedEndItemList(comp);
    		}
    	}
    }
    
    private void setOperationInfo(TCComponentBOMLine operationLine) throws TCException {
    	// 공법
		operationInfo.put("operation_id", operationLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

		// 공법 Rev.
		operationInfo.put("operation_rev", operationLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));

		// 공법 Status
		operationInfo.put("operation_status", operationLine.getProperty(SDVPropertyConstant.BL_RELEASE_STATUS));

		// 공법명
		operationInfo.put("operation_name", operationLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

		// 공법 사양
		HashMap<String, Object> option = SDVBOPUtilities.getVariant(operationLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
		operationInfo.put("operation_spec", option.get("printValues"));

		TCComponentChangeItemRevision changeItemRevision = (TCComponentChangeItemRevision) operationLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);

		if (changeItemRevision != null) {
			// IN_MECO
			operationInfo.put("in_meco", changeItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

			// IN_MECO_RELEASED_DATE
			Date date_released = changeItemRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
			if (date_released != null) {
				operationInfo.put("in_meco_released_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date_released));
			}
		}
    }
    
    private HashMap<String, Object> setEndItemInfo(TCComponentBOMLine endItemLine) throws TCException {
    	HashMap<String, Object> endItemInfo = new HashMap<String, Object>();
    	
    	endItemInfo.put("shop_code", shop_code);
    	
    	endItemInfo.put("line_code", lineInfo.get("line_code"));
    	endItemInfo.put("line_rev", lineInfo.get("line_rev"));
    	
    	endItemInfo.put("station_code", stationInfo.get("station_code"));
    	endItemInfo.put("station_rev", stationInfo.get("station_rev"));
    	
    	endItemInfo.put("operation_id", operationInfo.get("operation_id"));
    	endItemInfo.put("operation_rev", operationInfo.get("operation_rev"));
    	endItemInfo.put("operation_status", operationInfo.get("operation_status"));
    	endItemInfo.put("operation_name", operationInfo.get("operation_name"));
    	endItemInfo.put("operation_spec", operationInfo.get("operation_spec"));
    	endItemInfo.put("endItem_quantity", endItemLine.getProperty(SDVPropertyConstant.BL_QUANTITY));
    	endItemInfo.put("in_meco", operationInfo.get("in_meco"));
    	endItemInfo.put("in_meco_released_date", operationInfo.get("in_meco_released_date"));
    	
    	endItemInfo.put("PART_NO", endItemLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
    	endItemInfo.put("VER", endItemLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
    	endItemInfo.put("PART_NAME", endItemLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
    	
    	return endItemInfo;
    }
    
    private boolean isOperation(TCComponentBOMLine bopLine) throws TCException {
    	if(bopLine.getItem() instanceof TCComponentMEOP) {
    		return true;
    	}
    	
    	return false;
    }
    
    private boolean isAssyOperation(TCComponentBOMLine bopLine) throws TCException {
    	String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM.equals(type)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isShop(TCComponentBOMLine bopLine) throws TCException {
    	String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_SHOP_ITEM.equals(type)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isLine(TCComponentBOMLine bopLine) throws TCException {
    	String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_LINE_ITEM.equals(type)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isStation(TCComponentBOMLine bopLine) throws TCException {
    	String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_STATION_ITEM.equals(type)) {
            return true;
        }
        
        return false;
    }

    private boolean isEndItem(TCComponentBOMLine bopLine) throws TCException {
    	String type = bopLine.getItem().getType();
        if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
            return true;
        }
        
        return false;
    }

}
