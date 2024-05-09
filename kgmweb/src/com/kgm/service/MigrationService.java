package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.MigrationDao;

public class MigrationService {
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidationList(DataSet ds) {
        MigrationDao dao = new MigrationDao();
        return dao.getValidationList(ds);
    }
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getItemValidationList(DataSet ds) {
        MigrationDao dao = new MigrationDao();
        return dao.getItemValidationList(ds);
    }
    
    public void updateMigrationStatus(DataSet ds) {
        MigrationDao dao = new MigrationDao();
        dao.updateMigrationStatus(ds);
    }
    
    @SuppressWarnings("rawtypes")
    public List<HashMap> getBOMValidationList(DataSet ds) {
        MigrationDao dao = new MigrationDao();
        return dao.getBOMValidationList(ds);
    }
    
    public void updateMigrationBOMStatus(DataSet ds) {
        MigrationDao dao = new MigrationDao();
        dao.updateMigrationBOMStatus(ds);
    }
    
    public void updateMigrationStatusChange(DataSet ds) {
        MigrationDao dao = new MigrationDao();
        dao.updateMigrationStatusChange(ds);
    }
}

