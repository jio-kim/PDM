package com.kgm.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.ECOHistoryMapper;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;

/**
 * [20161102][ymjang] ORA-03113: 통신 채널에 EOF 가 있습니다 ORA-02063: line가 선행됨 (LINK_001_VNET로 부터) 오류 수정
 */
public class ECOHistoryDao extends AbstractDao {

    public List<String> selectUserWorkingECO(DataSet ds){
		List<String> result = null;
		try{
			SqlSession session = getSqlSession();
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
			result = mapper.selectUserWorkingECO(ds);
			//result = session.selectList("com.kgm.mapper.VariantMapper.getVariantValueDesc", ds);
			
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
    
    public List<String> selectEPLData(DataSet ds) {
        List<String> result = null;
        try{
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectEPLData(ds);
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }
        return result;
    }
    
    /*
    @SuppressWarnings("unchecked")
    public Boolean insertECOBOMWork(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        String ecoNo = ds.getString("ecoNo");
        String userId = ds.getString("userId");
        HashMap<String, SYMCBOMEditData> editData = (HashMap<String, SYMCBOMEditData>)ds.get("bomEditData");
        Iterator<SYMCBOMEditData> iterator = editData.values().iterator();
        try{
            session.getConnection().setAutoCommit(false);
            while(iterator.hasNext()) {
                SYMCBOMEditData bomEditData = iterator.next();
                bomEditData.setEcoNo(ecoNo);
                bomEditData.setUserId(userId);
                int rowCnt = mapper.insertECOBOMWork(bomEditData).intValue();
                if(rowCnt == 0) {
                    throw new Exception("Not appliable ECO Work..");
                }
                //session.insert("com.kgm.sqlmap.ECOHistoryMap.insertECOBOMWork", bomEditData);
            }
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
            try {
                mapper.applyBOMList(ecoNo);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }*/
    
    /**
     * ECO-B(DWG) List
     * 
     * @method selectECODwgList 
     * @date 2013. 2. 20.
     * @param
     * @return List<SYMCECODwgData>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCECODwgData> selectECODwgList(DataSet ds){
        List<SYMCECODwgData> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectECODwgList(ds);            
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    /**
     * ECO-B Properties Update
     * 
     * @method updateECOEPLProperties 
     * @date 2013. 3. 5.
     * @param
     * @return Boolean
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public Boolean updateECODwgProperties(DataSet ds){
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        ArrayList<SYMCECODwgData> editData = (ArrayList<SYMCECODwgData>)ds.get("bomEditData");        
        try{
            session.getConnection().setAutoCommit(false);
            for (int i = 0; editData != null && i < editData.size(); i++) {
                mapper.updateECODwgProperties(editData.get(i));   
            }
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * ECO-C(EPL) List
     * 
     * @method selectECOEplList 
     * @date 2013. 2. 20.
     * @param
     * @return List<SYMCECODwgData>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCBOMEditData> selectECOEplList(DataSet ds){
        List<SYMCBOMEditData> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectECOEplList(ds);            
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    public List<HashMap<String,String>> selectInECOlList(DataSet ds){
        List<HashMap<String,String>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectInECOlList(ds);            
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    /**
     * ECO-C Properties Update
     * 
     * @method updateECOEPLProperties 
     * @date 2013. 3. 5.
     * @param
     * @return Boolean
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public Boolean updateECOEPLProperties(DataSet ds){
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        ArrayList<SYMCBOMEditData> editData = (ArrayList<SYMCBOMEditData>)ds.get("bomEditData");        
        try{
            session.getConnection().setAutoCommit(false);
            for (int i = 0; editData != null && i < editData.size(); i++) {
                mapper.updateECOEPLProperties(editData.get(i));   
            }
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * ECO-D(PartList) 테이블 리스트
     * 
     * @method selectECOPartList 
     * @date 2013. 3. 8.
     * @param
     * @return List<SYMCECODwgData>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCPartListData> selectECOPartList(DataSet ds) {
        List<SYMCPartListData> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectECOPartList(ds);            
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    /**
     * ECO-D Properties Update
     * 
     * @method updateECOPartListProperties 
     * @date 2013. 3. 8.
     * @param
     * @return Boolean
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public Boolean updateECOPartListProperties(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        ArrayList<SYMCPartListData> editData = (ArrayList<SYMCPartListData>)ds.get("bomEditData");        
        try{
            session.getConnection().setAutoCommit(false);
            for (int i = 0; editData != null && i < editData.size(); i++) {
                mapper.updateECOPartListProperties(editData.get(i));   
            }
            session.getConnection().commit();
        }catch(Exception e){
            e.printStackTrace();
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            return Boolean.FALSE;
        }finally{
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    /*public Boolean reviseBOMPart(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        try{
            session.getConnection().setAutoCommit(false);
            mapper.reviseBOMPart(ds);
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

    public Boolean saveAsBOMPart(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        try{
            session.getConnection().setAutoCommit(false);
            mapper.saveAsBOMPart(ds);
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
    }*/
    
    // [20161102][ymjang] ORA-03113: 통신 채널에 EOF 가 있습니다 ORA-02063: line가 선행됨 (LINK_001_VNET로 부터) 오류 수정
    // [미사용]
    public Boolean extractEPL(DataSet ds) {
        SqlSession session = getSqlSession();
        try{            
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            mapper.extractEPL(ds);
        }catch(Exception e){
            e.printStackTrace();
            session.rollback();

            // [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        }finally{
        	session.commit();
            sqlSessionClose();
        }        
        return Boolean.TRUE;
    }
    /*
    public Boolean extractEPL(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        mapper.extractEPL(ds);
        sqlSessionClose();
        return Boolean.TRUE;
    }
    */
    
    // [20161102][ymjang] ORA-03113: 통신 채널에 EOF 가 있습니다 ORA-02063: line가 선행됨 (LINK_001_VNET로 부터) 오류 수정
    public Boolean generateECO(DataSet ds) {
    	System.out.println("  generateECO start ");
    	SqlSession session = getSqlSession();
        try{            
        	ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        	mapper.generateECO(ds);
        }catch(Exception e){
            e.printStackTrace();
            session.rollback();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

        }finally{
        	session.commit();
        	sqlSessionClose();
        }        
        System.out.println("  generateECO end ");
        return Boolean.TRUE;
    }
    /*
    public Boolean generateECO(DataSet ds) {
    	SqlSession session = getSqlSession();
    	ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    	mapper.generateECO(ds);
    	sqlSessionClose();
    	return Boolean.TRUE;
    }
    */
    
    // [20161102][ymjang] ORA-03113: 통신 채널에 EOF 가 있습니다 ORA-02063: line가 선행됨 (LINK_001_VNET로 부터) 오류 수정
    public Boolean isECOEPLChanged(DataSet ds) {
        try{
        	System.out.println("  isECOEPLChanged start ");
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            String result = mapper.isECOEPLChanged(ds);
            System.out.println("  change check 1 ");
            if(result != null && result.equals("TRUE")) {
                return Boolean.TRUE;
            }
            /**
             * [20180315] Released 되지 않은 다른 ECO에서 대상 ECO(파라메터 입력값)의 부품에 대해
             * 변경이 있는 경우 TRUE 를 반환한다.
             * */
            System.out.println("  change check 2 ");
			result = mapper.isECOEPLChangedByAnotherECO(ds);

			if(result != null && result.equals("TRUE")) {
				return Boolean.TRUE;
			}
			
            
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);

        }finally{
            sqlSessionClose();
            System.out.println("  isECOEPLChanged end ");
        }  
        
        return Boolean.FALSE;
    }
    /*
    public Boolean isECOEPLChanged(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        String result = mapper.isECOEPLChanged(ds);
        if(result.equals("TRUE")) {
            return Boolean.TRUE;
        }
        sqlSessionClose();
        return Boolean.FALSE;
    }
    */
    public List<SYMCBOMEditData> selectECOBOMList(DataSet ds){
        List<SYMCBOMEditData> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectECOBOMList(ds);            
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    // Demon
    public List<HashMap<String, String>> selectECODemonTarget() throws Exception {
        List<HashMap<String, String>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectECODemonTarget();            
        }catch(Exception e){
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
        	
            throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }

    public List<HashMap<String, String>> selectOccurrenceECO(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectOccurrenceECO(ds);            
        }catch(Exception e){
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    public Integer updateOccurrenceECOApplied(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try{
            return mapper.updateOccurrenceECOApplied(ds);   
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        }finally{
            session.commit();
            sqlSessionClose();
        }
    }
    
    public List<HashMap<String, String>> selectReleasedECO(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
            result = mapper.selectReleasedECO(ds);            
        }catch(Exception e){
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }
    
    public Boolean insertECOInfoToVPM(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            mapper.insertECOInfoToVPM(ds);
            mapper.updateECOInfoInterfacedToVPM(ds);
            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    public List<HashMap<String, String>> selectECOEplCorrectionList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectECOEplCorrectionList(ds);            
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> selectECOEplCOSModeCompareList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectECOEplCOSModeCompareList(ds);            
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> selectUnGeneratedCOPartList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectUnGeneratedCOPartList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> selectECOChangeCause(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectECOChangeCause(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
	public boolean deleteECOChangeCause(DataSet ds){
		boolean resultList = false;
		try{
			SqlSession sqlSession = getSqlSession();
			ECOHistoryMapper mapper = sqlSession.getMapper(ECOHistoryMapper.class);	
			mapper.deleteECOChangeCause(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
			resultList = true;
		}
		return resultList;
	}
	
    public Boolean insertECOChangeCause(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            mapper.insertECOChangeCause(ds);
            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    public List<HashMap<String, String>> selectECOEplEndItemList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectECOEplEndItemList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> selectECOEplEndItemNameList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectECOEplEndItemNameList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> selectECOBOMListEndItemNameList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.selectECOBOMListEndItemNameList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
	public boolean deleteECOEplEndItemList(DataSet ds){
		boolean resultList = false;
		try{
			SqlSession sqlSession = getSqlSession();
			ECOHistoryMapper mapper = sqlSession.getMapper(ECOHistoryMapper.class);	
			mapper.deleteECOEplEndItemList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
			resultList = true;
		}
		return resultList;
	}
	
    public Boolean insertECOEplEndItemList(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            mapper.insertECOEplEndItemList(ds);
            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * [SR151204-016][20151209][jclee] DWG, EPL Change Desc에 Revision의 Change Desc 입력용 Query
     */
    public String getChangeDescription(DataSet ds) throws Exception {
		String result = null;
		try{
			SqlSession session = getSqlSession();
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
			result = mapper.getChangeDescription(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
    
    public String getECOAdminCheckCommonMemo() throws Exception {
    	String result = null;
    	try{
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getECOAdminCheckCommonMemo();
    	}catch(Exception e){
    		e.printStackTrace();
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
    		
    	}finally{
    		sqlSessionClose();
    	}
    	
    	return result;
    }
    
    public Boolean insertECOAdminCheckCommonMemo(DataSet ds) throws Exception {
    	SqlSession session = getSqlSession();
    	ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
    	try {
    		session.getConnection().setAutoCommit(false);
    		mapper.insertECOAdminCheckCommonMemo(ds);
    		session.getConnection().commit();
    	} catch(Exception e) {
    		try {
    			session.getConnection().rollback();
    		} catch (SQLException e1) {
    			e1.printStackTrace();
    		}
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	} finally {
    		sqlSessionClose();
    	}
    	
    	return Boolean.TRUE;
    }
    
    /**
     * 설계변경 현황 Option Category  리스트
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getEcoStatusOptCategoryList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getEcoStatusOptCategoryList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * 변경현황 변경리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getEcoStatusChangeList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getEcoStatusChangeList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public List<HashMap<String, String>> getEcoNullValueList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getEcoNullValueList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * SYS_GUID 를 가져옴
     * @param ds
     * @return
     * @throws Exception
     */
    public String getSysGuid(DataSet ds) throws Exception {
		String result = null;
		try{
			SqlSession session = getSqlSession();
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
			result = mapper.getSysGuid(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
    
    public  List<String> getMultiSysGuidList(DataSet ds) throws Exception {
    	List<String> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getMultiSysGuidList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    
    /**
     * 설계변경 현황 기준정보리스트 저장
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Boolean insertRptStdInfo(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
            
			for( HashMap<String,String> map : dataList){
	            mapper.insertRptStdInfo(map);
            }

            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * 설계변경 현황 검토 리스트 저장
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Boolean insertRptChgReview(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
            
			for( HashMap<String,String> map : dataList){
	            mapper.insertRptChgReview(map);
            }

            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Boolean insertRptList(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
            
			for( HashMap<String,String> map : dataList){
	            mapper.insertRptList(map);
            }

            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
	/**
	 * 설계변경 현황 기준정보리스트 조회(중복된 여부 확인용)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<String> getDupRptInfoList(DataSet ds) throws Exception {
    	List<String> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getDupRptInfoList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * 설계변경 현황 정보 삭제
     * @param ds
     * @return
     * @throws Exception
     */
	public Boolean deleteRptChangeList(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);
			
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);	
			
			//설계변경 리스트 정보 삭제
			mapper.deleteRptList(ds);
			//Option Condition 정보 삭제
			mapper.deleteRptOptCnd(ds);
			// 검토 정보 삭제
			mapper.deleteChgReview(ds);
			//기준정보 삭제
			mapper.deleteRptStdInfo(ds);

			session.getConnection().commit();
		}catch(Exception e){
			try {
				session.getConnection().rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			 
	        throw e;
		}finally{
			sqlSessionClose();
		}
		return Boolean.TRUE;
	}
	
    /**
     * 변경 현황 전체리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getEcoTotalStatusList(DataSet ds) throws Exception {
    	List<HashMap<String, Object>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getEcoTotalStatusList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    
    /**
     * 변경 현황 기준정보리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getEcoStatusStdList(DataSet ds) throws Exception {
    	List<HashMap<String, Object>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getEcoStatusStdList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    

    /**
     * 설계변경 현황 변경 리스트 수정
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public Boolean updateRptList(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
        	ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
            session.getConnection().setAutoCommit(false);
			for( HashMap<String,String> map : dataList)
				mapper.updateRptList(map);
            
            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * 설계변경 현황 변경 검토  Row 삭제
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Boolean deleteRpListWithPuid(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);	
			
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
			for( HashMap<String,String> map : dataList)
				mapper.deleteRpListWithPuid(map);
            
			session.getConnection().commit();
		}catch(Exception e){
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
            throw e;
		}finally{
			sqlSessionClose();
		}
		return Boolean.TRUE;
	}
    
    /**
     * 설계변경 현황 변경 검토  Row 삭제
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public Boolean deleteChgReviewWithPuid(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);	
			
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
			for( HashMap<String,String> map : dataList)
			{
				// 삭제하려는 Option Category 가 변경리스트에 존재하면 삭제하지 않음
				int opCategoryCnt = mapper.getOpCategoryCount(map);
				if(opCategoryCnt > 0)
					continue;
				//변경 검토 리스트 삭제
				mapper.deleteChgReviewWithPuid(map);
				//변경 검토 리스트의 Condition 삭제
				mapper.deleteRptOptCndWithPuid(map);
			}
            
			session.getConnection().commit();
		}catch(Exception e){

            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		return Boolean.TRUE;
	}
	
    /**
     * Project 의 G Model 코드를 가져옴
     * @param ds
     * @return
     * @throws Exception
     */
    public String getGmodelWithProject(DataSet ds) throws Exception {
		String result = null;
		try{
			SqlSession session = getSqlSession();
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
			result = mapper.getGmodelWithProject(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
    
    

    /**
	 * 설계변경 현황 변경 리스트 수정
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public Boolean updateRptStdInfo(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
		try {
			mapper.updateRptStdInfo(ds);
		} catch (Exception e) {
			try {
				session.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		} finally {
            session.commit();
			sqlSessionClose();
		}

		return Boolean.TRUE;
	}
	
	/**
	 * OSPEC Revision 정보 리스트
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getOspecRevList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getOspecRevList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * Project 의 G Model 코드를 가져옴
     * @param ds
     * @return
     * @throws Exception
     */
    public String getEPLJobPuid(DataSet ds) throws Exception {
		String result = null;
		try{
			SqlSession session = getSqlSession();
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
			result = mapper.getEPLJobPuid(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
    
    
    /**
     * 설계변경현황 등록 대상 변경정보 리스트
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getChangeTargetEPLList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getChangeTargetEPLList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * 설계변경현황 EPL 리스트(중복포함)
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getAllChangeTargetEPLList(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * 설계변경현황 추가 검토 옵션 생성
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public Boolean insertRptOptCondition(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
            
			for( HashMap<String,String> map : dataList){
	            mapper.insertRptOptCondition(map);
            }

            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
	
	
    /**
     * ECO 현황 변경검토 Category 정보 리스트
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getRptChgReviewCategory(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getRptChgReviewCategory(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }

	
    /**
     * 설계변경 현황 변경 검토 리스트 Count
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public int getRptReviewCount(DataSet ds) throws Exception {
    	int result = 0;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getRptReviewCount(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    
    /**
     * 설계변경 현황 정보 삭제, 기준정보는 제외함
     * @param ds
     * @return
     * @throws Exception
     */
	public Boolean deleteRptReviewList(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);
			
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);	
			
			//설계변경 리스트 정보 삭제
			mapper.deleteRptList(ds);
			//Option Condition 정보 삭제
			mapper.deleteRptOptCnd(ds);
			// 검토 정보 삭제
			mapper.deleteChgReview(ds);

			session.getConnection().commit();
		}catch(Exception e){
			try {
				session.getConnection().rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			 
	        throw e;
		}finally{
			sqlSessionClose();
		}
		return Boolean.TRUE;
	}
	
    /**
     * 설계변경 현황 기준정보 With Master PUID
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getRptStdInformWithPuid(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getRptStdInformWithPuid(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * Function EPL Check 등록
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Boolean insertFncEplCheck(DataSet ds) throws Exception {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);        
        try {
            session.getConnection().setAutoCommit(false);
            
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
            
			for( HashMap<String,String> map : dataList){
	            mapper.insertFncEplCheck(map);
            }

            session.getConnection().commit();
        } catch(Exception e) {
            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
    
    /**
     * Function EPL 리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFncEplCheckList(DataSet ds) throws Exception {
    	List<HashMap<String, Object>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getFncEplCheckList(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    
    /**
     * Function EPL Check 리스트 삭제
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public Boolean deleteFncEpl(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);
			ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);	
			
            ArrayList<HashMap<String,String>> dataList = (ArrayList<HashMap<String,String>>)ds.get("DATA_LIST");
			for( HashMap<String,String> map : dataList)
			{
				mapper.deleteFncEpl(map);
			}
            
			session.getConnection().commit();
		}catch(Exception e){

            try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		return Boolean.TRUE;
	}
	
	
    /**
	 * Function EPL Check 수정
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public Boolean updateFncEplCheck(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
		try {
			mapper.updateFncEplCheck(ds);
		} catch (Exception e) {
			try {
				session.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		} finally {
            session.commit();
			sqlSessionClose();
		}

		return Boolean.TRUE;
	}
	
    /**
     * 변경 현황 전체리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFncEplCheckStatusList(DataSet ds) throws Exception {
    	List<HashMap<String, Object>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.getFncEplCheckStatusList(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
	
    //////////// ECO Eci Ecr I/F 신규 기능을 위해 추가
	
    public String searchEciNo(DataSet ds) throws Exception {
    	String result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.searchEciNo(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public String searchEcrNo(DataSet ds) throws Exception {
    	String result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.searchEcrNo(ds);
    	}catch(Exception e){
			LogUtil.error(e.getMessage(), ds);
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    public Boolean updateEci(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
		try {
			mapper.updateEci(ds);
		} catch (Exception e) {
			try {
				session.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		} finally {
            session.commit();
			sqlSessionClose();
		}

		return Boolean.TRUE;
	}
    
    public Boolean updateEcr(DataSet ds) throws Exception {
		SqlSession session = getSqlSession();
		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
		try {
			mapper.updateEcr(ds);
		} catch (Exception e) {
			try {
				session.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		} finally {
            session.commit();
			sqlSessionClose();
		}

		return Boolean.TRUE;
	}
    
    public List<HashMap<String, String>> datasetCheck(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try{            
    		SqlSession session = getSqlSession();
    		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
    		result = mapper.datasetCheck(ds);
    	}catch(Exception e){
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	}finally{
    		sqlSessionClose();
    	}        
    	return result;
    }
    
}
