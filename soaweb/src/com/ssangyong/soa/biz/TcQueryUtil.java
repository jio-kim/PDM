
package com.ssangyong.soa.biz;


import com.ssangyong.soa.tcservice.TcServiceManager;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.soa.client.model.strong.ImanQuery;


/**
 * 
 * Desc :
 * @author yunjae.jung
 */
public class TcQueryUtil {

    private TcServiceManager tcServiceManager;
    /**
     * Desc : Constructor of TcQueryUtil.java class
     */
    public TcQueryUtil(Session tcSession) {
        tcServiceManager = new TcServiceManager(tcSession);
    }
    
    /**
     * 
     * Desc : execute query and return result.
     * @Method Name : executeQueryResult
     * @param SavedQueriesResponse executeSavedQueries
     * @return QueryResults[]
     * @Comment
     */
    public QueryResults[] executeQueryResult(SavedQueriesResponse executeSavedQueries) {
        
        QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
        return queryresults;
    }
    
    /**
     * 
     * Desc : To execute query, create QueryInput object.
     * @Method Name : setQueryInputforSingle
     * @param String queryName
     * @param String[] entries
     * @param String[] values
     * @param String clientId
     * @return QueryInput[]
     * @throws Exception
     * @Comment
     */
    public QueryInput[] setQueryInputforSingle (String queryName,  String[] entries, String[] values, String clientId) throws Exception {
        /*
        The type of results expected from this operation: 
            0 (top-level objects only), 
            1 (top-level objects plus children: Hierarchical/Indented results), 
            2 (default value as specified on the query object
        */
        QueryInput[] queryInput = new QueryInput[1];
        queryInput[0] = new QueryInput();
        queryInput[0].clientId= clientId;
        queryInput[0].query = getQueryObject(queryName);
        queryInput[0].resultsType=2; 
        queryInput[0].entries = entries;
        queryInput[0].values = values;
        
        return queryInput;
    }
    
    /**
     * 
     * Desc : achieve query object to execute query. 
     * @Method Name : getQueryObject
     * @param String queryName
     * @return ImanQuery
     * @throws Exception
     * @Comment
     */
    private ImanQuery getQueryObject(String queryName) throws Exception {

        try {
            GetSavedQueriesResponse savedQueries = tcServiceManager.getSavedQueryService().getQueryObject();
            for (int i = 0; i < savedQueries.queries.length; i++) {

                if (savedQueries.queries[i].name.equals(queryName)){
                    return savedQueries.queries[i].query;
                }
            }  
        } catch (ServiceException e) {
            e.printStackTrace();                  
        }
        return null;          
    }
    
    /**
     * 
     * Desc : execute query via QueryInput
     * @Method Name : executeSavedQueries
     * @param QueryInput[] queryInput
     * @return SavedQueriesResponse
     * @throws Exception
     * @Comment
     */
    public SavedQueriesResponse executeSavedQueries (com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[] queryInput) throws Exception {
        
        SavedQueriesResponse savedQueries = tcServiceManager.getSavedQueryService().executeSavedQueries(queryInput);
        
        return savedQueries;
    }
    
    
    
}
