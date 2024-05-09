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

public class MECOHistoryDao extends AbstractDao {

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
    
    public Boolean extractEPL(DataSet ds) {
        SqlSession session = getSqlSession();
        ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
        mapper.extractEPL(ds);
        sqlSessionClose();
        return Boolean.TRUE;
    }
    
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
            throw e;
        } finally {
            sqlSessionClose();
        }
        
        return Boolean.TRUE;
    }
    
}
