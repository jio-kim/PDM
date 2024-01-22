/*==============================================================================
 Copyright 2009.
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
================================================================================
File description: Custom TaskPrep class for cgrtojt translator. This sub class
                  prepares a cgrtojt translation task. This is a configuration
                  specified class based on provider name and translator name in
                  DispatcherClient property file which creates the cgr specific
                  translation request by preparing the data for translation and
                  creating the Translation request object.

        Filename:   TaskPrep.java
================================================================================*/

//==== Package  ================================================================
package com.teamcenter.ets.translator.ugs.catparttocgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import com.teamcenter.ets.extract.DefaultTaskPrep;
import com.teamcenter.ets.request.TranslationRequest;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.ets.soa.TeamcenterServerProxy;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.translationservice.task.TranslationTask;

public class TaskPrep extends DefaultTaskPrep {

	private boolean isDebug = false;

	public TranslationTask prepareTask() throws Exception {

		Properties prop = Util.getDefaultProperties();
		try {
			isDebug = new Boolean(prop.getProperty("isDebug"));
		} catch (Exception e) {
			m_zTaskLogger.info("Could not find 'isDebug' property.");
		}

		if( isDebug ){
			m_zTaskLogger.info("========================= prepareTask Start ======================");
		}

//		m_scSourceFileExt = ".CATPart";

		TranslationTask zTransTask = new TranslationTask();

		ModelObject[] primary_objs = this.request.getPropertyObject(
				"primaryObjects").getModelObjectArrayValue();
		ModelObject[] secondary_objs = this.request.getPropertyObject(
				"secondaryObjects").getModelObjectArrayValue();
		
		//CGR파일 존재 할 경우, CGR생성하지 않도록 수정====================.
		for (int i = 0; i < secondary_objs.length; i++) {
			ItemRevision revision = (ItemRevision) secondary_objs[i];
			ArrayList<ModelObject> objList = getCgrDataset(revision);
			if( !objList.isEmpty()){
				//CGR이 이미 존재하므로 CGR파일을 생성하지 않음.
				SoaHelper.getProperties(revision, new String[]{"item_id", "item_revision_id", });
				if( isDebug ){
					SoaHelper.getProperties(revision, new String[]{"item_id", "item_revision_id"});
					m_zTaskLogger.info("========================= CGR 파일이 존재함.[START] ======================");
					m_zTaskLogger.info("Item ID : " + revision.get_item_id());
					m_zTaskLogger.info("Item Revision ID : " + revision.get_item_revision_id());
					m_zTaskLogger.info("========================= CGR 파일이 존재함.[END] ======================");
				}
				return zTransTask;
			}
		}
		//================================================
		
		for (int i = 0; i < primary_objs.length; i++) {
			Dataset dataset = (Dataset) primary_objs[i];

			if (!dataset.getTypeObject().getName().equalsIgnoreCase("CATPart")) {
				continue;
			}

			ItemRevision itemRev = (ItemRevision) secondary_objs[i];
			dataset = (Dataset) SoaHelper.getProperties(dataset, "ref_list");
			ModelObject[] contexts = dataset.get_ref_list();
			ImanFile zIFile = null;

			for (int j = 0; j < contexts.length; j++) {
				// get file extension and compare.
				if (contexts[j] instanceof ImanFile) {
					zIFile = (ImanFile) contexts[j];
					zIFile = (ImanFile) SoaHelper.getProperties(zIFile, "file_ext");
					String scFileExt = zIFile.get_file_ext();
					if (scFileExt.equalsIgnoreCase("CATPart")) {
						break;
					}
				}

				zIFile = null;
			}

			if (zIFile == null) {
				throw new Exception("No named reference found for "
						+ dataset.get_object_string());
			}

			File zFile = TranslationRequest.getFileToStaging(zIFile, stagingLoc);

			String fileName = zFile.getName();
			addOptions(zTransTask, "outputdir", stagingLoc + "\\result");
			zTransTask = addRefIdToTask(
					prepTransTask(zTransTask, dataset, itemRev, fileName, true,
							true, ".cgr", 0, null), i);
		}

		if( isDebug ){
			m_zTaskLogger.info("stagingLoc : " + stagingLoc);
			m_zTaskLogger.info("========================= prepareTask End ======================");
		}
		
		return zTransTask;
	}
	
    /**
     * Revision ir이 CGR Dataset을 가지고 있으면 해당 Dataset을 ArrayList타입으로 리턴.
     * 
     * @param ir
     * @return
     * @throws Exception
     */
    public ArrayList<ModelObject> getCgrDataset(ItemRevision ir) throws Exception {
        ArrayList<ModelObject> datasets = new ArrayList<ModelObject>();
        SoaHelper.getProperties(ir, "IMAN_reference");
        ModelObject[] relatedReferenceObject = ir.getPropertyObject("IMAN_reference").getModelObjectArrayValue();
        for (int i = 0; i < relatedReferenceObject.length; i++) {
            if(relatedReferenceObject[i] instanceof Dataset) {
            	SoaHelper.getProperties(relatedReferenceObject[i], "object_type");
				String typeStr = ((Dataset) relatedReferenceObject[i]).get_object_type();
				if( typeStr.equalsIgnoreCase("CATCache")){
					datasets.add(relatedReferenceObject[i]);
				}
            }
        }
        return datasets;
    }
}
