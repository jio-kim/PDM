package com.symc.work.service;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.util.ContextUtil;
import com.symc.common.util.DateUtil;

public class CheckWiringPartService
{
	private static final String WIRING_ASSY_MAIN = "WIRING ASSY-MAIN";
	private static final String WIRING_ASSY_ENGINE = "WIRING ASSY-ENGINE";
	private static final String WIRING_ASSY_FLOOR = "WIRING ASSY-FLOOR";
	private static final String WIRING_ASSY_FLOOR_LH = "WIRING ASSY-FLOOR-LH";
	private static final String WIRING_ASSY_FLOOR_RH = "WIRING ASSY-FLOOR-RH";
	private static final String WIRING_TYPE_1_MAIN = "1.MAIN";
	private static final String WIRING_TYPE_2_ENGINE = "2.ENGINE";
	private static final String WIRING_TYPE_3_FLOOR = "3.FLOOR";

	private static final String COL_PROJECT = "PROJECT";
	private static final String COL_PRODUCT = "PRODUCT";
	private static final String COL_SPEC = "SPEC";
	private static final String COL_WIRING_TYPE = "WIRING TYPE";
	private static final String COL_PART_ID = "PART ID";
	private static final String COL_DUPLICATION = "DUPLICATION";
	private static final String COL_STANDARD_OPTION = "STANDARD OPTION";
	private static final String COL_PART_OPTION = "PART OPTION";
	Logger logger = Logger.getLogger(CheckWiringPartService.class);

