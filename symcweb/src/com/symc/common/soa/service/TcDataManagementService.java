package com.symc.common.soa.service;

import java.util.Map;

import com.symc.common.soa.biz.Session;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Fnd0StaticTable;
import com.teamcenter.soa.client.model.strong.ImanType;
import com.teamcenter.soa.client.model.strong.POM_object;
import com.teamcenter.soa.client.model.strong.Table;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

public class TcDataManagementService extends DataManagementService {
	private Session tcSession;

	public TcDataManagementService(Session tcSession) {
		this.tcSession = tcSession;
	}
	@Override
	public AddParticipantOutput addParticipants(AddParticipantInfo[] arg0) {
		return null;
	}

	@Override
	public CreateResponse bulkCreateObjects(BulkCreIn[] arg0) {
		return null;
	}

	@Override
	public ServiceData changeOwnership(ObjectOwner[] arg0) {
		return getService().changeOwnership(arg0);
	}

	@Override
	public ServiceData createAlternateIdentifiers(
			AlternateIdentifiersInput[] arg0) {
		return null;
	}

	@Override
	public CreateConnectionsResponse createConnections(
			ConnectionProperties[] arg0, ModelObject arg1, String arg2) {
		return null;
	}

	@Override
	@Deprecated
	public com.teamcenter.services.strong.core._2006_03.DataManagement.CreateDatasetsResponse createDatasets(com.teamcenter.services.strong.core._2006_03.DataManagement.DatasetProperties[] arg0) {
		return getService().createDatasets(arg0);
	}

	@Override
	public com.teamcenter.services.strong.core._2010_04.DataManagement.CreateDatasetsResponse createDatasets(
			com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[] arg0) {
		return getService().createDatasets(arg0);
	}

	@Override
	public com.teamcenter.services.strong.core._2006_03.DataManagement.CreateDatasetsResponse createDatasets2(com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetProperties2[] arg0) {
		return getService().createDatasets2(arg0);
	}

	@Override
	public CreateFoldersResponse createFolders(CreateFolderInput[] arg0,
			ModelObject arg1, String arg2) {
		return getService().createFolders(arg0, arg1, arg2);
	}

	@Override
	public CreateItemsResponse createItems(ItemProperties[] arg0,
			ModelObject arg1, String arg2) {
		return getService().createItems(arg0, arg1, arg2);
	}

	@Override
	public CreateResponse createObjects(CreateIn[] arg0)
			throws ServiceException {
		return getService().createObjects(arg0);
	}

	@Override
	public CreateOrUpdateFormsResponse createOrUpdateForms(FormInfo[] arg0) {
		return getService().createOrUpdateForms(arg0);
	}

	@Override
	public CreateOrUpdateGDELinksResponse createOrUpdateGDELinks(
			GDELinkInfo[] arg0) {
		return getService().createOrUpdateGDELinks(arg0);
	}

	@Override
	public CreateOrUpdateItemElementsResponse createOrUpdateItemElements(
			ItemElementProperties[] arg0) {
		return getService().createOrUpdateItemElements(arg0);
	}

	@Override
	public CreateOrUpdateRelationsResponse createOrUpdateRelations(
			CreateOrUpdateRelationsInfo[] arg0, boolean arg1) {
	    return getService().createOrUpdateRelations(arg0, arg1);
	}

	@Override
	public CreateOrUpdateStaticTableDataResponse createOrUpdateStaticTableData(
			StaticTableInfo arg0, RowData[] arg1) {
		return getService().createOrUpdateStaticTableData(arg0, arg1);
	}

	@Override
	public CreateRelationsResponse createRelations(Relationship[] arg0) {
		return getService().createRelations(arg0);
	}

	@Override
	public ServiceData deleteObjects(ModelObject[] arg0) {
		return getService().deleteObjects(arg0);
	}

	@Override
	public ServiceData deleteRelations(Relationship[] arg0) {
		return getService().deleteRelations(arg0);
	}

	@Override
	public ExpandGRMRelationsResponse expandGRMRelationsForPrimary(
			ModelObject[] arg0, ExpandGRMRelationsPref arg1) {
		return getService().expandGRMRelationsForPrimary(arg0, arg1);
	}

	@Override
	public ExpandGRMRelationsResponse2 expandGRMRelationsForPrimary(
			ModelObject[] arg0, ExpandGRMRelationsPref2 arg1) {
		return getService().expandGRMRelationsForPrimary(arg0, arg1);
	}

