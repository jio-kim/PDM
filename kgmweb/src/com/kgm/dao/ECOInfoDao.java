package com.kgm.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.SYMCECOMapper;
import com.kgm.dto.ApprovalLineData;
import com.kgm.dto.SYMCECOStatusData;
import com.kgm.dto.TCEcoModel;
import com.kgm.dto.VnetTeamInfoData;

/**
 * [20160606][ymjang] ���� �߼� ��� ���� (through EAI)
 * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
 * [20160721][ymjang] java.io.NotSerializableException ���� ����
 * [20160912][ymjang] ORA-02050: Ʈ����� 15.1.435454�� �ѹ�ǰ�, �ٸ� ���� DB�� �Ҹ���� �����Դϴ� --> ���� ����
 * [SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
 */
public class ECOInfoDao extends AbstractDao {

	
	/**
	 * TC ITEM ID ����
	 * 
	 * @param ds #{itemId}, #{itemPuid}
	 * @return
	 */
	public boolean changeItemId(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.changeItemId(ds);
			mapper.refreshTCObject(ds);
			mapper.refreshTCTimeStamp(ds);
			mapper.confirmECIReceived(ds);
		}catch(Exception e){
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
            try {
            	sqlSession.getConnection().commit();
            	sqlSession.getConnection().setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			sqlSessionClose();
		}
		return true;
	}
	
	public boolean changeECIStatus(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			mapper.changeECIStatus(ds);
			mapper.refreshTCObject(ds);
			mapper.refreshTCTimeStamp(ds);
		}catch(Exception e){
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
            try {
            	sqlSession.getConnection().commit();
            	sqlSession.getConnection().setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			sqlSessionClose();
		}
		return true;
	}
	
	public boolean changeECOStatus(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			mapper.changeECOStatus(ds);
			mapper.updateEcoStatus(ds);
//			mapper.refreshTCObject(ds);
//			mapper.refreshTCTimeStamp(ds);
		}catch(Exception e){
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
            try {
            	sqlSession.getConnection().commit();
            	sqlSession.getConnection().setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			sqlSessionClose();
		}
		return true;
	}
	
	
	
	public boolean changeMECOStatus(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			mapper.changeMECOStatus(ds);
//			mapper.refreshTCObject(ds);
//			mapper.refreshTCTimeStamp(ds);
		}catch(Exception e){
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
            try {
            	sqlSession.getConnection().commit();
            	sqlSession.getConnection().setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			sqlSessionClose();
		}
		return true;
	}

