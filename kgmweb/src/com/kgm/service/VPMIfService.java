package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.VPMIfDao;

public class VPMIfService {
    
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getValidateVPMList(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getValidateVehPartList(ds);
    }
    
    /**
     * I/F VEHPART 조회
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
     * I/F VEHPART FILE 조회
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
     * VPM에 PartNo가 존재하는지 확인
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
     * DR Name인지 Check
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getIFValidateVPMList(ds);
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
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMValide(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getECOValideYn(ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_VPM - DB Status 변경
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
        VPMIfDao dao = new VPMIfDao();
        dao.updateECOVehPartValide(ds);
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
        VPMIfDao dao = new VPMIfDao();
        dao.updateNotECOVehPartValide();
    }
    
    /**
     * IF_VEHPART DB Status 변경
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
     * List 를 가지고 IF_VEHPART 상태를 일괄 변경한다.
     * 
     * VehPartReportDialog 에서 일괄 상태 변경에 사용
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getIfVehPartStatus(ds);
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
    public void updateVPMCustomSetWorker(DataSet ds) throws Exception {    
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomSetWorker(ds);
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
    public void updateVPMCustomNoticeProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomNoticeProcess(ds);
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
    public void updateVPMCustomCompleteProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomCompleteProcess(ds);
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
    public void updateVPMCustomUserSkip(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateVPMCustomUserSkip(ds);
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
        VPMIfDao dao = new VPMIfDao();
        return dao.getValidateTCList(ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - 작업자 설정
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
    public void updateTCCustomNoticeProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomNoticeProcess(ds);
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
    public void updateTCCustomCompleteProcess(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomCompleteProcess(ds);
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
    public void updateTCCustomUserSkip(DataSet ds) throws Exception {
        VPMIfDao dao = new VPMIfDao();
        dao.updateTCCustomUserSkip(ds);
    }
}
