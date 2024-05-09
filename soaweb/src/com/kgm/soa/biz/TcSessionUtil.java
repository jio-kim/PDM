package com.kgm.soa.biz;

import java.util.ArrayList;

import com.kgm.soa.common.constants.TcConstants;
import com.kgm.soa.common.constants.TcMessage;
import com.kgm.soa.tcservice.TcServiceManager;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.strong.core._2007_01.Session.GetTCSessionInfoResponse;
import com.teamcenter.services.strong.core._2007_12.Session.StateNameValue;
import com.teamcenter.services.strong.core._2010_04.Session.MultiPreferenceResponse2;
import com.teamcenter.services.strong.core._2010_04.Session.ReturnedPreferences2;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.User;

public class TcSessionUtil {
    private Session tcSession;
    private TcServiceManager tcServiceManager;

    public TcSessionUtil (Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(this.tcSession);
    }

    /**
     *
     * Desc :
     * @Method Name : getFmsBootStrapUrl
     * @return
     * @throws Exception
     * @Comment
     */
    //TODOS ?��기�?? ?��?��?��.
    public String[] getFmsBootStrapUrl () throws Exception {
//        ReturnedPreferences2 preferences[] = getPreferences2(TcConstants.TC_PREF_SCOPE_SITE, new String[]{TcConstants.TC_PREF_NAME_FMS_BOOTSTRP_URL});
        CompletePreference[] preferences = getPreference(TcConstants.TC_PREF_SCOPE_SITE, new String[]{TcConstants.TC_PREF_NAME_FMS_BOOTSTRP_URL});
//        String[] fmsUrlInfo =  fmsUrlInfo[i] = preferences[i].values
        String[] fmsUrlInfo =preferences[0].values.values;
        return fmsUrlInfo;
    }

    public User getUser() throws Exception {
        return getTCSessionInfo().user;
    }

    public boolean isByPass() throws Exception {
        return getTCSessionInfo().bypass;
    }

    public void setByPass() throws Exception {
        StateNameValue[] propPairs = new StateNameValue[1];
        propPairs[0] = new StateNameValue();
        propPairs[0].name  = new String("bypassFlag");
        propPairs[0].value = new String(Property.toBooleanString( true ));
        com.teamcenter.soa.client.model.ServiceData serviceData =
                tcServiceManager.getSessionService().setUserSessionState(propPairs);
        if(tcServiceManager.getDataService().ServiceDataError(serviceData)){
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
        }
    }

    @Deprecated
    public ReturnedPreferences2[] getPreferences2(String prefScope, String[] prefName) throws Exception {
        MultiPreferenceResponse2 multiPreferResp2 = null;
        com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[] scopedPrefnames = new com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[1];
        scopedPrefnames[0] = new com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames();
        scopedPrefnames[0].scope = prefScope;
        scopedPrefnames[0].names = prefName;
        multiPreferResp2 = tcServiceManager.getSessionService().getPreferences2(scopedPrefnames);
        if(!tcServiceManager.getDataService().ServiceDataError(multiPreferResp2.data)){
            return multiPreferResp2.preferences;
        }
        return null;
    }

    public CompletePreference[] getPreference(String prefScope, String[] prefName) throws Exception {
    	ArrayList<CompletePreference> prefList = new ArrayList<CompletePreference>();
    	GetPreferencesResponse prefResponse = tcServiceManager.getPreferenceService().getPreferences(prefName, true);
    	if (! tcServiceManager.getPreferenceService().ServiceDataError(prefResponse.data))
    	{
    		for (CompletePreference preference : prefResponse.response)
    			if (preference.definition.protectionScope.toUpperCase().equals(prefScope.toUpperCase()))
    				prefList.add(preference);
    	}

    	if (prefList.size() > 0)
    		return prefList.toArray(new CompletePreference[0]);
    	else
    		return null;
    }

    public GetTCSessionInfoResponse getTCSessionInfo() throws Exception {
        GetTCSessionInfoResponse getTcsessioninfoResp = tcServiceManager.getSessionService().getTCSessionInfo();
        return getTcsessioninfoResp;
    }
}
