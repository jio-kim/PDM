package com.kgm.soa.tcservice;

import com.kgm.soa.biz.Session;
import com.teamcenter.services.strong.structuremanagement.StructureService;

public class TcStructureService {

    private Session tcSession = null;

    public TcStructureService(Session tcSession) {
        this.tcSession = tcSession;
    }

    public StructureService getService() {
        return StructureService.getService(this.tcSession.getConnection());
    }   


}
