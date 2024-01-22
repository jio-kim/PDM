package com.symc.plm.rac.prebom.common.util;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;


public class SDVQueryUtils {

    /**
     *
     *
     * @method executeSavedQuery
     * @date 2013. 10. 18.
     * @param
     * @return TCComponent[]
     * @exception
     * @throws
     * @see
     */
    public static TCComponent[] executeSavedQuery(String queryName, String[] entryNames, String[] entryValues) throws TCException {
        TCComponent[] components = null;

        TCComponentQuery query = getSavedQuery(queryName);
        if(query != null) {
            String[] queryEntries = getTCSession().getTextService().getTextValues(entryNames);
            if(queryEntries != null) {
                for(int i = 0 ; i < queryEntries.length ; i++) {
                    if(queryEntries[i] == null || "".equals(queryEntries[i])) {
                        queryEntries[i] = entryNames[i];
                    }
                }
            }
            components = query.execute(queryEntries, entryValues);
        }

        return components;
    }

    public static TCComponentQuery getSavedQuery(String queryName) throws TCException {
        TCSession session = getTCSession();
        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");

        return (TCComponentQuery) queryType.find(queryName);
    }

    /**
     *
     * @method getTCSession
     * @date 2013. 10. 18.
     * @param
     * @return TCSession
     * @exception
     * @throws
     * @see
     */
    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

}
