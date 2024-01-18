package com.symc.work.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.util.DateUtil;
import com.symc.work.model.NameGroupConditionVO;
import com.symc.work.model.NameGroupCountResultVO;
import com.symc.work.model.NameGroupInfoVO;

public class PngVerificationService {
	private TcCommonDao dao;
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");

	public void startService() throws Exception {
		long lStart = logTime("START");

		dao = TcCommonDao.getTcCommonDao();

		// Product List 조회
		ArrayList<String> alProducts = getProducts();

		// FOR TEST
//		 ArrayList<String> alProducts = new ArrayList<String>();
//		 alProducts.add("PVUA2018");

		ArrayList<HashMap<String, Object>> alErrors = new ArrayList<HashMap<String, Object>>();
		//Error Log 생성일
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date toDay = new Date();
		String creationDate = df.format(toDay);
		
		String rowKey = getRowkey();

		for (int inx = 0; inx < alProducts.size(); inx++) {
			// 해당 Product, Spec의 End Item 조회
			ArrayList<HashMap<String, Object>> alGroups = new ArrayList<HashMap<String, Object>>();
			HashMap<String, NameGroupInfoVO> hmNameGroupInfo = new HashMap<String, NameGroupInfoVO>();

			HashMap<String, HashMap<String, NameGroupCountResultVO>> hmSpecNameGroupCountResult = new HashMap<String, HashMap<String, NameGroupCountResultVO>>(); // Spec별 Name Group Count Result

			String sProduct = alProducts.get(inx);
			
			set1LevelItemList(sProduct, rowKey);

			// Product 별 15일 생산계획 스펙 조회
			ArrayList<HashMap<String, String>> alPlanSpecs = getPlan15SpecList(sProduct);

			// 해당 Product의 모든 Part Name Group 조회
			alGroups = getNameGroupMaster(sProduct, null);

			ExecutorService executor = Executors.newFixedThreadPool(5);

			// 15일 생산계획 스펙 별 End Item Name List 조회
			for (int jnx = 0; jnx < alPlanSpecs.size(); jnx++) {
				HashMap<String, String> hmPlanSpec = alPlanSpecs.get(jnx);
				String sPUID = hmPlanSpec.get("PUID");

				// Product, PUID를 입력하여 hmSpecNameGroupCountResult, alGroups를 Output으로 받는다.
				SpecCollector collector = new SpecCollector(sProduct, sPUID, alGroups, hmSpecNameGroupCountResult, rowKey);
				executor.execute(collector);
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			// Group ID, Product 별 Master, Part Name List, Condition 조회
			for (int jnx = 0; jnx < alGroups.size(); jnx++) {
				HashMap<String, Object> hmGroup = alGroups.get(jnx);
				String sGroupID = hmGroup.get("GROUP_ID").toString();
				
				// FOR TEST
//				if (sGroupID.equals("PG8876")) {
//					System.out.println("TEST 1");
//				}
				
				String sGroupName = hmGroup.get("GROUP_NAME").toString();

				ArrayList<HashMap<String, Object>> alNameGroupMaster = getNameGroupMaster(sProduct, sGroupID);
				HashMap<String, Object> hmNameGroupMaster = alNameGroupMaster.get(0); // Name Group Master 조회

				ArrayList<HashMap<String, Object>> alNameGroupConditions = getNameGroupCondition(sGroupID); // Condition 조회

				NameGroupInfoVO voNameGroupInfo = new NameGroupInfoVO(sGroupID, sGroupName);
				voNameGroupInfo.setGroupID(hmNameGroupMaster.get("GROUP_ID").toString());
				voNameGroupInfo.setGroupName(hmNameGroupMaster.get("GROUP_NAME").toString());
				voNameGroupInfo.setRefFunctions(hmNameGroupMaster.get("REF_FUNCS").toString());
				voNameGroupInfo.setDefaultQuantity(((BigDecimal) hmNameGroupMaster.get("DEFAULT_QTY")).intValue());
				voNameGroupInfo.setEnable(hmNameGroupMaster.get("IS_ENABLED").toString().equals("1") ? true : false);
				voNameGroupInfo.setDescription(hmNameGroupMaster.get("DESCRIPTION") == null ? "" : hmNameGroupMaster.get("DESCRIPTION").toString());

				ArrayList<NameGroupConditionVO> alNameGroupConditionForInfo = new ArrayList<NameGroupConditionVO>();
				for (int knx = 0; knx < alNameGroupConditions.size(); knx++) {
					HashMap<String, Object> hmNameGroupCondition = alNameGroupConditions.get(knx);

					int iNameGroupConditionGroupNumber = Integer.parseInt(hmNameGroupCondition.get("GROUP_NUM").toString());
					String sNameGroupConditionProduct = hmNameGroupCondition.get("PRODUCT").toString();
					String sNameGroupConditionCondition = hmNameGroupCondition.get("CONDITION").toString();
					String sNameGroupConditionOperator = hmNameGroupCondition.get("OPERATOR").toString();
					int iNameGroupConditionQuantity = Integer.parseInt(hmNameGroupCondition.get("QTY").toString());
					String sNameGroupConditionPartName = hmNameGroupCondition.get("PART_NAME") == null ? "" : hmNameGroupCondition.get("PART_NAME").toString();

					boolean isContain = false;
					if (alNameGroupConditionForInfo.size() > 0) {
						for (int lnx = 0; lnx < alNameGroupConditionForInfo.size(); lnx++) {
							NameGroupConditionVO voNameGroupConditionTemp = alNameGroupConditionForInfo.get(lnx);

							int iNameGroupConditionGroupNumberTemp = voNameGroupConditionTemp.getGroupNumber();
							String sNameGroupConditionProductTemp = voNameGroupConditionTemp.getProduct();
							String sNameGroupConditionConditionTemp = voNameGroupConditionTemp.getCondition();
							String sNameGroupConditionOperatorTemp = voNameGroupConditionTemp.getOperator();
							int iNameGroupConditionQuantityTemp = voNameGroupConditionTemp.getQuantity();
							ArrayList<String> alNameGroupConditionPartNameListTemp = voNameGroupConditionTemp.getPartNameList();

							if (iNameGroupConditionGroupNumberTemp == iNameGroupConditionGroupNumber && sNameGroupConditionProductTemp.equals(sNameGroupConditionProduct) && sNameGroupConditionConditionTemp.equals(sNameGroupConditionCondition) && sNameGroupConditionOperatorTemp.equals(sNameGroupConditionOperator) && iNameGroupConditionQuantityTemp == iNameGroupConditionQuantity) {

								if (alNameGroupConditionPartNameListTemp == null) {
									alNameGroupConditionPartNameListTemp = new ArrayList<String>();
								}

								alNameGroupConditionPartNameListTemp.add(sNameGroupConditionPartName);
								voNameGroupConditionTemp.setPartNameList(alNameGroupConditionPartNameListTemp);

								isContain = true;
								break;
							}
						}
					}

					if (!isContain) {
						NameGroupConditionVO voNameGroupCondition = new NameGroupConditionVO(iNameGroupConditionGroupNumber, sNameGroupConditionProduct, sNameGroupConditionCondition, sNameGroupConditionOperator, iNameGroupConditionQuantity);

						ArrayList<String> alPartName = new ArrayList<String>();
						if (sNameGroupConditionPartName != null && !sNameGroupConditionPartName.equals("") && sNameGroupConditionPartName.length() != 0) {
							alPartName.add(sNameGroupConditionPartName);
						} else {
							//2019-04-08 Bug 수정
							//alPartName.add("");
						}

						voNameGroupCondition.setPartNameList(alPartName);
						alNameGroupConditionForInfo.add(voNameGroupCondition);
						voNameGroupInfo.setConditionList(alNameGroupConditionForInfo);
					}
				}

				hmNameGroupInfo.put(sGroupID, voNameGroupInfo);
			}

			// Verify
			Set<String> ksSpecNos = hmSpecNameGroupCountResult.keySet();
			Iterator<String> itSpecNos = ksSpecNos.iterator();
			while (itSpecNos.hasNext()) {
				String sSpecNo = itSpecNos.next();

				HashMap<String, NameGroupCountResultVO> hmNameGroupCountResult = hmSpecNameGroupCountResult.get(sSpecNo);
				String sSpecStr = getSpec(sSpecNo);
				//[CSH]20190504 15일 생산계획 스펙에 조회되지만 HBOM의 spec이 null 인것이 있네....
				//spec이 null 인것은 skip
				if(sSpecStr != null && !sSpecStr.equals("")){

					for (int jnx = 0; jnx < alGroups.size(); jnx++) {
						HashMap<String, Object> hmGroup = alGroups.get(jnx);
						
						String sGroupID = hmGroup.get("GROUP_ID").toString();
						
						// FOR TEST
	//					if (sGroupID.equals("PG8876")) {
	//						System.out.println("TEST 2");
	//					}
						
						NameGroupCountResultVO voNameGroupCountResult = hmNameGroupCountResult.get(sGroupID);
	
						// Group ID 별 Name Group Info, Condition List, Part Name List
						NameGroupInfoVO voNameGroupInfo = hmNameGroupInfo.get(sGroupID);
						ArrayList<NameGroupConditionVO> alNameGroupConditionLists = voNameGroupInfo.getConditionList();
	
						int iDefaultQty = voNameGroupInfo.getDefaultQuantity();
						int iTotalCount = voNameGroupCountResult.getTotCount();
	
						if (alNameGroupConditionLists == null || alNameGroupConditionLists.isEmpty()) {
							if (iTotalCount != iDefaultQty) {
								voNameGroupCountResult.setValid(false);
								voNameGroupCountResult.setReason(iDefaultQty + " &lt;&gt; " + iTotalCount);
							}
						} else {
							boolean isChecked = false;
							int iPreGroupNumber = -1;
	
							for (int lnx = 0; alNameGroupConditionLists != null && lnx < alNameGroupConditionLists.size(); lnx++) {
								NameGroupConditionVO voNameGroupCondition = alNameGroupConditionLists.get(lnx);
	
								int iGroupNumber = voNameGroupCondition.getGroupNumber();
								if (iPreGroupNumber != iGroupNumber && !voNameGroupCountResult.isValid()) {
									break;
								}
	
								ArrayList<String> alPartNameList = voNameGroupCondition.getPartNameList();
	
								if (sProduct.equals(voNameGroupCondition.getProduct())) {
									if (isAvailable(voNameGroupCondition.getCondition(), sSpecStr)) {
										String operator = null;
	
										if (voNameGroupCondition.getOperator().equals("=")) {
											operator = "==";
										} else {
											operator = voNameGroupCondition.getOperator();
										}
	
										String sOperation = null;
	
										int iCount = 0;
										if (alPartNameList == null || alPartNameList.isEmpty()) {
											iCount = iTotalCount;
										} else {
											// 특정 Part Name들의 합에 의한 수만 더하기
											for (String sPartName : alPartNameList) {
												iCount += voNameGroupCountResult.getNameCount(sPartName);
											}
										}
	
										sOperation = iCount + operator + voNameGroupCondition.getQuantity();
										Object obj = engine.eval(sOperation);
										if (obj instanceof Boolean) {
											Boolean b = (Boolean) obj;
											if (!b.booleanValue()) {
												String tmp = "";
	
												if (voNameGroupCondition.getOperator().equals("<")) {
													tmp = "&lt;";
												} else if (voNameGroupCondition.getOperator().equals(">")) {
													tmp = "&gt;";
												} else {
													tmp = voNameGroupCondition.getOperator();
												}
	
												// 조건식을 모두 보여주기위함.
												if (voNameGroupCountResult.getReason() == null || voNameGroupCountResult.getReason().equals("")) {
													voNameGroupCountResult.setReason("[" + voNameGroupCondition.getProduct() + "] " + voNameGroupCondition.getCondition() + ".Quantity " + tmp + " " + voNameGroupCondition.getQuantity());
												} else {
													voNameGroupCountResult.setReason(voNameGroupCountResult.getReason() + "." + "[" + voNameGroupCondition.getProduct() + "] " + voNameGroupCondition.getCondition() + ".Quantity " + tmp + " " + voNameGroupCondition.getQuantity());
												}
	
												voNameGroupCountResult.setValid(false);
	
												// 오류처리된 원인 표기.
												voNameGroupCountResult.setReason("[" + voNameGroupCondition.getProduct() + "] " + voNameGroupCondition.getCondition() + ".Quantity " + tmp + " " + voNameGroupCondition.getQuantity());
											}
	
											isChecked = true;
										} else {
											throw new Exception("Not available Operation : " + sOperation);
										}
									}
								}
	
								iPreGroupNumber = iGroupNumber;
							}
	
							if (!isChecked) {
								if (iTotalCount != iDefaultQty) {
									voNameGroupCountResult.setValid(false);
									voNameGroupCountResult.setReason(iDefaultQty + " &lt;&gt; " + iTotalCount);
								}
							}
						}
					}
				}
			}

			// Error Data 수집
			Set<String> ksSpecNosForError = hmSpecNameGroupCountResult.keySet();
			Iterator<String> itSpecNosForError = ksSpecNosForError.iterator();			

			while (itSpecNosForError.hasNext()) {
				String sSpecNo = itSpecNosForError.next();

				HashMap<String, NameGroupCountResultVO> hmNameGroupCountResult = hmSpecNameGroupCountResult.get(sSpecNo);
				Set<String> ksGroupIDs = hmNameGroupCountResult.keySet();
				Iterator<String> itGroupIDs = ksGroupIDs.iterator();

				while (itGroupIDs.hasNext()) {
					String sGroupID = itGroupIDs.next();
					NameGroupCountResultVO voNameGroupCountResult = hmNameGroupCountResult.get(sGroupID);

					if (!voNameGroupCountResult.isValid() && isAssigned(sProduct, sGroupID)) {
						HashMap<String, Object> hmError = new HashMap<String, Object>();
						hmError.put("PRODUCT", sProduct);
						hmError.put("SPEC_NO", sSpecNo);
						hmError.put("GROUP_ID", sGroupID);
						hmError.put("RESULT", voNameGroupCountResult);
						hmError.put("CREATION_DATE", creationDate);
						hmError.put("REASON", voNameGroupCountResult.getReason());// Reason 추가

						alErrors.add(hmError);
					}
				}
			}
		}
		
		deletePngEpl(rowKey);

		// Result
		for (int inx = 0; inx < alErrors.size(); inx++) {
			HashMap<String, Object> hmError = alErrors.get(inx);
			String sProduct = hmError.get("PRODUCT").toString();
			String sSpecNo = hmError.get("SPEC_NO").toString();
			String sGroupID = hmError.get("GROUP_ID").toString();
			NameGroupCountResultVO voNameGroupCountResult = (NameGroupCountResultVO) hmError.get("RESULT");
			String sReason = voNameGroupCountResult.getReason();

			System.out.println(sProduct + " / " + sSpecNo + " / " + sGroupID + " / " + sReason);
		}
		
		/**
		 * 에러 결과 리스트 저장
		 */
		insertResultList(alErrors);

		long lEnd = logTime("END");
		System.out.println("총 소요시간 : " + (lEnd - lStart) + "ms");
	}
	
	private String getRowkey(){
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("PRODUCT", null);
		
		return (String)  dao.selectOne("com.symc.partnamegroup.getRowKey", hmParam);
	}
	
	private void set1LevelItemList(String product, String rowKey){
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("PRODUCT", product);
		hmParam.put("ROWKEY", rowKey);
		dao.insert("com.symc.partnamegroup.set1LevelItemList", hmParam);
	}
	
	private void deletePngEpl(String rowkey){
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("ROWKEY", rowkey);
		dao.delete("com.symc.partnamegroup.deletePngEpl", hmParam);
	}

	/**
	 * Product List 조회
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String> getProducts() {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("DATA", null);

		return (ArrayList<String>) dao.selectList("com.symc.partnamegroup.getProducts", hmParam);
	}

	/**
	 * Product 15일 생산계획 스펙 조회
	 * 
	 * @param sProduct
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, String>> getPlan15SpecList(String sProduct) {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("PRODUCT", sProduct);
		
		// FOR TEST
//		hmParam.put("SPEC_NO", "     UP5 SD3300");

		return (ArrayList<HashMap<String, String>>) dao.selectList("com.symc.partnamegroup.getPlan15SpecList", hmParam);
	}

	/**
	 * End Item Name List 조회
	 * 
	 * @param product
	 * @param puid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, Object>> getEndItemNameList(String sProduct, String sPUID, String rowkey) {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("PRODUCT", sProduct);
		hmParam.put("SPEC_TYPE", "PLAN SPEC");
		hmParam.put("PUID", sPUID);
		hmParam.put("ROWKEY", rowkey);

		return (ArrayList<HashMap<String, Object>>) dao.selectList("com.symc.partnamegroup.getEndItemNameList", hmParam);
	}

	/**
	 * 
	 * @param sGroupID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, Object>> getNameGroupMaster(String sProduct, String sGroupID) {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("PRODUCT", sProduct);
		hmParam.put("GROUP_ID", sGroupID);

		return (ArrayList<HashMap<String, Object>>) dao.selectList("com.symc.partnamegroup.getNameGroupMaster", hmParam);
	}

	/**
	 * Group ID Condition 조회
	 * 
	 * @param sGroupID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, Object>> getNameGroupCondition(String sGroupID) {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("GROUP_ID", sGroupID);

		return (ArrayList<HashMap<String, Object>>) dao.selectList("com.symc.partnamegroup.getNameGroupCondition", hmParam);
	}

	/**
	 * Spec 조회
	 * 
	 * @param sSpecNo
	 * @return
	 */
	private String getSpec(String sSpecNo) {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("SPEC_TYPE", "PLAN SPEC");
		hmParam.put("PUID", sSpecNo);

		return (String) dao.selectOne("com.symc.partnamegroup.getSpec", hmParam);
	}

	/**
	 * Assign 조회
	 * 
	 * @param sProduct
	 * @param sGroupID
	 * @return
	 */
	private boolean isAssigned(String sProduct, String sGroupID) {
		HashMap<String, String> hmParam = new HashMap<String, String>();
		hmParam.put("PRODUCT", sProduct);
		hmParam.put("GROUP_ID", sGroupID);

		Object result = dao.selectOne("com.symc.partnamegroup.isAssigned", hmParam);
		return result == null ? false : Boolean.parseBoolean(result.toString());
	}

	/**
	 * End Item Name List를 Name Group Count Result 형식으로 반환 - NameGroupCountResultVO : Group ID 별 정보를 저장하고 있는 객체
	 * 
	 * @param alEndItemNameLists
	 * @return
	 */
	private HashMap<String, NameGroupCountResultVO> getNameGroupCountResult(ArrayList<HashMap<String, Object>> alEndItemNameLists) {
		HashMap<String, NameGroupCountResultVO> hmNameGroupCountResult = new HashMap<String, NameGroupCountResultVO>();

		for (HashMap<String, Object> hmEndItemNameList : alEndItemNameLists) {
			String sGroupID = (String) hmEndItemNameList.get("GROUP_ID");
			String sChildName = (String) hmEndItemNameList.get("CHILD_NAME");
			BigDecimal bdNameCount = (BigDecimal) hmEndItemNameList.get("NAME_COUNT");

			NameGroupCountResultVO voNameGroupCountResult = hmNameGroupCountResult.get(sGroupID);
			if (voNameGroupCountResult == null) {
				voNameGroupCountResult = new NameGroupCountResultVO();
				hmNameGroupCountResult.put(sGroupID, voNameGroupCountResult);
			}

			voNameGroupCountResult.addNameCount(sChildName, bdNameCount.intValue());
		}

		return hmNameGroupCountResult;
	}

	/**
	 * 
	 * @param sCondition
	 * @param sSpecStr
	 * @return
	 * @throws Exception
	 */
	public boolean isAvailable(String sCondition, String sSpecStr) throws Exception {
		if (sCondition == null || sCondition.equals("")) {
			return true;
		}

		String sTemp = sCondition;

		// Pattern이 맞는 옵셥값을 찾고, 앞에 #을 붙인다.
		ArrayList<String> alFoundOpValues = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[a-zA-Z0-9]{4}");
		Matcher matcher = pattern.matcher(sTemp);
		while (matcher.find()) {
			if (!alFoundOpValues.contains(matcher.group())) {
				alFoundOpValues.add(matcher.group());
			}
		}

		if (alFoundOpValues.isEmpty()) {
			return true;
		}

		for (String sFoundOpValue : alFoundOpValues) {
			sTemp = sTemp.replaceAll(sFoundOpValue, "#" + sFoundOpValue);
		}

		sTemp = sTemp.replaceAll("NOT", "!");
		sTemp = sTemp.replaceAll("AND", "&&");
		sTemp = sTemp.replaceAll("OR", "||");
		for (String sFoundOpValue : alFoundOpValues) {
			sTemp = sTemp.replaceAll("#" + sFoundOpValue, "('##CONDITION##'.indexOf('" + sFoundOpValue + "') > -1)");
		}

		String sDefault = sTemp;
		String[] saSubConditions = sSpecStr.split("OR");
		for (String sSubCondition : saSubConditions) {
			sDefault = sTemp;
			sDefault = sDefault.replaceAll("##CONDITION##", sSubCondition);

			Object obj = engine.eval(sDefault);
			if (obj instanceof Boolean) {
				Boolean b = (Boolean) obj;
				if (b.booleanValue()) {
					return b.booleanValue();
				}
			} else {
				throw new Exception("Not available Option : " + sCondition);
			}
		}

		return false;
	}

	/**
	 * 에러 결과 리스트 저장
	 * 
	 * @param sContents
	 */
	private void insertResultList(ArrayList<HashMap<String, Object>> hmResult) {
		try {
			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			commonDao.insertList("com.symc.partnamegroup.insertWeeklyErrorReport", hmResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Logging Time
	 * 
	 * @param sDescription
	 * @return
	 */
	private long logTime(String sDescription) {
		long lTime = System.currentTimeMillis();

		System.out.println(sDescription + " : " + DateUtil.getTimeInMillisToDate(lTime));

		return lTime;
	}

	/**
	 * Spec 별 End Item List 조회
	 * 
	 * @author jclee
	 * 
	 */
	private class SpecCollector extends Thread {
		private String sProduct = "";
		private String sPUID = "";
		private HashMap<String, HashMap<String, NameGroupCountResultVO>> hmSpecNameGroupCountResult;
		private ArrayList<HashMap<String, Object>> alGroups;
		private String rowkey = "";

		public SpecCollector(String sProduct, String sPUID, ArrayList<HashMap<String, Object>> alGroups, HashMap<String, HashMap<String, NameGroupCountResultVO>> hmSpecNameGroupCountResult, String rowkey) {
			this.sProduct = sProduct;
			this.sPUID = sPUID;
			this.alGroups = alGroups;
			this.hmSpecNameGroupCountResult = hmSpecNameGroupCountResult;
			this.rowkey = rowkey;
		}

		public void run() {
			long lSpecStart = System.currentTimeMillis();

			ArrayList<HashMap<String, Object>> alEndItemNameLists = getEndItemNameList(sProduct, sPUID, rowkey);

			// Verify를 하기 위한 객체로 변경.
			// Spec No
			// ㄴGroup ID
			//   ㄴName Group Count Result VO
			HashMap<String, NameGroupCountResultVO> hmNameGroupCountResult = getNameGroupCountResult(alEndItemNameLists);

			// Product End Item 조회 시 나오지 않는 Group도 추가. 수량이 0으로 표시되는 항목 들.
			for (int inx = 0; inx < alGroups.size(); inx++) {
				HashMap<String, Object> hmGroup = alGroups.get(inx);

				String sGroupID = hmGroup.get("GROUP_ID").toString();

				if (hmNameGroupCountResult.containsKey(sGroupID)) {
					continue;
				}

				hmNameGroupCountResult.put(sGroupID, new NameGroupCountResultVO());
			}

			hmSpecNameGroupCountResult.put(sPUID, hmNameGroupCountResult);

			long lSpecEnd = System.currentTimeMillis();

			System.out.println("Get SPEC End " + sProduct + "_" + sPUID + " > " + (lSpecEnd - lSpecStart) + "ms");
		}
	}
}
