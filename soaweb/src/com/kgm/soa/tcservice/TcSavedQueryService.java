package com.kgm.soa.tcservice;



import com.kgm.soa.biz.Session;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ImanQuery;

/**
 * 
 * Desc :
 * @author yunjae.jung
 */
public class TcSavedQueryService implements 
com.teamcenter.services.strong.query._2006_03.SavedQuery,
com.teamcenter.services.strong.query._2007_01.SavedQuery,
com.teamcenter.services.strong.query._2007_06.SavedQuery,
com.teamcenter.services.strong.query._2007_09.SavedQuery,
com.teamcenter.services.strong.query._2008_06.SavedQuery,
com.teamcenter.services.strong.query._2010_04.SavedQuery,
com.teamcenter.services.strong.query._2010_09.SavedQuery,
com.teamcenter.services.strong.query._2013_05.SavedQuery
{
	private Session tcSession;
	
    public TcSavedQueryService(Session tcSession) {
    	this.tcSession = tcSession;
    }
    
    @Override
    public FindSavedQueriesResponse findSavedQueries(
            FindSavedQueriesCriteriaInput[] arg0) throws ServiceException{
        return getService().findSavedQueries(arg0);
    }
    
    public GetSavedQueriesResponse getQueryObject() throws ServiceException {

        return getService().getSavedQueries();          
    }

    public SavedQueryService getService() {
        return SavedQueryService.getService(tcSession.getConnection());
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2008_06.SavedQuery#executeSavedQueries(com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[])
     */
    @Override
    public SavedQueriesResponse executeSavedQueries(
            com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[] arg0){
        return getService().executeSavedQueries(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_09.SavedQuery#executeSavedQueries(com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryInput[])
     */
    @Override
    @Deprecated
    public SavedQueriesResponse executeSavedQueries(com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryInput[] arg0){
        return getService().executeSavedQueries(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_06.SavedQuery#executeSavedQueries(com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryInput[])
     */
    @Override
    @Deprecated
    public ExecuteSavedQueriesResponse executeSavedQueries(
            SavedQueryInput[] arg0){
        return getService().executeSavedQueries(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_06.SavedQuery#retrieveSearchCriteria(java.lang.String[])
     */
    @Override
    @Deprecated
    public RetrieveSearchCriteriaResponse retrieveSearchCriteria(String[] arg0){
        return getService().retrieveSearchCriteria(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_06.SavedQuery#saveSearchCriteria(com.teamcenter.services.strong.query._2007_06.SavedQuery.SaveSearchCriteriaInfo[])
     */
    @Override
    @Deprecated
    public ServiceData saveSearchCriteria(SaveSearchCriteriaInfo[] arg0){
        return getService().saveSearchCriteria(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_01.SavedQuery#deleteQueryCriterias(java.lang.String[])
     */
    @Override
    @Deprecated
    public ServiceData deleteQueryCriterias(String[] arg0)
            throws ServiceException{
        return getService().deleteQueryCriterias(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_01.SavedQuery#reorderSavedQueryCriterias(java.lang.String[])
     */
    @Override
    @Deprecated
    public ServiceData reorderSavedQueryCriterias(String[] arg0)
            throws ServiceException{
        return getService().reorderSavedQueryCriterias(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_01.SavedQuery#retrieveQueryCriterias(java.lang.String[])
     */
    @Override
    @Deprecated
    public RetrieveQueryCriteriaResponse retrieveQueryCriterias(String[] arg0)
            throws ServiceException{
        return getService().retrieveQueryCriterias(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2007_01.SavedQuery#saveQueryCriterias(com.teamcenter.services.strong.query._2007_01.SavedQuery.SaveQueryCriteriaInfo[])
     */
    @Override
    @Deprecated
    public ServiceData saveQueryCriterias(SaveQueryCriteriaInfo[] arg0)
            throws ServiceException{
        return getService().saveQueryCriterias(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2006_03.SavedQuery#describeSavedQueries(com.teamcenter.soa.client.model.strong.ImanQuery[])
     */
    @Override
    public DescribeSavedQueriesResponse describeSavedQueries(ImanQuery[] arg0){
        return getService().describeSavedQueries(arg0);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2006_03.SavedQuery#executeSavedQuery(com.teamcenter.soa.client.model.strong.ImanQuery, java.lang.String[], java.lang.String[], int)
     */
    @Override
    @Deprecated
    public ExecuteSavedQueryResponse executeSavedQuery(ImanQuery arg0,
            String[] arg1, String[] arg2, int arg3) throws ServiceException{
        return getService().executeSavedQuery(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see com.teamcenter.services.strong.query._2006_03.SavedQuery#getSavedQueries()
     */
    @Override
    public GetSavedQueriesResponse getSavedQueries() throws ServiceException{
        return getService().getSavedQueries();
    } 
    
    public ImanQuery getQuery(String sQryName)
    {
        ImanQuery query = null;

        try
        {
            // *****************************
            // Execute the service operation
            // *****************************
            GetSavedQueriesResponse savedQueries = getService().getSavedQueries();
            
            if (savedQueries.queries.length == 0)
            {
               return null;
            }

            // Find one called 'Item Name'
            for (int i = 0; i < savedQueries.queries.length; i++)
            {
                if (savedQueries.queries[i].name.equals(sQryName))
                {
                    query = savedQueries.queries[i].query;
                    break;
                }
            }
        }
        catch (ServiceException e)
        {
            return null;
        }
        return query;
    }

	@Override
	public SavedQueriesResponse executeBusinessObjectQueries(
			BusinessObjectQueryInput[] paramArrayOfBusinessObjectQueryInput) {
		return getService().executeBusinessObjectQueries(paramArrayOfBusinessObjectQueryInput);
	}

	@Override
	public ServiceData createSavedQueries(SavedQueryProperties[] arg0) {
		return getService().createSavedQueries(arg0);
	}
}
