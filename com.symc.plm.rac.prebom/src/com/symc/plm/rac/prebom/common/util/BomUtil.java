package com.symc.plm.rac.prebom.common.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.StringUtil;
import com.ssangyong.common.utils.variant.VariantErrorCheck;
import com.ssangyong.common.utils.variant.VariantOption;
import com.ssangyong.common.utils.variant.VariantValue;
import com.symc.plm.rac.prebom.ccn.commands.dao.CustomCCNDao;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.masterlist.model.SimpleTcObject;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.RevisionRuleEntry;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OptionConstraint;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput;
import com.teamcenter.services.rac.structuremanagement.StructureFilterWithExpandService;
import com.teamcenter.services.rac.structuremanagement._2014_06.StructureFilterWithExpand;
import com.teamcenter.services.rac.structuremanagement._2014_06.StructureFilterWithExpand.ExpandAndSearchOutput;
import com.teamcenter.soa.client.model.LovValue;

/**
 * [20160907][ymjang] 컬럼명 오류 수정
 * [20170622][ljg] 1.whereUsed를 API 에서 DB쿼리 방식으로 변경(원인을 알수없는 속도 저하때문에), 2.whereUsed Data중 최종 리비전만 가져오도록 변경
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가
 * [20171114][ljg] 오류 발견 되어 SQL 방식에서 -> whereUsed API 방식으로 다시 원복
 * [SR171227-049][LJG] N,M,C,D에서 M을 -> M1,M2로 세분화
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 * [SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
 */
public class BomUtil {

