package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.FaqMapper;

/**
 * [SR150421-027][20150811][ymjang] PLM system
 * [20160928][ymjang] log4j에 의한 에러 로그 기록 
 */
public class FaqDao extends AbstractDao {

    public FaqDao() {

    }

    public boolean insertFaq(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            FaqMapper mapper = sqlSession.getMapper(FaqMapper.class);
            mapper.insertFaq(dataSet);
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

    public boolean updateFaqSeq(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            FaqMapper mapper = sqlSession.getMapper(FaqMapper.class);
            mapper.updateFaqSeq(dataSet);
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

    public boolean updateFaq(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            FaqMapper mapper = sqlSession.getMapper(FaqMapper.class);
            mapper.updateFaq(dataSet);
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

    public boolean deleteFaq(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            //AddfileMapper addFilemapper = sqlSession.getMapper(AddfileMapper.class);
            //addFilemapper.deleteAddfileAll(dataSet);
        
            FaqMapper mapper = sqlSession.getMapper(FaqMapper.class);
            mapper.deleteFaq(dataSet);

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

    public ArrayList<HashMap<String, Object>> selectFaqList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> faqList = null;

        try {
            sqlSession = getSqlSession();
            FaqMapper mapper = sqlSession.getMapper(FaqMapper.class);
            faqList = mapper.selectFaqList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return faqList;
    }

    public HashMap<String, Object> selectNextOUID() {
        SqlSession sqlSession = null;
        HashMap<String, Object> ouidMap = null;

        try {
            sqlSession = getSqlSession();
            FaqMapper mapper = sqlSession.getMapper(FaqMapper.class);
            ouidMap = mapper.selectNextOUID();
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
            
        } finally {
            sqlSessionClose();
        }

        return ouidMap;
    }

}
