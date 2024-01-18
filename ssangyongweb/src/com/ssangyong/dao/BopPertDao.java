package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.BopPertMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class BopPertDao extends AbstractDao {

    public BopPertDao() {

    }
    public ArrayList<HashMap<String, Object>> selectBopPertList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopPertList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopPertList = mapper.selectBopPertList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopPertList;
    }

    public ArrayList<HashMap<String, Object>> selectStationDecessorsList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationDecessorsList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationDecessorsList = mapper.selectStationDecessorsList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationDecessorsList;
    }

    public ArrayList<HashMap<String, Object>> selectBopEndItemList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationDecessorsEndItemList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationDecessorsEndItemList = mapper.selectBopEndItemList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationDecessorsEndItemList;
    }

    public ArrayList<HashMap<String, Object>> selectBopVehpartList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopVehpartList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopVehpartList = mapper.selectBopVehpartList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopVehpartList;
    }

    public ArrayList<HashMap<String, Object>> selectBopStationPertList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> selectBopStationPertList = null;
        
        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            selectBopStationPertList = mapper.selectBopStationPertList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }
        
        return selectBopStationPertList;
    }
    
    public ArrayList<HashMap<String, Object>> selectBopStationPertCountList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationPertCountList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationPertCountList = mapper.selectBopStationPertCountList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationPertCountList;
    }

    public ArrayList<HashMap<String, Object>> selectBopStationCount(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationCount = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationCount = mapper.selectBopStationCount(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationCount;
    }

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationDecessorsList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationDecessorsList = mapper.selectBopStationDecessorsList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationDecessorsList;
    }

    public boolean insertBopStationDecessorsInfo(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            mapper.insertBopStationDecessorsInfo(dataSet);
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

    public boolean updateBopPertInfo(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            mapper.updateBopPertInfo(dataSet);
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

    public boolean deleteBopPertInfo(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            mapper.deleteBopPertInfo(dataSet);
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

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsLastModDateList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationDecessorsLastModDateList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationDecessorsLastModDateList = mapper.selectBopStationDecessorsLastModDateList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationDecessorsLastModDateList;
    }

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsEndItemList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> bopStationDecessorsEndItemList = null;

        try {
            sqlSession = getSqlSession();
            BopPertMapper mapper = sqlSession.getMapper(BopPertMapper.class);
            bopStationDecessorsEndItemList = mapper.selectBopStationDecessorsEndItemList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return bopStationDecessorsEndItemList;
    }

}
