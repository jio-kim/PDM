package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.dto.EndItemData;
import com.ssangyong.mapper.NoticeMapper;
import com.ssangyong.mapper.SYMCBOPMapper;

public class SYMCBOPDao extends AbstractDao {

	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds){
		ArrayList<EndItemData> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
			resultList = mapper.findReplacedEndItems(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	/*
	 * SoaWeb에서 EndItemList 조회시 
	 * Mbom 과 Ebom 의 모든 EndItem을 조회 하여야 하나 속도가 너무 느려 
	 * 개선된 쿼리를 적용한 메서드 
	 */
	public ArrayList<EndItemData> findReplacedRootEndItems(DataSet ds){
		ArrayList<EndItemData> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
			resultList = mapper.findReplacedRootEndItems(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	
    public boolean insertOperationOccurenceForInstructionSheets(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
            mapper.insertOperationOccurenceForInstructionSheets(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return true;
    }
	
	/**
	 * [NONE_SR][20151123] taeku.jeong Operation검색 속도 개선을위해 API를 이용한 전개 방식이 아닌 Query를 이용하는 방식으로 개선
	 * @param ds
	 * @return
	 */
	 @SuppressWarnings("rawtypes")
	public List<HashMap> findOperationOccurenceForInstructionSheets(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findOperationOccurenceForInstructionSheets(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	 
	 @SuppressWarnings("rawtypes")
	public List<HashMap> findOperationOccurenceForInstructionSheetsNew(DataSet ds) {
	        List<HashMap> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
	            result = mapper.findOperationOccurenceForInstructionSheetsNew(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), ds);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
	 }
	 
	public boolean deleteOperationOccurenceForInstructionSheets(DataSet dataSet) {
	    SqlSession sqlSession = null;
	
	    try {
	        sqlSession = getSqlSession();
	        SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
	        mapper.deleteOperationOccurenceForInstructionSheets(dataSet);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
	        
	    } finally {
	        sqlSession.commit();
	        sqlSessionClose();
	    }
	
	    return true;
	}
	
	public boolean deleteOperationOccurenceForInstructionSheets2(DataSet dataSet) {
	    SqlSession sqlSession = null;
	
	    try {
	        sqlSession = getSqlSession();
	        SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
	        mapper.deleteOperationOccurenceForInstructionSheets2(dataSet);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
	        
	    } finally {
	        sqlSession.commit();
	        sqlSessionClose();
	    }
	
	    return true;
	}
	
	/**
	 * [NONE-SR] [20151126] taeku.jeong 조립작업 표준서 Password 일괄변경을 위한 대상 Data 검색
	 * @param ds
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<HashMap> findAISPasswordMigrationTarget(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findAISPasswordMigrationTarget(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	
	/**
	 * [SR151207-041] [20151211] taeku.jeong 용접점의 Occurrence 이름을 이용해 할당된 ABS_OCC_ID를 찾아준다.
	 * @param ds
	 * @return
	 */
	public List<HashMap> findPWProductAbsOccurenceId(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findPWProductAbsOccurenceId(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	
	/**
	 * [SR151207-041] [20151215] taeku.jeong 용접점의 ABS_OCC_ID를 이용해 할당된 Occurrence 이름을 찾아준다.
	 * @param ds
	 * @return
	 */
	public List<HashMap> findWPProductOccurenceName(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findWPProductOccurenceName(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	
}
