package com.symc.plm.rac.prebom.masterlist.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.kgm.common.remote.DataSet;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpTrim;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.service.MasterListService;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.client.model.LovValue;

/**
 * [20160622][ymjang] 도면배포일자 리비전 속성을 Query --> Property 속성 가져오기로 변경함.
 * [20160719][ymjang] 불필요한 Loading 정보 최소화 - Saved Query --> Custom Query 로 변경
 * [20160907][ymjang] 미사용 컬럼 정리
 * [20161006][ymjang] C/O Part의 경우, 실중량이 우선. 없을 경우, 예상중량으로 표기함.
 * [20161019][ymjang] BOM Loading 속도 개선 (SQL을 이용한 DB Query 방식으로 변경함)
 * [20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
 * [20170607][ljg] 부모 하위에 동일한 Part가 두개 있어도 상관없도록 변경 - 송대영 차장 요청
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가
 * [SR171227-049][LJG] N,M,C,D에서 M을 -> M1,M2로 세분화
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 * [SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MasterListDataMapper {

	private  HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> bomlineMap = new HashMap();
	private  HashMap<String, ArrayList<String>> bomlinePuidMap = new HashMap();
	private ArrayList<String> keyList = new ArrayList();
	private HashMap<String, Integer> parentChildCountMap = new HashMap<String, Integer>();  
	private HashMap<String, HashMap<String, Object>> propMap = new HashMap();
	private TCComponentBOMLine fmpLine = null;
	//	private String fmpId = null;
	private TCComponentItemRevision funcRev = null;
	private OSpec ospec = null;
	private List<LovValue> systemCodes = null;
	//	private ArrayList<String> essentialNames = null;
	private boolean isIncludeBOMLine = true;
	private HashMap<String, ArrayList<String>> childMap = new HashMap();
	private HashMap<String, String[]> dcsMap = new HashMap();
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js"); 
	private MasterListService masterListService = null;

	public MasterListDataMapper(TCComponentBOMLine fmpLine, OSpec ospec, ArrayList<String> essentialNames, boolean isIncludeBOMLine) throws TCException, Exception{
		initialize(fmpLine, ospec, essentialNames, isIncludeBOMLine);
	}

	public void initialize(TCComponentBOMLine fmpLine, OSpec ospec, ArrayList<String> essentialNames, boolean isIncludeBOMLine) throws TCException, Exception{
		this.fmpLine = fmpLine;
		//		this.fmpId = fmpLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
		//최상위가 FMP일 수 있다.
		if( fmpLine.parent() == null){
			funcRev = (TCComponentItemRevision)BomUtil.getParent(fmpLine.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if( funcRev == null){
				throw new Exception("Could not found PreFunction.");
			}
		}else{
			funcRev = this.fmpLine.parent().getItemRevision();
			if( funcRev == null){
				throw new Exception("Could not found PreFunction.");
			}else{
				if( !funcRev.getType().equals(TypeConstant.S7_PREFUNCTIONREVISIONTYPE)){
					throw new Exception("Could not found PreFunction.");
				}
			}
		}


		this.ospec = ospec;
		//		this.essentialNames = essentialNames;
		this.isIncludeBOMLine = isIncludeBOMLine;

		systemCodes = BomUtil.getLovValues(fmpLine.getSession(), "S7_SYSTEM_CODE");

		bomlinePuidMap.clear();
		bomlineMap.clear();
		keyList.clear();
		propMap.clear();
		childMap.clear();
	}

	private String getSystemName(String systemCode){
		if( systemCode == null || systemCode.equals("")){
			return "";
		}
		for( LovValue lovValue : systemCodes){
			if( lovValue.getStringValue().equals(systemCode)){
				return lovValue.getDescription();
			}
		}

		return "";
	}

	public void addBomLine(TCComponentBOMLine line, HashMap<String, StoredOptionSet> storedOptionSetMap, String product_project_code) throws Exception{

		//Revision Rule에 의한 리비전이 존재하지 않을 경우.
		if( line.getItemRevision() == null){
			return;
		}

		boolean isNewLineInStructure = false;
		String key = BomUtil.getBomKey(line);
		if( key == null || key.equals("")){
			//Structure Manager에서 추가한 BOMLine은 시스템 키가 존재하지 않으므로 생성함.
			key = BomUtil.getNewSystemRowKey();
			line.setProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, key);
			line.window().save();
			line.refresh();
			isNewLineInStructure = true;
		}

		// [20161019][ymjang] BOM Loading 속도 개선 (SQL을 이용한 DB Query 방식으로 변경함)
		String occThread = line.getProperty(PropertyConstant.ATTR_NAME_BL_OCC_THREAD);
		//String[] lineProps = line.getProperties(BomLineProp.getPropNames());
		//String occThread = lineProps[BomLineProp.OCC_THREAD.ordinal()];

		if( keyList.contains(key)){

			//BOM Line 추가
			// Working BOM은 변경이 되며, BOM Line 객체를 가지고 있다.
			if( isIncludeBOMLine){
				HashMap<String, ArrayList<TCComponentBOMLine>> map = bomlineMap.get(key);
				ArrayList<TCComponentBOMLine> lines = map.get(occThread);
				if( lines == null){
					lines = new ArrayList();
					lines.add(line);
					map.put(occThread, lines);
				}else{
					if( !lines.contains(line)){
						lines.add(line);
					}
				}
				//Parent Move에 의해 하위 BOM Line이 변경 될 수 있으므로,
				//기존에 있던 정보를 Update.
				if( line.isPacked()){
					TCComponentBOMLine[] packedLines = line.getPackedLines();
					for( int i = 0; i < packedLines.length; i++){
						occThread = packedLines[i].getProperty( PropertyConstant.ATTR_NAME_BL_OCC_THREAD);
						lines = map.get(occThread);
						if( lines == null){
							lines = new ArrayList();
							lines.add(packedLines[i]);
							map.put(occThread, lines);
						}else{
							if( !lines.contains(packedLines[i])){
								lines.add(packedLines[i]);
							}
						}
					}
				}

				HashMap<String, Object> map2 = propMap.get(key);
				map2.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, map.size() + "");

				//Release BOM은 변경이 없으며, 수량 체크를 위해 BOM Line Uid만 가지고 있다.
			}else{
				ArrayList<String> list = bomlinePuidMap.get(key);
				if( !list.contains(occThread)){
					list.add(occThread);
				}
				if( line.isPacked()){
					TCComponentBOMLine[] packedLines = line.getPackedLines();
					for( int i = 0; i < packedLines.length; i++){
						occThread = packedLines[i].getProperty( PropertyConstant.ATTR_NAME_BL_OCC_THREAD);
						if( !list.contains(occThread)){
							list.add(occThread);
						}
					}
				}
				HashMap<String, Object> map2 = propMap.get(key);
				map2.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, list.size() + "");
			}

		}else{

			if( isIncludeBOMLine){
				HashMap<String, ArrayList<TCComponentBOMLine>> map = new HashMap();
				ArrayList lines = new ArrayList();
				lines.add(line);
				map.put(occThread, lines);
				if( line.isPacked()){
					TCComponentBOMLine[] packedLines = line.getPackedLines();
					for( int i = 0; i < packedLines.length; i++){
						occThread = packedLines[i].getProperty( PropertyConstant.ATTR_NAME_BL_OCC_THREAD);
						lines = map.get(occThread);
						if( lines == null){
							lines = new ArrayList();
							lines.add(packedLines[i]);
							map.put(occThread, lines);
						}else{
							if( !lines.contains(packedLines[i])){
								lines.add(packedLines[i]);
							}
						}
					}
				}
				bomlineMap.put(key, map);
			}else{
				ArrayList<String> list = new ArrayList();
				if( !list.contains(line.getUid())){
					list.add(line.getUid());
				}
				if( line.isPacked()){
					TCComponentBOMLine[] packedLines = line.getPackedLines();
					for( int i = 0; i < packedLines.length; i++){
						if( !list.contains(packedLines[i].getUid())){
							list.add(packedLines[i].getUid());
						}
					}
				}
				bomlinePuidMap.put(key, list);
			}

			try{
				// [20161019][ymjang] BOM Loading 속도 개선 (SQL을 이용한 DB Query 방식으로 변경함)
				loadPropinSQL(key, line, storedOptionSetMap, isNewLineInStructure, product_project_code);
				//loadProp(key, line, lineProps, storedOptionSetMap, isNewLineInStructure);				
			}catch(Exception e){
				e.printStackTrace();
				keyList.remove(key);
				bomlineMap.remove(key);
				bomlinePuidMap.remove(key);
				propMap.remove(key);

				throw e;
			}

			/** [NoSR][20160404][jclee] 하위 2Lv 이상 Part에 대해 통용되지 않는 Logic이므로 주석 처리. 사용하지 않도록 한다.
			 - 이미 data로 Lv, Seq No순으로 정렬 완료 된 상태로 넘어오므로 단순히 keylist에 추가만 해주면 됨.
			 */
			//Master List상에 모자관계의 순서에 맞추기 위해.
			//			HashMap<String, Object> prop = propMap.get(key);
			//			String parentNo = prop.get("PARENT_NO").toString();
			//			if( parentNo.equals("")){
			//				keyList.add(key);
			//			}else{
			//				boolean bInserted = false;
			//				int parentIdx = -1;
			//				for( int i = 0; !parentNo.equals("") && i < keyList.size();i++){
			//					String tKey = keyList.get(i);
			//					prop = propMap.get(tKey);
			//					String itemId = prop.get(PropertyConstant.ATTR_NAME_ITEMID).toString();
			//					if( parentNo.equals(itemId)){
			//						parentIdx = i;
			//						
			//						int iChildCount = parentChildCountMap.get(parentNo) == null ? 0 : parentChildCountMap.get(parentNo);
			////						keyList.add(i + iChildCount, key);
			//						keyList.add(parentIdx + iChildCount + 1, key);
			//						parentChildCountMap.put(parentNo, iChildCount + 1);
			//						bInserted = true;
			//						break;
			//					}else{
			//						if( parentIdx != -1){
			//							// MLM내 2LV Part 정렬 시 Find No 반대 순으로 정렬되는 버그 수정
			////							int iChildCount = parentChildCountMap.get(parentNo) == null ? 0 : parentChildCountMap.get(parentNo);
			////							keyList.add(i + iChildCount, key);
			//							keyList.add(i, key);
			////							parentChildCountMap.put(parentNo, ++iChildCount);
			//							bInserted = true;
			//							break;
			//						}
			//					}
			//				}
			//				
			//				if( !bInserted){
			//					parentChildCountMap.put(parentNo, 1);
			//					keyList.add(key);
			//				}
			//			}

			keyList.add(key);
		}
	}	

	public static int getLevel(TCComponentBOMLine childLine, int baseLevel) throws TCException{
		TCComponentBOMLine parentLine = childLine.parent();
		if( parentLine == null){
			return 0;
		}

		if( parentLine.getItem().getType().equals(TypeConstant.S7_PREFUNCMASTERTYPE)){
			return baseLevel;
		}else{
			return getLevel(parentLine, baseLevel + 1);
		}
	}

	public int getDataCount(){
		return keyList.size();
	}

	public HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> getBomlineMap() {
		return bomlineMap;
	}

	@SuppressWarnings( "serial" )
	public Vector<Vector> createMasterListData(HashMap<String, Vector> keyRowMapper, 
			HashMap<String, StoredOptionSet> storedOptionSetMap, ScriptEngine engine){

		Vector data = new Vector() {

			@Override
			public synchronized Object clone() {
				Vector<Vector> newData = new Vector();
				for (int i = 0; i < this.elementCount; i++) {
					Vector row = (Vector) elementData[i];
					Vector newRow = new Vector();
					newRow.addAll(row);

					newData.add(newRow);
				}
				return newData;
			}

		};
		for( String key : keyList){
			HashMap<String, Object> map = propMap.get(key);
			int bomLineSize = 0;
			if( isIncludeBOMLine ){
				HashMap<String, ArrayList<TCComponentBOMLine>> bomlines = bomlineMap.get(key);
				if( bomlines != null){
					bomLineSize = bomlines.size();
				}else{
					bomLineSize = 0;
				}
			}else{
				ArrayList<String> bomlines = bomlinePuidMap.get(key);
				if( bomlines != null){
					bomLineSize = bomlines.size();
				}else{
					bomLineSize = 0;
				}
			}
			Vector row = getMasterListRow(map, ospec, storedOptionSetMap, engine, bomLineSize);
			if( row == null || row.isEmpty()){
				continue;
			}
			keyRowMapper.put(key, row);
			data.add(row.clone());
		}

		return data;
	}

	public HashMap<String, ArrayList<String>> getChildRowKeyMap(){
		HashMap<String, ArrayList<String>> childRowKeyMap = new HashMap();
		HashMap<String, ArrayList<String>> parentIdChildRowKeyMap = new HashMap();
		ArrayList<String> keyList = getKeyList();
		for( String key : keyList){
			HashMap<String, Object> propMap = getPropertyMap(key);
			String partNo = propMap.get(PropertyConstant.ATTR_NAME_ITEMID).toString();
			//			String systemRowKey = propMap.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY).toString();
			ArrayList<String> childRowKeys = parentIdChildRowKeyMap.get(partNo);
			if( childRowKeys == null){
				childRowKeys = new ArrayList();
				childRowKeys.add(key);
				parentIdChildRowKeyMap.put(partNo, childRowKeys);
			}else{
				if( !childRowKeys.contains(key)){
					childRowKeys.add(key);
				}
			}
		}

		for( String key : keyList){
			HashMap<String, Object> propMap = getPropertyMap(key);
			String parentNo = propMap.get("PARENT_NO").toString();
			ArrayList<String> parentIdChildRowKeys = parentIdChildRowKeyMap.get(parentNo);
			if( parentIdChildRowKeys != null){
				for(String childRowKey : parentIdChildRowKeys){

					String systemRowKey = propMap.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY).toString();

					ArrayList<String> childRowKeys = childRowKeyMap.get(childRowKey);
					if( childRowKeys == null){
						childRowKeys = new ArrayList();
						childRowKeys.add(systemRowKey);
						childRowKeyMap.put(childRowKey, childRowKeys);
					}else{
						if( !childRowKeys.contains(systemRowKey)){
							childRowKeys.add(systemRowKey);
						}
					}

				}
			}
		}

		return childRowKeyMap;
	}

	public HashMap<String, Object> getPropertyMap(String key){
		HashMap<String, Object> map = propMap.get(key);
		if( map != null){
			return map;
			//			return (HashMap<String, Object>)map.clone();
		}
		return null;
	}

	public void setPropertyMap(String key, HashMap<String, Object> changedProp) throws Exception{
		HashMap<String, Object> map = propMap.get(key);
		if( map == null){
			propMap.put(key, changedProp);
			//			throw new Exception("Could not found Key-Data.");
		}else{
			String[] propNames = changedProp.keySet().toArray(new String[changedProp.size()]);
			for( String propName: propNames){
				map.put(propName, changedProp.get(propName));
			}
		}
	}

	public void removePropertMap(String key, boolean onlyChildMap){
		HashMap map = propMap.get(key);
		String parentId = (String)map.get("PARENT_NO");
		String partId = (String)map.get(PropertyConstant.ATTR_NAME_ITEMID);

		if( !onlyChildMap){
			propMap.remove(key);
			keyList.remove(key);
			bomlineMap.remove(key);
			bomlinePuidMap.remove(key);
		}
		ArrayList<String> childList =  childMap.get(parentId);
		if( childList != null){
			childList.remove(partId);
			if( childList.isEmpty()){
				childMap.remove(parentId);
			}
		}
	}

	/**
	 * Excel 출력용 Master List Row Generate. Trim list의 순서를 정한 후 수행.
	 * [SR160316-025][20160325][jclee] MLM Compare BUG Fix
	 * @param map
	 * @param ospec
	 * @param storedOptionSetMap
	 * @param engine
	 * @param bomlineSize
	 * @param alTrims
	 * @return
	 */
	public static Vector<CellValue> getMasterListRow(HashMap<String, Object> map, OSpec ospec, HashMap<String, StoredOptionSet> storedOptionSetMap, ScriptEngine engine, int bomlineSize) {
		if( map == null){
			return null;
		}
		Vector row = new Vector();

		row.add(convertToCellValue(""));//첫 컬럼은 빈문자열.

		//Item ID는 사용자가 수정할 수 없고.
		//Item ID(Unique Key)에 system row key값을 넣어둔다.
		CellValue itemIdCellValue = convertToCellValue( map.get(PropertyConstant.ATTR_NAME_ITEMID));
		HashMap cellData = new HashMap();
		cellData.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, map.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
		cellData.put(PropertyConstant.ATTR_NAME_UOMTAG, map.get(PropertyConstant.ATTR_NAME_UOMTAG));
		cellData.put(PropertyConstant.ATTR_NAME_OWNINGUSER, map.get(PropertyConstant.ATTR_NAME_OWNINGUSER));
		cellData.put(PropertyConstant.ATTR_NAME_OWNINGGROUP, map.get(PropertyConstant.ATTR_NAME_OWNINGGROUP));
		itemIdCellValue.setData(cellData);

		boolean isEA = map.get(PropertyConstant.ATTR_NAME_UOMTAG).equals("EA") ? true : false;

		row.add(itemIdCellValue);

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_CONTENTS)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_BUDGETCODE)));
		row.add(convertToCellValue( map.get("SYSTEM_NAME")));
		row.add(convertToCellValue( map.get("FUNC")));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_LEV_M)));
		row.add(convertToCellValue( map.get("LEV_A")));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO)));

		row.add(convertToCellValue( map.get("PARENT_NO")));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_OLD_PART_NO)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_ITEMNAME)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_REQ_OPT)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_SPEC_DESC)));

		CellValue tmpCellValue = convertToCellValue( map.get("SPEC_DISP"));
		HashMap dataMap = new HashMap();
		dataMap.put("SPEC", map.get("SPEC"));
		tmpCellValue.setData(dataMap);
		row.add(tmpCellValue);

		//		String nmcd = "";
		String nm = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_CHG_TYPE_NM));
		String cd = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_CHG_CD));
		/** [20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
		if( nm.equals("")){
			if( cd.equals("")){
				row.add(convertToCellValue(""));
			}else{
				row.add(convertToCellValue(cd));
			}
		}else{
			row.add(convertToCellValue(nm));
		} **/

		//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
		if(!CustomUtil.isNullString(cd)){
			row.add(convertToCellValue(cd));
		}
		else{
			if(CustomUtil.isNullString(nm)){
				row.add(convertToCellValue(""));
			}else{
				row.add(convertToCellValue(nm));
			}
		}

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PROJCODE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING))); //[SR170703-020][LJG]Proto Tooling 컬럼 추가
		row.add(convertToCellValue( map.get(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY)));

		ArrayList<OpTrim> trimList = ospec.getTrimList();

		if( isEA){
			if( storedOptionSetMap == null){
				for( OpTrim trim : trimList){
					row.add(convertToCellValue( map.get(trim.getTrim())));
				}

			}else{
				//				CellValue specCellValue = convertToCellValue( map.get("SPEC"));
				CellValue specCellValue = convertToCellValue( map.get("COMPLEX_SPEC"));
				for( OpTrim trim : trimList){
					if( specCellValue.getValue().equals("")){
						row.add(convertToCellValue( bomlineSize == 0 ? "":"" + bomlineSize));
					}else{
						StoredOptionSet sosStd = storedOptionSetMap.get(trim.getTrim() + "_STD");
						StoredOptionSet sosOpt = storedOptionSetMap.get(trim.getTrim() + "_OPT");

						if (sosStd == null) {
							//							row.add(convertToCellValue(""));

							if (sosOpt == null) {
								row.add(convertToCellValue(""));
								continue;
							}

							if( sosOpt.isInclude(engine, specCellValue.getValue())){
								row.add(convertToCellValue(bomlineSize == 0 ? "":"(" + bomlineSize + ")"));
							}else{
								row.add(convertToCellValue(""));
							}

							continue;
						}

						if( sosStd.isInclude(engine, specCellValue.getValue())){
							row.add(convertToCellValue(bomlineSize == 0 ? "":"" + bomlineSize));
						}else{
							if (sosOpt == null) {
								row.add(convertToCellValue(""));
								continue;
							}

							if( sosOpt.isInclude(engine, specCellValue.getValue())){
								row.add(convertToCellValue(bomlineSize == 0 ? "":"(" + bomlineSize + ")"));
							}else{
								row.add(convertToCellValue(""));
							}
						}
					}
				}
			}
		}else{
			for( OpTrim trim : trimList){
				row.add(convertToCellValue( map.get(trim.getTrim())));
			}
		}

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_ESTWEIGHT)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_TARGET_WEIGHT)));
		
		// [CF-1706] WEIGHT MANAGEMENT 칼럼 추가. query 결과를 table에 넣을 vector에 추가 by 전성용(20201224)	
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT)));
		
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_MODULE_CODE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_ALTER_PART)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_DR)));
		// 20200915 seho EJS Column 추가. query 결과를 table에 넣을 vector에 추가
		row.add(convertToCellValue(map.get(PropertyConstant.ATTR_NAME_BL_EJS)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BOX)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)));
		//		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_CIC_DEPT_NM)));

		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동 */
		//		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY)));
		//		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_DVP_USE)));
		//		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_DVP_REQ_DEPT)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_DVP_USE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_CON_DWG_PLAN)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_CON_DWG_TYPE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_ECO_NO)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_OSPECNO))); //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
		row.add(convertToCellValue( map.get(MasterListTablePanel.MASTER_LIST_DCS_NO)));
		row.add(convertToCellValue( map.get(MasterListTablePanel.MASTER_LIST_DCS_DATE)));

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		//		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_ENG_DEPT_NM)));
		//		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_SELECTED_COMPANY)));

		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PRD_TOOL_COST)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_TOTAL)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PUR_DEPT_NM)));
		row.add(convertToCellValue( map.get(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY)));

		return row;
	}

	public static CellValue convertToCellValue(Object obj){
		if( obj instanceof CellValue){
			return (CellValue)obj;
		}else{
			return new CellValue( obj == null ? "":obj.toString());
		}
	}

	/**
	 * parent의 condition을 "AND"로 묶어 리턴(재귀)
	 * 
	 * @param line
	 * @param conditionStr
	 * @return
	 * @throws TCException
	 */
	public static String getConditionSet(TCComponentBOMLine line, String conditionStr) throws TCException{

		String resultCondition = "";
		if( line.window().getTopBOMLine().equals(line) 
				|| line.getItem().getType().equals(TypeConstant.S7_PREFUNCMASTERTYPE)){
			return conditionStr;
		}
		String str = line.getProperty(PropertyConstant.ATTR_NAME_BL_CONDITION);
		str = BomUtil.convertToSimpleCondition(str);
		if( conditionStr == null || conditionStr.equals("")){
			if(str != null && !str.equals("")){
				resultCondition = "(" + str +")";	
			}

		}else{
			if( str != null && !str.equals("")){
				resultCondition = "(" + str +")" + " and " + conditionStr;
			}else{
				resultCondition = conditionStr;
			}
		}

		if( line.parent().getItem().getType().equals(TypeConstant.S7_PREFUNCMASTERTYPE)){
			return resultCondition;
		}else{
			return getConditionSet(line.parent(), resultCondition);
		}
	}

	/**
	 * DB 쿼리를 이용하여 필요한 Property 값을 가져온다.
	 * @param key
	 * @param line
	 * @param lineProps
	 * @param storedOptionSetMap
	 * @param isNewLineInStructure
	 * @throws Exception
	 */
	private void loadPropinSQL(String key, TCComponentBOMLine line, HashMap<String, StoredOptionSet> storedOptionSetMap, boolean isNewLineInStructure, String product_project_code) throws Exception{		

		DataSet dataSet = new DataSet();

		if (isIncludeBOMLine)
			dataSet.put("IS_WORKING", "1"); // Working 우선
		else
			dataSet.put("IS_WORKING", "0"); // Released 우선
		dataSet.put("BASE_DATE", (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
		dataSet.put("PARENT_REV_PUID", line.parent().getItemRevision().getUid());
		dataSet.put("CHILD_ID", line.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
		dataSet.put("SYSTEM_ROW_KEY", key);
		
		//[20180724][CSH]productRevision(Product_Project Code)를 찾는 로직때문에 로딩 속도가 오래걸림. 밖으로 이동.....
		//[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
//		TCComponentItemRevision productRevision = (TCComponentItemRevision)BomUtil.getParent(fmpLine.getItemRevision(), TypeConstant.S7_PREPRODUCTREVISIONTYPE);
//		dataSet.put("PRODUCT_PROJECT_CODE", productRevision.getStringProperty("s7_PROJECT_CODE"));
		dataSet.put("PRODUCT_PROJECT_CODE", product_project_code);
		///////////////////////////////////////////////

		if (masterListService == null)
			masterListService = new MasterListService();
		
		List<Map<String, Object>> resultList = masterListService.getMLMLoadProp(dataSet);

		if (resultList == null || resultList.size() <= 0) 
			return;

		Map<String, Object> resultMap = resultList.get(0);

		//TCComponentItem item = line.getItem();
		TCComponentItemRevision revision = line.getItemRevision();
		HashMap<String, Object> map = propMap.get(key);
		if( map == null){
			map = new HashMap();
			propMap.put(key, map);
		}

		String s7_CHG_TYPE_NM = resultMap.get("S7_CHG_TYPE_NM") == null ? "" : resultMap.get("S7_CHG_TYPE_NM").toString();
		String S7_CHG_CD = resultMap.get("S7_CHG_CD") == null ? "" : resultMap.get("S7_CHG_CD").toString();
		String s7_PRD_PROJECT_CODE = resultMap.get("S7_PRD_PROJECT_CODE") == null ? "" : resultMap.get("S7_PRD_PROJECT_CODE").toString();
		String s7_PROJECT_CODE = resultMap.get("S7_PROJECT_CODE") == null ? "" : resultMap.get("S7_PROJECT_CODE").toString();
		String item_id = resultMap.get("ITEM_ID") == null ? "" : resultMap.get("ITEM_ID").toString();
		String systemCode = resultMap.get("S7_BUDGET_CODE") == null ? "" : resultMap.get("S7_BUDGET_CODE").toString();
		String dcs_info_str = resultMap.get("DCS_INFO") == null ? "" : resultMap.get("DCS_INFO").toString();
		String dcs_no = null;
		String dcs_release_date = null;
		String [] dcs_info = dcs_info_str.split(",");
		if (dcs_info_str != null && !dcs_info_str.equals("")) {
			if (dcs_info != null && dcs_info.length > 0) {
				dcs_no = dcs_info[0];
				dcs_release_date = 	dcs_info[1];	
			}
		}
		String S7_LEV_M = resultMap.get("S7_LEV_M") == null ? "" : resultMap.get("S7_LEV_M").toString();
		String bl_sequence_no = resultMap.get("BL_SEQUENCE_NO") == null ? "" : resultMap.get("BL_SEQUENCE_NO").toString();
		String parentId = resultMap.get("PARENT_ID") == null ? "" : resultMap.get("PARENT_ID").toString();
		String s7_OLD_PART_NO = resultMap.get("S7_OLD_PART_NO") == null ? "" : resultMap.get("S7_OLD_PART_NO").toString();
		String s7_DISPLAY_PART_NO = resultMap.get("S7_DISPLAY_PART_NO") == null ? "" : resultMap.get("S7_DISPLAY_PART_NO").toString();
		String object_name = resultMap.get("OBJECT_NAME") == null ? "" : resultMap.get("OBJECT_NAME").toString();
		String s7_CONTENTS = resultMap.get("S7_CONTENTS") == null ? "" : resultMap.get("S7_CONTENTS").toString();
		String uom_tag = resultMap.get("UOM_TAG") == null ? "" : resultMap.get("UOM_TAG").toString();
		String S7_SPECIFICATION = resultMap.get("S7_SPECIFICATION") == null ? "" : resultMap.get("S7_SPECIFICATION").toString();
		String qty = resultMap.get("BL_QUANTITY") == null ? "" : resultMap.get("BL_QUANTITY").toString();
		String S7_SUPPLY_MODE = resultMap.get("S7_SUPPLY_MODE") == null ? "" : resultMap.get("S7_SUPPLY_MODE").toString();
		String S7_MODULE_CODE = resultMap.get("S7_MODULE_CODE") == null ? "" : resultMap.get("S7_MODULE_CODE").toString();
		String S7_PRE_ALTER_PART = resultMap.get("S7_PRE_ALTER_PART") == null ? "" : resultMap.get("S7_PRE_ALTER_PART").toString();
		String S7_REQ_OPT = resultMap.get("S7_REQ_OPT") == null ? "" : resultMap.get("S7_REQ_OPT").toString();
		String object_desc = resultMap.get("OBJECT_DESC") == null ? "" : resultMap.get("OBJECT_DESC").toString();

		String s7_ACT_WEIGHT = resultMap.get("S7_ACT_WEIGHT") == null ? "" : resultMap.get("S7_ACT_WEIGHT").toString();
		String s7_EST_WEIGHT = resultMap.get("S7_EST_WEIGHT") == null ? "" : resultMap.get("S7_EST_WEIGHT").toString();
		String s7_TGT_WEIGHT = resultMap.get("S7_TGT_WEIGHT") == null ? "" : resultMap.get("S7_TGT_WEIGHT").toString();
		
		// 20200923 seho EJS Column 추가.
		String s7_EJS = resultMap.get("S7_EJS") == null ? "" : resultMap.get("S7_EJS").toString();
		String s7_REGULATION = resultMap.get("S7_REGULATION") == null ? "" : resultMap.get("S7_REGULATION").toString();

		// [CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
		String s7_Weight_Management = resultMap.get("S7_WEIGHT_MANAGEMENT") == null ? "" : resultMap.get("S7_WEIGHT_MANAGEMENT").toString();
		
		String s7_RESPONSIBILITY = resultMap.get("S7_RESPONSIBILITY") == null ? "" : resultMap.get("S7_RESPONSIBILITY").toString();
		String s7_SELECTED_COMPANY = resultMap.get("S7_SELECTED_COMPANY") == null ? "" : resultMap.get("S7_SELECTED_COMPANY").toString();
		String s7_CON_DWG_PLAN = resultMap.get("S7_CON_DWG_PLAN") == null ? "" : resultMap.get("S7_CON_DWG_PLAN").toString();
		String s7_CON_DWG_PERFORMANCE = resultMap.get("S7_CON_DWG_PERFORMANCE") == null ? "" : resultMap.get("S7_CON_DWG_PERFORMANCE").toString();
		String s7_CON_DWG_TYPE = resultMap.get("S7_CON_DWG_TYPE") == null ? "" : resultMap.get("S7_CON_DWG_TYPE").toString();
		String s7_DWG_DEPLOYABLE_DATE = resultMap.get("S7_DWG_DEPLOYABLE_DATE") == null ? "" : resultMap.get("S7_DWG_DEPLOYABLE_DATE").toString();
		String s7_PRD_DWG_PLAN = resultMap.get("S7_PRD_DWG_PLAN") == null ? "" : resultMap.get("S7_PRD_DWG_PLAN").toString();
		String s7_PRD_DWG_PERFORMANCE = resultMap.get("S7_PRD_DWG_PERFORMANCE") == null ? "" : resultMap.get("S7_PRD_DWG_PERFORMANCE").toString();
		String s7_ECO = resultMap.get("S7_ECO") == null ? "" : resultMap.get("S7_ECO").toString();
		String S7_DVP_NEEDED_QTY = resultMap.get("S7_DVP_NEEDED_QTY") == null ? "" : resultMap.get("S7_DVP_NEEDED_QTY").toString();
		String S7_DVP_USE = resultMap.get("S7_DVP_USE") == null ? "" : resultMap.get("S7_DVP_USE").toString();
		String S7_DVP_REQ_DEPT = resultMap.get("S7_DVP_REQ_DEPT") == null ? "" : resultMap.get("S7_DVP_REQ_DEPT").toString();
		String S7_ENG_DEPT_NM = resultMap.get("S7_ENG_DEPT_NM") == null ? "" : resultMap.get("S7_ENG_DEPT_NM").toString();
		String S7_ENG_RESPONSIBLITY = resultMap.get("S7_ENG_RESPONSIBLITY") == null ? "" : resultMap.get("S7_ENG_RESPONSIBLITY").toString();
		String s7_PUR_DEPT_NM = resultMap.get("S7_PUR_DEPT_NM") == null ? "" : resultMap.get("S7_PUR_DEPT_NM").toString();
		String s7_PUR_RESPONSIBILITY = resultMap.get("S7_PUR_RESPONSIBILITY") == null ? "" : resultMap.get("S7_PUR_RESPONSIBILITY").toString();
		String conditionStr = resultMap.get("BL_OCC_MVL_CONDITION") == null ? "" : resultMap.get("BL_OCC_MVL_CONDITION").toString();
		//[SR170703-020][LJG]Proto Tooling 컬럼 추가
		String s7_IS_PROTO_TOOLING = resultMap.get(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING) == null ? "" : resultMap.get(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING).toString();
		
		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
//		String s7_EST_COST_MATERIAL = resultMap.get("S7_EST_COST_MATERIAL") == null ? "" : resultMap.get("S7_EST_COST_MATERIAL").toString();
//		String s7_TARGET_COST_MATERIAL = resultMap.get("S7_TARGET_COST_MATERIAL") == null ? "" : resultMap.get("S7_TARGET_COST_MATERIAL").toString();
//		String s7_PRT_TOOLG_INVESTMENT = resultMap.get("s7_PRT_TOOLG_INVESTMENT") == null ? "" : resultMap.get("s7_PRT_TOOLG_INVESTMENT").toString();
//		String s7_PRD_TOOL_COST = resultMap.get("S7_PRD_TOOL_COST") == null ? "" : resultMap.get("S7_PRD_TOOL_COST").toString();
//		String s7_PRD_SERVICE_COST = resultMap.get("S7_PRD_SERVICE_COST") == null ? "" : resultMap.get("S7_PRD_SERVICE_COST").toString();
//		String s7_PRD_SAMPLE_COST = resultMap.get("S7_PRD_SAMPLE_COST") == null ? "" : resultMap.get("S7_PRD_SAMPLE_COST").toString();
		String s7_EST_COST_MATERIAL = "";
		String s7_TARGET_COST_MATERIAL = "";
		String s7_PRT_TOOLG_INVESTMENT = "";
		String s7_PRD_TOOL_COST = "";
		String s7_PRD_SERVICE_COST = "";
		String s7_PRD_SAMPLE_COST = "";
		
		
		map.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, key);

		//설계 컨셉
		// NM
		if( isNewLineInStructure ){
			map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, "N");
		}else{
			map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, s7_CHG_TYPE_NM);
		}
		// CD
		map.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, S7_CHG_CD);

		
