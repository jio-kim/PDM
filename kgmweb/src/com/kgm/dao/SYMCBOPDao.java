package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.NoticeMapper;
import com.kgm.mapper.SYMCBOPMapper;
import com.kgm.dto.EndItemData;

public class SYMCBOPDao extends AbstractDao {

	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds){
		ArrayList<EndItemData> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
			resultList = mapper.findReplacedEndItems(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	/*
	 * SoaWeb���� EndItemList ��ȸ�� 
	 * Mbom �� Ebom �� ��� EndItem�� ��ȸ �Ͽ��� �ϳ� �ӵ��� �ʹ� ���� 
	 * ������ ������ ������ �޼��� 
	 */
	public ArrayList<EndItemData> findReplacedRootEndItems(DataSet ds){
		ArrayList<EndItemData> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
			resultList = mapper.findReplacedRootEndItems(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	
    public boolean insertOperationOccurenceForInstructionSheets(DataSet dataSet) {
        SqlSession sqlSession = null;

        try {
            sqlSession = getSqlSession();
            SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
            mapper.insertOperationOccurenceForInstructionSheets(dataSet);
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
	
	/**
	 * [NONE_SR][20151123] taeku.jeong Operation�˻� �ӵ� ���������� API�� �̿��� ���� ����� �ƴ� Query�� �̿��ϴ� ������� ����
	 * @param ds
	 * @return
	 */
	 @SuppressWarnings("rawtypes")
	public List<HashMap> findOperationOccurenceForInstructionSheets(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findOperationOccurenceForInstructionSheets(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	 
	 @SuppressWarnings("rawtypes")
	public List<HashMap> findOperationOccurenceForInstructionSheetsNew(DataSet ds) {
	        List<HashMap> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
	            result = mapper.findOperationOccurenceForInstructionSheetsNew(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
	            
				// [20160928][ymjang] log4j�� ���� ���� �α� ���
				LogUtil.error(e.getMessage(), ds);
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
	 }
	 
	public boolean deleteOperationOccurenceForInstructionSheets(DataSet dataSet) {
	    SqlSession sqlSession = null;
	
	    try {
	        sqlSession = getSqlSession();
	        SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
	        mapper.deleteOperationOccurenceForInstructionSheets(dataSet);
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
	
	public boolean deleteOperationOccurenceForInstructionSheets2(DataSet dataSet) {
	    SqlSession sqlSession = null;
	
	    try {
	        sqlSession = getSqlSession();
	        SYMCBOPMapper mapper = sqlSession.getMapper(SYMCBOPMapper.class);
	        mapper.deleteOperationOccurenceForInstructionSheets2(dataSet);
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
	
	/**
	 * [NONE-SR] [20151126] taeku.jeong �����۾� ǥ�ؼ� Password �ϰ������� ���� ��� Data �˻�
	 * @param ds
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<HashMap> findAISPasswordMigrationTarget(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findAISPasswordMigrationTarget(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	
	/**
	 * [SR151207-041] [20151211] taeku.jeong �������� Occurrence �̸��� �̿��� �Ҵ�� ABS_OCC_ID�� ã���ش�.
	 * @param ds
	 * @return
	 */
	public List<HashMap> findPWProductAbsOccurenceId(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findPWProductAbsOccurenceId(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	
	/**
	 * [SR151207-041] [20151215] taeku.jeong �������� ABS_OCC_ID�� �̿��� �Ҵ�� Occurrence �̸��� ã���ش�.
	 * @param ds
	 * @return
	 */
	public List<HashMap> findWPProductOccurenceName(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCBOPMapper mapper = session.getMapper(SYMCBOPMapper.class);
            result = mapper.findWPProductOccurenceName(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	
}
