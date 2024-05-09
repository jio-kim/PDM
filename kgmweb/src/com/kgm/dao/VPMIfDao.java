package com.kgm.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.VPMIfMapper;

public class VPMIfDao extends AbstractDao {    

    /**
     * VPM Report Dialog ����Ʈ��
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * VehPart Report Dialog ����Ʈ��
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

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
        	session.commit();
            sqlSessionClose();
        }
        return cnt;
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM  ECO I/F Part�� VALIDE���� Ȯ��
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
        return cnt;
    }
    
    
    
    /**
     * IF_ECO_INFO_FROM_VPM - I/F DB ���� ����
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * ECO Validate ���� ��  IF_VEHPART ���̺� ��ȿ�� üũ ���θ� ����Ѵ�. - IF_VEHPART
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * ECO VEHPART Validate �� ������ (NON EPL)VEHPART Validate - IF_VEHPART
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage());
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * VPM I/F Damon ��� ����Ʈ
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * VPM I/F Damon ��� Update
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }    
    
    /**
     * IF_VEHPART DB Status ����
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        } finally {
            sqlSessionClose();
        }
    }
    
    /**
     * List �� ������ IF_VEHPART ���¸� �ϰ� �����Ѵ�.
     * 
     * VehPartReportDialog ���� �ϰ� ���� ���濡 ���
     */ 
    @SuppressWarnings("unchecked")
    public void updateListVehStatus(DataSet ds) throws Exception {        
        SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * ITEM ID�� ������ ��ü STATUS LIST�� ���´�.
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - �۾��� ����
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM -�뺸ó��
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - �Ϸ�ó��
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - ���� Skip
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * TC Report Dialog ����Ʈ��
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
        	
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
        	
            e.printStackTrace();
            throw e;
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - �۾��� ����
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_TC -�뺸ó��
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - �Ϸ�ó��
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - ���� Skip
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
            session.getConnection().setAutoCommit(false);   // Ʈ����� ó��..
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
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }        
    }
}
