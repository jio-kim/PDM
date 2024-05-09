package com.kgm.soa.tcservice;

import com.kgm.soa.biz.Session;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.administration.PreferenceManagementService;
import com.teamcenter.soa.client.model.ServiceData;

public class TcPreferenceManagementService extends PreferenceManagementService {
	Session tcSession = null;

    public TcPreferenceManagementService(Session tcSession) {
    	this.tcSession = tcSession;
    }
    
    public boolean ServiceDataError(final ServiceData serviceData) {
        if(serviceData.sizeOfPartialErrors() > 0)
        {
            for(int i = 0; i < serviceData.sizeOfPartialErrors(); i++)
            {
                for(String msg : serviceData.getPartialError(i).getMessages())
                    System.out.println(msg);
            }

            return true;
        }

        return false;
    }
    
    public PreferenceManagementService getService() {
        return PreferenceManagementService.getService(tcSession.getConnection());
    }


	@Override
	@Deprecated
	public ServiceData setPreferences(PreferencesSetInput[] arg0)
			throws ServiceException {
		return getService().setPreferences(arg0);
	}


	@Override
	public boolean lockSitePreferences() throws ServiceException {
		return getService().lockSitePreferences();
	}


	@Override
	public boolean unlockSitePreferences() throws ServiceException {
		return getService().unlockSitePreferences();
	}


	@Override
	public boolean refreshPreferences() throws ServiceException {
		return getService().refreshPreferences();
	}


	@Override
	public ServiceData deletePreferenceDefinitions(String[] arg0, boolean arg1) {
		return getService().deletePreferenceDefinitions(arg0, arg1);
	}


	@Override
	public ServiceData deletePreferencesAtLocations(
			PreferencesAtLocationIn[] arg0) {
		return getService().deletePreferencesAtLocations(arg0);
	}


	@Override
	public GetPreferencesResponse getPreferences(String[] arg0, boolean arg1) {
		return getService().getPreferences(arg0, arg1);
	}


	@Override
	public GetPreferencesAtLocationsResponse getPreferencesAtLocations(
			PreferencesAtLocationIn[] arg0, boolean arg1) {
		return getService().getPreferencesAtLocations(arg0, arg1);
	}


	@Override
	public ImportPreferencesAtLocationDryRunResponse importPreferencesAtLocationDryRun(
			ImportPreferencesAtLocationDryRunIn arg0) {
		return getService().importPreferencesAtLocationDryRun(arg0);
	}


	@Override
	public PreferenceResponseWithFileTicket importPreferencesAtLocations(
			ImportPreferencesAtLocationsIn arg0) {
		return getService().importPreferencesAtLocations(arg0);
	}


	@Override
	public ServiceData removeStalePreferenceInstancesAtLocations(
			PreferenceLocation[] arg0) {
		return getService().removeStalePreferenceInstancesAtLocations(arg0);
	}


	@Override
	public ServiceData setPreferences2(SetPreferences2In[] arg0) {
		return getService().setPreferences2(arg0);
	}


	@Override
	public ServiceData setPreferencesAtLocations(
			SetPreferencesAtLocationsIn[] arg0) {
		return getService().setPreferencesAtLocations(arg0);
	}


	@Override
	public ServiceData setPreferencesDefinition(
			SetPreferencesDefinitionIn[] arg0) {
		return getService().setPreferencesDefinition(arg0);
	}

	@Override
	public GetPreferencesResponse refreshPreferences2(String[] arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}


}
