package com.symc.work.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcDatasetUtil;
import com.symc.common.soa.biz.TcFileUtil;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.soa.util.TcUtil;
import com.symc.common.util.IFConstants;
import com.teamcenter.services.strong.core.DispatcherManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;

public class TcFileService {
    Session session;
    TcServiceManager tcServiceManager;
    TcFileUtil tcFileUtil;
    TcItemUtil tcItemUtil;
    TcDatasetUtil tcDatasetUtil;

    public TcFileService(Session session) {
        tcServiceManager = new TcServiceManager(session);
        tcFileUtil = new TcFileUtil(session);
        tcItemUtil = new TcItemUtil(session);
        tcDatasetUtil = new TcDatasetUtil(session);
        this.session = session;
    }

    /**
     * ImanFile File을 가지고온다.
     *
     * @method getFiles
     * @date 2013. 8. 6.
     * @param
     * @return File[]
     * @exception
     * @throws
     * @see
     */
    public File getFile(ImanFile imanFile) throws Exception {
        File[] files = tcFileUtil.getFiles(new ModelObject[] { imanFile });
        if(files != null && files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }

    /**
     * DATASET의 Iman File리스트를 가져온다.
     *
     * @method getImanFiles
     * @date 2013. 8. 6.
     * @param
     * @return HashMap<String,ImanFile[]>
     * @exception
     * @throws
     * @see
     */
    public HashMap<String, ImanFile[]> getImanFiles(ItemRevision ir) throws Exception {
        ModelObject[] relatedObjects = null;
        HashMap<String, ImanFile[]> datasetMap = new HashMap<String, ImanFile[]>();
        relatedObjects = getRelatedDataset(ir);
        for (ModelObject relobj : relatedObjects) {
            // CGR
            if (relobj.getTypeObject().getName().equals(IFConstants.TYPE_DATASET_CATCACHE)) {
                String[] reference_names = new String[] { IFConstants.TYPE_NR_CATCACHE };
                datasetMap.put(IFConstants.TYPE_DATASET_CATCACHE, tcDatasetUtil.getReferencedFileFromDataset(relobj.getUid(), reference_names));
            }
            // CATDrawing
            else if (relobj.getTypeObject().getName().equals(IFConstants.TYPE_DATASET_CATDRAWING)) {
                String[] reference_names = new String[] { IFConstants.TYPE_NR_CATDRAWING };
                datasetMap.put(IFConstants.TYPE_DATASET_CATDRAWING, tcDatasetUtil.getReferencedFileFromDataset(relobj.getUid(), reference_names));
            }
            // CATPart
            else if (relobj.getTypeObject().getName().equals(IFConstants.TYPE_DATASET_CATPART)) {
                String[] reference_names = new String[] { IFConstants.TYPE_NR_CATPART };
                datasetMap.put(IFConstants.TYPE_DATASET_CATPART, tcDatasetUtil.getReferencedFileFromDataset(relobj.getUid(), reference_names));
            }
        }
        return datasetMap;
    }

    /**
     * 연결 Dataset 리스트 조회
     *
     * @method getRelatedDataset
     * @date 2013. 8. 6.
     * @param
     * @return ModelObject[]
     * @exception
     * @throws
     * @see
     */
    public ModelObject[] getRelatedDataset(ItemRevision ir) throws Exception {
        ArrayList<ModelObject> datasets = new ArrayList<ModelObject>();
        // JT : TcConstants.RELATION_RENDERING 는 조회하지 않으므로 추후 조회시에는 Properties에 추가 할것.
        tcItemUtil.getProperties(new ModelObject[] { ir }, new String[] { TcConstants.RELATION_REFERENCES, TcConstants.RELATION_SPECIFICATION });
        // relatedObject =
        // modelobj[0].getProperty(TcConstants.RELATION_SPECIFICATION).getModelObjectArrayValue();
        ModelObject[] relatedReferenceObject = ir.getPropertyObject(TcConstants.RELATION_REFERENCES).getModelObjectArrayValue();
        for (int i = 0; i < relatedReferenceObject.length; i++) {
            if(relatedReferenceObject[i] instanceof Dataset) {
                datasets.add(relatedReferenceObject[i]);
            }
        }
        ModelObject[] relatedSpecificationObject = ir.getPropertyObject(TcConstants.RELATION_SPECIFICATION).getModelObjectArrayValue();
        for (int i = 0; i < relatedSpecificationObject.length; i++) {
            if(relatedSpecificationObject[i] instanceof Dataset) {
                datasets.add(relatedSpecificationObject[i]);
            }
        }
        return datasets.toArray(new ModelObject[datasets.size()]);
    }

    /**
     * Dispatcher Server에 CGR Request.
     *
     * @param session
     * @param revision
     *            CATPart가 들어있는 아이템 리비전
     * @throws Exception
     */
    public ModelObject createCGR(ItemRevision revision) throws Exception {
        ModelObject[] dataset = this.getRelatedDataset(revision);
        ModelObject[] catpartDataset = null;
        if (dataset != null) {
            for (int i = 0; i < dataset.length; i++) {
                if (IFConstants.TYPE_DATASET_CATPART.equals(dataset[i].getTypeObject().getName())) {
                    catpartDataset = new ModelObject[] { dataset[i] };
                    break;
                }
            }
        }
        if (catpartDataset != null) {
            DispatcherManagementService dispatcherMgmtService = DispatcherManagementService.getService(this.session.getConnection());
            com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestArgs args[] = new com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestArgs[1];
            args[0] = new com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestArgs();
            args[0].providerName = "SIEMENS";
            args[0].serviceName = "catparttocgr";
            args[0].priority = 1;
            args[0].interval = -1;
            args[0].primaryObjects = catpartDataset;
            args[0].secondaryObjects = new ModelObject[] { revision };
            com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestResponse responseObject = dispatcherMgmtService.createDispatcherRequest(args);
            if (!tcServiceManager.getDataService().ServiceDataError(responseObject.svcData)) {
                return responseObject.requestsCreated[0];
            } else {
                throw new Exception(TcUtil.makeMessageOfFail(responseObject.svcData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
            }
        }
        return null;
    }
}
