package com.ssangyong.soa.tcservice;

import com.ssangyong.soa.biz.Session;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.strong.core._2008_06.DataManagement.SaveAsNewItemInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.SaveAsNewItemResponse2;
import com.teamcenter.services.strong.core._2011_06.DataManagement.SaveAsObjectsResponse;
import com.teamcenter.services.strong.core._2012_09.DataManagement.RelateInfoIn;
import com.teamcenter.services.strong.core._2013_05.DataManagement.ReviseObjectsResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;

public class TcDataManagementInernalService extends DataManagementService {

	private Session tcSession = null;

    @Override
    public GetSubscribableTypesAndSubtypesResponse getSubscribableTypesAndSubTypes(String arg0) {
        return getService().getSubscribableTypesAndSubTypes(arg0);
    }


    @Override
    public CreateRelationsResponse createCachedRelations(Relationship[] arg0) {
        return getService().createCachedRelations(arg0);
    }


    @Override
    public MultiRelationMultiLevelExpandResponse multiRelationMultiLevelExpand(MultiRelMultiLevelExpandInput arg0) {
        return getService().multiRelationMultiLevelExpand(arg0);
    }

    /**
     * revise Object
     */
    @Override
    public ReviseResponse2 reviseObject(ReviseInfo[] arg0, boolean arg1) {
        return getService().reviseObject(arg0, arg1);
    }


    @Override
    public SaveAsNewItemResponse2 saveAsNewItemObject(SaveAsNewItemInfo[] arg0, boolean arg1) {
        return getService().saveAsNewItemObject(arg0, arg1);
    }


    @Override
    public ServiceData setDefaultProjectForProjectMembers(ModelObject arg0, ModelObject[] arg1) {
        return getService().setDefaultProjectForProjectMembers(arg0, arg1);
    }


    @Override
    public GetViewableDataResponse getViewableData(ModelObject[] arg0, String[] arg1, String arg2) throws ServiceException {
        return getService().getViewableData(arg0, arg1, arg2);
    }


    @Override
    public DatasetFilesResponse getDatasetFiles(DatasetFileQueryInfo[] arg0, boolean arg1) {
        return getService().getDatasetFiles(arg0, arg1);
    }


    @Override
    public WhereUsedResponseOccGroup whereUsedOccGroup(ModelObject[] arg0, int arg1, boolean arg2, ModelObject arg3) {
        return getService().whereUsedOccGroup(arg0, arg1, arg2, arg3);
    }


    @Override
    public GetAttributeValuesResponse getAttributeValues(GetAttributeValuesInputData[] arg0) throws ServiceException {
        return getService().getAttributeValues(arg0);
    }


    @Override
    public GetOrganizationInformationResponse getOrganizationInformation(GetOrganizationInformationInputData[] arg0) throws ServiceException {
        return getService().getOrganizationInformation(arg0);
    }
    
    public TcDataManagementInernalService(Session tcSession) {
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
    
    public DataManagementService getService() {
        return DataManagementService.getService(this.tcSession.getConnection());
    }


	@Override
	public CreateResponse2 createRelateAndSubmitObjects(CreateIn2[] arg0)
			throws ServiceException {
		return getService().createRelateAndSubmitObjects(arg0);
	}


	@Override
	public ReviseObjectsResponse reviseObjectsInBulk(ReviseIn[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SaveAsObjectsResponse saveAsObjectsInBulkAndRelate(SaveAsIn[] arg0, RelateInfoIn[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public TCSessionAndAnalyticsInfo getTCSessionAnalyticsInfo(String[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public GenerateDatasetNameResponse generateDatasetName(GenerateDsNameInput[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
