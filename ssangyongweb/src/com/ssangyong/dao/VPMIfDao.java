package com.ssangyong.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.VPMIfMapper;

public class VPMIfDao extends AbstractDao {    

    /**
     * VPM Report Dialog 리스트용
     * 
     * @method getValidateVPMList 
     * @date 2013. 5. 29.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidateVPMList(DataSet ds) throws Exception {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getValidateVPMList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * VehPart Report Dialog 리스트용
     * 
     * @method getValidateVehPartList 
     * @date 2013. 5. 29.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidateVehPartList(DataSet ds) throws Exception {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getValidateVehPartList(ds);
        } catch (Exception e) {
            e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIfVehPart() throws Exception {
        List<HashMap> result = null;
        try {            
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getIfVehPart();            
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
        	
            throw e;            
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIfVehPartFileList(DataSet ds) throws Exception {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getIfVehPartFileList(ds);
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    public Integer getExistVPMPartCnt(DataSet ds) throws Exception {        
        Integer cnt = 0;
        SqlSession session = null;
        try {
            session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            cnt = mapper.getExistVPMPartCnt(ds);
        } catch (Exception e) {
        	session.rollback();
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
        	session.commit();
            sqlSessionClose();
        }
        return cnt;
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM  ECO I/F Part가 VALIDE한지 확인
     * 
     * @method getECOValideYn 
     * @date 2013. 5. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getECOValideYn(DataSet ds) throws Exception {
        String result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getECOValideYn(ds);
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    public Integer getExistDRNameCnt(DataSet ds) throws Exception {        
        Integer cnt = 0;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            cnt = mapper.getExistDRNameCnt(ds);
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
        return cnt;
    }
    
    
    
    /**
     * IF_ECO_INFO_FROM_VPM - I/F DB 상태 변경
     * 
     * @method updateVPMStatus 
     * @date 2013. 4. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */    
    public void updateVPMStatus(DataSet ds) throws Exception {
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            mapper.updateVPMStatus(ds);           
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * ECO Validate 수행 후  IF_VEHPART 테이블에 유효성 체크 여부를 등록한다. - IF_VEHPART
     * 
     * @method updateECOVehPartValide 
     * @date 2013. 5. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void updateECOVehPartValide(DataSet ds) throws Exception {
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            mapper.updateECOVehPartValide(ds);           
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * ECO VEHPART Validate 후 나머지 (NON EPL)VEHPART Validate - IF_VEHPART
     * 
     * @method updateNotECOVehPartValide 
     * @date 2013. 5. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void updateNotECOVehPartValide() throws Exception {
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            mapper.updateNotECOVehPartValide();           
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * VPM I/F Damon 대상 리스트
     * 
     * @method getIFValidateVPMList 
     * @date 2013. 5. 21.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIFValidateVPMList(DataSet ds) throws Exception {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getIFValidateVPMList(ds);
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * VPM I/F Damon 대상 Update
     * 
     * @method updateVPMValide 
     * @date 2013. 5. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void updateVPMValide(DataSet ds) throws Exception {
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            mapper.updateVPMValide(ds);      
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }    
    
    /**
     * IF_VEHPART DB Status 변경
     * 
     * @method updateVehStatus 
     * @date 2013. 4. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */    
    public void updateVehStatus(DataSet ds) throws Exception {
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            mapper.updateVehStatus(ds);           
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * List 를 가지고 IF_VEHPART 상태를 일괄 변경한다.
     * 
     * VehPartReportDialog 에서 일괄 상태 변경에 사용
     */ 
    @SuppressWarnings("unchecked")
    public void updateListVehStatus(DataSet ds) throws Exception {        
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateVehStatus(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * ITEM ID를 가지고 전체 STATUS LIST를 얻어온다.
     * 
     * @method getIfVehPartStatus 
     * @date 2013. 6. 3.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    public List<String> getIfVehPartStatus(DataSet ds) throws Exception {
        List<String> result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getIfVehPartStatus(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - 작업자 설정
     * 
     * @method updateVPMCustomSetWorker 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateVPMCustomSetWorker(DataSet ds) throws Exception {    
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateVPMCustomSetWorker(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM -통보처리
     * 
     * @method updateVPMCustomNoticeProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateVPMCustomNoticeProcess(DataSet ds) throws Exception {
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateVPMCustomNoticeProcess(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - 완료처리
     * 
     * @method updateVPMCustomCompleteProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateVPMCustomCompleteProcess(DataSet ds) throws Exception {
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateVPMCustomCompleteProcess(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - 유져 Skip
     * 
     * @method updateVPMCustomUserSkip 
     * @date 2013. 6. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateVPMCustomUserSkip(DataSet ds) throws Exception {
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateVPMCustomUserSkip(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * TC Report Dialog 리스트용
     * 
     * @method getValidateTCList 
     * @date 2013. 5. 29.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidateTCList(DataSet ds) throws Exception {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            result = mapper.getValidateTCList(ds);
        } catch (Exception e) {
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            e.printStackTrace();
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - 작업자 설정
     * 
     * @method updateVPMCustomSetWorker 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateTCCustomSetWorker(DataSet ds) throws Exception {    
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateTCCustomSetWorker(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_TC -통보처리
     * 
     * @method updateTCCustomNoticeProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateTCCustomNoticeProcess(DataSet ds) throws Exception {
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateTCCustomNoticeProcess(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - 완료처리
     * 
     * @method updateTCCustomCompleteProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateTCCustomCompleteProcess(DataSet ds) throws Exception {
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateTCCustomCompleteProcess(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - 유져 Skip
     * 
     * @method updateTCCustomUserSkip 
     * @date 2013. 6. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked" })
    public void updateTCCustomUserSkip(DataSet ds) throws Exception {
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // 트랜잭션 처리..
            VPMIfMapper mapper = session.getMapper(VPMIfMapper.class);
            List<DataSet> list = (List<DataSet>)ds.get("UPDATE_LIST");
            if(list == null) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {                       
                mapper.updateTCCustomUserSkip(list.get(i));
            }
            session.getConnection().commit();   // Commit
        } catch (Exception e) {
            session.getConnection().rollback(); // Roolback
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
}
