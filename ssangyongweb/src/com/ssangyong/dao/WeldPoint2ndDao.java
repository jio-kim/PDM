package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.WeldPoint2ndMapper;

public class WeldPoint2ndDao extends AbstractDao {
	
	public void deleteWeldPointRawData(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.deleteWeldPointRawData(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}
	
	public void insertWeldPointRawDataRow(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.insertWeldPointRawDataRow(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}

	public void deleteWeldPointRaw2Data(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.deleteWeldPointRaw2Data(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}

	public void makeArrangedStartPointData(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.makeArrangedStartPointData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}
	
	public void updateArrangedStartPointDataScaling(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.updateArrangedStartPointDataScaling(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}

	public ArrayList<HashMap<String, Object>> getECOId(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            list = mapper.getECOId(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}
	
	public void deleteCurrentSavedData(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.deleteCurrentSavedData(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}
	
	public void deleteCurrentInboundData(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.deleteCurrentInboundData(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}
	
	public void makeInBoundData(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.makeInBoundData(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}
	
	public void makeEndDiffData(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.makeEndDiffData(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}
	
	public void makeSaveDataForDelete(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.makeSaveDataForDelete(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}	
	
	public void makeSaveDataForInBound(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.makeSaveDataForInBound(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

	}
	
	public void makeSaveDataForEndDiff(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.makeSaveDataForEndDiff(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

	}
	
	public void makeSaveDataForAdd(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
            mapper.makeSaveDataForAdd(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

	}
	
	public void deleteBOMWeldPointData(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.deleteBOMWeldPointData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}

	public void insertBOMWeldPointDataRow(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.insertBOMWeldPointDataRow(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}

	public void deleteBOMWeldPoint2Data(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.deleteBOMWeldPoint2Data(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}

	public void makeBOMArrangedStartPointData(DataSet ds) throws Exception{
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        mapper.makeBOMArrangedStartPointData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	}
	
	public ArrayList<HashMap<String, Object>> findHaveSameEcoWeldGroupRevisionData(DataSet ds) throws Exception {
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.findHaveSameEcoWeldGroupRevisionData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	}
	
	public ArrayList<HashMap<String, Object>> getECOMatchedFMPRevision(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getECOMatchedFMPRevision(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	}
	
	public ArrayList<HashMap<String, Object>> getDeleteTargetBOMLineData(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getDeleteTargetBOMLineData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	}
	
	public ArrayList<HashMap<String, Object>> getAddTargetWeldPointData(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getAddTargetWeldPointData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	}
	
	public ArrayList<HashMap<String, Object>> getMaxOccSeqNo(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getMaxOccSeqNo(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	    
	}
	
	public  ArrayList<HashMap<String, Object>> getAllNewBOMLineCount(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getAllNewBOMLineCount(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	    
	}
	
	public  ArrayList<HashMap<String, Object>> getUpdateTargetBOMLineData(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getUpdateTargetBOMLineData(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	    
	}
	
	public  ArrayList<HashMap<String, Object>> getChildNodeWeldTypeList(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        WeldPoint2ndMapper mapper = session.getMapper(WeldPoint2ndMapper.class);
	        list = mapper.getChildNodeWeldTypeList(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	}
	
}