//		if(s7_DISPLAY_PART_NO.equals("85500 XXXXX")){
//			System.out.println("");
//		}
		//NMCD값이 M에 해당하는 경우, PROJCODE가 아닌 PRD_PROJCODE 값을 가져온다.
		String projCode = "";
		String nm = BomUtil.convertToString(s7_CHG_TYPE_NM);
		if( nm.contains("M")){
			projCode = s7_PRD_PROJECT_CODE;
		}else{
			projCode = s7_PROJECT_CODE;
		}
		map.put(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING, s7_IS_PROTO_TOOLING); //[SR170703-020][LJG]Proto Tooling 컬럼 추가
		map.put(PropertyConstant.ATTR_NAME_PROJCODE, projCode);
		map.put(PropertyConstant.ATTR_NAME_ITEMID, item_id);
		map.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, systemCode);

		map.put(MasterListTablePanel.MASTER_LIST_DCS_NO, dcs_no);
		map.put(MasterListTablePanel.MASTER_LIST_DCS_DATE, dcs_release_date);

		map.put("SYSTEM_NAME", getSystemName(systemCode));
		map.put("FUNC", funcRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
		map.put(PropertyConstant.ATTR_NAME_BL_LEV_M, S7_LEV_M);
		map.put(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO, bl_sequence_no);

		//모파트가 FMP이면 ""
		map.put("PARENT_NO", parentId);
		map.put(PropertyConstant.ATTR_NAME_OLD_PART_NO, s7_OLD_PART_NO);
		map.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, s7_DISPLAY_PART_NO);
		map.put(PropertyConstant.ATTR_NAME_ITEMNAME, object_name);

		map.put(PropertyConstant.ATTR_NAME_CONTENTS, s7_CONTENTS);

		//옵션 셋팅
		HashMap<String, ArrayList<TCComponentBOMLine>> bomlines = bomlineMap.get(key);
		ArrayList<String> bomLinePuids = bomlinePuidMap.get(key);

		if(isIncludeBOMLine){
			map.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, bomlines.size() + "");
		}else{
			map.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, bomLinePuids.size() + "");
		}

		//UOM Set
		map.put(PropertyConstant.ATTR_NAME_UOMTAG, uom_tag);

		//EA는 Double형의 Quantity가 올 수 없다.
		boolean isIntegerQty = false; 
		//Integer Type이 아니면 그대로 표기함.
		try{
			double dNum = Double.parseDouble(qty);
			int iNum = (int)dNum;
			if( dNum == iNum){
				qty = "" + iNum;
				isIntegerQty = true;
			}else{
				isIntegerQty = false;
			}
		}catch(NumberFormatException nfe){
			isIntegerQty = false;
			map.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, qty);
		}
		map.put( PropertyConstant.ATTR_NAME_BL_SPEC_DESC, S7_SPECIFICATION);

		ArrayList<OpTrim> trimList = ospec.getTrimList();
		String parentCondition = getConditionSet(line.parent(), null);
		if( (parentCondition != null && !parentCondition.equals("")) || (conditionStr != null && !conditionStr.equals(""))){
			String simpleCondition = "";

			if(conditionStr != null && !conditionStr.equals("")){
				// [20161019][ymjang] BOM Loading 속도 개선 (SQL을 이용한 DB Query 방식으로 변경함)
				//simpleCondition = BomUtil.convertToSimpleCondition(conditionStr);
				simpleCondition = conditionStr;
				String tCondition = BomUtil.removeTrimOptionValue(simpleCondition);
				map.put("SPEC_DISP", tCondition);
			}else{
				map.put("SPEC_DISP", "");
			}

			if( simpleCondition != null && !simpleCondition.equals("")){
				map.put("SPEC", simpleCondition);
			}else{
				map.put("SPEC", "");
			}

			//상위 파트의 컨디션과 'and'
			if( parentCondition != null && !parentCondition.equals("")){
				simpleCondition = parentCondition + (simpleCondition.equals("") ? "" : " and (" + simpleCondition + ")");
			}
			map.put("COMPLEX_SPEC", simpleCondition);

			// Trim 별 Usage 계산
			//			if( !tCondition.equalsIgnoreCase("NONE")){

			int totUsage = 0;
			if( storedOptionSetMap != null){

				for( OpTrim trim : trimList){
					String sosStdName = trim.getTrim() + "_STD";
					String sosOptName = trim.getTrim() + "_OPT";
					StoredOptionSet sosStd = storedOptionSetMap.get(sosStdName);
					StoredOptionSet sosOpt = storedOptionSetMap.get(sosOptName);

					if( sosStd == null || sosOpt == null){
						map.put(trim.getTrim(), "");
						continue;
					}

					if( sosStd.isInclude(engine, simpleCondition)){

						if( isIntegerQty){
							if( isIncludeBOMLine ){
								if( bomlines != null && !bomlines.isEmpty()){
									map.put(trim.getTrim(), "" + bomlines.size());
									totUsage += bomlines.size();
								}else{
									map.put(trim.getTrim(), "");
								}
							}else{
								if( bomLinePuids != null && !bomLinePuids.isEmpty()){
									map.put(trim.getTrim(), "" + bomLinePuids.size());
									totUsage += bomLinePuids.size();
								}else{
									map.put(trim.getTrim(), "");
								}
							}
						}else{
							map.put(trim.getTrim(), qty);
						}


					}else if( sosOpt.isInclude(engine, simpleCondition)){
						if( isIntegerQty){
							if( isIncludeBOMLine ){
								if( bomlines != null && !bomlines.isEmpty()){
									map.put(trim.getTrim(), "(" + bomlines.size() + ")");
									totUsage += bomlines.size();
								}else{
									map.put(trim.getTrim(), "");
								}
							}else{
								if( bomLinePuids != null && !bomLinePuids.isEmpty()){
									map.put(trim.getTrim(), "(" + bomLinePuids.size() + ")");
									totUsage += bomLinePuids.size();
								}else{
									map.put(trim.getTrim(), "");
								}
							}
						}else{
							map.put(trim.getTrim(), qty);
						}
					}else{
						map.put(trim.getTrim(), "");
					}
				}
				map.put("TOT_USAGE", "" + totUsage);
			}
			//			}else{
			//				for( OpTrim trim : trimList){
			//					map.put(trim.getTrim(), "");
			//				}
			//			}

		}else{
			map.put("SPEC", "");
			map.put("SPEC_DISP", "");
			map.put("COMPLEX_SPEC", "");
			for( OpTrim trim : trimList){
				map.put(trim.getTrim(), map.get(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY));
			}
		}

		// 부모 하위에 동일한 Part가 두개 있을수 없다
		if( line.getItem().getType().equals(TypeConstant.S7_PREVEHICLEPARTTYPE)){
			ArrayList<String> childList = childMap.get(map.get("PARENT_NO"));
			if( childList == null){
				childList = new ArrayList();
				childList.add(map.get(PropertyConstant.ATTR_NAME_ITEMID).toString());
				childMap.put(map.get("PARENT_NO").toString(), childList);
			}else{
				//[20170607][ljg] 부모 하위에 동일한 Part가 두개 있어도 상관없도록 변경 - 송대영 차장 요청 
				//				if( childList.contains(map.get(PropertyConstant.ATTR_NAME_ITEMID).toString())){
				//						throw new Exception("Parent Part can not have more than two identical Child Part.");
				//				}else{
				childList.add(map.get(PropertyConstant.ATTR_NAME_ITEMID).toString());
				//				}
			}
		}

		map.put(PropertyConstant.ATTR_NAME_BL_MODULE_CODE, S7_MODULE_CODE);
		map.put(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE, S7_SUPPLY_MODE);
		map.put(PropertyConstant.ATTR_NAME_BL_ALTER_PART, S7_PRE_ALTER_PART);

		map.put("LEV_A", "" + getLevel(line, 1));

		map.put(PropertyConstant.ATTR_NAME_BL_REQ_OPT, S7_REQ_OPT);
		map.put(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION, object_desc);

		//Weight
		// [20161006][ymjang] C/O Part의 경우, 실중량이 우선. 없을 경우, 예상중량으로 표기함.
		// [SR150828-014][20150828][jclee] C/O Part에 대해 실중량 기입
		if ("C".equals(S7_CHG_CD)) {
			if (s7_ACT_WEIGHT == null || s7_ACT_WEIGHT.equals("") || Float.parseFloat(s7_ACT_WEIGHT) == 0f ) {
				map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, s7_EST_WEIGHT);
			} else {
				map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, s7_ACT_WEIGHT);
			}
		} else {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, s7_EST_WEIGHT);
		}

		map.put(PropertyConstant.ATTR_NAME_TARGET_WEIGHT, s7_TGT_WEIGHT);

		// [CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
		map.put(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT, s7_Weight_Management);		
		
		//DR
		map.put(PropertyConstant.ATTR_NAME_DR, s7_REGULATION);
		// 20200923 seho EJS Column 추가.
		map.put(PropertyConstant.ATTR_NAME_BL_EJS, s7_EJS);
		//BOX
		map.put(PropertyConstant.ATTR_NAME_BOX, s7_RESPONSIBILITY);

		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
		map.put(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, s7_EST_COST_MATERIAL);
		map.put(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, s7_TARGET_COST_MATERIAL);

		// 부품개발(양산 투자비)
		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
		map.put(PropertyConstant.ATTR_NAME_PRD_TOOL_COST, s7_PRD_TOOL_COST);
		map.put(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST, s7_PRD_SERVICE_COST);
		map.put(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, s7_PRD_SAMPLE_COST);

		// [20160907][ymjang] 미사용 컬럼 정리
		map.put(PropertyConstant.ATTR_NAME_TOTAL, "");

		//선정업체
		map.put(PropertyConstant.ATTR_NAME_SELECTED_COMPANY, s7_SELECTED_COMPANY);

		//Concept DWG
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PLAN, s7_CON_DWG_PLAN);
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE, s7_CON_DWG_PERFORMANCE);
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_TYPE, s7_CON_DWG_TYPE);
		map.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, s7_DWG_DEPLOYABLE_DATE);

		//도면 작성(양산)
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN, s7_PRD_DWG_PLAN);
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE, s7_PRD_DWG_PERFORMANCE);
		map.put(PropertyConstant.ATTR_NAME_ECO_NO, s7_ECO);

		//PRT-TEST
		map.put(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY, S7_DVP_NEEDED_QTY);
		map.put(PropertyConstant.ATTR_NAME_BL_DVP_USE, S7_DVP_USE);
		map.put(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT, S7_DVP_REQ_DEPT);

		//설계담당
		TCComponentUser owingUser = (TCComponentUser)line.getItemRevision().getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGUSER);
		TCComponentGroup owingGroup = (TCComponentGroup)line.getItemRevision().getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGGROUP);
		//		TCComponentPerson person = (TCComponentPerson)owingUser.getRelatedComponent("person");

		map.put(PropertyConstant.ATTR_NAME_OWNINGUSER, owingUser.getUserId());
		map.put(PropertyConstant.ATTR_NAME_OWNINGGROUP, owingGroup.getGroupName());

		map.put(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, S7_ENG_DEPT_NM);
		map.put(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY, S7_ENG_RESPONSIBLITY);

		//예상 투자비
		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
		map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, s7_PRT_TOOLG_INVESTMENT);

		//구매담당
		map.put(PropertyConstant.ATTR_NAME_PUR_DEPT_NM, s7_PUR_DEPT_NM);
		map.put(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY, s7_PUR_RESPONSIBILITY);
		map.put(PropertyConstant.ATTR_NAME_EMPLOYEE_NO, "");

		//[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
//		if(revision.getType().equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)){
//			TCComponent ccn = revision.getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
//			String ospec_no = ccn.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO);
//			//System.out.println(ospec_no);
//			map.put(PropertyConstant.ATTR_NAME_OSPECNO, ospec_no);
//		}
		
		//[20180307][csh] 설계구상서 및 o-spec no 등록요청 쿼리변경
		String s7_OSPEC_NO = resultMap.get("S7_OSPEC_NO") == null ? "" : resultMap.get("S7_OSPEC_NO").toString();
		map.put(PropertyConstant.ATTR_NAME_OSPECNO, s7_OSPEC_NO);
	}

	public ArrayList<String> getKeyList() {
		return keyList;
	}

}