	/**
	 * Wiring part 체크.
	 * 매일 0시에 이전 하루동안 변경된 생산 스펙을 기준으로 존재하지 않는 wiring part를 찾아내 설계자들에게 메일로 알려준다.
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Object startService() throws Exception
	{
		logger.info("★★★★★★★★★★★★★★★★★★");
		logger.info("★ Wiring part 체크 프로그램 시작 ★");
		logger.info("★★★★★★★★★★★★★★★★★★");

		StringBuffer log = new StringBuffer();
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		try
		{
			//#########################################################
			//### 어제 기준으로 수정된 Spec 정보, product, option을 가져옴.
			//#########################################################
			List<HashMap<String, String>> updatedSpecList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.checkwiringpart.getUpdatedSpecList");
			if (updatedSpecList == null || updatedSpecList.isEmpty())
			{
				logger.info("## 업데이트된 생산 Spec이 존재하지 않음.");
				logger.info("★★★★★★★★★★★★★★★★★★");
				logger.info("★         프로그램 종료          ★");
				logger.info("★★★★★★★★★★★★★★★★★★");
				return log;
			}
			//product arrayList
			ArrayList<String> productList = new ArrayList<String>();
			//project-Spec Hash
			Hashtable<String, ArrayList<String>> productSpecHash = new Hashtable<String, ArrayList<String>>();
			//spec-option hash
			Hashtable<String, ArrayList<String>> specOptionHash = new Hashtable<String, ArrayList<String>>();
			//spec-project Hash
			Hashtable<String, String> specProjectHash = new Hashtable<String, String>();
			for (HashMap<String, String> rowMap : updatedSpecList)
			{
				String projectNo = rowMap.get(COL_PROJECT);
				String specNo = rowMap.get(COL_SPEC);
				String optionNo = rowMap.get(COL_STANDARD_OPTION);
				String productNo = rowMap.get(COL_PRODUCT);
				if (!productList.contains(productNo))
				{
					productList.add(productNo);
				}
				if (productSpecHash.containsKey(productNo))
				{
					ArrayList<String> specList = productSpecHash.get(productNo);
					if (!specList.contains(specNo))
					{
						specList.add(specNo);
					}
				} else
				{
					ArrayList<String> specList = new ArrayList<String>();
					specList.add(specNo);
					productSpecHash.put(productNo, specList);
				}
				if (specOptionHash.containsKey(specNo))
				{
					ArrayList<String> optionList = specOptionHash.get(specNo);
					if (!optionList.contains(optionNo))
					{
						optionList.add(optionNo);
					}
				} else
				{
					ArrayList<String> optionList = new ArrayList<String>();
					optionList.add(optionNo);
					specOptionHash.put(specNo, optionList);
				}
				if (!specProjectHash.containsKey(specNo))
				{
					specProjectHash.put(specNo, projectNo);
				}
			}
			logger.info("## Product 개수 = " + productList.size());
			logger.info("## Spec 개수 = " + specOptionHash.size());
			//###################################################
			//##### spec no 기준으로 하위 전개하면서 Wiring part 검색
			//###################################################
			int pcount = 1;
			ArrayList<HashMap<String, String>> reportList = new ArrayList<HashMap<String, String>>();
			for (String productNo : productList)
			{
				logger.info("■## product (" + pcount++ + "/" + productList.size() + ") = " + productNo);
				long b = new Date().getTime();
				//product 전개...
				DataSet ds = new DataSet();
				ds.put("PRODUCT_NO", productNo);
				List<HashMap<String, Object>> wiringBomList = (List<HashMap<String, Object>>) commonDao.selectList("com.symc.checkwiringpart.getWiringBOMList", ds);
				long a = new Date().getTime();
				logger.info(" ## product 전개 시간 = " + ((a - b) / 1000));
				//id 기준 이름을 저장해둠....
				Hashtable<String, String> idNameHash = new Hashtable<String, String>();
				//id 기준 option을 저장 해둠. option은 or 조건이 있기 때문에 여러개의 옵션 리스트를 관리하기 위해 이중 arrayList를 사용함.
				Hashtable<String, ArrayList<ArrayList<String>>> idOptionHash = new Hashtable<String, ArrayList<ArrayList<String>>>();
				if (wiringBomList != null && !wiringBomList.isEmpty())
				{
					//product 기준 wiring part data를 비교를 위해 저장해둔다.
					for (HashMap<String, Object> wiringBomMap : wiringBomList)
					{
						String childId = (String) wiringBomMap.get("CHILE_ID");
						String childName = (String) wiringBomMap.get("CHILD_NAME");
						if (!idNameHash.containsKey(childId))
						{
							idNameHash.put(childId, childName);
						}
						if (!idOptionHash.containsKey(childId))
						{
							idOptionHash.put(childId, convertOptionData(wiringBomMap.get(COL_PART_OPTION)));
						}
					}
				}
				logger.info("    ## Spec별 체크 시작.");
				//Spec 별로 wiring part 가 존재하는지 체크한다.
				//main 1개, engine 1개, floor 1개 또는 floor-lh, floor-rh 각 한쌍이 존재해야 함.
				int scount = 1;
				ArrayList<String> specList = productSpecHash.get(productNo);
				for (String specNo : specList)
				{
					String projectNo = specProjectHash.get(specNo);
					logger.info("    ●## Spec (" + scount++ + "/" + specList.size() + ") = " + specNo + " (" + projectNo + ")");
					ArrayList<String> specOptionList = specOptionHash.get(specNo);
					//Spec 번호에 대한 표준 Option 정보를 미리...가져온다.
					DataSet rds = new DataSet();
					rds.put("SPEC_NO", specNo);
					List<HashMap<String, String>> reportSpecList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.checkwiringpart.getReportSpecList", rds);
					HashMap<String, String> mainStandardOptionHash = new HashMap<String, String>();
					HashMap<String, String> engineStandardOptionHash = new HashMap<String, String>();
					HashMap<String, String> floorStandardOptionHash = new HashMap<String, String>();
					for (HashMap<String, String> tmpReportSpecHash : reportSpecList)
					{
						if (tmpReportSpecHash.get(COL_WIRING_TYPE).equals(WIRING_TYPE_1_MAIN))
						{
							mainStandardOptionHash = tmpReportSpecHash;
						} else if (tmpReportSpecHash.get(COL_WIRING_TYPE).equals(WIRING_TYPE_2_ENGINE))
						{
							engineStandardOptionHash = tmpReportSpecHash;
						} else if (tmpReportSpecHash.get(COL_WIRING_TYPE).equals(WIRING_TYPE_3_FLOOR))
						{
							floorStandardOptionHash = tmpReportSpecHash;
						}
					}
					//spec 옵션에 포함된 part들을 모두 찾아온다.
					ArrayList<String> existPartIDList = checkWiringPart(specOptionList, idOptionHash);
					if (existPartIDList.isEmpty())
					{
						//하나도 없는 경우... 기준 옵션 정보를 레포팅한다.
						reportList.addAll(reportSpecList);
						logger.info("     ## Wiring part가 하나도 없어서 report에 추가.");
					} else
					{
						//각 type 별로 분류 시킨다...
						ArrayList<String> mainPartList = new ArrayList<String>();
						ArrayList<String> enginePartList = new ArrayList<String>();
						ArrayList<String> floorPartList = new ArrayList<String>();
						ArrayList<String> floorLHPartList = new ArrayList<String>();
						ArrayList<String> floorRHPartList = new ArrayList<String>();
						for (String partId : existPartIDList)
						{
							String partName = idNameHash.get(partId);
							if (partName.equals(WIRING_ASSY_MAIN))
							{
								mainPartList.add(partId);
							} else if (partName.equals(WIRING_ASSY_ENGINE))
							{
								enginePartList.add(partId);
							} else if (partName.equals(WIRING_ASSY_FLOOR))
							{
								floorPartList.add(partId);
							} else if (partName.equals(WIRING_ASSY_FLOOR_LH))
							{
								floorLHPartList.add(partId);
							} else if (partName.equals(WIRING_ASSY_FLOOR_RH))
							{
								floorRHPartList.add(partId);
							}
						}
						logger.info("     #####################################################");
						logger.info("     ## MAIN     Wiring part = " + mainPartList.toString());
						logger.info("     ## ENGINE   Wiring part = " + enginePartList.toString());
						logger.info("     ## FLOOR    Wiring part = " + floorPartList.toString());
						logger.info("     ## FLOOR-LH Wiring part = " + floorLHPartList.toString());
						logger.info("     ## FLOOR-RH Wiring part = " + floorRHPartList.toString());
						logger.info("     #####################################################");
						//main wiring part는 하나만 존재해야 한다.
						//없거나 하나 이상이면 오류임.
						if (mainPartList.isEmpty())
						{
							//part가 하나도 없음.
							reportList.add(mainStandardOptionHash);
						} else if (mainPartList.size() > 1)
						{
							//part가 하나 이상 붙어 있다
							ArrayList<HashMap<String, String>> mainReportList = new ArrayList<HashMap<String, String>>();
							for (String partId : mainPartList)
							{
								HashMap<String, String> reportSpecHash = new HashMap<String, String>();
								reportSpecHash.put(COL_PROJECT, projectNo);
								reportSpecHash.put(COL_SPEC, specNo);
								reportSpecHash.put(COL_WIRING_TYPE, WIRING_TYPE_1_MAIN);
								reportSpecHash.put(COL_PART_ID, partId);
								reportSpecHash.put(COL_STANDARD_OPTION, mainStandardOptionHash.get(COL_STANDARD_OPTION));
								reportSpecHash.put(COL_PART_OPTION, getOptionToString(idOptionHash.get(partId)));
								mainReportList.add(reportSpecHash);
							}
							reportList.addAll(mainReportList);
						}
						//engine wiring part는 하나만 존재해야 한다.
						if (enginePartList.isEmpty())
						{
							//part가 하나도 없음.
							reportList.add(engineStandardOptionHash);
						} else if (enginePartList.size() > 1)
						{
							//part가 하나이상임.
							ArrayList<HashMap<String, String>> engineReportList = new ArrayList<HashMap<String, String>>();
							for (String partId : enginePartList)
							{
								HashMap<String, String> reportSpecHash = new HashMap<String, String>();
								reportSpecHash.put(COL_PROJECT, projectNo);
								reportSpecHash.put(COL_SPEC, specNo);
								reportSpecHash.put(COL_WIRING_TYPE, WIRING_TYPE_2_ENGINE);
								reportSpecHash.put(COL_PART_ID, partId);
								reportSpecHash.put(COL_STANDARD_OPTION, engineStandardOptionHash.get(COL_STANDARD_OPTION));
								reportSpecHash.put(COL_PART_OPTION, getOptionToString(idOptionHash.get(partId)));
								engineReportList.add(reportSpecHash);
							}
							reportList.addAll(engineReportList);
						}
						//floor part는 floor part 하나로 구성되거나, lh, rh 두개가 한쌍으로 구성되어야 함.
						if (floorPartList.isEmpty() && floorLHPartList.isEmpty() && floorRHPartList.isEmpty())
						{
							//세개다 하나도 없는 경우...
							reportList.add(floorStandardOptionHash);
						}
						//floor 가 하나이상이면 오류
						//floor가 하나인데 lh나 rh가 한개라도 있으면 오류. > floor 를 레포팅..
						if (floorPartList.size() > 1 || (floorPartList.size() == 1 && (!floorLHPartList.isEmpty() || !floorRHPartList.isEmpty())))
						{
							ArrayList<HashMap<String, String>> floorReportList = new ArrayList<HashMap<String, String>>();
							for (String partId : floorPartList)
							{
								HashMap<String, String> reportSpecHash = new HashMap<String, String>();
								reportSpecHash.put(COL_PROJECT, projectNo);
								reportSpecHash.put(COL_SPEC, specNo);
								reportSpecHash.put(COL_WIRING_TYPE, WIRING_TYPE_3_FLOOR);
								reportSpecHash.put(COL_PART_ID, partId);
								reportSpecHash.put(COL_STANDARD_OPTION, floorStandardOptionHash.get(COL_STANDARD_OPTION));
								reportSpecHash.put(COL_PART_OPTION, getOptionToString(idOptionHash.get(partId)));
								floorReportList.add(reportSpecHash);
							}
							reportList.addAll(floorReportList);
						}
						//floor lh가 하나 이상이면 오류
						//floor lh가 하나인데 rh가 하나가 아니면 오류.
						//floor lh가 하나인데 floor가 존재하면 오류.
						if (floorLHPartList.size() > 1 || (floorLHPartList.size() == 1 && (!floorPartList.isEmpty() || floorRHPartList.size() != 1)))
						{
							ArrayList<HashMap<String, String>> floorReportList = new ArrayList<HashMap<String, String>>();
							for (String partId : floorLHPartList)
							{
								HashMap<String, String> reportSpecHash = new HashMap<String, String>();
								reportSpecHash.put(COL_PROJECT, projectNo);
								reportSpecHash.put(COL_SPEC, specNo);
								reportSpecHash.put(COL_WIRING_TYPE, WIRING_TYPE_3_FLOOR);
								reportSpecHash.put(COL_PART_ID, partId);
								reportSpecHash.put(COL_STANDARD_OPTION, floorStandardOptionHash.get(COL_STANDARD_OPTION));
								reportSpecHash.put(COL_PART_OPTION, getOptionToString(idOptionHash.get(partId)));
								floorReportList.add(reportSpecHash);
							}
							reportList.addAll(floorReportList);
						}
						//floor rh가 하나 이상이면 오류
						//floor rh가 하나인데 lh가 하나가 아니면 오류
						//floor rh가 하나인데 floor가 존재하면 오류.
						if (floorRHPartList.size() > 1 || (floorRHPartList.size() == 1 && (!floorPartList.isEmpty() || floorLHPartList.size() != 1)))
						{
							ArrayList<HashMap<String, String>> floorReportList = new ArrayList<HashMap<String, String>>();
							for (String partId : floorRHPartList)
							{
								HashMap<String, String> reportSpecHash = new HashMap<String, String>();
								reportSpecHash.put(COL_PROJECT, projectNo);
								reportSpecHash.put(COL_SPEC, specNo);
								reportSpecHash.put(COL_WIRING_TYPE, WIRING_TYPE_3_FLOOR);
								reportSpecHash.put(COL_PART_ID, partId);
								reportSpecHash.put(COL_STANDARD_OPTION, floorStandardOptionHash.get(COL_STANDARD_OPTION));
								reportSpecHash.put(COL_PART_OPTION, getOptionToString(idOptionHash.get(partId)));
								floorReportList.add(reportSpecHash);
							}
							reportList.addAll(floorReportList);
						}
					}
				}
			}
			logger.info("## 비교 완료.#######################");
			// 결과 값이 하나도 없으면 정상임. > 종료/..
			if (reportList.isEmpty())
			{
				logger.info("## 모든 파트가 존재해서 정상... 완료.");
				logger.info("★★★★★★★★★★★★★★★★★★");
				logger.info("★         프로그램 종료          ★");
				logger.info("★★★★★★★★★★★★★★★★★★");
				return log;
			}
			//중복 데이터 체크.
			CheckDuplication(reportList);
			//########################
			//##### 레포트 메일 발송...
			//########################
			//메일 수신 리스트...
			logger.info("## 메일 발송.");
			List<HashMap<String, String>> mailUserList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.checkwiringpart.getMailList");
			String mailUserString = mailUserList.get(0).get("MAIL_LIST");
			EnvService envService = (EnvService) ContextUtil.getBean("envService");
			String plm_admin = envService.getTCWebEnv().get("PLM_ADMIN");
			mailUserString += "," + plm_admin;
			//레포트 내용으로 엑셀 생성.
			File reportFile = excelExport(reportList);
			//메일 본문 작성
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			String body = "<PRE>" + "[체크 기준일 : " + cal.get(Calendar.YEAR) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DAY_OF_MONTH) + "일] <BR><BR>";
			body += "생성(수정)된 생산 Spec 에 대해 누락된 Wiring Part들이 존재합니다." + "<BR><BR>";
			body += " Report 파일을 확인하시고 조치를 취하시기 바랍니다.<BR><BR>";
			body += " -Report File : " + "<a href='" + reportFile.getAbsolutePath() + "'>" + reportFile.getName() + "</a>" + "<BR>";
			body += "</PRE>";
			//메일 발송..
			DataSet ds = new DataSet();
			ds.put("the_sysid", "NPLM");
			ds.put("the_sabun", "NPLM");
			ds.put("the_title", "[PLM] Missing Wiring Part Report");
			ds.put("the_remark", body);
			ds.put("the_tsabun", mailUserString);
			logger.info(body);
			//메일 발송
			TcCommonDao.getTcCommonDao().update("com.symc.interface.sendMailEai", ds);
			logger.info("## 수신자 : " + mailUserString);
			logger.info("## 파일 : " + reportFile.getAbsolutePath());
			logger.info("★★★★★★★★★★★★★★★★★★");
			logger.info("★         프로그램 종료          ★");
			logger.info("★★★★★★★★★★★★★★★★★★");
		} catch (Exception e)
		{
			throw e;
		}
		return log;
	}

	/**
	 * 중복 체크를 수행한다.
	 * 중복 체크란, 하나의 product 아래에 동일한 옵션값을 가진 part가 두개 이상 존재하면 안되기 때문에 이를 표시 해준다.
	 * 기준은 프로젝트,wiring type, standard option이 같으면 중복이다.
	 * 중복된 데이터는 duplication column에 같은것들끼리 같은 숫자로 표시하도록 한다.
	 * 
	 * @param reportList
	 */
	private void CheckDuplication(ArrayList<HashMap<String, String>> reportList)
	{
		int duplicationCount = 0;
		for (HashMap<String, String> reportHash : reportList)
		{
			if (reportHash.get(COL_DUPLICATION) != null)
			{
				continue;
			}
			boolean isSame = false;
			String projectCode = reportHash.get(COL_PROJECT);
			String wiringType = reportHash.get(COL_WIRING_TYPE);
			String option = reportHash.get(COL_STANDARD_OPTION);
			String key = projectCode + wiringType + option;
			for (HashMap<String, String> subReportHash : reportList)
			{
				if (subReportHash.get(COL_DUPLICATION) != null)
				{
					continue;
				}
				if (reportHash.equals(subReportHash))
				{
					continue;
				}
				String subProjectCode = subReportHash.get(COL_PROJECT);
				String subWiringType = subReportHash.get(COL_WIRING_TYPE);
				String subOption = subReportHash.get(COL_STANDARD_OPTION);
				String subKey = subProjectCode + subWiringType + subOption;
				if (key.equals(subKey))
				{
					if (!isSame)
					{
						isSame = true;
						duplicationCount++;
					}
					subReportHash.put(COL_DUPLICATION, duplicationCount + "");
				}
			}
			if (isSame)
			{
				reportHash.put(COL_DUPLICATION, duplicationCount + "");
			}
		}
	}