	@Override
	public ExpandGRMRelationsResponse expandGRMRelationsForSecondary(
			ModelObject[] arg0, ExpandGRMRelationsPref arg1) {
		return getService().expandGRMRelationsForSecondary(arg0, arg1);
	}

	@Override
	public ExpandGRMRelationsResponse2 expandGRMRelationsForSecondary(
			ModelObject[] arg0, ExpandGRMRelationsPref2 arg1) {
		return getService().expandGRMRelationsForSecondary(arg0, arg1);
	}

	@Override
	public DisplayableSubBOsResponse findDisplayableSubBusinessObjects(
			BOWithExclusionIn[] arg0) throws ServiceException {
		return getService().findDisplayableSubBusinessObjects(arg0);
	}

	@Override
	public DisplayableSubBusinessObjectsResponse findDisplayableSubBusinessObjectsWithDisplayNames(
			BOWithExclusionIn[] arg0) throws ServiceException {
		return getService().findDisplayableSubBusinessObjectsWithDisplayNames(arg0);
	}

	@Override
	public GenerateItemIdsAndInitialRevisionIdsResponse generateItemIdsAndInitialRevisionIds(
			GenerateItemIdsAndInitialRevisionIdsProperties[] arg0) {
		return getService().generateItemIdsAndInitialRevisionIds(arg0);
	}

	@Override
	public GenerateRevisionIdsResponse generateRevisionIds(
			GenerateRevisionIdsProperties[] arg0) {
		return getService().generateRevisionIds(arg0);
	}

	@Override
	public GenerateUIDResponse generateUID(int arg0) {
		return getService().generateUID(arg0);
	}

	@Override
	public GetAvailableTypesResponse getAvailableTypes(BaseClassInput[] arg0) {
		return getService().getAvailableTypes(arg0);
	}

	@Override
	public GetAvailableBusinessObjectTypesResponse getAvailableTypesWithDisplayNames(
			BaseClassInput[] arg0) {
		return getService().getAvailableTypesWithDisplayNames(arg0);
	}

	@Override
	public GetContextAndIdentifiersResponse getContextsAndIdentifierTypes(
			ImanType[] arg0) {
		return getService().getContextsAndIdentifierTypes(arg0);
	}

	@Override
	public GetDatasetCreationRelatedInfoResponse getDatasetCreationRelatedInfo(
			String arg0, ModelObject arg1) {
		return getService().getDatasetCreationRelatedInfo(arg0, arg1);
	}

	@Override
	public GetDatasetCreationRelatedInfoResponse2 getDatasetCreationRelatedInfo2(
			String arg0, ModelObject arg1) {
		return getService().getDatasetCreationRelatedInfo2(arg0, arg1);
	}

	@Override
	public DatasetTypeInfoResponse getDatasetTypeInfo(String[] arg0) {
		return getService().getDatasetTypeInfo(arg0);
	}

	@Override
	public EventTypesResponse getEventTypes(EventObject[] arg0) {
		return getService().getEventTypes(arg0);
	}

	@Override
	public GetItemAndRelatedObjectsResponse getItemAndRelatedObjects(
			GetItemAndRelatedObjectsInfo[] arg0) {
		return getService().getItemAndRelatedObjects(arg0);
	}

	@Override
	public GetItemCreationRelatedInfoResponse getItemCreationRelatedInfo(
			String arg0, ModelObject arg1) {
		return getService().getItemCreationRelatedInfo(arg0, arg1);
	}

	@Override
	public GetItemFromAttributeResponse getItemFromAttribute(
			GetItemFromAttributeInfo[] arg0, int arg1, GetItemFromIdPref arg2) {
		return getService().getItemFromAttribute(arg0, arg1, arg2);
	}

	@Override
	@Deprecated
	public GetItemFromIdResponse getItemFromId(GetItemFromIdInfo[] arg0,
			int arg1, GetItemFromIdPref arg2) {
		return getService().getItemFromId(arg0, arg1, arg2);
	}

	@Override
	public LocalizedPropertyValuesList getLocalizedProperties(
			PropertyInfo[] arg0) {
		return getService().getLocalizedProperties(arg0);
	}

	@Override
	public GetNextIdsResponse getNextIds(InfoForNextId[] arg0) {
		return getService().getNextIds(arg0);
	}

