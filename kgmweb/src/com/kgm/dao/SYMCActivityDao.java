package com.kgm.dao;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.SYMCActivityMapper;

/**
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSession.commit();
            sqlSessionClose();
        }

        return retVal > 0 ? true : false;
    }

}