	/**
	 * 이중 arraylist를 하나의 string 옵션으로 변경함.
	 * arraylist들끼리는 or 조건이고 arraylist 안에 string 값 끼리는 and 조건이다.
	 * 
	 * @param optionOrList
	 * @return
	 */
	private String getOptionToString(ArrayList<ArrayList<String>> optionOrList)
	{
		String optionNo = "";
		for (int i = 0; i < optionOrList.size(); i++)
		{
			ArrayList<String> optionAndList = optionOrList.get(i);
			for (int j = 0; j < optionAndList.size(); j++)
			{
				String option = optionAndList.get(j);
				optionNo += (j == 0 ? (i == 0 ? "" : " OR ") : " AND ") + option;
			}
		}
		return optionNo;
	}

	/**
	 * spec 하나에 해당하는 option 리스트에 wiring part의 option이 모두 포함되어 있는지 체크하고...
	 * 모두 포함되어 있으면 그 아이디 리스트를 리턴해준다.
	 * 딱, 3개가 나오면 정상임.(lh,rh의 경우 4개)
	 * 
	 * @param specOptionList
	 * @param idOptionHash
	 * @return
	 */
	private ArrayList<String> checkWiringPart(ArrayList<String> specOptionList, Hashtable<String, ArrayList<ArrayList<String>>> idOptionHash)
	{
		ArrayList<String> findWiringPartIDList = new ArrayList<String>();

		Enumeration<String> enum1 = idOptionHash.keys();
		while (enum1.hasMoreElements())
		{
			String itemId = enum1.nextElement();
			ArrayList<ArrayList<String>> orWiringOptionList = idOptionHash.get(itemId);
			for (ArrayList<String> wiringOptionList : orWiringOptionList)
			{
				//스펙 option 전체에 wiring option이 포함되어 있으면 존재하는 경우임.
				boolean isOptionMatch = specOptionList.containsAll(wiringOptionList);
				if (isOptionMatch)
				{
					findWiringPartIDList.add(itemId);
					break;
				}
			}
		}
		return findWiringPartIDList;
	}