	@Override
	public GetNRPatternsWithCountersResponse getNRPatternsWithCounters(
			NRAttachInfo[] arg0) {
		return getService().getNRPatternsWithCounters(arg0);
	}

	@Override
	public ServiceData getProperties(ModelObject[] arg0, String[] arg1) {
		return getService().getProperties(arg0, arg1);
	}

	@Override
	public GetRevNRAttachResponse getRevNRAttachDetails(
			TypeAndItemRevInfo[] arg0) {
		return getService().getRevNRAttachDetails(arg0);
	}

	@Override
	public StaticTableDataResponse getStaticTableData(Fnd0StaticTable arg0) {
		return getService().getStaticTableData(arg0);
	}

	@Override
	public GetTablePropertiesResponse getTableProperties(Table[] arg0) {
		return getService().getTableProperties(arg0);
	}

	@Override
	@Deprecated
	public TraceabilityReportOutput getTraceReport(TraceabilityInfoInput arg0) {
		return getService().getTraceReport(arg0);
	}

	@Override
	@Deprecated
	public LocalizableStatusResponse isPropertyLocalizable(
			LocalizableStatusInput[] arg0) throws ServiceException {
		return getService().isPropertyLocalizable(arg0);
	}

	@Override
	public ListAlternateIdDisplayRulesResponse listAlternateIdDisplayRules(
			ListAlternateIdDisplayRulesInfo arg0) {
		return getService().listAlternateIdDisplayRules(arg0);
	}

	@Override
	public ServiceData loadObjects(String[] arg0) {
		return getService().loadObjects(arg0);
	}

    public ModelObject loadModelObject(String uid) {
        ModelObject[] modelobject = loadModelObjects(new String[] {uid});
        if(modelobject.length > 0) {
            return modelobject[0];
        }
        return null;
	}

    public ModelObject[] loadModelObjects(String[] uid) {

        ServiceData serviceData = getService().loadObjects(uid);
        int cnt = serviceData.sizeOfPlainObjects();
        if(!ServiceDataError(serviceData) && cnt == uid.length) {

            ModelObject[] modelObjects = new ModelObject[cnt];

            for(int i=0; i<serviceData.sizeOfPlainObjects(); i++) {
                modelObjects[i] = serviceData.getPlainObject(i);
                refreshObjects(modelObjects[i]);  //Do we need?? if not necessary, remark refreshObjects.
            }
            return modelObjects;
        }
        return null;
    }

	@Override
	public MoveToNewFolderResponse moveToNewFolder(MoveToNewFolderInfo[] arg0) {
		return getService().moveToNewFolder(arg0);
	}

	@Override
	public PostEventResponse postEvent(PostEventObjectProperties[] arg0,
			String arg1) throws ServiceException {
		return getService().postEvent(arg0, arg1);
	}

	@Override
	public ServiceData purgeSequences(PurgeSequencesInfo[] arg0) {
		return getService().purgeSequences(arg0);
	}

	@Override
	public ServiceData refreshObjects(ModelObject[] model) {
        return getService().refreshObjects(model);
	}

    public ModelObject refreshObjects(ModelObject model) {
		ServiceData serviceData = getService().refreshObjects(
				new ModelObject[] { model });

		if (serviceData != null && serviceData.sizeOfPlainObjects() > 0) {

			return serviceData.getPlainObject(0);
		}

		return model;
	}

    /**
     * 구현이 안되어 있어서 구현함.
     * 2014.01.02 hybyeon
     */
	@Override
	public ServiceData removeNamedReferenceFromDataset(
			RemoveNamedReferenceFromDatasetInfo[] arg0) {
		return getService().removeNamedReferenceFromDataset(arg0);
	}

