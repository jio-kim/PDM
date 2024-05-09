package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;

public interface VPMIfMapper {  
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidateVPMList(DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidateVehPartList(DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIfVehPart();
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIfVehPartFileList(DataSet ds);
    
    public Integer getExistVPMPartCnt(DataSet ds);    
    
    public Integer getExistDRNameCnt(DataSet ds);
    
    public String getECOValideYn(DataSet ds);
    
    public void updateVPMStatus(DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getIFValidateVPMList(DataSet ds);
    
    public void updateVPMValide(DataSet ds);
    
    public void updateECOVehPartValide(DataSet ds);
    
    public void updateNotECOVehPartValide();   
    
    public void updateVehStatus(DataSet ds);
    
    public List<String> getIfVehPartStatus(DataSet ds);
    
    public void updateVPMCustomSetWorker (DataSet ds);
    
    public void updateVPMCustomNoticeProcess(DataSet ds);
    
    public void updateVPMCustomCompleteProcess (DataSet ds);
    
    public void updateVPMCustomUserSkip (DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidateTCList(DataSet ds);
    
    public void updateTCCustomSetWorker (DataSet ds);
    
    public void updateTCCustomNoticeProcess(DataSet ds);
    
    public void updateTCCustomCompleteProcess (DataSet ds);
    
    public void updateTCCustomUserSkip (DataSet ds);
}
