package com.symc.common.soa.service;

import com.symc.common.soa.biz.Session;
import com.teamcenter.services.internal.strong.structuremanagement.VariantManagementService;

public class TcVariantManagementService {
    private Session tcSession = null;
    
    public TcVariantManagementService(Session tcSession) {
        this.tcSession = tcSession;
    }
    
    public VariantManagementService getService() {
        return VariantManagementService.getService(this.tcSession.getConnection());
    }    
    
}
