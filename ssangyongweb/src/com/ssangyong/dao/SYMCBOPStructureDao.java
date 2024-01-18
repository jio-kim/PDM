package com.ssangyong.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.SYMCBOPStructureMapper;

/**
 * [NONE_SR][20160126] taeku.jeong BOP Structure 정보를 관리할 목적으로 만든 Class
 * [NONE_SR][20161107] commit/rollback 소스 위치 조정
 * @author Taeku
 *
 */
public class SYMCBOPStructureDao extends AbstractDao {
	
    public boolean saveShopStructureData(DataSet dataSet) {
    	
        SqlSession sqlSession = null;
        
        try {
            sqlSession = getSqlSession();
            SYMCBOPStructureMapper mapper = sqlSession.getMapper(SYMCBOPStructureMapper.class);
            mapper.saveShopStructureData(dataSet);
            sqlSession.commit();
        } catch (Exception e) {
        	e.printStackTrace();
        	sqlSession.rollback();
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return true;
    }
	
	public boolean deleteOldShopStructureData(DataSet dataSet) {
		
	    SqlSession sqlSession = null;
	
	    try {
	        sqlSession = getSqlSession();
	        SYMCBOPStructureMapper mapper = sqlSession.getMapper(SYMCBOPStructureMapper.class);
	        mapper.deleteOldShopStructureData(dataSet);
	        sqlSession.commit();
	    } catch (Exception e) {
	        e.printStackTrace();
        	sqlSession.rollback();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return true;
	}
	
	@SuppressWarnings("rawtypes")
	public List<HashMap> getAllStationCount(DataSet dataSet) {
		
	        List<HashMap> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
	            result = mapper.getAllStationCount(dataSet);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), dataSet);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
	 }
	
	@SuppressWarnings("rawtypes")
	public List<HashMap> getPredecessorLines(DataSet dataSet) {
		
	        List<HashMap> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
	            result = mapper.getPredecessorLines(dataSet);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), dataSet);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
	 }
	 
	@SuppressWarnings("rawtypes")
	public List<HashMap> getPredecessorStationsAtLine(DataSet dataSet) {
		
	        List<HashMap> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
	            result = mapper.getPredecessorStationsAtLine(dataSet);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), dataSet);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
	 }
	 
	@SuppressWarnings("rawtypes")
	public List<HashMap> getPredecessorStationsAtAllLine(DataSet dataSet) {
	        List<HashMap> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
	            result = mapper.getPredecessorStationsAtAllLine(dataSet);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), dataSet);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
	 }
	
    @SuppressWarnings("rawtypes")
	public List<HashMap> getUnPertedStationList(DataSet dataSet) {
		 
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
            result = mapper.getUnPertedStationList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	 
	@SuppressWarnings("rawtypes")
	public List<HashMap> keyCodeListFind(DataSet dataSet) {
		 
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
            result = mapper.keyCodeListFind(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}

	@SuppressWarnings("rawtypes")
	public List<HashMap> getLatestKeyCodeForShop(DataSet dataSet) {
		 
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPStructureMapper mapper = session.getMapper(SYMCBOPStructureMapper.class);
            result = mapper.getLatestKeyCodeForShop(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	
}
