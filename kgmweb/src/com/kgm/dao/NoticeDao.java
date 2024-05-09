package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.NoticeMapper;

public class NoticeDao extends AbstractDao {

    public NoticeDao() {

    }

    public boolean insertNotice(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            mapper.insertNotice(dataSet);
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

    public boolean insertNoticeContents(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            mapper.insertNoticeContents(dataSet);
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

    public boolean updateNotice(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            mapper.updateNotice(dataSet);
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

    public boolean deleteNotice(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            mapper.deleteNotice(dataSet);
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

    public boolean deleteNoticeContents(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            mapper.deleteNoticeContents(dataSet);
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

    public ArrayList<HashMap<String, Object>> selectNoticeList() {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> noticeList = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            noticeList = mapper.selectNoticeList();
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
            
        } finally {
            sqlSessionClose();
        }

        return noticeList;
    }

    public ArrayList<HashMap<String, Object>> selectNoticeContentsList(DataSet dataSet) {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> noticeContentsList = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            noticeContentsList = mapper.selectNoticeContentsList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
            
        } finally {
            sqlSessionClose();
        }

        return noticeContentsList;
    }

    public ArrayList<HashMap<String, Object>> selectPopUpList() {
        SqlSession sqlSession = null;
        ArrayList<HashMap<String, Object>> popUpList = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
            popUpList = mapper.selectPopUpList();
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
            
        } finally {
            sqlSessionClose();
        }

        return popUpList;
    }

    public HashMap<String, Object> selectNextOUID() {
        SqlSession sqlSession = null;
        HashMap<String, Object> ouidMap = null;

        try {
            sqlSession = getSqlSession();
            NoticeMapper mapper = sqlSession.getMapper(NoticeMapper.class);
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