	/**
	 * 팀센터에 저장 된 option 값을 가져와서 이중 array에 넣는다.
	 * 팀센터 DB에는 CLOB로 저장되어 있다.
	 * 전체String에서 or를 기준으로 분리한다.
	 * 그다음, and를 기준으로 나눈다.
	 * 그다음 " 따옴표 기준으로 분리한 후 두번째 값이 옵션이다.
	 * 
	 * @param optionData
	 * @return
	 * @throws Exception
	 */
	private ArrayList<ArrayList<String>> convertOptionData(Object optionData) throws Exception
	{
		ArrayList<ArrayList<String>> optionList = new ArrayList<ArrayList<String>>();
		if (optionData == null)
		{
			return optionList;
		}
		Clob clob = (Clob) optionData;
		String optionClob = "";
		BufferedReader br = new BufferedReader(clob.getCharacterStream());
		while (true)
		{
			String s = br.readLine();
			if (s == null || s.isEmpty())
			{
				break;
			}
			optionClob += s;
		}
		br.close();
		String[] orSplits = optionClob.split("or");
		for (String orSplit : orSplits)
		{
			ArrayList<String> andOptionList = new ArrayList<String>();
			String[] andSplits = orSplit.split("and");
			for (String andSplit : andSplits)
			{
				//andSplit 은 이러한 형태를 가지게 됨... 여기서 따옴표로 구분하면 두번째가 옵션 값이다.... F80CHA1923:A40 = "A40E"  
				String[] commaSplit = andSplit.split("\"");
				String option = commaSplit[1];
				andOptionList.add(option);
			}
			optionList.add(andOptionList);
		}
		return optionList;
	}

