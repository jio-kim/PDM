package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.BOMViewerMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class BOMViewerDao extends AbstractDao {
	public ArrayList<HashMap<String, Object>> selectBOMViewer(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			BOMViewerMapper mapper = sqlSession.getMapper(BOMViewerMapper.class);
			resultList = mapper.selectBOMViewer(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
}
