package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class OSpecDao extends AbstractDao {
	
	public void insertOSpec(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);
            HashMap oSpecMaster = (HashMap)ds.get("OSPEC_MASTER");
            ArrayList<HashMap<String, String>> data = (ArrayList<HashMap<String, String>>)ds.get("OSPEC_DETAIL");
            
            if(oSpecMaster == null){
            	throw new Exception("Could not find the OSPEC_MASTER info.");
            }
            
            if(data == null){
            	throw new Exception("Could not find the OSPEC_DETAIL info.");
            }
            
            HashMap tmpMap = session.selectOne("com.kgm.mapper.ospec.getOspecMaster", oSpecMaster);
            if(tmpMap!=null && !tmpMap.isEmpty()){
            	session.delete("com.kgm.mapper.ospec.deleteOspecDetail", oSpecMaster);
            }
            
            ArrayList tmpList = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecTrim", oSpecMaster);
            if(tmpList!=null && !tmpList.isEmpty()){
            	session.delete("com.kgm.mapper.ospec.deleteOspecTrim", oSpecMaster);
            }            
        	ArrayList<HashMap<String, String>> trimList = (ArrayList<HashMap<String, String>>)ds.get("OSPEC_TRIM");
        	for( HashMap<String, String> trim : trimList){
        		session.insert("com.kgm.mapper.ospec.insertOspecTrim", trim);
        	}
            
            session.insert("com.kgm.mapper.ospec.insertOspecMaster", oSpecMaster);
            for( HashMap<String, String> map : data){
            	session.insert("com.kgm.mapper.ospec.insertOspecDetail", map);
            }
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
	
	public ArrayList getOspecMaster(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList masterInfo = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecMaster", ds);
			
			return masterInfo;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getOspecDetail(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList ospecDetail = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecDetail", ds);
			
			return ospecDetail;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public HashMap getOspec(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList ospecMaster = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecMaster", ds);
			ArrayList ospecTrim = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecTrim", ds);
			ArrayList ospecDetail = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecDetail", ds);
			HashMap map = new HashMap();
			map.put("OSPEC_MASTER", ospecMaster);
			map.put("OSPEC_TRIM", ospecTrim);
			map.put("OSPEC_DETAIL", ospecDetail);
			return map;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getGModel(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList gModels = (ArrayList)session.selectList("com.kgm.mapper.ospec.getGModel", ds);
			
			return gModels;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public ArrayList getProject(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList projects = (ArrayList)session.selectList("com.kgm.mapper.ospec.getProject", ds);
			
			return projects;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}		
	
	public ArrayList getOspecTrim(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList ospecTrim = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOspecTrim", ds);
			
			return ospecTrim;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public ArrayList getOptionGroup(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList optionGroup = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOptionGroup", ds);
			
			return optionGroup;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public void insertOptionGroup(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            ArrayList<HashMap<String, String>> data = (ArrayList<HashMap<String, String>>)ds.get("DATA");
            for( int i = 0; data != null && i < data.size(); i++){
            	HashMap<String, String> map = data.get(i);
            	if( i == 0 ){
            		session.delete("com.kgm.mapper.ospec.deleteOptionGroup", map);
            	}
            	session.insert("com.kgm.mapper.ospec.insertOptionGroup", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}	
	
	public ArrayList getOptionGroupDetail(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList optionGroupDetail = (ArrayList)session.selectList("com.kgm.mapper.ospec.getOptionGroupDetail", ds);
			
			return optionGroupDetail;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public void deleteOptionGroup(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.delete("com.kgm.mapper.ospec.deleteOptionGroup", ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}	
	
	public HashMap<String, ArrayList> getReferedOptionGroup(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			
			HashMap<String, ArrayList> resultMap = new HashMap();
			HashMap<String, ArrayList<String>> paraMap = (HashMap<String, ArrayList<String>>)ds.get("DATA");
			Iterator<String> its = paraMap.keySet().iterator();
			while(its.hasNext()){
				String project = its.next();
				ArrayList<String> list = paraMap.get(project);
				for( String opValue : list){
					HashMap map = new HashMap();
					map.put("VALUE", opValue);
					map.put("PROJECT", project);
					
					ArrayList referredOptionGroup = (ArrayList)session.selectList("com.kgm.mapper.ospec.getReferedOptionGroup", map);
					resultMap.put(project + "_" + opValue, referredOptionGroup);
				}
			}
			
			return resultMap;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	
	public ArrayList getFunctionList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList functionList = (ArrayList)session.selectList("com.kgm.mapper.ospec.getFunctionList", ds);
			
			return functionList;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getUsedCondition(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			
			ArrayList result = new ArrayList();
			ArrayList<String> functionList = (ArrayList<String>)ds.get("FUNCTION_LIST");
			for(int i = 0; functionList!=null && i < functionList.size(); i++){
				String functionID = functionList.get(i);
				HashMap map = new HashMap();
				map.put("FUNCTION_ID", functionID);
				map.put("IS_RELEASE_FLAG", ds.get("IS_RELEASE_FLAG"));
				map.put("PART_NAME", ds.get("PART_NAME"));
				ArrayList conditionList = (ArrayList)session.selectList("com.kgm.mapper.ospec.getUsedCondition", map);
				for( int j = 0; conditionList != null && j < conditionList.size(); j++){
					HashMap<String, String> resultMap = (HashMap<String, String>)conditionList.get(j);
					String inUseCondition = resultMap.get("CONDITION");
					if( !result.contains(inUseCondition)){
						result.add(resultMap);
					}
				}
			}
			
			return result;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	/**
	 * OSpec 등록 후 변경된 Trim 유무에 따라 Flag 변경 작업 수행 Procedure 호출
	 * @param ds
	 * @throws Exception
	 */
    public void updateOSpecTrimStat(DataSet ds) throws Exception {
    	try{
			SqlSession session = getSqlSession();
			session.update("com.kgm.mapper.ospec.updateOSpecTrimStat", ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
    }
    
    /**
     * OSpec Group Master 정보를 Update 함
     * @param ds
     * @throws Exception
     */
    public void updateOpGroupMaster(DataSet ds) throws Exception {
    	try{
			SqlSession session = getSqlSession();
			session.update("com.kgm.mapper.ospec.updateOpGroupMaster", ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
    }
    
    
    /**
     * OSpec Group Master Condition 정보를 Update 함
     * @param ds
     * @throws Exception
     */
    public void updateOpGroupCondition(DataSet ds) throws Exception {
    	try{
			SqlSession session = getSqlSession();
			session.update("com.kgm.mapper.ospec.updateOpGroupCondition", ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
    }
    
    /**
     * OSpec Group 의 Condition 을 가져옴
     * @param ds
     * @return
     * @throws Exception
     */
    public String getOpGroupCondition(DataSet ds) throws Exception {
    	try{
			SqlSession session = getSqlSession();
			return (String)session.selectOne("com.kgm.mapper.ospec.getOpGroupCondition", ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
    }
    
    //[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
    public ArrayList getDCSInfo(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.ospec.getDCSInfo", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
    
    
}
