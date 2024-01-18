/**
 * 
 */
package com.symc.common.soa.service;

import java.util.Map;

import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.schemas.soa._2011_06.metamodel.TypeSchema;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.IdDispRule;
import com.teamcenter.soa.common.ObjectPropertyPolicy;
import com.teamcenter.soa.common.PolicyType;


/**
 * 
 * Desc :
 * @author yunjae.jung
 */
public class TcSessionServiceManager implements
		com.teamcenter.services.internal.strong.core._2007_05.Session ,
        com.teamcenter.services.internal.strong.core._2007_12.Session ,
        com.teamcenter.services.internal.strong.core._2008_03.Session ,
        com.teamcenter.services.internal.strong.core._2008_06.Session ,
        com.teamcenter.services.strong.core._2006_03.Session ,
        com.teamcenter.services.strong.core._2007_01.Session ,
        com.teamcenter.services.strong.core._2007_06.Session ,
        com.teamcenter.services.strong.core._2007_12.Session ,
        com.teamcenter.services.strong.core._2008_03.Session ,
        com.teamcenter.services.strong.core._2008_06.Session ,
        com.teamcenter.services.strong.core._2009_04.Session ,
        com.teamcenter.services.strong.core._2010_04.Session ,
        com.teamcenter.services.strong.core._2011_06.Session ,
        com.teamcenter.services.strong.core._2012_02.Session {

	private com.symc.common.soa.biz.Session tcSession;
    public TcSessionServiceManager(com.symc.common.soa.biz.Session tcSession) {
    	this.tcSession = tcSession;
    }
    
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_06.Session#cancelOperation(java.lang.String)
     */
    @Override
    public boolean cancelOperation(String arg0){
        return getInternalService().cancelOperation(arg0);
    }
    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_03.Session#connect(java.lang.String, java.lang.String)
     */
    @Override
    public ConnectResponse connect(String arg0, String arg1){
        return getService().connect(arg0, arg1);
    }
    
    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2008_03.Session#disableUserSessionState(java.lang.String[])
     */
    @Override
    @Deprecated
    public ServiceData disableUserSessionState(String[] arg0){
        return null;
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#getAvailableServices()
     */
    @Override
    public GetAvailableServicesResponse getAvailableServices(){
        return getService().getAvailableServices();
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_06.Session#getDisplayStrings(java.lang.String[])
     */
    @Override
    public GetDisplayStringsResponse getDisplayStrings(String[] arg0){
        return getService().getDisplayStrings(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_03.Session#getFavorites()
     */
    @Override
    @Deprecated
    public FavoritesResponse getFavorites() throws ServiceException{
        return getService().getFavorites();
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#getGroupMembership()
     */
    @Override
    public GetGroupMembershipResponse getGroupMembership()
            throws ServiceException{
        return getService().getGroupMembership();
    }

    public com.teamcenter.services.internal.strong.core.SessionService getInternalService(){
        return com.teamcenter.services.internal.strong.core.SessionService.getService(tcSession.getConnection());
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2007_01.Session#getPreferences(com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[])
     */
    @Override
    @Deprecated
    public MultiPreferencesResponse getPreferences(ScopedPreferenceNames[] arg0)
            throws ServiceException{
        return getService().getPreferences(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#getPreferences(java.lang.String, java.lang.String[])
     */
    @Override
    @Deprecated
    public PreferencesResponse getPreferences(String arg0, String[] arg1)
            throws ServiceException{
        return getService().getPreferences(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2010_04.Session#getPreferences2(com.teamcenter.services.strong.core._2007_01.Session.ScopedPreferenceNames[])
     */
    @Override
    @Deprecated
    public MultiPreferenceResponse2 getPreferences2(ScopedPreferenceNames[] scopedPrefnames){
        return getService().getPreferences2(scopedPrefnames);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2007_12.Session#getProperties(com.teamcenter.soa.client.model.ModelObject[], java.lang.String[])
     */
    @Override
    public ServiceData getProperties(ModelObject[] arg0, String[] arg1){
        return getInternalService().getProperties(arg0, arg1);
    }

    /**
     * Desc : Constructor of TcSessionService.java class
     */
    public SessionService getService() {
        return SessionService.getService(tcSession.getConnection());
        
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#getSessionGroupMember()
     */
    @Override
    public GetSessionGroupMemberResponse getSessionGroupMember()
            throws ServiceException{
        return getService().getSessionGroupMember();
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2010_04.Session#getShortcuts(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
	@Override
    public GetShortcutsResponse getShortcuts(Map map) throws ServiceException{
        return getService().getShortcuts(map);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2007_01.Session#getTCSessionInfo()
     */
    @Override
    public GetTCSessionInfoResponse getTCSessionInfo() throws ServiceException{
        return getService().getTCSessionInfo();
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#login(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Deprecated
    public com.teamcenter.services.strong.core._2006_03.Session.LoginResponse login(String arg0, String arg1, String arg2,
            String arg3, String arg4) throws InvalidCredentialsException{
        return getService().login(arg0, arg1, arg2, arg3, arg4);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_06.Session#login(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public com.teamcenter.services.strong.core._2006_03.Session.LoginResponse login(String arg0, String arg1, String arg2,
            String arg3, String arg4, String arg5)
            throws InvalidCredentialsException{
        return getService().login(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#loginSSO(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Deprecated
    public com.teamcenter.services.strong.core._2006_03.Session.LoginResponse loginSSO(String arg0, String arg1, String arg2,
            String arg3, String arg4) throws InvalidCredentialsException{
        return getService().loginSSO(arg0, arg1, arg2, arg3, arg4);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_06.Session#loginSSO(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public com.teamcenter.services.strong.core._2006_03.Session.LoginResponse loginSSO(String arg0, String arg1, String arg2,
            String arg3, String arg4, String arg5)
            throws InvalidCredentialsException{
        return getService().loginSSO(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#logout()
     */
    @Override
    public ServiceData logout() throws ServiceException{
        return getService().logout();
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.internal.strong.core._2007_05.Session#refreshPOMCachePerRequest(boolean)
     */
    @Override
    public boolean refreshPOMCachePerRequest(boolean arg0){
        return getService().refreshPOMCachePerRequest(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2007_12.Session#setAndEvaluateIdDisplayRule(com.teamcenter.soa.client.model.ModelObject[], com.teamcenter.soa.client.model.strong.IdDispRule, boolean)
     */
    @Override
    public ServiceData setAndEvaluateIdDisplayRule(ModelObject[] arg0,
            IdDispRule arg1, boolean arg2){
        return getService().setAndEvaluateIdDisplayRule(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_03.Session#setFavorites(com.teamcenter.services.strong.core._2008_03.Session.FavoritesInfo)
     */
    @Override
    public ServiceData setFavorites(FavoritesInfo arg0) throws ServiceException{
        return getService().setFavorites(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2008_06.Session#setObjectPropertyPolicy(com.teamcenter.soa.common.ObjectPropertyPolicy)
     */
    @Override
    public String setObjectPropertyPolicy(ObjectPropertyPolicy arg0){
        return getService().setObjectPropertyPolicy(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2007_01.Session#setObjectPropertyPolicy(java.lang.String)
     */
    @Override
    public boolean setObjectPropertyPolicy(String arg0) throws ServiceException{
        return getService().setObjectPropertyPolicy(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#setPreferences(com.teamcenter.services.strong.core._2006_03.Session.PrefSetting[])
     */
    @Override
    @Deprecated
    public PreferencesResponse setPreferences(PrefSetting[] arg0)
            throws ServiceException{
        return getService().setPreferences(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2006_03.Session#setSessionGroupMember(com.teamcenter.soa.client.model.strong.GroupMember)
     */
    @Override
    public ServiceData setSessionGroupMember(GroupMember arg0)
            throws ServiceException{
        return getService().setSessionGroupMember(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2007_12.Session#setUserSessionState(com.teamcenter.services.strong.core._2007_12.Session.StateNameValue[])
     */
    @Override
    public ServiceData setUserSessionState(StateNameValue[] arg0){
        return getService().setUserSessionState(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2009_04.Session#startOperation()
     */
    @Override
    public String startOperation(){
        return getService().startOperation();
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.core._2009_04.Session#stopOperation(java.lang.String)
     */
    @Override
    public boolean stopOperation(String arg0){
        return getService().stopOperation(arg0);
    }

	@Override
	public RegisterIndex registerState(String paramString) {
		return getService().registerState(paramString);
	}

	@Override
	public SetPolicyResponse setObjectPropertyPolicy(String paramString,
			boolean paramBoolean) throws ServiceException {
		return getService().setObjectPropertyPolicy(paramString, paramBoolean);
	}

	@Override
	public boolean unregisterState(int paramInt) {
		return getService().unregisterState(paramInt);
	}

	@Override
	public ClientCacheInfo getClientCacheData(String[] paramArrayOfString) {
		return getService().getClientCacheData(paramArrayOfString);
	}

	@Override
	public TypeSchema getTypeDescriptions(String[] paramArrayOfString) {
		return getService().getTypeDescriptions(paramArrayOfString);
	}

	@Override
	public com.teamcenter.services.strong.core._2011_06.Session.LoginResponse login(
			Credentials paramCredentials) throws InvalidCredentialsException {
		return getService().login(paramCredentials);
	}

	@Override
	public com.teamcenter.services.strong.core._2011_06.Session.LoginResponse loginSSO(
			Credentials paramCredentials) throws InvalidCredentialsException {
		return getService().loginSSO(paramCredentials);
	}

	@Override
	public String updateObjectPropertyPolicy(String paramString,
			PolicyType[] paramArrayOfPolicyType1,
			PolicyType[] paramArrayOfPolicyType2) throws ServiceException {
		return getService().updateObjectPropertyPolicy(paramString, paramArrayOfPolicyType1, paramArrayOfPolicyType2);
	}

}
