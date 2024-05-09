package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;

public interface MigrationMapper {
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidationList(DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getItemValidationList(DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public void updateMigrationItemStatus(HashMap ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getBOMValidationList(DataSet ds);
    
    @SuppressWarnings("rawtypes")
    public void updateMigrationBOMStatus(HashMap ds);
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getX100ItemValidationList(DataSet ds);
}
