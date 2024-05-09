package com.kgm.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.SYMCSubsidiaryMapper;

public class SYMCSubsidiaryDao extends AbstractDao {

    public List<HashMap<String, String>> serchSubsidiary(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;  
        SqlSession session = null;
        try{            
            session = getSqlSession();
            SYMCSubsidiaryMapper mapper = session.getMapper(SYMCSubsidiaryMapper.class);
            result = mapper.searchSubsidiary(ds);            
        }catch(Exception e){
        	session.rollback();
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        }finally{
        	session.commit();
            sqlSessionClose();
        }        
        return result;
    }    
}
