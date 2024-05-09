package com.kgm.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.CCNInfoMapper;

/**
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 * [20160715][ymjang] CCN EPL 보정 기능 신규 추가
 * [20160718] IF CCN Master 정보 생성 로직 개선 --> Stored Procedure 로 이관함.
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 * [20170622][ljg]whereused() API방식에서 DB쿼리로 변경 --> BOM을 역전개 하여 부모의 리비전이 여러개일 경우 해당 부모의 최종 리비전만 가져옴
 */
public class CCNInfoDao extends AbstractDao {

	public CCNInfoDao() {

	}
	public ArrayList<HashMap<String, Object>> selectMasterSystemCode(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.selectMasterSystemCode(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}

	public ArrayList<HashMap<String, Object>> selectMasterInfoList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.selectMasterInfoList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}

	public ArrayList<HashMap<String, Object>> selectMasterUsageInfoList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.selectMasterUsageInfoList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}

	public ArrayList<HashMap<String, Object>> selectOSpecHeaderInfoList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.selectOSpecHeaderInfoList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}

	/**
	 * SYMC 인트라넷을 통한 메일 발송
	 * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
	 * @return
	 */
	public boolean sendMail(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			mapper.sendMail(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * SYMC 인트라넷을 통한 메일 발송 (through EAI)
	 * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
	 * @return
	 */
	public boolean sendMailEai(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);

			// E-Mail 발송 내용 Logging
			// 			Logger logger = Logger.getLogger(CCNInfoDao.class);
			// 			logger.info("\n############ SEND MAIL START ############");
			// 			logger.info("\n# the_sysid : " + ds.get("the_sysid"));
			// 			logger.info("\n# the_sabun : " + ds.get("the_sabun"));
			// 			logger.info("\n# the_title : " + ds.get("the_title"));
			// 			logger.info("\n# the_remark : " + ds.get("the_remark"));
			// 			logger.info("\n# the_tsabun : " + ds.get("the_tsabun"));
			// 			logger.info("\n############ SEND MAIL END   ############");

			mapper.sendMailEai(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public boolean insertCCNMaster(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			mapper.insertCCNMaster(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			return false;
		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public boolean insertIfCCNMaster(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			mapper.insertIfCCNMaster(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			return false;
		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public boolean deleteCCNMaster(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			mapper.deleteCCNMaster(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			return false;
		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * Master 정보를 Insert 한다 
	 * 1. 기존 EPL LIST_ID 를 가져온다
	 * 2. 가져온 LIST_ID 로 USAGE 테이블에 데이터 삭제
	 * 3. EPL 마스터 테이블 값 삭제
	 * 4. EPL 마스터 정보 Insert 
	 * 5. EPL Usage 정보 Insert
	 * 
	 * @param arrayDataSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean insertEPLList(DataSet dataSet) throws Exception {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ArrayList<HashMap<String, Object>> arrayDataSet = (ArrayList<HashMap<String, Object>>) dataSet.get("EPL_LIST");
			String ccnId = (String) dataSet.get("CCN_NO");

			// 1. 해당 CCN 의 USAGE 정보 삭제
			mapper.deleteEPLUsageInfoAll(ccnId);            
			/* [20160718][ymjang] USAGE 삭제 로직 개선
            // 1. 기존 EPL LIST_ID 를 가져온다
            ArrayList<HashMap<String, Object>> ccnInfoList = mapper.selectUsageListId(ccnId);
            // 2. 가져온 LIST_ID 로 USAGE 테이블에 데이터 삭제
            if (ccnInfoList.size() > 0) {
                for (HashMap<String, Object> ccnInfo : ccnInfoList) {
                    mapper.deleteEPLUsageInfo(ccnInfo.get("LIST_ID").toString());
                }
            }
			 */

			// 2. EPL 마스터 테이블 값 삭제
			mapper.deleteEPLList(ccnId);

			// 3. CCN MASTER LIST 정보 Insert 
			for (HashMap<String, Object> dataMap : arrayDataSet) {
				String listId = mapper.selectMasterListKey();
				dataMap.put("LIST_ID", listId);

				mapper.insertEPLList(dataMap);

				// 4. CCN MASTER LIST USAGE 정보 Insert (OLD)
				ArrayList<HashMap<String, Object>> oldUsageList = (ArrayList<HashMap<String, Object>>) dataMap.get("OLD_USAGE_LIST");
				if (null != oldUsageList && oldUsageList.size() > 0) {
					for (HashMap<String, Object> usageInfo : oldUsageList) {
						usageInfo.put("LIST_ID", listId);
						mapper.insertEPLUsageInfo(usageInfo);
					}
				}

				// 5. CCN MASTER LIST USAGE 정보 Insert (NEW)
				ArrayList<HashMap<String, Object>> newUsageList = (ArrayList<HashMap<String, Object>>) dataMap.get("NEW_USAGE_LIST");
				if (null != newUsageList && newUsageList.size() > 0) {
					for (HashMap<String, Object> usageInfo : newUsageList) {
						usageInfo.put("LIST_ID", listId);
						mapper.insertEPLUsageInfo(usageInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			throw e;

		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean insertEPLList_(DataSet dataSet) throws Exception {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ArrayList<HashMap<String, Object>> arrayDataSet = (ArrayList<HashMap<String, Object>>) dataSet.get("EPL_LIST");
			String ccnId = (String) dataSet.get("CCN_NO");

			// 1. 해당 CCN 의 USAGE 정보 삭제
			mapper.deleteEPLUsageInfoAll(ccnId);            
			/* [20160718][ymjang] USAGE 삭제 로직 개선
            // 1. 기존 EPL LIST_ID 를 가져온다
            ArrayList<HashMap<String, Object>> ccnInfoList = mapper.selectUsageListId(ccnId);
            // 2. 가져온 LIST_ID 로 USAGE 테이블에 데이터 삭제
            if (ccnInfoList.size() > 0) {
                for (HashMap<String, Object> ccnInfo : ccnInfoList) {
                    mapper.deleteEPLUsageInfo(ccnInfo.get("LIST_ID").toString());
                }
            }
			 */

			// 2. EPL 마스터 테이블 값 삭제
			mapper.deleteEPLList(ccnId);

			// 3. CCN MASTER LIST 정보 Insert 
			for (HashMap<String, Object> dataMap : arrayDataSet) {
				String listId = mapper.selectMasterListKey();
				dataMap.put("LIST_ID", listId);

				mapper.insertEPLList_(dataMap);

				// 4. CCN MASTER LIST USAGE 정보 Insert (OLD)
				ArrayList<HashMap<String, Object>> oldUsageList = (ArrayList<HashMap<String, Object>>) dataMap.get("OLD_USAGE_LIST");
				if (null != oldUsageList && oldUsageList.size() > 0) {
					for (HashMap<String, Object> usageInfo : oldUsageList) {
						usageInfo.put("LIST_ID", listId);
						mapper.insertEPLUsageInfo(usageInfo);
					}
				}

				// 5. CCN MASTER LIST USAGE 정보 Insert (NEW)
				ArrayList<HashMap<String, Object>> newUsageList = (ArrayList<HashMap<String, Object>>) dataMap.get("NEW_USAGE_LIST");
				if (null != newUsageList && newUsageList.size() > 0) {
					for (HashMap<String, Object> usageInfo : newUsageList) {
						usageInfo.put("LIST_ID", listId);
						mapper.insertEPLUsageInfo(usageInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			throw e;

		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean insertUsage(DataSet dataSet) {
		SqlSession session = getSqlSession();
		CCNInfoMapper mapper = session.getMapper(CCNInfoMapper.class);        
		try {
			session.getConnection().setAutoCommit(false);

			HashMap<String, Object> arrayDataSet = (HashMap<String, Object>) dataSet.get("EPL_LIST");

			ArrayList<HashMap<String, Object>> oldUsageList = (ArrayList<HashMap<String, Object>>) arrayDataSet.get("OLD_USAGE_LIST");
			String sListID = arrayDataSet.get("LIST_ID").toString();

			if (null != oldUsageList && oldUsageList.size() > 0) {
				for (HashMap<String, Object> usageInfo : oldUsageList) {
					usageInfo.put("LIST_ID", sListID);
					mapper.insertEPLUsageInfo(usageInfo);
				}
			}

			ArrayList<HashMap<String, Object>> newUsageList = (ArrayList<HashMap<String, Object>>) arrayDataSet.get("NEW_USAGE_LIST");
			if (null != newUsageList && newUsageList.size() > 0) {
				for (HashMap<String, Object> usageInfo : newUsageList) {
					usageInfo.put("LIST_ID", sListID);
					mapper.insertEPLUsageInfo(usageInfo);
				}
			}

			session.getConnection().commit();
		} catch(Exception e) {
			try {
				session.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return Boolean.TRUE;
	}

	/**
	 * Usage 정보를 Insert 한다 
	 * 1. 기존 EPL LIST_ID 로 USAGE 테이블에 데이터 삭제
	 * 2. EPL Usage 정보 Insert
	 * 
	 * @param arrayDataSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean insertEPLListDiff(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			HashMap<String, HashMap<String, Object>> arrayDataSet = (HashMap<String, HashMap<String, Object>>) dataSet.get("EPL_LIST");
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> usageDataSet = (HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>>) dataSet.get("USAGE_LIST");
			String ccnId = (String) dataSet.get("CCN_NO");

			//            deleteEPLList(ccnId);
			// 1. 기존 EPL LIST_ID 를 가져온다
			ArrayList<HashMap<String, Object>> ccnInfoList = mapper.selectUsageListId(ccnId);
			// 2. 가져온 LIST_ID 로 USAGE 테이블에 데이터 삭제
			if (ccnInfoList.size() > 0) {
				for (HashMap<String, Object> ccnInfo : ccnInfoList) {
					mapper.deleteEPLUsageInfo(ccnInfo.get("LIST_ID").toString());
				}
			}
			// 3. EPL 마스터 테이블 값 삭제
			mapper.deleteEPLList(ccnId);

			// 4. EPL 마스터 정보 Insert 
			//            ArrayList<String> listIds = new ArrayList<String>(); 
			for (String partKey : arrayDataSet.keySet()) {
				//            for (HashMap<String, Object> dataMap : arrayDataSet) {
				HashMap<String, Object> dataMap = arrayDataSet.get(partKey);

				String listId = mapper.selectMasterListKey();
				dataMap.put("LIST_ID", listId);
				mapper.insertEPLList(dataMap);
				HashMap<String, HashMap<String, HashMap<String, Object>>> usageList = (HashMap<String, HashMap<String, HashMap<String, Object>>>) usageDataSet.get(partKey);
				if (null != usageList && usageList.size() > 0) {
					for (String sosKey : usageList.keySet())
					{
						for (String oldNewKey : usageList.get(sosKey).keySet())
						{
							HashMap<String, Object> usageInfo = usageList.get(sosKey).get(oldNewKey);
							usageInfo.put("LIST_ID", listId);
							mapper.insertEPLUsageInfo(usageInfo);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}


	/**
	 * Master 정보를 Insert 한다  (IF 용)
	 * 1. EPL 마스터 정보 Insert 
	 * 2. EPL Usage 정보 Insert
	 * 
	 * @param arrayDataSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean insertIfEPLList(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ArrayList<HashMap<String, Object>> arrayDataSet = (ArrayList<HashMap<String, Object>>) dataSet.get("EPL_LIST");
			String ccnId = (String) dataSet.get("CCN_NO");

			/**
			 * [20160610]중복 오류로 인해서 EPL Usage, EPL Master 데이터 삭제
			 */
			// EPL Usage 데이터 삭제 
			mapper.deleteIFEPLUsageInfo(ccnId);
			// EPL Master 데이터 삭제
			mapper.deleteIFEPLList(ccnId);

			// 1. EPL 마스터 정보 Insert (IF 용)
			for (HashMap<String, Object> dataMap : arrayDataSet) {
				String listId = mapper.selectMasterListKey();
				dataMap.put("LIST_ID", listId);
				mapper.insertIfEPLList(dataMap);
				ArrayList<HashMap<String, Object>> oldUsageList = (ArrayList<HashMap<String, Object>>) dataMap.get("OLD_USAGE_LIST");
				if (null != oldUsageList && oldUsageList.size() > 0) {
					for (HashMap<String, Object> usageInfo : oldUsageList) {
						usageInfo.put("LIST_ID", listId);
						mapper.insertIfEPLUsageInfo(usageInfo);
					}
				}
				ArrayList<HashMap<String, Object>> newUsageList = (ArrayList<HashMap<String, Object>>) dataMap.get("NEW_USAGE_LIST");
				if (null != newUsageList && newUsageList.size() > 0) {
					for (HashMap<String, Object> usageInfo : newUsageList) {
						usageInfo.put("LIST_ID", listId);
						mapper.insertIfEPLUsageInfo(usageInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * Master 정보를 Insert 한다  (IF 용)
	 * 1. EPL 마스터 정보 Insert 
	 * 2. EPL Usage 정보 Insert
	 * 
	 * @param arrayDataSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean insertIfEPLListDiff(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			
			String ccnId = (String) dataSet.get("CCN_NO");
			// EPL Usage 데이터 삭제 
			mapper.deleteIFEPLUsageInfo(ccnId);
			// EPL Master 데이터 삭제
			mapper.deleteIFEPLList(ccnId);
						
						
						
			HashMap<String, HashMap<String, Object>> arrayDataSet = (HashMap<String, HashMap<String, Object>>) dataSet.get("EPL_LIST");
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> usageDataSet = (HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>>) dataSet.get("USAGE_LIST");
			//            String ccnId = (String) dataSet.get("CCN_NO");

			// 1. EPL 마스터 정보 Insert (IF 용)
			for (String partKey : arrayDataSet.keySet()) {
				HashMap<String, Object> dataMap = arrayDataSet.get(partKey);

				String listId = mapper.selectMasterListKey();
				dataMap.put("LIST_ID", listId);
				mapper.insertIfEPLList(dataMap);

				//                ArrayList<HashMap<String, Object>> usageList = (ArrayList<HashMap<String, Object>>) dataMap.get("USAGE_LIST");
				HashMap<String, HashMap<String, HashMap<String, Object>>> usageList = (HashMap<String, HashMap<String, HashMap<String, Object>>>) usageDataSet.get(partKey);
				if (null != usageList && usageList.size() > 0) {
					for (String sosKey : usageList.keySet())
					{
						for (String oldNewKey : usageList.get(sosKey).keySet())
						{
							HashMap<String, Object> usageInfo = usageList.get(sosKey).get(oldNewKey);

							usageInfo.put("LIST_ID", listId);
							mapper.insertIfEPLUsageInfo(usageInfo);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * [20160715][ymjang] CCN EPL 보정 기능 신규 추가
	 * @param dataSet
	 * @return
	 */
	@SuppressWarnings("unchecked")    
	public boolean correctCCNEPL(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			mapper.correctCCNEPL(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public Object getDwgDeployableDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.CCNInfoMapper.selectDwgDeployableDate", ds);

			return obj;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	} 

	public Object getDcsReleasedDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.CCNInfoMapper.selectDcsReleasedDate", ds);

			return obj;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList<HashMap<String, Object>> selectCCNValidateMessage(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.selectCCNValidateMessage(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}

	/**
	 *  [20160610] IF CCN Master 정보 삭제
	 * @param ccnId
	 * @return
	 */
	public boolean deleteIFCCNMaster(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			mapper.deleteIFCCNMaster(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			return false;
		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * [20160718] IF CCN Master 정보 생성 로직 개선 --> Stored Procedure 로 이관함.
	 * @param dataSet
	 * @return
	 * @throws Exception
	 */
	public Boolean createIfCCN(DataSet dataSet) throws Exception
	{

		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);

			CCNInfoMapper mapper = session.getMapper(CCNInfoMapper.class);        
			mapper.createIfCCN(dataSet);

			session.getConnection().commit();
		}catch(Exception e){
			e.printStackTrace();

			try {
				session.getConnection().rollback();	
			} catch (Exception e2) {
				e.printStackTrace();
			}

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			return Boolean.FALSE;
		}finally{
			sqlSessionClose();
		}
		return Boolean.TRUE;

	}

	/**
	 * CCN에 Reference 된 Pre BOM 파트 리스트를  가져옴 
	 * @param dataSet
	 * @return
	 */
	public ArrayList<HashMap<String, String>> selectPreBomPartsReferencedFromCCN(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, String>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.selectPreBomPartsReferencedFromCCN(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}


	/**
	 * CCN EPL 리스트를 삭제함
	 * @param ccnId
	 * @return
	 */
	public boolean deleteEPLAllList(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			String ccnId = (String) dataSet.get("CCN_NO");
			// 1. 해당 CCN 의 USAGE 정보 삭제
			mapper.deleteEPLUsageInfoAll(ccnId);       
			// 2. EPL 마스터 테이블 값 삭제
			mapper.deleteEPLList(ccnId);

		} catch (Exception e) {
			e.printStackTrace();

			LogUtil.error(e.getMessage());

			return false;
		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * BOM을 역전개 하여 부모의 리비전이 여러개일 경우 해당 부모의 최종 리비전만 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 22.
	 * @param dataSet
	 * @return
	 */
	public ArrayList<String> whereUsed(DataSet dataSet) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList<?> list = (ArrayList<?>)session.selectList("com.kgm.mapper.CCNInfoMapper.whereUsed", dataSet);

			return (ArrayList<String>)list;
		}catch(Exception e){
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public String getParent4Digit(DataSet dataSet) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String str = session.selectOne("com.kgm.mapper.CCNInfoMapper.getParent4Digit", dataSet);

			return str;
		}catch(Exception e){
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public String getParent4DigitReleased(DataSet dataSet) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String str = session.selectOne("com.kgm.mapper.CCNInfoMapper.getParent4DigitReleased", dataSet);

			return str;
		}catch(Exception e){
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public String getPreRevisionPuid(DataSet dataSet) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String str = session.selectOne("com.kgm.mapper.CCNInfoMapper.getPreRevisionPuid", dataSet);

			return str;
		}catch(Exception e){
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList<HashMap<String, Object>> arrParentEPLData(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.arrParentEPLData(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}
	
	public ArrayList<HashMap<String, Object>> arrParentEPLDataOld(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.arrParentEPLDataOld(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}
	
	public ArrayList<HashMap<String, Object>> arrParentEPLDataNew(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.arrParentEPLDataNew(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}
	
	public ArrayList<HashMap<String, Object>> getChildBOMPro(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> ccnInfoList = null;

		try {
			sqlSession = getSqlSession();
			CCNInfoMapper mapper = sqlSession.getMapper(CCNInfoMapper.class);
			ccnInfoList = mapper.getChildBOMPro(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);

		} finally {
			sqlSessionClose();
		}

		return ccnInfoList;
	}
}
