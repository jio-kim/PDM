package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;

/**
 * ���BOM �����ϱ� ���� DAO
 * [SR140722-022][20140522] swyoon ���� ����
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
 */
public class ModuleBomValidationDao extends AbstractDao {

	@SuppressWarnings("rawtypes")
	public ArrayList validateModule(DataSet ds) throws Exception{
		ArrayList list = null;
        try {
            SqlSession session = getSqlSession();
            session.selectList("com.symc.module.validateModule", ds);
            list = (ArrayList)ds.get("list");
//            return list;
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			throw new Exception(e.getMessage()); 
//			throw e;
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap<String, Object>> getModulePart(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            List list = session.selectList("com.symc.module.getModulePart", ds);
            return (ArrayList<HashMap<String, Object>>)list;
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap<String, String>> getModuleValidationResult(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            List list = session.selectList("com.symc.module.getModuleValidationResult", ds);
            return (ArrayList<HashMap<String, String>>)list;
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return null;
	}
}
