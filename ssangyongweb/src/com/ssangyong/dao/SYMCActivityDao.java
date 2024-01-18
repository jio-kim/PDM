package com.ssangyong.dao;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.SYMCActivityMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class SYMCActivityDao extends AbstractDao {

    public String getTimeStamp(DataSet ds) {
        String timeStamp = null;
        try {
            SqlSession sqlSession = getSqlSession();
            SYMCActivityMapper mapper = sqlSession.getMapper(SYMCActivityMapper.class);
            timeStamp = mapper.getTimeStamp(ds);
        } catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally{
            sqlSessionClose();
        }

        return timeStamp;
    }

    public boolean updateTimeStamp(DataSet ds) {
        int retVal = 0;
        SqlSession sqlSession = null;
        try {
            sqlSession = getSqlSession();
            SYMCActivityMapper mapper = sqlSession.getMapper(SYMCActivityMapper.class);
            retVal = mapper.updateTimeStamp(ds);
        } catch(Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return retVal > 0 ? true : false;
    }

    public boolean updateEnglishName(DataSet ds) {
        int retVal = 0;
        SqlSession sqlSession = null;
        try {
            sqlSession = getSqlSession();
            SYMCActivityMapper mapper = sqlSession.getMapper(SYMCActivityMapper.class);
            retVal = mapper.updateEnglishName(ds);
        } catch(Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return retVal > 0 ? true : false;
    }

    public boolean mergeTimeStamp(DataSet ds) {
        int retVal = 0;
        SqlSession sqlSession = null;
        try {
            sqlSession = getSqlSession();
            SYMCActivityMapper mapper = sqlSession.getMapper(SYMCActivityMapper.class);
            retVal = mapper.mergeTimeStamp(ds);
        } catch(Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return retVal > 0 ? true : false;
    }

}
