package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.AddfileMapper;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }

        return seq;
    }
    
}