	/**
	 * 설계자에게 배포할 엑셀 데이터를 만든다.
	 * 
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
	public static File excelExport(List<HashMap<String, String>> dataList) throws Exception
	{
		String title = "Missing Wiring Part Report";
		String sheetName = "Report";
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String comments = "[체크 기준일 : " + cal.get(Calendar.YEAR) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DAY_OF_MONTH) + "일] " + "   ※ 아래 리스트를 확인하시고 누락된 Wiring Part를 구성하십시오.";
		ArrayList<String> columnList = new ArrayList<String>();
		columnList.add(COL_PROJECT);
		columnList.add(COL_SPEC);
		columnList.add(COL_WIRING_TYPE);
		columnList.add(COL_PART_ID);
		columnList.add(COL_DUPLICATION);
		columnList.add(COL_STANDARD_OPTION);
		columnList.add(COL_PART_OPTION);

		int tableHeaderRowIndex = 3;
		int tableHeaderColumnIndex = 1;

		XSSFWorkbook wb = null;
		XSSFSheet sheet = null;
		XSSFCellStyle tableHeaderStyle = null;
		XSSFCellStyle tableCellStyle = null;
		XSSFCellStyle tableCellColorStyle = null;

		boolean isColor = true;
		for (int row = 0; row < dataList.size(); row++)
		{
			if (row == 0)
			{
				wb = new XSSFWorkbook();
				sheet = (XSSFSheet) wb.createSheet(sheetName);

				XSSFCellStyle titleStyle = (XSSFCellStyle) wb.createCellStyle();
				titleStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				titleStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
				titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
				XSSFFont titleFont = (XSSFFont) wb.createFont();
				titleFont.setBold(true);
				titleFont.setFontHeight(20D);
				titleStyle.setFont(titleFont);

				tableHeaderStyle = (XSSFCellStyle) wb.createCellStyle();
				tableHeaderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
				tableHeaderStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setFillForegroundColor(new XSSFColor(new Color(252, 213, 180)));
				tableHeaderStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

				tableCellStyle = (XSSFCellStyle) wb.createCellStyle();
				tableCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				tableCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

				tableCellColorStyle = (XSSFCellStyle) wb.createCellStyle();
				tableCellColorStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				tableCellColorStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableCellColorStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellColorStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableCellColorStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellColorStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableCellColorStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellColorStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableCellColorStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellColorStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
				tableCellColorStyle.setFillForegroundColor(new XSSFColor(new Color(224, 255, 255)));
				tableCellColorStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

				//title
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, columnList.size()));
				Row titleRow = sheet.createRow(1);
				Cell titleCell = titleRow.createCell(1);
				titleCell.setCellValue(title);
				titleCell.setCellStyle(titleStyle);

				//comments
				Row titleRow2 = sheet.createRow(tableHeaderRowIndex - 1);
				titleRow2.createCell(1).setCellValue(comments);

				//header
				int excelColumn = tableHeaderColumnIndex;
				Row xssfRow = sheet.createRow(row + tableHeaderRowIndex);
				for (int i = 0; i < columnList.size(); i++)
				{
					String headerString = columnList.get(i);
					Cell cell = xssfRow.createCell(excelColumn);
					cell.setCellValue(headerString);
					cell.setCellStyle(tableHeaderStyle);
					excelColumn++;
				}
			}

			//data
			String ccellString = dataList.get(row).get(COL_SPEC);
			String bcellString = sheet.getRow(row + tableHeaderRowIndex).getCell(2).getStringCellValue();
			if (!ccellString.equals(bcellString))
			{
				isColor = !isColor;
			}
			XSSFCellStyle xtableCellStyle = tableCellStyle;
			if (isColor)
			{
				xtableCellStyle = tableCellColorStyle;
			} else
			{
				xtableCellStyle = tableCellStyle;
			}
			int excelColumn = tableHeaderColumnIndex;
			Row xssfRow = sheet.createRow(row + tableHeaderRowIndex + 1);
			for (int i = 0; i < columnList.size(); i++)
			{
				String headerString = columnList.get(i);
				String cellString = dataList.get(row).get(headerString);
				if (cellString == null)
				{
					cellString = "";
				}
				Cell cell = xssfRow.createCell(excelColumn);
				cell.setCellValue(cellString);
				cell.setCellStyle(xtableCellStyle);
				sheet.autoSizeColumn(excelColumn);
				excelColumn++;
			}
		}
		sheet.setColumnWidth(tableHeaderColumnIndex - 1, 1 * 256);
		sheet.setColumnWidth(tableHeaderColumnIndex, 8 * 256);
		//EAI를 이용한 메일 보내기는 첨부 파일을 포함 시키는게 불가능 함.
		//그래서 별도 서버의 공간에 파일을 넣어두고 메일 내용에 링크를 만들어서 보냄.
		//경로는 env.properties에 지정되어 있음.
		Properties contextProperties = (Properties) ContextUtil.getBean("contextProperties");
		String filePath = contextProperties.getProperty("wiringReportFilePath");
		File folder = new File(filePath);
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		String fullFileName = filePath + File.separator + "WiringReport_" + DateUtil.getLogFileName("xlsx");
		File wiringFile = new File(fullFileName);
		FileOutputStream fileOut = new FileOutputStream(wiringFile);
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
		return wiringFile;
	}
}
