package com.kgm.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.GetParentMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class GetParentDao extends AbstractDao {

    public List<HashMap<String, String>> searchUpperBOM(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;        
        try{
            SqlSession session = getSqlSession();
            GetParentMapper mapper = session.getMapper(GetParentMapper.class);
            result = mapper.searchUpperBOM(ds);
        }catch(Exception e){

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
            
        	throw e;
        }finally{
            sqlSessionClose();
        }
        return result;
    }
    
    public List<HashMap<String, String>> searchAll(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;        
        try{
            SqlSession session = getSqlSession();
            GetParentMapper mapper = session.getMapper(GetParentMapper.class);
            result = mapper.searchAll(ds);
        }catch(Exception e){

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
			
        	throw e;
        }finally{
            sqlSessionClose();
        }
        return result;
    }
    
    public List<HashMap<String, String>> searchLatestReleased(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{
    		SqlSession session = getSqlSession();
    		GetParentMapper mapper = session.getMapper(GetParentMapper.class);
    		result = mapper.searchLatestReleased(ds);            
    	}catch(Exception e){

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
			
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> searchLatestWorking(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		GetParentMapper mapper = session.getMapper(GetParentMapper.class);
    		result = mapper.searchLatestWorking(ds);            
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }    
    
    public List<HashMap<String, String>> isConnectedFunction(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		GetParentMapper mapper = session.getMapper(GetParentMapper.class);
    		result = mapper.isConnectedFunction(ds);            
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }    

    public List<HashMap<String, Object>> whereUsedStructure(DataSet ds) throws Exception {
        List<HashMap<String, Object>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            GetParentMapper mapper = session.getMapper(GetParentMapper.class);
            result = mapper.whereUsedStructure(ds);      
            if(result != null){
                for( HashMap<String, Object> map : result){
                    Clob vc = (Clob)map.get("VARIANT_CONDITION");
                    map.put("VARIANT_CONDITION", clobToString(vc));
                }
            }
        }catch(Exception e){
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
        	
            throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    public List<HashMap<String, Object>> whereUsedPreBOMStructure(DataSet ds) throws Exception {
        List<HashMap<String, Object>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            GetParentMapper mapper = session.getMapper(GetParentMapper.class);
            result = mapper.whereUsedPreBOMStructure(ds);      
            if(result != null){
                for( HashMap<String, Object> map : result){
                    Clob vc = (Clob)map.get("VARIANT_CONDITION");
                    map.put("VARIANT_CONDITION", clobToString(vc));
                }
            }
        }catch(Exception e){
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
        	
            throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }    

    public String clobToString(Clob clob) throws SQLException, IOException {
        if (clob == null) {
            return "";
        }
        StringBuffer strOut = new StringBuffer();
        String str = "";
        BufferedReader br = new BufferedReader(clob.getCharacterStream());
        while ((str = br.readLine()) != null) {
            strOut.append(str);
        }
        return strOut.toString();
    }
}