	public static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");
	/**
	 * 옵션을 정의하기 위한 문자열 생성.
	 * 
	 * @param option
	 * @param isExternal
	 * @return
	 */
	public static String getOptionString(VariantOption option, boolean isExternal){
		String name = MVLLexer.mvlQuoteId(option.getOptionName(), false);
		String desc = option.getOptionDesc();
		if(desc.length() > 0)
			desc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(desc)).append(" ").toString();
		else
			desc = " ";
		String s = "public ";
		s += name;
		//      implements ==> uses(외부) 변경함.
		s += (isExternal ? " uses ": " implements ") + desc + MVLLexer.mvlQuoteId(option.getItemId(), true) + ":" + name ;
		return s;
	}

	public static List<LovValue> getLovValues(TCSession session, String string) throws TCException {
		TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
		TCComponentListOfValues[] listofvalues = listofvaluestype.find(string);
		TCComponentListOfValues listofvalue = listofvalues[0];
		List<LovValue> list = listofvalue.getListOfValues().getValues();

		return list;
	}   

	public static String getOptionString(VariantOption option, String itemID){
		String name = MVLLexer.mvlQuoteId(option.getOptionName(), false);
		String desc = option.getOptionDesc();
		if(desc.length() > 0)
			desc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(desc)).append(" ").toString();
		else
			desc = " ";
		String s = "public ";
		s += name;
		s += " string " + desc + "= " ;

		String tmpStr = null;
		List<VariantValue> list = option.getValues();
		for( int i = 0; i < list.size(); i++){
			VariantValue variantValue = list.get(i);
			if( tmpStr == null ){
				tmpStr = MVLLexer.mvlQuoteString(variantValue.getValueName());
			}else{
				tmpStr += ", " + MVLLexer.mvlQuoteString(variantValue.getValueName());
			}
		}

		s += tmpStr;

		return s;
	}

	/**
	 * 기존 Variant Option Value에 Allowed Option Value를 Append하여 반환
	 * @param variantOptionList
	 * @param value
	 * @return
	 */
	public static String getOptionString(ArrayList<VariantOption> variantOptionList, VariantValue value, HashMap<String,String[]> options) {
		String sOptionName = value.getOption().getOptionName();

		for (int inx = 0; inx < variantOptionList.size(); inx++) {
			VariantOption variantOption = variantOptionList.get(inx);

			if (variantOption.getOptionName().equals(sOptionName)) {
				String name = MVLLexer.mvlQuoteId(variantOption.getOptionName(), false);
				String desc = variantOption.getOptionDesc();

				if(desc.length() > 0)
					desc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(desc)).append(" ").toString();
				else
					desc = " ";

//				List<VariantValue> values = variantOption.getValues();
//				
//				String s = "public ";
//				s += name;
//				s += " string " + desc + "= " ;
//
//				String tmpStr = null;
//				for (int jnx = 0; jnx < values.size(); jnx++) {
//					VariantValue variantValue = values.get(jnx);
//
//					if( tmpStr == null ){
//						tmpStr = MVLLexer.mvlQuoteString(variantValue.getValueName());
//					}else{
//						tmpStr += ", " + MVLLexer.mvlQuoteString(variantValue.getValueName());
//					}
//				}
//
//				if (!tmpStr.contains(MVLLexer.mvlQuoteString(value.getValueName()))) {
//					tmpStr += ", " + MVLLexer.mvlQuoteString(value.getValueName());
//				}
//				s += tmpStr;
				
				
				//기존 product이 가지고 있는 option의 Allowed Option Value가 중복으로 들어가 있는 Case가 있음 (왜 중복으로 들어갔는지 확인 필요)
				//중복 값은 있는그대로 표현을 해줘야 업데이트 시 오류 발생하지 않음.
				//중복값 표현을 위해 소스 수정
				
				String s = "public ";
				s += name;
				s += " string " + desc + "= " ;
				
				String tmpStr = null;
				String[] allowdValue = options.get(name);
				for (int jnx = 0; jnx < allowdValue.length; jnx++) {

					if( tmpStr == null ){
						tmpStr = MVLLexer.mvlQuoteString(allowdValue[jnx]);
					}else{
						tmpStr += ", " + MVLLexer.mvlQuoteString(allowdValue[jnx]);
					}
				}

			if (!tmpStr.contains(MVLLexer.mvlQuoteString(value.getValueName()))) {
				tmpStr += ", " + MVLLexer.mvlQuoteString(value.getValueName());
				s += tmpStr;
			} else {
				s = "";
			}
				
//				System.out.println(s);
				return s;
			}
		}

		return "";
	}

	/**
	 * 상부 BOM을 검색하여, targetType이 나올때 까지 올라가고, 찾아서 리턴함.
	 * 
	 * @param childRevision
	 * @param targetType
	 * @return
	 * @throws Exception
	 */
	public static TCComponent getParent(TCComponent childRevision, String targetType) throws Exception{
		String revRuleName = null;
		if( CustomUtil.isReleased(childRevision)){
			revRuleName = "Latest Released";
		}else{
			revRuleName = "Latest Working";
		}
		TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(childRevision.getSession(), revRuleName);
		TCComponent[] imanComps = childRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, null);
		for( int i = 0; imanComps != null && i < imanComps.length; i++){
			if( imanComps[i].getType().equals(targetType)){
				return (TCComponentItemRevision)imanComps[i];
			}else{
				return getParent(imanComps[i], targetType);
			}
		}

		return null;
	}
	
	public static String getParent4Digit(TCComponentItemRevision parentRevision, String type, String gubun) throws Exception{
		String funcStr = "";
		String id = parentRevision.getProperty("item_id");
		if(parentRevision.getType().equals(type)){
			funcStr = id.substring(0,4);
		} else {
			String rev = parentRevision.getProperty("item_revision_id");
			CustomCCNDao dao = new CustomCCNDao();
			if(gubun.equals("NEW")){
				funcStr = dao.getParent4Digit(id, rev, type);
			} else {
				funcStr = dao.getParent4DigitReleased(id, rev, type);
			}
		}
		
		return funcStr;
	}

	public static ArrayList<SimpleTcObject> getOSpecList(TCComponentItemRevision preProductRevision) throws Exception{
		ArrayList<SimpleTcObject> list = new ArrayList<SimpleTcObject>();
		TCComponent[] coms = CustomUtil.queryComponent("SYMC_Search_OspecSet_Revision", new String[]{"Project"}, new String[]{preProductRevision.getProperty("s7_PROJECT_CODE")});
		for (int i = 0; coms != null && i < coms.length; i++) {
			list.add(new SimpleTcObject(coms[i].getProperty("item_id"), coms[i].getProperty("item_revision_id"), coms[i].getUid()));
		}

		return list;
	}

	public static ArrayList<VariantOption> getOptionSet(TCComponentBOMLine line, StringBuffer mvlBuffer, boolean bFlag) throws TCException{

		ArrayList<VariantOption> list = new ArrayList<VariantOption>();
		String itemId = line.getItem().getProperty("item_id");
		TCComponentBOMWindow tccomponentbomwindow = line.window();
		VariantManagementService variantmanagementservice = VariantManagementService.getService(line.getSession());
		VariantManagement.BOMVariantConfigOptionResponse bomvariantconfigoptionresponse = variantmanagementservice.getBOMVariantConfigOptions(tccomponentbomwindow, line);
		VariantManagement.BOMVariantConfigOutput bomvariantconfigoutput = bomvariantconfigoptionresponse.output;
		ArrayList arraylist = new ArrayList();
		for(int i = 0; i < bomvariantconfigoutput.configuredOptions.length; i++)
		{
			//          String itemId = line.getItem().getProperty("item_id");
			VariantManagement.BOMVariantConfigurationOption bomvariantconfigurationoption = bomvariantconfigoutput.configuredOptions[i];
			int optionId = bomvariantconfigurationoption.modularOption.optionId;
			VariantOption option = new VariantOption(null, itemId, bomvariantconfigurationoption.modularOption.optionName, bomvariantconfigurationoption.modularOption.optionDescription, null, optionId);

			String[] values = bomvariantconfigurationoption.modularOption.allowedValues;
			for( int j = 0; values != null && j < values.length; j++){
				//              VariantValue variantValue = new VariantValue( option, values[j], values[j], VariantValue.VALUE_USE
			}
			list.add(option);
		}

		if( bFlag ){
			ModularOptionsInput modularInput = new ModularOptionsInput();
			modularInput.bomWindow = tccomponentbomwindow;
			modularInput.bomLines = new TCComponentBOMLine[]{line};
			VariantManagement.ModularOptionsForBomResponse modularOptionsForBomResponse = variantmanagementservice.getModularOptionsForBom(new ModularOptionsInput[]{modularInput});
			String mvl = modularOptionsForBomResponse.optionsOutput[0].optionsInfo[0].options.mvl;
			String itemPuid = line.getItem().getUid();
			itemPuid = itemPuid.replaceAll("\\$", "___T___");
			mvl = mvl.replaceAll("\\$", "___T___");
			mvl = mvl.replaceAll(itemPuid, itemId);
			mvlBuffer.append(mvl);
		}

		return list;
	}

	public static void copyOptionToPreFMP(TCComponentItemRevision fmpRevision, WaitProgressBar waitProgress) throws Exception{
		TCComponentItemRevision prodRev = (TCComponentItemRevision)getParent(fmpRevision, TypeConstant.S7_PREPRODUCTREVISIONTYPE);
		TCComponentBOMLine prodLine = null, fmpTopLine = null;
		try{
			if( waitProgress != null){
				waitProgress.setStatus("Finding Pre Product.");
			}

			TCSession session = fmpRevision.getSession();
			prodLine = CustomUtil.getBomline(prodRev, session);
			if( prodLine == null){
				throw new Exception("Could not found Pre-Product.");
			}

			OptionManager optionManager = new OptionManager(prodLine, false);
			ArrayList<VariantOption> prodOptionList = null;
			StringBuffer prodMvlBuffer = new StringBuffer();

			if( waitProgress != null){
				waitProgress.setStatus("Looking for a Pre-Product...");
			}
			prodOptionList = getOptionSet(prodLine, prodMvlBuffer, true);
			TCComponentItem fmpItem = fmpRevision.getItem();

			if( waitProgress != null){
				waitProgress.setStatus("Looking for a FMP BOM line...");
			}

			fmpTopLine = CustomUtil.getBomline(fmpRevision, session);
			TCComponentBOMLine fmpLine = findBOMLine(prodLine, fmpItem.getProperty("item_id"));
			if( fmpLine == null){
				throw new Exception("Could not found FMP Line.");
			}
			fmpLine.refresh();

			ArrayList<VariantOption> fmpOptionList = null;
			StringBuffer fmpMvlBuffer = new StringBuffer();
			fmpOptionList = getOptionSet(fmpTopLine, fmpMvlBuffer, false);

			//Fmp에만 있는 옵션은 제거함.
			ArrayList<VariantOption> fmpOptionListClone = (ArrayList<VariantOption>)fmpOptionList.clone();
			fmpOptionListClone.removeAll(prodOptionList);

			ArrayList<VariantOption> prodOptionListClone = (ArrayList<VariantOption>)prodOptionList.clone();
			prodOptionListClone.removeAll(fmpOptionList);

			TCVariantService tcVariantService = session.getVariantService();

			// FMP 옵션 Clear
			if( waitProgress != null){
				waitProgress.setStatus("Clearing FMP BOM line...");
			}
			tcVariantService.setLineMvl(fmpTopLine, "");
			fmpTopLine.window().save();
			if( fmpOptionListClone != null && !fmpOptionListClone.isEmpty()){
				for( VariantOption variantOption : fmpOptionListClone){
					tcVariantService.lineDeleteOption(fmpTopLine, variantOption.getOveOptionId());
				}
				fmpTopLine.window().save();
			}

			//          if( true){
			//              return;
			//          }
			//Pre Product의 옵션을 FMP로 복사
			if( waitProgress != null){
				waitProgress.setStatus("Copying the option of Pre-Product with FMP...");
			}

			if( prodOptionListClone != null && !prodOptionListClone.isEmpty()){

				for( VariantOption variantOption : prodOptionListClone){
					String mvlStr = getOptionString(variantOption, true);
					tcVariantService.lineDefineOption(fmpLine, mvlStr);
				}
				prodLine.window().save();

				if( prodMvlBuffer.length() > 0 ){
					String prodId = prodRev.getItem().getProperty("item_id");
					String fmpId = fmpTopLine.getItem().getProperty("item_id");
					tcVariantService.setLineMvl(fmpTopLine, prodMvlBuffer.toString().replaceAll(prodId, fmpId));
					prodLine.window().save();
				}
			}

		}catch(Exception e){
			throw e;
		}finally{

			if( fmpTopLine != null){
				fmpTopLine.window().close();
				fmpTopLine = null;
			}

			if( prodLine != null){
				prodLine.window().close();
				prodLine = null;
			}
		}
	}

	/**
	 * ItemID로 BOMLine 찾기
	 * 
	 * @param parentBOMLine
	 * @param targetItemID
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOMLine findBOMLine(TCComponentBOMLine parentBOMLine, String targetItemID) throws TCException {
		TCComponentBOMLine targetBOMLine = null;
		String parentId = parentBOMLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
		if( parentId.equals(targetItemID)){
			return parentBOMLine;
		}
		AIFComponentContext[] arrAIFComponentContexts = parentBOMLine.getChildren();
		if (arrAIFComponentContexts != null) {
			for (AIFComponentContext aifComponentContext : arrAIFComponentContexts) {
				TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
				if (targetItemID.equals(tcComponentBOMLine.getItem().getProperty("item_id"))) {
					targetBOMLine = tcComponentBOMLine;
					break;
				}else{
					targetBOMLine = findBOMLine(tcComponentBOMLine, targetItemID);
					if( targetBOMLine != null){
						break;
					}
				}
			}
		}
		return targetBOMLine;
	}

	public static void apply(OptionManager optionManager, Vector<VariantValue> optionData, Vector<String[]> userDefineErrorList
			, ArrayList<VariantOption> optionSetToDelete, ArrayList<String[]> moduleConstraintList
			, TCComponentBOMLine selectedLine, WaitProgressBar waitProgress, boolean isExternal) throws TCException{

		//이전에 정의 되어 있던 옵션은 제외함.
		ArrayList<String> appliedOption = new ArrayList<String>();
		//      for( VariantOption option :selectedLineOptionSet){
		//          appliedOption.add(option.getOptionName());
		//      }

		Registry registry = Registry.getRegistry(BomUtil.class);

		HashMap<String, VariantErrorCheck> notUseErrorMap = new HashMap<String, VariantErrorCheck>();
		HashMap<String, VariantErrorCheck> notDefineErrorMap = new HashMap<String, VariantErrorCheck>();

		TCVariantService tcvariantservice = selectedLine.getSession().getVariantService();

		String itemID = selectedLine.getItem().getProperty("item_id");
		int curNum = 0;
		//설정된 옵션 코드값을 확인합니다.
		if( waitProgress != null){
			waitProgress.setStatus(registry.getString("variant.loadingOptionCode"), true);
		}

		/**
		 * [NoSR][2016.02.19][jclee] 새롭게 Option을 Define하기 위해 기존에 적용되어있는 Option을 가져옴.
		 */
		ArrayList<VariantOption> variantOptionList = null;
		try{
			variantOptionList = optionManager.getOptionSet(selectedLine, null, null, null, true, false);;
		}catch( Exception e){
			//Product에 옵션이 존재하지 않을 경우 에러 발생. 무시함.
			e.printStackTrace();
		}
		
		
		
		
		//product이 가지고 있는 Option들
		ModularOption[] modularOptions = null;
		try{
			modularOptions = optionManager.getModularOptions(selectedLine);
		} catch(Exception e){
		}
		HashMap productRealOptions = new HashMap();
		if( modularOptions != null && modularOptions.length > 0){
			for( ModularOption mOption : modularOptions){
				productRealOptions.put(mOption.optionName, mOption.allowedValues);
			}
		}
		
		
		

		HashMap<String, String> hmAppliedOptions = new HashMap<String, String>();
		for(VariantValue value : optionData){
			curNum++;
			//          VariantValue value = (VariantValue)row.get(0);
			VariantOption option = value.getOption();

//			if(value.getValueName().startsWith("S25")){
//				System.out.println("aaaaaaaaaaaaaaaaa");
//			}
			if( value.getValueStatus() == VariantValue.VALUE_USE ){

				if( !value.isNew() ){
					continue;
				}
				if( !value.isNew() && appliedOption.contains(option.getOptionName())){
					continue;
				}

				if( waitProgress != null){
					waitProgress.setStatus(StringUtil.getString(registry, "variant.addOption", new String[]{option.getOptionName()}), true);
				}
				//              String s = OptionManager.getOptionString(option);
				String s = null;
				if( isExternal){
					s = getOptionString(option, isExternal);
				}else{
					s = getOptionString(option, itemID);
				}

				try{
					tcvariantservice.lineDefineOption(selectedLine, s);
					hmAppliedOptions.put(option.getOptionName(), s);
				}catch(TCException e){
//					e.printStackTrace();

					// 현재 Option에 새로운 Value를 Append
					if (hmAppliedOptions.containsKey(option.getOptionName())) {
						if(!hmAppliedOptions.get(option.getOptionName()).contains(value.getValueName())){
							s = hmAppliedOptions.get(option.getOptionName()) + ", " + MVLLexer.mvlQuoteString(value.getValueName());
						} else {
							s = "";
						}
					} else {
						s = getOptionString(variantOptionList, value, productRealOptions);
					}
					if(!s.equals("")){
						VariantOption variantOption = getVariantOption(variantOptionList, value);
	
						try {
							tcvariantservice.lineChangeOption(selectedLine, variantOption.getOveOption().id, s);
						} catch (Exception e2) {
							// Corporate Option에 선언하지 않은 New Value가 들어왔을 경우 발생할 가능성이 높음.
							// 추 후 Corporate Option에 추가한 이후 수작업으로 추가해주는 방법이 훨씬 효율적이므로 Exception이 발생해도 일단 Print하고나서 그냥 무시하고 진행.
							e2.printStackTrace();
						}
	
						hmAppliedOptions.put(option.getOptionName(), s);
					}
				}finally{
					if( appliedOption.contains(option.getOptionName())){
						continue;
					} else {
						appliedOption.add(option.getOptionName());
						selectedLine.refresh();
					}
				}

				//체크박스를 해제 한 경우
			}else if(value.getValueStatus() == VariantValue.VALUE_NOT_USE){

				VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(option.getOptionName());
				if( notUseErrorcheck == null){
					notUseErrorcheck = new VariantErrorCheck();
					notUseErrorcheck.type = "inform";
					notUseErrorcheck.message = VariantValue.TC_MESSAGE_NOT_USE;
				}

				//              condition 추가                        
				ConditionElement condition = new ConditionElement();
				if( notUseErrorcheck.getConditionSize() == 0 ){
					condition.ifOrAnd = "if";
				}else{
					condition.ifOrAnd = "or";
				}
				condition.item = selectedLine.getItem().getProperty("item_id");
				condition.op = "=";
				condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
				condition.value = value.getValueName();
				condition.valueIsString = true;
				condition.fullName = condition.item + ":" + condition.option;
				notUseErrorcheck.addCondition( condition );     

				notUseErrorMap.put(option.getOptionName(), notUseErrorcheck);
				//옵션 트리에서 옵션테이블로 이동 조차 하지 않은 Value
			}else{

				VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(option.getOptionName());
				if( notDefineErrorcheck == null){
					notDefineErrorcheck = new VariantErrorCheck();
					notDefineErrorcheck.type = "inform";
					notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
				}

				//              condition 추가                        
				ConditionElement condition = new ConditionElement();
				if( notDefineErrorcheck.getConditionSize() == 0 ){
					condition.ifOrAnd = "if";
				}else{
					condition.ifOrAnd = "or";
				}
				condition.item = selectedLine.getItem().getProperty("item_id");
				condition.op = "=";
				condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
				condition.value = value.getValueName();
				condition.valueIsString = true;
				condition.fullName = condition.item + ":" + condition.option;
				notDefineErrorcheck.addCondition( condition );

				notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
			}
		}

		selectedLine.window().save();
		selectedLine.refresh();

		//기존에 있던 옵션값을 모두 뺀 경우는 해당 옵션이 제거됨 
		ArrayList<String> deletedOption = new ArrayList<String>();
		for( VariantOption option : optionSetToDelete){

			if( deletedOption.contains(option.getOptionName())) continue;

			VariantErrorCheck notDefineErrorcheck = new VariantErrorCheck();
			notDefineErrorcheck.type = "inform";
			notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;

			try{
				List<VariantValue> values = option.getValues();
				for( VariantValue value : values){
					//                  condition 추가                        
					ConditionElement condition = new ConditionElement();
					if( notDefineErrorcheck.getConditionSize() == 0 ){
						condition.ifOrAnd = "if";
					}else{
						condition.ifOrAnd = "or";
					}
					condition.item = selectedLine.getItem().getProperty("item_id");
					condition.op = "=";
					condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
					condition.value = value.getValueName();
					condition.valueIsString = true;
					condition.fullName = condition.item + ":" + condition.option;
					notDefineErrorcheck.addCondition( condition );
				}
				notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);

			}catch(TCException tce){
				if( waitProgress != null){
					waitProgress.setStatus(StringUtil.getString(registry, "variant.canNotDeleteOption", new String[]{option.getOptionName()}), true);
				}
				tce.printStackTrace();
				throw tce;
			}finally{
				deletedOption.add(option.getOptionName());
			}
		}
		//삭제되어야 할 옵션셋 초기화.
		optionSetToDelete.clear();

		StringBuilder sb = new StringBuilder();
		Set<String> set = notUseErrorMap.keySet();
		Iterator<String> its = set.iterator();
		while( its.hasNext()){
			String key = its.next();
			VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(key);
			String msg = VariantValue.TC_MESSAGE_NOT_USE;
			ConditionElement[] elements = notUseErrorcheck.getCondition();
			for( int i = 0; elements != null && i < elements.length; i++){
				if( i == 0 ){
					msg += "[";
				}
				msg += (i > 0 ? ", ":"") + elements[i].value;
				if( i == elements.length-1 ){
					msg += "]";
				}
			}
			notUseErrorcheck.message = msg;     
			notUseErrorcheck.appendConstraints(sb);
		}

		set = notDefineErrorMap.keySet();
		its = set.iterator();
		while( its.hasNext()){
			String key = its.next();
			VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(key);
			String msg = VariantValue.TC_MESSAGE_NOT_DEFINE;
			ConditionElement[] elements = notDefineErrorcheck.getCondition();
			for( int i = 0; elements != null && i < elements.length; i++){
				if( i == 0 ){
					msg += "[";
				}
				msg += (i > 0 ? ", ":"") + elements[i].value;
				if( i == elements.length-1 ){
					msg += "]";
				}
			}
			notDefineErrorcheck.message = msg;  
			notDefineErrorcheck.appendConstraints(sb);
		}

		if( waitProgress != null){
			waitProgress.setStatus(registry.getString("variant.checkUserDefineError"), true);
		}

		//사용자 정의 오류 체크
		VariantErrorCheck userDefineErrorcheck = null;
		if( userDefineErrorList != null && !userDefineErrorList.isEmpty()){
			for( String[] errorInfo : userDefineErrorList){
				ConditionElement condition = new ConditionElement();

				if( errorInfo[0] != null && !errorInfo[0].equals("")){
					if( userDefineErrorcheck != null){
						userDefineErrorcheck.appendConstraints(sb);
					}
					userDefineErrorcheck = new VariantErrorCheck();
					userDefineErrorcheck.type = errorInfo[0];
				}
				if( errorInfo[1] != null && !errorInfo[1].equals("")){
					userDefineErrorcheck.message = errorInfo[1];

				}
				condition.ifOrAnd = errorInfo[2];
				condition.item = errorInfo[3];
				condition.op = errorInfo[5];
				condition.option = errorInfo[4];
				condition.value = errorInfo[6];
				condition.valueIsString = true;
				condition.fullName = errorInfo[3] + ":" + errorInfo[4];
				userDefineErrorcheck.addCondition( condition );
			}
			userDefineErrorcheck.appendConstraints(sb);
		}

		if( waitProgress != null){
			waitProgress.setStatus(registry.getString("variant.checkConstraint"), true);
		}
		//내부 모듈 구속 조건
		OptionConstraint moduleConstraintCheck = null;
		if( moduleConstraintList != null && !moduleConstraintList.isEmpty()){
			Vector<ConditionElement> conditionVec = new Vector<ConditionElement>();
			for( String[] moduleConstraint : moduleConstraintList){
				ConditionElement condition = new ConditionElement();
				if( moduleConstraint[0] != null && !moduleConstraint[0].equals("")){
					if( moduleConstraintCheck != null){
						ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
						moduleConstraintCheck.setCondition(conditionElms);
						if(conditionElms != null && conditionElms.length > 0)
						{
							sb.append("if ");
							ConstraintsModel.appendCondition(conditionElms, sb);
							sb.append(" then\n ");
							moduleConstraintCheck.appendConstraint(sb);
							sb.append("\nendif");
						}
						sb.append((char)13);
						conditionVec.clear();
					}
					moduleConstraintCheck = new OptionConstraint();
					moduleConstraintCheck.type = moduleConstraint[0];
					moduleConstraintCheck.fullName = moduleConstraint[4] + ":" + moduleConstraint[1];
					moduleConstraintCheck.item = moduleConstraint[4];
					moduleConstraintCheck.option = moduleConstraint[1];
					moduleConstraintCheck.value = moduleConstraint[2];
					moduleConstraintCheck.valueIsString = true;
				}
				condition.ifOrAnd = moduleConstraint[3];
				condition.item = moduleConstraint[4];
				condition.op = moduleConstraint[6];
				condition.option = moduleConstraint[5];
				condition.value = moduleConstraint[7];
				condition.valueIsString = true;
				condition.fullName = moduleConstraint[4] + ":" + moduleConstraint[5];
				conditionVec.add( condition );
			}
			ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
			moduleConstraintCheck.setCondition(conditionElms);
			if(conditionElms != null && conditionElms.length > 0)
			{
				sb.append("if ");
				ConstraintsModel.appendCondition(conditionElms, sb);
				sb.append(" then\n ");
				moduleConstraintCheck.appendConstraint(sb);
				sb.append("\nendif");
			}
			sb.append((char)13);
		}

		try{
			if( waitProgress != null){
				waitProgress.setStatus(registry.getString("variant.addValidation"), true);
			}
			if( notUseErrorMap.size() > 0 || notDefineErrorMap.size() > 0 
					|| ( userDefineErrorcheck != null && userDefineErrorcheck.getConditionSize() > 0 )
					|| (moduleConstraintList != null && !moduleConstraintList.isEmpty())
					){
				tcvariantservice.setLineMvl(selectedLine, sb.toString());
			}else{
				tcvariantservice.setLineMvl(selectedLine, "");
			}
		}catch(TCException tce){

			if( waitProgress != null){
				waitProgress.setStatus(tce.getDetailsMessage());
			}
			System.out.println(sb.toString());
			tce.printStackTrace();
			throw tce;
		}finally{
			//설정되었던 유효성 체크문이 바뀌었으므로 Window를 저장해야 옵션 삭제가 가능함.
			selectedLine.window().save();
			//          selectedLine.window().refresh();
			selectedLine.refresh();
		}
	}

	/**
	 * Variant Option 반환
	 * VariantValue가 기본으로 제공하는 getOption의 경우 OVEOption을 포함하고 있지 않기 때문에 본 Method를 이용한다.
	 * @param variantOptionList
	 * @param value
	 * @return
	 */
	private static VariantOption getVariantOption(ArrayList<VariantOption> variantOptionList, VariantValue value) {
		if (variantOptionList == null) {
			return null;
		}

		String sOptionName = value.getOption().getOptionName();
		for (int inx = 0; inx < variantOptionList.size(); inx++) {
			VariantOption variantOption = variantOptionList.get(inx);
			String sOptionNameTemp = variantOption.getOptionName();

			if (sOptionNameTemp.equals(sOptionName)) {
				return variantOption;
			}
		}

		return null;
	}

	/**
	 * 모파트의 item_id + '_' + sequence NO
	 * 
	 * @param line
	 * @return
	 * @throws TCException
	 */
	public static String getBomKey(TCComponentBOMLine line) throws TCException{
		//      if( line.parent() == null){
		//          return null;
		//      }
		return line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
		//      return line.parent().getItem().getProperty("item_id") + "_" + line.getProperty("bl_sequence_no") 
		//              + "_" + line.getItem().getProperty("item_id");
		//      return line.parent().getItem().getProperty("item_id") + "_" + line.getProperty("bl_sequence_no");
	}

	public static OSpec getOSpec(TCComponentItemRevision ospecRev) throws Exception{

		String ospecStr = ospecRev.getProperty("item_id") + "-" + ospecRev.getProperty("item_revision_id");
		OSpec ospec = null;

		AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
		for( int i = 0; context != null && i < context.length; i++){
			TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
			if( ospecStr.equals(ds.getProperty("object_name"))){
				File[] files = DatasetService.getFiles(ds);
				//                files = new File[1];
				//                files[0] = new File("C:\\Users\\slobbie\\Documents\\쌍용PreBOM\\Option\\OSPEC_Version_Detail_OSI-C300-001_20150522.xls");
				ospec = OpUtil.getOSpec(files[0]);
				break;
			};
		}

		return ospec;
	}

	public static TCComponentItemRevision getOSpecRevisionWithCCN(TCComponentItemRevision ccnRevision) throws Exception{
		String ospecNo = ccnRevision.getProperty("s7_OSPEC_NO");
		if( ospecNo == null || ospecNo.equals("")){
			throw new TCException("Could not found OSPEC_NO.");
		}
		int idx = ospecNo.lastIndexOf("-");
		if( idx < 0){
			throw new TCException("Invalid OSPEC_NO.");
		}
		String ospecId = ospecNo.substring(0, idx);
		String ospecRevId = ospecNo.substring( idx + 1 );

		return CustomUtil.findItemRevision("S7_OspecSetRevision", ospecId, ospecRevId);
	}

	/**
	 * OSpec Revision을 리턴.
	 * 
	 * @param revision  Pre-FMP Revision or Pre_VehPart Revision
	 * @return
	 * @throws Exception 
	 */
	public static TCComponentItemRevision getOSpecRevision(TCComponentItemRevision revision) throws Exception{
		TCProperty tcProperty = revision.getTCProperty("s7_CCN_NO");
		TCComponentItemRevision ccnRevision = (TCComponentItemRevision)tcProperty.getReferenceValue();
		if( ccnRevision == null){
			throw new TCException("Could not found CCN_NO.");
		}
		String ospecNo = ccnRevision.getProperty("s7_OSPEC_NO");
		if( ospecNo == null || ospecNo.equals("")){
			throw new TCException("Could not found OSPEC_NO.");
		}
		int idx = ospecNo.lastIndexOf("-");
		if( idx < 0){
			throw new TCException("Invalid OSPEC_NO.");
		}
		String ospecId = ospecNo.substring(0, idx);
		String ospecRevId = ospecNo.substring( idx + 1 );

		return CustomUtil.findItemRevision("S7_OspecSetRevision", ospecId, ospecRevId);
	}

	public static String convertToString(Object obj){
		if( obj == null ){
			return "";
		}else{
			return obj.toString();
		}
	}

	public static String convertToSimpleCondition(String condition){
		ArrayList<String> foundOpValueList = new ArrayList<String>();
		Pattern p = Pattern.compile(" or | and |\"[a-zA-Z0-9]{4}\"|\"[a-zA-Z0-9]{5}_STD\"|\"[a-zA-Z0-9]{5}_OPT\"");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			//          System.out.println(m.start() + " " + m.group());
			foundOpValueList.add(m.group().trim());
		}

		String conditionResult = null;
		for( String opValue : foundOpValueList){
			String con = opValue.replaceAll("\"", "");
			if( conditionResult == null){
				conditionResult = con;
			}else{
				conditionResult += " " + con;
			}
		}

		if( conditionResult == null){
			conditionResult = "";
		}

		return conditionResult;
	}

	public static String removeTrimOptionValue(String condition){
		String resultStr = "";
		String[] tmpArray = condition.split(" or ");
		for( int i = 0; tmpArray != null && i < tmpArray.length; i++){
			String str = tmpArray[i].replaceAll("[a-zA-Z0-9]{5}_STD and |[a-zA-Z0-9]{5}_OPT and ", "");
			str = str.replaceAll(" and [a-zA-Z0-9]{5}_STD| and [a-zA-Z0-9]{5}_OPT", "");
			str = str.replaceAll("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT", "").trim(); 
			if( resultStr.equals("")){
				resultStr = str;
			}else{
				resultStr += " or " + str;
			}
		}

		return resultStr;
	}

	public static String getCategory(String optionValue){

		if( optionValue == null || optionValue.length() < 4){
			return null;
		}

		if( optionValue.equals("3C61") || optionValue.equals("3WCC")){
			return "301";
		}else if( optionValue.equals("3F02") || optionValue.equals("3W02")){
			return "302";
		}else if( optionValue.equals("3D00") || optionValue.equals("3WDD")){
			return "303";
		}else if( optionValue.equals("3B16") || optionValue.equals("3W16")){
			return "304";
		}else if( optionValue.equals("3A17") || optionValue.equals("3W17")){
			return "305";
		}else if( optionValue.equals("3A51") || optionValue.equals("3W51")){
			return "321";
		}else if( optionValue.equals("3E35") || optionValue.equals("3W35")){
			return "342";
		}else if( optionValue.equals("3D01") || optionValue.equals("3W01")){
			return "344";
		}else if( optionValue.equals("3A46") || optionValue.equals("3W46")){
			return "345";
		}else if( optionValue.equals("3D25") || optionValue.equals("3W25")){
			return "346";
		}else if( optionValue.indexOf("_STD") > -1 || optionValue.indexOf("_OPT") > -1 || optionValue.equals("NONE")){
			return "TRIM";
		}else{
			return optionValue.substring(0, 3);
		}
	}   

	public static TCComponentItemRevision findLatestItemRevision(String type, String itemId) throws Exception{

		TCComponentItemRevision latestRevision = null;
		TCComponent[] tccomponents = CustomUtil.queryComponent("Item...", new String[]{"Type", "Item ID"}, new String[]{type, itemId});
		if( tccomponents != null && tccomponents.length > 0){
			latestRevision = ((TCComponentItem)tccomponents[0]).getLatestItemRevision();
		}
		return latestRevision;
	}

	public static String getNewId(String project, String systemCode) throws Exception{
		if( project == null || project.equals("")){
			throw new Exception("Could not found Project Code.");
		}

		if( systemCode == null || systemCode.equals("")){
			throw new Exception("Could not found System Code.");
		}

		int maxLenth = 11;
		if( project.length() > 4){
			maxLenth = 13;
		}else{
			maxLenth = 11;
		}

		// [SR없음][20150811][jclee] 채번 방법 변경
		// 보류
		return SYMTcUtil.getNewID(project, maxLenth);
		//        return SYMTcUtil.getNextID(project, maxLenth);
	}

	public static String getNewSystemRowKey() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PARAM", null);
		String sysRowKey = (String)remote.execute("com.ssangyong.service.MasterListService", "getSysGuid", ds);

		return sysRowKey;
	}

	public static String getDwgDeployableDate(String revPuid) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PUID", revPuid);
		String dateStr = (String)remote.execute("com.ssangyong.service.MasterListService", "getDwgDeployableDate", ds);

		return dateStr;
	}

	public static Object getDwgDeployableDateForOspecImport(String revPuid) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PUID", revPuid);
		Object dateStr = (Object)remote.execute("com.ssangyong.service.CCNService", "getDwgDeployableDate", ds);

		return dateStr;
	}

	public static Object getDCSReleasedDate(String revPuid) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PUID", revPuid);
		Object dateStr = (Object)remote.execute("com.ssangyong.service.CCNService", "getDcsReleasedDate", ds);

		return dateStr;
	}

	public static String getBVRModifyDate(String revPuid) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PUID", revPuid);
		String dateStr = (String)remote.execute("com.ssangyong.service.MasterListService", "getBVRModifyDate", ds);

		return dateStr;
	}

	/*
	 *  입력된 Item에서 releaseDate에 가장 가까운 이전 Revision을 리턴하는 함수
	 */
	public static TCComponentItemRevision getItemRevisionOnReleaseDate(TCComponentItem item, Date releaseDate) throws Exception
	{
		try
		{
			TCComponentItemRevision findRevision = null;
			Date findReleaseDate = null;

			for (TCComponentItemRevision releaseReivsion : item.getReleasedItemRevisions())
			{
				Date revReleaseDate = releaseReivsion.getDateProperty(PropertyConstant.ATTR_NAME_DATERELEASED);

				if (revReleaseDate.before(releaseDate))
				{
					if ((findReleaseDate == null) || (findReleaseDate != null && findReleaseDate.before(revReleaseDate)))
					{
						findReleaseDate = revReleaseDate;
						findRevision = releaseReivsion;
					}
				}
			}

			return findRevision;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	public static TCComponentRevisionRule getRevisionRule(String ruleName) throws TCException {

		TCComponentRevisionRule atccomponentrevisionrule[] = TCComponentRevisionRule.listAllRules(CustomUtil.getTCSession());
		for (int i = 0; i < atccomponentrevisionrule.length; i++) {
			if (atccomponentrevisionrule[i].getProperty("object_string").equals(ruleName))
				return atccomponentrevisionrule[i];
		}

		return null;
	}

	public static TCComponentRevisionRule getReleaedRevisionRule(Date date) throws TCException{

		//임시 수정
		//            TCComponentRevisionRule revisionRule = getRevisionRule("Latest Working");
		TCComponentRevisionRule revisionRule = getRevisionRule("Latest Released_revision_rule");
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		TCComponentRevisionRule clonedRevisionRule = revisionRule.copy("Latest Released(" + dateFormat.format(c.getTime()) + ")");
		RevisionRuleEntry[] entries = clonedRevisionRule.getEntries();
		for( int i = 0; entries != null && i < entries.length; i++){
			if( !entries[i].getEntryText().startsWith("Has Status")){
				clonedRevisionRule.removeEntry(entries[i]);
				continue;
			}
		}

		if( date != null){
			RevisionRuleEntry revisionruleentry = clonedRevisionRule.createEntry(3);
			revisionruleentry.getTCComponent().setDateProperty("effective_date", date);
			revisionruleentry.getTCComponent().setLogicalProperty("date_today", false);
			clonedRevisionRule.addEntry(revisionruleentry);
		}
		return clonedRevisionRule;
	}

	/**
	 * BOM Window를 생성하므로, 완전히 사용 후에는 Window Close해야함.
	 * 
	 * @param rev
	 * @param revRuleName
	 * @return
	 * @throws Exception
	 */
	public static TCComponentBOMLine getBomLine(TCComponentItemRevision rev, String revRuleName) throws Exception
	{
		try
		{
			if (rev == null)
				return null;

			TCComponentBOMWindowType winType = (TCComponentBOMWindowType) rev.getSession().getTypeComponent("BOMWindow");
			TCComponentRevisionRule latestReleasedRule = null;
			TCComponentRevisionRule[] allRevisionRules = TCComponentRevisionRule.listAllRules(rev.getSession());
			for (TCComponentRevisionRule revisionRule : allRevisionRules) {
				String ruleName = revisionRule.getProperty("object_name");
				if (ruleName.equals(revRuleName)) {
					latestReleasedRule = revisionRule;
					break;
				}
			}
			if (latestReleasedRule != null)
			{
				TCComponentBOMWindow window = winType.create(latestReleasedRule);
				window.setClearCacheOnClose(true);
				TCComponentBOMLine newTopLine = window.setWindowTopLine(null, rev, null, null);

				return newTopLine;
			}

			return null;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * BOM Window를 생성하므로, 완전히 사용 후에는 Window Close해야함.
	 * 
	 * @param item
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static TCComponentBOMLine getBomLine(TCComponentItem item, Date date) throws Exception{

		TCSession session = item.getSession();
		TCComponentRevisionRule revisionRule = null;
		TCComponentItemRevision revision = null;

		if( date == null){
			TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session
					.getTypeComponent("RevisionRule");
			revisionRule = ruleType.getDefaultRule();
			revision = item.getLatestItemRevision();
		}else{
			revisionRule = getReleaedRevisionRule(date);
			revision = getItemRevisionOnReleaseDate(item, date);
		}
		if( revision == null){
			return null;
		}

		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		TCComponentBOMWindow bomWindow = windowType.create(revisionRule);
		TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, revision, null, null);

		if( date != null){
			topLine.window().setClearCacheOnClose(true);
		}

		return topLine;
	}

	public static ArrayList<String> getEssentialName() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("DATA", null);
		try {
			ArrayList<String> list = (ArrayList<String>)remote.execute("com.ssangyong.service.MasterListService", "getEssentialName", ds);

			return list;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *  데이터 생성 로직 변경으로 사용하지 않아 주석 처리 buildCCNEPL__을 사용함 
	*/
//	public static ArrayList<HashMap<String, Object>> buildCCNEPL(TCComponentChangeItemRevision ccnRevision, boolean checkSoltionItem, WaitProgressBar progress) throws Exception {
////		System.out.println("BomUtil.buildCCNEPL start : "+ new Date());
//		ArrayList<HashMap<String, Object>> arrResultEPL = null;
//		TCComponent[] solutionList = null;
//
//		TCComponentItemRevision ospecRevision = getOSpecRevisionWithCCN(ccnRevision);
//		OSpec ospec = getOSpec(ospecRevision);
//		HashMap<String, StoredOptionSet> optionSetMap = getOptionSet(ospec);
//
////		System.out.println("getOspec end : "+ new Date());
//		if(checkSoltionItem) {
//			solutionList = getSolutionItemsAfterReGenerate(ccnRevision);
//		}else{
//			solutionList = ccnRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
//		}
////		System.out.println("get solutionList end : "+ new Date());
//		TCComponentItemRevision prevItemRevision = null;
//		arrResultEPL = new ArrayList<HashMap<String, Object>>() ;
//
//		if(solutionList.length ==0) {
//
//			/**
//			 * [20161212] Reload 일 경우 Solution Item 에 값이 없을 경우,  현재저장된 EPL 리스트를 삭제함
//			 */
//			if(checkSoltionItem)
//			{
//				CustomCCNDao dao = new CustomCCNDao();
//				dao.deleteEPLAllList(ccnRevision.getProperty(IPropertyName.ITEM_ID));
//			}
//			else
//				MessageBox.post("No exist change data. First perform to create change data.", "Notify", MessageBox.INFORMATION);
//
//
//			return null;
//		}
////		System.out.println("compareChildItems start : "+ new Date());
//		int iiii = 0;
//		for(TCComponent solTccomponent : solutionList) {
//			iiii++;
////			System.out.println(iiii + " / " + solutionList.length);
//			if(progress != null){
//				progress.setStatus("  " + iiii + " / " + solutionList.length);
//			}
//			
//			//TODO: Compare PARENT_MOD_DATE in MECO_EPL to BOMView Last Mod Date of Solution ItemRevision
//			//SYMCBOMEditData.setParent_mod_date(simpleDateFormat.format(tcBomviewRevision.getDateProperty(PropertyConstant.ITEM_LAST_MODIFY_DATE)));
//			prevItemRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)solTccomponent);
//
//			if(null != prevItemRevision && isRelatedCCN(prevItemRevision, TypeConstant.CCN_PROBLEM_ITEM)) {
//				//TODO : implement error. Message.
//				String prevItemNo = prevItemRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
//				MessageBox.post("No exist in problem folder. Contact system administratory. -"+prevItemNo, "Error", MessageBox.ERROR);
//				return null;
//			}
//
//			arrResultEPL.addAll(compareChildItems(ccnRevision, solTccomponent, prevItemRevision, solutionList, ospec, optionSetMap));
//		}
////		System.out.println("compareChildItems end : "+ new Date());
////		System.out.println("BomUtil.buildCCNEPL end : "+ new Date());
//		return arrResultEPL;
//	}

	public static ArrayList<HashMap<String, Object>> buildCCNEPL__(TCComponentChangeItemRevision ccnRevision, boolean checkSoltionItem, WaitProgressBar progress) throws Exception {
//		System.out.println("BomUtil.buildCCNEPL start : "+ new Date());
		ArrayList<HashMap<String, Object>> arrResultEPL = null;
		TCComponent[] solutionList = null;

		TCComponentItemRevision ospecRevision = getOSpecRevisionWithCCN(ccnRevision);
		OSpec ospec = getOSpec(ospecRevision);
		HashMap<String, StoredOptionSet> optionSetMap = getOptionSet(ospec);

//		System.out.println("getOspec end : "+ new Date());
		if(checkSoltionItem) {
			solutionList = getSolutionItemsAfterReGenerate_(ccnRevision);
		}else{
			solutionList = ccnRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
		}
//		System.out.println("get solutionList end : "+ new Date());
		TCComponentItemRevision prevItemRevision = null;
		arrResultEPL = new ArrayList<HashMap<String, Object>>() ;
		
		String ospec_no = ccnRevision.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO);
		
		String ccn_id = ccnRevision.getProperty(IPropertyName.ITEM_ID);
				
		String ccn_owner = ((TCComponentUser)ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGUSER)).getUserId();

		if(solutionList.length ==0) {

			/**
			 * [20161212] Reload 일 경우 Solution Item 에 값이 없을 경우,  현재저장된 EPL 리스트를 삭제함
			 */
			if(checkSoltionItem)
			{
				CustomCCNDao dao = new CustomCCNDao();
				dao.deleteEPLAllList(ccn_id);
			}
			else
				MessageBox.post("No exist change data. First perform to create change data.", "Notify", MessageBox.INFORMATION);


			return null;
		}

//		System.out.println("compareChildItems start : "+ new Date());
		int iiii = 0;
		//old
		
		//new 5번
		/*
		HashMap<String, TCComponent> solHash = new HashMap<String, TCComponent>();
		for(TCComponent solComponent : solutionList) {
			solHash.put(solComponent.getProperty(PropertyConstant.ATTR_NAME_ITEMID), solComponent);
		}
		*/
		for(TCComponent solTccomponent : solutionList) {
			iiii++;
//			System.out.println(iiii + " / " + solutionList.length);
			if(progress != null){
				progress.setStatus("  " + iiii + " / " + solutionList.length);
			}
			//TODO: Compare PARENT_MOD_DATE in MECO_EPL to BOMView Last Mod Date of Solution ItemRevision
			//SYMCBOMEditData.setParent_mod_date(simpleDateFormat.format(tcBomviewRevision.getDateProperty(PropertyConstant.ITEM_LAST_MODIFY_DATE)));
			//old
			/*
			prevItemRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)solTccomponent);
			*/
			//new 7번
			
			prevItemRevision = getPrevItemRevision((TCComponentItemRevision)solTccomponent);
			
			//old
			/*
			if(null != prevItemRevision && isRelatedCCN(prevItemRevision, TypeConstant.CCN_PROBLEM_ITEM)) {
			*/
			//new 4번
			
			if(null != prevItemRevision && !isRelatedCCN__(prevItemRevision, TypeConstant.CCN_PROBLEM_ITEM)) {
			
				//TODO : implement error. Message.
				String prevItemNo = prevItemRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
				MessageBox.post("No exist in problem folder. Contact system administratory. -"+prevItemNo, "Error", MessageBox.ERROR);
//				throw new Exception("No exist in problem folder. Contact system administratory. -"+prevItemNo);
				return null;
			}

			//old
			/*
			arrResultEPL.addAll(compareChildItems(ccnRevision, solTccomponent, prevItemRevision, solutionList, ospec, optionSetMap));
            */
			//new 5번
			/*
			arrResultEPL.addAll(compareChildItems_(ccnRevision, solTccomponent, prevItemRevision, solHash, ospec, optionSetMap));
			*/
			//new 5번, sql
			
			arrResultEPL.addAll(compareChildItems__(ccn_id, ccn_owner, solTccomponent, prevItemRevision, ospec, optionSetMap, ospec_no));
			
		}
//		System.out.println("compareChildItems end : "+ new Date());
//		System.out.println("BomUtil.buildCCNEPL end : "+ new Date());
		return arrResultEPL;
	}
	
	
	public static TCComponentItemRevision getPrevItemRevision(TCComponentItemRevision revision) throws Exception {
		String id = revision.getProperty("item_id");
		String rev = revision.getProperty("item_revision_id");
		TCComponentItemRevision prevRevision = null;
		CustomCCNDao dao = new CustomCCNDao();
		String puid = dao.getPreRevisionPuid(id, rev);
		if(puid != null && !puid.equals("") && !puid.toUpperCase().equals("NULL")){
			prevRevision = (TCComponentItemRevision)revision.getSession().stringToComponent(puid);
		}
		return prevRevision;
	}


	@SuppressWarnings("unchecked")
	public static TCComponent[] getSolutionItemsAfterReGenerate(TCComponentChangeItemRevision changeRevision) throws Exception {
		// 중복 방지 삭제
		TCComponent[] solutionList = changeRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
		TCComponent[] problemList = changeRevision.getRelatedComponents(TypeConstant.CCN_PROBLEM_ITEM);

		HashMap<String, TCComponent> solutionMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> problemMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> changeItemMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> changeItemPrevRevMap = new HashMap<String, TCComponent>(); // 변경된 Item Revision의 이전 Revision 목록. Problem Items에 추가 또는 삭제될 대상 비교 목적.

		for(TCComponent tccomponent : solutionList) {
			solutionMap.put(tccomponent.getUid(), tccomponent);
		}

		for(TCComponent tccomponent : problemList) {
			problemMap.put(tccomponent.getUid(), tccomponent);
		}

		//        AIFComponentContext[] aifcomponentcontexts = changeRevision.whereReferenced();
		//        ArrayList<String> changedItemRevison = new ArrayList<String>();
		//        for(AIFComponentContext aifcomponentcontext : aifcomponentcontexts) {
		//
		//            if(!"0".equals(aifcomponentcontext.getComponent().getProperty("active_seq")) && !(aifcomponentcontext.getComponent() instanceof TCComponentChangeItem)) {
		//                changeItemMap.put(aifcomponentcontext.getComponent().getUid(), (TCComponent)aifcomponentcontext.getComponent());  
		//                
		//                TCComponentItemRevision preRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)aifcomponentcontext.getComponent());
		//                if (preRevision != null) {
		//                    changeItemPrevRevMap.put(preRevision.getUid(), preRevision);
		//                }
		//            }else{
		//                //Skip.
		//            }
		//
		//        }
		/**
		 * [20161212] CCN과 연결된 BOM에 연결된 Part 만 가져와서 저장함
		 */
		String ccnId = changeRevision.getProperty(IPropertyName.ITEM_ID);
		String ccnRevId = changeRevision.getProperty(IPropertyName.ITEM_REVISION_ID);
		CustomCCNDao dao = new CustomCCNDao();
		ArrayList<HashMap<String, String>>  preBOMParts = dao.selectPreBomPartsReferencedFromCCN(ccnId, ccnRevId);
		TCSession tcSession = changeRevision.getSession();
		for(HashMap<String, String> map :preBOMParts)
		{
			String partPuid = map.get("PUID");
			TCComponent partComp = tcSession.stringToComponent(partPuid);
			if(partComp == null)
				continue;
			changeItemMap.put(partPuid, partComp);  
			//old
			
			TCComponentItemRevision preRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)partComp);
			
			//new 7번
			/*
			TCComponentItemRevision preRevision = getPrevItemRevision((TCComponentItemRevision)partComp);
			*/
			if (preRevision != null) 
				changeItemPrevRevMap.put(preRevision.getUid(), preRevision);
		}

		Object[] noAttach_solution_ids = CollectionUtils.subtract(changeItemMap.keySet(),solutionMap.keySet()).toArray(new Object[0]);
		Object[] dettach_solution_ids =  CollectionUtils.subtract(solutionMap.keySet(), changeItemMap.keySet()).toArray(new Object[0]);

		// [SR141117-014][2014.12.19][jclee] 기존 Problem Items에 존재하는 목록과 현재 변경된 Problem Items 목록 비교. 추가해야할 대상과 삭제해야될 대상 구분.
		Object[] noAttach_problem_ids = CollectionUtils.subtract(changeItemPrevRevMap.keySet(),problemMap.keySet()).toArray(new Object[0]);
		Object[] dettach_problem_ids =  CollectionUtils.subtract(problemMap.keySet(), changeItemPrevRevMap.keySet()).toArray(new Object[0]);

		int resultCount1 = noAttach_solution_ids.length;
		if(noAttach_solution_ids!=null && resultCount1 > 0){

			String[] itemRevisions = new String[resultCount1];
			for(int i = 0 ; i < resultCount1 ; i++){
				itemRevisions[i] = noAttach_solution_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0) {
				changeRevision.add(TypeConstant.CCN_SOLUTION_ITEM, tcComponents);
			}
		}


		int resultCount2 = dettach_solution_ids.length;
		if(dettach_solution_ids!=null && resultCount2 > 0){

			String[] itemRevisions = new String[resultCount2];
			for(int i = 0 ; i < resultCount2 ; i++){
				itemRevisions[i] = dettach_solution_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.remove(TypeConstant.CCN_SOLUTION_ITEM,tcComponents);
		}

		// [SR141117-014][2014.12.19][jclee] Problem Items 추가 리스트 반영
		int resultCount3 = noAttach_problem_ids.length;
		if(noAttach_problem_ids!=null && resultCount3 > 0){

			String[] itemRevisions = new String[resultCount3];
			for(int i = 0 ; i < resultCount3 ; i++){
				itemRevisions[i] = noAttach_problem_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0) {
				changeRevision.add(TypeConstant.CCN_PROBLEM_ITEM, tcComponents);
			}
		}

		// [SR141117-014][2014.12.19][jclee] Problem Items 제거 리스트 반영
		int resultCount4 = dettach_problem_ids.length;
		if(dettach_problem_ids!=null && resultCount4 > 0){

			String[] itemRevisions = new String[resultCount4];
			for(int i = 0 ; i < resultCount4 ; i++){
				itemRevisions[i] = dettach_problem_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.remove(TypeConstant.CCN_PROBLEM_ITEM,tcComponents);
		}

		return changeRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
	}
	
	@SuppressWarnings("unchecked")
	public static TCComponent[] getSolutionItemsAfterReGenerate_(TCComponentChangeItemRevision changeRevision) throws Exception {
		// 중복 방지 삭제
		TCComponent[] solutionList = changeRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
		TCComponent[] problemList = changeRevision.getRelatedComponents(TypeConstant.CCN_PROBLEM_ITEM);

		HashMap<String, TCComponent> solutionMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> problemMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> changeItemMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> changeItemPrevRevMap = new HashMap<String, TCComponent>(); // 변경된 Item Revision의 이전 Revision 목록. Problem Items에 추가 또는 삭제될 대상 비교 목적.

		for(TCComponent tccomponent : solutionList) {
			solutionMap.put(tccomponent.getUid(), tccomponent);
		}

		for(TCComponent tccomponent : problemList) {
			problemMap.put(tccomponent.getUid(), tccomponent);
		}

		//        AIFComponentContext[] aifcomponentcontexts = changeRevision.whereReferenced();
		//        ArrayList<String> changedItemRevison = new ArrayList<String>();
		//        for(AIFComponentContext aifcomponentcontext : aifcomponentcontexts) {
		//
		//            if(!"0".equals(aifcomponentcontext.getComponent().getProperty("active_seq")) && !(aifcomponentcontext.getComponent() instanceof TCComponentChangeItem)) {
		//                changeItemMap.put(aifcomponentcontext.getComponent().getUid(), (TCComponent)aifcomponentcontext.getComponent());  
		//                
		//                TCComponentItemRevision preRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)aifcomponentcontext.getComponent());
		//                if (preRevision != null) {
		//                    changeItemPrevRevMap.put(preRevision.getUid(), preRevision);
		//                }
		//            }else{
		//                //Skip.
		//            }
		//
		//        }
		/**
		 * [20161212] CCN과 연결된 BOM에 연결된 Part 만 가져와서 저장함
		 */
		String ccnId = changeRevision.getProperty(IPropertyName.ITEM_ID);
		String ccnRevId = changeRevision.getProperty(IPropertyName.ITEM_REVISION_ID);
		CustomCCNDao dao = new CustomCCNDao();
		ArrayList<HashMap<String, String>>  preBOMParts = dao.selectPreBomPartsReferencedFromCCN(ccnId, ccnRevId);
		TCSession tcSession = changeRevision.getSession();
		for(HashMap<String, String> map :preBOMParts)
		{
			String partPuid = map.get("PUID");
			TCComponent partComp = tcSession.stringToComponent(partPuid);
			if(partComp == null)
				continue;
			changeItemMap.put(partPuid, partComp);  
			//old
			/*
			TCComponentItemRevision preRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)partComp);
			*/
			//new 7번
			
			TCComponentItemRevision preRevision = getPrevItemRevision((TCComponentItemRevision)partComp);
			
			if (preRevision != null) 
				changeItemPrevRevMap.put(preRevision.getUid(), preRevision);
		}

		Object[] noAttach_solution_ids = CollectionUtils.subtract(changeItemMap.keySet(),solutionMap.keySet()).toArray(new Object[0]);
		Object[] dettach_solution_ids =  CollectionUtils.subtract(solutionMap.keySet(), changeItemMap.keySet()).toArray(new Object[0]);

		// [SR141117-014][2014.12.19][jclee] 기존 Problem Items에 존재하는 목록과 현재 변경된 Problem Items 목록 비교. 추가해야할 대상과 삭제해야될 대상 구분.
		Object[] noAttach_problem_ids = CollectionUtils.subtract(changeItemPrevRevMap.keySet(),problemMap.keySet()).toArray(new Object[0]);
		Object[] dettach_problem_ids =  CollectionUtils.subtract(problemMap.keySet(), changeItemPrevRevMap.keySet()).toArray(new Object[0]);

		int resultCount1 = noAttach_solution_ids.length;
		if(noAttach_solution_ids!=null && resultCount1 > 0){

			String[] itemRevisions = new String[resultCount1];
			for(int i = 0 ; i < resultCount1 ; i++){
				itemRevisions[i] = noAttach_solution_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0) {
				changeRevision.add(TypeConstant.CCN_SOLUTION_ITEM, tcComponents);
			}
		}


		int resultCount2 = dettach_solution_ids.length;
		if(dettach_solution_ids!=null && resultCount2 > 0){

			String[] itemRevisions = new String[resultCount2];
			for(int i = 0 ; i < resultCount2 ; i++){
				itemRevisions[i] = dettach_solution_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.remove(TypeConstant.CCN_SOLUTION_ITEM,tcComponents);
		}

		// [SR141117-014][2014.12.19][jclee] Problem Items 추가 리스트 반영
		int resultCount3 = noAttach_problem_ids.length;
		if(noAttach_problem_ids!=null && resultCount3 > 0){

			String[] itemRevisions = new String[resultCount3];
			for(int i = 0 ; i < resultCount3 ; i++){
				itemRevisions[i] = noAttach_problem_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0) {
				changeRevision.add(TypeConstant.CCN_PROBLEM_ITEM, tcComponents);
			}
		}

		// [SR141117-014][2014.12.19][jclee] Problem Items 제거 리스트 반영
		int resultCount4 = dettach_problem_ids.length;
		if(dettach_problem_ids!=null && resultCount4 > 0){

			String[] itemRevisions = new String[resultCount4];
			for(int i = 0 ; i < resultCount4 ; i++){
				itemRevisions[i] = dettach_problem_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.remove(TypeConstant.CCN_PROBLEM_ITEM,tcComponents);
		}

		return changeRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
	}

	public static TCSession getTCSession() {
		return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
	}

	//old
	
	public static boolean isRelatedCCN (TCComponentItemRevision itemRevision, String rel_name) throws TCException {

		TCComponentItemRevision changeItemRevision = (TCComponentItemRevision) itemRevision.getRelatedComponent(rel_name);
		if(null != changeItemRevision) {
			return true;
		}
		return false;
	}
	
	//new 4번
	
	public static boolean isRelatedCCN__ (TCComponentItemRevision itemRevision, String rel_name) throws TCException {
		String[] relationNames = new String[]{rel_name};
		AIFComponentContext[] referenced = itemRevision.whereReferencedByTypeRelation( null, relationNames);
		if(referenced == null || referenced.length == 0){
			return false;
		} else {
			return true;
		}
	}
	
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 사용하지 않아 주석 처리 isChangeBOMLineProp__을 사용함 
	*/
//	@SuppressWarnings("unchecked")
	//old
	
//	public static ArrayList<HashMap<String, Object>> compareChildItems(TCComponentChangeItemRevision ccnRevision, TCComponent solutionComponent, TCComponent problemComponent, TCComponent[] solutionList, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap) throws Exception {
//
//		HashMap<String, TCComponent> solutionBOM = new HashMap<String, TCComponent>();
//		HashMap<String, TCComponent> problemBOM = new HashMap<String, TCComponent>();
//		HashMap<String, Object> ccnEditData = null;
//		ArrayList<HashMap<String, Object>> arrCcnBomData = new ArrayList<HashMap<String, Object>>();
//		ArrayList<TCComponentBOMWindow> alOpenedSolutionBOMWindow = new ArrayList<TCComponentBOMWindow>();
//		ArrayList<TCComponentBOMWindow> alOpenedProblemBOMWindow = new ArrayList<TCComponentBOMWindow>();
//
//		try{
//			
//			//Get parent of SolutionItem, but except parent in solutionFolder.
//			ArrayList<TCComponent> arrParentList = addHistoryOnlyRevise((TCComponentItemRevision)solutionComponent, solutionList);
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss"); 
//			if(arrParentList.size() > 0) {
//				for(TCComponent parentComponent : arrParentList) {
//					TCComponentBOMLine parentBomline = getBomline((TCComponentItemRevision)parentComponent, getTCSession());
//					ccnEditData = new HashMap<String, Object>();
//
//					setParentTobomEditData(ccnRevision,(TCComponentItemRevision)parentComponent, ccnEditData, null);
//
//					if(problemComponent !=null) {
//						setOldPartTobomEditData(getTargetBOMLine(parentBomline, (TCComponentItemRevision)problemComponent, "Latest Released"), ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					}
//					setNewPartTobomEditData(getTargetBOMLine(parentBomline, (TCComponentItemRevision)solutionComponent, null), ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					arrCcnBomData.add(ccnEditData);
//				}
//			}
//			//      String[] newBoMrops = null;
//			//Solution
//			if(null != solutionComponent) {
//				alOpenedSolutionBOMWindow = setChildsOnHashMap(solutionComponent, "Latest Working", solutionBOM);
//			}
//
//			//Problem
//			if(null != problemComponent) {
//				alOpenedProblemBOMWindow = setChildsOnHashMap(problemComponent, "Latest Released", problemBOM);
//			}
//			// if Change QTY (add or delete occthread)
//			Object[] change_qty_old_bom_ids = CollectionUtils.subtract(problemBOM.keySet(), solutionBOM.keySet()).toArray(new Object[0]);
//			Object[] change_qty_new_bom_ids = CollectionUtils.subtract(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//
//			HashMap<String, TCComponent[]> bomTemp = new HashMap<String, TCComponent[]>();
//
//			// 수량 변경 BOM Line인지 확인
//			for (int inx = 0; inx < change_qty_old_bom_ids.length; inx++) {
//				ccnEditData = new HashMap<String, Object>();
//				String sOLDBOMCombinationKey = change_qty_old_bom_ids[inx].toString();
//
//				// Combination Key에서 Item ID를 제거 후 OCC Thread 값만 남긴다.
//				// Combination Key : [ItemID]|[OccThreadUIDs]
//				String sOCCThreadsOLDBOM = sOLDBOMCombinationKey.substring(sOLDBOMCombinationKey.indexOf('|') + 1, sOLDBOMCombinationKey.length());
//
//				// OLD BOM의 ID를 14자리(Occ UID 자리수)로 Split
//				String[] saOldBOMIDs = StringUtil.getSplitString(sOCCThreadsOLDBOM, 14);
//
//				for (int jnx = 0; jnx < change_qty_new_bom_ids.length; jnx++) {
//					boolean isChangeQTY = false;
//					String sNEWBOMCombinationKey = change_qty_new_bom_ids[jnx].toString();
//					// Combination Key에서 Item ID를 제거 후 OCC Thread 값만 남긴다.
//					// Combination Key : [ItemID]|[OccThreadUIDs]
//					String sOCCThreadsNEWBOM = sNEWBOMCombinationKey.substring(sNEWBOMCombinationKey.indexOf('|') + 1, sNEWBOMCombinationKey.length());
//
//					// NEW BOM의 ID를 14자리(Occ UID 자리수)로 Split
//					String[] saNewBOMIDs = StringUtil.getSplitString(sOCCThreadsNEWBOM, 14);
//
//					// BOM Line 수, System Row Key가 같을 경우 수량변경 EPL 추출 하지 않음.
//					// (수량을 변경했다가 다시 원상복구했을때)
//					if (saOldBOMIDs.length == saNewBOMIDs.length) {
//						// System Row Key가 서로 같은지 확인하고 추 후 Revise 내역 추출 로직에서 재 처리
//						TCComponentBOMLine change_qty_pro_bomline = null;
//						TCComponentBOMLine change_qty_sol_bomline = null;
//
//						change_qty_pro_bomline =  (TCComponentBOMLine)problemBOM.get(sOLDBOMCombinationKey);
//						change_qty_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(sNEWBOMCombinationKey);
//
//						if (change_qty_pro_bomline != null && change_qty_sol_bomline != null) {
//							String[] change_qty_pro_properties = change_qty_pro_bomline.getProperties(bomlineProperties);
//							String[] change_qty_sol_properties = change_qty_sol_bomline.getProperties(bomlineProperties);
//
//							if (change_qty_pro_properties[16].equals(change_qty_sol_properties[16])) {
//								// Revise 내역 추출 로직에서 재처리하기 위한 임시 변수에 저장 <System Row Key, TCComponent{old, new}>
//								bomTemp.put(change_qty_pro_properties[16], new TCComponent[] {change_qty_pro_bomline, change_qty_sol_bomline});
//
//								problemBOM.remove(sOLDBOMCombinationKey);
//								solutionBOM.remove(sNEWBOMCombinationKey);
//
//								continue;
//							}
//						}
//					}
//
//					for (int knx = 0; knx < saOldBOMIDs.length; knx++) {
//						String sOldBOMID = saOldBOMIDs[knx];
//
//						for (int lnx = 0; lnx < saNewBOMIDs.length; lnx++) {
//							String sNewBOMID = saNewBOMIDs[lnx];
//
//							// 현재 비교대상 ID가 OLD BOM에도 있고 NEW BOM에도 있는데 서로 Combination 이 달라 현 로직에 들어왔다면 수량이 변경된 경우임.
//							//  * 한개라도 같을 경우 로직 발동
//							if (sOldBOMID.equals(sNewBOMID)) {
//								TCComponentBOMLine change_qty_sol_bomline = null;
//								TCComponentBOMLine change_qty_pro_bomline = null;
//
//								change_qty_pro_bomline =  (TCComponentBOMLine)problemBOM.get(sOLDBOMCombinationKey);
//								change_qty_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(sNEWBOMCombinationKey);
//
//								setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//								setOldPartTobomEditData(change_qty_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//								setNewPartTobomEditData(change_qty_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//
//								arrCcnBomData.add(ccnEditData);
//
//								problemBOM.remove(sOLDBOMCombinationKey);
//								solutionBOM.remove(sNEWBOMCombinationKey);
//
//								isChangeQTY = true;
//								break;
//							}
//						}
//
//						if (isChangeQTY) {
//							break;
//						}
//					}
//
//					if (isChangeQTY) {
//						break;
//					}
//				}
//			}
//
//			//if exist only problem, then "Delete"
//			Object[] del_bom_ids = CollectionUtils.subtract(problemBOM.keySet(), solutionBOM.keySet()).toArray(new Object[0]);
//			Object[] add_bom_ids_temp = CollectionUtils.subtract(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine del_bomline = null;
//			for(Object del_bom_id : del_bom_ids) {
//				//{"bl_item_item_id","bl_occ_fnd0objectId", "bl_occ_int_order_no", "bl_item_object_type"(3), "bl_rev_object_name", "bl_quantity", "bl_variant_condition", "bl_abs_occ_id(7)", "bl_rev_item_revision_id", "bl_rev_s7_DISPLAY_PART_NO" };
//				del_bomline =  (TCComponentBOMLine)problemBOM.get(del_bom_id);
//
//				// [SR없음][20151102][jclee] Cut and Paste 대응
//				String[] delProperties = del_bomline.getProperties(bomlineProperties);
//
//				boolean isCutAndPaste = false;
//
//				for (Object add_item_id_temp : add_bom_ids_temp) {
//					TCComponentBOMLine add_bomline_temp =  (TCComponentBOMLine)solutionBOM.get(add_item_id_temp);
//					String[] addTempProperties = add_bomline_temp.getProperties(bomlineProperties);
//
//					boolean isModified = false;
//					if (compareString(addTempProperties[0], delProperties[0])	// Item ID
//							&& compareString(addTempProperties[1], delProperties[1])	// Seq No
//							&& compareString(addTempProperties[4], delProperties[4])	// Quantity
//							&& compareString(addTempProperties[16], delProperties[16])	// System Row Key
//							) {
//						for (int inx = 0; inx < bomlineProperties.length; inx++) {
//							if (!compareString(addTempProperties[inx], delProperties[inx])) {
//								isModified = true;
//								break;
//							}
//						}
//					} else {
//						continue;
//					}
//
//					if (!isModified) {
//						solutionBOM.remove(del_bomline);
//						isCutAndPaste = true;
//						break;
//					}
//				}
//
//				if (!isCutAndPaste) {
//					ccnEditData = new HashMap<String, Object>();
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(del_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CUT);
//
//					arrCcnBomData.add(ccnEditData);
//				}
//
//				problemBOM.remove(del_bom_id);
//			}
//
//			//if exist only solution, then "Add"
//			Object[] add_bom_ids = CollectionUtils.subtract(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//			Object[] del_bom_ids_temp = CollectionUtils.subtract(problemBOM.keySet(), solutionBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine add_bomline = null;
//			for(Object add_bom_id : add_bom_ids) {
//				add_bomline =  (TCComponentBOMLine)solutionBOM.get(add_bom_id);
//				String[] addProperties = add_bomline.getProperties(bomlineProperties);
//
//				// [SR없음][20151102][jclee] Cut and Paste 대응
//				boolean isCutAndPaste = false;
//
//				for (Object del_item_id_temp : del_bom_ids_temp) {
//					TCComponentBOMLine del_bomline_temp =  (TCComponentBOMLine)solutionBOM.get(del_item_id_temp);
//					String[] delTempProperties = del_bomline_temp.getProperties(bomlineProperties);
//
//					boolean isModified = false;
//					if (compareString(delTempProperties[0], addProperties[0])	// Item ID
//							&& compareString(delTempProperties[1], addProperties[1])	// Seq No
//							&& compareString(delTempProperties[4], addProperties[4])	// Quantity
//							&& compareString(delTempProperties[16], addProperties[16])	// System Row Key
//							) {
//						for (int inx = 0; inx < bomlineProperties.length; inx++) {
//							if (!compareString(delTempProperties[inx], addProperties[inx])) {
//								isModified = true;
//								break;
//							}
//						}
//					} else {
//						continue;
//					}
//
//					if (!isModified) {
//						problemBOM.remove(add_bomline);
//						isCutAndPaste = true;
//						break;
//					}
//				}
//
//				if (!isCutAndPaste) {
//					ccnEditData = new HashMap<String, Object>();
//					setParentTobomEditData(ccnRevision,(TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setNewPartTobomEditData(add_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_ADD);
//
//					arrCcnBomData.add(ccnEditData);
//				}
//
//				solutionBOM.remove(add_bom_id);
//			}
//
//			//if exist both pro. and sol. then "Revise" or no changed.
//			Object[] revise_bom_ids = CollectionUtils.intersection(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine revise_sol_bomline = null;
//			TCComponentBOMLine revise_pro_bomline = null;
//
//			for(Object revise_bom_id : revise_bom_ids) {
//
//				revise_pro_bomline =  (TCComponentBOMLine)problemBOM.get(revise_bom_id);
//				revise_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(revise_bom_id);
//				ccnEditData = new HashMap<String, Object>();
//
//				// if not equals, then "Revise"
//				if(isChangeBOMLineProp(revise_pro_bomline, revise_sol_bomline)) {
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(revise_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					setNewPartTobomEditData(revise_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//
//					arrCcnBomData.add(ccnEditData);
//				}
//
//				problemBOM.remove(revise_bom_id);
//				solutionBOM.remove(revise_bom_id);
//			}
//
//			// 수량 변경 여부 확인 로직에서 넘어온 항목 중 Revise 이력이 있는지 확인.
//			// (수량과 System Row Key가 같으면서 OCC Thread가 완전 다른 경우)
//			revise_bom_ids = bomTemp.keySet().toArray();
//			for(Object revise_bom_id : revise_bom_ids) {
//				TCComponent[] tempComponents = bomTemp.get(revise_bom_id);
//				revise_pro_bomline =  (TCComponentBOMLine)tempComponents[0];
//				revise_sol_bomline =  (TCComponentBOMLine)tempComponents[1];
//				ccnEditData = new HashMap<String, Object>();
//
//				// if not equals, then "Revise"
//				if(isChangeBOMLineProp(revise_pro_bomline, revise_sol_bomline)) {
//
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(revise_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					setNewPartTobomEditData(revise_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//
//					arrCcnBomData.add(ccnEditData);
//					bomTemp.remove(revise_bom_id);
//				}
//			}
//
//			//Replace (Change)
//
//			Object[] replace_bom_ids = (solutionBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine replace_sol_bomline = null;
//			TCComponentBOMLine replace_pro_bomline = null;
//			for(Object replace_bom_id : replace_bom_ids) {
//
//				replace_pro_bomline =  (TCComponentBOMLine)problemBOM.get(replace_bom_id);
//				replace_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(replace_bom_id);
//				ccnEditData = new HashMap<String, Object>();
//
//				setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//				setOldPartTobomEditData(replace_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//				setNewPartTobomEditData(replace_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//
//				arrCcnBomData.add(ccnEditData);
//
//				problemBOM.remove(replace_bom_id);
//				solutionBOM.remove(replace_bom_id);
//			}
//
//			// 수량 변경 여부 확인 로직에서 넘어온 항목 중 Replace 이력이 있는지 확인.
//			// (수량과 System Row Key가 같으면서 OCC Thread가 완전 다른 경우)
//			replace_bom_ids = (bomTemp.keySet()).toArray(new Object[0]);
//			for(Object replace_bom_id : replace_bom_ids) {
//
//				TCComponent[] tempComponents = bomTemp.get(replace_bom_id);
//				replace_pro_bomline =  (TCComponentBOMLine)tempComponents[0];
//				replace_sol_bomline =  (TCComponentBOMLine)tempComponents[1];
//				ccnEditData = new HashMap<String, Object>();
//
//				String sProItemID = replace_pro_bomline.getItem().getProperty("item_id");
//				String sSolItemID = replace_sol_bomline.getItem().getProperty("item_id");
//
//				// System Row Key가 같으면서 Item ID가 다른 경우 Replace로 인식
//				if (!sProItemID.equals(sSolItemID)) {
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(replace_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//					setNewPartTobomEditData(replace_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//
//					arrCcnBomData.add(ccnEditData);
//					bomTemp.remove(replace_bom_id);
//				}
//			}
//
//			if (alOpenedSolutionBOMWindow != null && alOpenedSolutionBOMWindow.size() > 0) {
//				for (int inx = 0; inx < alOpenedSolutionBOMWindow.size(); inx++) {
//					alOpenedSolutionBOMWindow.get(inx).close();
//				}
//			}
//
//			if (alOpenedProblemBOMWindow != null && alOpenedProblemBOMWindow.size() > 0) {
//				for (int inx = 0; inx < alOpenedProblemBOMWindow.size(); inx++) {
//					alOpenedProblemBOMWindow.get(inx).close();
//				}
//			}
//		}catch(Exception ex) {
//			ex.printStackTrace();
//		}
//
//		return arrCcnBomData;
//
//	}
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 사용하지 않아 주석 처리 isChangeBOMLineProp__을 사용함 
	*/
	//new 5번,6번,7번
	
//	public static ArrayList<HashMap<String, Object>> compareChildItems_(TCComponentChangeItemRevision ccnRevision, TCComponent solutionComponent, TCComponent problemComponent, HashMap<String, TCComponent> solHash, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap) throws Exception {
//
//		HashMap<String, TCComponent> solutionBOM = new HashMap<String, TCComponent>();
//		HashMap<String, TCComponent> problemBOM = new HashMap<String, TCComponent>();
//		HashMap<String, Object> ccnEditData = null;
//		ArrayList<HashMap<String, Object>> arrCcnBomData = new ArrayList<HashMap<String, Object>>();
//		ArrayList<TCComponentBOMWindow> alOpenedSolutionBOMWindow = new ArrayList<TCComponentBOMWindow>();
//		ArrayList<TCComponentBOMWindow> alOpenedProblemBOMWindow = new ArrayList<TCComponentBOMWindow>();
//
////		try{
//			
//			//Get parent of SolutionItem, but except parent in solutionFolder.
//			ArrayList<TCComponent> arrParentList = addHistoryOnlyRevise_((TCComponentItemRevision)solutionComponent, solHash);
////			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss"); 
//			if(arrParentList.size() > 0) {
//				for(TCComponent parentComponent : arrParentList) {
////					TCComponentBOMLine parentBomline = getBomline((TCComponentItemRevision)parentComponent, getTCSession());
//					TCComponentBOMWindow bwParent = getBOMWindow((TCComponentItemRevision)parentComponent, "Latest Working", "bom_view");
//					ccnEditData = new HashMap<String, Object>();
//
//					setParentTobomEditData(ccnRevision,(TCComponentItemRevision)parentComponent, ccnEditData, null);
//
//					if(problemComponent !=null) {
////						setOldPartTobomEditData(getTargetBOMLine(parentBomline, (TCComponentItemRevision)problemComponent, "Latest Released"), ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//						setOldPartTobomEditData(getTargetBOMLine_(bwParent, (TCComponentItemRevision)problemComponent, "Latest Released"), ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					}
////					setNewPartTobomEditData(getTargetBOMLine(parentBomline, (TCComponentItemRevision)solutionComponent, null), ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					setNewPartTobomEditData(getTargetBOMLine_(bwParent, (TCComponentItemRevision)solutionComponent, "Latest Working"), ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					arrCcnBomData.add(ccnEditData);
//				}
//			}
//			//      String[] newBoMrops = null;
//			//Solution
//			if(null != solutionComponent) {
//				alOpenedSolutionBOMWindow = setChildsOnHashMap(solutionComponent, "Latest Working", solutionBOM);
//			}
//			//Problem
//			if(null != problemComponent) {
//				alOpenedProblemBOMWindow = setChildsOnHashMap(problemComponent, "Latest Released", problemBOM);
//			}
//			
//			// if Change QTY (add or delete occthread)
//			Object[] change_qty_old_bom_ids = CollectionUtils.subtract(problemBOM.keySet(), solutionBOM.keySet()).toArray(new Object[0]);
//			Object[] change_qty_new_bom_ids = CollectionUtils.subtract(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//
//			HashMap<String, TCComponent[]> bomTemp = new HashMap<String, TCComponent[]>();
//
//			// 수량 변경 BOM Line인지 확인
//			for (int inx = 0; inx < change_qty_old_bom_ids.length; inx++) {
//				ccnEditData = new HashMap<String, Object>();
//				String sOLDBOMCombinationKey = change_qty_old_bom_ids[inx].toString();
//
//				// Combination Key에서 Item ID를 제거 후 OCC Thread 값만 남긴다.
//				// Combination Key : [ItemID]|[OccThreadUIDs]
//				String sOCCThreadsOLDBOM = sOLDBOMCombinationKey.substring(sOLDBOMCombinationKey.indexOf('|') + 1, sOLDBOMCombinationKey.length());
//
//				// OLD BOM의 ID를 14자리(Occ UID 자리수)로 Split
//				String[] saOldBOMIDs = StringUtil.getSplitString(sOCCThreadsOLDBOM, 14);
//
//				for (int jnx = 0; jnx < change_qty_new_bom_ids.length; jnx++) {
//					boolean isChangeQTY = false;
//					String sNEWBOMCombinationKey = change_qty_new_bom_ids[jnx].toString();
//					// Combination Key에서 Item ID를 제거 후 OCC Thread 값만 남긴다.
//					// Combination Key : [ItemID]|[OccThreadUIDs]
//					String sOCCThreadsNEWBOM = sNEWBOMCombinationKey.substring(sNEWBOMCombinationKey.indexOf('|') + 1, sNEWBOMCombinationKey.length());
//
//					// NEW BOM의 ID를 14자리(Occ UID 자리수)로 Split
//					String[] saNewBOMIDs = StringUtil.getSplitString(sOCCThreadsNEWBOM, 14);
//
//					// BOM Line 수, System Row Key가 같을 경우 수량변경 EPL 추출 하지 않음.
//					// (수량을 변경했다가 다시 원상복구했을때)
//					if (saOldBOMIDs.length == saNewBOMIDs.length) {
//						// System Row Key가 서로 같은지 확인하고 추 후 Revise 내역 추출 로직에서 재 처리
//						TCComponentBOMLine change_qty_pro_bomline = null;
//						TCComponentBOMLine change_qty_sol_bomline = null;
//
//						change_qty_pro_bomline =  (TCComponentBOMLine)problemBOM.get(sOLDBOMCombinationKey);
//						change_qty_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(sNEWBOMCombinationKey);
//
//						if (change_qty_pro_bomline != null && change_qty_sol_bomline != null) {
//							String[] change_qty_pro_properties = change_qty_pro_bomline.getProperties(bomlineProperties);
//							String[] change_qty_sol_properties = change_qty_sol_bomline.getProperties(bomlineProperties);
//
//							if (change_qty_pro_properties[16].equals(change_qty_sol_properties[16])) {
//								// Revise 내역 추출 로직에서 재처리하기 위한 임시 변수에 저장 <System Row Key, TCComponent{old, new}>
//								bomTemp.put(change_qty_pro_properties[16], new TCComponent[] {change_qty_pro_bomline, change_qty_sol_bomline});
//
//								problemBOM.remove(sOLDBOMCombinationKey);
//								solutionBOM.remove(sNEWBOMCombinationKey);
//
//								continue;
//							}
//						}
//					}
//
//					for (int knx = 0; knx < saOldBOMIDs.length; knx++) {
//						String sOldBOMID = saOldBOMIDs[knx];
//
//						for (int lnx = 0; lnx < saNewBOMIDs.length; lnx++) {
//							String sNewBOMID = saNewBOMIDs[lnx];
//
//							// 현재 비교대상 ID가 OLD BOM에도 있고 NEW BOM에도 있는데 서로 Combination 이 달라 현 로직에 들어왔다면 수량이 변경된 경우임.
//							//  * 한개라도 같을 경우 로직 발동
//							if (sOldBOMID.equals(sNewBOMID)) {
//								TCComponentBOMLine change_qty_sol_bomline = null;
//								TCComponentBOMLine change_qty_pro_bomline = null;
//
//								change_qty_pro_bomline =  (TCComponentBOMLine)problemBOM.get(sOLDBOMCombinationKey);
//								change_qty_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(sNEWBOMCombinationKey);
//
//								setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//								setOldPartTobomEditData(change_qty_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//								setNewPartTobomEditData(change_qty_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//
//								arrCcnBomData.add(ccnEditData);
//
//								problemBOM.remove(sOLDBOMCombinationKey);
//								solutionBOM.remove(sNEWBOMCombinationKey);
//
//								isChangeQTY = true;
//								break;
//							}
//						}
//
//						if (isChangeQTY) {
//							break;
//						}
//					}
//
//					if (isChangeQTY) {
//						break;
//					}
//				}
//			}
//
//			//if exist only problem, then "Delete"
//			Object[] del_bom_ids = CollectionUtils.subtract(problemBOM.keySet(), solutionBOM.keySet()).toArray(new Object[0]);
//			Object[] add_bom_ids_temp = CollectionUtils.subtract(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine del_bomline = null;
//			for(Object del_bom_id : del_bom_ids) {
//				//{"bl_item_item_id","bl_occ_fnd0objectId", "bl_occ_int_order_no", "bl_item_object_type"(3), "bl_rev_object_name", "bl_quantity", "bl_variant_condition", "bl_abs_occ_id(7)", "bl_rev_item_revision_id", "bl_rev_s7_DISPLAY_PART_NO" };
//				del_bomline =  (TCComponentBOMLine)problemBOM.get(del_bom_id);
//
//				// [SR없음][20151102][jclee] Cut and Paste 대응
//				String[] delProperties = del_bomline.getProperties(bomlineProperties);
//
//				boolean isCutAndPaste = false;
//
//				for (Object add_item_id_temp : add_bom_ids_temp) {
//					TCComponentBOMLine add_bomline_temp =  (TCComponentBOMLine)solutionBOM.get(add_item_id_temp);
//					String[] addTempProperties = add_bomline_temp.getProperties(bomlineProperties);
//
//					boolean isModified = false;
//					if (compareString(addTempProperties[0], delProperties[0])	// Item ID
//							&& compareString(addTempProperties[1], delProperties[1])	// Seq No
//							&& compareString(addTempProperties[4], delProperties[4])	// Quantity
//							&& compareString(addTempProperties[16], delProperties[16])	// System Row Key
//							) {
//						for (int inx = 0; inx < bomlineProperties.length; inx++) {
//							if (!compareString(addTempProperties[inx], delProperties[inx])) {
//								isModified = true;
//								break;
//							}
//						}
//					} else {
//						continue;
//					}
//
//					if (!isModified) {
//						solutionBOM.remove(del_bomline);
//						isCutAndPaste = true;
//						break;
//					}
//				}
//
//				if (!isCutAndPaste) {
//					ccnEditData = new HashMap<String, Object>();
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(del_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CUT);
//
//					arrCcnBomData.add(ccnEditData);
//				}
//
//				problemBOM.remove(del_bom_id);
//			}
//
//			//if exist only solution, then "Add"
//			Object[] add_bom_ids = CollectionUtils.subtract(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//			Object[] del_bom_ids_temp = CollectionUtils.subtract(problemBOM.keySet(), solutionBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine add_bomline = null;
//			for(Object add_bom_id : add_bom_ids) {
//				add_bomline =  (TCComponentBOMLine)solutionBOM.get(add_bom_id);
//				String[] addProperties = add_bomline.getProperties(bomlineProperties);
//
//				// [SR없음][20151102][jclee] Cut and Paste 대응
//				boolean isCutAndPaste = false;
//
//				for (Object del_item_id_temp : del_bom_ids_temp) {
//					TCComponentBOMLine del_bomline_temp =  (TCComponentBOMLine)solutionBOM.get(del_item_id_temp);
//					String[] delTempProperties = del_bomline_temp.getProperties(bomlineProperties);
//
//					boolean isModified = false;
//					if (compareString(delTempProperties[0], addProperties[0])	// Item ID
//							&& compareString(delTempProperties[1], addProperties[1])	// Seq No
//							&& compareString(delTempProperties[4], addProperties[4])	// Quantity
//							&& compareString(delTempProperties[16], addProperties[16])	// System Row Key
//							) {
//						for (int inx = 0; inx < bomlineProperties.length; inx++) {
//							if (!compareString(delTempProperties[inx], addProperties[inx])) {
//								isModified = true;
//								break;
//							}
//						}
//					} else {
//						continue;
//					}
//
//					if (!isModified) {
//						problemBOM.remove(add_bomline);
//						isCutAndPaste = true;
//						break;
//					}
//				}
//
//				if (!isCutAndPaste) {
//					ccnEditData = new HashMap<String, Object>();
//					setParentTobomEditData(ccnRevision,(TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setNewPartTobomEditData(add_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_ADD);
//
//					arrCcnBomData.add(ccnEditData);
//				}
//
//				solutionBOM.remove(add_bom_id);
//			}
//
//			//if exist both pro. and sol. then "Revise" or no changed.
//			Object[] revise_bom_ids = CollectionUtils.intersection(solutionBOM.keySet(), problemBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine revise_sol_bomline = null;
//			TCComponentBOMLine revise_pro_bomline = null;
//
//			for(Object revise_bom_id : revise_bom_ids) {
//
//				revise_pro_bomline =  (TCComponentBOMLine)problemBOM.get(revise_bom_id);
//				revise_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(revise_bom_id);
//				ccnEditData = new HashMap<String, Object>();
//
//				// if not equals, then "Revise"
//				if(isChangeBOMLineProp(revise_pro_bomline, revise_sol_bomline)) {
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(revise_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					setNewPartTobomEditData(revise_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//
//					arrCcnBomData.add(ccnEditData);
//				}
//
//				problemBOM.remove(revise_bom_id);
//				solutionBOM.remove(revise_bom_id);
//			}
//			
//			// 수량 변경 여부 확인 로직에서 넘어온 항목 중 Revise 이력이 있는지 확인.
//			// (수량과 System Row Key가 같으면서 OCC Thread가 완전 다른 경우)
//			revise_bom_ids = bomTemp.keySet().toArray();
//			for(Object revise_bom_id : revise_bom_ids) {
//				TCComponent[] tempComponents = bomTemp.get(revise_bom_id);
//				revise_pro_bomline =  (TCComponentBOMLine)tempComponents[0];
//				revise_sol_bomline =  (TCComponentBOMLine)tempComponents[1];
//				ccnEditData = new HashMap<String, Object>();
//
//				// if not equals, then "Revise"
//				if(isChangeBOMLineProp(revise_pro_bomline, revise_sol_bomline)) {
//
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(revise_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//					setNewPartTobomEditData(revise_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
//
//					arrCcnBomData.add(ccnEditData);
//					bomTemp.remove(revise_bom_id);
//				}
//			}
//			
//			//Replace (Change)
//
//			Object[] replace_bom_ids = (solutionBOM.keySet()).toArray(new Object[0]);
//
//			TCComponentBOMLine replace_sol_bomline = null;
//			TCComponentBOMLine replace_pro_bomline = null;
//			for(Object replace_bom_id : replace_bom_ids) {
//
//				replace_pro_bomline =  (TCComponentBOMLine)problemBOM.get(replace_bom_id);
//				replace_sol_bomline =  (TCComponentBOMLine)solutionBOM.get(replace_bom_id);
//				ccnEditData = new HashMap<String, Object>();
//
//				setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//				setOldPartTobomEditData(replace_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//				setNewPartTobomEditData(replace_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//
//				arrCcnBomData.add(ccnEditData);
//
//				problemBOM.remove(replace_bom_id);
//				solutionBOM.remove(replace_bom_id);
//			}
//			
//			// 수량 변경 여부 확인 로직에서 넘어온 항목 중 Replace 이력이 있는지 확인.
//			// (수량과 System Row Key가 같으면서 OCC Thread가 완전 다른 경우)
//			replace_bom_ids = (bomTemp.keySet()).toArray(new Object[0]);
//			for(Object replace_bom_id : replace_bom_ids) {
//
//				TCComponent[] tempComponents = bomTemp.get(replace_bom_id);
//				replace_pro_bomline =  (TCComponentBOMLine)tempComponents[0];
//				replace_sol_bomline =  (TCComponentBOMLine)tempComponents[1];
//				ccnEditData = new HashMap<String, Object>();
//
//				String sProItemID = replace_pro_bomline.getItem().getProperty("item_id");
//				String sSolItemID = replace_sol_bomline.getItem().getProperty("item_id");
//
//				// System Row Key가 같으면서 Item ID가 다른 경우 Replace로 인식
//				if (!sProItemID.equals(sSolItemID)) {
//					setParentTobomEditData(ccnRevision, (TCComponentItemRevision)solutionComponent, ccnEditData, null);
//					setOldPartTobomEditData(replace_pro_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//					setNewPartTobomEditData(replace_sol_bomline, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
//
//					arrCcnBomData.add(ccnEditData);
//					bomTemp.remove(replace_bom_id);
//				}
//			}
//			
//			if (alOpenedSolutionBOMWindow != null && alOpenedSolutionBOMWindow.size() > 0) {
//				for (int inx = 0; inx < alOpenedSolutionBOMWindow.size(); inx++) {
//					alOpenedSolutionBOMWindow.get(inx).close();
//				}
//			}
//
//			if (alOpenedProblemBOMWindow != null && alOpenedProblemBOMWindow.size() > 0) {
//				for (int inx = 0; inx < alOpenedProblemBOMWindow.size(); inx++) {
//					alOpenedProblemBOMWindow.get(inx).close();
//				}
//			}
//			
////		}catch(Exception ex) {
////			ex.printStackTrace();
////		}
//
//		return arrCcnBomData;
//
//	}
	
	//new2 5번,6번,7번, SQL Query 적용
	
	public static ArrayList<HashMap<String, Object>> compareChildItems__(String ccn_no, String ccn_owner, TCComponent solutionComponent, TCComponent problemComponent, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String ospec_no) throws Exception {
		String proId = "";
		String proRev = "";
		String solId = "";
		String solRev = "";
		try {
			HashMap<String, String[]> solutionHash = new HashMap<String, String[]>();
			HashMap<String, String[]> problemHash = new HashMap<String, String[]>();
			HashMap<String, Object> ccnEditData = null;
			ArrayList<HashMap<String, Object>> arrCcnBomData = new ArrayList<HashMap<String, Object>>();

			solId = ((TCComponentItemRevision)solutionComponent).getStringProperty("item_id");
			solRev = ((TCComponentItemRevision)solutionComponent).getStringProperty("item_revision_id");
			
			if(problemComponent != null){
				proId = ((TCComponentItemRevision)problemComponent).getStringProperty("item_id");
				proRev = ((TCComponentItemRevision)problemComponent).getStringProperty("item_revision_id");
			}
	
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("CCN_NO", ccn_no);
			ds.put("SOL_ID", solId);
			ds.put("SOL_REV", solRev);
			ds.put("PROB_ID", proId);
			ds.put("PROB_REV", proRev);
			
			//solution에 없는 부모 리스트 추출
			ArrayList<HashMap<String, Object>> arrParentEPLData = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.CCNService", "arrParentEPLData", ds);
			
			for(int i=0; i<arrParentEPLData.size(); i++){
				ccnEditData = new HashMap<String, Object>();
				Iterator<String> iterParent = arrParentEPLData.get(i).keySet().iterator();
				while (iterParent.hasNext()) {
					String sKey = (String) iterParent.next();
					Object value = arrParentEPLData.get(i).get(sKey);
					if(value != null && !value.equals("")){
						ccnEditData.put(sKey, value);
					}
				}
				Object parent_no_obj = ccnEditData.get("PARENT_NO");
				String parent_unique_no = ccnEditData.get("PARENT_UNIQUE_NO").toString();
				ds.put("PARENT_ID", parent_unique_no);
				if(parent_no_obj == null || parent_no_obj.toString().equals("")){
					ccnEditData.put("PARENT_NO", parent_unique_no);
				}
				ccnEditData.put("CCN_ID", ccn_no);
				ccnEditData.put("USER_ID", ccn_owner);
				
				//old data 추출
				if(!proId.equals("")){
					setOldPartTobomEditData_(remote, ds, (TCComponentItemRevision)problemComponent, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
				}
				
				//new data 추출
				setNewPartTobomEditData_(remote, ds, (TCComponentItemRevision)solutionComponent, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
				ccnEditData.put("NEW_OSPEC_NO", ospec_no);
				arrCcnBomData.add(ccnEditData);
			}
	
			//Solution
			if(null != solutionComponent) {
				setChildsOnHashMap_(remote, solutionComponent, "Working", solutionHash);
			}
			//Problem
			if(null != problemComponent) {
				setChildsOnHashMap_(remote, problemComponent, "Released", problemHash);
			}
			
			// if Change QTY (add or delete occthread)
			Object[] change_qty_old_bom_ids = CollectionUtils.subtract(problemHash.keySet(), solutionHash.keySet()).toArray(new Object[0]);
			Object[] change_qty_new_bom_ids = CollectionUtils.subtract(solutionHash.keySet(), problemHash.keySet()).toArray(new Object[0]);

			HashMap<String, String[][]> tempHash = new HashMap<String, String[][]>();

			// 수량 변경 BOM Line인지 확인
			for (int inx = 0; inx < change_qty_old_bom_ids.length; inx++) {
				ccnEditData = new HashMap<String, Object>();
				String sOLDBOMCombinationKey = change_qty_old_bom_ids[inx].toString();

				// Combination Key에서 Item ID를 제거 후 OCC Thread 값만 남긴다.
				// Combination Key : [ItemID]|[OccThreadUIDs]
				String sOCCThreadsOLDBOM = sOLDBOMCombinationKey.substring(sOLDBOMCombinationKey.indexOf('|') + 1, sOLDBOMCombinationKey.length());

				// OLD BOM의 ID를 14자리(Occ UID 자리수)로 Split
				String[] saOldBOMIDs = StringUtil.getSplitString(sOCCThreadsOLDBOM, 14);

				for (int jnx = 0; jnx < change_qty_new_bom_ids.length; jnx++) {
					boolean isChangeQTY = false;
					String sNEWBOMCombinationKey = change_qty_new_bom_ids[jnx].toString();
					// Combination Key에서 Item ID를 제거 후 OCC Thread 값만 남긴다.
					// Combination Key : [ItemID]|[OccThreadUIDs]
					String sOCCThreadsNEWBOM = sNEWBOMCombinationKey.substring(sNEWBOMCombinationKey.indexOf('|') + 1, sNEWBOMCombinationKey.length());

					// NEW BOM의 ID를 14자리(Occ UID 자리수)로 Split
					String[] saNewBOMIDs = StringUtil.getSplitString(sOCCThreadsNEWBOM, 14);

					// BOM Line 수, System Row Key가 같을 경우 수량변경 EPL 추출 하지 않음.
					// (수량을 변경했다가 다시 원상복구했을때)
					if (saOldBOMIDs.length == saNewBOMIDs.length) {
						// System Row Key가 서로 같은지 확인하고 추 후 Revise 내역 추출 로직에서 재 처리
						
						String[] change_qty_pro_properties =  problemHash.get(sOLDBOMCombinationKey);
						String[] change_qty_sol_properties =  solutionHash.get(sNEWBOMCombinationKey);
						
						if (change_qty_pro_properties != null && change_qty_sol_properties != null) {


							if (change_qty_pro_properties[16].equals(change_qty_sol_properties[16])) {
								// Revise 내역 추출 로직에서 재처리하기 위한 임시 변수에 저장 <System Row Key, TCComponent{old, new}>
								tempHash.put(change_qty_pro_properties[16], new String[][] {change_qty_pro_properties, change_qty_sol_properties});

								problemHash.remove(sOLDBOMCombinationKey);
								solutionHash.remove(sNEWBOMCombinationKey);

								continue;
							}
						}
					}

					for (int knx = 0; knx < saOldBOMIDs.length; knx++) {
						String sOldBOMID = saOldBOMIDs[knx];

						for (int lnx = 0; lnx < saNewBOMIDs.length; lnx++) {
							String sNewBOMID = saNewBOMIDs[lnx];

							// 현재 비교대상 ID가 OLD BOM에도 있고 NEW BOM에도 있는데 서로 Combination 이 달라 현 로직에 들어왔다면 수량이 변경된 경우임.
							//  * 한개라도 같을 경우 로직 발동
							if (sOldBOMID.equals(sNewBOMID)) {
								TCComponentBOMLine change_qty_sol_bomline = null;
								TCComponentBOMLine change_qty_pro_bomline = null;
//
								String[] change_qty_pro_properties =  problemHash.get(sOLDBOMCombinationKey);
								String[] change_qty_sol_properties =  solutionHash.get(sNEWBOMCombinationKey);

								setParentTobomEditData__(ccn_no, ccn_owner, change_qty_sol_properties, ccnEditData, null);
								setOldPartTobomEditData__(change_qty_pro_properties, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
								setNewPartTobomEditData__(change_qty_sol_properties, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);

								arrCcnBomData.add(ccnEditData);

								problemHash.remove(sOLDBOMCombinationKey);
								solutionHash.remove(sNEWBOMCombinationKey);

								isChangeQTY = true;
								break;
							}
						}

						if (isChangeQTY) {
							break;
						}
					}

					if (isChangeQTY) {
						break;
					}
				}
			}

			//if exist only problem, then "Delete"
			Object[] del_bom_ids = CollectionUtils.subtract(problemHash.keySet(), solutionHash.keySet()).toArray(new Object[0]);
			Object[] add_bom_ids_temp = CollectionUtils.subtract(solutionHash.keySet(), problemHash.keySet()).toArray(new Object[0]);

//			TCComponentBOMLine del_bomline = null;
			String[] delProperties = null;
			for(Object del_bom_id : del_bom_ids) {
				delProperties =  problemHash.get(del_bom_id);

				// [SR없음][20151102][jclee] Cut and Paste 대응
				boolean isCutAndPaste = false;

				for (Object add_item_id_temp : add_bom_ids_temp) {
					String[] addTempProperties = solutionHash.get(add_item_id_temp);

					boolean isModified = false;
					if (compareString(addTempProperties[0], delProperties[0])	// Item ID
							&& compareString(addTempProperties[1], delProperties[1])	// Seq No
							&& compareString(addTempProperties[4], delProperties[4])	// Quantity
							&& compareString(addTempProperties[16], delProperties[16])	// System Row Key
							) {
						for (int inx = 0; inx < bomlineProperties.length; inx++) {
							if (!compareString(addTempProperties[inx], delProperties[inx])) {
								isModified = true;
								break;
							}
						}
					} else {
						continue;
					}

					if (!isModified) {
						solutionHash.remove(del_bom_id);
						isCutAndPaste = true;
						break;
					}
				}

				if (!isCutAndPaste) {
					ccnEditData = new HashMap<String, Object>();
					setParentTobomEditData__(ccn_no, ccn_owner, delProperties, ccnEditData, null);
					setOldPartTobomEditData__(delProperties, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CUT);

					arrCcnBomData.add(ccnEditData);
				}

				problemHash.remove(del_bom_id);
			}

			//if exist only solution, then "Add"
			Object[] add_bom_ids = CollectionUtils.subtract(solutionHash.keySet(), problemHash.keySet()).toArray(new Object[0]);
			Object[] del_bom_ids_temp = CollectionUtils.subtract(problemHash.keySet(), solutionHash.keySet()).toArray(new Object[0]);

//			TCComponentBOMLine add_bomline = null;
			String[] addProperties = null;
			for(Object add_bom_id : add_bom_ids) {
				addProperties =  solutionHash.get(add_bom_id);

				// [SR없음][20151102][jclee] Cut and Paste 대응
				boolean isCutAndPaste = false;

				for (Object del_item_id_temp : del_bom_ids_temp) {
					String[] delTempProperties = solutionHash.get(del_item_id_temp);

					boolean isModified = false;
					if (compareString(delTempProperties[0], addProperties[0])	// Item ID
							&& compareString(delTempProperties[1], addProperties[1])	// Seq No
							&& compareString(delTempProperties[4], addProperties[4])	// Quantity
							&& compareString(delTempProperties[16], addProperties[16])	// System Row Key
							) {
						for (int inx = 0; inx < bomlineProperties.length; inx++) {
							if (!compareString(delTempProperties[inx], addProperties[inx])) {
								isModified = true;
								break;
							}
						}
					} else {
						continue;
					}

					if (!isModified) {
						problemHash.remove(add_bom_id);
						isCutAndPaste = true;
						break;
					}
				}

				if (!isCutAndPaste) {
					ccnEditData = new HashMap<String, Object>();
					setParentTobomEditData__(ccn_no, ccn_owner,addProperties, ccnEditData, null);
					setNewPartTobomEditData__(addProperties, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_ADD);

					arrCcnBomData.add(ccnEditData);
				}

				solutionHash.remove(add_bom_id);
			}

			//if exist both pro. and sol. then "Revise" or no changed.
			Object[] revise_bom_ids = CollectionUtils.intersection(solutionHash.keySet(), problemHash.keySet()).toArray(new Object[0]);

			String[] revise_sol_bom_prop = null;
			String[] revise_pro_bom_prop = null;

			for(Object revise_bom_id : revise_bom_ids) {

				revise_pro_bom_prop =  problemHash.get(revise_bom_id);
				revise_sol_bom_prop =  solutionHash.get(revise_bom_id);
				ccnEditData = new HashMap<String, Object>();

				if(isChangeBOMLineProp__(revise_pro_bom_prop, revise_sol_bom_prop)) {
					setParentTobomEditData__(ccn_no, ccn_owner, revise_sol_bom_prop, ccnEditData, null);
					setOldPartTobomEditData__(revise_pro_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
					setNewPartTobomEditData__(revise_sol_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);

					arrCcnBomData.add(ccnEditData);
				}

				problemHash.remove(revise_bom_id);
				solutionHash.remove(revise_bom_id);
			}
			
			// 수량 변경 여부 확인 로직에서 넘어온 항목 중 Revise 이력이 있는지 확인.
			// (수량과 System Row Key가 같으면서 OCC Thread가 완전 다른 경우)
			revise_bom_ids = tempHash.keySet().toArray();

			for(Object revise_bom_id : revise_bom_ids) {
				String[][] tempProps = tempHash.get(revise_bom_id);
				if(tempProps != null && tempProps.length > 0){
					revise_pro_bom_prop =  tempProps[0];
					revise_sol_bom_prop =  tempProps[1];
					
					ccnEditData = new HashMap<String, Object>();
	
					if(isChangeBOMLineProp__(revise_pro_bom_prop, revise_sol_bom_prop)) {
						setParentTobomEditData__(ccn_no, ccn_owner, revise_sol_bom_prop, ccnEditData, null);
						setOldPartTobomEditData__(revise_pro_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
						setNewPartTobomEditData__(revise_sol_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE);
						arrCcnBomData.add(ccnEditData);
						tempHash.remove(revise_bom_id);
					}
				}
			}
			
			//Replace (Change)

			Object[] replace_bom_ids = (solutionHash.keySet()).toArray(new Object[0]);

			String[] replace_sol_bom_prop = null;
			String[] replace_pro_bom_prop = null;
			
			for(Object replace_bom_id : replace_bom_ids) {

				replace_pro_bom_prop =  problemHash.get(replace_bom_id);
				replace_sol_bom_prop =  solutionHash.get(replace_bom_id);
				ccnEditData = new HashMap<String, Object>();

				setParentTobomEditData__(ccn_no, ccn_owner, replace_sol_bom_prop, ccnEditData, null);
				setOldPartTobomEditData__(replace_pro_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
				setNewPartTobomEditData__(replace_sol_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);

				arrCcnBomData.add(ccnEditData);

				problemHash.remove(replace_bom_id);
				solutionHash.remove(replace_bom_id);
			}
			
			// 수량 변경 여부 확인 로직에서 넘어온 항목 중 Replace 이력이 있는지 확인.
			// (수량과 System Row Key가 같으면서 OCC Thread가 완전 다른 경우)
			replace_bom_ids = (tempHash.keySet()).toArray(new Object[0]);
			for(Object replace_bom_id : replace_bom_ids) {
				String[][] tempProps = tempHash.get(replace_bom_id);
				if(tempProps != null && tempProps.length > 0){
					replace_pro_bom_prop =  tempProps[0];
					replace_sol_bom_prop =  tempProps[1];
					ccnEditData = new HashMap<String, Object>();
					
					String sProItemID = replace_pro_bom_prop[0];
					String sSolItemID = replace_sol_bom_prop[0];

					// System Row Key가 같으면서 Item ID가 다른 경우 Replace로 인식
					if (!sProItemID.equals(sSolItemID)) {
						setParentTobomEditData__(ccn_no, ccn_owner, replace_sol_bom_prop, ccnEditData, null);
						setOldPartTobomEditData__(replace_pro_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);
						setNewPartTobomEditData__(replace_sol_bom_prop, ccnEditData, ospec, optionSetMap, PropertyConstant.CONST_CCN_CHG_TYPE_REPLACE);

						arrCcnBomData.add(ccnEditData);
						tempHash.remove(replace_bom_id);
					}
				}

			}
				
			return arrCcnBomData;
		} catch (Exception e){
			throw new Exception("compareChildItems Error, solId :" + solId + ", proId : " + proId + "\n[Exception] : " + e.getMessage());
		}

	}
	
	private static boolean isChangeBOMLineProp__(String[] revise_pro_bom_prop, String[] revise_sol_bom_prop) {
		try {
//			String[] property_array = {PropertyConstant.ATTR_NAME_BL_QUANTITY,PropertyConstant.ATTR_NAME_BL_VARIANT_CONDITION,PropertyConstant.ATTR_NAME_BL_ITEM_REVISION_ID,
//					PropertyConstant.ATTR_NAME_BL_REV_ITEM_NAME,PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE,PropertyConstant.ATTR_NAME_BL_CHG_CD,
//					PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO,PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY,PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT,
//					PropertyConstant.ATTR_NAME_BL_DVP_USE,PropertyConstant.ATTR_NAME_BL_SPEC_DESC,PropertyConstant.ATTR_NAME_BL_MODULE_CODE,
//					PropertyConstant.ATTR_NAME_BL_ALTER_PART,PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING};
//			String[] problem_value_array = revise_pro_bomline.getProperties(property_array);
//			String[] solution_value_array = revise_sol_bomline.getProperties(property_array);

			return !compareString(revise_pro_bom_prop[4],revise_sol_bom_prop[4])
					||
					!compareString(revise_pro_bom_prop[5],revise_sol_bom_prop[5])
					||
					!compareString(revise_pro_bom_prop[7],revise_sol_bom_prop[7])
					||
					!compareString(revise_pro_bom_prop[25],revise_sol_bom_prop[25])
					||
					!compareString(revise_pro_bom_prop[10],revise_sol_bom_prop[10])
					||
					!compareString(revise_pro_bom_prop[13],revise_sol_bom_prop[13])
					||
					!compareString(revise_pro_bom_prop[1],revise_sol_bom_prop[1])
					||
					!compareString(revise_pro_bom_prop[17],revise_sol_bom_prop[17])
					||
					!compareString(revise_pro_bom_prop[19],revise_sol_bom_prop[19])
					||
					!compareString(revise_pro_bom_prop[18],revise_sol_bom_prop[18])
					||
					!compareString(revise_pro_bom_prop[12],revise_sol_bom_prop[12])
					||
					!compareString(revise_pro_bom_prop[9],revise_sol_bom_prop[9])
					||
					!compareString(revise_pro_bom_prop[14],revise_sol_bom_prop[14])
					||
					!compareString(revise_pro_bom_prop[22],revise_sol_bom_prop[22])
					/*
					 * [CF-4358][20230901]
					수정 이유 : 조직 변경으로 Master List Manager(MLM)에서 TEAM속성만 변경 하였는데 TEAM속성은 비교 로직에 포함 안되어 있어서 I-PASS로 인터페이스 되지 않아 조회가 안되는 문제 발생
					SYSTEM, LEV(MAN), Weight 관리(STD), TEAM, CHARGER, EJS 6가지 속성이 위와 같은 현상인데 기술관리팀에서 SYSTEM[23], TEAM[20], CHARGER[38] 3가지 속성만 추가 요청함  
					*/   
					||
					!compareString(revise_pro_bom_prop[20],revise_sol_bom_prop[20])
					||
					!compareString(revise_pro_bom_prop[23],revise_sol_bom_prop[23])
					||
					!compareString(revise_pro_bom_prop[38],revise_sol_bom_prop[38])
					;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 사용하지 않아 주석 처리 isChangeBOMLineProp__을 사용함 
	*/
//	private static boolean isChangeBOMLineProp(TCComponentBOMLine revise_pro_bomline, TCComponentBOMLine revise_sol_bomline) {
//		try {
//			String[] property_array = {PropertyConstant.ATTR_NAME_BL_QUANTITY,PropertyConstant.ATTR_NAME_BL_VARIANT_CONDITION,PropertyConstant.ATTR_NAME_BL_ITEM_REVISION_ID,
//					PropertyConstant.ATTR_NAME_BL_REV_ITEM_NAME,PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE,PropertyConstant.ATTR_NAME_BL_CHG_CD,
//					PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO,PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY,PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT,
//					PropertyConstant.ATTR_NAME_BL_DVP_USE,PropertyConstant.ATTR_NAME_BL_SPEC_DESC,PropertyConstant.ATTR_NAME_BL_MODULE_CODE,
//					PropertyConstant.ATTR_NAME_BL_ALTER_PART,PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING};
//			String[] problem_value_array = revise_pro_bomline.getProperties(property_array);
//			String[] solution_value_array = revise_sol_bomline.getProperties(property_array);
//
//			return !compareString(problem_value_array[0],solution_value_array[0])
//					||
//					!compareString(problem_value_array[1], solution_value_array[1])
//					||
//					!compareString(problem_value_array[2],solution_value_array[2])
//					||
//					!compareString(problem_value_array[3],solution_value_array[3])
//					||
//					!compareString(problem_value_array[4],solution_value_array[4])
//					||
//					!compareString(problem_value_array[5],solution_value_array[5])
//					||
//					!compareString(problem_value_array[6],solution_value_array[6])
//					||
//					!compareString(problem_value_array[7],solution_value_array[7])
//					||
//					!compareString(problem_value_array[8],solution_value_array[8])
//					||
//					!compareString(problem_value_array[9],solution_value_array[9])
//					||
//					!compareString(problem_value_array[10],solution_value_array[10])
//					||
//					!compareString(problem_value_array[11],solution_value_array[11])
//					||
//					!compareString(problem_value_array[12],solution_value_array[12])
//					||
//					!compareString(problem_value_array[13],solution_value_array[13])
//					;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}

	public static TCComponentBOMLine getBomline(TCComponentItemRevision targetRevision, TCSession session)
			throws TCException {
		TCComponentBOMLine topLine = null;
		if (targetRevision != null) {
			TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session
					.getTypeComponent("BOMWindow");
			TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session
					.getTypeComponent("RevisionRule");
			TCComponentBOMWindow bomWindow = windowType.create(ruleType.getDefaultRule());
			topLine = (TCComponentBOMLine) bomWindow.setWindowTopLine(null, targetRevision, null, null);
		}
		return topLine;
	}

	@SuppressWarnings("unchecked")
	//old
	
	public static ArrayList<TCComponent> addHistoryOnlyRevise(TCComponentItemRevision tcItemRevision,TCComponent[] solutionList) throws TCException {

		ArrayList<TCComponent> parentTccomponentlist = new ArrayList<TCComponent>();
		HashMap<String, TCComponent> parentHash = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> solHash = new HashMap<String, TCComponent>();
		TCComponentRevisionRule[] allRevisionRules = TCComponentRevisionRule.listAllRules(getTCSession());
		TCComponentRevisionRule latestReleasedRule = null;
		for(TCComponentRevisionRule revisionRule : allRevisionRules) {
			String ruleName = revisionRule.getProperty("object_name");
			if(ruleName.equals("Latest Working")) {
				latestReleasedRule = revisionRule;
				break;
			}
		}
		//[20170622][ljg] whereUsed API 방식에서 -> SQL 방식으로 변경
		//		ArrayList<TCComponent> arrTccomponent =  whereUsed(tcItemRevision);

		//[20171114][ljg] 오류 발견 되어 SQL 방식에서 -> whereUsed API 방식으로 원복
		ArrayList<TCComponent> arrTccomponent =  getWhereUsed(tcItemRevision, new String[]{}, latestReleasedRule);
		
		for(TCComponent parentComponent : arrTccomponent) {

			parentHash.put(parentComponent.getProperty(PropertyConstant.ATTR_NAME_ITEMID), parentComponent);
		}

		for(TCComponent solComponent : solutionList) {

			solHash.put(solComponent.getProperty(PropertyConstant.ATTR_NAME_ITEMID), solComponent);
		}


		Object[] parent_bom_ids = CollectionUtils.subtract(parentHash.keySet(), solHash.keySet()).toArray(new Object[0]);
		for(Object  parentObject : parent_bom_ids) {

			parentTccomponentlist.add(parentHash.get(parentObject));
		}
		return parentTccomponentlist;

	}
	
	//new 5번
	
	public static ArrayList<TCComponent> addHistoryOnlyRevise_(TCComponentItemRevision tcItemRevision, HashMap<String, TCComponent> solHash) throws TCException {
		
		ArrayList<TCComponent> parentTccomponentlist = new ArrayList<TCComponent>();
		HashMap<String, TCComponent> parentHash = new HashMap<String, TCComponent>();
		
		ArrayList<TCComponent> arrTccomponent =  whereUsed(tcItemRevision);
		
		for(TCComponent parentComponent : arrTccomponent) {

			parentHash.put(parentComponent.getProperty(PropertyConstant.ATTR_NAME_ITEMID), parentComponent);
		}

		Object[] parent_bom_ids = CollectionUtils.subtract(parentHash.keySet(), solHash.keySet()).toArray(new Object[0]);
		for(Object  parentObject : parent_bom_ids) {

			parentTccomponentlist.add(parentHash.get(parentObject));
		}
		return parentTccomponentlist;

	}
	
	
	public static ArrayList<TCComponent> getWhereUsed(TCComponent imanRev, String type, TCComponentRevisionRule revRule) throws TCException {
		TCComponent imanCompRev = null;

		ArrayList<TCComponent> compsList = new ArrayList<TCComponent>();

		TCComponent[] imanComps = imanRev.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
		for (int i = 0; i < imanComps.length; i++) {
			if (imanComps[i].getType().equalsIgnoreCase(type)) {
				imanCompRev = imanComps[i];
				compsList.add(imanCompRev);
			}
		}
		return compsList;
	}

	public static ArrayList<TCComponent> getWhereUsed(TCComponent imanRev, String[] types, TCComponentRevisionRule revRule) throws TCException {
		TCComponent imanCompRev = null;

		ArrayList<TCComponent> compsList = new ArrayList<TCComponent>();

		TCComponent[] imanComps = imanRev.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
		for (int i = 0; i < imanComps.length; i++) {

			if (imanComps[i] instanceof TCComponentItemRevision) {
				imanCompRev = imanComps[i];
				compsList.add(imanCompRev);

			}

		}

		return compsList;
	}

	/**
	 * whereUsed DB 쿼리 방식으로 변경
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 22.
	 * @param rev
	 * @return
	 */
	private static ArrayList<TCComponent> whereUsed(TCComponent rev){
		ArrayList<TCComponent> compsList = null;
		try {
			compsList = new ArrayList<TCComponent>();

			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();

			TCComponentItem item = ((TCComponentItemRevision)rev).getItem();
			String item_id = item.getStringProperty("item_id");
			String revision_id = ((TCComponentItemRevision)rev).getStringProperty("item_revision_id");

			ds.put("ITEM_ID", item_id);
			ds.put("REVISION_ID", revision_id);
			ArrayList<String> whereUsedList = (ArrayList<String>)remote.execute("com.ssangyong.service.CCNService", "whereUsed", ds);

			for(int i=0; i<whereUsedList.size(); i++){
				compsList.add(rev.getSession().getComponentManager().getTCComponent(whereUsedList.get(i)));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return compsList;
	}
	
	
	private static TCComponentBOMLine getTargetBOMLine_(TCComponentBOMWindow bwParent, TCComponentItemRevision targetRevision, String revisionRule) throws Exception {

//		TCComponentBOMWindow bwParent = getBOMWindow(parentRevision, revisionRule, "bom_view");/
		TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(bwParent.getSession(), revisionRule);
		bwParent.setRevisionRule(revRule);
		TCComponentBOMLine blParent = (TCComponentBOMLine)bwParent.getTopBOMLine();
		
		ArrayList<String> childIdList = new ArrayList();
		childIdList.add(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID));
		LinkedList<TCComponentBOMLine> childLines = getChildrens(childIdList, blParent);
		if(childLines != null && childLines.size() > 0){
			return childLines.get(0);
		} else {
			return null;
		}
	}
	
	private static LinkedList<TCComponentBOMLine> getChildrens(ArrayList<String> findIdList, TCComponentBOMLine topBOMLine){
		LinkedList<TCComponentBOMLine> findedBOMLineList = new LinkedList<TCComponentBOMLine>();
		StructureFilterWithExpand.ExpandAndSearchResponse expandSearchResponse = null;
		StructureFilterWithExpandService strExpandService = StructureFilterWithExpandService.getService(topBOMLine.getSession());
		TCComponentBOMLine[] expandedBOMLines = { topBOMLine };
		
		StructureFilterWithExpand.SearchCondition[] conditions = new StructureFilterWithExpand.SearchCondition[findIdList.size()];
		for (int i = 0; i < conditions.length; i++)
		{
			StructureFilterWithExpand.SearchCondition localSearchCondition = new StructureFilterWithExpand.SearchCondition();
			localSearchCondition.logicalOperator = "OR";
			localSearchCondition.propertyName = "bl_item_item_id";
//			localSearchCondition.propertyName = "bl_clone_stable_occurrence_id";
			localSearchCondition.relationalOperator = "=";
			localSearchCondition.inputValue = findIdList.get(i);
			conditions[i] = localSearchCondition;
		}

		expandSearchResponse = strExpandService.expandAndSearch(expandedBOMLines, conditions);
		
		for (ExpandAndSearchOutput output : expandSearchResponse.outputLines)
		{
			TCComponentBOMLine findedBOMLine = output.resultLine;
			if (findedBOMLineList.contains(findedBOMLine))
				continue;
			findedBOMLineList.add(findedBOMLine);
		}
		
		
		return findedBOMLineList;
	}
	
	public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {        
		TCComponentBOMWindow bomWindow = null;
		TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
		TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
		// 리비전 룰을 가져옴
		TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(session, ruleName);
		// BOMWindow를 생성
		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		bomWindow = windowType.create(revRule);
		bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);

		return bomWindow;
	}
	
	public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
		comp.refresh();

		//Component 타입이 TCComponentBOMLine인 경우에는 getRelatedComponents를 가져오기 위해서 TCComponentItemRevision 으로 변경한다.
		if(comp.getType().equals("BOMLine")) {
			comp = ((TCComponentBOMLine) comp).getItemRevision();
		}

		TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");
		for (TCComponent bvr : arrayStructureRevision) {
			TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) bvr;
			if (bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type").equals(viewType)) {
				return bomViewRevision;
			}
		}

		return null;
	}

	public static TCComponentBOMLine getTargetBOMLine(TCComponentBOMLine tcComponent, TCComponentItemRevision targetRevision, String revisionRule) throws Exception {

		ArrayList<TCComponentBOMLine> arrlist = getChildren(tcComponent.getItemRevision(), revisionRule);

		for(TCComponentBOMLine tccomponentBomline : arrlist){
			if((targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID)).equals(tccomponentBomline.getProperty(PropertyConstant.ATTR_NAME_BL_ITEM_ID))) {
				return tccomponentBomline;
			}
		}
		return null;
	}


	public static ArrayList<TCComponentBOMLine> getChildren(TCComponentItemRevision tcComponent, String revisionRule) throws Exception {
		ArrayList<TCComponentBOMLine> arrlist = new ArrayList<TCComponentBOMLine>();
		TCComponentBOMWindow bomWindow = null;
		try{

			if(!(BundleUtil.nullToString(revisionRule)).equals("")) {

				bomWindow = getTopBOMWindow(tcComponent, revisionRule);

			}else{

				bomWindow = getTopBOMWindow(tcComponent);

			}


			TCComponentBOMLine topLine = (TCComponentBOMLine) bomWindow.getTopBOMLine();

			AIFComponentContext[] aifcomponentContexts = topLine.getChildren();

			TCComponentBOMLine childBomLine = null;


			for(AIFComponentContext aifcomponentContext : aifcomponentContexts) {
				childBomLine = (TCComponentBOMLine)aifcomponentContext.getComponent();
				arrlist.add(childBomLine);
			}

		}catch(TCException e) {
			MessageBox.post( e.getMessage(), "Error", MessageBox.ERROR);
		}
		return arrlist;
	}


	public static TCComponentBOMLine getTopBOMLine(TCComponentItemRevision revision) throws TCException {
		TCSession session = revision.getSession();
		TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		TCComponentRevisionRuleType type2 = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
		TCComponentRevisionRule rule = type2.getDefaultRule();
		TCComponentBOMWindow bomWindow = type.create(rule);
		bomWindow.setWindowTopLine(null, revision, null, null);
		TCComponentBOMLine topLine = bomWindow.getTopBOMLine();
		return topLine;
	}

	public static TCComponentBOMWindow getTopBOMWindow(TCComponentItemRevision revision) throws TCException {
		TCSession session = revision.getSession();
		TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		TCComponentRevisionRuleType type2 = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
		TCComponentRevisionRule rule = type2.getDefaultRule();
		TCComponentBOMWindow bomWindow = (TCComponentBOMWindow) type.create(rule);
		bomWindow.setWindowTopLine(null, revision, null, null);
		return bomWindow;
	}

	public static TCComponentBOMWindow getTopBOMWindow(TCComponentItemRevision revision, String ruleName) throws Exception {
		TCSession session = revision.getSession();
		TCComponentRevisionRule revRule;
		revRule = getRevisionRule(session, ruleName);
		TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		// TCComponentRevisionRuleType type2 = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
		// TCComponentRevisionRule rule = type2.getDefaultRule();
		TCComponentBOMWindow bomWindow = (TCComponentBOMWindow) type.create(revRule);
		bomWindow.setWindowTopLine(null, revision, null, null);
		return bomWindow;
	}

	public static TCComponentRevisionRule getRevisionRule(TCSession session, String ruleName) throws Exception {
		if (ruleName == null || ruleName.length() == 0)
			return null;

		TCComponentRevisionRule revRule = null;
		TCComponentRevisionRule[] revRules = TCComponentRevisionRule.listAllRules(session);

		if (revRules == null)
			return null;

		for (int i = 0; i < revRules.length; i++) {
			String revRuleName = revRules[i].getProperty("object_name");
			if (ruleName.trim().equalsIgnoreCase(revRuleName)) {
				revRule = revRules[i];
				break;
			}
		}

		return revRule;
	}
	
	public static void setChildsOnHashMap_(SYMCRemoteUtil remote, TCComponent tcComponent, String revision_rule, HashMap<String, String[]> bomHash) throws Exception {
		String id = ((TCComponentItemRevision)tcComponent).getStringProperty("item_id");
		String rev = ((TCComponentItemRevision)tcComponent).getStringProperty("item_revision_id");

		DataSet ds = new DataSet();
		ds.put("ID", id);
		ds.put("REV", rev);
		ds.put("RULE", revision_rule);
		ArrayList<HashMap<String, Object>> getChildBOMPro = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.CCNService", "getChildBOMPro", ds);
		for(int i=0; i<getChildBOMPro.size(); i++){
			String cId = getChildBOMPro.get(i).get("ITEM_ID").toString();
			String occId = getChildBOMPro.get(i).get("OCC_ID").toString();
			String seq = getChildBOMPro.get(i).get("SEQ_NO").toString();
			String itype = getChildBOMPro.get(i).get("OBJECT_TYPE").toString();
			String rtype = getChildBOMPro.get(i).get("REV_OBJECT_TYPE").toString();
			String qty = getChildBOMPro.get(i).get("QTY").toString();
			Object vc_o = getChildBOMPro.get(i).get("VC");
			String vc = vc_o == null ? "" : vc_o.toString();
			Object occ_fnd_id_o = getChildBOMPro.get(i).get("OCC_FND_ID");
			String occ_fnd_id = occ_fnd_id_o == null ? "" : occ_fnd_id_o.toString();
			String rev_id = getChildBOMPro.get(i).get("REV_ID").toString();
			Object abs_occ_id_o = getChildBOMPro.get(i).get("ABS_OCC_ID");
			String abs_occ_id = abs_occ_id_o == null ? "" : abs_occ_id_o.toString();
			Object module_o = getChildBOMPro.get(i).get("MODULE");
			String module = module_o == null ? "" : module_o.toString();
			Object smode_o = getChildBOMPro.get(i).get("SMODE");
			String smode = smode_o == null ? "" : smode_o.toString();
			Object req_opt_o = getChildBOMPro.get(i).get("REQ_OPT");
			String req_opt = req_opt_o == null ? "" : req_opt_o.toString();
			Object spec_desc_o = getChildBOMPro.get(i).get("SPEC_DESC");
			String spec_desc = spec_desc_o == null ? "" : spec_desc_o.toString();
			Object chg_cd_o = getChildBOMPro.get(i).get("CHG_CD");
			String chg_cd = chg_cd_o == null ? "" : chg_cd_o.toString();
			Object alter_part_o = getChildBOMPro.get(i).get("ALTER_PART");
			String alter_part = alter_part_o == null ? "" : alter_part_o.toString();
			Object lev_m_o = getChildBOMPro.get(i).get("LEV_M");
			String lev_m = lev_m_o == null ? "" : lev_m_o.toString();
			Object system_row_key_o = getChildBOMPro.get(i).get("SYSTEM_ROW_KEY");
			String system_row_key = system_row_key_o == null ? "" : system_row_key_o.toString();
			Object dvp_needed_qty_o = getChildBOMPro.get(i).get("DVP_NEEDED_QTY");
			String dvp_needed_qty = dvp_needed_qty_o == null ? "" : dvp_needed_qty_o.toString();
			Object dvp_use_o = getChildBOMPro.get(i).get("DVP_USE");
			String dvp_use = dvp_use_o == null ? "" : dvp_use_o.toString();
			Object dvp_req_dept_o = getChildBOMPro.get(i).get("DVP_REQ_DEPT");
			String dvp_req_dept = dvp_req_dept_o == null ? "" : dvp_req_dept_o.toString();
			Object eng_dept_nm_o = getChildBOMPro.get(i).get("ENG_DEPT_NM");
			String eng_dept_nm = eng_dept_nm_o == null ? "" : eng_dept_nm_o.toString();
			Object eng_responsiblity_o = getChildBOMPro.get(i).get("ENG_RESPONSIBLITY");
			String eng_responsiblity = eng_responsiblity_o == null ? "" : eng_responsiblity_o.toString();
			Object proto_tooling_o = getChildBOMPro.get(i).get("PROTO_TOOLING");
			String proto_tooling = proto_tooling_o == null ? "" : proto_tooling_o.toString();
			Object system_code_o = getChildBOMPro.get(i).get("SYSTEM_CODE");
			String system_code = system_code_o == null ? "" : system_code_o.toString();
			Object system_name_o = getChildBOMPro.get(i).get("SYSTEM_NAME");
			String system_name = system_name_o == null ? "" : system_name_o.toString();
			String cpuid = getChildBOMPro.get(i).get("CPUID").toString();
			String cname = getChildBOMPro.get(i).get("CNAME").toString();
			Object cdisplay_no_o = getChildBOMPro.get(i).get("CDISPLAY_NO");
			String cdisplay_no = cdisplay_no_o == null ? "" : cdisplay_no_o.toString();
			Object func_o = getChildBOMPro.get(i).get("FUNC");
			String func = func_o == null ? "" : func_o.toString();
			Object project_code_o = getChildBOMPro.get(i).get("PROJECT_CODE");
			String project_code = project_code_o == null ? "" : project_code_o.toString();
			String pid = getChildBOMPro.get(i).get("PID").toString();
			Object p_display_no_o = getChildBOMPro.get(i).get("P_DISPLAY_NO");
			String p_display_no = p_display_no_o == null ? "" : p_display_no_o.toString();
			String p_rev_type = getChildBOMPro.get(i).get("P_REV_TYPE").toString();
			String p_name = getChildBOMPro.get(i).get("P_NAME").toString();
			String p_rev = getChildBOMPro.get(i).get("P_REV").toString();
			String p_uid = getChildBOMPro.get(i).get("P_UID").toString();
			Object p_mod_date_o = getChildBOMPro.get(i).get("P_MOD_DATE");
			String p_mod_date = p_mod_date_o == null ? "" : p_mod_date_o.toString();
			Object dcs_info_o = getChildBOMPro.get(i).get("DCS_INFO");
			String dcs_info = dcs_info_o == null ? "" : dcs_info_o.toString();
			/*
			 * [CF-4358][20230901]
			수정 이유 : 조직 변경으로 MLM에서 TEAM속성만 변경 하였는데 TEAM속성은 비교 로직에 포함 안되어 있어서 I-PASS로 인터페이스 되지 않아 조회가 안되는 문제 발생
			SYSTEM, LEV(MAN), Weight 관리(STD), TEAM, CHARGER, EJS 6가지 속성이 위와 같은 현상인데 기술관리팀에서 SYSTEM[23], TEAM[20], CHARGER[38] 3가지 속성만 추가 요청함  
			*/   
			Object eng_respon_o = getChildBOMPro.get(i).get("ENG_RESPON");
			String eng_respon = eng_respon_o == null ? "" : eng_respon_o.toString();
			
			String[] props = new String[]{cId				//0
					                    , seq				//1
					                    , itype				//2
					                    , rtype				//3
					                    , qty				//4
					                    , vc				//5
					                    , occ_fnd_id		//6
					                    , rev_id			//7
					                    , abs_occ_id		//8
					                    , module			//9
					                    , smode				//10
					                    , req_opt			//11
					                    , spec_desc			//12
					                    , chg_cd			//13
					                    , alter_part		//14
					                    , lev_m				//15
					                    , system_row_key	//16
					                    , dvp_needed_qty	//17
					                    , dvp_use			//18
					                    , dvp_req_dept		//19
					                    , eng_dept_nm		//20
					                    , eng_responsiblity	//21
					                    , proto_tooling		//22
					                    , system_code		//23
					                    , cpuid				//24
					                    , cname				//25
					                    , cdisplay_no		//26
					                    , func				//27
					                    , project_code		//28
					                    , system_name		//29
					                    , pid				//30
					                    , p_display_no		//31
					                    , p_rev_type		//32
					                    , p_name			//33
					                    , p_rev				//34
					                    , p_uid				//35
					                    , p_mod_date		//36
					                    , dcs_info			//37
					                    , eng_respon        //38
					                    };
			
			bomHash.put(cId + "|" + occId, props);
			
		}
	}

	public static ArrayList<TCComponentBOMWindow> setChildsOnHashMap(TCComponent tcComponent, String revision_rule, HashMap<String, TCComponent> bomHash) throws Exception {
		ArrayList<TCComponentBOMWindow> alOpenedBOMWindow = new ArrayList<TCComponentBOMWindow>();
		TCComponentBOMWindow bomWindow = null;
		TCComponentBOMLine childBomLine = null;
		TCComponentBOMLine topLine = null;
		try{

			bomWindow = getTopBOMWindow((TCComponentItemRevision)tcComponent, revision_rule);

			topLine = (TCComponentBOMLine) bomWindow.getTopBOMLine();
			
			AIFComponentContext[] aifcomponentContexts = topLine.getChildren();
			
			// [SR없음][20151102][jclee] Child BOM Line Pack(New System Row Key 중복 추출 대응)
			//[CSH][20180503]unPack 설정시 앞단에서 차단하여 여기까지 넘어오지 않도록 조치 (epl reload / approval)
//			if(aifcomponentContexts.length > 0){
//				boolean setPacked = false;
//				for (int inx = 0; inx < aifcomponentContexts.length; inx++) {
//					TCComponentBOMLine bl = (TCComponentBOMLine)aifcomponentContexts[inx].getComponent();
//					if (!bl.isPacked()) {
//						bl.pack();
//						setPacked = true;
//					}
//				}
//				// [SR없음][20180430][csh] Child BOM Line Pack 시 topLine referesh
//				if(setPacked){
//					try{
//						topLine.refresh();
//					}catch(TCException e){
//						MessageBox.post("Please select an option below and try again. \nEdit > Optons > Product Structure : 'Pack Structure Manager display by default'", "Error", MessageBox.ERROR);
//					}
//						
//				}
//			}
//			
//			aifcomponentContexts = topLine.getChildren();

			for(AIFComponentContext aifcomponentContext : aifcomponentContexts) {
				childBomLine = (TCComponentBOMLine)aifcomponentContext.getComponent();
				bomHash.put(makeCombinationKey(childBomLine), childBomLine);
			}

			alOpenedBOMWindow.add(bomWindow);
		}catch(TCException e) {
			System.out.println("childBomLine-->"+childBomLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
			MessageBox.post(e.getMessage(), "Error", MessageBox.ERROR);
		}

		return alOpenedBOMWindow;
	}

	public static String makeCombinationKey (TCComponentBOMLine tcbomline) throws TCException {

		String[] props = tcbomline.getProperties(bomlineProperties);

		// [SR없음][2015.11.09][jclee] Pack 된 BOM Line의 경우 OCC UID를 가져올 때 Pack된 모든 BOMLine의 OCCUID를 가져와 정렬 후 Append하여 Key로 잡아주도록 수정(BOMLine 수정 EPL이 OLD, NEW로 분리되는 현상 대응)
		//        String bl_occ_thread_id = (tcbomline.getReferenceProperty("bl_occurrence")).getUid();
		String bl_occ_thread_id = "";

		if (tcbomline.isPacked()) {
			TCComponentBOMLine[] packedLines = tcbomline.getPackedLines();
			ArrayList<String> alOccThreadUID = new ArrayList<String>();
			alOccThreadUID.add((tcbomline.getReferenceProperty("bl_occurrence")).getUid());

			for (int inx = 0; inx < packedLines.length; inx++) {
				TCComponentBOMLine tcComponentBOMLine = packedLines[inx];

				alOccThreadUID.add((tcComponentBOMLine.getReferenceProperty("bl_occurrence")).getUid());
			}

			Collections.sort(alOccThreadUID);

			for (int inx = 0; inx < alOccThreadUID.size(); inx++) {
				bl_occ_thread_id += alOccThreadUID.get(inx);
			}
		} else {
			bl_occ_thread_id = (tcbomline.getReferenceProperty("bl_occurrence")).getUid();
		}

		String combiKey = props[0]+"|"+bl_occ_thread_id;

		return combiKey;
	}
	
	public static void setOldPartTobomEditData__(String[] props, HashMap<String,Object> ccnEditData, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String changeType) throws Exception {

		try {
			TCComponentItemRevision revision = (TCComponentItemRevision)getTCSession().stringToComponent(props[24]);
			
			
//			String oldSysName = "";
//			String[] oldBomProps = bomComponent.getProperties(bomlineProperties);
			String[] oldRevProps = revision.getProperties(revisionProperties);
			// BOMLine
			ccnEditData.put("OLD_CHILD_UNIQUE_NO", props[0]);
			ccnEditData.put("OLD_CHILD_PUID", props[24]);
			ccnEditData.put("OLD_CHILD_TYPE", props[2]);
			ccnEditData.put("OLD_CHILD_NAME", props[25]);
			ccnEditData.put("OLD_CHILD_QTY", props[4]);
			ccnEditData.put("OLD_CHILD_REV", props[7]);
			ccnEditData.put("OLD_MODULE", props[9]);
			ccnEditData.put("OLD_SMODE", props[10]);
			ccnEditData.put("OLD_MANDATORY_OPT", props[11]);
			ccnEditData.put("OLD_SPECIFICATION", props[12]);
			//            ccnEditData.put("OLD_CHG_CD", oldBomProps[13]);
			ccnEditData.put("OLD_ALTER_PART", props[14]);
			ccnEditData.put("OLD_LEV", props[15]);
			if (null == props[15] || props[15].equals("")) {
				ccnEditData.put("OLD_LEV", "0");
			}
			ccnEditData.put("OLD_SYSTEM_ROW_KEY", props[16]);
			
			//func 정보를 가져오는 로직 수정
			//old
			/*
			TCComponent funcComp = getParent(bomComponent.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if (null != funcComp) {
				ccnEditData.put("OLD_FUNCTION", funcComp.getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
			}
			 */
			//new 1번
			
//			String funcStr = getParent4Digit(bomComponent.parent().getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE, "OLD");
			ccnEditData.put("OLD_FUNCTION", props[27]);
			
			// Item Rev
			ccnEditData.put("OLD_PROJECT", props[28]);
			ccnEditData.put("OLD_SYSTEM_CODE", props[23]);
//			if (null != oldBomProps[23] && !oldBomProps[23].equals("")) {
//				oldSysName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, oldBomProps[23]);
//			}
			ccnEditData.put("OLD_SYSTEM_NAME", props[29]);
			//[20180306][ljg]주석
			//			if (bomComponent.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) {
			//				ccnEditData.put("OLD_SYSTEM_CODE", "X00");
			//				ccnEditData.put("OLD_SYSTEM_NAME", "STANDARD HARD-WARES");
			//			}
			ccnEditData.put("OLD_COLOR_ID", oldRevProps[2]);
			ccnEditData.put("OLD_EST_WEIGHT", oldRevProps[3]);
			ccnEditData.put("OLD_CAL_WEIGHT", oldRevProps[4]);
			ccnEditData.put("OLD_TGT_WEIGHT", oldRevProps[5]);
			ccnEditData.put("OLD_CONTENTS", oldRevProps[6]);
			/** [20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
            if (null != oldRevProps[7] && !oldRevProps[7].equals("")) {
                ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
            }else{
                ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldBomProps[13]);
            } **/
			//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
			if (null != props[13] && !props[13].equals("")) {
				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", props[13]);
			}else{
				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
			}
			ccnEditData.put("OLD_CON_DWG_PLAN", oldRevProps[8]);
			ccnEditData.put("OLD_CON_DWG_PERFORMANCE", oldRevProps[9]);
			ccnEditData.put("OLD_CON_DWG_TYPE", oldRevProps[10]);
			ccnEditData.put("OLD_DWG_DEPLOYABLE_DATE", revision.getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
			ccnEditData.put("OLD_PRD_DWG_PERFORMANCE", oldRevProps[12]);
			ccnEditData.put("OLD_PRD_DWG_PLAN", oldRevProps[13]);

			/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
			//            ccnEditData.put("OLD_DVP_NEEDED_QTY", oldRevProps[14]);
			//            ccnEditData.put("OLD_DVP_USE", oldRevProps[15]);
			//            ccnEditData.put("OLD_DVP_REQ_DEPT", oldRevProps[16]);
			ccnEditData.put("OLD_DVP_NEEDED_QTY", props[17]);
			ccnEditData.put("OLD_DVP_USE", props[18]);
			ccnEditData.put("OLD_DVP_REQ_DEPT", props[19]);

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//            ccnEditData.put("OLD_ENG_DEPT_NM", oldRevProps[17]);
			//            ccnEditData.put("OLD_ENG_RESPONSIBLITY", getUserIdForName(oldRevProps[18], oldRevProps[17]));
			ccnEditData.put("OLD_ENG_DEPT_NM", props[20]);
			ccnEditData.put("OLD_ENG_RESPONSIBLITY", props[21]);

			//[SR170703-020] Proto Tooling 컬럼 추가
			ccnEditData.put("OLD_PROTO_TOOLING", props[22]);

			// 여기서 부터는 NEW 에만 값이 필요함
			//            ccnEditData.put("EST_COST_MATERIAL", oldRevProps[19]);
			//            ccnEditData.put("TGT_COST_MATERIAL", oldRevProps[20]);
			//            ccnEditData.put("SELECTED_COMPANY", oldRevProps[21]);
			//            ccnEditData.put("PRT_TOOLG_INVESTMENT", oldRevProps[22]);
			//            ccnEditData.put("PRD_TOOL_COST", oldRevProps[23]);
			//            ccnEditData.put("PRD_SERVICE_COST", oldRevProps[24]);
			//            ccnEditData.put("PRD_SAMPLE_COST", oldRevProps[25]);
			//            ccnEditData.put("PUR_TEAM", oldRevProps[26]);
			//            ccnEditData.put("PUR_RESPONSIBILITY", oldRevProps[27]);
			//            ccnEditData.put("EMPLOYEE_NO", oldRevProps[28]);
			//            ccnEditData.put("CHANGE_DESC", oldRevProps[29]);
			ccnEditData.put("OLD_SELECTIVE_PART", oldRevProps[30]);
			ccnEditData.put("OLD_CATEGORY", oldRevProps[31]);
			ccnEditData.put("OLD_PRD_PART_NO", oldRevProps[32]);
			ccnEditData.put("OLD_BOX", getBoxValue(oldRevProps[33]));
			ccnEditData.put("OLD_REGULATION", oldRevProps[34]);
			if (null != oldRevProps[35] && !oldRevProps[35].equals("")) {
				ccnEditData.put("OLD_CHILD_NO", oldRevProps[35]);
			}else{
				ccnEditData.put("OLD_CHILD_NO", oldRevProps[36]);
			}
			ccnEditData.put("OLD_CHILD_UNIQUE_NO", oldRevProps[36]);
			ccnEditData.put("OLD_ECO", oldRevProps[37]);
			ccnEditData.put("OLD_PRD_PROJECT", oldRevProps[38]);

			// DCS 관련 정보 가져오기
			//            HashMap<String, Object> dcsMapInfo = getDCSInfo(oldRevProps[0], oldBomProps[23]);
			//            ccnEditData.put("OLD_DC_ID", dcsMapInfo.get("DC_ID"));
			//            ccnEditData.put("OLD_DC_REV", dcsMapInfo.get("DC_REV"));
			//            ccnEditData.put("OLD_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));

//			if(!BundleUtil.nullToString(changeType).equals("")) {
//				ccnEditData.put("OLD_SEQ", props[1]);
//				ccnEditData.put("CHG_TYPE",changeType);
//				HashMap<String, Object> mapVc = getVariant(oldBomProps[5]);
//				ccnEditData.put("OLD_VC",mapVc.get("printDescriptions").toString());
//				//              ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
//				if (!bomComponent.getItemRevision().getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
//					ccnEditData.put("OLD_USAGE_LIST", getTrimInfo(bomComponent, "OLD", oldBomProps[5], ospec, optionSetMap));
//				}
//			}
			
			if(!BundleUtil.nullToString(changeType).equals("")) {
				ccnEditData.put("OLD_SEQ", props[1]);
				ccnEditData.put("CHG_TYPE",changeType);
				HashMap<String, Object> mapVc = getVariant(props[5]);
				ccnEditData.put("OLD_VC",mapVc.get("printDescriptions").toString());
				if (!props[3].equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					ccnEditData.put("OLD_USAGE_LIST", getTrimInfo_(props[4], props[16], "OLD", props[5], ospec, optionSetMap));
				}
			}
		} catch (TCException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void setNewPartTobomEditData__(String[] props, HashMap<String,Object> ccnEditData, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String changeType) throws Exception {

		try {
			TCComponentItemRevision revision = (TCComponentItemRevision)getTCSession().stringToComponent(props[24]);
			
//			String newSysName = "";
//			String[] newBomProps = bomComponent.getProperties(bomlineProperties);
			String[] newRevProps = revision.getProperties(revisionProperties);
			// BOMLine
			ccnEditData.put("NEW_CHILD_UNIQUE_NO", props[0]);
			ccnEditData.put("NEW_CHILD_PUID", props[24]);
			ccnEditData.put("NEW_CHILD_TYPE", props[2]);
			ccnEditData.put("NEW_CHILD_NAME", props[25]);
			ccnEditData.put("NEW_CHILD_QTY", props[4]);
			ccnEditData.put("NEW_CHILD_REV", props[7]);
			ccnEditData.put("NEW_MODULE", props[9]);
			ccnEditData.put("NEW_SMODE", props[10]);
			ccnEditData.put("NEW_MANDATORY_OPT", props[11]);
			ccnEditData.put("NEW_SPECIFICATION", props[12]);
			//            ccnEditData.put("NEW_CHG_CD", newBomProps[13]);
			ccnEditData.put("NEW_ALTER_PART", props[14]);
			ccnEditData.put("NEW_LEV", props[15]);
			if (null == props[15] || props[15].equals("")) {
				ccnEditData.put("NEW_LEV", "0");
			}
			ccnEditData.put("NEW_SYSTEM_ROW_KEY", props[16]);
			
			//func 정보를 가져오는 로직 수정
			//old
			/*
			TCComponent funcComp = getParent(bomComponent.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if (null != funcComp) {
				ccnEditData.put("NEW_FUNCTION", funcComp.getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
			}
			*/
			//new 1번
			
//			String funcStr = getParent4Digit(bomComponent.parent().getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE, "NEW");
			ccnEditData.put("NEW_FUNCTION", props[27]);
			
			// Item Rev
			ccnEditData.put("NEW_PROJECT", newRevProps[0]);
			ccnEditData.put("NEW_SYSTEM_CODE", props[23]);
//			if (null != newBomProps[23] && !newBomProps[23].equals("")) {
//				newSysName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, newBomProps[23]);
//			}
			ccnEditData.put("NEW_SYSTEM_NAME", props[29]);
			//[20180306][ljg]주석
			//			if (bomComponent.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) {
			//				ccnEditData.put("NEW_SYSTEM_CODE", "X00");
			//				ccnEditData.put("NEW_SYSTEM_NAME", "STANDARD HARD-WARES");
			//			}
			ccnEditData.put("NEW_COLOR_ID", newRevProps[2]);
			ccnEditData.put("NEW_EST_WEIGHT", newRevProps[3]);
			ccnEditData.put("NEW_CAL_WEIGHT", newRevProps[4]);
			ccnEditData.put("NEW_TGT_WEIGHT", newRevProps[5]);
			ccnEditData.put("NEW_CONTENTS", newRevProps[6]);

			/**[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
            if (null != newRevProps[7] && !newRevProps[7].equals("")) {
                ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
            }else{
                ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newBomProps[13]);
            } **/

			//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
			if (null != props[13] && !props[13].equals("")) {
				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", props[13]);
			}else{
				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
			}

			ccnEditData.put("NEW_CON_DWG_PLAN", newRevProps[8]);
			ccnEditData.put("NEW_CON_DWG_PERFORMANCE", newRevProps[9]);
			ccnEditData.put("NEW_CON_DWG_TYPE", newRevProps[10]);
			ccnEditData.put("NEW_DWG_DEPLOYABLE_DATE", revision.getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
			ccnEditData.put("NEW_PRD_DWG_PERFORMANCE", newRevProps[12]);
			ccnEditData.put("NEW_PRD_DWG_PLAN", newRevProps[13]);

			/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
			//            ccnEditData.put("NEW_DVP_NEEDED_QTY", newRevProps[14]);
			//            ccnEditData.put("NEW_DVP_USE", newRevProps[15]);
			//            ccnEditData.put("NEW_DVP_REQ_DEPT", newRevProps[16]);
			ccnEditData.put("NEW_DVP_NEEDED_QTY", props[17]);
			ccnEditData.put("NEW_DVP_USE", props[18]);
			ccnEditData.put("NEW_DVP_REQ_DEPT", props[19]);

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//            ccnEditData.put("NEW_ENG_DEPT_NM", newRevProps[17]);
			//            ccnEditData.put("NEW_ENG_RESPONSIBLITY", getUserIdForName(newRevProps[18], newRevProps[17]));
			ccnEditData.put("NEW_ENG_DEPT_NM", props[20]);
			ccnEditData.put("NEW_ENG_RESPONSIBLITY", props[21]);
			//[SR170703-020][LJG] Proto Tooling 컬럼 추가
			ccnEditData.put("NEW_PROTO_TOOLING", props[22]);

			ccnEditData.put("EST_COST_MATERIAL", newRevProps[19]);
			ccnEditData.put("TGT_COST_MATERIAL", newRevProps[20]);
			ccnEditData.put("SELECTED_COMPANY", newRevProps[21]);
			ccnEditData.put("PRT_TOOLG_INVESTMENT", newRevProps[22]);
			ccnEditData.put("PRD_TOOL_COST", newRevProps[23]);
			ccnEditData.put("PRD_SERVICE_COST", newRevProps[24]);
			ccnEditData.put("PRD_SAMPLE_COST", newRevProps[25]);
			ccnEditData.put("PUR_TEAM", newRevProps[26]);
			ccnEditData.put("PUR_RESPONSIBILITY", newRevProps[27]);
			ccnEditData.put("EMPLOYEE_NO", newRevProps[28]);
			ccnEditData.put("CHANGE_DESC", newRevProps[29]);
			ccnEditData.put("NEW_SELECTIVE_PART", newRevProps[30]);
			ccnEditData.put("NEW_CATEGORY", newRevProps[31]);
			ccnEditData.put("NEW_PRD_PART_NO", newRevProps[32]);
			ccnEditData.put("NEW_BOX", getBoxValue(newRevProps[33]));
			ccnEditData.put("NEW_REGULATION", newRevProps[34]);
			if (null != newRevProps[35] && !newRevProps[35].equals("")) {
				ccnEditData.put("NEW_CHILD_NO", newRevProps[35]);
			}else{
				ccnEditData.put("NEW_CHILD_NO", newRevProps[36]);
			}
			ccnEditData.put("NEW_CHILD_UNIQUE_NO", newRevProps[36]);
			ccnEditData.put("NEW_ECO", newRevProps[37]);
			// PREBOM_UNIQUE_ID 는 부모에 ID + "_" + FIND NO + "_" + 자식에 ID 로 구성 된다
			ccnEditData.put("PREBOM_UNIQUE_ID", ccnEditData.get("PARENT_UNIQUE_NO") + "_" + props[1] + "_" + newRevProps[36]);
			ccnEditData.put("NEW_PRD_PROJECT", newRevProps[38]);

			//[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
			TCComponent ccn = revision.getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
			
			if( ccn != null ) {
				String ospec_no = ccn.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO);
				ccnEditData.put("NEW_OSPEC_NO", ospec_no);
			}

			// DCS 관련 정보 가져오기
			//[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
//			HashMap<String, Object> dcsMapInfo = getDCSInfo(newRevProps[0], newBomProps[23]);
//			HashMap<String, Object> dcsMapInfo = getNewDCSInfo(newRevProps[0], newBomProps[23]);
//			ccnEditData.put("NEW_DC_ID", dcsMapInfo.get("DC_ID"));
//			ccnEditData.put("NEW_DC_REV", dcsMapInfo.get("DC_REV"));
//			ccnEditData.put("NEW_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));
			
//			Object dcs_info_obj = ccnEditData.get("DCS_INFO");
			if(props[37] != null && !props[37].equals("")){
				StringTokenizer st = new StringTokenizer(props[37], ",");
				ccnEditData.put("NEW_DC_ID", st.nextElement());
				ccnEditData.put("NEW_DC_REV", st.nextElement());
				ccnEditData.put("NEW_RELEASED_DATE", st.nextElement());
			}

//			if(!BundleUtil.nullToString(changeType).equals("")) {
//				ccnEditData.put("NEW_SEQ", newBomProps[1]);
//				ccnEditData.put("CHG_TYPE",changeType);
//				HashMap<String, Object> mapVc = getVariant(newBomProps[5]);
//				ccnEditData.put("NEW_VC",mapVc.get("printDescriptions").toString());
//				//          ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
//				if (!bomComponent.getItemRevision().getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
//					ccnEditData.put("NEW_USAGE_LIST", getTrimInfo(bomComponent, "NEW", newBomProps[5], ospec, optionSetMap));
//				}
//			}
			
			if(!BundleUtil.nullToString(changeType).equals("")) {
				ccnEditData.put("NEW_SEQ", props[1]);
				ccnEditData.put("CHG_TYPE",changeType);
				HashMap<String, Object> mapVc = getVariant(props[5]);
				ccnEditData.put("NEW_VC",mapVc.get("printDescriptions").toString());
				if (!props[3].equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					ccnEditData.put("NEW_USAGE_LIST", getTrimInfo_(props[4], props[16], "NEW", props[5], ospec, optionSetMap));
				}
			}
		} catch (TCException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void setOldPartTobomEditData_(SYMCRemoteUtil remote, DataSet ds, TCComponentItemRevision revision, HashMap<String,Object> ccnEditData, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String changeType) throws Exception {

		try {
			ArrayList<HashMap<String, Object>> arrParentEPLDataOld = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.CCNService", "arrParentEPLDataOld", ds);
			if(arrParentEPLDataOld != null && arrParentEPLDataOld.size() > 0){
				Iterator<String> iterOld = arrParentEPLDataOld.get(0).keySet().iterator();
				while (iterOld.hasNext()) {
					String sKey = (String) iterOld.next();
					Object value = arrParentEPLDataOld.get(0).get(sKey);
					if(value != null && !value.equals("")){
						ccnEditData.put(sKey, value);
					}
				}
			}
			
			
			String oldSysName = "";
//			String[] oldBomProps = bomComponent.getProperties(bomlineProperties);
			String[] oldRevProps = revision.getProperties(revisionProperties);
			// BOMLine
//			ccnEditData.put("OLD_CHILD_UNIQUE_NO", oldBomProps[0]);
//			ccnEditData.put("OLD_CHILD_PUID", bomComponent.getItemRevision().getUid());
//			ccnEditData.put("OLD_CHILD_TYPE", oldBomProps[2]);
//			ccnEditData.put("OLD_CHILD_NAME", oldBomProps[3]);
//			ccnEditData.put("OLD_CHILD_QTY", oldBomProps[4]);
//			ccnEditData.put("OLD_CHILD_REV", oldBomProps[7]);
//			ccnEditData.put("OLD_MODULE", oldBomProps[9]);
//			ccnEditData.put("OLD_SMODE", oldBomProps[10]);
//			ccnEditData.put("OLD_MANDATORY_OPT", oldBomProps[11]);
//			ccnEditData.put("OLD_SPECIFICATION", oldBomProps[12]);
//			//            ccnEditData.put("OLD_CHG_CD", oldBomProps[13]);
//			ccnEditData.put("OLD_ALTER_PART", oldBomProps[14]);
//			ccnEditData.put("OLD_LEV", oldBomProps[15]);
//			if (null == oldBomProps[15] || oldBomProps[15].equals("")) {
			Object old_lev_obj = ccnEditData.get("OLD_LEV");
			if(old_lev_obj == null || old_lev_obj.toString().equals("")){
				ccnEditData.put("OLD_LEV", "0");
			}
//			ccnEditData.put("OLD_SYSTEM_ROW_KEY", oldBomProps[16]);
			
			//func 정보를 가져오는 로직 수정
			//old
			/*
			TCComponent funcComp = getParent(bomComponent.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if (null != funcComp) {
				ccnEditData.put("OLD_FUNCTION", funcComp.getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
			}
			 */
			//new 1번
			
//			String funcStr = getParent4Digit(bomComponent.parent().getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE, "OLD");
//			ccnEditData.put("OLD_FUNCTION", funcStr);
			
			// Item Rev
//			ccnEditData.put("OLD_PROJECT", oldRevProps[0]);
//			ccnEditData.put("OLD_SYSTEM_CODE", oldBomProps[23]);
//			if (null != oldBomProps[23] && !oldBomProps[23].equals("")) {
//				oldSysName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, oldBomProps[23]);
//			}
//			ccnEditData.put("OLD_SYSTEM_NAME", oldSysName);
			//[20180306][ljg]주석
			//			if (bomComponent.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) {
			//				ccnEditData.put("OLD_SYSTEM_CODE", "X00");
			//				ccnEditData.put("OLD_SYSTEM_NAME", "STANDARD HARD-WARES");
			//			}
			ccnEditData.put("OLD_COLOR_ID", oldRevProps[2]);
			ccnEditData.put("OLD_EST_WEIGHT", oldRevProps[3]);
			ccnEditData.put("OLD_CAL_WEIGHT", oldRevProps[4]);
			ccnEditData.put("OLD_TGT_WEIGHT", oldRevProps[5]);
			ccnEditData.put("OLD_CONTENTS", oldRevProps[6]);
			/** [20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
            if (null != oldRevProps[7] && !oldRevProps[7].equals("")) {
                ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
            }else{
                ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldBomProps[13]);
            } **/
			//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
			Object old_chg_type_engconcept = ccnEditData.get("OLD_CHG_TYPE_ENGCONCEPT");
			if(old_chg_type_engconcept == null || old_chg_type_engconcept.toString().equals("")){
				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
			}
//			if (null != oldBomProps[13] && !oldBomProps[13].equals("")) {
//				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldBomProps[13]);
//			}else{
//				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
//			}
			ccnEditData.put("OLD_CON_DWG_PLAN", oldRevProps[8]);
			ccnEditData.put("OLD_CON_DWG_PERFORMANCE", oldRevProps[9]);
			ccnEditData.put("OLD_CON_DWG_TYPE", oldRevProps[10]);
			ccnEditData.put("OLD_DWG_DEPLOYABLE_DATE", revision.getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
			ccnEditData.put("OLD_PRD_DWG_PERFORMANCE", oldRevProps[12]);
			ccnEditData.put("OLD_PRD_DWG_PLAN", oldRevProps[13]);

			/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
			//            ccnEditData.put("OLD_DVP_NEEDED_QTY", oldRevProps[14]);
			//            ccnEditData.put("OLD_DVP_USE", oldRevProps[15]);
			//            ccnEditData.put("OLD_DVP_REQ_DEPT", oldRevProps[16]);
//			ccnEditData.put("OLD_DVP_NEEDED_QTY", oldBomProps[17]);
//			ccnEditData.put("OLD_DVP_USE", oldBomProps[18]);
//			ccnEditData.put("OLD_DVP_REQ_DEPT", oldBomProps[19]);

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//            ccnEditData.put("OLD_ENG_DEPT_NM", oldRevProps[17]);
			//            ccnEditData.put("OLD_ENG_RESPONSIBLITY", getUserIdForName(oldRevProps[18], oldRevProps[17]));
//			ccnEditData.put("OLD_ENG_DEPT_NM", oldBomProps[20]);
//			ccnEditData.put("OLD_ENG_RESPONSIBLITY", getUserIdForName(oldBomProps[21], oldBomProps[20]));

			//[SR170703-020] Proto Tooling 컬럼 추가
//			ccnEditData.put("OLD_PROTO_TOOLING", oldBomProps[22]);

			// 여기서 부터는 NEW 에만 값이 필요함
			//            ccnEditData.put("EST_COST_MATERIAL", oldRevProps[19]);
			//            ccnEditData.put("TGT_COST_MATERIAL", oldRevProps[20]);
			//            ccnEditData.put("SELECTED_COMPANY", oldRevProps[21]);
			//            ccnEditData.put("PRT_TOOLG_INVESTMENT", oldRevProps[22]);
			//            ccnEditData.put("PRD_TOOL_COST", oldRevProps[23]);
			//            ccnEditData.put("PRD_SERVICE_COST", oldRevProps[24]);
			//            ccnEditData.put("PRD_SAMPLE_COST", oldRevProps[25]);
			//            ccnEditData.put("PUR_TEAM", oldRevProps[26]);
			//            ccnEditData.put("PUR_RESPONSIBILITY", oldRevProps[27]);
			//            ccnEditData.put("EMPLOYEE_NO", oldRevProps[28]);
			//            ccnEditData.put("CHANGE_DESC", oldRevProps[29]);
			ccnEditData.put("OLD_SELECTIVE_PART", oldRevProps[30]);
			ccnEditData.put("OLD_CATEGORY", oldRevProps[31]);
			ccnEditData.put("OLD_PRD_PART_NO", oldRevProps[32]);
			ccnEditData.put("OLD_BOX", getBoxValue(oldRevProps[33]));
			ccnEditData.put("OLD_REGULATION", oldRevProps[34]);
			if (null != oldRevProps[35] && !oldRevProps[35].equals("")) {
				ccnEditData.put("OLD_CHILD_NO", oldRevProps[35]);
			}else{
				ccnEditData.put("OLD_CHILD_NO", oldRevProps[36]);
			}
//			ccnEditData.put("OLD_CHILD_UNIQUE_NO", oldRevProps[36]);
			ccnEditData.put("OLD_ECO", oldRevProps[37]);
			ccnEditData.put("OLD_PRD_PROJECT", oldRevProps[38]);

			// DCS 관련 정보 가져오기
			//            HashMap<String, Object> dcsMapInfo = getDCSInfo(oldRevProps[0], oldBomProps[23]);
			//            ccnEditData.put("OLD_DC_ID", dcsMapInfo.get("DC_ID"));
			//            ccnEditData.put("OLD_DC_REV", dcsMapInfo.get("DC_REV"));
			//            ccnEditData.put("OLD_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));

			if(!BundleUtil.nullToString(changeType).equals("")) {
//				ccnEditData.put("OLD_SEQ", oldBomProps[1]);
				ccnEditData.put("CHG_TYPE",changeType);
				Object old_vc_obj = ccnEditData.get("OLD_VC");
				String old_vc = "";
				if(old_vc_obj != null && !old_vc_obj.toString().equals("")){
					old_vc = old_vc_obj.toString();
				}
				HashMap<String, Object> mapVc = getVariant(old_vc);
				ccnEditData.put("OLD_VC",mapVc.get("printDescriptions").toString());
				//              ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
				if (!revision.getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					Object old_child_qty_obj = ccnEditData.get("OLD_CHILD_QTY");
					String old_child_qty = "1";
					if(old_child_qty_obj != null && !old_child_qty_obj.toString().equals("")){
						old_child_qty = old_child_qty_obj.toString();
					}
					Object old_system_row_key_obj = ccnEditData.get("OLD_SYSTEM_ROW_KEY");
					String old_system_row_key = "";
					if(old_system_row_key_obj != null && !old_system_row_key_obj.toString().equals("")){
						old_system_row_key = old_system_row_key_obj.toString();
					}
					ccnEditData.put("OLD_USAGE_LIST", getTrimInfo_(old_child_qty, old_system_row_key, "OLD", old_vc, ospec, optionSetMap));
				}
			}
		} catch (TCException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void setNewPartTobomEditData_(SYMCRemoteUtil remote, DataSet ds, TCComponentItemRevision revision, HashMap<String,Object> ccnEditData, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String changeType) throws Exception {

		try {
			ArrayList<HashMap<String, Object>> arrParentEPLDataNew = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.CCNService", "arrParentEPLDataNew", ds);
			if(arrParentEPLDataNew != null && arrParentEPLDataNew.size() > 0){
				Iterator<String> iterNew = arrParentEPLDataNew.get(0).keySet().iterator();
				while (iterNew.hasNext()) {
					String sKey = (String) iterNew.next();
					Object value = arrParentEPLDataNew.get(0).get(sKey);
					if(value != null && !value.equals("")){
						ccnEditData.put(sKey, value);
					}
				}
			}
			
			
//			String newSysName = "";
//			String[] newBomProps = bomComponent.getProperties(bomlineProperties);
			String[] newRevProps = revision.getProperties(revisionProperties);
			// BOMLine
//			ccnEditData.put("NEW_CHILD_UNIQUE_NO", newBomProps[0]);
//			ccnEditData.put("NEW_CHILD_PUID", bomComponent.getItemRevision().getUid());
//			ccnEditData.put("NEW_CHILD_TYPE", newBomProps[2]);
//			ccnEditData.put("NEW_CHILD_NAME", newBomProps[3]);
//			ccnEditData.put("NEW_CHILD_QTY", newBomProps[4]);
//			ccnEditData.put("NEW_CHILD_REV", newBomProps[7]);
//			ccnEditData.put("NEW_MODULE", newBomProps[9]);
//			ccnEditData.put("NEW_SMODE", newBomProps[10]);
//			ccnEditData.put("NEW_MANDATORY_OPT", newBomProps[11]);
//			ccnEditData.put("NEW_SPECIFICATION", newBomProps[12]);
			//            ccnEditData.put("NEW_CHG_CD", newBomProps[13]);
//			ccnEditData.put("NEW_ALTER_PART", newBomProps[14]);
//			ccnEditData.put("NEW_LEV", newBomProps[15]);
//			if (bomComponent.getItem().getType().equals(TypeConstant.S7_PREFUNCMASTERTYPE) && (null == newBomProps[15] || newBomProps[15].equals(""))) {
//				ccnEditData.put("NEW_LEV", "0");
//			}
			Object new_lev_obj = ccnEditData.get("NEW_LEV");
			if(new_lev_obj == null || new_lev_obj.toString().equals("")){
				ccnEditData.put("NEW_LEV", "0");
			}
//			ccnEditData.put("NEW_SYSTEM_ROW_KEY", newBomProps[16]);
			
			//func 정보를 가져오는 로직 수정
			//old
			/*
			TCComponent funcComp = getParent(bomComponent.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if (null != funcComp) {
				ccnEditData.put("NEW_FUNCTION", funcComp.getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
			}
			*/
			//new 1번
			
//			String funcStr = getParent4Digit(bomComponent.parent().getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE, "NEW");
//			ccnEditData.put("NEW_FUNCTION", funcStr);
			
			// Item Rev
//			ccnEditData.put("NEW_PROJECT", newRevProps[0]);
//			ccnEditData.put("NEW_SYSTEM_CODE", newBomProps[23]);
//			if (null != newBomProps[23] && !newBomProps[23].equals("")) {
//				newSysName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, newBomProps[23]);
//			}
//			ccnEditData.put("NEW_SYSTEM_NAME", newSysName);
			//[20180306][ljg]주석
			//			if (bomComponent.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) {
			//				ccnEditData.put("NEW_SYSTEM_CODE", "X00");
			//				ccnEditData.put("NEW_SYSTEM_NAME", "STANDARD HARD-WARES");
			//			}
			ccnEditData.put("NEW_COLOR_ID", newRevProps[2]);
			ccnEditData.put("NEW_EST_WEIGHT", newRevProps[3]);
			ccnEditData.put("NEW_CAL_WEIGHT", newRevProps[4]);
			ccnEditData.put("NEW_TGT_WEIGHT", newRevProps[5]);
			ccnEditData.put("NEW_CONTENTS", newRevProps[6]);

			/**[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
            if (null != newRevProps[7] && !newRevProps[7].equals("")) {
                ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
            }else{
                ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newBomProps[13]);
            } **/

			//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
			Object new_chg_type_engconcept = ccnEditData.get("NEW_CHG_TYPE_ENGCONCEPT");
			if(new_chg_type_engconcept == null || new_chg_type_engconcept.toString().equals("")){
				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
			}
			
//			if (null != newBomProps[13] && !newBomProps[13].equals("")) {
//				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newBomProps[13]);
//			}else{
//				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
//			}

			ccnEditData.put("NEW_CON_DWG_PLAN", newRevProps[8]);
			ccnEditData.put("NEW_CON_DWG_PERFORMANCE", newRevProps[9]);
			ccnEditData.put("NEW_CON_DWG_TYPE", newRevProps[10]);
			ccnEditData.put("NEW_DWG_DEPLOYABLE_DATE", revision.getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
			ccnEditData.put("NEW_PRD_DWG_PERFORMANCE", newRevProps[12]);
			ccnEditData.put("NEW_PRD_DWG_PLAN", newRevProps[13]);

			/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
			//            ccnEditData.put("NEW_DVP_NEEDED_QTY", newRevProps[14]);
			//            ccnEditData.put("NEW_DVP_USE", newRevProps[15]);
			//            ccnEditData.put("NEW_DVP_REQ_DEPT", newRevProps[16]);
//			ccnEditData.put("NEW_DVP_NEEDED_QTY", newBomProps[17]);
//			ccnEditData.put("NEW_DVP_USE", newBomProps[18]);
//			ccnEditData.put("NEW_DVP_REQ_DEPT", newBomProps[19]);

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//            ccnEditData.put("NEW_ENG_DEPT_NM", newRevProps[17]);
			//            ccnEditData.put("NEW_ENG_RESPONSIBLITY", getUserIdForName(newRevProps[18], newRevProps[17]));
//			ccnEditData.put("NEW_ENG_DEPT_NM", newBomProps[20]);
//			ccnEditData.put("NEW_ENG_RESPONSIBLITY", getUserIdForName(newBomProps[21], newBomProps[20]));
			//[SR170703-020][LJG] Proto Tooling 컬럼 추가
//			ccnEditData.put("NEW_PROTO_TOOLING", newBomProps[22]);

			ccnEditData.put("EST_COST_MATERIAL", newRevProps[19]);
			ccnEditData.put("TGT_COST_MATERIAL", newRevProps[20]);
			ccnEditData.put("SELECTED_COMPANY", newRevProps[21]);
			ccnEditData.put("PRT_TOOLG_INVESTMENT", newRevProps[22]);
			ccnEditData.put("PRD_TOOL_COST", newRevProps[23]);
			ccnEditData.put("PRD_SERVICE_COST", newRevProps[24]);
			ccnEditData.put("PRD_SAMPLE_COST", newRevProps[25]);
			ccnEditData.put("PUR_TEAM", newRevProps[26]);
			ccnEditData.put("PUR_RESPONSIBILITY", newRevProps[27]);
			ccnEditData.put("EMPLOYEE_NO", newRevProps[28]);
			ccnEditData.put("CHANGE_DESC", newRevProps[29]);
			ccnEditData.put("NEW_SELECTIVE_PART", newRevProps[30]);
			ccnEditData.put("NEW_CATEGORY", newRevProps[31]);
			ccnEditData.put("NEW_PRD_PART_NO", newRevProps[32]);
			ccnEditData.put("NEW_BOX", getBoxValue(newRevProps[33]));
			ccnEditData.put("NEW_REGULATION", newRevProps[34]);
			if (null != newRevProps[35] && !newRevProps[35].equals("")) {
				ccnEditData.put("NEW_CHILD_NO", newRevProps[35]);
			}else{
				ccnEditData.put("NEW_CHILD_NO", newRevProps[36]);
			}
//			ccnEditData.put("NEW_CHILD_UNIQUE_NO", newRevProps[36]);
			ccnEditData.put("NEW_ECO", newRevProps[37]);
			// PREBOM_UNIQUE_ID 는 부모에 ID + "_" + FIND NO + "_" + 자식에 ID 로 구성 된다
			ccnEditData.put("PREBOM_UNIQUE_ID", ccnEditData.get("PARENT_UNIQUE_NO") + "_" + ccnEditData.get("NEW_SEQ") + "_" + newRevProps[36]);
			ccnEditData.put("NEW_PRD_PROJECT", newRevProps[38]);

			//[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
//			TCComponent ccn = bomComponent.getItemRevision().getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
//			
//			if( ccn != null ) {
//				String ospec_no = ccn.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO);
//				ccnEditData.put("NEW_OSPEC_NO", ospec_no);
//			}

			// DCS 관련 정보 가져오기
			//[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
//			HashMap<String, Object> dcsMapInfo = getDCSInfo(newRevProps[0], newBomProps[23]);
			Object dcs_info_obj = ccnEditData.get("DCS_INFO");
			if(dcs_info_obj != null && !dcs_info_obj.toString().equals("")){
				StringTokenizer st = new StringTokenizer(dcs_info_obj.toString(), ",");
				ccnEditData.put("NEW_DC_ID", st.nextElement());
				ccnEditData.put("NEW_DC_REV", st.nextElement());
				ccnEditData.put("NEW_RELEASED_DATE", st.nextElement());
			}
//			HashMap<String, Object> dcsMapInfo = getNewDCSInfo(newRevProps[0], newBomProps[23]);
//			ccnEditData.put("NEW_DC_ID", dcsMapInfo.get("DC_ID"));
//			ccnEditData.put("NEW_DC_REV", dcsMapInfo.get("DC_REV"));
//			ccnEditData.put("NEW_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));

			if(!BundleUtil.nullToString(changeType).equals("")) {
//				ccnEditData.put("NEW_SEQ", newBomProps[1]);
				ccnEditData.put("CHG_TYPE",changeType);
				Object new_vc_obj = ccnEditData.get("NEW_VC");
				String new_vc = "";
				if(new_vc_obj != null && !new_vc_obj.toString().equals("")){
					new_vc = new_vc_obj.toString();
				}
				HashMap<String, Object> mapVc = getVariant(new_vc);
				ccnEditData.put("NEW_VC",mapVc.get("printDescriptions").toString());
				//          ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
				if (!revision.getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					Object new_child_qty_obj = ccnEditData.get("NEW_CHILD_QTY");
					String new_child_qty = "1";
					if(new_child_qty_obj != null && !new_child_qty_obj.toString().equals("")){
						new_child_qty = new_child_qty_obj.toString();
					}
					Object new_system_row_key_obj = ccnEditData.get("NEW_SYSTEM_ROW_KEY");
					String new_system_row_key = "";
					if(new_system_row_key_obj != null && !new_system_row_key_obj.toString().equals("")){
						new_system_row_key = new_system_row_key_obj.toString();
					}
					ccnEditData.put("NEW_USAGE_LIST", getTrimInfo_(new_child_qty, new_system_row_key, "NEW", new_vc, ospec, optionSetMap));
				}
			}
			
			
			if(!BundleUtil.nullToString(changeType).equals("")) {

				Object old_vc_obj = ccnEditData.get("OLD_VC");
				String old_vc = "";
				if(old_vc_obj != null && !old_vc_obj.toString().equals("")){
					old_vc = old_vc_obj.toString();
				}
				HashMap<String, Object> mapVc = getVariant(old_vc);
				ccnEditData.put("OLD_VC",mapVc.get("printDescriptions").toString());
				//              ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
				if (!revision.getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					Object old_child_qty_obj = ccnEditData.get("OLD_CHILD_QTY");
					String old_child_qty = "1";
					if(old_child_qty_obj != null && !old_child_qty_obj.toString().equals("")){
						old_child_qty = old_child_qty_obj.toString();
					}
					Object old_system_row_key_obj = ccnEditData.get("OLD_SYSTEM_ROW_KEY");
					String old_system_row_key = "";
					if(old_system_row_key_obj != null && !old_system_row_key_obj.toString().equals("")){
						old_system_row_key = old_system_row_key_obj.toString();
					}
					ccnEditData.put("OLD_USAGE_LIST", getTrimInfo_(old_child_qty, old_system_row_key, "OLD", old_vc, ospec, optionSetMap));
				}
			}
		} catch (TCException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void setOldPartTobomEditData(TCComponentBOMLine bomComponent, HashMap<String,Object> ccnEditData, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String changeType) throws Exception {

		try {
			String oldSysName = "";
			String[] oldBomProps = bomComponent.getProperties(bomlineProperties);
			String[] oldRevProps = bomComponent.getItemRevision().getProperties(revisionProperties);
			// BOMLine
			ccnEditData.put("OLD_CHILD_UNIQUE_NO", oldBomProps[0]);
			ccnEditData.put("OLD_CHILD_PUID", bomComponent.getItemRevision().getUid());
			ccnEditData.put("OLD_CHILD_TYPE", oldBomProps[2]);
			ccnEditData.put("OLD_CHILD_NAME", oldBomProps[3]);
			ccnEditData.put("OLD_CHILD_QTY", oldBomProps[4]);
			ccnEditData.put("OLD_CHILD_REV", oldBomProps[7]);
			ccnEditData.put("OLD_MODULE", oldBomProps[9]);
			ccnEditData.put("OLD_SMODE", oldBomProps[10]);
			ccnEditData.put("OLD_MANDATORY_OPT", oldBomProps[11]);
			ccnEditData.put("OLD_SPECIFICATION", oldBomProps[12]);
			//            ccnEditData.put("OLD_CHG_CD", oldBomProps[13]);
			ccnEditData.put("OLD_ALTER_PART", oldBomProps[14]);
			ccnEditData.put("OLD_LEV", oldBomProps[15]);
			if (bomComponent.getItem().getType().equals(TypeConstant.S7_PREFUNCMASTERTYPE) && (null == oldBomProps[15] || oldBomProps[15].equals(""))) {
				ccnEditData.put("OLD_LEV", "0");
			}
			ccnEditData.put("OLD_SYSTEM_ROW_KEY", oldBomProps[16]);
			
			//func 정보를 가져오는 로직 수정
			//old
			
			TCComponent funcComp = getParent(bomComponent.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if (null != funcComp) {
				ccnEditData.put("OLD_FUNCTION", funcComp.getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
			}
			 
			//new 1번
			/*
			String funcStr = getParent4Digit(bomComponent.parent().getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE, "OLD");
			ccnEditData.put("OLD_FUNCTION", funcStr);
			*/
			// Item Rev
			ccnEditData.put("OLD_PROJECT", oldRevProps[0]);
			ccnEditData.put("OLD_SYSTEM_CODE", oldBomProps[23]);
			if (null != oldBomProps[23] && !oldBomProps[23].equals("")) {
				oldSysName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, oldBomProps[23]);
			}
			ccnEditData.put("OLD_SYSTEM_NAME", oldSysName);
			//[20180306][ljg]주석
			//			if (bomComponent.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) {
			//				ccnEditData.put("OLD_SYSTEM_CODE", "X00");
			//				ccnEditData.put("OLD_SYSTEM_NAME", "STANDARD HARD-WARES");
			//			}
			ccnEditData.put("OLD_COLOR_ID", oldRevProps[2]);
			ccnEditData.put("OLD_EST_WEIGHT", oldRevProps[3]);
			ccnEditData.put("OLD_CAL_WEIGHT", oldRevProps[4]);
			ccnEditData.put("OLD_TGT_WEIGHT", oldRevProps[5]);
			ccnEditData.put("OLD_CONTENTS", oldRevProps[6]);
			/** [20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
            if (null != oldRevProps[7] && !oldRevProps[7].equals("")) {
                ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
            }else{
                ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldBomProps[13]);
            } **/
			//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
			if (null != oldBomProps[13] && !oldBomProps[13].equals("")) {
				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldBomProps[13]);
			}else{
				ccnEditData.put("OLD_CHG_TYPE_ENGCONCEPT", oldRevProps[7]);
			}
			ccnEditData.put("OLD_CON_DWG_PLAN", oldRevProps[8]);
			ccnEditData.put("OLD_CON_DWG_PERFORMANCE", oldRevProps[9]);
			ccnEditData.put("OLD_CON_DWG_TYPE", oldRevProps[10]);
			ccnEditData.put("OLD_DWG_DEPLOYABLE_DATE", bomComponent.getItemRevision().getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
			ccnEditData.put("OLD_PRD_DWG_PERFORMANCE", oldRevProps[12]);
			ccnEditData.put("OLD_PRD_DWG_PLAN", oldRevProps[13]);

			/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
			//            ccnEditData.put("OLD_DVP_NEEDED_QTY", oldRevProps[14]);
			//            ccnEditData.put("OLD_DVP_USE", oldRevProps[15]);
			//            ccnEditData.put("OLD_DVP_REQ_DEPT", oldRevProps[16]);
			ccnEditData.put("OLD_DVP_NEEDED_QTY", oldBomProps[17]);
			ccnEditData.put("OLD_DVP_USE", oldBomProps[18]);
			ccnEditData.put("OLD_DVP_REQ_DEPT", oldBomProps[19]);

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//            ccnEditData.put("OLD_ENG_DEPT_NM", oldRevProps[17]);
			//            ccnEditData.put("OLD_ENG_RESPONSIBLITY", getUserIdForName(oldRevProps[18], oldRevProps[17]));
			ccnEditData.put("OLD_ENG_DEPT_NM", oldBomProps[20]);
			ccnEditData.put("OLD_ENG_RESPONSIBLITY", getUserIdForName(oldBomProps[21], oldBomProps[20]));

			//[SR170703-020] Proto Tooling 컬럼 추가
			ccnEditData.put("OLD_PROTO_TOOLING", oldBomProps[22]);

			// 여기서 부터는 NEW 에만 값이 필요함
			//            ccnEditData.put("EST_COST_MATERIAL", oldRevProps[19]);
			//            ccnEditData.put("TGT_COST_MATERIAL", oldRevProps[20]);
			//            ccnEditData.put("SELECTED_COMPANY", oldRevProps[21]);
			//            ccnEditData.put("PRT_TOOLG_INVESTMENT", oldRevProps[22]);
			//            ccnEditData.put("PRD_TOOL_COST", oldRevProps[23]);
			//            ccnEditData.put("PRD_SERVICE_COST", oldRevProps[24]);
			//            ccnEditData.put("PRD_SAMPLE_COST", oldRevProps[25]);
			//            ccnEditData.put("PUR_TEAM", oldRevProps[26]);
			//            ccnEditData.put("PUR_RESPONSIBILITY", oldRevProps[27]);
			//            ccnEditData.put("EMPLOYEE_NO", oldRevProps[28]);
			//            ccnEditData.put("CHANGE_DESC", oldRevProps[29]);
			ccnEditData.put("OLD_SELECTIVE_PART", oldRevProps[30]);
			ccnEditData.put("OLD_CATEGORY", oldRevProps[31]);
			ccnEditData.put("OLD_PRD_PART_NO", oldRevProps[32]);
			ccnEditData.put("OLD_BOX", getBoxValue(oldRevProps[33]));
			ccnEditData.put("OLD_REGULATION", oldRevProps[34]);
			if (null != oldRevProps[35] && !oldRevProps[35].equals("")) {
				ccnEditData.put("OLD_CHILD_NO", oldRevProps[35]);
			}else{
				ccnEditData.put("OLD_CHILD_NO", oldRevProps[36]);
			}
			ccnEditData.put("OLD_CHILD_UNIQUE_NO", oldRevProps[36]);
			ccnEditData.put("OLD_ECO", oldRevProps[37]);
			ccnEditData.put("OLD_PRD_PROJECT", oldRevProps[38]);

			// DCS 관련 정보 가져오기
			//            HashMap<String, Object> dcsMapInfo = getDCSInfo(oldRevProps[0], oldBomProps[23]);
			//            ccnEditData.put("OLD_DC_ID", dcsMapInfo.get("DC_ID"));
			//            ccnEditData.put("OLD_DC_REV", dcsMapInfo.get("DC_REV"));
			//            ccnEditData.put("OLD_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));

			if(!BundleUtil.nullToString(changeType).equals("")) {
				ccnEditData.put("OLD_SEQ", oldBomProps[1]);
				ccnEditData.put("CHG_TYPE",changeType);
				HashMap<String, Object> mapVc = getVariant(oldBomProps[5]);
				ccnEditData.put("OLD_VC",mapVc.get("printDescriptions").toString());
				//              ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
				if (!bomComponent.getItemRevision().getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					ccnEditData.put("OLD_USAGE_LIST", getTrimInfo(bomComponent, "OLD", oldBomProps[5], ospec, optionSetMap));
				}
			}
		} catch (TCException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void setNewPartTobomEditData(TCComponentBOMLine bomComponent, HashMap<String,Object> ccnEditData, OSpec ospec, HashMap<String, StoredOptionSet> optionSetMap, String changeType) throws Exception {

		try {
			String newSysName = "";
			String[] newBomProps = bomComponent.getProperties(bomlineProperties);
			String[] newRevProps = bomComponent.getItemRevision().getProperties(revisionProperties);
			// BOMLine
			ccnEditData.put("NEW_CHILD_UNIQUE_NO", newBomProps[0]);
			ccnEditData.put("NEW_CHILD_PUID", bomComponent.getItemRevision().getUid());
			ccnEditData.put("NEW_CHILD_TYPE", newBomProps[2]);
			ccnEditData.put("NEW_CHILD_NAME", newBomProps[3]);
			ccnEditData.put("NEW_CHILD_QTY", newBomProps[4]);
			ccnEditData.put("NEW_CHILD_REV", newBomProps[7]);
			ccnEditData.put("NEW_MODULE", newBomProps[9]);
			ccnEditData.put("NEW_SMODE", newBomProps[10]);
			ccnEditData.put("NEW_MANDATORY_OPT", newBomProps[11]);
			ccnEditData.put("NEW_SPECIFICATION", newBomProps[12]);
			//            ccnEditData.put("NEW_CHG_CD", newBomProps[13]);
			ccnEditData.put("NEW_ALTER_PART", newBomProps[14]);
			ccnEditData.put("NEW_LEV", newBomProps[15]);
			if (bomComponent.getItem().getType().equals(TypeConstant.S7_PREFUNCMASTERTYPE) && (null == newBomProps[15] || newBomProps[15].equals(""))) {
				ccnEditData.put("NEW_LEV", "0");
			}
			ccnEditData.put("NEW_SYSTEM_ROW_KEY", newBomProps[16]);
			
			//func 정보를 가져오는 로직 수정
			//old
			
			TCComponent funcComp = getParent(bomComponent.getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
			if (null != funcComp) {
				ccnEditData.put("NEW_FUNCTION", funcComp.getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4));
			}
			
			//new 1번
			/*
			String funcStr = getParent4Digit(bomComponent.parent().getItemRevision(), TypeConstant.S7_PREFUNCTIONREVISIONTYPE, "NEW");
			ccnEditData.put("NEW_FUNCTION", funcStr);
			*/
			// Item Rev
			ccnEditData.put("NEW_PROJECT", newRevProps[0]);
			ccnEditData.put("NEW_SYSTEM_CODE", newBomProps[23]);
			if (null != newBomProps[23] && !newBomProps[23].equals("")) {
				newSysName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, newBomProps[23]);
			}
			ccnEditData.put("NEW_SYSTEM_NAME", newSysName);
			//[20180306][ljg]주석
			//			if (bomComponent.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) {
			//				ccnEditData.put("NEW_SYSTEM_CODE", "X00");
			//				ccnEditData.put("NEW_SYSTEM_NAME", "STANDARD HARD-WARES");
			//			}
			ccnEditData.put("NEW_COLOR_ID", newRevProps[2]);
			ccnEditData.put("NEW_EST_WEIGHT", newRevProps[3]);
			ccnEditData.put("NEW_CAL_WEIGHT", newRevProps[4]);
			ccnEditData.put("NEW_TGT_WEIGHT", newRevProps[5]);
			ccnEditData.put("NEW_CONTENTS", newRevProps[6]);

			/**[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
            if (null != newRevProps[7] && !newRevProps[7].equals("")) {
                ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
            }else{
                ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newBomProps[13]);
            } **/

			//[20170524][ljg] cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
			if (null != newBomProps[13] && !newBomProps[13].equals("")) {
				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newBomProps[13]);
			}else{
				ccnEditData.put("NEW_CHG_TYPE_ENGCONCEPT", newRevProps[7]);
			}

			ccnEditData.put("NEW_CON_DWG_PLAN", newRevProps[8]);
			ccnEditData.put("NEW_CON_DWG_PERFORMANCE", newRevProps[9]);
			ccnEditData.put("NEW_CON_DWG_TYPE", newRevProps[10]);
			ccnEditData.put("NEW_DWG_DEPLOYABLE_DATE", bomComponent.getItemRevision().getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
			ccnEditData.put("NEW_PRD_DWG_PERFORMANCE", newRevProps[12]);
			ccnEditData.put("NEW_PRD_DWG_PLAN", newRevProps[13]);

			/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
			//            ccnEditData.put("NEW_DVP_NEEDED_QTY", newRevProps[14]);
			//            ccnEditData.put("NEW_DVP_USE", newRevProps[15]);
			//            ccnEditData.put("NEW_DVP_REQ_DEPT", newRevProps[16]);
			ccnEditData.put("NEW_DVP_NEEDED_QTY", newBomProps[17]);
			ccnEditData.put("NEW_DVP_USE", newBomProps[18]);
			ccnEditData.put("NEW_DVP_REQ_DEPT", newBomProps[19]);

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//            ccnEditData.put("NEW_ENG_DEPT_NM", newRevProps[17]);
			//            ccnEditData.put("NEW_ENG_RESPONSIBLITY", getUserIdForName(newRevProps[18], newRevProps[17]));
			ccnEditData.put("NEW_ENG_DEPT_NM", newBomProps[20]);
			ccnEditData.put("NEW_ENG_RESPONSIBLITY", getUserIdForName(newBomProps[21], newBomProps[20]));
			//[SR170703-020][LJG] Proto Tooling 컬럼 추가
			ccnEditData.put("NEW_PROTO_TOOLING", newBomProps[22]);

			ccnEditData.put("EST_COST_MATERIAL", newRevProps[19]);
			ccnEditData.put("TGT_COST_MATERIAL", newRevProps[20]);
			ccnEditData.put("SELECTED_COMPANY", newRevProps[21]);
			ccnEditData.put("PRT_TOOLG_INVESTMENT", newRevProps[22]);
			ccnEditData.put("PRD_TOOL_COST", newRevProps[23]);
			ccnEditData.put("PRD_SERVICE_COST", newRevProps[24]);
			ccnEditData.put("PRD_SAMPLE_COST", newRevProps[25]);
			ccnEditData.put("PUR_TEAM", newRevProps[26]);
			ccnEditData.put("PUR_RESPONSIBILITY", newRevProps[27]);
			ccnEditData.put("EMPLOYEE_NO", newRevProps[28]);
			ccnEditData.put("CHANGE_DESC", newRevProps[29]);
			ccnEditData.put("NEW_SELECTIVE_PART", newRevProps[30]);
			ccnEditData.put("NEW_CATEGORY", newRevProps[31]);
			ccnEditData.put("NEW_PRD_PART_NO", newRevProps[32]);
			ccnEditData.put("NEW_BOX", getBoxValue(newRevProps[33]));
			ccnEditData.put("NEW_REGULATION", newRevProps[34]);
			if (null != newRevProps[35] && !newRevProps[35].equals("")) {
				ccnEditData.put("NEW_CHILD_NO", newRevProps[35]);
			}else{
				ccnEditData.put("NEW_CHILD_NO", newRevProps[36]);
			}
			ccnEditData.put("NEW_CHILD_UNIQUE_NO", newRevProps[36]);
			ccnEditData.put("NEW_ECO", newRevProps[37]);
			// PREBOM_UNIQUE_ID 는 부모에 ID + "_" + FIND NO + "_" + 자식에 ID 로 구성 된다
			ccnEditData.put("PREBOM_UNIQUE_ID", ccnEditData.get("PARENT_UNIQUE_NO") + "_" + newBomProps[1] + "_" + newRevProps[36]);
			ccnEditData.put("NEW_PRD_PROJECT", newRevProps[38]);

			//[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
			TCComponent ccn = bomComponent.getItemRevision().getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
			
			if( ccn != null ) {
				String ospec_no = ccn.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO);
				ccnEditData.put("NEW_OSPEC_NO", ospec_no);
			}

			// DCS 관련 정보 가져오기
			//[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
//			HashMap<String, Object> dcsMapInfo = getDCSInfo(newRevProps[0], newBomProps[23]);
			HashMap<String, Object> dcsMapInfo = getNewDCSInfo(newRevProps[0], newBomProps[23]);
			ccnEditData.put("NEW_DC_ID", dcsMapInfo.get("DC_ID"));
			ccnEditData.put("NEW_DC_REV", dcsMapInfo.get("DC_REV"));
			ccnEditData.put("NEW_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));

			if(!BundleUtil.nullToString(changeType).equals("")) {
				ccnEditData.put("NEW_SEQ", newBomProps[1]);
				ccnEditData.put("CHG_TYPE",changeType);
				HashMap<String, Object> mapVc = getVariant(newBomProps[5]);
				ccnEditData.put("NEW_VC",mapVc.get("printDescriptions").toString());
				//          ccnEditData.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
				if (!bomComponent.getItemRevision().getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
					ccnEditData.put("NEW_USAGE_LIST", getTrimInfo(bomComponent, "NEW", newBomProps[5], ospec, optionSetMap));
				}
			}
		} catch (TCException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static String getBoxValue(String boxValue) {
		if (null == boxValue || boxValue.equals("")) {
			return "";
		}
		return boxValue.substring(0, 1) + "B";
	}

	public static HashMap<String, Object> getDCSInfo(String projectCode, String sysCode) throws Exception {
		HashMap<String, Object> dcsMapInfo = new HashMap<String, Object>();
		TCComponent[] tccomps = SDVQueryUtils.executeSavedQuery("SYMC_Search_DesignConcept", new String[]{"Project Code", "System Code"}, new String[]{projectCode, sysCode});
		if (null != tccomps && tccomps.length > 0) {
			TCComponentItem dcsItem = (TCComponentItem) tccomps[0];
			TCComponentItemRevision dcsItemRev = SYMTcUtil.getLatestReleasedRevision(dcsItem);
			if (null != dcsItemRev) {
				dcsMapInfo.put("DC_ID", dcsItemRev.getProperty(PropertyConstant.ATTR_NAME_ITEMID));
				dcsMapInfo.put("DC_REV", dcsItemRev.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID));
				dcsMapInfo.put("DC_RELEASED_DATE", dcsItemRev.getDateProperty(PropertyConstant.ATTR_NAME_DATERELEASED));
			}
		}
		return dcsMapInfo;
	}
	
	//[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
	public static HashMap<String, Object> getNewDCSInfo(String projectCode, String sysCode) throws Exception {
        HashMap<String, Object> dcsMapInfo = new HashMap<String, Object>();
        
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PROJECT_CODE", projectCode);
		ds.put("SYSTEM_CODE", sysCode);
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.OSpecService", "getDCSInfo", ds);
		if(list.size() > 0){
        	dcsMapInfo = list.get(0);
        }

        return dcsMapInfo;
    }
	
	public static void setParentTobomEditData__(String ccn_no, String ccn_owner, String[] props, HashMap<String, Object> ccnEditData, String changeType) throws Exception {

		ccnEditData.put("PARENT_UNIQUE_NO",props[30]);
		if(!props[31].equals("")){
			ccnEditData.put("PARENT_NO",props[31]);
		} else {
			ccnEditData.put("PARENT_NO",props[30]);
		}
		ccnEditData.put("PARENT_TYPE", props[32]);
		ccnEditData.put("PARENT_NAME", props[33]);
		ccnEditData.put("PARENT_REV", props[34]);
		ccnEditData.put("CCN_ID", ccn_no);
		ccnEditData.put("PARENT_UID", props[35]);
		ccnEditData.put("PARENT_MOD_DATE", props[36]);
		ccnEditData.put("USER_ID", ccn_owner);
		if(!BundleUtil.nullToString(changeType).equals("")) {
			ccnEditData.put("CHG_TYPE", changeType);
		}

//		AIFComponentContext[] acc = tcItemRevision.getChildren(PropertyConstant.ATTR_NAME_BL_STRC_REVISION);
//
//		if(acc.length > 0 && null != acc[0]) {
//			TCComponentBOMViewRevision tcBomviewRevision = (TCComponentBOMViewRevision)acc[0].getComponent();
//			ccnEditData.put("PARENT_MOD_DATE", simpleDateFormat.format(tcBomviewRevision.getDateProperty(PropertyConstant.ATTR_NAME_LASTMODDATE)));
//			ccnEditData.put("USER_ID", ((TCComponentUser)ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGUSER)).getUserId());
//			if(!BundleUtil.nullToString(changeType).equals("")) {
//				ccnEditData.put("CHG_TYPE", changeType);
//			}
//		}
	}

	public static void setParentTobomEditData(TCComponentChangeItemRevision ccnRevision, TCComponentItemRevision tcItemRevision, HashMap<String, Object> ccnEditData, String changeType) throws Exception {

		ccnEditData.put("PARENT_UNIQUE_NO",tcItemRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
		if (null != tcItemRevision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO) && !tcItemRevision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO).equals("")) {
			ccnEditData.put("PARENT_NO",tcItemRevision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO));
		}else{
			ccnEditData.put("PARENT_NO",tcItemRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
		}
		ccnEditData.put("PARENT_TYPE", tcItemRevision.getTypeObject().getName());
		ccnEditData.put("PARENT_NAME", tcItemRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME));
		ccnEditData.put("PARENT_REV", tcItemRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID));
		ccnEditData.put("CCN_ID", ccnRevision.getItem().toString());
		ccnEditData.put("PARENT_UID", tcItemRevision.getUid());

		AIFComponentContext[] acc = tcItemRevision.getChildren(PropertyConstant.ATTR_NAME_BL_STRC_REVISION);

		if(acc.length > 0 && null != acc[0]) {
			TCComponentBOMViewRevision tcBomviewRevision = (TCComponentBOMViewRevision)acc[0].getComponent();
			ccnEditData.put("PARENT_MOD_DATE", simpleDateFormat.format(tcBomviewRevision.getDateProperty(PropertyConstant.ATTR_NAME_LASTMODDATE)));
			ccnEditData.put("USER_ID", ((TCComponentUser)ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGUSER)).getUserId());
			if(!BundleUtil.nullToString(changeType).equals("")) {
				ccnEditData.put("CHG_TYPE", changeType);
			}
		}
	}

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
		for (String value : values) {
			if (!descriptions.containsKey(value))
				continue;
			printDescriptions = printDescriptions.replace(value, descriptions.get(value));
		}

		data.put("values", values);
		data.put("descriptions", descriptions);
		data.put("printValues", printValues);
		data.put("printDescriptions", printDescriptions);

		return data;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, String> getDescriptionFromVariant(ArrayList<String> values) {
		HashMap<String, String> descriptions = new HashMap<String, String>();

		// 1차 WAS를 호출
		SYMCRemoteUtil remote = new SYMCRemoteUtil();

		DataSet ds = new DataSet();

		for (String value : values) {
			ds.put("code_name", value);

			try {
				ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.VariantService", "getVariantValueDesc", ds);
				if (list != null) {
					for (HashMap<String, String> map : list) {
						descriptions.put(map.get("CODE_NAME"), map.get("CODE_DESC"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return descriptions;
	}


	public static boolean compareString (String oldStr, String newStr) throws Exception{

		if("".equals(BundleUtil.nullToString(oldStr)) && "".equals(BundleUtil.nullToString(newStr))) {
			return true;
		}else if(!"".equals(BundleUtil.nullToString(oldStr)) && "".equals(BundleUtil.nullToString(newStr))){
			return false;
		}else if("".equals(BundleUtil.nullToString(oldStr)) && !"".equals(BundleUtil.nullToString(newStr))) {
			return false;
		} else {
			if(oldStr.equals(newStr)) {
				return true;
			}else{
				return false;
			}
		}
	}

	public static String[] bomlineProperties =
		{
		PropertyConstant.ATTR_NAME_BL_ITEM_ID,               // 0
		PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO,           // 1
		PropertyConstant.ATTR_NAME_BL_OBJECT_TYPE,           // 2
		PropertyConstant.ATTR_NAME_BL_REV_OBJECT_TYPE,       // 3
		PropertyConstant.ATTR_NAME_BL_QUANTITY,              // 4
		PropertyConstant.ATTR_NAME_BL_VARIANT_CONDITION,     // 5
		PropertyConstant.ATTR_NAME_BL_OCC_FND_OBJECT_ID,     // 6
		PropertyConstant.ATTR_NAME_BL_ITEM_REVISION_ID,      // 7
		PropertyConstant.ATTR_NAME_BL_ABS_OCC_ID,            // 8
		PropertyConstant.ATTR_NAME_BL_MODULE_CODE,           // 9
		PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE,           // 10
		PropertyConstant.ATTR_NAME_BL_REQ_OPT,               // 11  REQ OPT
		PropertyConstant.ATTR_NAME_BL_SPEC_DESC,             // 12
		PropertyConstant.ATTR_NAME_BL_CHG_CD,                // 13     
		PropertyConstant.ATTR_NAME_BL_ALTER_PART,            // 14
		PropertyConstant.ATTR_NAME_BL_LEV_M,                 // 15  LEV M
		PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY,        // 16

		/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
		PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY,		 // 17
		PropertyConstant.ATTR_NAME_BL_DVP_USE,               // 18
		PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT,          // 19

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM,           // 20
		PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY,      // 21

		//[SR170703-020][LJG] Proto Tooling 컬럼 추가
		PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING, // 22

		//[20180213][LJG] BOMLine으로 이동 ->사용 안함
		PropertyConstant.ATTR_NAME_BL_BUDGETCODE // 23
		};

	public static String[] revisionProperties =
		{
		PropertyConstant.ATTR_NAME_PROJCODE,               // 0
		PropertyConstant.ATTR_NAME_BUDGETCODE,             // 1
		PropertyConstant.ATTR_NAME_COLORID,                // 2
		PropertyConstant.ATTR_NAME_ESTWEIGHT,              // 3     ESTIMATE WEIGHT
		PropertyConstant.ATTR_NAME_CALWEIGHT,              // 4
		PropertyConstant.ATTR_NAME_TARGET_WEIGHT,          // 5     TARGET WEIGHT
		PropertyConstant.ATTR_NAME_CONTENTS,               // 6
		PropertyConstant.ATTR_NAME_CHG_TYPE_NM,            // 7
		//            PropertyConstant.ATTR_NAME_ORIGIN_PROJECT,        // 8 번째
		PropertyConstant.ATTR_NAME_CON_DWG_PLAN,           // 8
		PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE,    // 9
		PropertyConstant.ATTR_NAME_CON_DWG_TYPE,           // 10
		PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE,    // 11
		PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE,    // 12
		PropertyConstant.ATTR_NAME_PRD_DWG_PLAN,           // 13

		/* [SR없음][20150914][jclee] DVP Sample 정보 BOMLine으로 이동 */
		//            PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY,         // 14
		//            PropertyConstant.ATTR_NAME_DVP_USE,                // 15
		//            PropertyConstant.ATTR_NAME_DVP_REQ_DEPT,           // 16
		"",											         // 14
		"",                									 // 15
		"",           										 // 16

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		//            PropertyConstant.ATTR_NAME_ENG_DEPT_NM,            // 17
		//            PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY,      // 18
		"",            // 17
		"",      // 18
		//            PropertyConstant.ATTR_NAME_CIC_DEPT_NM,             // 19 번째
		PropertyConstant.ATTR_NAME_EST_COST_MATERIAL,      // 19
		PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL,   // 20
		PropertyConstant.ATTR_NAME_SELECTED_COMPANY,       // 21
		PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT,   // 22
		PropertyConstant.ATTR_NAME_PRD_TOOL_COST,          // 23
		PropertyConstant.ATTR_NAME_PRD_SERVICE_COST,       // 24
		PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST,        // 25
		// [20160907][ymjang] 컬럼명 오류 수정
		PropertyConstant.ATTR_NAME_PUR_DEPT_NM,               // 26
		PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY,     // 27
		// [20160907][ymjang] 컬럼명 오류 수정
		"",            // 28
		//PropertyConstant.ATTR_NAME_EMPLOYEE_NO,            // 28
		PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION,     // 29
		PropertyConstant.ATTR_NAME_SELECTIVEPART,          // 30
		PropertyConstant.ATTR_NAME_DR,                     // 31
		PropertyConstant.ATTR_NAME_OLD_PART_NO,            // 32
		PropertyConstant.ATTR_NAME_BOX,                    // 33
		PropertyConstant.ATTR_NAME_REGULATION,             // 34
		PropertyConstant.ATTR_NAME_DISPLAYPARTNO,          // 35
		PropertyConstant.ATTR_NAME_ITEMID,                 // 36
		PropertyConstant.ATTR_NAME_ECO_NO,                 // 37
		PropertyConstant.ATTR_NAME_PRD_PROJ_CODE           // 38
		};

	/**
	 * Master List에서 비용 관련 컬럼 열람 권한 체크
	 * @param groupName
	 * @param userName
	 * @return
	 * @throws TCException
	 */
	public static boolean hasCostViewRight(String groupName, String userName) throws TCException{
		TCSession session = CustomUtil.getTCSession();
		TCComponentUser user = session.getUser();
		TCComponentPerson person = (TCComponentPerson)user.getRelatedComponent("person");
		String currentUserName = person.getProperty("user_name");
		String pa6 = person.getProperty("PA6");

		if( pa6.equals(groupName) ){
			if( currentUserName.equals(userName)){
				return true;
			}else{
				Map<TCComponentGroup, List<TCComponentRole>> roleTable = user.getGroupRolesTable();
				for( TCComponentGroup group : roleTable.keySet()){
					List<TCComponentRole> roleList = roleTable.get(group);
					for( TCComponentRole role : roleList){
						String roleName = role.getProperty("role_name");
						if( roleName.equals("CORDINATOR")){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 *  BOMLine 에 Usage 정보를 가져온다
	 * @param line
	 * @param historyType
	 * @param conditionStr
	 * @param ospec
	 * @param storedOptionSetMap
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<HashMap<String, Object>> getTrimInfo(TCComponentBOMLine line, String historyType, String conditionStr, OSpec ospec, HashMap<String, StoredOptionSet> storedOptionSetMap) throws Exception{
		ArrayList<HashMap<String, Object>> usageList = new ArrayList<HashMap<String, Object>>();

		//EA는 Double형의 Quantity가 올 수 없다.
		String qty = line.getProperty(PropertyConstant.ATTR_NAME_BL_QUANTITY);

		//Integer Type이 아니면 그대로 표기함.
		double dNum = Double.parseDouble(qty);
		int iNum = (int)dNum;
		if( dNum == iNum){
			qty = "" + iNum;
		}
		String sysRowKey = line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);

		ArrayList<OpTrim> trimList = ospec.getTrimList();
		String simpleCondition = BomUtil.convertToSimpleCondition(conditionStr);

		if( storedOptionSetMap != null){
			for( OpTrim trim : trimList){
				if (null != conditionStr && !conditionStr.equals("")) {
					String sosStdName = trim.getTrim() + "_STD";
					String sosOptName = trim.getTrim() + "_OPT";
					StoredOptionSet sosStd = storedOptionSetMap.get(sosStdName);
					StoredOptionSet sosOpt = storedOptionSetMap.get(sosOptName);
					if( sosStd.isInclude( simpleCondition)){
						usageList.add(getUsageInfo(trim, qty, "STD", historyType, sysRowKey));
					}else if( sosOpt.isInclude( simpleCondition)){
						usageList.add(getUsageInfo(trim, qty, "OPT", historyType, sysRowKey));
					}
				}else{
					usageList.add(getUsageInfo(trim, qty, "STD", historyType, sysRowKey));
				}
			}
		}
		return usageList;
	}
	
	public static ArrayList<HashMap<String, Object>> getTrimInfo_(String qty, String sysRowKey, String historyType, String conditionStr, OSpec ospec, HashMap<String, StoredOptionSet> storedOptionSetMap) throws Exception{
		ArrayList<HashMap<String, Object>> usageList = new ArrayList<HashMap<String, Object>>();

		//EA는 Double형의 Quantity가 올 수 없다.
//		String qty = line.getProperty(PropertyConstant.ATTR_NAME_BL_QUANTITY);

		//Integer Type이 아니면 그대로 표기함.
		double dNum = Double.parseDouble(qty);
		int iNum = (int)dNum;
		if( dNum == iNum){
			qty = "" + iNum;
		}
//		String sysRowKey = line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);

		ArrayList<OpTrim> trimList = ospec.getTrimList();
		String simpleCondition = BomUtil.convertToSimpleCondition(conditionStr);

		if( storedOptionSetMap != null){
			for( OpTrim trim : trimList){
				if (null != conditionStr && !conditionStr.equals("")) {
					String sosStdName = trim.getTrim() + "_STD";
					String sosOptName = trim.getTrim() + "_OPT";
					StoredOptionSet sosStd = storedOptionSetMap.get(sosStdName);
					StoredOptionSet sosOpt = storedOptionSetMap.get(sosOptName);
					if( sosStd.isInclude( simpleCondition)){
						usageList.add(getUsageInfo(trim, qty, "STD", historyType, sysRowKey));
					}else if( sosOpt.isInclude( simpleCondition)){
						usageList.add(getUsageInfo(trim, qty, "OPT", historyType, sysRowKey));
					}
				}else{
					usageList.add(getUsageInfo(trim, qty, "STD", historyType, sysRowKey));
				}
			}
		}
		return usageList;
	}

	/**
	 * USAGE 정보를 DB 테이블에 맞게 바꾼다
	 * @param trim
	 * @param qty
	 * @param optionType
	 * @param historyType
	 * @return
	 */
	public static HashMap<String, Object> getUsageInfo(OpTrim trim, String qty, String optionType, String historyType, String sysRowKey) {
		HashMap<String, Object> usageMap = new HashMap<String, Object>();
		usageMap.put("USAGE_LV1", trim.getArea());
		usageMap.put("USAGE_LV2", trim.getPassenger());
		usageMap.put("USAGE_LV3", trim.getEngine());
		usageMap.put("USAGE_LV4", trim.getGrade());
		usageMap.put("USAGE_LV5", trim.getTrim());
		usageMap.put("USAGE_QTY", qty);
		usageMap.put("OPTION_TYPE", optionType);
		usageMap.put("HISTORY_TYPE", historyType);
		usageMap.put("SYSTEM_ROW_KEY", sysRowKey);
		return usageMap;
	}

	/**
	 * StoredOptionSet 정보를 가져온다
	 * [SR160316-025][201603250][jclee] BUG Fix
	 * @param ospec
	 * @return
	 * @throws Exception
	 */
	public static HashMap<String, StoredOptionSet> getOptionSet(OSpec ospec) throws Exception{
		HashMap<String, StoredOptionSet> optionSetMap = new HashMap<String, StoredOptionSet>();
		ArrayList<OpTrim> trimList = ospec.getTrimList();
		HashMap<String, ArrayList<Option>> trimOptionMap = ospec.getOptions();

		for( OpTrim opTrim : trimList){
			ArrayList<Option> options = new ArrayList<Option>();
			String stdName = "";
			String optName = "";

			options = trimOptionMap.get(opTrim.getTrim());
			stdName = opTrim.getTrim() + "_STD";
			optName = opTrim.getTrim() + "_OPT";

			StoredOptionSet stdSos = new StoredOptionSet(stdName);
			stdSos.add("TRIM", stdName);
			StoredOptionSet optSos = new StoredOptionSet(optName);
			optSos.add("TRIM", optName);

			if (options == null) {
				optionSetMap.put(stdName, null);
				optionSetMap.put(optName, null);

				continue;
			}

			for( Option option : options){
				if( option.getValue().equalsIgnoreCase("S")){
					stdSos.add(option.getOp(), option.getOpValue());
					optSos.add(option.getOp(), option.getOpValue());
				}else if( !option.getValue().equalsIgnoreCase("-") ){
					optSos.add(option.getOp(), option.getOpValue());
				}
			}
			optionSetMap.put(stdName, stdSos);
			optionSetMap.put(optName, optSos);
		}

		return optionSetMap;
	}

	/**
	 * 1. CCN 마스터 정보를 Insert 한다
	 * 2. CCN 을지 정보를 생성한다
	 * 3. CCN 을지 정보를 DB 에 Insert 한다  
	 *
     *[CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 * 데이터 생성 로직 변경으로 사용하지 않아 주석 처리 buildCCNEPL__을 사용함 
	 * @param ccnRevision   (타겟 CCN Rev)
	 * @param checkSoltionItem   (SoltionItem 다시 재 구성 여부, TRUE 이면 재구성)
	 * @param checkIFData   (IF 테이블에 값을 넣을지 여부, TRUE 이면 INSERT)
	 * @throws Exception
	 */
//	public static void setCCNEplInfo(TCComponentChangeItemRevision ccnRevision, boolean checkSoltionItem, boolean checkIFData) throws Exception{
//		CustomCCNDao dao = new CustomCCNDao();
//		String ccnId = ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
//		// CCN Master 테이블에 값을 넣는다
//		dao.deleteCCNMaster(ccnId);
//		dao.insertCCNMaster(ccnRevision, checkIFData);
//
//		ArrayList<HashMap<String, Object>> arrResultEPL = buildCCNEPL(ccnRevision, checkSoltionItem, null);
//
//		// 가져온 값을 DB 테이블에 넣는다
//		if (null != arrResultEPL && arrResultEPL.size() > 0) {
//			dao.insertCCNEplList(ccnId, arrResultEPL);
//			if (checkIFData) {
//				dao.insertIfCCNEplList(ccnId, arrResultEPL);
//			}
//		}
//	}

	/**
	 * Home Folder 하위에서 PREBOM Folder를 찾고 존재하지 않을 시, 생성한 후 리턴함.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static TCComponentFolder getPreBomFolder() throws Exception{

		TCComponentFolder preBomFolder = null;

		TCSession session = CustomUtil.getTCSession();
		TCComponentUser user = session.getUser();
		TCComponentFolder homeFolder = user.getHomeFolder();
		AIFComponentContext[] contexts = homeFolder.getRelated("contents");
		boolean bFound = false;
		for( int i = 0; contexts != null && i < contexts.length; i++){
			TCComponent com = (TCComponent)contexts[i].getComponent();
			String type = com.getType();
			if( type.equals("Folder")){
				String objectName = com.getProperty("object_name");
				if( objectName.equals("PREBOM")){
					preBomFolder = (TCComponentFolder)com;
					bFound = true;
				}
			}
		}

		if( !bFound){
			preBomFolder = CustomUtil.createFolder("PREBOM", "", "Folder");
			homeFolder.add("contents", preBomFolder);
		}

		return preBomFolder;
	}

	public static ArrayList getKeyList(HashMap map){
		IteratorEnumeration iEnum = new IteratorEnumeration( map.keySet().iterator());
		ArrayList keyList = Collections.list(iEnum);
		Collections.sort(keyList);
		return keyList;
	}

	public static class IteratorEnumeration<E> implements Enumeration<E>
	{
		private final Iterator<E> iterator;

		public IteratorEnumeration(Iterator<E> iterator)
		{
			this.iterator = iterator;
		}

		public E nextElement() {
			return iterator.next();
		}

		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

	}

	public static String getUserIdForName(String userName, String groupNm) throws Exception {
		if (userName == null || userName.trim().length() == 0){
			return "";
		}

		TCComponent[] userComponents = CustomUtil.queryComponent("SYMC_Search_User", new String[]{"PersonName"}, new String[]{userName});

		if (userComponents == null || userComponents.length == 0){
			return "";
		} else if (userComponents.length > 1){
			for (TCComponent userComponent : userComponents) {
				TCComponentPerson person = (TCComponentPerson)userComponent.getRelatedComponent("person");
				if (person.getProperty(PropertyConstant.PROP_DEPT_NAME).equals(groupNm)) {
					return ((TCComponentUser) userComponent).getUserId();
				}
			}
		} else {
			return ((TCComponentUser) userComponents[0]).getUserId();
		}
		return "";
	}

	/**
	 * Item Id 리스트로 현재 BOM Line 에 사용하고 있는 BOM리스트를 찾음
	 * 
	 * @param findIdList
	 * @return
	 */
	public static LinkedList<TCComponentBOMLine> findBOMLinesWithId(ArrayList<String> findIdList, TCComponentBOMLine topBOMLine)
	{
		LinkedList<TCComponentBOMLine> findedBOMLineList = new LinkedList<TCComponentBOMLine>();
		StructureFilterWithExpand.ExpandAndSearchResponse expandSearchResponse = null;
		StructureFilterWithExpandService strExpandService = StructureFilterWithExpandService.getService(topBOMLine.getSession());
		TCComponentBOMLine[] expandedBOMLines = { topBOMLine };

		StructureFilterWithExpand.SearchCondition[] conditions = new StructureFilterWithExpand.SearchCondition[findIdList.size()];
		for (int i = 0; i < conditions.length; i++)
		{
			StructureFilterWithExpand.SearchCondition localSearchCondition = new StructureFilterWithExpand.SearchCondition();
			localSearchCondition.logicalOperator = "OR";
			localSearchCondition.propertyName = "bl_item_item_id";
			localSearchCondition.relationalOperator = "=";
			localSearchCondition.inputValue = findIdList.get(i);
			conditions[i] = localSearchCondition;
		}

		expandSearchResponse = strExpandService.expandAndSearch(expandedBOMLines, conditions);

		for (ExpandAndSearchOutput output : expandSearchResponse.outputLines)
		{
			TCComponentBOMLine findedBOMLine = output.resultLine;
			if (findedBOMLineList.contains(findedBOMLine))
				continue;
			findedBOMLineList.add(findedBOMLine);
		}

		return findedBOMLineList;
	}
}
