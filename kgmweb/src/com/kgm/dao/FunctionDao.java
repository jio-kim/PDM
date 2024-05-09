package com.kgm.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.FunctionMapper;

/**
 * [SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� Class �ű� ����
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
 */
public class FunctionDao extends AbstractDao {

    public List<HashMap<String, String>> serchProductFunction(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            FunctionMapper mapper = session.getMapper(FunctionMapper.class);
            result = mapper.serchProductFunction(ds);            
        }catch(Exception e){

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        	throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }    
}
