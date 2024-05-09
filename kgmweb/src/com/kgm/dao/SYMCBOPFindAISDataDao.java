package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.SYMCBOPFindAISDataMapper;

public class SYMCBOPFindAISDataDao extends AbstractDao {
	
	public ArrayList<HashMap> findKORInstructionSheets(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
			resultList = mapper.findKORInstructionSheets(ds);
		} catch(Exception e) {
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap> findENGInstructionSheets(DataSet ds){
		ArrayList<HashMap> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
			resultList = mapper.findENGInstructionSheets(ds);
		} catch(Exception e) {
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	
    public int insertKORAssySheetsData(DataSet dataSet) {
        SqlSession sqlSession = null;
        int returnValue = -1;
        try {
            sqlSession = getSqlSession();
            SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
            returnValue = mapper.insertKORAssySheetsData(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return returnValue;
    }
    
    public int insertKORBodySheetsData(DataSet dataSet) {
        SqlSession sqlSession = null;
        int returnValue = -1;
        try {
            sqlSession = getSqlSession();
            SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
            returnValue = mapper.insertKORBodySheetsData(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return returnValue;
    }
    
    public int insertKORPaintSheetsData(DataSet dataSet) {
        SqlSession sqlSession = null;
        int returnValue = -1;
        try {
            sqlSession = getSqlSession();
            SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
            returnValue = mapper.insertKORPaintSheetsData(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return returnValue;
    }
    
    public int insertENGAssySheetsData(DataSet dataSet) {
        SqlSession sqlSession = null;
        int returnValue = -1;
        try {
            sqlSession = getSqlSession();
            SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
            returnValue = mapper.insertENGAssySheetsData(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return returnValue;
    }
    
    public int insertENGBodySheetsData(DataSet dataSet) {
        SqlSession sqlSession = null;
        int returnValue = -1;
        try {
            sqlSession = getSqlSession();
            SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
            returnValue = mapper.insertENGBodySheetsData(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return returnValue;
    }
    
    public int insertENGPaintSheetsData(DataSet dataSet) {
        SqlSession sqlSession = null;
        int returnValue = -1;
        try {
            sqlSession = getSqlSession();
            SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
            returnValue = mapper.insertENGPaintSheetsData(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return returnValue;
    }

	public ArrayList<HashMap<String, Object>> findPublishItemRevListDataXML(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
			resultList = (ArrayList<HashMap<String, Object>>)mapper.findPublishItemRevListDataXML(ds);
		} catch(Exception e) {
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> findPublishItemRevListDataList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
			resultList = mapper.findPublishItemRevListDataList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	 public int deleteBeforDatas(DataSet dataSet){
		 
			int resultList = -1;
			try {
				SqlSession sqlSession = getSqlSession();
				SYMCBOPFindAISDataMapper mapper = sqlSession.getMapper(SYMCBOPFindAISDataMapper.class);
				resultList = mapper.deleteBeforDatas(dataSet);
			} catch(Exception e) {
				e.printStackTrace();
				
				LogUtil.error(e.getMessage(), dataSet);
			} finally {
				sqlSessionClose();
			}
			
			return resultList;
	 }
}
