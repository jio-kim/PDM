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
package com.teamcenter.ets.translator.ugs.cgrtojt;

//==== Imports  ================================================================
import java.io.File;

import com.teamcenter.ets.extract.DefaultTaskPrep;
import com.teamcenter.ets.request.TranslationRequest;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.translationservice.task.TranslationTask;

//==== Class ===================================================================
public class TaskPrep extends DefaultTaskPrep {

	public TranslationTask prepareTask() throws Exception {

		TranslationTask zTransTask = null;

		ModelObject[] primary_objs = this.request.getPropertyObject("primaryObjects").getModelObjectArrayValue();
		ModelObject[] secondary_objs = this.request.getPropertyObject("secondaryObjects").getModelObjectArrayValue();

		String exportFilename = null;

		for (int i = 0; i < primary_objs.length; i++) {
			Dataset dataset = (Dataset) primary_objs[i];

			if (!dataset.getTypeObject().getName().equalsIgnoreCase("CATCache")) {
				continue;
			}

			ItemRevision itemRev = (ItemRevision) secondary_objs[i];

			DataManagementService dataMgtService = DataManagementService.getService(SoaHelper.getSoaConnection());

			ServiceData serviceData = dataMgtService.getProperties(new ModelObject[] { dataset }, new String[] { "ref_list" });
			dataset = (Dataset) serviceData.getPlainObject(0);

			Property refListProperty = dataset.getPropertyObject("ref_list");

			ModelObject[] refObjs = refListProperty.getModelObjectArrayValue();

			ImanFile zIFile = null;

			//         if (refObjs[0] instanceof ImanFile) {
			//        	 ImanFile[] files = new ImanFile[1];
			//        	 files[0] = (ImanFile) refObjs[0];
			//        	 
			//        	 zIFile = (ImanFile) refObjs[0];
			//         }

			// 20141231. HY. refObjs가 여러개. 기존에 refObjs[0]으로 사용하던 것을 변경 함
			if(refObjs != null) {
				for(int j = 0; j < refObjs.length; j++) {
					if (refObjs[j] instanceof ImanFile) {
						//        			 ImanFile[] files = new ImanFile[1];
						//        			 files[0] = (ImanFile) refObjs[j];
						zIFile = (ImanFile) refObjs[j];
					} else {
						System.out.println("type of refObjs[" + j + "] : " + refObjs[j].getClass().getName()); 
					}
				}
			}     

			File zFile = TranslationRequest.getFileToStaging(zIFile, stagingLoc);

			Util ut = new Util();

			String fileName = zFile.getName();

			int index = fileName.lastIndexOf(m_scSourceFileExt);
			String fileNameExtTrim = fileName.substring(0, index);

			exportFilename = zFile.getName();
			// exportFilename = fileNameExtTrim;

			String s = "\nfileName : " + fileName + "\nfileNameExtTrm : " + fileNameExtTrim + "\nexportFilename :" + exportFilename;
			ut.setLog(s);

			zTransTask = addRefIdToTask(prepTransTask(zTransTask, dataset, itemRev, exportFilename, true, true, ".jt", 0, null), i);
		}

		String out = stagingLoc + "\\result";

		return addOptions(zTransTask, "outputpath", out);
	}
}
