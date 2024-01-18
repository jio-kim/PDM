package com.ssangyong.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.PreBOMUssageExportMapper;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class PreBOMUssageExportDao extends AbstractDao {
	 
	 @SuppressWarnings("rawtypes")
	public List<HashMap> getExportTargetProjectList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
            result = mapper.getExportTargetProjectList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	
	 @SuppressWarnings("rawtypes")
    public List<HashMap> geProjectUssageHeaderList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
            result = mapper.geProjectUssageHeaderList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	    
	 @SuppressWarnings("rawtypes")
    public List<HashMap> geProjectMasterDataList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
            result = mapper.geProjectMasterDataList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
    }
	    
	 @SuppressWarnings("rawtypes")
    public List<HashMap> geProjectUssageDataList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
            result = mapper.geProjectUssageDataList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	 
	 @SuppressWarnings("rawtypes")
	    public void updateCost(DataSet ds) {
	        try {
	            SqlSession session = getSqlSession();
	            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
	            mapper.updateCost(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), ds);
	            
	        } finally {
	            sqlSessionClose();
	        }
		 }
	 
//	 @SuppressWarnings("rawtypes")
//	    public void updateCost(DataSet ds) throws Exception{
//		 	boolean result = true; 
//		 	SqlSessionFactory sessionFactory = null;
//		 	SqlSession session = null;
//	        try {
//	        	InputStream inputStream = Resources.getResourceAsStream("com/ssangyong/config/mybatis-config.xml");
//	        	sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
//	        	
//	            session = sessionFactory.openSession(ExecutorType.BATCH);
//	            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
////	            mapper.updateCost(ds);
//	            
//	            HashMap<String,Object> executeMap = (HashMap)ds.get("MAP");
//	            int exeCount = executeMap.size();
//	            String targetCost = "";
//				String prdToolCost = "";
//				String uniqNo = "";
//				String eaiDate = ds.get("EAIDATE").toString();
//				String targetProject = ds.get("TARGETPROJECT").toString();
//				HashMap<String,Object> updateMap = new HashMap();
//				
//	            for(int i=0; i<exeCount; i++){
//	            	updateMap = (HashMap)executeMap.get(String.valueOf(i));
//					targetCost = updateMap.get("targetCost").toString();
//					prdToolCost = updateMap.get("prdToolCost").toString();
//					uniqNo = updateMap.get("itemId").toString();
//					DataSet ds1 = new DataSet();
//			        ds1.put("TARGETPROJECT", targetProject);
//			        ds1.put("UNIQNO", uniqNo);
//			        ds1.put("TARGETCOST", targetCost);
//			        ds1.put("PRDTOOLCOST", prdToolCost);
//			        ds1.put("EAIDATE", eaiDate);
//			        mapper.updateCost(ds1);
//					
//	            }
//	            session.commit();
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	            
//				// [20160928][ymjang] log4j에 의한 에러 로그 기록
//				LogUtil.error(e.getMessage(), ds);
//				result = false;
//	        } finally {
//	            if( session != null )
//	            	session.close();
//	            if(!result){
//	            	throw new Exception("오류 발생");
//	            }
//	        }
//		 }
	 
	 @SuppressWarnings("rawtypes")
	    public String getEaiDate(DataSet ds) {
		 String result = "";
	        try {
	            SqlSession session = getSqlSession();
	            PreBOMUssageExportMapper mapper = session.getMapper(PreBOMUssageExportMapper.class);
	            result = mapper.getEaiDate(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j에 의한 에러 로그 기록
				LogUtil.error(e.getMessage(), ds);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
		 }
	 
}
