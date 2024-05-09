package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.VPMIfDao;

public class VPMIfService {
    
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getValidateVPMList(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getValidateVehPartList(ds);
    }
    
    /**
     * I/F VEHPART ��ȸ
     * 
     * @method getIfVehPart 
     * @date 2013. 5. 7.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIfVehPart() throws Exception {
        VPMIfDao dao = new VPMIfDao();
        return dao.getIfVehPart();
    }
    
    /**
     * I/F VEHPART FILE ��ȸ
     * 
     * @method getIfVehPartFileList 
     * @date 2013. 5. 7.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIfVehPartFileList(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        return dao.getIfVehPartFileList(ds);
    }
    
    /**
     * VPM�� PartNo�� �����ϴ��� Ȯ��
     * 
     * @method getExistVPMPartCnt 
     * @date 2013. 5. 4.
     * @param
     * @return Integer
     * @exception
     * @throws
     * @see
     */
    public Integer getExistVPMPartCnt(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        return dao.getExistVPMPartCnt(ds);
    }   
    
    
    /**
     * DR Name���� Check
     * 
     * @method getExistVPMPartCnt 
     * @date 2013. 5. 4.
     * @param
     * @return Integer
     * @exception
     * @throws
     * @see
     */
    public Integer getExistDRNameCnt(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        return dao.getExistDRNameCnt(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getIFValidateVPMList(ds);
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
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMValide(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getECOValideYn(ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - DB Status ����
     * 
     * @method updateVPMStatus 
     * @date 2013. 5. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void updateVPMStatus(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMStatus(ds);
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
        VPMIfDao dao = new VPMIfDao();
        dao.updateECOVehPartValide(ds);
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
        VPMIfDao dao = new VPMIfDao();
        dao.updateNotECOVehPartValide();
    }
    
    /**
     * IF_VEHPART DB Status ����
     * 
     * @method updateStatus 
     * @date 2013. 5. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void updateVehStatus(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVehStatus(ds);
    }
    
    /**
     * List �� ������ IF_VEHPART ���¸� �ϰ� �����Ѵ�.
     * 
     * VehPartReportDialog ���� �ϰ� ���� ���濡 ���
     * 
     * @method updateListVehStatus 
     * @date 2013. 5. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see com.kgm.commands.vpm.report.VehPartReportDialog
     */    
    public void updateListVehStatus(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateListVehStatus(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getIfVehPartStatus(ds);
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
    public void updateVPMCustomSetWorker(DataSet ds) throws Exception {    
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomSetWorker(ds);
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
    public void updateVPMCustomNoticeProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomNoticeProcess(ds);
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
    public void updateVPMCustomCompleteProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomCompleteProcess(ds);
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
    public void updateVPMCustomUserSkip(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomUserSkip(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getValidateTCList(ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - �۾��� ����
     * 
     * @method updateTCCustomSetWorker 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */    
    public void updateTCCustomSetWorker(DataSet ds) throws Exception {    
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomSetWorker(ds);
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
    public void updateTCCustomNoticeProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomNoticeProcess(ds);
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
    public void updateTCCustomCompleteProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomCompleteProcess(ds);
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
    public void updateTCCustomUserSkip(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomUserSkip(ds);
    }
}
