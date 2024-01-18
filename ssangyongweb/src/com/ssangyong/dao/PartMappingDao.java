package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class PartMappingDao extends AbstractDao {
	@SuppressWarnings("unchecked")
    public void insertTrim(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);

            ArrayList<HashMap<String, Object>> trims = (ArrayList<HashMap<String, Object>>)ds.get("DATA");
            
            for (HashMap<String, Object> map : trims) {
                session.insert("com.ssangyong.mapper.partmapping.insertTrim", map);
            }
//            String projNo = (String)ds.get("PROJ_NO");
//            String sysCode = (String)ds.get("SYS_CODE");
//            String prePartNo = (String)ds.get("PRE_PART_NO");
//            String preDispNo = (String)ds.get("PRE_DISP_NO");
//            String prdPartNo = (String)ds.get("PRD_PART_NO");
//
//
////            session.delete("com.ssangyong.mapper.partmapping.deleteTrim", ds);
//            for( HashMap<String, Object> map : trims){
//            	map.put("PROJ_NO", projNo);
//                map.put("SYS_CODE", sysCode);
//                map.put("PRE_PART_NO", prePartNo);
//                map.put("PRE_DISP_NO", preDispNo);
//                map.put("PRD_PART_NO", prdPartNo);
//            	session.insert("com.ssangyong.mapper.partmapping.insertTrim", map);
//            }
            session.commit();
        } catch (Exception e) {
        	session.rollback();
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}	
}
