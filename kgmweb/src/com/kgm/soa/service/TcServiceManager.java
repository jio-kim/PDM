/**
 * 
 */
package com.kgm.soa.service;

import com.kgm.soa.biz.Session;

/**
 * @author jungy
 *
 */
public class TcServiceManager {

    private Session tcSession;
    private TcDataManagementService dataService;
    private TcDataManagementInernalService internalDataService;
    private TcFileManagementService fileService;
    private TcSessionServiceManager sessionService;  
    private TcQueryService queryService;
    private TcStructureManagementService structureService;
    private TcWorkflowManagementService tcWorkflowManagementService;
    private TcPreferenceManagementService tcPreferenceManagementService;
    
	public TcServiceManager(Session tcSession) {
	    this.tcSession = tcSession;
	}
	
	public TcDataManagementService getDataService() throws Exception{
		if(dataService == null) {
		    dataService = new TcDataManagementService(tcSession);
		}
		return dataService;
	}
	
	public TcDataManagementInernalService getInternalDataService() throws Exception{
        if(internalDataService == null) {
            internalDataService = new TcDataManagementInernalService(tcSession);
        }
        return internalDataService;
    }
	
	public TcFileManagementService getFileService() throws Exception{
        if(fileService == null) {
            fileService = new TcFileManagementService(tcSession);
        }
        return fileService;
    }
	
	public TcSessionServiceManager getSessionService() throws Exception{
        if(sessionService == null) {
            sessionService = new TcSessionServiceManager(tcSession);
        }
        return sessionService;
    }
	
	public TcQueryService getQueryService() throws Exception{
        if(queryService == null) {
            queryService = new TcQueryService(tcSession);
        }
        return queryService;
    }
	
	public TcStructureManagementService getStructureService() throws Exception{
	    if(structureService == null) {
	        structureService = new TcStructureManagementService(tcSession);
	    }
	    return structureService;
    }
	
	public TcWorkflowManagementService getWorkflowService() throws Exception{
        if(tcWorkflowManagementService == null) {
            tcWorkflowManagementService = new TcWorkflowManagementService(tcSession);
        }
        return tcWorkflowManagementService;
    }

	public TcPreferenceManagementService getPreferenceService() throws Exception {
		if (tcPreferenceManagementService == null)
		{
			tcPreferenceManagementService = new TcPreferenceManagementService(tcSession);
		}
		return tcPreferenceManagementService;
	}
	
}