	public String getDuplicateCategoryInVC(DataSet ds){
		String sMessage = null;
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			sMessage = mapper.getDuplicateCategoryInVC(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return sMessage;
	}
	
	public ArrayList<HashMap<String,String>> checkECOEPL(DataSet ds) throws Exception {
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.checkECOEPL(ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			 
			throw e;
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<String> checkEndtoEnd(DataSet ds){
		ArrayList<String> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.checkEndtoEnd(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * ECI I/F ���̺� ������Ʈ ����Ȯ�� ������Ʈ
	 * @param ds key= itemPuid
	 * @return
	 */
	public boolean confirmECIReceived(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.confirmECIReceived(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	/**
	 * IF_ECI_REVIEW_FROM_VNET ���̺� ����Ȯ�� ������Ʈ
	 * @param ds key= itemPuid
	 * @return
	 */
	public boolean confirmECIReviewReceived(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.confirmECIReviewReceived(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	public String getNextECOSerial(DataSet ds){
		String nextID = null;
		try{
			SqlSession sqlSession = getSqlSession();

			String prefix = (String) ds.getObject("prefix");

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			nextID = mapper.getNextECOSerial(prefix);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return nextID;
	}
	
	public HashMap<String,String> getECIfileInfo(DataSet ds){
		HashMap<String,String> resultMap = null;
		try{
			SqlSession sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultMap = mapper.getECIfileInfo(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultMap;
	}
	
	public String getAffectedProject(DataSet ds){
		String result = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			result = mapper.getAffectedProject(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return result;
	}

	public ArrayList<ApprovalLineData> getApprovalLine(DataSet ds){
		ArrayList<ApprovalLineData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<String> getProblemItems(DataSet ds){
		ArrayList<String> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getProblemItems(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<String> getSolutionItems(DataSet ds){
		ArrayList<String> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getSolutionItems(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * Vision Net���� �μ� ���� ������ ����
	 * @param ds
	 * @return
	 */
	public ArrayList<VnetTeamInfoData> getVnetTeamInfo(DataSet ds){
		ArrayList<VnetTeamInfoData> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			VnetTeamInfoData paramMap = (VnetTeamInfoData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getVnetTeamInfo(paramMap);
		}catch(Exception e){
			sqlSession.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * Vision Net���� �μ��ڵ�� �̸� ������ ����
	 * @param ds
	 * @return
	 */
	public ArrayList<VnetTeamInfoData> getVnetTeamNames(DataSet ds){
		ArrayList<VnetTeamInfoData> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			VnetTeamInfoData paramMap = (VnetTeamInfoData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getVnetTeamNames(paramMap);
		}catch(Exception e){
			sqlSession.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * ECI > Vision-Net �������̽� ����
	 * @param ds
	 * @return
	 */
	public boolean interfaceECI(DataSet ds) {
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.interfaceECI(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	/**
	 * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
	 * @param ds
	 * @return
	 */
	public boolean interfaceECONoToVnetEAI(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.interfaceECONoToVnetEAI(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	public boolean interfaceECONoToVnet(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.interfaceECONoToVnet(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	public boolean deleteApprovalLine(DataSet ds){
		boolean resultList = false;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.deleteApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
			resultList = true;
		}
		return resultList;
	}
	

	public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(DataSet ds){
		ArrayList<ApprovalLineData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			resultList = mapper.loadSavedUserApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
    public boolean makePartHistory(DataSet ds) {
        SqlSession session = null;
        try{
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = session.getMapper(SYMCECOMapper.class);	
            mapper.makePartHistory(ds);
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            return false;
        }finally{
            sqlSessionClose();
        }
        
        return true;
    }
	
	/**
	 * SQL�� ������ Object�� ������Ʈ �� ���� �α׾ƿ� ���� refresh
	 * refreshTCTimeStamp�� ���� ���� �ؾ� ��
	 * @param ds key= itemPuid
	 * @return
	 */
	public boolean refreshTCObject(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.refreshTCObject(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	/**
	 * SQL�� ������ Object�� ������Ʈ �� ���� �α׾ƿ� ���� refresh
	 * @param ds key= itemPuid
	 * @return
	 */
	public boolean refreshTCTimeStamp(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.refreshTCTimeStamp(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public boolean removeApprovalLine(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			mapper.removeApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean saveUserApprovalLine(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			ArrayList<ApprovalLineData> paramMapList = (ArrayList<ApprovalLineData>) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			for(ApprovalLineData map : paramMapList){
				mapper.saveUserApprovalLine(map);
			}
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public ArrayList<ApprovalLineData> loadApprovalLine(DataSet ds){
		ArrayList<ApprovalLineData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.loadApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	

	@SuppressWarnings("unchecked")
	public boolean saveApprovalLine(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			ArrayList<ApprovalLineData> paramMapList = (ArrayList<ApprovalLineData>) ds.getObject("data");

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			for(ApprovalLineData map : paramMapList){
				mapper.saveApprovalLine(map);
			}
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	public ArrayList<SYMCECOStatusData> searchEOStatus(DataSet ds){
		ArrayList<SYMCECOStatusData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOStatusData paramMap = (SYMCECOStatusData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			resultList = mapper.searchEOStatus(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
	 * [20160912][ymjang] ORA-02050: Ʈ����� 15.1.435454�� �ѹ�ǰ�, �ٸ� ���� DB�� �Ҹ���� �����Դϴ� --> ���� ����
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECREAI(DataSet ds){
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.searchECREAI(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<HashMap<String,String>> searchECR(DataSet ds){
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.searchECR(ds);
			
		}catch(Exception e){
			sqlSession.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
	 * [20160912][ymjang] ORA-02050: Ʈ����� 15.1.435454�� �ѹ�ǰ�, �ٸ� ���� DB�� �Ҹ���� �����Դϴ� --> ���� ����
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECIEAI(DataSet ds){
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.searchECIEAI(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return resultList;
	}
		
	public ArrayList<HashMap<String,String>> searchECI(DataSet ds){
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.searchECI(ds);
			
		}catch(Exception e){
			sqlSession.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return resultList;
	}
	
	public boolean updateFileName(DataSet ds) {
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.updateFileName(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	public boolean updateECIRevisionWithInterface(DataSet ds) {
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.updateECIRevisionWithInterface(ds);
			mapper.refreshTCObject(ds);
			mapper.refreshTCTimeStamp(ds);
			mapper.confirmECIReceived(ds);
		}catch(Exception e){
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
            try {
            	sqlSession.getConnection().commit();
            	sqlSession.getConnection().setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			sqlSessionClose();
		}
		return true;
	}
	
	/**
	 * SYMC ��Ʈ����� ���� ���� �߼�
	 * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
	 * @return
	 */
	public boolean sendMail(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			
			// E-Mail �߼� ���� Logging
//			Logger logger = Logger.getLogger(ECOInfoDao.class);
//			logger.info("\n############ SEND MAIL START ############");
//			logger.info("\n# the_sysid : " + ds.get("the_sysid"));
//			logger.info("\n# the_sabun : " + ds.get("the_sabun"));
//			logger.info("\n# the_title : " + ds.get("the_title"));
//			logger.info("\n# the_remark : " + ds.get("the_remark"));
//			logger.info("\n# the_tsabun : " + ds.get("the_tsabun"));
//			logger.info("\n############ SEND MAIL END   ############");
			
			mapper.sendMail(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	/**
	 * SYMC ��Ʈ����� ���� ���� �߼� (through EAI)
	 */
    public boolean sendMailEai(DataSet ds){
        SqlSession sqlSession = null;
        try{
            sqlSession = getSqlSession();
            SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
            
            // E-Mail �߼� ���� Logging
// 			Logger logger = Logger.getLogger(ECOInfoDao.class);
// 			logger.info("\n############ SEND MAIL START ############");
// 			logger.info("\n# the_sysid : " + ds.get("the_sysid"));
// 			logger.info("\n# the_sabun : " + ds.get("the_sabun"));
// 			logger.info("\n# the_title : " + ds.get("the_title"));
// 			logger.info("\n# the_remark : " + ds.get("the_remark"));
// 			logger.info("\n# the_tsabun : " + ds.get("the_tsabun"));
// 			logger.info("\n############ SEND MAIL END   ############");
         			
            mapper.sendMailEai(ds);
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSession.commit();
            sqlSessionClose();
        }
        return true;
    }
	
    /**
     * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
     * [20160912][ymjang] ORA-02050: Ʈ����� 15.1.435454�� �ѹ�ǰ�, �ٸ� ���� DB�� �Ҹ���� �����Դϴ� --> ���� ����
     * @param ds
     * @return
     */
	public TCEcoModel getEcoInfoEAI(DataSet ds) {
		SqlSession sqlSession = null;
		TCEcoModel eplModel = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			eplModel = mapper.getEcoInfoEAI(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return eplModel;
	}
	
	public TCEcoModel getEcoInfo(DataSet ds) {
		SqlSession sqlSession = null;
		TCEcoModel eplModel = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			eplModel = mapper.getEcoInfo(ds);
		}catch(Exception e){
			sqlSession.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return eplModel;
	}
	
	public ArrayList<HashMap<String,String>> getEcoWorkflowInfo(DataSet ds) {
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getEcoWorkflowInfo(ds);

		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}

	public String childrenCount(DataSet ds) {
		String childrenCount = "0";
		try{
			SqlSession sqlSession = getSqlSession();

			String bvrPuid = (String) ds.getObject("bvrPuid");

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			childrenCount = mapper.childrenCount(bvrPuid);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return childrenCount;
	}

	public String workflowCount(DataSet ds) {
		String workflowCount = "";
		try{
			SqlSession sqlSession = getSqlSession();

			String ecoNo = (String) ds.getObject("ecoNo");

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			workflowCount = mapper.workflowCount(ecoNo);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return workflowCount;
	}

	public String getEcoRevisionPuid(DataSet ds) {
		String ecoRevisionPuid = "";
		try{
			SqlSession sqlSession = getSqlSession();

			String ecoNo = (String) ds.getObject("ecoNo");

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			ecoRevisionPuid = mapper.getEcoRevisionPuid(ecoNo);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return ecoRevisionPuid;
	}
	
	//[20160912][ymjang] ORA-02050: Ʈ����� 15.1.435454�� �ѹ�ǰ�, �ٸ� ���� DB�� �Ҹ���� �����Դϴ� --> ���� ����
	public ArrayList<HashMap<String,String>> searchUserOnVnet(DataSet ds) {
		
		SqlSession sqlSession = null;
		try{
			
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			return mapper.searchUserOnVnet(ds);
				
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		
		return null;
	}
	
	/**
	 * [SR140806-002][20140725] swyoon ALC�� ����(PG_ID, PG_ID_VERSION). Replace�� �ش�Ǵ� ��츸, New�� Null�̰�, Old�� Null�� �ƴѰ�� Old���� New�� ������.
	 * 
	 * @param ds
	 */
	public void updateALC(DataSet ds) {
		try{
			
			SqlSession sqlSession = getSqlSession();
	
			sqlSession.update("com.kgm.mapper.SYMCECOMapper.updateALC", ds);
							
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}

	}	
	
	/**
	 * [SR��ȣ����(Migration�� ����)][20140820] swyoon EPL ����.
	 * 
	 * @param ds
	 * @throws Exception 
	 */	
	public void correctEPL(DataSet ds) throws Exception {
		try{
			
			SqlSession sqlSession = getSqlSession();
	
			sqlSession.update("com.kgm.mapper.SYMCECOMapper.correctEPL", ds);
							
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}

	}	
		
	/**
	 * [20160721][ymjang] java.io.NotSerializableException ���� ����
	 * [SR��ȣ����(Migration�� ����)][20140820] swyoon EPL ����.
	 * @param ds
	 * @throws Exception 
	 */	
	public ArrayList<HashMap<String, String>> getIncorrectList(DataSet ds) throws Exception {
		
		try{
			
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			ArrayList<HashMap<String, String>> rtnList = mapper.getIncorrectList(ds);
			
			return rtnList;
				
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
	}	
	
	/**
	 * [SR��ȣ����(Migration�� ����)][20140820] swyoon EPL ����.
	 * 
	 * @param ds
	 * @throws Exception 
	 */	
	@SuppressWarnings("rawtypes")
	public HashMap getEPL(DataSet ds) throws Exception {
		
		try{
			
			SqlSession sqlSession = getSqlSession();
			return sqlSession.selectOne("com.kgm.mapper.SYMCECOMapper.getEPL", ds);
				
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
	}	
	
	/**
	 * SRME:: [][20140916] Cut and paste �߻��� OccThread ���� �۾��� ���� ���� ����.
	 * 
	 * @param ds
	 * @return
	 */
	public List<Object> getChangedOcc(DataSet ds) throws Exception{
		
		try{
			
			SqlSession sqlSession = getSqlSession();
			return sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.getChangedOcc", ds);
				
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}		
	
	
	/**
	 * SRME:: [][20140919] Cut and paste �߻��� ECO_BOM_LIST�� �ִ� Occ_Threads �� ������.
	 * 
	 * @param ds
	 */
	public void updateOccthread(DataSet ds) {
		try{
			
			SqlSession sqlSession = getSqlSession();
	
			sqlSession.update("com.kgm.mapper.SYMCECOMapper.updateOccthread", ds);
							
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}

	}	
	
	/**
	 * SRME:: [][20141007] Order No�� �ߺ��Ǵ� List ����.
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<Object> getDuplicatedOrderNoList(DataSet ds) throws Exception {
		try{
			
			SqlSession sqlSession = getSqlSession();
	
			return sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.getDuplicatedOrderNoList", ds);
							
		}catch(Exception e){
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	/**
	 * SRME:: [][20141007] �ִ� Order No ����.
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public Object getMaxOrderNo(DataSet ds) throws Exception {
		try{
			
			SqlSession sqlSession = getSqlSession();
	
			return sqlSession.selectOne("com.kgm.mapper.SYMCECOMapper.getMaxOrderNo", ds);
							
		}catch(Exception e){
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	
	public String getInEcoFromECOBOMList(DataSet ds) throws Exception {
		String ecoNo = null;
		try{
			SqlSession sqlSession = getSqlSession();



			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			ecoNo = mapper.getInEcoFromECOBOMList(ds);
			
			ecoNo = sqlSession.selectOne("com.kgm.mapper.SYMCECOMapper.getInEcoFromECOBOMList", ds);
			
//			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
//			resultList = mapper.getEcoWorkflowInfo(ds);
				
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return ecoNo;
	}	
	
	/**
	 * [SR141120-043][2014.11.21][jclee] Color ID�� �����ϸ鼭 Color Section No�� �������� �ʴ� �׸� ����Ʈ ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getColorIDWarningList(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
		
			return mapper.getColorIDWarningList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}	
	
	/**
	 * [20180718][CSH]End Item���� 500�� �ʰ��� HBOM(�̱��� ����)�� Mail�뺸
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String getEcoEndItemCount(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
		
			return mapper.getEcoEndItemCount(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}	
	
	/**
	 * [SR141205-027][2014.12.16][jclee] Color ID�� ����� �׸� ����Ʈ ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getColorIDChangingList(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			
			return mapper.getColorIDChangingList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}
	
	/**
	 * [2015.02.11][jclee] IN ECO ���� ����
	 * @param ds
	 * @return
	 */
	public boolean makeBOMHistoryMaster(DataSet ds) {
		SqlSession session = null;
		try{
			session = getSqlSession();
			session.getConnection().setAutoCommit(false);
			SYMCECOMapper mapper = session.getMapper(SYMCECOMapper.class);	
			mapper.makeBOMHistoryMaster(ds);
			session.getConnection().commit();
		}catch(Exception e){
			e.printStackTrace();
			try {
				session.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			return false;
		}finally{
			sqlSessionClose();
		}
		
		return true;
	}
	
	/**
	 * [2015.01.26][jclee] Function�� ����Ǿ��ִ���(BOM�� �����Ǿ��ִ���) Ȯ��
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String isConnectedFunction(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			
			return mapper.isConnectedFunction(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}	
	
	/**
	 * [SR150213-010][2015.02.25][jclee] EPL���� Ư�� FMP ���� 1Lv Part �� Supply Mode�� P�� �����ϴ� EPL�� Car Project�� �����ϴ��� ��ȸ 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCarProjectInEPL(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			
			return mapper.getCarProjectInEPL(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}
	
	/**
	 * ECO No�� ���� �����鼭 EPL�� New Part No �� ���ԵǾ� ���� �ʴ� Part ��� ��ȯ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCANNOTGeneratedList(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			
			return mapper.getCANNOTGeneratedList(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}
	
	/**
	 * EPL Cut �� Revise�Ͽ� �ٽ� Paste�� ��� Ȯ�� (Revise �̷� ����)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCANNOTGeneratedReviseList(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			
			return mapper.getCANNOTGeneratedReviseList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}
	
	public ArrayList<SYMCECOStatusData> searchECOCorrectionHistory(DataSet ds){
		ArrayList<SYMCECOStatusData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOStatusData paramMap = (SYMCECOStatusData) ds.getObject("data");
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.searchECOCorrectionHistory(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * Part �� �������� ECO ����Ʈ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getRefEcoFromPartList(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			
			return mapper.getRefEcoFromPartList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return null;
	}
	
	/**
	 * �ߺ��� ECO ���缱 ����Ʈ
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,Object>> getEcoDupApprovalLines(DataSet ds) {
		ArrayList<HashMap<String,Object>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getEcoDupApprovalLines(ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	
	/**
	 * ���� Revision �� �߸��� Part ����Ʈ�� ������
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getOldRevNotMatchedParts(DataSet ds) {
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.getOldRevNotMatchedParts(ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	/**
	 * Order No �ߺ� üũ �߰�getECOEPL
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> duplicateOrderNoCheck(DataSet ds) {
		ArrayList<?> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			resultList = (ArrayList<?>)sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.duplicateOrderNoCheck", ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return (ArrayList<HashMap<String, Object>>) resultList;
	}
	
	/**
	 * ECO�� EPL�� ������
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 */
	public ArrayList<String> getECOEPL(DataSet ds) {
		ArrayList<?> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			resultList = (ArrayList<?>)sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.getECOEPL", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return (ArrayList<String>) resultList;
	}
	
	/**
	 * [SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
  	   1. ECO ���� Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
  	   2. Module code : FCM or RCM
  	   3. Part�� Option : Z999�� �����ϴ� ���
	 */
	public ArrayList<String> checkChassisModule(DataSet ds) {
		ArrayList<?> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			resultList = (ArrayList<?>)sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.checkChassisModule", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return (ArrayList<String>) resultList;
	}
	
	public boolean updateStep(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			mapper.updateStep(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			return false;
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	public String getNMCDUpdatePartList(DataSet ds) {
		String partList = "";
		try{
			SqlSession sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			partList = mapper.getNMCDUpdatePartList(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return partList;
	}
	
	public String getAdmin(DataSet ds) {
		String str = "";
		try{
			SqlSession sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			str = mapper.getAdmin(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return str;
	}
	
	public ArrayList<HashMap<String,String>> getProjectCodeList(DataSet ds) {
		ArrayList<HashMap<String, String>> rtnList = null;
		
		try{
			SqlSession sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			rtnList = mapper.getProjectCodeList(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return rtnList;
	}
	
	public ArrayList<HashMap<String, Object>> notConnectedFunctionList(DataSet ds) {
		ArrayList<?> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			resultList = (ArrayList<?>)sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.notConnectedFunctionList", ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return (ArrayList<HashMap<String, Object>>) resultList;
	}
	
	public ArrayList<HashMap<String,String>> checkPowerTraing(DataSet ds) throws Exception {
		ArrayList<HashMap<String,String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);
			resultList = mapper.checkPowerTraing(ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			 
			throw e;
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
//	public ArrayList<HashMap<String, String>> checkPowerTraing(DataSet ds) {
//		ArrayList<?> resultList = null;
//		SqlSession sqlSession = null;
//		try{
//			sqlSession = getSqlSession();
//
//			resultList = (ArrayList<?>)sqlSession.selectList("com.kgm.mapper.SYMCECOMapper.checkPowerTraing", ds);
//
//		}catch(Exception e){
//			e.printStackTrace();
//			
//			// [20160928][ymjang] log4j�� ���� ���� �α� ���
//			LogUtil.error(e.getMessage(), ds);
//			
//		}finally{
//			sqlSessionClose();
//		}
//		return (ArrayList<HashMap<String, String>>) resultList;
//	}
	

}
