/**
 * 
 */
package com.kgm.commands.prebommapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.search.PSESearchInClassElement;
import com.teamcenter.rac.pse.search.PSESearchOperationParameters;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.structuremanagement.StructureSearchService;

/**
 * @author jinil
 *
 */
@SuppressWarnings("rawtypes")
public class BOMLineSearchOperation extends AbstractAIFOperation {
    private TCComponentBOMLine topBOMLine;
    private TCComponentBOMWindow bomWindow;
    private TCSession session;
    private String []searchNames;
    private String []searchValues;
    private boolean bSearchTarget = true;

    public BOMLineSearchOperation(TCComponentBOMLine topLine, String[] queryNames, String[] queryValues, boolean bSearchCriteria)
    {
        if (topLine == null)
            throw new NullPointerException("topLine is null.");
        if (queryNames == null || queryNames.length == 0)
            throw new NullPointerException("queryNames is invalid.");
        if (queryValues == null || queryValues.length == 0)
            throw new NullPointerException("queryValues is invalid.");

        topBOMLine = topLine;
        bomWindow = topBOMLine.getCachedWindow();
        session = topBOMLine.getSession();
        searchNames = queryNames;
        searchValues = queryValues;
        bSearchTarget = bSearchCriteria;
    }

    /* (non-Javadoc)
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(bSearchTarget ? "__SYMC_S7_VehpartRevision_Mapping" : "__SYMC_S7_PreVehpartRevision_Mapping");

        ArrayList localArrayList3 = new ArrayList();
        ArrayList localArrayList4 = new ArrayList();
        boolean bool2 = false;
        try {
            bool2 = this.session.getPreferenceService().getLogicalValue("StructureManagerIncludeSubComponentsForSpatialSearch").booleanValue();
        } catch (Exception localException) {
            throw localException;
//            logger.error(localException.getClass().getName(), localException);
        }

        PSESearchOperationParameters searchParam = new PSESearchOperationParameters(bomWindow.getRevisionRule(), -1.0D, null, query, searchNames, searchValues, null, null, null, null, "", null, null, null, null, localArrayList3, localArrayList4, null, false, false, bool2, false, false, new TCComponentBOMLine[]{topBOMLine});
        Collection<TCComponentBOMLine> searchResult = performSearch(searchParam);
        storeOperationResult(searchResult);
    }

    public Collection<TCComponentBOMLine> performSearch(PSESearchOperationParameters psesearchoperationparameters) throws Exception
    {
        ArrayList<TCComponentBOMLine> findLines = new ArrayList<TCComponentBOMLine>(0);

        StructureSearchService structuresearchservice;
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse structuresearchresultresponse;
        PSESearchInClassElement apsesearchinclasselement[] = psesearchoperationparameters.getInClassViewNames();
        if(apsesearchinclasselement != null && apsesearchinclasselement.length > 0)
        {
            String as[] = new String[apsesearchinclasselement.length];
            for(int i = 0; i < apsesearchinclasselement.length; i++)
                as[i] = psesearchoperationparameters.getInClassViewNames()[i].getClassId();

        }
        com.teamcenter.services.rac.structuremanagement._2010_09.StructureSearch.SearchExpressionSet searchexpressionset = new com.teamcenter.services.rac.structuremanagement._2010_09.StructureSearch.SearchExpressionSet();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression aattributeexpression[] = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression[0];
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression aattributeexpression1[] = psesearchoperationparameters.buildAttributeExpression();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.ProximityExpression aproximityexpression[] = psesearchoperationparameters.buildProximityExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.BoxZoneExpression aboxzoneexpression[] = psesearchoperationparameters.buildBoxZoneExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SavedQueryExpression asavedqueryexpression[] = psesearchoperationparameters.buildSavedQueryExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression aoccurrencenoteexpression[] = psesearchoperationparameters.buildOccurrenceNoteExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.FormAttributeExpression aformattributeexpression[] = psesearchoperationparameters.buildFormAttributeExpressions();
        com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression aplanezoneexpression[] = new com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression[0];
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression ainclassexpression[] = psesearchoperationparameters.buildInClassExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SearchScope searchscope = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SearchScope();
        searchscope.window = bomWindow;
        searchscope.scopeBomLines = psesearchoperationparameters.getScopeLines();
        searchexpressionset.itemAndRevisionAttributeExpressions = aattributeexpression1;
        searchexpressionset.itemAndRevisionAttributeExpressions = aattributeexpression;
        searchexpressionset.occurrenceNoteExpressions = aoccurrencenoteexpression;
        searchexpressionset.formAttributeExpressions = aformattributeexpression;
        searchexpressionset.proximitySearchExpressions = aproximityexpression;
        searchexpressionset.boxZoneExpressions = aboxzoneexpression;
        searchexpressionset.planeZoneExpressions = aplanezoneexpression;
        searchexpressionset.savedQueryExpressions = asavedqueryexpression;
        searchexpressionset.inClassQueryExpressions = ainclassexpression;
        searchexpressionset.doTrushapeRefinement = psesearchoperationparameters.isTrushapeFilterEnabled();
        searchexpressionset.returnScopedSubTreesHit = psesearchoperationparameters.isReturnScopedSubTreesHit();
        searchexpressionset.executeVOOFilter = psesearchoperationparameters.isVOOFilterEnabled();
        structuresearchservice = StructureSearchService.getService(session);
        structuresearchresultresponse = null;
        try
        {
            structuresearchresultresponse = structuresearchservice.startSearch(searchscope, searchexpressionset);
            extractBOMlinesFromSearchResponse(structuresearchresultresponse, findLines);
        }
        catch(ServiceException serviceexception)
        {
            throw serviceexception;
        }
        catch(Exception exception)
        {
            throw exception;
        }
        while(structuresearchresultresponse != null && !structuresearchresultresponse.finished) 
            try
            {
                structuresearchresultresponse = structuresearchservice.nextSearch(structuresearchresultresponse.searchCursor);
                extractBOMlinesFromSearchResponse(structuresearchresultresponse, findLines);
            }
            catch(ServiceException serviceexception1)
            {
                throw serviceexception1;
            }
            catch(Exception exception1)
            {
                throw exception1;
            }

        try
        {
            if(structuresearchresultresponse != null)
                structuresearchservice.stopSearch(structuresearchresultresponse.searchCursor);
        }
        catch(ServiceException serviceexception2)
        {
            throw serviceexception2;
        }
        catch(Exception exception3)
        {
            throw exception3;
        }
        return findLines;
    }

    private void extractBOMlinesFromSearchResponse(com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse structuresearchresultresponse, ArrayList<TCComponentBOMLine> list)
    {
        ArrayList<TCComponentBOMLine> findLines = new ArrayList<TCComponentBOMLine>(Arrays.asList(structuresearchresultresponse.bomLines));
        Collections.sort(findLines, new SeqSortComparator());
        list.addAll(findLines);
//        for(int i = 0; i < structuresearchresultresponse.bomLines.length; i++)
//            list.add(structuresearchresultresponse.bomLines[i]);
    }

    protected class SeqSortComparator implements Comparator<TCComponentBOMLine>
    {
        @Override
        public int compare(TCComponentBOMLine arg0, TCComponentBOMLine arg1)
        {
            try {
                String seq1 = arg0.getProperty("bl_sequence_no");
                String seq2 = arg1.getProperty("bl_sequence_no");

                if (seq1 == null)
                    return seq2 == null ? 0 : 1;
                else if (seq2 == null)
                    return -1;
                else
                    return seq1.compareTo(seq2);
            } catch (TCException e) {
                e.printStackTrace();
            }

            return 0;
        }
    }
}
