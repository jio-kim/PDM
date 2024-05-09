package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;

/**
 * [SR170707-024][ljg] Product의 OSI No를 가져 오는 쿼리 추가
 * [SR170707-024][ljg] Product의 Variant 가져 오는 쿼리 추가
 */
public class MasterListDao extends AbstractDao {

	public ArrayList getWorkingCCN(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList workingCCNs = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectWorkingCCN", ds);

			return workingCCNs;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList getPart(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList parts = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectPart", ds);

			return parts;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList getStoredOptionSet(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList optionSet = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectStoredOptionSet", ds);

			return optionSet;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList getEssentialName(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectEssentialName", ds);

			return list;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public Object getSysGuid(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectSysGuid", ds);

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

	public Object getDwgDeployableDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectDwgDeployableDate", ds);

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

	public Object getBVRModifyDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectBVRModifyDate", ds);

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

	public Object getExistPart(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectExistPart", ds);

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

	/**
	 * 최신 Total Weight Master List 대상 정보를 가져옴
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> getLatestWMLMTargetData(DataSet ds) throws Exception {
		HashMap<String, String> result = null;        
		try{            
			SqlSession session = getSqlSession();
			result = session.selectOne("com.kgm.mapper.masterlist.getLatestWMLMTargetData", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * 최신 Total Weight Master List BOM 정보 조회
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getWeightMasterDataList(DataSet ds) throws Exception {
		List<HashMap<String, Object>> result = null;        
		try{            
			SqlSession session = getSqlSession();
			result = session.selectList("com.kgm.mapper.masterlist.getWeightMasterDataList", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * BOMLine의 Trim 정보를 가져옴
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getBOMLineTrimList(DataSet ds) throws Exception {
		List<HashMap<String, Object>> result = null;     
		try{            
			SqlSession session = getSqlSession();
			result = session.selectList("com.kgm.mapper.masterlist.getBOMLineTrimList", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * [20161019][ymjang] BOM Loading 속도 개선 (SQL을 이용한 DB Query 방식으로 변경함)
	 * MLM 로드시 각 Line 별 Item 및 BOMLine 속성을 가져온다.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getMLMLoadProp(DataSet ds) throws Exception {
		List<Map<String, Object>> result = null;     
		try{            
			SqlSession session = getSqlSession();
			result = session.selectList("com.kgm.mapper.masterlist.getMLMLoadProp", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * [SR170707-024][ljg] Product의 Variant 가져 오는 쿼리
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList getVariantList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList varialtList = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getVariantList", ds);

			return varialtList;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	/**
	 * [SR170707-024][ljg] Product의 OSI No를 가져 오는 쿼리
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String getOSINo(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String osi_no = (String)session.selectOne("com.kgm.mapper.masterlist.getOSINo", ds);

			return osi_no;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	/**
	 * [SR170707-024][ljg] 해당 FMP 하위의 최하위까지 모든 Part 가져 오기
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList getEpl(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getEpl", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	

	/**
	 * [SR170707-024][ljg] 부모 바로 1레벨 하위의 모든 자식들을 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getChildren(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getChildren", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	
	/**
	 * [csh] 전체 하위 자식 노드 가져오기(BOM 정전개 latest working)
	 * @Copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 14.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllChildren(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getAllChildren", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	/**
	 * [csh] 전체 하위 자식 노드 가져오기(BOM 정전개 latest Released)
	 * @Copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 14.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllChildrenReleased(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getAllChildrenReleased", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
}