	@Override
	public ServiceData removeParticipants(Participants[] arg0) {
		return getService().removeParticipants(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	@Deprecated
	public ReviseResponse revise(Map arg0) throws ServiceException {
		return getService().revise(arg0);
	}

	@Override
	public ReviseResponse2 revise2(ReviseInfo[] arg0) {
		return getService().revise2(arg0);
	}

	@Override
	@Deprecated
	public SaveAsNewItemResponse saveAsNewItem(com.teamcenter.services.strong.core._2007_01.DataManagement.SaveAsNewItemInfo[] arg0)
			throws ServiceException {
		return getService().saveAsNewItem(arg0);
	}

	@Override
	public SaveAsNewItemResponse2 saveAsNewItem2(
			com.teamcenter.services.strong.core._2008_06.DataManagement.SaveAsNewItemInfo[] arg0) {
		return getService().saveAsNewItem2(arg0);
	}

	@Override
	public SaveAsObjectsResponse saveAsObjects(com.teamcenter.services.strong.core._2011_06.DataManagement.SaveAsIn[] arg0)
			throws ServiceException {
		return getService().saveAsObjects(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ServiceData setDisplayProperties(ModelObject[] arg0, Map arg1) {
		return getService().setDisplayProperties(arg0, arg1);
	}

	@Override
	public ServiceData setLocalizedProperties(LocalizedPropertyValuesInfo arg0) {
		return getService().setLocalizedProperties(arg0);
	}

	@Override
	public ServiceData setLocalizedPropertyValues(
			LocalizedPropertyValuesInfo[] arg0) {
		return getService().setLocalizedPropertyValues(arg0);
	}

	@Override
	public ServiceData setOrRemoveImmunity(SetOrRemoveImmunityInfo[] arg0) {
		return getService().setOrRemoveImmunity(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ServiceData setProperties(ModelObject[] arg0, Map arg1) {
		return getService().setProperties(arg0, arg1);
	}

	@Override
	public SetPropertyResponse setProperties(PropInfo[] arg0, String[] arg1) {
		return getService().setProperties(arg0, arg1);
	}

	@Override
	public ServiceData setTableProperties(TableInfo[] arg0) {
		return getService().setTableProperties(arg0);
	}

	@Override
	public ServiceData unloadObjects(ModelObject[] arg0) {
		return getService().unloadObjects(arg0);
	}

	@Override
	public ValidateAlternateIdResponse validateAlternateIds(
			ValidateAlternateIdInput[] arg0) {
		return getService().validateAlternateIds(arg0);
	}

	@Override
	public ValidationResponse validateIdValue(CreateIn[] arg0) {
		return getService().validateIdValue(arg0);
	}

	@Override
	@Deprecated
	public ValidateItemIdsAndRevIdsResponse validateItemIdsAndRevIds(
			ValidateIdsInfo[] arg0) {
		return getService().validateItemIdsAndRevIds(arg0);
	}

	@Override
	public ValidateRevIdsResponse validateRevIds(ValidateRevIdsInfo[] arg0) {
		return getService().validateRevIds(arg0);
	}

	@Override
	public VerifyExtensionResponse verifyExtension(VerifyExtensionInfo[] arg0) {
		return getService().verifyExtension(arg0);
	}

	@Override
	public WhereReferencedResponse whereReferenced(WorkspaceObject[] arg0,
			int arg1) {
		return getService().whereReferenced(arg0, arg1);
	}

	@Override
	public WhereReferencedByRelationNameResponse whereReferencedByRelationName(
			WhereReferencedByRelationNameInfo[] arg0, int arg1) {
		return getService().whereReferencedByRelationName(arg0, arg1);
	}

	@Override
	@Deprecated
	public com.teamcenter.services.strong.core._2007_01.DataManagement.WhereUsedResponse whereUsed(ModelObject[] arg0, int arg1,
			boolean arg2, ModelObject arg3) {
		return getService().whereUsed(arg0, arg1, arg2, arg3);
	}

//	@Override
//	public com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedResponse whereUsed(
//			WhereUsedInputData[] arg0, WhereUsedConfigParameters arg1) {
//		return getService().whereUsed(arg0, arg1);
//	}

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
    public DataManagementService getService() {
		return DataManagementService.getService(tcSession.getConnection());
	}

    @Override
	public com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedResponse whereUsed(
			WhereUsedInputData[] paramArrayOfWhereUsedInputData,
			WhereUsedConfigParameters paramWhereUsedConfigParameters) {
		return getService().whereUsed(paramArrayOfWhereUsedInputData, paramWhereUsedConfigParameters);
	}

    @Override
	public SaveAsObjectsResponse saveAsObjectAndRelate(com.teamcenter.services.strong.core._2011_06.DataManagement.SaveAsIn[] arg0,
			RelateInfoIn[] arg1) {
		return getService().saveAsObjectAndRelate(arg0, arg1);
	}
	@Override
	public GetDatasetTypesWithFileExtensionResponse getDatasetTypesWithFileExtension(
			String[] arg0) throws ServiceException {
		return getService().getDatasetTypesWithFileExtension(arg0);
	}
	@Override
	@Deprecated
	public TraceabilityReportOutput1 getTraceReport(
			TraceabilityInfoInput1[] arg0) {
		return getService().getTraceReport(arg0);
	}
	@Override
	public ServiceData refreshObjects2(ModelObject[] arg0, boolean arg1) {
		return getService().refreshObjects2(arg0, arg1);
	}
	@Override
	public GenerateNextValuesResponse generateNextValues(
			GenerateNextValuesIn[] arg0) {
		return getService().generateNextValues(arg0);
	}
	@Override
	public GetChildrenResponse getChildren(GetChildrenInputData[] arg0) {
		return getService().getChildren(arg0);
	}
	@Override
	public GetPasteRelationsResponse getPasteRelations(
			GetPasteRelationsInputData[] arg0) {
		return getService().getPasteRelations(arg0);
	}
	@Override
	public SubTypeNamesResponse getSubTypeNames(SubTypeNamesInput[] arg0) {
		return getService().getSubTypeNames(arg0);
	}
	@Override
	public ReviseObjectsResponse reviseObjects(ReviseIn[] arg0) {
		return getService().reviseObjects(arg0);
	}
	@Override
	public ValidateValuesResponse validateValues(ValidateInput[] arg0) {
		return getService().validateValues(arg0);
	}
	@Override
	public TraceabilityReportOutput2 getTraceReport2(
			TraceabilityInfoInput1[] arg0) {
		return getService().getTraceReport2(arg0);
	}
	@Override
	public TraceabilityReportOutputLegacy getTraceReportLegacy(
			TraceabilityInfoInput arg0) {
		return getService().getTraceReportLegacy(arg0);
	}
	@Override
	public ServiceData addChildren(ChildrenInputData[] arg0) {
		return getService().addChildren(arg0);
	}
	@Override
	public ServiceData removeChildren(ChildrenInputData[] arg0) {
		return getService().removeChildren(arg0);
	}
	@Override
	public GenerateIdsResponse generateIdsUsingIDGenerationRules(GenerateIdInput[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public com.teamcenter.services.strong.core._2014_10.DataManagement.GetDeepCopyDataResponse getDeepCopyData(com.teamcenter.services.strong.core._2014_10.DataManagement.DeepCopyDataInput[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public GetPasteRelationsResponse2 getPasteRelations2(GetPasteRelationsInputData[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ServiceData pruneNamedReferences(POM_object[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SaveAsObjectsResponse saveAsObjectsAndRelate(
			com.teamcenter.services.strong.core._2014_10.DataManagement.SaveAsIn[] arg0, RelateInfoIn[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CreateResponse createRelateAndSubmitObjects2(CreateIn2[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public GenerateNextValuesResponse generateNextValuesForProperties(PropertyNamingruleInfo[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CreatableSubBONamesResponse getCreatbleSubBuisnessObjectNames(CreatableSubBONamesInput[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public com.teamcenter.services.strong.core._2015_07.DataManagement.GetDeepCopyDataResponse getDeepCopyData(
			com.teamcenter.services.strong.core._2015_07.DataManagement.DeepCopyDataInput arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public DomainOfObjectOrTypeResponse getDomainOfObjectOrType(GetDomainInput[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public LocalizedPropertyValuesResponse getLocalizedProperties2(PropertyInfo[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ReassignParticipantResponse reassignParticipants(ReassignParticipantInfo[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public GenerateContextSpecificIDsResponse generateContextSpecificIDs(GenerateContextIDsInput[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ServiceData resetContextID(String[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SetPropsAndDetectOverwriteResponse setPropertiesAndDetectOverwrite(PropData[] arg0,
			Map<String, String[]> arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CreateAttachResponse createAttachAndSubmitObjects(CreateIn2[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CreateResponse createObjectsInBulkAndRelate(CreateIn3[] arg0) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ServiceData unlinkAndDeleteObjects(DeleteIn[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ServiceData createIdDisplayRules(IDDispRuleCreateIn[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public IDContextOutput getIdContexts(WorkspaceObject[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public IdentifierTypesOut getIdentifierTypes(IdentifierTypesIn[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public GenerateContextSpecificIDsResponse generateContextSpecificIDs2(GenerateContextIDsInput2[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
