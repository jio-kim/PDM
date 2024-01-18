package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.AddfileMapper;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class AddfileDao extends AbstractDao {

    public AddfileDao() {

    }

    public boolean insertAddfile(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            AddfileMapper mapper = sqlSession.getMapper(AddfileMapper.class);
            mapper.insertAddfile(dataSet);
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

    public boolean deleteAddfile(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            AddfileMapper mapper = sqlSession.getMapper(AddfileMapper.class);
            mapper.deleteAddfile(dataSet);
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

    public boolean updAddfile(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            AddfileMapper mapper = sqlSession.getMapper(AddfileMapper.class);
            mapper.updAddfile(dataSet);
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

    public ArrayList<HashMap<String, Object>> selectAddfileList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> addFileList = null;

        try {
            sqlSession = getSqlSession();
            AddfileMapper mapper = sqlSession.getMapper(AddfileMapper.class);
            addFileList = mapper.selectAddfileList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return addFileList;
    }

    public int selectMaxSeq(DataSet dataSet) {
        SqlSession sqlSession = null;
        int seq = 0;
        try {
            sqlSession = getSqlSession();
            AddfileMapper mapper = sqlSession.getMapper(AddfileMapper.class);
            seq = mapper.selectMaxSeq(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return seq;
    }
    
}
