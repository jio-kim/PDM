package com.kgm.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.mapper.DCSVisionNetMapper;
import com.kgm.mapper.SMTestMapper;

public class SMTestDao extends AbstractDao {

	public ArrayList<HashMap<String, Object>> getEngPSRepublishingTarget(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getEngPSRepublishingTarget(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getShopLineInfo(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getShopLineInfo(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getMigEngList(){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getMigEngList();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getMigRepublishingList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getMigRepublishingList(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getWeldOPItemList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getWeldOPItemList(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getOperationListForWorkCount(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getOperationListForWorkCount(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getFuncListByProduct(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getFuncListByProduct(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getInEcoBP(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getInEcoBP(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
    public int getDCSWorkflowHistoryMaxSeq() {
    	SqlSession session = getSqlSession();
        int seq = 0;
        try {
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);
            seq = mapper.getDCSWorkflowHistoryMaxSeq();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqlSessionClose();
        }

        return seq;
    }
	
	public Boolean insertDCSWorkflowHistory(DataSet ds){
		SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
            mapper.insertDCSWorkflowHistory(ds);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
	}
	
	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSVisionNetMapper mapper = sqlSession.getMapper(DCSVisionNetMapper.class);
			resultList = mapper.selectVNetUserList(dataSet);
		} catch (Exception e) {
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			sqlSession.commit();
			sqlSessionClose();
		}

		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getProductList(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getProductList(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
    public Boolean createEndItemList(DataSet dataSet) throws Exception
    {
    	
    	SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
        	mapper.createEndItemList(dataSet);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
        
    }
	
    public Boolean deleteMfgSpec(DataSet dataSet) throws Exception
    {
    	
    	SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
        	mapper.deleteMfgSpec(dataSet);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
    }
    
    public Boolean insertMfgSpec(DataSet dataSet) throws Exception
    {
    	
    	SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
        	mapper.insertMfgSpec(dataSet);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
    }
    
	public ArrayList<HashMap<String, Object>> getEndItemListforNameValidation(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getEndItemListforNameValidation(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getDeleteTargetItemList(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getDeleteTargetItemList(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
    
    public Boolean sendMail(DataSet dataSet) throws Exception
    {
    	
    	SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
        	mapper.sendMail(dataSet);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
    }

	public ArrayList<HashMap<String, Object>> getFunctions(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getFunctions(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
    
	public String getJobPuid(){
		String jobPuid = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			jobPuid = mapper.getJobPuid();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return jobPuid;
	}
	
    public Boolean createEPL(DataSet dataSet) throws Exception
    {
    	
    	SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
        	mapper.createEPL(dataSet);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
        
    }
	
	public Boolean insertProcessedParent(DataSet ds){
		SqlSession session = getSqlSession();
        try{
            session.getConnection().setAutoCommit(false);
            
        	SMTestMapper mapper = session.getMapper(SMTestMapper.class);        
            mapper.insertProcessedParent(ds);
            
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        return Boolean.TRUE;
	}
	
	public ArrayList<HashMap<String, Object>> getProcessedParent(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getProcessedParent(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
    
	public ArrayList<HashMap<String, Object>> getEPLInfo(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getEPLInfo(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	
	
	
	
	
	
	
	
	public String getJobPUIDNO(DataSet dataSet){
		String resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getJobPUIDNO(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	public String getCountItems(DataSet dataSet){
		String resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getCountItems(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	public ArrayList<HashMap<String, Object>> getItemMasterInfo(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getItemMasterInfo(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getSpecInfo(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getSpecInfo(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	//
	// 생기쪽 FTP 로 전송 되지 못한 CGR 파일 조회 쿼리
	public ArrayList<HashMap<String, Object>> getNotUploadCGRFile(DataSet dataSet){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			resultList = mapper.getNotUploadCGRFile(dataSet);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public boolean setLicenseLevel(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		boolean result = true;
		SqlSession sqlSession = null;
		try {
//			SqlSession sqlSession = getSqlSession();
			sqlSession = getOtherSession("infodba");
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			mapper.setLicenseLevel(ds);
			sqlSession.getConnection().commit();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
//			sqlSessionClose();
			if(sqlSession != null){
				sqlSession.close();
			}
		}
		return result;
	}
	
	public boolean setUserInactive(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		boolean result = true;
		SqlSession sqlSession = null;
		try {
//			SqlSession sqlSession = getSqlSession();
			sqlSession = getOtherSession("infodba");
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			mapper.setUserInactive(ds);
//			mapper.refreshTCObject(ds);
//			mapper.refreshTCTimeStamp(ds);
			sqlSession.getConnection().commit();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
//			sqlSessionClose();
			if(sqlSession != null){
				sqlSession.close();
			}
		}
		return result;
	}
	
	public boolean setGroupMemberInactive(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		boolean result = true;
		SqlSession sqlSession = null;
		try {
//			SqlSession sqlSession = getSqlSession();
			sqlSession = getOtherSession("infodba");
			SMTestMapper mapper = sqlSession.getMapper(SMTestMapper.class);
			mapper.setGroupMemberInactive(ds);
			sqlSession.getConnection().commit();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
//			sqlSessionClose();
			if(sqlSession != null){
				sqlSession.close();
			}
		}
		return result;
	}
	
}
