package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.dto.EndItemData;
import com.ssangyong.mapper.SYMCMEPLMapper;

public class SYMCMEPLDao extends AbstractDao {

	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds){
		ArrayList<EndItemData> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
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
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getChangedStructureCompareResultList(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
			resultList = mapper.getChangedStructureCompareResultList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getItemRevisionList(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
			resultList = mapper.getItemRevisionList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getOperationEPLCrationDate(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
			resultList = mapper.getOperationEPLCrationDate(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public void deleteOperationEPL(DataSet dataSet) {
	    SqlSession sqlSession = null;
	
	    try {
	        sqlSession = getSqlSession();
	        SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
	        mapper.deleteOperationEPL(dataSet);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
	        
	    } finally {
	        sqlSession.commit();
	        sqlSessionClose();
	    }
	
	}
	
    public void insertOperationMECOEPL(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
            mapper.insertOperationMECOEPL(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

    }

    @SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getMissingMEPLObjectList(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
			resultList = mapper.getMissingMEPLObjectList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap> getMEPLResultList(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
			resultList = mapper.getMEPLResultList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public ArrayList<HashMap> getBOPChildErrorList(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCMEPLMapper mapper = sqlSession.getMapper(SYMCMEPLMapper.class);
			resultList = mapper.getBOPChildErrorList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			LogUtil.error(e.getMessage(), ds);
		} finally {
			sqlSessionClose();
		}
		return resultList;
	}
}
