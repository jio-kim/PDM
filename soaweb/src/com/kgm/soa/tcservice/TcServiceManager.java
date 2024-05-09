/**
 * 
 */
package com.kgm.soa.tcservice;

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
    private TcStructureManagementService structureService;
    private TcWorkflowManagementService tcWorkflowManagementService;
    private TcVariantManagementService tcVariantManagementService;
    private TcPreferenceManagementService tcPreferenceService;
    private TcSavedQueryService tcSavedQueryService;
    
	public TcServiceManager(Session tcSession) {
	    this.tcSession = tcSession;
	}
	
	public TcDataManagementService getDataService() throws Exception{
		if (dataService == null) {
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
	
	public TcVariantManagementService getTcVariantManagementService() throws Exception{
        if(tcVariantManagementService == null) {
            tcVariantManagementService = new TcVariantManagementService(tcSession);
        }
        return tcVariantManagementService;
	}

	public TcPreferenceManagementService getPreferenceService() throws Exception {
		if (tcPreferenceService == null) {
			tcPreferenceService = new TcPreferenceManagementService(tcSession);
		}
		return tcPreferenceService;
	}
	
	public TcSavedQueryService getSavedQueryService() throws Exception{
        if(tcSavedQueryService == null) {
        	tcSavedQueryService = new TcSavedQueryService(tcSession);
        }
        return tcSavedQueryService;
    }	
}
